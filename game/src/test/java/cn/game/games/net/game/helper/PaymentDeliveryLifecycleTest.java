package cn.game.games.net.game.helper;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import cn.game.core.execute.SaveMailboxFixture;
import cn.game.core.execute.TaskExecutorService;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.process.IdVirtualThreadProcessor;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.log.GameSSLogger;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.handler.ServerHandler;
import cn.game.games.net.game.module.account.Account;
import cn.game.games.net.game.module.activity.ActivityBase;
import cn.game.games.net.game.module.activity.ActivityModule;
import cn.game.games.net.game.module.activity.impl.player.FirstChargeActivity;
import cn.game.games.net.game.module.currency.CurrencyModule;
import cn.game.games.net.game.module.player.PlayerModule;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.games.net.game.module.recharge.PayType;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderShipRequest_7d000022;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderShipResponse_7d000023;
import cn.game.util.DateUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/** 实际 ship/业务分支/保存收尾共用真实邮箱，替代远端加载、事务 IO 与最终活动奖励依赖。 */
@Isolated
class PaymentDeliveryLifecycleTest {
    /** 保存原统计日志入口；本例不启动外部统计设备。 */
    private static Object originalStatistics;
    /** 初始化元数据并隔离统计输出；常规 GameLogger 与真实 Charge 分派仍执行。 */
    @BeforeAll static void metadata() throws Exception {
        ClassManager.getInstance().init();
        var field = GameSSLogger.class.getDeclaredField("instance"); field.setAccessible(true); originalStatistics = field.get(null);
        field.set(null, new GameSSLogger() { @Override public void logRecharge(Player player, String step, PayItem item) {} });
    }
    /** 恢复本例替换的统计入口。 */
    @AfterAll static void restore() throws Exception {
        var field = GameSSLogger.class.getDeclaredField("instance"); field.setAccessible(true); field.set(null, originalStatistics);
    }

    /** 在线回调及真实事件只执行一次，提交前没有 RPC 成功，提交后在同一邮箱收尾。 */
    @Test void onlineDeliveryWaitsForCommitInsideMailbox() throws Exception {
        try (Fixture f = new Fixture(true)) {
            f.ship(); f.captured();
            Assertions.assertEquals(1, f.awards.get()); Assertions.assertEquals(1, f.charges.get()); Assertions.assertTrue(f.replies.isEmpty());
            Promise<Void> queued = Promise.promise(); f.processor.processAsync(f.id(), queued::complete, false);
            Assertions.assertFalse(queued.future().isComplete()); f.commit(); f.flush();
            Assertions.assertEquals(List.of(true), f.replies); Assertions.assertTrue(queued.future().isComplete());
            Assertions.assertSame(f.player, f.owner); Assertions.assertEquals(0, f.removes.get());
            Assertions.assertFalse(f.saves.status(f.id()).observedDirty());
        }
    }

    /** 驻留离线对象沿用默认 login=true，必须走真实 offlinePay，确认后才清理临时对象。 */
    @Test void residentOfflineUsesOfflinePayAndCleansAfterCommit() throws Exception {
        try (Fixture f = new Fixture(false)) {
            Assertions.assertTrue(f.player.isIslogining()); f.ship(); f.captured();
            Assertions.assertFalse(f.payment.future().isComplete(), "不能因驻留Manager就消费在线callback");
            f.commit(); f.flush(); Assertions.assertEquals(List.of(true), f.replies);
            Assertions.assertEquals(1, f.awards.get()); Assertions.assertEquals(1, f.charges.get());
            Assertions.assertNull(f.owner); Assertions.assertEquals(1, f.removes.get());
        }
    }

