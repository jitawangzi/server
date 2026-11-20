package cn.game.games.net.game.gm.acceptance;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.core.id.IdUtil;
import cn.game.core.id.SegmentIdService;
import cn.game.core.id.SnowflakeService;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.socket.controller.DispatcherImpl;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.GmDispatcher;
import cn.game.games.net.game.gm.GmResult;
import cn.game.games.net.game.gm.PlayerInspectService;
import cn.game.games.net.game.gm.RequestTraceService;
import cn.game.games.net.game.gm.proto.ProtocolSimulationService;
import cn.game.games.net.game.gm.proto.ProtocolSimulationService.ProtoInvokeRequest;
import cn.game.util.IdWorker;
import io.vertx.core.Future;

/**
 * 进程内黑白盒操作与断言工具包。
 * 封装协议模拟分发、GM指令执行、模块状态探查与标准验收断言原语。
 * 内置内联同步处理器以保证在无完整服务容器或纯单测环境下100%可测。
 *
 * @author shuyongqiang
 */
public class InProcessAcceptanceKit {

    private static final Logger log = LoggerFactory.getLogger(InProcessAcceptanceKit.class);

    private static volatile ProtocolSimulationService fallbackSimService;

    static {
        ensureIdUtilInitialized();
    }

    /**
     * 确保测试环境下的分布式 ID 服务与雪花算法已初始化
     */
    public static void ensureIdUtilInitialized() {
        try {
            Field sfField = IdUtil.class.getDeclaredField("snowflakeService");
            sfField.setAccessible(true);
            if (sfField.get(null) == null) {
                SnowflakeService sf = new SnowflakeService("sandbox-test-server");
                Field workerField = SnowflakeService.class.getDeclaredField("idWorker");
                workerField.setAccessible(true);
                workerField.set(sf, new IdWorker(1, 0));
                sfField.set(null, sf);
            }
            Field segField = IdUtil.class.getDeclaredField("segmentService");
            segField.setAccessible(true);
            if (segField.get(null) == null) {
                segField.set(null, new SegmentIdService());
            }
        } catch (Throwable t) {
            log.warn("初始化测试 IdUtil 警告", t);
        }
    }

    /**
     * 内联同步沙盒邮箱处理器替身，用于在纯单测或无 Vert.x 容器环境下同步执行协议
     */
    public static class InlineSandboxProcessor implements Processor {

        @Override
        public void process(NetClient gameClient, IProtocol<?> protocol) {
            if (protocol != null) {
                new DispatcherImpl().dispatch(gameClient, protocol);
            }
        }

        @Override
        public void process(Runnable task) {
            if (task != null) {
                task.run();
            }
        }

        @Override
        public void process(long objectId, NetClient gameClient, IProtocol<?> protocol) {
            process(gameClient, protocol);
        }

        @Override
        public void process(long objectId, Runnable task) {
            process(task);
        }

        @Override
        public void process(long objectId, Runnable task, boolean fast) {
            process(task);
        }

