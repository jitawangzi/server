package cn.game.games.net.game.helper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
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
 * 此处只跟踪已观察的保存与显式 markDirty，不宣称覆盖所有业务变更或跨进程恢复。
 */
public final class PlayerSaveCoordinator {
    /** 保存状态和恢复裁决日志。 */
    private static final Logger log = LoggerFactory.getLogger(PlayerSaveCoordinator.class);
    /** 当前进程共享保存通道；首次使用才初始化资源。 */
    private static final class Shared {
        /** 独立等待计时器，不执行数据库工作。 */
        private static final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(
                Thread.ofPlatform().daemon(true).name("player-save-timeout").factory());
        /** 每玩家最多 16 个排队快照；所有请求都不合并，默认等待 30 秒。 */
        private static final PlayerSaveCoordinator instance = new PlayerSaveCoordinator(16, 30_000,
                new PlayerSnapshotWriter()::write, (action, delay) -> {
                    var scheduled = timer.schedule(action, delay, TimeUnit.MILLISECONDS);
                    return () -> scheduled.cancel(false);
                }, DateUtil::currentTimeMillis);
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
            boolean waiterTimedOut, boolean workerExited, String failure) {}
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
        /** 当前风险期间首次观察到快照的时点；部分确认不将其伪装为当前最早未确认快照。 */
        Long snapshotAt;
        /** 最近失败信息。 */
        String failure;
        /** @return 是否必须保留权威对象 */
        boolean risky() { return active != null || !queue.isEmpty() || version > confirmedVersion; }
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
        if (capacity < 1 || waitMillis < 1) { throw new IllegalArgumentException("保存容量和等待期限必须为正"); }
        this.capacity = capacity; this.waitMillis = waitMillis;
        this.writer = Objects.requireNonNull(writer); this.timer = Objects.requireNonNull(timer);
        this.clock = Objects.requireNonNull(clock);
    }
    /** @param owner 权威对象 @param id 玩家；捕获前登记风险，准备失败也不能清理玩家 */
    void preparing(Object owner, long id) {
        lanes.compute(id, (key, lane) -> {
            if (lane == null) { throw new SaveFailure("PLAYER_NOT_REGISTERED", id, 0, null); }
            if (lane.owner != owner) { throw new SaveFailure("OWNER_CHANGED", id, 0, null); }
            lane.version++;
            return lane;
        });
    }
    /**
     * 接收已经捕获的参数，不在 IO 回调中访问在线 Player；拒绝也保留已观察未保存状态。
     * @param owner 权威玩家身份 @param data 独占参数 @param insert 是否创建 @return 本代等待视图
     */
    Submission submit(Object owner, PlayerData data, boolean insert) {
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
            lane.version++;
            if (lane.snapshotAt == null) { lane.snapshotAt = clock.getAsLong(); }
            if (lane.unknown || lane.queue.size() >= capacity) {
                String code = lane.unknown ? "UNKNOWN_BLOCKED" : "QUEUE_FULL";
                lane.failure = code;
                rejected.set(new SaveFailure(code, id, generation, null));
                return lane;
            }
            Job job = new Job(data, insert, generation, clock.getAsLong(), lane.version);
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
            if (result.outcome() == Outcome.COMMITTED) { receipt.set(confirm(id, lane, job)); }
            else { lane.failure = String.valueOf(result.cause()); }
            if (result.outcome() == Outcome.UNKNOWN) { lane.unknown = true; }
            else { lane.active = lane.queue.pollFirst(); next.set(lane.active); }
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
        Receipt receipt = new Receipt(id, job.generation, job.capturedAt, clock.getAsLong());
        lane.confirmed = receipt; lane.confirmedVersion = job.version;
        if (lane.version == job.version) { lane.dirtyAt = null; lane.snapshotAt = null; lane.failure = null; }
        else { lane.dirtyAt = null; } // 未保留全业务变更日志，部分确认后无法推知剩余变更的精确起点。
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
            return lane;
        });
        if (next.get() != null) { start(id, next.get()); }
        return status(id);
    }
    /** @param id 玩家 @return 当前状态，未知玩家返回 null，不将其视为已持久化 */
    public Status status(long id) {
        AtomicReference<Status> value = new AtomicReference<>();
        lanes.computeIfPresent(id, (key, lane) -> {
            Receipt c = lane.confirmed;
            value.set(new Status(identity, id, lane.unknown ? "UNKNOWN" : lane.active != null ? "IN_FLIGHT" : "IDLE",
                    lane.active == null ? 0 : lane.active.generation, lane.queue.size(), c == null ? 0 : c.generation(),
                    c == null ? 0 : c.committedAt(), c == null ? 0 : c.capturedAt(), lane.version > lane.confirmedVersion,
                    lane.dirtyAt, lane.snapshotAt, lane.active != null && lane.active.timedOut, lane.unknown, lane.failure));
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
            if (lane.version == lane.confirmedVersion) { lane.dirtyAt = changedAt; }
            else if (lane.dirtyAt != null) { lane.dirtyAt = Math.min(lane.dirtyAt, changedAt); }
            lane.version++;
            return lane;
        });
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
