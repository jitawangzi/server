package cn.game.core.execute;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.process.IdVirtualThreadProcessor;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.core.net.socket.controller.Dispatcher;
import cn.game.core.net.socket.handler.Handler;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.handler.PlayerHandler;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.internal.buffer.BufferInternal;

/**
 * 有限、隔离的真实邮箱/协议/心跳路径基准。仅替换分发注册与WebSocket传输，不连接服务或业务账号。
 * 每个操作创建独占连接夹具并发送一次合法心跳，避免重放缓存和每连接限流污染测量。
 * 不冒充已部署Game/Login或网络往返；无DB阶段的窗口明确报告DB样本为零。
 */
public final class RequestTimingBenchmark {
    /** 确定性矩阵和有限总样本。 */
    private static final int WARMUP = 128, MEASURED = 512;
    /** 固定夹具编号，只用于本进程实体键，不对应数据库账号。 */
    private static final long SEED = 84017;
    /** 工具类不创建实例。 */
    private RequestTimingBenchmark() {}

    /** @param args 唯一参数是本轮私有JSON输出文件 @throws Exception 编解码、计数或资源回收异常时失败 */
    public static void main(String[] args) throws Exception {
        if (args.length != 1 || Files.exists(Path.of(args[0]))) { throw new IllegalArgumentException("fresh output path required"); }
        cn.game.games.core.clazz.ClassManager.getInstance().init();
        Map<String, Object> report = new LinkedHashMap<>();
        Map<String, String> executionClassHashes = executionClassHashes();
        report.put("executionClassHashes", executionClassHashes);
        report.put("measurementScope", "IN_PROCESS_REAL_MAILBOX_PROTOBUF_GAMECLIENT_SYNTHETIC_TRANSPORT");
        report.put("jdk", System.getProperty("java.runtime.version"));
        report.put("vm", System.getProperty("java.vm.name"));
        report.put("os", System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
        report.put("logicalProcessors", Runtime.getRuntime().availableProcessors());
        report.put("cpu", System.getenv().getOrDefault("PROCESSOR_IDENTIFIER", "unreported"));
        report.put("jvmArguments", ManagementFactory.getRuntimeMXBean().getInputArguments());
        report.put("seed", SEED); report.put("route", "PlayerHeartbeatRequest_01000005");
        report.put("payloadBytes", 0); report.put("dbPoolSize", 0);
        report.put("dataset", "one fresh synthetic GameClient/connection and seq=1 per operation; no loaded player/account/DB");
        report.put("overheadComparison", "OFF/ON observations only; uncalibrated host, fixed order, no causal speedup assertion");
        List<Object> windows = new ArrayList<>();
        try {
            for (boolean enabled : List.of(false, true)) {
                for (int concurrency : List.of(1, 4, 16)) {
                    for (boolean hotspot : List.of(true, false)) {
                        windows.add(window(enabled, concurrency, hotspot, WARMUP, "warmup"));
                        windows.add(window(enabled, concurrency, hotspot, MEASURED, "measured"));
                    }
                }
            }
            report.put("windows", windows); report.put("resourcesClosed", true);
            if (!executionClassHashes.equals(executionClassHashes())) { throw new IllegalStateException("executing class resources changed"); }
            Files.writeString(Path.of(args[0]), JsonUtil.toJsonString(report));
        } finally { TimingMetrics.reset(false); }
    }
    /** @return 实际执行链当前classpath字节指纹，窗口前后比较，不将旧JAR当作本轮构建 */
    private static Map<String, String> executionClassHashes() throws Exception {
        Map<String, String> hashes = new LinkedHashMap<>();
        for (Class<?> type : List.of(RequestTimingBenchmark.class, TaskExecutorService.class, TaskWrapper.class,
                MailboxProcessor.class, TimingMetrics.class, RequestTiming.class, GameClient.class, PlayerHandler.class,
                IdVirtualThreadProcessor.class, ProtobufProtocol.class)) {
            try (var stream = type.getResourceAsStream(type.getSimpleName() + ".class")) {
                if (stream == null) { throw new IllegalStateException("missing executing class bytes"); }
                hashes.put(type.getName(), java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())));
            }
        }
        return hashes;
    }

    /** @param enabled 观测开关 @param concurrency 生产者数 @param hotspot 是否同一实体 @param count 总样本 @param phase 窗口类型 @return 排空后的真实测量 */
    static Map<String, Object> window(boolean enabled, int concurrency, boolean hotspot, int count, String phase) throws Exception {
        TimingMetrics metrics = TimingMetrics.reset(enabled);
        long[] raw = new long[count];
        AtomicInteger errors = new AtomicInteger(), writes = new AtomicInteger();
        var worker = Executors.newVirtualThreadPerTaskExecutor();
        var callers = Executors.newFixedThreadPool(concurrency);
        TaskExecutorService service = new TaskExecutorService(TaskExecutionConfig.builder().maxQueueSize(64).defaultTaskTimeoutMs(0).build(),
                new OneToOneMailboxManager(64), worker,
                (a, d, u) -> { throw new IllegalStateException("heartbeat must not retry"); }, DateUtil::currentTimeMillis);
        BenchmarkProcessor processor = new BenchmarkProcessor(service);
        long begin = System.nanoTime();
        try {
            List<java.util.concurrent.Future<?>> jobs = new ArrayList<>();
            for (int lane = 0; lane < concurrency; lane++) {
                final int index = lane;
                jobs.add(callers.submit(() -> {
                    for (int i = index; i < count; i += concurrency) {
                        try { raw[i] = request(processor, hotspot ? SEED : SEED + index, writes); }
                        catch (Exception failure) {
                            org.slf4j.LoggerFactory.getLogger(RequestTimingBenchmark.class).warn("基准操作失败，窗口作废", failure);
                            errors.incrementAndGet(); throw new IllegalStateException(failure);
                        }
                    }
                }));
            }
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            for (var job : jobs) { job.get(Math.max(1, deadline - System.nanoTime()), TimeUnit.NANOSECONDS); }
        } finally {
            callers.shutdownNow();
            if (!callers.awaitTermination(5, TimeUnit.SECONDS)) { throw new IllegalStateException("caller cleanup failed"); }
            service.close();
        }
        long duration = System.nanoTime() - begin;
        if (!worker.isTerminated() || writes.get() != count || errors.get() != 0 || Arrays.stream(raw).anyMatch(n -> n <= 0)) {
            throw new IllegalStateException("missing or failed real measurement");
        }
        long[] sorted = raw.clone(); Arrays.sort(sorted);
        var snapshot = metrics.snapshot();
        long requestCount = snapshot.rows().stream().filter(r -> r.stage() == TimingMetrics.Stage.REQUEST).mapToLong(TimingMetrics.Row::count).sum();
        long requestDropped = snapshot.rows().stream().filter(r -> r.stage() == TimingMetrics.Stage.REQUEST).mapToLong(TimingMetrics.Row::dropped).sum();
        long requestFailures = snapshot.rows().stream().filter(r -> r.stage() == TimingMetrics.Stage.REQUEST && r.outcome() != TimingMetrics.Outcome.SUCCESS).mapToLong(r -> r.count() + r.dropped()).sum();
        if (enabled && (requestFailures != 0 || requestCount == 0 || requestCount > count
                || requestCount + requestDropped != count || snapshot.failures() != 0)) {
            throw new IllegalStateException("metrics do not match real completions");
        }
        if (enabled) {
            for (var stage : new TimingMetrics.Stage[]{TimingMetrics.Stage.MAILBOX_QUEUE, TimingMetrics.Stage.EXECUTION, TimingMetrics.Stage.TASK_ACTUAL}) {
                long attempts = snapshot.rows().stream().filter(r -> r.stage() == stage).mapToLong(r -> r.count() + r.dropped()).sum();
                if (attempts != count) { throw new IllegalStateException("missing stage measurements:" + stage); }
            }
        }
        if (!enabled && !snapshot.rows().isEmpty()) { throw new IllegalStateException("disabled metrics recorded"); }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("phase", phase); row.put("instrumentation", enabled); row.put("concurrency", concurrency);
        row.put("players", hotspot ? 1 : concurrency); row.put("distribution", hotspot ? "single_entity_hotspot" : "distributed");
        row.put("callerThreads", concurrency); row.put("workerThreads", "virtual-per-active-mailbox"); row.put("mailboxCapacity", 64);
        row.put("count", count); row.put("success", writes.get()); row.put("failure", errors.get()); row.put("reject", 0); row.put("timeout", 0);
        row.put("durationNanosIncludingDrain", duration); row.put("latencyUnit", "nanoseconds");
        row.put("sampleScope", "before protobuf decode/client construction to synchronous synthetic transport response validation");
        row.put("percentiles", "exact nearest-rank from all raw samples in this window");
        row.put("p50Nanos", sorted[(int) Math.ceil(count * .50) - 1]); row.put("p95Nanos", sorted[(int) Math.ceil(count * .95) - 1]);
        row.put("p99Nanos", sorted[(int) Math.ceil(count * .99) - 1]);
        long over = Arrays.stream(raw).filter(n -> n > 10_000_000L).count();
        row.put("over10ms", over); row.put("over10msDenominator", count); row.put("over10msProportion", (double) over / count);
        row.put("rawNanos", raw); row.put("telemetry", snapshot); row.put("dbSamples", 0); row.put("rpcSamples", 0); row.put("snapshotSamples", 0);
        row.put("resourcesClosed", true);
        return row;
    }

    /** @param processor 真实串行处理器 @param entity 夹具实体键 @param writes 成功写入数 @return 真实纳秒耗时 */
    private static long request(BenchmarkProcessor processor, long entity, AtomicInteger writes) throws Exception {
        long started = System.nanoTime();
        CompletableFuture<Void> response = new CompletableFuture<>();
        ServerWebSocket socket = (ServerWebSocket) Proxy.newProxyInstance(ServerWebSocket.class.getClassLoader(), new Class<?>[]{ServerWebSocket.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("isClosed")) { return false; }
                    if (method.getName().equals("writeBinaryMessage")) {
                        Buffer buffer = (Buffer) args[0];
                        try {
                            if (buffer.getInt(0) != buffer.length() || buffer.getInt(4) != 1
                                    || buffer.getInt(8) != PbProtocol.PlayerHeartbeatResponse_01000006 || buffer.getInt(12) != 0) {
                                throw new IllegalStateException("invalid real response frame");
                            }
                            var decoded = PlayerMsg.PlayerHeartbeatResponse_01000006.parseFrom(buffer.getBytes(16, buffer.length()));
                            if (Long.parseLong(decoded.getTime()) <= 0) { throw new IllegalStateException("invalid heartbeat time"); }
                            writes.incrementAndGet(); response.complete(null); return Future.succeededFuture();
                        } finally { ((BufferInternal) buffer).getByteBuf().release(); }
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        GameClient client = new GameClient(socket); client.setPlayerId(entity);
        ProtobufProtocol protocol = new ProtobufProtocol(); protocol.setMsgID(PbProtocol.PlayerHeartbeatRequest_01000005);
        protocol.setSeq(1); protocol.deserializeData(PlayerMsg.PlayerHeartbeatRequest_01000005.getDefaultInstance().toByteArray());
        processor.process(entity, client, protocol);
        response.get(5, TimeUnit.SECONDS);
        return System.nanoTime() - started;
    }
    /** 仅注册真实心跳方法，保留BaseHandler/GameBaseHandler和GameClient路径，不装配业务容器。 */
    private static final class HeartbeatHandler extends PlayerHandler {
        /** 注册本基准唯一协议。 */
        HeartbeatHandler() { putInvoker(PbProtocol.PlayerHeartbeatRequest_01000005, this::heartbeat); }
    }
    /** 仅替换容器注册，执行与响应均使用现有路径。 */
    private static final class BenchmarkProcessor extends IdVirtualThreadProcessor {
        /** @param service 本窗口拥有的服务 */
        BenchmarkProcessor(TaskExecutorService service) {
            super(service);
            HeartbeatHandler handler = new HeartbeatHandler();
            dispatcher = new Dispatcher() {
                @Override public void put(int module, Handler h) { throw new UnsupportedOperationException(); }
                @Override public void dispatch(NetClient client, IProtocol<?> protocol) { handler.dispatch(client, protocol); }
            };
        }
    }
}
