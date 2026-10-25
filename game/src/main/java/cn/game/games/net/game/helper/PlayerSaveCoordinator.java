package cn.game.games.net.game.helper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.LongPredicate;
import java.util.function.LongSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.PlayerData;
import cn.game.util.DateUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * 按玩家 ID 保持单个真实写入在途，等待超时不释放写入权；结果不明时隔离后继。
 * 状态独立于连接与 Player 生命周期，所有状态转换在按 ID 的短临界区完成。
 * IO、等待者通知均在临界区外，不依赖玩家邮箱，允许玩家任务同步等待保存。
 * 业务入口按保守变更作用域跟踪风险；确认仅清除快照覆盖的版本，不提供跨进程恢复证明。
 */
public final class PlayerSaveCoordinator {
    /** 保存状态和恢复裁决日志。 */
    private static final Logger log = LoggerFactory.getLogger(PlayerSaveCoordinator.class);
    /** 当前进程共享保存通道；首次使用才初始化资源。 */
    private static final class Shared {
        /** 独立等待计时器，不执行数据库工作。 */
        private static final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(
                Thread.ofPlatform().daemon(true).name("player-save-timeout").factory());
        /** 以墙钟建立诊断时间原点，运行时用单调时长，避免校时回拨冻结五分钟风险计时。 */
        private static final long timeOrigin = DateUtil.currentTimeMillis(), nanoOrigin = System.nanoTime();
        /** 每玩家最多 16 个排队快照；所有请求都不合并，默认等待 30 秒。 */
        private static final PlayerSaveCoordinator instance = new PlayerSaveCoordinator(16, 30_000,
                new PlayerSnapshotWriter()::write, (action, delay) -> {
                    var scheduled = timer.schedule(action, delay, TimeUnit.MILLISECONDS);
                    return () -> scheduled.cancel(false);
                }, () -> timeOrigin + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - nanoOrigin));
    }
    /** @return 进程内统一通道，不能以新实例绕过 UNKNOWN */
    public static PlayerSaveCoordinator shared() { return Shared.instance; }
    /** 实际事务结果；NOT_WRITTEN 必须有未写入证明，任何不明异常使用 UNKNOWN。 */
    enum Outcome { COMMITTED, NOT_WRITTEN, UNKNOWN }
    /** 写入器的真实结果，不含调用者等待期限。 */
    record WriteResult(Outcome outcome, Throwable cause) {}
    /** 专用写入器必须在显式提交确认后成功，不得返回带等待超时的包装 Future。 */
    @FunctionalInterface
    interface Writer {
        /** @param data 已封存参数 @param insert 是否创建 @return 真实执行结果 */
        Future<WriteResult> write(PlayerData data, boolean insert);
    }
    /** 可替换计时器，用于确定性超时测试。 */
    @FunctionalInterface
    interface Timer {
        /** @param action 到期动作 @param delayMillis 等待毫秒 @return 取消本次计时的动作 */
        Runnable schedule(Runnable action, long delayMillis);
    }
    /** 每次确认都绑定其自身代次与快照覆盖时点。 */
    public record Receipt(long playerId, long generation, long capturedAt, long committedAt) {}
    /** 已返回的超时/失败不被人工核对改写；诊断通过 status 读取。 */
    record Submission(long generation, Future<Receipt> result) {}
    /** 明确区分拒绝、无操作、未写入及结果不明，均不能当作持久化成功。 */
    public static final class SaveFailure extends IllegalStateException {
        /** 可供调用方区分的稳定原因。 */
        public final String code;
        /** @param code 失败原因 @param playerId 玩家 @param generation 保存代次 @param cause 原因链 */
        SaveFailure(String code, long playerId, long generation, Throwable cause) {
            super(code + " playerId=" + playerId + " generation=" + generation, cause);
            this.code = code;
        }
    }
    /** 供诊断与后续保护使用；null 变更时间表示未知，不能用快照时间冒充业务变更时间。 */
    public record Status(String coordinatorId, long playerId, String state, long generation, int queued,
            long lastConfirmedGeneration, long lastConfirmedAt, long confirmedSnapshotAt,
            boolean observedDirty, Long earliestUnsavedChangeAt, Long riskFirstObservedSnapshotAt,
            boolean waiterTimedOut, boolean workerExited, String failure, long lastAttemptAt,
            long riskAgeMillis, boolean warning, boolean writeProtected, int consecutiveFailures,
            long retryAt, boolean automaticBlocked, int deferredTimers) {}
    /** 受信运维核对后的裁决，不提供强制跳过 UNKNOWN 的选项。 */
    public enum Resolution { COMMITTED, NOT_COMMITTED }
    /**
     * 受信运维证据引用。必须先排除数据库及代理后端的旧命令/事务晚提交，再核对持久化结果。
     * 字符串只是审计记录，程序无法验证外部证明；禁止由普通客户端或单次 SELECT 自动生成。
     */
    public record RecoveryEvidence(String operator, String databaseTarget, String incidentReference,
            String oldTransactionTerminationEvidence, String persistedResultEvidence) {
        /** 构造时要求所有核对信息完整，不将非空字符串宣称为机器证明。 */
        public RecoveryEvidence {
            for (String field : List.of(operator, databaseTarget, incidentReference,
                    oldTransactionTerminationEvidence, persistedResultEvidence)) {
                if (field.isBlank()) { throw new IllegalArgumentException("恢复必须提供完整的受信核对记录"); }
            }
        }
    }
    /** 本实例身份，防止跨进程或陈旧恢复指令误放行。 */
    private final String identity = UUID.randomUUID().toString();
    /** 按 ID 的短临界区与状态，健康清理时释放。 */
    private final ConcurrentHashMap<Long, Lane> lanes = new ConcurrentHashMap<>();
    /** 实例内代次不复用，清理后重新登录也不能匹配旧指令。 */
    private final AtomicLong generations = new AtomicLong();
    /** 最大等待队列长度，不含一个在途写入。 */
    private final int capacity;
    /** 每个等待视图的最长等待毫秒。 */
    private final long waitMillis;
    /** 不带等待超时的实际事务入口。 */
    private final Writer writer;
    /** 独立计时器。 */
    private final Timer timer;
    /** 可控毫秒时钟。 */
    private final LongSupplier clock;
    /** 墙钟回拨不得缩短已观察风险。 */
    private final java.util.concurrent.atomic.AtomicInteger automaticOutstanding = new java.util.concurrent.atomic.AtomicInteger();
    /** 墙钟观察上界。 */
    private final AtomicLong monotonicTime = new AtomicLong();
    /** 固定五分钟预算；预留一分钟数据库处理余量。 */
    private final RiskPolicy policy;
    /** 可验证的风险与重试边界；不得将预算配置为超过用户允许的五分钟。 */
    record RiskPolicy(long saveAfter, long warnAfter, long budget, int maxRetries, long backoff, int dispatchLimit) {
        RiskPolicy {
            if (saveAfter <= 0 || saveAfter >= warnAfter || warnAfter >= budget || budget > 300_000
                    || maxRetries < 0 || maxRetries > 8 || backoff <= 0 || dispatchLimit <= 0) {
                throw new IllegalArgumentException("保存阈值必须满足 0 < 提前保存 < 告警 < 预算 <= 300000");
            }
        }
        /** @return 默认提前240秒、270秒告警、300秒保护，最多三次自动重试 */
        static RiskPolicy defaults() { return new RiskPolicy(240_000, 270_000, 300_000, 3, 1_000, 128); }
    }
    /** 每次延期都有独立身份，取消后重新登记同一Runnable不能被旧投递误执行。 */
    private record DeferredTimer(Runnable action) {}
    /** 一段尚未确认的变化；只在接受快照时封段，内存受保存队列容量约束。 */
    private static final class Change {
        long version, at;
        Change(long version, long at) { this.version = version; this.at = at; }
    }
    /** 捕获开始时点和覆盖版本，不能用序列化完成时点冒充覆盖起点。 */
    record Capture(long version, long at) {}
    /** 同步业务作用域；跨快照的活动作用域始终保留原始风险时间。 */
    public final class Mutation implements AutoCloseable {
        private final long id, version, at;
        private final Object owner;
        private boolean closed;
        private Mutation(long id, Object owner, long version, long at) {
            this.id = id; this.owner = owner; this.version = version; this.at = at;
        }
        @Override
        public void close() {
            lanes.computeIfPresent(id, (key, lane) -> {
                if (!closed && lane.owner == owner && lane.mutations.remove(this)) {
                    if (lane.sealedVersion >= version) { changed(lane, at); }
                    closed = true;
                }
                return lane;
            });
        }
    }
    /** 一个玩家的已观察状态，仅在该键的 compute 内访问。 */
    private static final class Lane {
        /** 当前权威对象身份，不让旧对象提交覆盖新对象。 */
        Object owner;
        /** 当前实际写入或 UNKNOWN 原请求。 */
        Job active;
        /** 严格 FIFO；不合并关键或普通保存。 */
        final ArrayDeque<Job> queue = new ArrayDeque<>();
        /** 结果不明时保持 active，不释放写入权。 */
        boolean unknown;
        /** 已观察变更及已确认覆盖版本；拒绝也增加前者。 */
        long version, confirmedVersion;
        /** 最近已确认提交的回执。 */
        Receipt confirmed;
        /** 未明确埋点时保持 null，避免伪造最早业务变更时点。 */
        Long dirtyAt;
        /** 封存边界之后的变更合并在同一段，避免逐请求保留日志。 */
        long sealedVersion, captureVersion, lastAttemptAt, retryAt, terminalSequence;
        final ArrayDeque<Change> changes = new ArrayDeque<>();
        final Set<Mutation> mutations = new HashSet<>();
        /** 已到期但因保护尚未执行的一次性任务，最多64个，不重放业务异常。 */
        final ArrayDeque<DeferredTimer> deferredTimers = new ArrayDeque<>();
        boolean timerPending;
        boolean protectedWrite, warned, automaticPending, automaticBlocked;
        int failures;
        /** 当前风险期间首次观察到快照的时点；部分确认不将其伪装为当前最早未确认快照。 */
        Long snapshotAt;
        /** 最近失败信息。 */
        String failure;
        /** @return 是否必须保留权威对象 */
        boolean risky() { return active != null || !queue.isEmpty() || version > confirmedVersion || !mutations.isEmpty(); }
    }
    /** 一次请求独占的快照、代次与等待视图。 */
    private static final class Job {
        /** 封存快照，仅供本次写入器读取。 */
        final PlayerData data;
        /** 创建或更新操作。 */
        final boolean insert;
        /** 请求代次、捕获时间及覆盖的已观察版本。 */
        final long generation, capturedAt, version;
        /** 无邮箱/Vert.x Context 绑定的等待 Promise。 */
        final Promise<Receipt> waiter = Promise.promise();
        /** 独立等待计时取消句柄，支持排队过程中即完成的竞争。 */
        volatile Runnable cancelTimer = () -> {};
        /** 当前请求是否已有等待超时，状态由键临界区保护。 */
        boolean timedOut;
        /** @param data 参数 @param insert 操作 @param generation 代次 @param capturedAt 捕获时间 @param version 覆盖版本 */
        Job(PlayerData data, boolean insert, long generation, long capturedAt, long version) {
            this.data = data; this.insert = insert; this.generation = generation;
            this.capturedAt = capturedAt; this.version = version;
        }
    }
    /** @param capacity 排队上限 @param waitMillis 等待期限 @param writer 真实写入器 @param timer 独立计时器 @param clock 毫秒时钟 */
    PlayerSaveCoordinator(int capacity, long waitMillis, Writer writer, Timer timer, LongSupplier clock) {
        this(capacity, waitMillis, writer, timer, clock, RiskPolicy.defaults());
    }
    /** 测试注入策略和时钟，生产使用固定预算。 */
    PlayerSaveCoordinator(int capacity, long waitMillis, Writer writer, Timer timer, LongSupplier clock, RiskPolicy policy) {
        if (capacity < 1 || waitMillis < 1) { throw new IllegalArgumentException("保存容量和等待期限必须为正"); }
        this.capacity = capacity; this.waitMillis = waitMillis;
        this.writer = Objects.requireNonNull(writer); this.timer = Objects.requireNonNull(timer);
        this.clock = Objects.requireNonNull(clock); this.policy = Objects.requireNonNull(policy);
    }
    /** @param owner 权威对象 @param id 玩家；捕获前登记风险，准备失败也不能清理玩家 */
    Capture preparing(Object owner, long id) {
        AtomicReference<Capture> result = new AtomicReference<>();
        lanes.compute(id, (key, lane) -> {
            requireOwner(lane, owner, id);
            long at = now();
            // beforeSave 也可能修改聚合字段，捕获失败仍保留原始风险。
            changed(lane, at);
            lane.lastAttemptAt = at;
            lane.captureVersion = lane.version;
            lane.sealedVersion = Math.max(lane.sealedVersion, lane.version);
            result.set(new Capture(lane.version, at));
            return lane;
        });
        return result.get();
    }
    /**
     * 接收已经捕获的参数，不在 IO 回调中访问在线 Player；拒绝也保留已观察未保存状态。
     * @param owner 权威玩家身份 @param data 独占参数 @param insert 是否创建 @return 本代等待视图
     */
    Submission submit(Object owner, PlayerData data, boolean insert) {
        return submit(owner, data, insert, null);
    }
    /** 带捕获边界的生产入口；旧测试/手工快照入口按提交时观察到的变化保守登记。 */
    Submission submit(Object owner, PlayerData data, boolean insert, Capture capture) {
        long id = data.getPlayerId();
        long generation = generations.incrementAndGet();
        AtomicReference<Job> accepted = new AtomicReference<>();
        AtomicReference<Job> start = new AtomicReference<>();
        AtomicReference<SaveFailure> rejected = new AtomicReference<>();
        lanes.compute(id, (key, lane) -> {
            if (lane == null) { lane = new Lane(); lane.owner = owner; }
            if (lane.owner != owner) {
                rejected.set(new SaveFailure("OWNER_CHANGED", id, generation, null));
                return lane;
            }
            if (capture == null) { changed(lane, now()); }
            lane.lastAttemptAt = now();
            if (lane.snapshotAt == null) { lane.snapshotAt = capture == null ? now() : capture.at(); }
            if (lane.unknown || lane.queue.size() >= capacity) {
                String code = lane.unknown ? "UNKNOWN_BLOCKED" : "QUEUE_FULL";
                lane.failure = code;
                rejected.set(new SaveFailure(code, id, generation, null));
                return lane;
            }
            Job job = new Job(data, insert, generation, capture == null ? now() : capture.at(),
                    capture == null ? lane.version : capture.version());
            lane.sealedVersion = Math.max(lane.sealedVersion, job.version);
            accepted.set(job);
            if (lane.active == null) { lane.active = job; start.set(job); }
            else { lane.queue.addLast(job); }
            return lane;
        });
        if (rejected.get() != null) { return new Submission(generation, Future.failedFuture(rejected.get())); }
        Job job = accepted.get();
        try {
            job.cancelTimer = timer.schedule(() -> timeout(id, job), waitMillis);
            if (job.waiter.future().isComplete()) { job.cancelTimer.run(); }
        } catch (RuntimeException failure) {
            log.error("保存等待计时失败，真实写入仍由通道跟踪 playerId={}", id, failure);
            job.waiter.tryFail(failure);
        }
        if (start.get() != null) { start(id, start.get()); }
        return new Submission(generation, job.waiter.future());
    }
    /** @param id 玩家 @param job 到期请求；只结束等待，不移除请求或释放写入权 */
    private void timeout(long id, Job job) {
        AtomicReference<Boolean> waiting = new AtomicReference<>(false);
        lanes.computeIfPresent(id, (key, lane) -> {
            if (lane.active == job || lane.queue.contains(job)) { job.timedOut = true; waiting.set(true); }
            return lane;
        });
        if (waiting.get()) {
            Thread.startVirtualThread(() -> job.waiter.tryFail(new SaveFailure("WAIT_TIMEOUT", id, job.generation, null)));
        }
    }
    /** @param id 玩家 @param job 已预留写入权的请求，IO 与回调必须在键临界区外执行 */
    private void start(long id, Job job) {
        try {
            writer.write(job.data, job.insert).onComplete(result -> finish(id, job, result.succeeded()
                    ? result.result() : new WriteResult(Outcome.UNKNOWN, result.cause())));
        } catch (Throwable failure) {
            log.error("保存提交器异常，无法证明未写入，隔离 playerId={} generation={}", id, job.generation, failure);
            finish(id, job, new WriteResult(Outcome.UNKNOWN, failure));
        }
    }
    /** @param id 玩家 @param job 本次请求 @param result 实际终态，先预留并启动后继再通知等待者 */
    private void finish(long id, Job job, WriteResult result) {
        AtomicReference<Job> next = new AtomicReference<>();
        AtomicReference<Receipt> receipt = new AtomicReference<>();
        lanes.computeIfPresent(id, (key, lane) -> {
            if (lane.active != job || lane.unknown) { return lane; }
            lane.terminalSequence++;
            if (result.outcome() == Outcome.COMMITTED) { receipt.set(confirm(id, lane, job)); }
            else { failed(lane, result.cause(), result.outcome() == Outcome.NOT_WRITTEN); }
            if (result.outcome() == Outcome.UNKNOWN) { lane.unknown = true; }
            else { lane.active = lane.queue.pollFirst(); next.set(lane.active); }
            refreshRisk(id, lane);
            return lane;
        });
        if (next.get() != null) { start(id, next.get()); }
        job.cancelTimer.run();
        // 独立通知线程避免用户同步回调等待其他代次时阻塞写入推进或等待计时器。
        Thread.startVirtualThread(() -> {
            if (result.outcome() == Outcome.COMMITTED) { job.waiter.tryComplete(receipt.get()); }
            else {
                log.warn("玩家保存未确认 playerId={} generation={} outcome={}", id, job.generation, result.outcome(), result.cause());
                job.waiter.tryFail(new SaveFailure(result.outcome().name(), id, job.generation, result.cause()));
            }
        });
    }
    /** @param id 玩家 @param lane 所属状态 @param job 已确认请求 @return 本代提交回执 */
    private Receipt confirm(long id, Lane lane, Job job) {
        Receipt receipt = new Receipt(id, job.generation, job.capturedAt, now());
        lane.confirmed = receipt; lane.confirmedVersion = job.version;
        while (!lane.changes.isEmpty() && lane.changes.peekFirst().version <= job.version) { lane.changes.removeFirst(); }
        lane.failures = 0; lane.retryAt = 0; lane.automaticBlocked = false;
        if (lane.version == job.version) { lane.snapshotAt = null; lane.failure = null; }
        updateDirtyAt(lane);
        return receipt;
    }
    /**
     * 受信服务端运维入口：只有排除旧事务晚提交并核对结果后才调用；不执行自动读值解锁。
     * @param coordinatorId 从诊断获得的本进程身份 @param id 玩家 @param generation 原 UNKNOWN 代次
     * @param resolution 已核对的结果 @param evidence 外部核对证据 @return 核对后的即时状态
     */
    public Status reconcile(String coordinatorId, long id, long generation, Resolution resolution, RecoveryEvidence evidence) {
        Objects.requireNonNull(evidence); Objects.requireNonNull(resolution);
        AtomicReference<Job> next = new AtomicReference<>();
        lanes.compute(id, (key, lane) -> {
            if (!identity.equals(coordinatorId) || lane == null || !lane.unknown || lane.active.generation != generation) {
                throw new IllegalStateException("恢复身份/代次/UNKNOWN 不匹配，保持原状态");
            }
            // UNKNOWN 仅由真实写入器退出后的结果建立；外部数据库终止证明仍须由受信运维提供。
            log.warn("人工核对保存 playerId={} generation={} resolution={} evidence={}", id, generation, resolution, evidence);
            if (resolution == Resolution.COMMITTED) { confirm(id, lane, lane.active); }
            lane.unknown = false;
            lane.active = lane.queue.pollFirst(); next.set(lane.active);
            refreshRisk(id, lane);
            return lane;
        });
        if (next.get() != null) { start(id, next.get()); }
        return status(id);
    }
    /** @param id 玩家 @return 当前状态，未知玩家返回 null，不将其视为已持久化 */
    public Status status(long id) {
        AtomicReference<Status> value = new AtomicReference<>();
        lanes.computeIfPresent(id, (key, lane) -> {
            refreshRisk(id, lane);
            Receipt c = lane.confirmed;
            value.set(new Status(identity, id, lane.unknown ? "UNKNOWN" : lane.active != null ? "IN_FLIGHT" : "IDLE",
                    lane.active == null ? 0 : lane.active.generation, lane.queue.size(), c == null ? 0 : c.generation(),
                    c == null ? 0 : c.committedAt(), c == null ? 0 : c.capturedAt(), lane.version > lane.confirmedVersion || !lane.mutations.isEmpty(),
                    lane.dirtyAt, lane.snapshotAt, lane.active != null && lane.active.timedOut, lane.unknown, lane.failure,
                    lane.lastAttemptAt, age(lane), lane.warned, lane.protectedWrite, lane.failures, lane.retryAt, lane.automaticBlocked, lane.deferredTimers.size()));
            return lane;
        });
        return value.get();
    }
    /** @param disconnectedResident 判断仍驻留但连接已断开的玩家 @return 含脏、在途或 UNKNOWN 的诊断快照 */
    public List<Status> disconnectedDirtyPlayers(LongPredicate disconnectedResident) {
        List<Status> result = new ArrayList<>();
        for (long id : lanes.keySet()) {
            Status status = status(id);
            if (status != null && (status.observedDirty() || !status.state().equals("IDLE")) && disconnectedResident.test(id)) {
                result.add(status);
            }
        }
        return List.copyOf(result);
    }
    /** @param id 玩家 @param changedAt 首次已知变更时间；调用者负责真实变更埋点，不能用保存尝试时间代替 */
    public void markDirty(long id, long changedAt) {
        lanes.computeIfPresent(id, (key, lane) -> {
            changed(lane, Math.min(now(), changedAt));
            return lane;
        });
    }
    /** @return 单调观察时钟；回拨期间风险不会变年轻 */
    private long now() { return monotonicTime.accumulateAndGet(clock.getAsLong(), Math::max); }
    /** 验证权威对象，不能用旧回调或未登记对象重新建通道。 */
    private void requireOwner(Lane lane, Object owner, long id) {
        if (lane == null) { throw new SaveFailure("PLAYER_NOT_REGISTERED", id, 0, null); }
        if (lane.owner != owner) { throw new SaveFailure("OWNER_CHANGED", id, 0, null); }
    }
    /** 登记保守业务变更；同一未封存区段只保留最早时间。 */
    private void changed(Lane lane, long at) {
        lane.version++;
        compactChanges(lane);
        Change tail = lane.changes.peekLast();
        if (tail != null && tail.version > lane.sealedVersion) { tail.version = lane.version; tail.at = Math.min(tail.at, at); }
        else { lane.changes.addLast(new Change(lane.version, at)); }
        updateDirtyAt(lane);
    }
    /** 已终结且无快照引用的区段重新合并；显式失败保存也不能让风险日志无限增长。 */
    private void compactChanges(Lane lane) {
        java.util.TreeSet<Long> boundaries = new java.util.TreeSet<>();
        if (lane.active != null) { boundaries.add(lane.active.version); }
        for (Job job : lane.queue) { boundaries.add(job.version); }
        if (lane.captureVersion > lane.confirmedVersion) { boundaries.add(lane.captureVersion); }
        ArrayDeque<Change> compact = new ArrayDeque<>();
        for (Change change : lane.changes) {
            Change tail = compact.peekLast();
            Long boundary = tail == null ? null : boundaries.ceiling(tail.version);
            if (tail != null && (boundary == null || boundary >= change.version)) {
                tail.version = change.version; tail.at = Math.min(tail.at, change.at);
            } else { compact.addLast(change); }
        }
        lane.changes.clear(); lane.changes.addAll(compact);
    }
    /** 测试有界风险日志，不公开可修改的内部集合。 */
    int changeSegments(long id) {
        AtomicReference<Integer> size = new AtomicReference<>(0);
        lanes.computeIfPresent(id, (key, lane) -> { compactChanges(lane); size.set(lane.changes.size()); return lane; });
        return size.get();
    }
    /** 活动业务不能因中途保存成功而短暂变干净。 */
    private void updateDirtyAt(Lane lane) {
        lane.dirtyAt = null;
        for (Change change : lane.changes) { lane.dirtyAt = lane.dirtyAt == null ? change.at : Math.min(lane.dirtyAt, change.at); }
        for (Mutation mutation : lane.mutations) { lane.dirtyAt = lane.dirtyAt == null ? mutation.at : Math.min(lane.dirtyAt, mutation.at); }
    }
    /** @return 最早尚未覆盖的保守业务变更年龄 */
    private long age(Lane lane) { return lane.dirtyAt == null ? 0 : Math.max(0, now() - lane.dirtyAt); }
    /** 保护一旦触发，只在所有风险被确认消除后解除。 */
    private void refreshRisk(long id, Lane lane) {
        updateDirtyAt(lane);
        long age = age(lane);
        if (!lane.warned && age >= policy.warnAfter()) { log.warn("玩家保存风险接近预算 playerId={} ageMs={}", id, age); lane.warned = true; }
        if (lane.unknown || age >= policy.budget()) { lane.protectedWrite = true; }
        if (!lane.risky() && !lane.unknown) { lane.protectedWrite = false; lane.warned = false; }
    }
    /** 在实际执行业务前检查，排队时的检查不能替代此处检查。 */
    public Mutation beginMutation(long id, Object owner) {
        AtomicReference<Mutation> result = new AtomicReference<>();
        lanes.compute(id, (key, lane) -> {
            requireOwner(lane, owner, id); refreshRisk(id, lane);
            if (lane.protectedWrite) { throw new SaveFailure("WRITE_PROTECTED", id, 0, null); }
            if (lane.mutations.size() >= 64) { throw new SaveFailure("MUTATION_CAPACITY", id, 0, null); }
            long at = now(); changed(lane, at);
            Mutation mutation = new Mutation(id, owner, lane.version, at);
            lane.mutations.add(mutation); result.set(mutation);
            return lane;
        });
        return result.get();
    }
    /** 只对明确尚未写入且暂时不可受理的错误重试；配置/行数错误不得热循环。 */
    private boolean retryable(Throwable cause) {
        for (Throwable c = cause; c != null; c = c.getCause()) {
            if (c instanceof cn.game.core.execute.TaskNotExecutedException || c instanceof java.util.concurrent.RejectedExecutionException || c instanceof java.sql.SQLTransientException
                    || c instanceof org.springframework.jdbc.CannotGetJdbcConnectionException) { return true; }
        }
        return false;
    }
    /** 实际失败不改变最早业务变化时间；重试次数不因新业务变化重置。 */
    private void failed(Lane lane, Throwable cause, boolean knownNotWritten) {
        lane.failure = String.valueOf(cause); lane.failures++;
        lane.automaticBlocked = !knownNotWritten || !retryable(cause) || lane.failures > policy.maxRetries();
        lane.retryAt = now() + Math.min(30_000, policy.backoff() * (1L << Math.min(8, lane.failures - 1)));
    }
    /** @return 无在途/不明/排队/活动业务时才能自动捕获新的快照 */
    private boolean automaticEligible(Lane lane) {
        return lane.version > lane.confirmedVersion && lane.active == null && lane.queue.isEmpty() && !lane.unknown
                && lane.mutations.isEmpty() && !lane.automaticBlocked && now() >= lane.retryAt
                && (lane.failures > 0 || age(lane) >= policy.saveAfter());
    }
    /** 一次性业务timer到期时执行；只有明确开始前的保护拒绝才延期。 */
    public void runOrDeferTimer(long id, Object owner, Runnable action) {
        Mutation mutation;
        try { mutation = beginMutation(id, owner); }
        catch (SaveFailure failure) {
            if (!"WRITE_PROTECTED".equals(failure.code)) { throw failure; }
            lanes.compute(id, (key, lane) -> {
                requireOwner(lane, owner, id);
                if (lane.deferredTimers.size() >= 64) { throw new SaveFailure("TIMER_BACKLOG_FULL", id, 0, failure); }
                lane.deferredTimers.addLast(new DeferredTimer(action)); return lane;
            });
            return;
        }
        try (mutation) { action.run(); }
    }
    /** 玩家取消全部timer时同时取消尚未开始的延期任务。 */
    public void cancelDeferredTimers(long id, Object owner) {
        lanes.computeIfPresent(id, (key, lane) -> { if (lane.owner == owner) { lane.deferredTimers.clear(); } return lane; });
    }
    /** 健康恢复后在相同玩家邮箱执行一次，出队与门禁原子化；旧owner/取消的任务不再执行。 */
    private void sweepTimers(BiFunction<Long, Supplier<Future<?>>, Future<?>> dispatch) {
        int dispatched = 0;
        for (long id : lanes.keySet()) {
            if (automaticOutstanding.get() >= policy.dispatchLimit()) { break; }
            AtomicReference<Lane> reserved = new AtomicReference<>(); AtomicReference<DeferredTimer> action = new AtomicReference<>();
            lanes.computeIfPresent(id, (key, lane) -> {
                refreshRisk(id, lane);
                if (!lane.protectedWrite && !lane.timerPending && !lane.deferredTimers.isEmpty()) {
                    if (automaticOutstanding.incrementAndGet() <= policy.dispatchLimit()) {
                        lane.timerPending = true; reserved.set(lane); action.set(lane.deferredTimers.peekFirst());
                    } else { automaticOutstanding.decrementAndGet(); }
                }
                return lane;
            });
            Lane reservation = reserved.get(); if (reservation == null) { continue; }
            Future<?> result;
            try {
                result = dispatch.apply(id, () -> {
                    AtomicReference<Mutation> scope = new AtomicReference<>();
                    lanes.computeIfPresent(id, (key, lane) -> {
                        refreshRisk(id, lane);
                        if (lane == reservation && !lane.protectedWrite && lane.deferredTimers.peekFirst() == action.get()) {
                            lane.deferredTimers.removeFirst(); long at = now(); changed(lane, at);
                            Mutation mutation = new Mutation(id, lane.owner, lane.version, at);
                            lane.mutations.add(mutation); scope.set(mutation);
                        }
                        return lane;
                    });
                    if (scope.get() != null) { try (Mutation ignored = scope.get()) { action.get().action().run(); } }
                    return Future.succeededFuture();
                });
            } catch (RuntimeException failure) { log.warn("延期timer投递失败 playerId={}", id, failure); result = Future.failedFuture(failure); }
            result.onComplete(done -> {
                automaticOutstanding.decrementAndGet();
                lanes.computeIfPresent(id, (key, lane) -> { if (lane == reservation) { lane.timerPending = false; } return lane; });
                if (done.failed()) { log.warn("延期timer未完成，已开始的业务不重放 playerId={}", id, done.cause()); }
            });
            if (++dispatched >= policy.dispatchLimit()) { break; }
        }
    }
    /**
     * 独立维护扫描，连接/玩家业务定时器取消后仍运行。每玩家最多一个待派发任务，每轮有上限。
     * dispatch 必须将传入任务放入玩家邮箱；save 仅在真正执行时调用，不在扫描线程捕获数据。
     */
    public void sweep(BiFunction<Long, Supplier<Future<?>>, Future<?>> dispatch, BiFunction<Long, Object, Future<?>> save) {
        sweepTimers(dispatch);
        int dispatched = 0;
        for (long id : lanes.keySet()) {
            if (automaticOutstanding.get() >= policy.dispatchLimit()) { break; }
            AtomicReference<Lane> reserved = new AtomicReference<>();
            lanes.computeIfPresent(id, (key, lane) -> {
                refreshRisk(id, lane);
                if (!lane.automaticPending && automaticEligible(lane)) {
                    int count = automaticOutstanding.incrementAndGet();
                    if (count <= policy.dispatchLimit()) { lane.automaticPending = true; reserved.set(lane); }
                    else { automaticOutstanding.decrementAndGet(); }
                }
                return lane;
            });
            Lane reservation = reserved.get();
            if (reservation == null) { continue; }
            AtomicLong terminalBefore = new AtomicLong(-1);
            try {
                dispatch.apply(id, () -> {
                    AtomicReference<Boolean> allowed = new AtomicReference<>(false);
                    lanes.computeIfPresent(id, (key, lane) -> {
                        allowed.set(lane == reservation && lane.automaticPending && automaticEligible(lane));
                        if (allowed.get()) { terminalBefore.set(lane.terminalSequence); }
                        return lane;
                    });
                    return allowed.get() ? save.apply(id, reservation.owner) : Future.succeededFuture();
                }).onComplete(result -> {
                    automaticOutstanding.decrementAndGet();
                    lanes.computeIfPresent(id, (key, lane) -> {
                        if (lane == reservation) {
                            lane.automaticPending = false;
                            // 实际写入的失败由 finish 处理，等待超时不创建重试；此处分派/捕获异常有界停住。
                            if (result.failed() && lane.active == null
                                    && (terminalBefore.get() < 0 || lane.terminalSequence == terminalBefore.get())) {
                                failed(lane, result.cause(), true);
                            }
                        }
                        return lane;
                    });
                });
            } catch (RuntimeException failure) {
                log.warn("自动保存派发失败 playerId={}", id, failure);
                automaticOutstanding.decrementAndGet();
                lanes.computeIfPresent(id, (key, lane) -> {
                    if (lane == reservation) { lane.automaticPending = false; failed(lane, failure, true); }
                    return lane;
                });
            }
            if (++dispatched >= policy.dispatchLimit()) { break; }
        }
    }
    /** @param id 玩家 @param owner 待登记对象 @param register 原子登记动作，有旧对象时必须拒绝替换 */
    public void register(long id, Object owner, Runnable register) {
        lanes.compute(id, (key, lane) -> {
            if (lane != null && lane.owner != owner) { throw new IllegalStateException("旧玩家仍驻留，禁止替换 playerId=" + id); }
            register.run();
            if (lane == null) { lane = new Lane(); lane.owner = owner; }
            return lane;
        });
    }
    /** @param id 玩家 @param owner 原对象身份 @param remove 身份匹配删除动作 @return 仅健康且确实删除时为 true */
    public boolean release(long id, Object owner, BooleanSupplier remove) {
        AtomicReference<Boolean> removed = new AtomicReference<>(false);
        lanes.compute(id, (key, lane) -> {
            if (lane != null && (lane.owner != owner || lane.risky())) { return lane; }
            removed.set(remove.getAsBoolean());
            return removed.get() ? null : lane;
        });
        return removed.get();
    }
}
