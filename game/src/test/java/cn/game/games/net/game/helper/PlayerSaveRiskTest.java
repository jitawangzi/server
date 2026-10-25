package cn.game.games.net.game.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.games.net.client.GameClient;
import cn.game.core.net.protocol.bytes.ByteArrayProtocol;
import cn.game.core.net.process.IdVirtualThreadProcessor;
import cn.game.core.execute.SaveMailboxFixture;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.manual.ErrorMsgEnum;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/** 可控时钟、真实网络分派及邮箱验证风险预算；不将内存替身当作真实数据库冷读。 */
class PlayerSaveRiskTest {
    /** 只初始化模块类型元数据。 */
    @BeforeAll static void metadata() { ClassManager.getInstance().init(); }

    /** 冷加载很久未发生业务变化仍健康，加载不伪造任何提交回执。 */
    @Test void coldLoadDoesNotInventCommitOrDirtyWindow() {
        Harness h = new Harness(); h.time.set(9_000_000);
        h.sweep();
        Assertions.assertEquals(0, h.saves.status(1).lastConfirmedAt());
        Assertions.assertNull(h.saves.status(1).earliestUnsavedChangeAt());
        Assertions.assertFalse(h.saves.status(1).writeProtected());
        Assertions.assertEquals(0, h.writes.size());
        h.mutate();
        Assertions.assertEquals(9_000_000L, h.saves.status(1).earliestUnsavedChangeAt());
    }
    /** 提前保存、告警、精确预算边界及回拨不会让风险变年轻。 */
    @Test void thresholdAndRollbackProtectBeforeAnySideEffect() {
        Harness h = new Harness(); h.mutate();
        h.time.set(240_099); h.sweep(); Assertions.assertTrue(h.writes.isEmpty());
        h.time.set(240_100); h.sweep(); Assertions.assertEquals(1, h.writes.size());
        h.time.set(270_100); Assertions.assertTrue(h.saves.status(1).warning());
        h.time.set(300_100);
        Assertions.assertEquals("WRITE_PROTECTED", Assertions.assertThrows(PlayerSaveCoordinator.SaveFailure.class, h::mutate).code);
        h.time.set(100); Assertions.assertEquals(300_000, h.saves.status(1).riskAgeMillis());
        Assertions.assertFalse(h.saves.release(1, h.owner, () -> true));
    }
    /** A 捕获后 B 变化，A 的迟到成功不能移除 B 的风险。 */
    @Test void lateOldCommitKeepsNewMutationStart() throws Exception {
        Harness h = new Harness(); h.mutate(); h.save();
        h.time.set(200); h.mutate(); h.time.set(300_200); h.commit(0);
        Assertions.assertEquals(200L, h.saves.status(1).earliestUnsavedChangeAt());
        Assertions.assertTrue(h.saves.status(1).writeProtected());
        var recovery = h.save(); h.commit(1); await(recovery);
        Assertions.assertFalse(h.saves.status(1).observedDirty());
        Assertions.assertFalse(h.saves.status(1).writeProtected());
    }
    /** 序列化前的捕获边界与提交之间发生变化，部分确认仍保留新变化时点。 */
    @Test void captureBoundarySplitsLaterChangeBeforeSubmission() {
        Harness h = new Harness(); h.mutate();
        h.time.set(110); var capture = h.saves.preparing(h.owner, 1);
        h.time.set(120); h.mutate();
        h.saves.submit(h.owner, data(), false, capture); h.commit(0);
        Assertions.assertEquals(120L, h.saves.status(1).earliestUnsavedChangeAt());
    }
    /** 活动作用域跨越保存成功时始终有风险，关闭幂等，完整新快照才解除保护。 */
    @Test void openScopeCannotBeReleasedByMidScopeCommit() {
        Harness h = new Harness(); var scope = h.saves.beginMutation(1, h.owner);
        h.save(); h.commit(0);
        Assertions.assertTrue(h.saves.status(1).observedDirty());
        Assertions.assertFalse(h.saves.release(1, h.owner, () -> true));
        h.time.set(300_100); Assertions.assertTrue(h.saves.status(1).writeProtected());
        scope.close(); scope.close();
        Assertions.assertEquals(100L, h.saves.status(1).earliestUnsavedChangeAt());
        h.save(); h.commit(1);
        Assertions.assertFalse(h.saves.status(1).writeProtected());
        Assertions.assertTrue(h.saves.release(1, h.owner, () -> true));
    }
    /** 反复明确失败也不能积累无界变更段或刷新原始风险起点。 */
    @Test void repeatedFailedSnapshotsKeepBoundedHistory() {
        Harness h = new Harness(); h.mutate();
        for (int i = 0; i < 20; i++) {
            h.time.incrementAndGet(); h.save();
            h.fail(i, new RejectedExecutionException("not admitted"));
        }
        Assertions.assertTrue(h.saves.changeSegments(1) <= 3);
        Assertions.assertEquals(100L, h.saves.status(1).earliestUnsavedChangeAt());
    }
    /** UNKNOWN 立即保护，等待超时不释放写入权，扫描不产生第二笔写入。 */
    @Test void timeoutAndUnknownNeverAutomaticallyResubmit() throws Exception {
        Harness h = new Harness(); h.mutate(); h.time.set(240_100); h.sweep();
        h.deadlines.getFirst().run();
        h.time.set(500_100); h.sweep(); Assertions.assertEquals(1, h.writes.size());
        h.writes.getFirst().complete(new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.UNKNOWN, new IllegalStateException("lost ack")));
        h.sweep(); Assertions.assertEquals(1, h.writes.size());
        Assertions.assertTrue(h.saves.status(1).writeProtected());
        Assertions.assertEquals("UNKNOWN", h.saves.status(1).state());
    }
    /** 确定尚未写入的临时错误按1/2/4秒退避，最多三次重试，新变化不重置上限。 */
    @Test void retryBackoffAndLimitRemainBounded() throws Exception {
        Harness h = new Harness(); h.mutate(); h.time.set(240_100);
        for (int i = 0; i < 4; i++) {
            h.sweep(); Assertions.assertEquals(i + 1, h.writes.size());
            h.fail(i, new RejectedExecutionException("busy")); awaitFailure(h.dispatches.getLast());
            long retry = h.saves.status(1).retryAt();
            h.time.set(retry - 1); h.sweep(); Assertions.assertEquals(i + 1, h.writes.size());
            h.mutate(); h.time.set(retry);
        }
        h.sweep(); Assertions.assertEquals(4, h.writes.size());
        Assertions.assertTrue(h.saves.status(1).automaticBlocked());
    }
    /** 已有失败后捕获异常仍须停止自动尝试，不能因时间戳变化进入无限重试。 */
    @Test void captureFailureAfterRetryIsAccountedAndBlocked() throws Exception {
        Harness h = new Harness(); h.mutate(); h.time.set(240_100); h.sweep();
        h.fail(0, new RejectedExecutionException("busy")); awaitFailure(h.dispatches.getLast());
        h.time.set(h.saves.status(1).retryAt());
        h.saves.sweep((id, task) -> {
            try { return task.get(); } catch (RuntimeException e) { org.slf4j.LoggerFactory.getLogger(getClass()).warn("预期捕获失败", e); return Future.failedFuture(e); }
        }, (id, owner) -> { h.saves.preparing(owner, id); throw new IllegalStateException("snapshot failed"); });
        Assertions.assertEquals(2, h.saves.status(1).consecutiveFailures());
        Assertions.assertTrue(h.saves.status(1).automaticBlocked());
        h.time.set(900_000); h.sweep(); Assertions.assertEquals(1, h.writes.size());
    }
    /** 旧超时通知晚于实际提交成功时不能重新污染已恢复状态。 */
    @Test void lateWaitFailureCannotUndoRealCommit() {
        Harness h = new Harness(); h.mutate(); h.time.set(240_100);
        Promise<Void> late = Promise.promise();
        h.saves.sweep((id, task) -> { task.get(); return late.future(); }, (id, owner) -> h.save());
        h.commit(0); late.fail(new IllegalStateException("old wait timeout"));
        Assertions.assertEquals(0, h.saves.status(1).consecutiveFailures());
        Assertions.assertFalse(h.saves.status(1).automaticBlocked());
        h.mutate(); h.time.addAndGet(240_000); h.sweep();
        Assertions.assertEquals(2, h.writes.size());
    }
    /** 明确的永久失败不自动重试，新的人工确认仍可恢复。 */
    @Test void permanentFailureRequiresExplicitRecoverySave() throws Exception {
        Harness h = new Harness(); h.mutate(); h.time.set(240_100); h.sweep();
        h.fail(0, new IllegalStateException("zero rows")); awaitFailure(h.dispatches.getLast());
        h.time.set(400_100); h.sweep(); Assertions.assertEquals(1, h.writes.size());
        Assertions.assertTrue(h.saves.status(1).writeProtected());
        h.save(); h.commit(1); Assertions.assertFalse(h.saves.status(1).writeProtected());
    }
    /** 派发拒绝也有退避；取消玩家业务timer/连接不会影响独立巡检。 */
    @Test void rejectedMaintenanceDispatchIsRetriedWithoutLoginTimers() {
        Harness h = new Harness(); h.mutate(); h.time.set(240_100);
        h.saves.sweep((id, task) -> Future.failedFuture(new RejectedExecutionException("mailbox full")), (id, owner) -> h.save());
        Assertions.assertEquals(1, h.saves.status(1).consecutiveFailures());
        h.sweep(); Assertions.assertTrue(h.writes.isEmpty());
        h.time.set(h.saves.status(1).retryAt()); h.sweep(); Assertions.assertEquals(1, h.writes.size());
    }
    /** 重复扫描只有一个挂起任务，已变更的对象身份不能由迟到维护任务复活。 */
    @Test void delayedMaintenanceIsUniqueAndChecksOwnerAgain() {
        Harness h = new Harness(); h.mutate(); h.time.set(240_100);
        List<Supplier<Future<?>>> queued = new ArrayList<>(); Promise<Void> held = Promise.promise();
        for (int i = 0; i < 20; i++) { h.saves.sweep((id, task) -> { queued.add(task); return held.future(); }, (id, owner) -> h.save()); }
        Assertions.assertEquals(1, queued.size());
        h.save(); h.commit(0); Assertions.assertTrue(h.saves.release(1, h.owner, () -> true));
        h.saves.register(1, new Object(), () -> {}); queued.getFirst().get(); held.complete();
        Assertions.assertEquals(1, h.writes.size());
        Assertions.assertEquals(0, h.saves.status(1).lastConfirmedAt());
    }
    /** 真实玩家邮箱执行维护捕获，等待事务结果不向同一邮箱回投完成通知。 */
    @Test void actualMailboxCanSaveProtectedDisconnectedResident() throws Exception {
        Harness h = new Harness(); h.mutate(); h.time.set(300_100);
        try (var service = SaveMailboxFixture.create()) {
            var processor = new IdVirtualThreadProcessor(service);
            Promise<Void> captured = Promise.promise();
            h.saves.sweep((id, task) -> processor.process(id, task::get, result -> result), (id, owner) -> {
                Future<?> result = h.save(); captured.complete(); return result;
            });
            await(captured.future()); Assertions.assertTrue(h.saves.status(1).writeProtected());
            h.commit(0); Assertions.assertFalse(h.saves.status(1).writeProtected());
        }
    }
    /** 无 InitialUI 的真实网络入口也必须拦截，拒绝只回一次且绑定本次序号。 */
    @Test void networkWithoutInitialUiRejectsBeforeInvocation() {
        FixturePlayer player = new FixturePlayer(); Harness h = new Harness(player);
        AtomicInteger invoked = new AtomicInteger(); TestHandler handler = new TestHandler(player, h.saves, invoked::incrementAndGet);
        RecordingClient client = new RecordingClient();
        handler.dispatch(client, new ByteArrayProtocol(123, new byte[0], 11));
        Assertions.assertEquals(1, invoked.get());
        h.time.set(300_100); handler.dispatch(client, new ByteArrayProtocol(123, new byte[0], 12));
        Assertions.assertEquals(1, invoked.get()); Assertions.assertEquals(1, client.replies);
        Assertions.assertEquals(12, client.seq); Assertions.assertEquals(ErrorMsgEnum.system_overload.ID, client.error);
    }
    /** 心跳不产生脏数据，保护状态下仍可心跳和进入退出保存通道。 */
    @Test void heartbeatAndLogoutRemainAvailableUnderProtection() {
        FixturePlayer player = new FixturePlayer(); Harness h = new Harness(player); AtomicInteger calls = new AtomicInteger();
        TestHandler handler = new TestHandler(player, h.saves, calls::incrementAndGet); RecordingClient client = new RecordingClient();
        handler.dispatch(client, new ByteArrayProtocol(PbProtocol.PlayerHeartbeatRequest_01000005, new byte[0], 1));
        Assertions.assertFalse(h.saves.status(1).observedDirty());
        h.mutate(); h.time.set(300_100); player.setIslogouting(true);
        handler.dispatch(client, new ByteArrayProtocol(PbProtocol.PlayerHeartbeatRequest_01000005, new byte[0], 2));
        handler.dispatch(client, new ByteArrayProtocol(PbProtocol.PlayerLogoutRequest_01000003, new byte[0], 3));
        Assertions.assertEquals(3, calls.get()); Assertions.assertEquals(0, client.replies);
    }
    /** 异常退出关闭真实handler作用域；恢复保存后可释放，不能残留活动作用域。 */
    @Test void failedHandlerClosesScopeAndKeepsDirtyUntilSaved() {
        FixturePlayer player = new FixturePlayer(); Harness h = new Harness(player);
        TestHandler handler = new TestHandler(player, h.saves, () -> { throw new IllegalStateException("business failed"); });
        handler.dispatch(new RecordingClient(), new ByteArrayProtocol(123, new byte[0], 3));
        Assertions.assertTrue(h.saves.status(1).observedDirty()); h.save(); h.commit(0);
        Assertions.assertTrue(h.saves.release(1, player, () -> true));
    }
    /** 旧对象迟到业务和非法策略都立即拒绝。 */
    @Test void staleOwnerAndInvalidPolicyAreRejected() {
        Harness h = new Harness(); Assertions.assertTrue(h.saves.release(1, h.owner, () -> true));
        h.saves.register(1, new Object(), () -> {});
        Assertions.assertEquals("OWNER_CHANGED", Assertions.assertThrows(PlayerSaveCoordinator.SaveFailure.class, h::mutate).code);
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PlayerSaveCoordinator.RiskPolicy(300_000, 400_000, 600_000, 3, 1000, 128));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PlayerSaveCoordinator.RiskPolicy(270_000, 240_000, 300_000, 3, 1000, 128));
    }
    /** 负序号不会借用上一条正序号，保护响应仍恰好一次。 */
    @Test void protectedNegativeSequenceIsPreserved() {
        FixturePlayer player = new FixturePlayer(); Harness h = new Harness(player);
        TestHandler handler = new TestHandler(player, h.saves, () -> {}); RecordingClient client = new RecordingClient();
        handler.dispatch(client, new ByteArrayProtocol(123, new byte[0], 20)); h.time.set(300_100);
        handler.dispatch(client, new ByteArrayProtocol(123, new byte[0], -7));
        Assertions.assertEquals(-7, client.seq); Assertions.assertEquals(1, client.replies);
    }
    /** 全局自动任务也有上限，反复扫描不会继续向拥堵邮箱灌入任务。 */
    @Test void automaticDispatchHasGlobalOutstandingLimit() {
        AtomicLong time = new AtomicLong(100); AtomicInteger dispatched = new AtomicInteger();
        var saves = new PlayerSaveCoordinator(1, 100, (data, insert) -> Future.failedFuture("not invoked"),
                (action, delay) -> () -> {}, time::get, new PlayerSaveCoordinator.RiskPolicy(240_000, 270_000, 300_000, 3, 1000, 2));
        for (long id = 1; id <= 10; id++) { Object owner = new Object(); saves.register(id, owner, () -> {}); saves.markDirty(id, 100); }
        time.set(240_100); Promise<Void> held = Promise.promise();
        for (int i = 0; i < 10; i++) {
            saves.sweep((id, task) -> { dispatched.incrementAndGet(); return held.future(); }, (id, owner) -> Future.succeededFuture());
        }
        Assertions.assertEquals(2, dispatched.get()); held.complete();
    }
    /** 真实投递在排队时健康、执行时超预算，业务不可开始；恢复通道仍可保存。 */
    @Test void actualGuardChecksAtExecutionAndRecoveryBypassesIt() throws Exception {
        FixturePlayer player = new FixturePlayer(); Harness h = new Harness(player); h.mutate();
        try (var service = SaveMailboxFixture.create()) {
            var processor = new IdVirtualThreadProcessor(service);
            java.util.concurrent.CountDownLatch entered = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
            Future<Void> blocker = processor.processAsync(1, () -> { entered.countDown(); waitLatch(release); }, false);
            Assertions.assertTrue(entered.await(5, TimeUnit.SECONDS));
            AtomicInteger effects = new AtomicInteger();
            Future<Void> action = PlayerWriteGuard.submit(player, effects::incrementAndGet, processor,
                    () -> new PlayerWriteGuard.Scope(h.saves.beginMutation(1, player)));
            h.time.set(300_100); release.countDown(); await(blocker); awaitFailure(action);
            Assertions.assertEquals(0, effects.get());
            Promise<Void> captured = Promise.promise();
            Future<?> recovery = PlayerHelper.submitRecovery(processor, 1, () -> { Future<?> f = h.save(); captured.complete(); return f; });
            await(captured.future()); h.commit(0); await(recovery);
            Assertions.assertFalse(h.saves.status(1).writeProtected());
        }
    }
    /** 真实满邮箱的拒绝保留未执行标记，退避后使用同一生产巡检回调恢复断线对象。 */
    @Test void realFullMailboxAndDisconnectedSweepRecover() throws Exception {
        FixturePlayer player = new FixturePlayer(); player.setOnline(false); player.cancelAllTimer();
        Harness h = new Harness(player); h.mutate(); h.time.set(240_100);
        try (var service = SaveMailboxFixture.create(1)) {
            var processor = new IdVirtualThreadProcessor(service);
            java.util.concurrent.CountDownLatch entered = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
            Future<Void> blocker = processor.processAsync(1, () -> { entered.countDown(); waitLatch(release); }, false);
            Assertions.assertTrue(entered.await(5, TimeUnit.SECONDS));
            Future<Void> queued = processor.processAsync(1, () -> {}, false);
            try { PlayerHelper.sweepSaveRisks(h.saves, processor, id -> player, id -> h.save()); }
            finally { release.countDown(); }
            await(blocker); await(queued);
            Assertions.assertEquals(1, h.saves.status(1).consecutiveFailures());
            Assertions.assertFalse(h.saves.status(1).automaticBlocked());
            h.time.set(h.saves.status(1).retryAt()); Promise<Void> captured = Promise.promise();
            PlayerHelper.sweepSaveRisks(h.saves, processor, id -> player, id -> { Future<?> f = h.save(); captured.complete(); return f; });
            await(captured.future()); h.commit(0);
            Assertions.assertFalse(h.saves.status(1).observedDirty());
            Assertions.assertFalse(player.isOnline());
        }
    }
    /** 一次性过期动作保护期间延期，恢复后只执行一次；执行异常不会重放。 */
    @Test void oneShotTimerRunsOnceAfterRecoveryAndDoesNotReplayFailure() {
        Harness h = new Harness(); h.mutate(); h.time.set(300_100); AtomicInteger calls = new AtomicInteger();
        h.saves.runOrDeferTimer(1, h.owner, calls::incrementAndGet); Assertions.assertEquals(0, calls.get());
        h.save(); h.commit(0); h.sweep(); h.sweep(); Assertions.assertEquals(1, calls.get());
        h.time.addAndGet(300_000);
        h.saves.runOrDeferTimer(1, h.owner, () -> { calls.incrementAndGet(); throw new IllegalStateException("after effect"); });
        h.save(); h.commit(1);
        h.saves.sweep((id, task) -> {
            try { return task.get(); } catch (RuntimeException failure) {
                org.slf4j.LoggerFactory.getLogger(getClass()).warn("预期timer异常", failure); return Future.failedFuture(failure);
            }
        }, (id, owner) -> h.save());
        h.sweep(); Assertions.assertEquals(2, calls.get());
    }
    /** 取消延期timer和旧对象释放后，已经派发但未开始的任务也失效。 */
    @Test void cancelledDeferredTimerCannotRunFromOldQueuedCallback() {
        Harness h = new Harness(); h.mutate(); h.time.set(300_100); AtomicInteger calls = new AtomicInteger();
        h.saves.runOrDeferTimer(1, h.owner, calls::incrementAndGet); h.save(); h.commit(0);
        List<Supplier<Future<?>>> queued = new ArrayList<>(); Promise<Void> held = Promise.promise();
        h.saves.sweep((id, task) -> { queued.add(task); return held.future(); }, (id, owner) -> h.save());
        h.saves.cancelDeferredTimers(1, h.owner); queued.getFirst().get(); held.complete();
        Assertions.assertEquals(0, calls.get()); Assertions.assertTrue(h.saves.release(1, h.owner, () -> true));
    }
    /** 延期容量超限明确失败，不无限保留回调。 */
    @Test void deferredTimerCapacityIsExplicit() {
        Harness h = new Harness(); h.mutate(); h.time.set(300_100);
        for (int i = 0; i < 64; i++) { h.saves.runOrDeferTimer(1, h.owner, () -> {}); }
        Assertions.assertEquals("TIMER_BACKLOG_FULL", Assertions.assertThrows(PlayerSaveCoordinator.SaveFailure.class,
                () -> h.saves.runOrDeferTimer(1, h.owner, () -> {})).code);
    }
    /** 真实初始化hook的部分变化失败保留风险，不把冷加载等同于已确认提交。 */
    @Test void actualLoadHookFailureRetainsDirtyObject() throws Exception {
        FixturePlayer player = new FixturePlayer(); Harness h = new Harness(player);
        cn.game.testsupport.SaveProbeModule module = new cn.game.testsupport.SaveProbeModule(); module.failOnInit = true;
        player.getModules().put("probe", module);
        try (var service = SaveMailboxFixture.create()) {
            var processor = new IdVirtualThreadProcessor(service);
            Future<Player> initialized = PlayerHelper.initPlayerModuleFromDb(player, List.of(), true,
                    (owner, action) -> processor.process(1, () -> {
                        try (var ignored = h.saves.beginMutation(1, owner)) { return action.get(); }
                    }, null));
            awaitFailure(initialized);
            Assertions.assertEquals(1, module.initCalls); Assertions.assertTrue(module.values.contains("initialized"));
            Assertions.assertTrue(h.saves.status(1).observedDirty()); Assertions.assertEquals(0, h.saves.status(1).lastConfirmedAt());
            Assertions.assertFalse(h.saves.release(1, player, () -> true));
        }
    }
    /** 真实独立重连协议：新连接ID为0也必须按payload玩家的保护状态拒绝。 */
    @Test void actualReconnectProtocolWithZeroClientIdCannotBypassProtection() throws Exception {
        FixturePlayer player = new FixturePlayer(); Harness h = new Harness(player); h.mutate(); h.time.set(300_100);
        try (var service = SaveMailboxFixture.create()) {
            var processor = new IdVirtualThreadProcessor(service); Promise<Void> replied = Promise.promise();
            RecordingClient client = new RecordingClient() {
                @Override public boolean sendProtocol(int id, int seq, byte[] data, int error, boolean record) {
                    boolean result = super.sendProtocol(id, seq, data, error, record); replied.tryComplete(); return result;
                }
            }; client.setPlayerId(0);
            cn.game.games.net.game.handler.PlayerHandler handler = new cn.game.games.net.game.handler.PlayerHandler() {
                { inititialize(); }
                @Override protected Player findPlayer(long id) { return player; }
                @Override protected Future<Void> submitPlayerMutation(Player value, Runnable action) {
                    return PlayerWriteGuard.submit(value, action, processor, () -> new PlayerWriteGuard.Scope(h.saves.beginMutation(1, value)));
                }
            };
            handler.dispatch(client, new cn.game.core.net.protocol.object.ProtobufProtocol(PbProtocol.PlayerReconnecRequest_01000065,
                    cn.game.protocol.protobuf.PlayerMsg.PlayerReconnecRequest_01000065.newBuilder().setPlayerId(1).build(), 22));
            await(replied.future()); Assertions.assertEquals(1, client.replies);
            Assertions.assertEquals(ErrorMsgEnum.reconnect_fail.ID, client.error);
            Assertions.assertEquals(0, client.getPlayerId()); Assertions.assertNull(player.getGameClient());
        }
    }
    /** 真实充值编排先关闭scope再保存：无论即时或迟到提交，离线对象均能健康释放。 */
    @Test void offlinePaymentClosesScopeBeforeSavingAndCleanup() throws Exception {
        for (boolean immediate : new boolean[] {true, false}) {
            FixturePlayer player = new FixturePlayer(); Harness h = new Harness(player); PaymentHandler handler = new PaymentHandler(player, h, immediate);
            handler.payment(); if (!immediate) { h.commit(0); }
            await(handler.done.future()); Assertions.assertTrue(handler.removed); Assertions.assertEquals(1, handler.replies);
            Assertions.assertNull(h.saves.status(1));
        }
    }
    /** 等待测试栅栏必须有期限，异常不能静默吞掉。 */
    private static void waitLatch(java.util.concurrent.CountDownLatch latch) {
        try { if (!latch.await(5, TimeUnit.SECONDS)) { throw new IllegalStateException("fixture latch timed out"); } }
        catch (InterruptedException failure) { Thread.currentThread().interrupt(); org.slf4j.LoggerFactory.getLogger(PlayerSaveRiskTest.class).warn("测试栅栏等待被中断", failure); throw new IllegalStateException(failure); }
    }
    /** 保留ServerHandler实际业务→保存编排，仅替换外部支付逻辑和数据库IO。 */
    private static final class PaymentHandler extends cn.game.games.net.game.handler.ServerHandler {
        final FixturePlayer player; final Harness h; final boolean immediate; final Promise<Void> done = Promise.promise();
        volatile boolean removed; volatile int replies;
        PaymentHandler(FixturePlayer player, Harness h, boolean immediate) { this.player = player; this.h = h; this.immediate = immediate; }
        void payment() { handlePaymentForPlayer(new RecordingClient(), player, 1, "order", false); }
        @Override protected PlayerWriteGuard.Scope beginPaymentMutation(Player value) { return new PlayerWriteGuard.Scope(h.saves.beginMutation(1, value)); }
        @Override protected cn.game.games.net.game.module.recharge.PayItem findAndValidatePayItem(Player value, long uid, boolean online) {
            return new cn.game.games.net.game.module.recharge.PayItem();
        }
        @Override protected boolean processPayment(Player value, cn.game.games.net.game.module.recharge.PayItem item, boolean online) { return true; }
        @Override protected void persistAndReply(cn.game.core.net.client.NetClient client, Player value, boolean online) {
            Future<?> saved = h.save(); if (immediate) { h.commit(0); }
            saved.onComplete(result -> {
                if (result.failed()) { done.tryFail(result.cause()); return; }
                replies++; removed = h.saves.release(1, value, () -> true); done.tryComplete();
            });
        }
        @Override protected void sendResult(cn.game.core.net.client.NetClient client, boolean success) { replies++; }
        @Override protected void cleanupIfOffline(Player value, boolean online) { removed = h.saves.release(1, value, () -> true); }
    }
    /** 有界等待测试完成，不把超时作为预期错误。 */
    private static <T> T await(Future<T> future) throws Exception { return future.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS); }
    /** 等待明确失败。 */
    private static void awaitFailure(Future<?> future) { Assertions.assertThrows(java.util.concurrent.ExecutionException.class, () -> await(future)); }
    /** 最小独占参数。 */
    private static PlayerData data() { PlayerData data = new PlayerData(); data.setPlayerId(1L); data.setModules("{}"); return data; }
    /** 无全局环境的时间/事务替身；使用真实协调器。 */
    private static final class Harness {
        final AtomicLong time = new AtomicLong(100);
        final List<Promise<PlayerSaveCoordinator.WriteResult>> writes = new CopyOnWriteArrayList<>();
        final List<Runnable> deadlines = new CopyOnWriteArrayList<>();
        final List<Future<?>> dispatches = new ArrayList<>();
        final Object owner;
        final PlayerSaveCoordinator saves;
        Harness() { this(new Object()); }
        Harness(Object owner) {
            this.owner = owner;
            saves = new PlayerSaveCoordinator(2, 30_000, (data, insert) -> {
                Promise<PlayerSaveCoordinator.WriteResult> p = Promise.promise(); writes.add(p); return p.future();
            }, (action, delay) -> { deadlines.add(action); return () -> {}; }, time::get);
            saves.register(1, owner, () -> {});
        }
        void mutate() { try (var ignored = saves.beginMutation(1, owner)) { /* 保守变化 */ } }
        Future<?> save() { var capture = saves.preparing(owner, 1); return saves.submit(owner, data(), false, capture).result(); }
        void commit(int index) { writes.get(index).complete(new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.COMMITTED, null)); }
        void fail(int index, Throwable cause) { writes.get(index).complete(new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.NOT_WRITTEN, cause)); }
        void sweep() { saves.sweep((id, task) -> { Future<?> f = task.get(); dispatches.add(f); return f; }, (id, value) -> save()); }
    }
    /** 无完整业务配置的真实Player对象。 */
    private static final class FixturePlayer extends Player {
        @Override public long getPlayerId() { return 1; }
    }
    /** 调用真实BaseHandler分派，仅注入独立协调器/玩家。 */
    private static final class TestHandler extends GameBaseHandler {
        final Player player; final PlayerSaveCoordinator saves;
        TestHandler(Player player, PlayerSaveCoordinator saves, Runnable action) {
            this.player = player; this.saves = saves;
            for (int cmd : new int[] {123, PbProtocol.PlayerHeartbeatRequest_01000005, PbProtocol.PlayerLogoutRequest_01000003}) { putInvoker(cmd, (client, data) -> action.run()); }
        }
        @Override protected void inititialize() {}
        @Override protected int getModule() { return 1; }
        @Override protected Player findPlayer(long id) { return player; }
        @Override protected PlayerWriteGuard.Scope beginWrite(Player value) { return new PlayerWriteGuard.Scope(saves.beginMutation(value.getPlayerId(), value)); }
    }
    /** 保留真实GameClient的needProcess和回复序号，仅替换末端网络写入。 */
    private static class RecordingClient extends GameClient {
        int replies, seq, error;
        RecordingClient() { super(null); setPlayerId(1); }
        @Override public boolean sendProtocol(int id, int seq, byte[] data, int error, boolean record) {
            replies++; this.seq = seq; this.error = error; return true;
        }
    }
}