    /** 实际异步加载需要回投邮箱；初始化未完成时的第二次请求共用加载，不先消费半初始化对象。 */
    @Test void concurrentOfflineRequestsShareLoadingWithoutMailboxDeadlock() throws Exception {
        try (Fixture f = new Fixture(false)) {
            f.owner = null; f.ship(); Assertions.assertEquals(1, f.loads.get());
            f.owner = f.player; f.ship(); f.flush();
            Assertions.assertEquals(0, f.awards.get()); Assertions.assertEquals(1, f.loads.get());
            await(f.processor.processAsync(f.id(), () -> f.loaded.complete(f.player), false));
            f.captured(); f.commit(); f.flush();
            Assertions.assertEquals(1, f.awards.get()); Assertions.assertEquals(List.of(true, false), f.replies);
            Assertions.assertEquals(1, f.removes.get());
        }
    }

    /** 排队期间身份、活跃、退出及在线登录状态变化，不能进入发奖或保存。 */
    @Test void queuedLifecycleChangesRejectBeforeBusiness() throws Exception {
        for (int change = 0; change < 4; change++) {
            try (Fixture f = new Fixture(true)) {
                Promise<Void> release = Promise.promise(), entered = Promise.promise();
                f.processor.processAsync(f.id(), () -> { entered.complete(); cn.game.core.util.AsyncUtils.await(release.future()); }, false);
                await(entered.future()); f.ship();
                if (change == 0) { f.owner = new Player(); }
                if (change == 1) { f.player.setActive(false); }
                if (change == 2) { f.player.setIslogouting(true); }
                if (change == 3) { f.player.setIslogining(true); }
                release.complete(); f.flush(); f.assertRejectedBeforeBusiness();
            }
        }
    }

    /** 加载完成返回的旧实例已失去权威身份，不能借后继玩家的保存结果完成旧订单。 */
    @Test void replacedOwnerAfterLoadingIsRejected() throws Exception {
        try (Fixture f = new Fixture(false)) {
            f.owner = null; f.ship(); f.owner = new Player(); f.loaded.complete(f.player);
            f.flush(); f.assertRejectedBeforeBusiness();
        }
    }

    /** 保存失败、结果不明和等待超时都只返回失败并保留原对象；迟到提交不追加成功。 */
    @Test void failedUnknownAndTimedOutSavesKeepResident() throws Exception {
        for (int mode = 0; mode < 3; mode++) {
            try (Fixture f = new Fixture(false)) {
                f.ship(); f.captured();
                if (mode == 2) { f.deadlines.getFirst().run(); }
                else { f.write.complete(new PlayerSaveCoordinator.WriteResult(mode == 0 ? PlayerSaveCoordinator.Outcome.NOT_WRITTEN
                        : PlayerSaveCoordinator.Outcome.UNKNOWN, new IllegalStateException("write fault"))); }
                f.flush(); Assertions.assertEquals(List.of(false), f.replies); Assertions.assertSame(f.player, f.owner);
                Assertions.assertEquals(0, f.removes.get());
                if (mode == 2) { f.commit(); f.flush(); Assertions.assertEquals(List.of(false), f.replies); }
            }
        }
    }

    /** 保存等待期间同一对象重连，即便旧 online 标志未改，也不能被旧临时离线收尾清除。 */
    @Test void sameObjectReconnectSurvivesOldSaveCompletion() throws Exception {
        for (boolean onlineFlag : List.of(false, true)) {
            try (Fixture f = new Fixture(false)) {
                f.ship(); f.captured();
                f.player.setGameClient(new PaymentOrderPublisherTest.RecordingClient());
                f.player.setOnline(onlineFlag); f.player.setIslogining(false); f.commit(); f.flush();
                Assertions.assertSame(f.player, f.owner); Assertions.assertEquals(0, f.removes.get());
                Assertions.assertEquals(List.of(onlineFlag), f.replies);
            }
        }
    }

    /** 保存期间被替换、退出或重新进入登录的原实例不得返回确认成功或清理新实例。 */
    @Test void staleOwnerOrLoginDuringSaveCannotAcknowledge() throws Exception {
        for (int change = 0; change < 3; change++) {
            try (Fixture f = new Fixture(true)) {
                f.ship(); f.captured();
                if (change == 0) { f.owner = new Player(); }
                if (change == 1) { f.player.setIslogouting(true); }
                if (change == 2) { f.player.setIslogining(true); }
                f.commit(); f.flush(); Assertions.assertEquals(List.of(false), f.replies); Assertions.assertEquals(0, f.removes.get());
            }
        }
    }

