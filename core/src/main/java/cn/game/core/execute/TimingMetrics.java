package cn.game.core.execute;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;
import io.vertx.core.Future;

/**
 * 固定维度的进程内耗时观测。默认关闭；业务写入只尝试取得单元锁，忙时丢弃观测而不等待。
 * 没有外部监听器、任务队列、后台线程或业务身份索引。重置以新窗口替换，旧操作仍归旧窗口。
 * 直方图仅提供微秒二次幂桶上界近似值，10ms超限计数直接比较纳秒值，不由桶推算。
 */
public final class TimingMetrics {
    /** 固定阶段；执行/事务等包含等待子阶段，不允许盲目相加。 */
    public enum Stage { REQUEST, MAILBOX_QUEUE, EXECUTION, TASK_ACTUAL, TASK_WAIT,
        DB_WORKER_QUEUE, DB_WORKER, DB_WAIT, CONNECTION, SQL, TRANSACTION, RPC_WAIT, SNAPSHOT }
    /** 固定路由，不保存协议名、地址、玩家或订单。 */
    public enum Route { ALL, HEARTBEAT, LOGIN, OTHER }
    /** 等待超时与迟到实际完成分开；没有响应并不伪报请求成功。 */
    public enum Outcome { SUCCESS, FAILURE, REJECTED, TIMEOUT, LATE_SUCCESS, LATE_FAILURE, NO_RESPONSE }
    /** 纳秒阈值：严格大于10ms才算超限。 */
    private static final long TARGET_NS = 10_000_000L;
    /** 桶上界为1us至2^30us，最后一桶表示溢出。 */
    private static final int BUCKETS = 32;
    /** 缓存枚举维度，热路径不复制values数组。 */
    private static final int ROUTES = Route.values().length, OUTCOMES = Outcome.values().length;
    /** 默认关闭，只读取本地JVM参数，不变更外部配置。 */
    private static volatile TimingMetrics current = new TimingMetrics(Boolean.getBoolean("game.timing.enabled"), System::nanoTime);
    /** 不执行回调的固定空操作。 */
    private static final Span NOOP = new Span();
    /** 本窗口是否启用。 */
    private final boolean enabled;
    /** 仅包内确定性测试注入；生产固定System.nanoTime。 */
    private final LongSupplier clock;
    /** 固定数量单元，不随流量或业务身份增长。 */
    private final Cell[] cells = new Cell[Stage.values().length * Route.values().length * Outcome.values().length];
    /** 竞争导致的观测丢弃数，分母为尝试记录数。 */
    private final AtomicLong dropped = new AtomicLong();
    /** 时钟故障导致的观测丢弃数；无日志风暴，诊断时读取。 */
    private final AtomicLong failures = new AtomicLong();

