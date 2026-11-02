package cn.game.games.net.game.helper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import cn.game.core.execute.SaveMailboxFixture;
import cn.game.core.net.process.IdVirtualThreadProcessor;
import cn.game.core.util.AsyncUtils;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.clazz.ClassManager;
import cn.game.testsupport.SaveProbeModule;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/** 确定性控制真实完成与等待超时，验证保存通道而非模拟数据库成功即可放行的错误假设。 */
class PlayerSaveOrderingTest {
    /** 只加载玩家模块扫描所需元数据，不启动服务器。 */
    @BeforeAll
    static void metadata() { ClassManager.getInstance().init(); }
    /** A 等待超时后 B 仍不进入写入器；其他玩家推进，最终替身中同玩家的值为 B。 */
    @Test
    void timeoutKeepsWriteOwnershipAndOtherPlayersProgress() throws Exception {
        Harness h = new Harness(2);
        Object owner = new Object();
        var a = h.submit(owner, 1, "A");
        var b = h.submit(owner, 1, "B");
        var other = h.submit(new Object(), 2, "other");
        h.timer.expire(0);
        Assertions.assertEquals("WAIT_TIMEOUT", failure(a.result()).code);
        Assertions.assertEquals(2, h.writer.rows.size());
        Assertions.assertEquals(1, h.saves.status(1).queued());
        Assertions.assertFalse(h.saves.status(1).workerExited());
        h.writer.commit(1);
        Assertions.assertEquals(other.generation(), await(other.result()).generation());
        h.writer.commit(0);
        Assertions.assertEquals("B", h.writer.rows.get(2).getName());
        h.writer.commit(2);
        Assertions.assertEquals(b.generation(), await(b.result()).generation());
        Assertions.assertEquals("B", h.writer.stored.get(1L));
        Assertions.assertEquals("WAIT_TIMEOUT", failure(a.result()).code);
        Assertions.assertFalse(h.saves.status(1).observedDirty());
    }
    /** UNKNOWN 不自动重投、不放 B；旧进程/错代次/重复裁决拒绝，受信核对后才继续。 */
    @Test
    void unknownRequiresMatchingAuditedReconciliation() throws Exception {
        Harness h = new Harness(2); Object owner = new Object();
        var a = h.submit(owner, 1, "A"); var b = h.submit(owner, 1, "B");
        var before = h.saves.status(1);
        Assertions.assertThrows(IllegalStateException.class, () -> h.saves.reconcile(before.coordinatorId(), 1,
                a.generation(), PlayerSaveCoordinator.Resolution.COMMITTED, evidence()));
        h.writer.unknown(0);
        Assertions.assertEquals("UNKNOWN", failure(a.result()).code);
        var unknown = h.saves.status(1);
        Assertions.assertEquals("UNKNOWN", unknown.state());
        Assertions.assertTrue(unknown.workerExited());
        Assertions.assertEquals(1, h.writer.rows.size());
        Assertions.assertThrows(IllegalStateException.class, () -> h.saves.reconcile("old-process", 1,
                a.generation(), PlayerSaveCoordinator.Resolution.COMMITTED, evidence()));
        Assertions.assertThrows(IllegalStateException.class, () -> h.saves.reconcile(unknown.coordinatorId(), 1,
                b.generation(), PlayerSaveCoordinator.Resolution.COMMITTED, evidence()));
        Assertions.assertThrows(IllegalStateException.class, () -> h.saves.reconcile(unknown.coordinatorId(), 2,
                a.generation(), PlayerSaveCoordinator.Resolution.COMMITTED, evidence()));
        Assertions.assertEquals("UNKNOWN_BLOCKED", failure(h.submit(owner, 1, "C").result()).code);
        h.saves.reconcile(unknown.coordinatorId(), 1, a.generation(), PlayerSaveCoordinator.Resolution.COMMITTED, evidence());
        Assertions.assertEquals(2, h.writer.rows.size());
        Assertions.assertEquals(a.generation(), h.saves.status(1).lastConfirmedGeneration());
        h.writer.commit(1); await(b.result());
        Assertions.assertTrue(h.saves.status(1).observedDirty(), "被拒绝 C 的状态不能由旧 B 确认清零");
        Assertions.assertEquals("UNKNOWN", failure(a.result()).code, "恢复不得改写已经返回的失败");
        Assertions.assertThrows(IllegalStateException.class, () -> h.saves.reconcile(unknown.coordinatorId(), 1,
                a.generation(), PlayerSaveCoordinator.Resolution.COMMITTED, evidence()));
    }
    /** 未提交裁决保留脏状态；空证据与单次读取不提供自动解锁路径。 */
    @Test
    void notCommittedKeepsDirtyAndNoAutomaticReadUnlockExists() throws Exception {
        Harness h = new Harness(1); Object owner = new Object();
        var a = h.submit(owner, 1, "A"); h.writer.unknown(0); failure(a.result());
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PlayerSaveCoordinator.RecoveryEvidence(
                "operator", "database", "incident", "", "read A"));
        var state = h.saves.status(1);
        h.saves.reconcile(state.coordinatorId(), 1, a.generation(), PlayerSaveCoordinator.Resolution.NOT_COMMITTED, evidence());
        Assertions.assertEquals("IDLE", h.saves.status(1).state());
        Assertions.assertTrue(h.saves.status(1).observedDirty());
        Assertions.assertEquals(0, h.saves.status(1).lastConfirmedGeneration());
        Assertions.assertFalse(h.saves.release(1, owner, () -> true));
    }
    /** 队列满明确失败，A/B 完成不能消除 C 风险；新快照成功才清除已观察状态。 */
    @Test
    void boundedQueueRetainsRejectedDirtyVersion() throws Exception {
        Harness h = new Harness(1); Object owner = new Object();
        var a = h.submit(owner, 1, "A"); var b = h.submit(owner, 1, "B");
        Assertions.assertEquals("QUEUE_FULL", failure(h.submit(owner, 1, "C").result()).code);
        h.writer.commit(0); h.writer.commit(1); await(a.result()); await(b.result());
        Assertions.assertTrue(h.saves.status(1).observedDirty());
        var d = h.submit(owner, 1, "D"); h.writer.commit(2); await(d.result());
        Assertions.assertFalse(h.saves.status(1).observedDirty());
    }
    /** 真实失败归属本代，已证明未写入可推进；同步抛错无法证明未写入则隔离。 */
    @Test
    void perGenerationFailuresAndImmediateResultsAreDistinct() throws Exception {
        Harness h = new Harness(2); Object owner = new Object();
        var a = h.submit(owner, 1, "A"); var b = h.submit(owner, 1, "B");
        Throwable rejected = new IllegalStateException("not started");
        h.writer.results.get(0).complete(new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.NOT_WRITTEN, rejected));
        Assertions.assertSame(rejected, failure(a.result()).getCause());
        h.writer.commit(1);
        Assertions.assertEquals(b.generation(), await(b.result()).generation());
        var immediate = new PlayerSaveCoordinator(1, 100, (data, insert) -> Future.succeededFuture(
                new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.COMMITTED, null)), h.timer, h.clock::get);
        Assertions.assertNotNull(await(immediate.submit(owner, data(1, "immediate"), false).result()));
        var broken = new PlayerSaveCoordinator(1, 100, (data, insert) -> { throw new IllegalStateException("unknown side effect"); }, h.timer, h.clock::get);
        Assertions.assertEquals("UNKNOWN", failure(broken.submit(owner, data(1, "broken"), false).result()).code);
        Assertions.assertEquals("UNKNOWN", broken.status(1).state());
    }
    /** 对象保留/替换与通道状态原子协调；旧回调不能删除新对象，新代次不能复用。 */
    @Test
    void lifecycleRetainsRiskAndRejectsStaleOwner() throws Exception {
        Harness h = new Harness(2); Object old = new Object(); Object replacement = new Object();
        h.saves.register(1, old, () -> {});
        var a = h.submit(old, 1, "A");
        Assertions.assertFalse(h.saves.release(1, old, () -> Assertions.fail("must retain")));
        Assertions.assertThrows(IllegalStateException.class, () -> h.saves.register(1, replacement, () -> Assertions.fail("must not replace")));
        h.writer.commit(0); await(a.result());
        Assertions.assertTrue(h.saves.release(1, old, () -> true));
        h.saves.register(1, replacement, () -> {});
        Assertions.assertFalse(h.saves.release(1, old, () -> Assertions.fail("stale removal")));
        Assertions.assertEquals("OWNER_CHANGED", failure(h.submit(old, 1, "old").result()).code);
        var b = h.submit(replacement, 1, "B");
        Assertions.assertTrue(b.generation() > a.generation());
        h.writer.commit(1); await(b.result());
    }
    /** 诊断区分确认时间、覆盖时点和未知变更时间，断线列表包含驻留风险对象。 */
    @Test
    void diagnosticsDoNotInventBusinessMutationTime() throws Exception {
        Harness h = new Harness(2); Object owner = new Object();
        h.saves.register(1, owner, () -> {});
        h.saves.markDirty(1, 90);
        var a = h.submit(owner, 1, "A");
        Assertions.assertEquals(90L, h.saves.status(1).earliestUnsavedChangeAt());
        Assertions.assertEquals(100L, h.saves.status(1).riskFirstObservedSnapshotAt());
        Assertions.assertEquals(1, h.saves.disconnectedDirtyPlayers(id -> id == 1).size());
        h.clock.set(200); h.writer.commit(0); await(a.result());
        var confirmed = h.saves.status(1);
        Assertions.assertEquals(100, confirmed.confirmedSnapshotAt());
        Assertions.assertEquals(200, confirmed.lastConfirmedAt());
        Assertions.assertNull(confirmed.earliestUnsavedChangeAt());
        h.submit(owner, 1, "B");
        Assertions.assertEquals(200L, h.saves.status(1).earliestUnsavedChangeAt(), "手工快照按观察时点保守登记");
    }
    /** 使用真实邮箱执行保存并 await，独立完成不回投该邮箱。 */
    @Test
    void mailboxCanAwaitSaveWithoutSelfDeadlock() throws Exception {
        Harness h = new Harness(2);
        CountDownLatch started = new CountDownLatch(1);
        try (var service = SaveMailboxFixture.create()) {
            var processor = new IdVirtualThreadProcessor(service);
            Future<PlayerSaveCoordinator.Receipt> result = processor.process(1, () -> {
                var save = h.submit(new Object(), 1, "mailbox");
                started.countDown();
                return AsyncUtils.await(save.result(), 5, TimeUnit.SECONDS);
            }, null);
            Assertions.assertTrue(started.await(5, TimeUnit.SECONDS));
            h.writer.commit(0);
            Assertions.assertEquals(1, await(result).playerId());
        }
    }
    /** 等待者的同步回调等待后继，不得阻塞后继开始或完成。 */
    @Test
    void waiterCallbackCannotBlockNextWrite() throws Exception {
        Harness h = new Harness(2); Object owner = new Object();
        var a = h.submit(owner, 1, "A"); var b = h.submit(owner, 1, "B");
        CountDownLatch callback = new CountDownLatch(1);
        a.result().onSuccess(done -> { AsyncUtils.await(b.result(), 5, TimeUnit.SECONDS); callback.countDown(); });
        h.writer.commit(0);
        Assertions.assertEquals(2, h.writer.rows.size());
        h.writer.commit(1);
        Assertions.assertTrue(callback.await(5, TimeUnit.SECONDS));
    }
    /** 新建与周期/退出/充值共用的保存装配接入同一通道，S2A 参数仍隔离。 */
    @Test
    void realHelperEntrypointsShareTheLane() throws Exception {
        Harness h = new Harness(2); FixturePlayer player = new FixturePlayer(data(1, "A"));
        h.saves.register(1, player, () -> {});
        Future<Player> created = PlayerHelper.savePlayerToDb(player, h.saves);
        player.getData().setName("B");
        Future<List<Object>> saved = PlayerHelper.saveClientCache(1, id -> player, () -> true, h.saves,
                tasks -> { throw new AssertionError("unexpected standalone"); });
        Assertions.assertEquals(1, h.writer.rows.size());
        Assertions.assertTrue(h.writer.inserts.get(0));
        h.writer.commit(0);
        Assertions.assertSame(player, await(created));
        Assertions.assertFalse(h.writer.inserts.get(1));
        Assertions.assertEquals("A", h.writer.rows.get(0).getName());
        Assertions.assertEquals("B", h.writer.rows.get(1).getName());
        h.writer.commit(1); Assertions.assertEquals(1, await(saved).size());
    }
    /** 准备阶段抛错后仍保留权威对象，缺失/不活跃不生成伪提交回执。 */
    @Test
    void preparationAndNoOpCannotReportPersisted() throws Exception {
        Harness h = new Harness(2);
        PlayerData bad = new PlayerData() { @Override public void beforeSave() { throw new IllegalStateException("prepare"); } };
        bad.setPlayerId(1); FixturePlayer player = new FixturePlayer(bad);
        h.saves.register(1, player, () -> {});
        Assertions.assertThrows(IllegalStateException.class, () -> PlayerHelper.savePlayerToDb(player, h.saves));
        Assertions.assertFalse(h.saves.release(1, player, () -> true));
        Assertions.assertEquals("PLAYER_MISSING", failure(PlayerHelper.saveClientCache(2, id -> null, () -> true, h.saves,
                tasks -> Future.succeededFuture())).code);
        player.setActive(false);
        Assertions.assertEquals("SAVE_SKIPPED", failure(PlayerHelper.saveClientCache(1, id -> player, () -> true, h.saves,
                tasks -> Future.succeededFuture())).code);
        Assertions.assertTrue(h.writer.rows.isEmpty());
    }
    /** 排队等待超时不取消关键快照，它仍按顺序执行并通过状态暴露实际确认。 */
    @Test
    void queuedTimeoutDoesNotDiscardSnapshot() throws Exception {
        Harness h = new Harness(1); Object owner = new Object();
        var a = h.submit(owner, 1, "A"); var b = h.submit(owner, 1, "B");
        h.timer.expire(1); Assertions.assertEquals("WAIT_TIMEOUT", failure(b.result()).code);
        h.writer.commit(0); await(a.result());
        Assertions.assertEquals("B", h.writer.rows.get(1).getName());
        h.writer.commit(1);
        Assertions.assertEquals(b.generation(), h.saves.status(1).lastConfirmedGeneration());
        Assertions.assertEquals("WAIT_TIMEOUT", failure(b.result()).code);
    }
    /** 已清理旧对象不得从捕获入口重新登记，防止迟到任务复活旧数据。 */
    @Test
    void removedOwnerCannotPrepareAgain() {
        Harness h = new Harness(1); Object owner = new Object();
        h.saves.register(1, owner, () -> {});
        Assertions.assertTrue(h.saves.release(1, owner, () -> true));
        Assertions.assertEquals("PLAYER_NOT_REGISTERED", Assertions.assertThrows(PlayerSaveCoordinator.SaveFailure.class,
                () -> h.saves.preparing(owner, 1)).code);
    }
    /** 部分确认后风险起点仍是保守区间起点，剩余业务变更时点不以已覆盖的旧值冒充。 */
    @Test
    void partialConfirmationKeepsOnlyConservativeRiskStart() throws Exception {
        Harness h = new Harness(2); Object owner = new Object(); h.saves.register(1, owner, () -> {});
        h.saves.markDirty(1, 90); var a = h.submit(owner, 1, "A");
        h.clock.set(200); h.saves.markDirty(1, 150); var b = h.submit(owner, 1, "B");
        h.writer.commit(0); await(a.result());
        Assertions.assertEquals(100L, h.saves.status(1).riskFirstObservedSnapshotAt());
        Assertions.assertEquals(150L, h.saves.status(1).earliestUnsavedChangeAt());
        h.writer.commit(1); await(b.result());
        Assertions.assertNull(h.saves.status(1).riskFirstObservedSnapshotAt());
    }
    /** @return 测试明确声明的受信核对证据；不代表进行了真实数据库核对 */
    private static PlayerSaveCoordinator.RecoveryEvidence evidence() {
        return new PlayerSaveCoordinator.RecoveryEvidence("test operator", "fake database", "fixture incident",
                "fixture explicitly confirms no old transaction can execute", "fixture confirms terminal result");
    }
    /** @param id 玩家 @param name 本代内容 @return 已独占的测试参数 */
    private static PlayerData data(long id, String name) { PlayerData data = new PlayerData(); data.setPlayerId(id); data.setName(name); data.setModules(null); return data; }
    /** @param result 本次 Future @return 有界等待的成功结果 */
    private static <T> T await(Future<T> result) throws Exception { return result.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS); }
    /** @param result 本次 Future @return 原始保存失败，拒绝将测试等待超时当作预期失败 */
    private static PlayerSaveCoordinator.SaveFailure failure(Future<?> result) {
        ExecutionException error = Assertions.assertThrows(ExecutionException.class,
                () -> result.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS));
        return Assertions.assertInstanceOf(PlayerSaveCoordinator.SaveFailure.class, error.getCause());
    }
    /** 真实 Player 类与真实模块集合，省去完整业务初始化。 */
    private static final class FixturePlayer extends Player {
        /** 在线数据。 */
        private final PlayerData data;
        /** @param data 在线数据 */
        FixturePlayer(PlayerData data) { this.data = data; getModules().put("probe", new SaveProbeModule()); }
        @Override public PlayerData getData() { return data; }
        @Override public long getPlayerId() { return data.getPlayerId(); }
    }
    /** 可控定时器，不使用 sleep 猜测交错。 */
    private static final class ManualTimer implements PlayerSaveCoordinator.Timer {
        /** 等待到期动作；取消不移除索引。 */
        final List<Runnable> deadlines = new CopyOnWriteArrayList<>();
        @Override public Runnable schedule(Runnable action, long delay) {
            int index = deadlines.size(); deadlines.add(action); return () -> deadlines.set(index, () -> {});
        }
        /** @param index 指定请求等待到期 */
        void expire(int index) { deadlines.get(index).run(); }
    }
    /** 原参数保留到显式执行，不在写入替身内复制或提前完成。 */
    private static final class ManualWriter implements PlayerSaveCoordinator.Writer {
        /** 已进入的参数。 */
        final List<PlayerData> rows = new CopyOnWriteArrayList<>();
        /** 对应操作类型。 */
        final List<Boolean> inserts = new CopyOnWriteArrayList<>();
        /** 对应真实执行结果。 */
        final List<Promise<PlayerSaveCoordinator.WriteResult>> results = new CopyOnWriteArrayList<>();
        /** 仅用于时序断言的存储替身，不是数据库验收。 */
        final java.util.Map<Long, String> stored = new java.util.concurrent.ConcurrentHashMap<>();
        @Override public Future<PlayerSaveCoordinator.WriteResult> write(PlayerData data, boolean insert) {
            rows.add(data); inserts.add(insert); Promise<PlayerSaveCoordinator.WriteResult> result = Promise.promise(); results.add(result); return result.future();
        }
        /** @param index 明确完成指定写入并确认提交 */
        void commit(int index) { PlayerData row = rows.get(index); stored.put(row.getPlayerId(), row.getName()); results.get(index).complete(new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.COMMITTED, null)); }
        /** @param index 模拟连接异常，库侧结果无法确定 */
        void unknown(int index) { results.get(index).complete(new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.UNKNOWN, new IllegalStateException("connection lost"))); }
    }
    /** 每个测试独立通道及资源，不替换全局生产状态。 */
    private static final class Harness {
        /** 原始写入替身。 */
        final ManualWriter writer = new ManualWriter();
        /** 可控超时。 */
        final ManualTimer timer = new ManualTimer();
        /** 可控毫秒时间。 */
        final AtomicLong clock = new AtomicLong(100);
        /** 被测通道。 */
        final PlayerSaveCoordinator saves;
        /** @param capacity 队列容量 */
        Harness(int capacity) { saves = new PlayerSaveCoordinator(capacity, 100, writer, timer, clock::get); }
        /** @param owner 对象身份 @param id 玩家 @param name 快照标识 @return 本代请求 */
        PlayerSaveCoordinator.Submission submit(Object owner, long id, String name) { return saves.submit(owner, data(id, name), false); }
    }
}
