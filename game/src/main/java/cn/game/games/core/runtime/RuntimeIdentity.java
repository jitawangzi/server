package cn.game.games.core.runtime;

import cn.game.core.runtime.RuntimeIdentityInventory;

import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.AsyncUtils;
import io.vertx.core.http.HttpServer;

/**
 * 本地测试专用的运行身份；HTTP 与玩家协议探针共享同一进程快照。
 * 启动前后输入必须一致；更新配置或制品后保持拒绝，要求重新启动并生成新期望。
 */
public final class RuntimeIdentity {
    /** 只记录故障，不输出配置正文。 */
    private static final Logger log = LoggerFactory.getLogger(RuntimeIdentity.class);
    /** 使用普通 JSON，不启用多态类型解析。 */
    private static final ObjectMapper JSON = new ObjectMapper();
    /** 当前进程唯一的运行观察状态。 */
    private static volatile RuntimeIdentity current;
    /** 实际输入读取器。 */
    private final RuntimeIdentityInventory inventory;
    /** 在业务初始化之前采集，成功就绪后再次核对。 */
    private final Map<String, String> baseline;
    /** 进程实例标识，不接受外部覆盖。 */
    private final String instanceId = UUID.randomUUID().toString();
    /** JVM 真实启动时间，不使用可调整的游戏业务时间。 */
    private final String startedAt = Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime()).toString();
    /** 只有完整业务初始化成功才允许返回身份。 */
    private volatile boolean ready;
    /** 发现漂移后不允许恢复原文件伪造连续运行。 */
    private volatile boolean invalid;
    /** 只绑定回环地址的 HTTP 服务。 */
    private HttpServer server;

    /** @param targetId 真实启动节点 @param root 独立制品目录 */
    private RuntimeIdentity(String targetId, Path root) throws Exception {
        inventory = new RuntimeIdentityInventory(root, true);
        baseline = Map.copyOf(inventory.capture(targetId));
    }

    /**
     * 必须在业务配置加载前调用；默认不开启，也不允许生产模式启用。
     * @param targetId 当前 GameServer 解析出的节点名
     */
    public static void prepare(String targetId) throws Exception {
        cn.game.core.runtime.RuntimeStartup.verify(targetId);
        if (!Boolean.getBoolean("game.runtimeIdentity.enabled")) { return; }
        if (!cn.game.util.runtime.FrozenApollo.enabled()) { throw new IllegalStateException("RUNTIME_CONFIG_FREEZE_REQUIRED"); }
        if (!"test".equalsIgnoreCase(System.getProperty("server.run.mode"))) {
            throw new IllegalStateException("RUNTIME_IDENTITY_REQUIRES_TEST_MODE");
        }
        current = new RuntimeIdentity(targetId, Path.of(System.getProperty("game.runtimeIdentity.root", "")));
    }

    /** 业务启动完成后核对输入并开启本机只读接口，失败使本次启动失败。 */
    public static void ready() throws Exception {
        RuntimeIdentity state = current;
        if (state == null) { return; }
        state.assertStable();
        int port = Integer.parseInt(System.getProperty("game.runtimeIdentity.port", "17011"));
        if (port < 1024 || port > 65535) { throw new IllegalArgumentException("RUNTIME_PORT_INVALID"); }
        state.server = VxHolder.vertx.createHttpServer();
        state.server.requestHandler(request -> {
            String challenge = request.getParam("challenge");
            if (!"/runtime-identity".equals(request.path()) || request.method() != io.vertx.core.http.HttpMethod.GET
                    || !validChallenge(challenge)) {
                request.response().setStatusCode(400).end("RUNTIME_REQUEST_INVALID");
                return;
            }
            // 文件摘要计算移到工作线程，不占用网络事件循环。
            VxHolder.vertx.executeBlocking(() -> JSON.writeValueAsString(observe(challenge))).onComplete(result -> {
                request.response().putHeader("Cache-Control", "no-store");
                if (result.succeeded()) {
                    request.response().putHeader("Content-Type", "application/json").end(result.result());
                } else {
                    log.warn("运行身份观察失败", result.cause());
                    request.response().setStatusCode(503).end("RUNTIME_IDENTITY_UNVERIFIED");
                }
            });
        });
        state.ready = true;
        try { AsyncUtils.await(state.server.listen(port, "127.0.0.1")); }
        catch (Exception e) { state.ready = false; log.error("运行身份接口启动失败", e); throw e; }
    }

    /** @param value 每次观察独立生成的32位十六进制挑战 @return 是否符合接口边界 */
    public static boolean validChallenge(String value) { return value != null && value.matches("[0-9a-f]{32}"); }

    /**
     * HTTP 与真实玩家处理入口共用，返回实际进程身份和当前挑战。
     * @param challenge 本次独立挑战
     * @return 仅含摘要及非敏感标识的身份
     */
    public static Map<String, String> observe(String challenge) throws Exception {
        if (!validChallenge(challenge)) { throw new IllegalArgumentException("RUNTIME_CHALLENGE_INVALID"); }
        RuntimeIdentity state = current;
        if (state == null || !state.ready) { throw new IllegalStateException("RUNTIME_IDENTITY_DISABLED_OR_NOT_READY"); }
        state.assertStable();
        Map<String, String> identity = new LinkedHashMap<>(state.baseline);
        identity.put("instanceId", state.instanceId);
        identity.put("startedAt", state.startedAt);
        identity.put("challenge", challenge);
        return identity;
    }

    /** 不允许当前文件或配置与启动快照发生漂移。 */
    private synchronized void assertStable() throws Exception {
        if (invalid) { throw new IllegalStateException("RUNTIME_INPUTS_CHANGED"); }
        try {
            if (!baseline.equals(inventory.capture(baseline.get("targetId")))) {
                throw new IllegalStateException("RUNTIME_INPUTS_CHANGED");
            }
        } catch (Exception e) { invalid = true; log.warn("运行身份输入已无法确认，需重启重新取证", e); throw e; }
    }

    /** 停服先撤销就绪，防止关闭窗口继续得到有效身份。 */
    public static void stop() {
        RuntimeIdentity state = current;
        if (state != null) {
            state.ready = false;
            if (state.server != null) { state.server.close().onFailure(e -> log.warn("运行身份接口关闭失败", e)); }
        }
    }
}
