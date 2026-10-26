package cn.game.games.net.game.helper;

import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.micrometer.backends.BackendRegistries;

/**
 * 只观察已确认提交的订单数量与完整 modules UTF-8 字节，不参与保存、履约或证据淘汰。
 * 回调只消费封存的标量，不读取在线玩家；短锁内预留限频名额，锁外执行日志和指标。
 * 保存回执先完成，生产指标和日志由进程共享的单工作线程异步观察；队列最多一个样本，繁忙时丢弃观测。
 * 玩家限频表满且最旧名额尚未到期时抑制新玩家告警，不驱逐活跃名额造成轮换刷屏。
 */
public final class PaymentCapacityMonitor {
    /** 只输出玩家 ID、标量与固定分类，不输出快照或 SDK 数据。 */
    private static final Logger log = LoggerFactory.getLogger(PaymentCapacityMonitor.class);
    /** 配置前缀；JVM 启动时读取一次。 */
    private static final String PREFIX = "payment.capacity.";
    /** 已提交快照的容量标量，不保留在线对象或 JSON 引用。 */
    public record Sample(long playerId, int orders, long modulesUtf8Bytes) {}
    /** 两个维度独立分类，达到阈值即告警。 */
    public record Alert(long playerId, String dimension, String severity, long value, long threshold) {}
    /** 初始运营预算，不代表实测 SLA，也不是保存硬上限。 */
    public record Policy(long ordersWarn, long ordersCritical, long bytesWarn, long bytesCritical,
            long intervalMillis, int trackedPlayers, int alertsPerInterval) {
        /** 拒绝倒置、非正数及无界限频配置。 */
        public Policy {
            if (ordersWarn <= 0 || ordersCritical <= ordersWarn || bytesWarn <= 0 || bytesCritical <= bytesWarn
                    || intervalMillis < 1_000 || intervalMillis > 86_400_000
                    || trackedPlayers < 1 || trackedPlayers > 65_536 || alertsPerInterval < 1 || alertsPerInterval > 1_024) {
                throw new IllegalArgumentException("容量预算必须为递增正数，限频窗口及状态容量必须有界");
            }
        }
        /** @return 初始每玩家 1000/5000 单、完整模块 1/4 MiB、五分钟告警窗口 */
        public static Policy defaults() { return new Policy(1_000, 5_000, 1_048_576, 4_194_304, 300_000, 8_192, 128); }
        /** @param values JVM 属性；任一无效则整组回退安全默认，不输出属性正文 @return 生效预算 */
        public static Policy from(Properties values) {
            Policy d = defaults();
            try {
                return new Policy(Long.parseLong(values.getProperty(PREFIX + "orders.warn", "" + d.ordersWarn)),
                        Long.parseLong(values.getProperty(PREFIX + "orders.critical", "" + d.ordersCritical)),
                        Long.parseLong(values.getProperty(PREFIX + "modulesBytes.warn", "" + d.bytesWarn)),
                        Long.parseLong(values.getProperty(PREFIX + "modulesBytes.critical", "" + d.bytesCritical)),
                        Long.parseLong(values.getProperty(PREFIX + "alertIntervalMillis", "" + d.intervalMillis)),
                        Integer.parseInt(values.getProperty(PREFIX + "trackedPlayers", "" + d.trackedPlayers)),
                        Integer.parseInt(values.getProperty(PREFIX + "alertsPerInterval", "" + d.alertsPerInterval)));
            } catch (RuntimeException failure) {
                log.warn("支付容量配置无效，整组使用默认预算", redacted(failure));
                return d;
            }
        }
    }
    /** 首次已确认保存时才读取配置，不在静态加载时访问监控设施。 */
    private static final class Shared {
        /** 进程共享的有界限频器。 */
        private static final PaymentCapacityMonitor instance = new PaymentCapacityMonitor(Policy.from(System.getProperties()),
                PaymentCapacityMonitor::elapsedMillis, PaymentCapacityMonitor::metrics,
                a -> log.warn("支付容量需人工核查 playerId={} dimension={} severity={} value={} threshold={} source=COMMITTED_MODULES",
                        a.playerId, a.dimension, a.severity, a.value, a.threshold));
    }
    /** @param sample 来自真实提交结果的标量；空值表示该保存入口没有支付容量数据 */
    static void committed(Sample sample) {
        if (sample == null) { return; }
        try { Shared.instance.observe(sample); }
        catch (Throwable failure) { reportFailure(failure); }
    }
    /** 只用于时长比较的单调原点；日志时间戳仍由日志设施使用墙钟。 */
    private static final long nanoOrigin = System.nanoTime();
    /** 进程最多一个观测工作线程及一个排队标量；慢出口不会积累保存通知线程。 */
    private static final Publisher publisher = new Publisher("payment-capacity-observer");
    /** 故障日志单独隔离，探针在封存线程失败时也不能调用可能阻塞的日志设施。 */
    private static final Publisher failurePublisher = new Publisher("payment-capacity-failure");
    /** 故障日志预留也使用单调时长，并在日志设施自身失败时终止递归报告。 */
    private static final FailureReporter failures = new FailureReporter(PaymentCapacityMonitor::elapsedMillis,
            failure -> log.warn("支付容量观测失败，保存结果保持不变", redacted(failure)));