    /** 同样是临时连接也必须匹配保存前身份，不能删除等待期间交接的新离线生命周期。 */
    @Test void changedTemporaryConnectionIsRetained() throws Exception {
        try (Fixture f = new Fixture(false)) {
            f.ship(); f.captured(); f.player.setGameClient(new GameClient(null)); f.commit(); f.flush();
            Assertions.assertEquals(List.of(true), f.replies); Assertions.assertSame(f.player, f.owner); Assertions.assertEquals(0, f.removes.get());
        }
    }

    /** 提交后发送或删除抛错都不再追加失败响应；保存结果已确认，不重新执行业务。 */
    @Test void sendAndCleanupExceptionsNeverProduceOppositeReply() throws Exception {
        for (int failure : List.of(1, 2)) {
            try (Fixture f = new Fixture(false)) {
                f.replyThrows = failure == 1; f.removeThrows = failure == 2;
                f.ship(); f.captured(); f.commit(); f.flush();
                Assertions.assertEquals(List.of(true), f.replies); Assertions.assertEquals(1, f.awards.get());
                if (failure == 2) { Assertions.assertSame(f.player, f.owner); }
            }
        }
    }

    /** 正常重连任务排在保存之后，必须在清理结束后看到最终归属，而不是与外部回调竞争。 */
    @Test void queuedReconnectRunsAfterOfflineCleanup() throws Exception {
        try (Fixture f = new Fixture(false)) {
            f.ship(); f.captured();
            Future<Void> reconnect = f.processor.processAsync(f.id(), () -> {
                Assertions.assertNull(f.owner); Assertions.assertEquals(1, f.removes.get());
            }, false);
            Assertions.assertFalse(reconnect.isComplete()); f.commit(); await(reconnect);
        }
    }

    /** 独立表、缺失订单和已finish订单仍拒绝，不把历史finish升级为可信幂等成功。 */
    @Test void unsupportedOrFinishedOrdersDoNotExecuteBusiness() throws Exception {
        for (int mode = 0; mode < 3; mode++) {
            try (Fixture f = new Fixture(true)) {
                if (mode == 0) { f.single = false; }
                if (mode == 1) { f.orderId = 92; }
                if (mode == 2) { f.player.getPlayerModule().getPayItems(91).setFinish(true); }
                f.ship(); f.flush(); f.assertRejectedBeforeBusiness();
            }
        }
    }

    /** 同步捕获抛错与缺少保存回执的成功不能冒充充值持久化完成。 */
    @Test void captureFailureAndEmptySaveReceiptFailClosed() throws Exception {
        for (int mode : List.of(1, 2)) {
            try (Fixture f = new Fixture(false)) {
                f.saveFailure = mode; f.ship(); f.flush();
                Assertions.assertEquals(List.of(false), f.replies); Assertions.assertSame(f.player, f.owner); Assertions.assertEquals(0, f.removes.get());
            }
        }
    }

    /** 真实 Charge 异常发生在 finish 前，后续通知仍保守拒绝并保留原对象。 */
    @Test void failedChargeIsNotUpgradedToSuccessOnReplay() throws Exception {
        try (Fixture f = new Fixture(true)) {
            f.player.registerEventHandler(EventTypeEnum.Charge, event -> { throw new IllegalStateException("charge fault"); });
            f.ship(); f.flush(); f.ship(); f.flush();
            Assertions.assertEquals(List.of(false, false), f.replies); Assertions.assertEquals(1, f.awards.get());
            Assertions.assertFalse(f.captured.future().isComplete()); Assertions.assertSame(f.player, f.owner);
        }
    }