    /** @param enabled 是否启用 @param clock 单调纳秒源，只供包内测试使用 */
    TimingMetrics(boolean enabled, LongSupplier clock) {
        this.enabled = enabled; this.clock = clock;
        for (int i = 0; i < cells.length; i++) { cells[i] = new Cell(); }
    }
    /** @return 当前窗口，读取无需锁 */
    public static TimingMetrics current() { return current; }
    /** @param enabled 新窗口开关 @return 新窗口；旧操作不会污染新窗口 */
    public static TimingMetrics reset(boolean enabled) {
        return reset(enabled, System::nanoTime);
    }
    /** @param enabled 窗口开关 @param clock 包内确定性测试时钟 @return 新窗口 */
    static TimingMetrics reset(boolean enabled, LongSupplier clock) {
        TimingMetrics next = new TimingMetrics(enabled, clock);
        current = next; return next;
    }
    /** @param stage 固定阶段 @return 默认ALL路由的单次观测 */
    public static Span start(Stage stage) { return current.begin(stage, Route.ALL); }
    /** @param stage 固定阶段 @param route 固定路由 @return 绑定本窗口的单次观测 */
    public Span begin(Stage stage, Route route) {
        if (!enabled) { return NOOP; }
        long now = now();
        return now == Long.MIN_VALUE ? NOOP : new Span(this, stage, route, now);
    }
    /** @return 单调读数，故障只计数，不调用日志或监听器以免阻塞完成链 */
    private long now() {
        try { return clock.getAsLong(); }
        catch (Throwable failure) {
            // 观测路径不能调用可能阻塞的日志；故障通过固定计数供诊断主动拉取。
            failures.incrementAndGet(); return Long.MIN_VALUE;
        }
    }
    /** @param stage 阶段 @param route 路由 @param outcome 结果 @param ns 非负纳秒 */
    private void record(Stage stage, Route route, Outcome outcome, long ns) {
        Cell cell = cells[(stage.ordinal() * ROUTES + route.ordinal()) * OUTCOMES + outcome.ordinal()];
        if (!cell.lock.tryLock()) { cell.dropped.incrementAndGet(); dropped.incrementAndGet(); return; }
        try {
            cell.count++; cell.max = Math.max(cell.max, ns);
            if (ns > TARGET_NS) { cell.over10ms++; }
            long us = ns / 1000 + (ns % 1000 == 0 ? 0 : 1);
            int bucket = us <= 1 ? 0 : Math.min(BUCKETS - 1, 64 - Long.numberOfLeadingZeros(us - 1));
            cell.buckets[bucket]++;
        } finally { cell.lock.unlock(); }
    }
    /** @return 固定大小快照；活跃窗口各单元逐个读取，完全一致的计数须在调用方排空后采集 */
    public Snapshot snapshot() {
        List<Row> rows = new ArrayList<>();
        for (Stage stage : Stage.values()) {
            for (Route route : Route.values()) {
                for (Outcome outcome : Outcome.values()) {
                    Cell cell = cells[(stage.ordinal() * Route.values().length + route.ordinal()) * Outcome.values().length + outcome.ordinal()];
                    cell.lock.lock();
                    try {
                        if (cell.count != 0 || cell.dropped.get() != 0) { rows.add(new Row(stage, route, outcome, cell.count, cell.dropped.get(), cell.over10ms,
                                cell.max, percentile(cell, 50), percentile(cell, 95), percentile(cell, 99))); }
                    } finally { cell.lock.unlock(); }
                }
            }
        }
        return new Snapshot(enabled, dropped.get(), failures.get(), List.copyOf(rows));
    }
    /** @return 当前窗口是否启用，调用者可跳过请求上下文分配 */
    public boolean enabled() { return enabled; }
    /** @param failure 等待边界失败 @return 超时或普通失败，最多检查八层包装 */
    public static Outcome failureOutcome(Throwable failure) {
        for (int i = 0; failure != null && i < 8; i++, failure = failure.getCause()) {
            if (failure instanceof TimeoutException || failure instanceof java.sql.SQLTimeoutException
                    || failure instanceof io.vertx.core.eventbus.ReplyException reply
                    && reply.failureType() == io.vertx.core.eventbus.ReplyFailure.TIMEOUT) { return Outcome.TIMEOUT; }
        }
        return Outcome.FAILURE;
    }
    /** @param cell 已持锁的单元 @param percent 分位数百分比 @return 微秒桶上界，溢出桶为Long.MAX_VALUE */
    private static long percentile(Cell cell, int percent) {
        if (cell.count == 0) { return 0; }
        long rank = cell.count / 100 * percent + (cell.count % 100 * percent + 99) / 100;
        long accumulated = 0;
        for (int i = 0; i < BUCKETS; i++) {
            accumulated += cell.buckets[i];
            if (accumulated >= rank) { return i == BUCKETS - 1 ? Long.MAX_VALUE : 1L << i; }
        }
        return 0;
    }
    /** 固定单元；快照持锁时生产者直接丢样，不阻塞业务。 */
    private static final class Cell {
        /** 单元锁只保护标量与固定桶。 */
        final ReentrantLock lock = new ReentrantLock();
        /** 固定桶计数。 */
        final long[] buckets = new long[BUCKETS];
        /** 按本单元记录竞争丢样，便于排空后核对各阶段尝试总数。 */
        final AtomicLong dropped = new AtomicLong();
        /** 已记录样本、严格超限数与真实最大纳秒。 */
        long count, over10ms, max;
    }
    /** 结果计数的分母为该行count，单位显式包含在字段名中。 */
    public record Row(Stage stage, Route route, Outcome outcome, long count, long dropped, long over10ms,
            long maxNanos, long p50UpperMicros, long p95UpperMicros, long p99UpperMicros) {}
    /** dropped不进入任何行的分母，failures表示时钟故障。 */
    public record Snapshot(boolean enabled, long dropped, long failures, List<Row> rows) {}