    /** @return 从单调原点经过的毫秒；不受墙钟快进、回拨影响 */
    static long elapsedMillis() { return java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - nanoOrigin); }

    /** @param sample 已封存标量 @param observer 独立出口；繁忙时允许丢弃，诊断中的确认容量仍保留 */
    static void publish(Sample sample, Consumer<Sample> observer) {
        if (sample != null) {
            publisher.submit(() -> {
                try { observer.accept(sample); }
                catch (Throwable failure) { reportFailure(failure); }
            });
        }
    }

    /** @param failure 监控故障；异步、限频、脱敏报告，不把日志阻塞或异常带回封存和保存 */
    static void reportFailure(Throwable failure) {
        failurePublisher.submit(() -> failures.report(failure));
    }

    /** 有界执行出口，最多一个线程和一个待处理任务；不在调用线程执行被拒任务。 */
    private static final class Publisher {
        /** 空闲线程可回收，队列仅容纳一个任务。 */
        private final java.util.concurrent.ThreadPoolExecutor executor;
        /** @param name 后台线程名 */
        Publisher(String name) {
            executor = new java.util.concurrent.ThreadPoolExecutor(0, 1, 30, java.util.concurrent.TimeUnit.SECONDS,
                    new java.util.concurrent.ArrayBlockingQueue<>(1), Thread.ofPlatform().daemon(true).name(name).factory(),
                    new java.util.concurrent.ThreadPoolExecutor.DiscardPolicy());
        }
        /** @param action 仅持有标量或故障引用的任务；调度失败也不得影响保存 */
        void submit(Runnable action) {
            try { executor.execute(action); }
            catch (Throwable failure) {
                // 执行器无法启动时仅尝试独立故障日志；故障执行器本身不可递归报告。
                if (this != failurePublisher) { reportFailure(failure); }
            }
        }

    }

    /** 可注入单调时钟及日志出口，验证限频并把日志设施故障截断于最终边界。 */
    static final class FailureReporter {
        /** 单调经过毫秒，不接受墙钟。 */
        private final LongSupplier clock;
        /** 脱敏日志出口。 */
        private final Consumer<Throwable> logger;
        /** 最近预留的故障日志时刻，原子预留避免并发刷屏。 */
        private final java.util.concurrent.atomic.AtomicLong last = new java.util.concurrent.atomic.AtomicLong(Long.MIN_VALUE);
        /** @param clock 单调毫秒时钟 @param logger 脱敏日志出口 */
        FailureReporter(LongSupplier clock, Consumer<Throwable> logger) { this.clock = clock; this.logger = logger; }
        /** @param failure 观测故障；日志出口故障已位于最终边界，不再向同一设施递归报告 */
        void report(Throwable failure) {
            try {
                long now = clock.getAsLong(), previous = last.get();
                if ((previous == Long.MIN_VALUE || now - previous >= 300_000) && last.compareAndSet(previous, now)) {
                    logger.accept(failure);
                }
            } catch (Throwable loggingFailure) {
                // 日志设施自身已不可用；再次调用只会递归失败，不能传播至封存或保存。
            }
        }
    }
    /** @param failure 原异常 @return 仅类型与栈帧，不包含配置或支付正文 */
    private static Throwable redacted(Throwable failure) {
        var safe = new IllegalStateException("PAYMENT_CAPACITY_" + failure.getClass().getSimpleName());
        safe.setStackTrace(failure.getStackTrace());
        return safe;
    }
    /** @param sample 确认提交的完整 modules 容量；指标无玩家 ID 标签 */
    static void metrics(Sample sample) {
        var registry = BackendRegistries.getDefaultNow();
        if (registry == null) { return; }
        metrics(registry, sample);
    }
    /** @param registry 既有指标注册表 @param sample 已提交标量；固定两个无标签指标避免玩家基数增长 */
    static void metrics(io.micrometer.core.instrument.MeterRegistry registry, Sample sample) {
        registry.summary("payment.capacity.committed.orders").record(sample.orders);
        registry.summary("payment.capacity.committed.modules.bytes").record(sample.modulesUtf8Bytes);
    }
    /** 当前预算及依赖均在构造后固定。 */
    private final Policy policy;
    /** 单调经过毫秒时钟；注入契约与生产 nanoTime 派生时长一致。 */
    private final LongSupplier clock;
    /** 指标与告警分别隔离失败。 */
    private final Consumer<Sample> metrics;
    /** 只接收脱敏标量的告警出口。 */
    private final Consumer<Alert> alerts;
    /** 按最后一次告警顺序存储；每玩家两个维度的最近名额。 */
    private final LinkedHashMap<Long, long[]> players = new LinkedHashMap<>();
    /** 全局窗口的单调起点，未开始时使用哨兵。 */
    private long window = Long.MIN_VALUE;
    /** 单窗口进程告警上限，约束不同玩家的日志总量。 */
    private int emitted;

    /** @param policy 有界预算 @param clock 单调经过毫秒时钟，不能传入墙钟 @param metrics 指标出口 @param alerts 告警出口 */
    public PaymentCapacityMonitor(Policy policy, LongSupplier clock, Consumer<Sample> metrics, Consumer<Alert> alerts) {
        this.policy = java.util.Objects.requireNonNull(policy); this.clock = java.util.Objects.requireNonNull(clock);
        this.metrics = java.util.Objects.requireNonNull(metrics); this.alerts = java.util.Objects.requireNonNull(alerts);
    }
    /** @param sample 已确认提交的标量；可由任意回调线程调用，不读写玩家对象 */
    public void observe(Sample sample) {
        try { metrics.accept(sample); }
        catch (Throwable failure) { reportFailure(failure); }
        emit(sample, 0, "orders", sample.orders, policy.ordersWarn, policy.ordersCritical);
        emit(sample, 1, "modules_bytes", sample.modulesUtf8Bytes, policy.bytesWarn, policy.bytesCritical);
    }
    /** @param sample 样本 @param index 维度槽 @param dimension 固定维度 @param value 当前值 @param warn 预警 @param critical 严重预算 */
    private void emit(Sample sample, int index, String dimension, long value, long warn, long critical) {
        if (value < warn || !reserve(sample.playerId, index)) { return; }
        try { alerts.accept(new Alert(sample.playerId, dimension, value >= critical ? "critical" : "warn", value, value >= critical ? critical : warn)); }
        catch (Throwable failure) { reportFailure(failure); }
    }
    /** @param player 玩家 ID，仅用于有界限频状态 @param index 独立维度 @return 是否已预留本次告警；不在锁内调用出口 */
    private synchronized boolean reserve(long player, int index) {
        long time = clock.getAsLong();
        if (window == Long.MIN_VALUE || time - window >= policy.intervalMillis) { window = time; emitted = 0; }
        if (emitted >= policy.alertsPerInterval) { return false; }
        long[] last = players.get(player);
        if (last == null) {
            if (players.size() >= policy.trackedPlayers) {
                var oldest = players.entrySet().iterator().next();
                if (time - Math.max(oldest.getValue()[0], oldest.getValue()[1]) < policy.intervalMillis) { return false; }
                players.remove(oldest.getKey());
            }
            last = new long[] {Long.MIN_VALUE, Long.MIN_VALUE};
        }
        if (last[index] != Long.MIN_VALUE && time - last[index] < policy.intervalMillis) { return false; }
        last[index] = time;
        players.remove(player); players.put(player, last); emitted++;
        return true;
    }
    /** @return 当前限频表大小，仅用于有界性测试 */
    synchronized int trackedPlayers() { return players.size(); }

    /**
     * 现有封存仅产生字符串；单次遍历该结果计算 UTF-8 字节，避免分配第二份大字节数组或再次序列化。
     * 与 String.getBytes(UTF_8) 一致：有效代理对四字节，孤立代理字符使用一个替换字节。
     * @param text 已封存完整 modules JSON @return UTF-8 编码字节数，不是字符数
     */
    public static long utf8Bytes(String text) {
        long bytes = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < 0x80) { bytes++; }
            else if (c < 0x800) { bytes += 2; }
            else if (Character.isHighSurrogate(c) && i + 1 < text.length() && Character.isLowSurrogate(text.charAt(i + 1))) { bytes += 4; i++; }
            else { bytes += Character.isSurrogate(c) ? 1 : 3; }
        }
        return bytes;
    }
}