    /** 加载失败或抛错清理共享加载记录，后续请求仍能重新发起；邮箱拒绝有明确失败响应。 */
    @Test void loadAndMailboxFailuresAreObservable() throws Exception {
        try (Fixture f = new Fixture(false)) {
            f.owner = null; f.ship(); f.loaded.fail("load failed");
            Assertions.assertEquals(List.of(false), f.replies);
            f.loaded = Promise.promise(); f.ship(); Assertions.assertEquals(2, f.loads.get());
            f.owner = f.player; f.loaded.complete(f.player); f.captured(); f.commit(); f.flush();
            Assertions.assertEquals(List.of(false, true), f.replies);
        }
        try (Fixture f = new Fixture(true)) {
            f.executor.close(); f.ship(); Assertions.assertEquals(List.of(false), f.replies); Assertions.assertEquals(0, f.awards.get());
        }
    }

    /** 加载登记后初始化失败，下一次发货必须隔离原实例，不能把删除加载记录当作初始化成功。 */
    @Test void failedRegisteredLoadCannotBecomeReadyOnRetry() throws Exception {
        try (Fixture f = new Fixture(false)) {
            f.owner = null; f.ship(); f.owner = f.player;
            f.loaded.fail(new OfflinePlayerInitializationFailure(f.player, new IllegalStateException("module initialization failed")));
            f.ship(); f.flush();
            Assertions.assertEquals(List.of(false, false), f.replies); Assertions.assertEquals(0, f.awards.get());
            Assertions.assertFalse(f.captured.future().isComplete()); Assertions.assertSame(f.player, f.owner);
            Assertions.assertEquals(0, f.removes.get()); Assertions.assertEquals(1, f.loads.get());
            // 模拟外部完成安全释放后可重新发起加载；本 Handler 本身绝不删除失败对象来解锁。
            f.owner = null; f.loaded = Promise.promise(); f.ship(); Assertions.assertEquals(2, f.loads.get());
            f.loaded.fail("no replacement fixture");
        }
    }

    /** 正常登录抢先登记的后继对象不能被旧加载失败按 ID 误隔离，原异常保持明确失败。 */
    @Test void failedOldLoadDoesNotQuarantineSuccessfulLogin() throws Exception {
        for (boolean wrappedOldInstance : List.of(false, true)) {
            try (Fixture f = new Fixture(true)) {
                f.owner = null; f.ship(); f.owner = f.player;
                Throwable failure = new IllegalStateException("old registration lost race");
                if (wrappedOldInstance) {
                    Player old = new Player(); old.setPlayerId(f.id()); failure = new OfflinePlayerInitializationFailure(old, failure);
                }
                f.loaded.fail(failure); f.ship(); f.captured(); f.commit(); f.flush();
                Assertions.assertEquals(List.of(false, true), f.replies); Assertions.assertEquals(1, f.awards.get());
                Assertions.assertSame(f.player, f.owner);
            }
        }
    }

    /** 生产初始化包装只标记成功登记的原实例，覆盖同步、异步和登记后异常，不能误标后继对象。 */
    @Test void offlineInitializationFailureCarriesOnlyRegisteredIdentity() throws Exception {
        for (int mode = 0; mode < 4; mode++) {
            try (Fixture f = new Fixture(false)) {
                f.owner = null; int fault = mode;
                IllegalStateException cause = new IllegalStateException("initialization boundary failure");
                Future<Player> result = PlayerHelper.initializeOfflinePlayer(f.player, value -> {
                    if (fault == 0) { f.owner = new Player(); throw cause; }
                    f.owner = value; if (fault == 1) { throw cause; }
                }, value -> {
                    if (fault == 2) { throw cause; } return Future.failedFuture(cause);
                }, id -> f.owner);
                Assertions.assertThrows(java.util.concurrent.ExecutionException.class, () -> await(result));
                if (mode == 0) { Assertions.assertSame(cause, result.cause()); }
                else {
                    var typed = Assertions.assertInstanceOf(OfflinePlayerInitializationFailure.class, result.cause());
                    Assertions.assertSame(f.player, typed.player()); Assertions.assertSame(cause, typed.getCause());
                }
            }
        }
    }