        @Override
        public <T> T process(long objectId, Callable<T> supplier) {
            try {
                return supplier != null ? supplier.call() : null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public <T, R> Future<T> process(long objectId, Callable<R> supplier, Function<R, Future<T>> mapper) {
            try {
                R result = supplier != null ? supplier.call() : null;
                return mapper != null ? mapper.apply(result) : Future.succeededFuture(null);
            } catch (Exception e) {
                return Future.failedFuture(e);
            }
        }
    }

    /**
     * 获取适合当前运行环境的协议模拟分发服务
     *
     * @return 协议模拟执行服务
     */
    public static ProtocolSimulationService getSimulationService() {
        try {
            if (cn.game.util.SpringContextLoader.getContext() != null && ServerContext.getInstance().getProcessor() != null) {
                return ProtocolSimulationService.getInstance();
            }
        } catch (Throwable t) {
            log.warn("无法从Spring容器获取Processor，使用内联沙盒处理器替身: {}", t.getMessage());
        }
        if (fallbackSimService == null) {
            synchronized (InProcessAcceptanceKit.class) {
                if (fallbackSimService == null) {
                    fallbackSimService = new ProtocolSimulationService(new InlineSandboxProcessor(), null, null);
                }
            }
        }
        return fallbackSimService;
    }

    /**
     * 确保指定模块的业务协议处理器已完成注册
     *
     * @param moduleId 模块号
     * @param supplier 处理器单例创建器
     */
    public static void ensureHandlerRegistered(int moduleId, Supplier<BaseHandler> supplier) {
        if (!DispatcherImpl.getModuleHandlers().containsKey(moduleId)) {
            try {
                BaseHandler handler = supplier.get();
                Field df = BaseHandler.class.getDeclaredField("dispatcher");
                df.setAccessible(true);
                df.set(handler, new DispatcherImpl());
                handler.init();
            } catch (Exception t) {
                log.warn("注册协议处理器异常: moduleId={}", moduleId, t);
            }
        }
    }

    /**
     * 模拟分发业务协议并同步等待返回
     *
     * @param playerId 目标玩家ID
     * @param msgId 协议ID
     * @param payload 协议结构体参数
     * @return 结构化执行结果字典
     */
    public static Map<String, Object> invokeProto(long playerId, int msgId, Map<String, Object> payload) {
        ProtoInvokeRequest req = new ProtoInvokeRequest();
        req.setPlayerId(playerId);
        req.setMsgId(msgId);
        req.setPayload(payload != null ? payload : Collections.emptyMap());
        req.setWithTrace(true);
        req.setWithDiff(true);
        req.setTimeoutMs(5000);

        return getSimulationService().invoke(req);
    }

    /**
     * 在沙盒环境中执行GM指令
     *
     * @param playerId 目标玩家ID
     * @param command GM文本指令
     * @return GM执行结果
     */
    public static GmResult gm(long playerId, String command) {
        return GmDispatcher.getInstance().dispatchHttpText(playerId, command, null, false);
    }

    /**
     * 探查指定模块的内部状态字段
     *
     * @param player 目标玩家
     * @param moduleName 模块名
     * @param dotPath 字段点分路径
     * @return 探查到的字段值
     */
    public static Object inspectModuleField(Player player, String moduleName, String dotPath) {
        if (player == null || dotPath == null || dotPath.isEmpty()) {
            return null;
        }
        Map<String, Object> snapshot = PlayerInspectService.getInstance().captureModulesSnapshot(player, Collections.singletonList(moduleName));
        if (snapshot == null) {
            return null;
        }
        Object curr = snapshot.get(moduleName);
        if (curr == null) {
            curr = snapshot;
        }
        String[] parts = dotPath.split("\\.");
        for (String part : parts) {
            if (curr instanceof Map) {
                curr = ((Map<?, ?>) curr).get(part);
            } else if (curr != null) {
                try {
                    Field f = curr.getClass().getDeclaredField(part);
                    f.setAccessible(true);
                    curr = f.get(curr);
                } catch (Exception e) {
                    log.warn("探查模块字段反射异常: dotPath={}, part={}", dotPath, part, e);
                    return null;
                }
            } else {
                return null;
            }
        }
        return curr;
    }

    /**
     * 断言协议调用执行成功且无错误事件
     *
     * @param invokeResult 协议模拟执行结果
     */
    public static void assertProtocolSuccess(Map<String, Object> invokeResult) {
        if (invokeResult == null) {
            throw new AssertionError("协议调用结果为空");
        }
        Object errObj = invokeResult.get("errorCode");
        int errCode = (errObj instanceof Number) ? ((Number) errObj).intValue() : -1;
        if (errCode != 0) {
            Object desc = invokeResult.get("errorDesc");
            String errDesc = desc != null ? String.valueOf(desc) : String.valueOf(invokeResult.get("message"));
            throw new AssertionError("协议执行返回业务错误码: " + errCode + ", errorName=" + invokeResult.get("errorName") + ", desc=" + errDesc);
        }
        Object traceObj = invokeResult.get("trace");
        if (traceObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> trace = (Map<String, Object>) traceObj;
            Object errors = trace.get("errorEvents");
            if (errors instanceof List && !((List<?>) errors).isEmpty()) {
                throw new AssertionError("协议执行轨迹中捕获到服务端异常事件: " + errors);
            }
        }
    }

    /**
     * 断言协议返回了期望的业务拦截错误码
     *
     * @param invokeResult 协议模拟执行结果
     * @param expectedErrorCode 期望错误码
     */
    public static void assertErrorCode(Map<String, Object> invokeResult, int expectedErrorCode) {
        if (invokeResult == null) {
            throw new AssertionError("协议调用结果为空");
        }
        Object errObj = invokeResult.get("errorCode");
        int actualCode = (errObj instanceof Number) ? ((Number) errObj).intValue() : -1;
        if (actualCode != expectedErrorCode) {
            Object pushesObj = invokeResult.get("pushes");
            if (pushesObj instanceof List) {
                for (Object p : (List<?>) pushesObj) {
                    if (p instanceof Map) {
                        Map<?, ?> pMap = (Map<?, ?>) p;
                        Object pErr = pMap.get("errorCode");
                        if (pErr instanceof Number && ((Number) pErr).intValue() == expectedErrorCode) {
                            return;
                        }
                    }
                }
            }
            Object desc = invokeResult.get("errorDesc");
            String errDesc = desc != null ? String.valueOf(desc) : String.valueOf(invokeResult.get("errorName"));
            throw new AssertionError("期望业务错误码 " + expectedErrorCode + "，实际返回: " + actualCode + " (" + errDesc + ")");
        }
    }

    /**
     * 断言指定资源/道具净变动量
     *
     * @param invokeResult 协议模拟执行结果
     * @param configId 资源配置ID
     * @param expectedSignedDelta 期望带符号净增量（正增负减）
     */
    public static void assertResourceDelta(Map<String, Object> invokeResult, int configId, long expectedSignedDelta) {
        if (invokeResult == null) {
            throw new AssertionError("协议调用结果为空");
        }
        long actualDelta = 0;
        Object traceObj = invokeResult.get("trace");
        if (traceObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> trace = (Map<String, Object>) traceObj;
            Object eventsObj = trace.get("resourceEvents");
            if (eventsObj instanceof List) {
                for (Object item : (List<?>) eventsObj) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> ev = (Map<String, Object>) item;
                        Number cid = (Number) ev.get("configId");
                        if (cid == null) {
                            cid = (Number) ev.get("id");
                        }
                        if (cid != null && cid.intValue() == configId) {
                            Boolean add = (Boolean) ev.get("add");
                            if (add == null) {
                                add = (Boolean) ev.get("isAdd");
                            }
                            Number cnt = (Number) ev.get("count");
                            long val = cnt != null ? cnt.longValue() : 0L;
                            actualDelta += (Boolean.TRUE.equals(add) ? val : -val);
                        }
                    } else if (item instanceof RequestTraceService.ResourceEvent) {
                        RequestTraceService.ResourceEvent ev = (RequestTraceService.ResourceEvent) item;
                        if (ev.getId() == configId) {
                            actualDelta += (ev.isAdd() ? ev.getCount() : -ev.getCount());
                        }
                    }
                }
            }
        }
        if (actualDelta != expectedSignedDelta) {
            throw new AssertionError("资源变动量断言失败: configId=" + configId + ", 期望=" + expectedSignedDelta + ", 实际=" + actualDelta);
        }
    }