    /** 单次计时只携带固定枚举、窗口与起点；CAS保证并发结束只记一次。 */
    public static final class Span {
        /** 空操作owner为null。 */
        private final TimingMetrics owner;
        /** 固定阶段、路由及单调起点。 */
        private final Stage stage;
        private final Route route;
        private final long started;
        /** 多结束分支竞争的唯一记录权。 */
        private final AtomicBoolean done = new AtomicBoolean();
        /** 构造无操作计时。 */
        private Span() { owner = null; stage = null; route = null; started = 0; }
        /** @param owner 窗口 @param stage 阶段 @param route 路由 @param started 单调起点 */
        private Span(TimingMetrics owner, Stage stage, Route route, long started) {
            this.owner = owner; this.stage = stage; this.route = route; this.started = started;
        }
        /** @return 本计时是否绑定启用窗口，不受后续reset影响 */
        public boolean enabled() { return owner != null; }
        /** @param outcome 真实边界结果，多次调用只记录第一次 */
        public void end(Outcome outcome) {
            if (owner == null || !done.compareAndSet(false, true)) { return; }
            long end = owner.now();
            if (end != Long.MIN_VALUE) { owner.record(stage, route, outcome, Math.max(0, end - started)); }
        }
        /** @param actual 原实际结果 @param waited 原等待结果；不替换Future或改变异常 @param <T> 结果类型 */
        public <T> void observe(Future<T> actual, Future<T> waited) {
            if (owner == null) { return; }
            if (actual == waited) {
                waited.onComplete(result -> end(result.succeeded() ? Outcome.SUCCESS : failureOutcome(result.cause())));
                return;
            }
            WaitState state = new WaitState(this);
            actual.onComplete(result -> {
                state.actualSuccess = result.succeeded(); state.actualEnd = owner.now(); state.actualDone.set(true); state.late();
            });
            waited.onComplete(result -> {
                boolean timeout = result.failed() && failureOutcome(result.cause()) == Outcome.TIMEOUT;
                end(result.succeeded() ? Outcome.SUCCESS : timeout ? Outcome.TIMEOUT : Outcome.FAILURE);
                // 原实际失败本身可能是TimeoutException，不能伪装成等待视图先超时。
                if (timeout && !(actual.failed() && actual.cause() == result.cause())) { state.timedOut.set(true); state.late(); }
            });
        }
    }
    /** 只随原Future存活，不登记到全局集合，超时不取得任何业务执行权。 */
    private static final class WaitState {
        /** 原窗口和起点，用于区分等待结果与迟到实际结果。 */
        final Span span;
        /** 两端回调以原子发布顺序协调。 */
        final AtomicBoolean actualDone = new AtomicBoolean(), timedOut = new AtomicBoolean(), recorded = new AtomicBoolean();
        /** actualDone发布前写入。 */
        boolean actualSuccess;
        /** 实际回调时间，不能用迟到的等待回调时间伪装执行时长。 */
        long actualEnd;
        /** @param span 等待观测 */
        WaitState(Span span) { this.span = span; }
        /** 两端任意到达顺序都只记一次迟到实际完成。 */
        void late() {
            if (actualDone.get() && timedOut.get() && recorded.compareAndSet(false, true)) {
                long end = actualEnd;
                if (end != Long.MIN_VALUE) { span.owner.record(span.stage, span.route,
                        actualSuccess ? Outcome.LATE_SUCCESS : Outcome.LATE_FAILURE, Math.max(0, end - span.started)); }
            }
        }
    }
}