    /** @param future 异步边界 @return 有界等待值；不以固定睡眠推断时序 */
    static <T> T await(Future<T> future) throws Exception { return future.toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS); }

    /** 共用生产 Handler 与保存链路；真实库用例只替换 writer 与专用玩家行。 */
    static final class Fixture implements AutoCloseable {
        /** 每个用例独享真实玩家邮箱。 */
        final TaskExecutorService executor = SaveMailboxFixture.create();
        final IdVirtualThreadProcessor processor = new IdVirtualThreadProcessor(executor);
        /** 实际处理对象及权威归属。 */
        final Player player;
        volatile Player owner;
        /** 真实业务入口的可观察副作用。 */
        final AtomicInteger awards = new AtomicInteger(), charges = new AtomicInteger(), removes = new AtomicInteger(), loads = new AtomicInteger();
        /** 原付款承诺、外部加载与事务终态。 */
        final Promise<Boolean> payment = Promise.promise();
        /** 可替换的奖励外部完成边界；保留生产回调绑定与发货流程。 */
        java.util.function.Supplier<Future<Void>> reward = () -> { award(); return Future.succeededFuture(); };
        /** 保存确认后才完成的购买结果。 */
        final Future<Void> purchase;
        Promise<Player> loaded = Promise.promise();
        final Promise<PlayerSaveCoordinator.WriteResult> write = Promise.promise();
        /** 生产快照装配实际交给 writer 的行，用于核对标记与奖励处于同一保存。 */
        volatile PlayerData writtenSnapshot;
        final Promise<Void> captured = Promise.promise();
        final List<Runnable> deadlines = new CopyOnWriteArrayList<>();
        final PlayerSaveCoordinator saves;
        /** RPC实际响应内容，包括网络发送抛错之前的发送尝试。 */
        final List<Boolean> replies = new CopyOnWriteArrayList<>();
        final NetClient rpc;
        final Handler handler = new Handler();
        /** 外部故障注入，不改写真实业务或保存收尾分支。 */
        boolean single = true, replyThrows, removeThrows;
        int saveFailure;
        long orderId = 91;
        /** 记录业务执行线程，以断言删除仍在同一邮箱任务。 */
        volatile Thread businessThread;

        /** @param online 本例起始在线状态 */
        Fixture(boolean online) { this(data(), online, null); }
        /** @param data 独立玩家行 @param online 起始状态 @param writer 真实库可提供生产写入器 */
        Fixture(PlayerData data, boolean online, PlayerSaveCoordinator.Writer writer) {
            player = new Player() {
                @Override public PlayerData getData() { return data; }
                @Override public int getLevel() { return 1; }
                @Override public long getGuildId() { return 0; }
                @Override public int getVipLevel() { return 0; }
                @Override public ActivityModule getActivityModule() {
                    return new ActivityModule() { @Override @SuppressWarnings("unchecked") public <T extends ActivityBase> T get(int id) {
                        return (T) new FirstChargeActivity() { @Override public void buy(int id) { award(); } };
                    }};
                }
            };
            player.setPlayerId(data.getPlayerId()); player.setAccount(new Account()); player.setOnline(online);
            if (online) { player.setIslogining(false); }
            player.setGameClient(online ? new PaymentOrderPublisherTest.RecordingClient() : new GameClient(null));
            player.getModules().put(PlayerModule.class.getName(), new PlayerModule());
            player.getModules().put(CurrencyModule.class.getName(), new CurrencyModule());
            PayItem item = new PayItem(); item.setOrderId(91); item.setPayType(PayType.FirstCharge); item.setPayId(73); item.setRmb(12); item.addPaySubIds(17);
            player.getPlayerModule().addPayItems(item);
            player.getPlayerModule().addPayCallback(91, payment);
            purchase = cn.game.games.net.game.module.recharge.PaymentCompletion.async(player, payment.future(), () -> reward.get());
            player.registerEventHandler(EventTypeEnum.Charge, event -> charges.incrementAndGet());
            owner = player;
            saves = new PlayerSaveCoordinator(4, 30_000, writer == null ? (row, insert) -> { writtenSnapshot = row; return write.future(); } : writer,
                    (action, delay) -> { deadlines.add(action); return () -> {}; }, DateUtil::currentTimeMillis);
            saves.register(id(), player, () -> {});
            rpc = (NetClient) Proxy.newProxyInstance(NetClient.class.getClassLoader(), new Class<?>[] {NetClient.class}, (proxy, method, args) -> {
                if (method.getName().equals("sendProtocol")) {
                    replies.add(((PaymentOrderShipResponse_7d000023) args[0]).getSuccess());
                    if (replyThrows) { throw new IllegalStateException("RPC send failed"); } return null;
                }
                throw new AssertionError("非预期RPC方法 " + method.getName());
            });
        }
        /** @return 本例玩家 ID */
        long id() { return player.getPlayerId(); }
        /** 实际奖励依赖写入可持久计数，便于冷读核对。 */
        void award() { businessThread = Thread.currentThread(); awards.incrementAndGet(); player.getPlayerModule().getGuideMap().merge(700, 1, Integer::sum); }
        /** 进入生产注册的发货方法。 */
        void ship() { handler.invoke(); }
        /** 等待实际保存捕获完成。 */
        void captured() throws Exception { await(captured.future()); }
        /** 确认受控事务完成。 */
        void commit() { write.complete(new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.COMMITTED, null)); }
        /** 当前任务之后的真实邮箱屏障。 */
        void flush() throws Exception { await(processor.processAsync(id(), () -> {}, false)); }
        /** 失败发生在任何业务或保存之前。 */
        void assertRejectedBeforeBusiness() {
            Assertions.assertEquals(List.of(false), replies); Assertions.assertEquals(0, awards.get());
            Assertions.assertEquals(0, charges.get()); Assertions.assertFalse(captured.future().isComplete()); Assertions.assertEquals(0, removes.get());
        }
        /** @return 不访问真实数据库的基础行 */
        private static PlayerData data() { PlayerData data = new PlayerData(); data.setPlayerId(8291); data.setUid(8291); data.setModules("{}"); return data; }
        @Override public void close() {
            write.tryComplete(new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.NOT_WRITTEN, new IllegalStateException("fixture closed")));
            executor.close();
        }

        /** 只替换外部边界，保留find/process/persist/cleanup等生产决策。 */
        final class Handler extends ServerHandler {
            /** 调用实际 ship，不复制其处理流程。 */
            void invoke() { ship(rpc, PaymentOrderShipRequest_7d000022.newBuilder().setPlayerId(id()).setUid(orderId).setSdkOrderId("sdk-91").build()); }
            @Override protected Player paymentPlayer(long id) { return owner; }
            @Override protected Future<Player> loadPaymentPlayer(long id) { loads.incrementAndGet(); return loaded.future(); }
            @Override protected Future<Void> submitPayment(long id, Runnable action) { return processor.processAsync(id, action, false); }
            @Override protected boolean singlePaymentStorage() { return single; }
            @Override protected PlayerWriteGuard.Scope beginPaymentMutation(Player value) { return new PlayerWriteGuard.Scope(saves.beginMutation(id(), value)); }
            @Override protected Future<List<Object>> savePayment(Player value) {
                Assertions.assertSame(player, value);
                if (saveFailure == 1) { throw new IllegalStateException("capture failure"); }
                if (saveFailure == 2) { return Future.succeededFuture(List.of()); }
                Future<List<Object>> result = PlayerHelper.saveClientCache(id(), ignored -> owner, () -> single, saves,
                        tasks -> { throw new AssertionError("独立表不得放行"); });
                captured.complete(); return result;
            }
            @Override protected boolean removePaymentPlayer(Player value) {
                Assertions.assertSame(businessThread, Thread.currentThread(), "清理必须留在发货邮箱任务");
                removes.incrementAndGet(); if (removeThrows) { throw new IllegalStateException("remove failure"); }
                return saves.release(id(), value, () -> { if (owner != value) { return false; } owner = null; return true; });
            }
        }
    }
}