    /**
     * 断言本次协议请求至少产生了正向奖励事件
     *
     * @param invokeResult 协议模拟执行结果
     */
    public static void assertHasRewardEvent(Map<String, Object> invokeResult) {
        if (invokeResult == null) {
            throw new AssertionError("协议调用结果为空");
        }
        boolean hasReward = false;
        Object traceObj = invokeResult.get("trace");
        if (traceObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> trace = (Map<String, Object>) traceObj;
            Object eventsObj = trace.get("resourceEvents");
            if (eventsObj instanceof List) {
                for (Object item : (List<?>) eventsObj) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> ev = (Map<String, Object>) item;
                        Boolean add = (Boolean) ev.get("add");
                        if (add == null) {
                            add = (Boolean) ev.get("isAdd");
                        }
                        Number cnt = (Number) ev.get("count");
                        if (Boolean.TRUE.equals(add) && cnt != null && cnt.longValue() > 0) {
                            hasReward = true;
                            break;
                        }
                    } else if (item instanceof RequestTraceService.ResourceEvent) {
                        RequestTraceService.ResourceEvent ev = (RequestTraceService.ResourceEvent) item;
                        if (ev.isAdd() && ev.getCount() > 0) {
                            hasReward = true;
                            break;
                        }
                    }
                }
            }
        }
        if (!hasReward) {
            Object resp = invokeResult.get("response");
            if (resp instanceof Map && ((Map<?, ?>) resp).containsKey("rewards")) {
                hasReward = true;
            }
        }
        if (!hasReward) {
            throw new AssertionError("协议执行未产生任何正向奖励流水或奖励下发事件");
        }
    }

    /**
     * 断言模块字段取值符合预期
     *
     * @param player 目标玩家
     * @param moduleName 模块名
     * @param dotPath 字段点分路径
     * @param expected 期望值
     */
    public static void assertModuleFieldEquals(Player player, String moduleName, String dotPath, Object expected) {
        Object actual = inspectModuleField(player, moduleName, dotPath);
        if (expected == null) {
            if (actual != null) {
                throw new AssertionError("字段 " + moduleName + "." + dotPath + " 期望为 null，实际为: " + actual);
            }
            return;
        }
        if (expected instanceof Number && actual instanceof Number) {
            long eLong = ((Number) expected).longValue();
            long aLong = ((Number) actual).longValue();
            if (eLong != aLong) {
                throw new AssertionError("数值字段 " + moduleName + "." + dotPath + " 期望: " + expected + "，实际: " + actual);
            }
            return;
        }
        if (!expected.equals(actual)) {
            throw new AssertionError("字段 " + moduleName + "." + dotPath + " 期望: " + expected + "，实际: " + actual);
        }
    }
}
