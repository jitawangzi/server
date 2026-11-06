package cn.game.simulation.test.ai;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.protocol.protobuf.TestMsg;
import cn.game.simulation.client.Client;

/**
 * 从当前认证玩家连接回传处理节点，在场景前后核对；不把客户端配置当作实际实例。
 * 报告只证明两次探针的目标及该场景退出结果，跨服业务处理需单独提供路由证据。
 */
public final class ScenarioRuntimeEvidence {
    /** 普通 JSON 解析器，不加载配置中指定的类型。 */
    private static final ObjectMapper JSON = new ObjectMapper();
    /** 期望的发布字段，加载一次避免测试期间清单被替换。 */
    private final JsonNode expected;
    /** HTTP 前观察返回的实际实例，场景探针必须命中它。 */
    private final String expectedInstance;
    /** 单次运行标识，避免读取上一次场景留下的文件。 */
    private final String runId;
    /** 场景结果文件，由入口在独立临时目录中分配。 */
    private final Path output;
    /** 初次探针的实际响应。 */
    private JsonNode before;
    /** 最近收到的响应，验证失败时也保存白名单字段。 */
    private JsonNode lastResponse;

    /** @param expected 清单 @param instance HTTP观察实例 @param runId 本次标识 @param output 输出路径 */
    private ScenarioRuntimeEvidence(JsonNode expected, String instance, String runId, Path output) {
        this.expected = expected;
        this.expectedInstance = instance;
        this.runId = runId;
        this.output = output;
    }

    /** @return 未启用时为空；只传部分参数时拒绝运行 */
    public static ScenarioRuntimeEvidence configured() throws Exception {
        String path = System.getProperty("scenario.runtime.expected");
        if (path == null) { return null; }
        String instance = System.getProperty("scenario.runtime.instance", "");
        String runId = System.getProperty("scenario.runtime.runId", "");
        String output = System.getProperty("scenario.runtime.output", "");
        if (instance.isBlank() || !runId.matches("[0-9a-f]{32}") || output.isBlank()
                || System.getProperty("scenario.logicServerId", "").isBlank()) {
            throw new IllegalArgumentException("SCENARIO_RUNTIME_ARGUMENTS_INVALID");
        }
        return new ScenarioRuntimeEvidence(JSON.readTree(Files.readString(Path.of(path))), instance, runId, Path.of(output));
    }

    /** @param client 已完成初始化的真实玩家连接 */
    public void before(Client client) throws Exception {
        try {
            before = query(client);
            write(null, false, "STARTED");
        } catch (Exception e) {
            before = lastResponse;
            try { write(null, false, "FAILED"); } catch (Exception reportFailure) { e.addSuppressed(reportFailure); }
            throw e;
        }
    }

    /** @param client 同一认证连接 @param succeeded 场景本身是否成功；即使失败仍尝试保存后观察 */
    public void after(Client client, boolean succeeded) throws Exception {
        JsonNode after = null;
        String status = "FAILED";
        try {
            after = query(client);
            for (String field : List.of("instanceId", "startedAt", "targetId", "playerId", "logicServerId")) {
                if (!before.path(field).equals(after.path(field))) { throw new IllegalStateException("SCENARIO_RUNTIME_CHANGED:" + field); }
            }
            if (before.path("challenge").equals(after.path("challenge"))) { throw new IllegalStateException("SCENARIO_CHALLENGE_REUSED"); }
            status = "PROBES_MATCH";
        } catch (Exception e) {
            try { write(lastResponse, succeeded, status); } catch (Exception reportFailure) { e.addSuppressed(reportFailure); }
            throw e;
        }
        write(after, succeeded, status);
    }

    /** @param client 当前连接 @return 由服务端实际处理并验证过的身份 */
    private JsonNode query(Client client) throws Exception {
        lastResponse = null;
        if (!client.getInit()) { throw new IllegalStateException("SCENARIO_LOGIN_NOT_READY"); }
        String nonce = UUID.randomUUID().toString().replace("-", "");
        var request = TestMsg.TestGmCmdRequest_6f000001.newBuilder().setCmd("runtimeIdentity " + nonce).build();
        TestMsg.TestGmCmdResponse_6f000002 response = (TestMsg.TestGmCmdResponse_6f000002) client.sendProtocolAndWait(request, 30);
        String body = response.getDebugInfo();
        if (body.isBlank() || body.length() > 16384) { throw new IllegalStateException("SCENARIO_RUNTIME_RESPONSE_INVALID"); }
        JsonNode actual = JSON.readTree(body);
        lastResponse = actual;
        verify(expected, actual, nonce, expectedInstance, Long.toString(client.getPlayerId()), System.getProperty("scenario.logicServerId", ""));
        return actual;
    }

    /**
     * 验证服务回传字段；配置节点名不替代实际玩家身份和实例。
     * @param expected 期望清单 @param actual 实际响应 @param nonce 本次挑战
     * @param instance HTTP观察实例 @param playerId 当前认证玩家
     */
    public static void verify(JsonNode expected, JsonNode actual, String nonce, String instance, String playerId, String logicServerId) {
        if (actual == null || !actual.isObject() || !"1.0".equals(expected.path("schemaVersion").asText())) {
            throw new IllegalStateException("SCENARIO_RUNTIME_RESPONSE_INVALID");
        }
        for (String field : List.of("schemaVersion", "targetId", "buildId", "sourceDigest", "artifactSha256", "configSha256", "protocolSha256", "dataNamespace")) {
            if (!expected.path(field).isTextual() || expected.path(field).asText().isBlank()
                    || !expected.path(field).equals(actual.path(field))) { throw new IllegalStateException("SCENARIO_RUNTIME_MISMATCH:" + field); }
        }
        if (!nonce.equals(actual.path("challenge").asText()) || !instance.equals(actual.path("instanceId").asText())
                || !playerId.equals(actual.path("playerId").asText()) || logicServerId.isBlank()
                || !logicServerId.equals(actual.path("logicServerId").asText())
                || actual.path("startedAt").asText().isBlank()) { throw new IllegalStateException("SCENARIO_RUNTIME_BINDING_INVALID"); }
    }

    /** @param after 后观察或空 @param succeeded 场景结果 @param status 探针核验状态 */
    private void write(JsonNode after, boolean succeeded, String status) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("schemaVersion", "1.0");
        report.put("runId", runId);
        report.put("status", status);
        report.put("scenarioSucceeded", succeeded);
        report.put("scope", "AUTHENTICATED_GAME_NODE_PROBES");
        report.put("before", sanitize(before));
        report.put("after", sanitize(after));
        Files.writeString(output, JSON.writeValueAsString(report));
    }

    /** 失败响应也仅保留契约字段，避免任意服务返回内容进入证据。 */
    private static JsonNode sanitize(JsonNode response) {
        if (response == null || !response.isObject()) { return null; }
        var safe = JSON.createObjectNode();
        for (String key : List.of("schemaVersion", "targetId", "buildId", "sourceDigest", "artifactSha256", "configSha256", "protocolSha256",
                "dataNamespace", "instanceId", "startedAt", "challenge", "playerId", "logicServerId")) {
            if (response.path(key).isTextual() && response.path(key).asText().length() <= 200) { safe.set(key, response.get(key)); }
        }
        return safe;
    }
}
