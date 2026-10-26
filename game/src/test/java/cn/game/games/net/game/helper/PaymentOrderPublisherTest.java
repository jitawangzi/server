package cn.game.games.net.game.helper;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import cn.game.core.execute.SaveMailboxFixture;
import cn.game.core.execute.TaskExecutorService;
import cn.game.core.net.process.IdVirtualThreadProcessor;
import cn.game.core.net.protocol.bytes.ByteArrayProtocol;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.module.account.Account;
import cn.game.games.net.game.module.player.PlayerModule;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.games.net.game.module.recharge.PayType;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderCreateRequest_7d000020;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderCreateResponse_7d000021;
import cn.game.protocol.protobuf.ShopMsg.PaymentOrderPush_15010020;
import cn.game.util.DateUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.ServerWebSocket;

/** 真实邮箱、保存协调器与快照验证发布门槛；RPC 和事务终态受控，数据库另有显式集成用例。 */
@Isolated
class PaymentOrderPublisherTest {
    /** 初始化生产快照需要的模块元数据。 */
    @BeforeAll static void metadata() { ClassManager.getInstance().init(); }

    /** 实际 Player.pay 与默认发布器接线，只替代外部事件总线；核对普通与一元充值的不可变 RPC 参数。 */
    @Test void actualPlayerPayCapturesNormalAndOneYuanOrderParameters() throws Exception {
        var context = cn.game.core.base.ServerContext.getInstance();
        var processorField = context.getClass().getDeclaredField("processor"); processorField.setAccessible(true);
        Object oldProcessor = processorField.get(context);
        io.vertx.core.Vertx oldVertx = cn.game.core.net.vertx.VxHolder.vertx;
        String oldOneYuan = System.getProperty("AllRecharge1"), oldDisabled = System.getProperty("DisableRecharge");
        var facadeField = com.ctrip.framework.apollo.ConfigService.class.getDeclaredField("s_instance"); facadeField.setAccessible(true);
        Object facade = facadeField.get(null);
        var managerField = com.ctrip.framework.apollo.ConfigService.class.getDeclaredField("m_configManager"); managerField.setAccessible(true);
        Object oldManager = managerField.get(facade);
        try {
            // 只提供本例必需的单表配置，避免真实入口测试启动 Apollo 或读取本机缓存。
            var config = Proxy.newProxyInstance(com.ctrip.framework.apollo.Config.class.getClassLoader(),
                    new Class<?>[] {com.ctrip.framework.apollo.Config.class}, (proxy, method, args) -> {
                        Assertions.assertEquals("getBooleanProperty", method.getName());
                        Assertions.assertEquals("player_db_single_table", args[0]); return true;
                    });
            managerField.set(facade, Proxy.newProxyInstance(managerField.getType().getClassLoader(), new Class<?>[] {managerField.getType()},
                    (proxy, method, args) -> {
                        Assertions.assertEquals("getConfig", method.getName()); Assertions.assertEquals("application", args[0]); return config;
                    }));
            System.clearProperty("DisableRecharge");
            for (boolean oneYuan : List.of(false, true)) {
                System.setProperty("AllRecharge1", Boolean.toString(oneYuan));
                try (Fixture f = new Fixture()) {
                    f.player.getAccount().sdkPayChannel = "0010";
                    f.player.getModules().put(cn.game.games.net.game.module.currency.CurrencyModule.class.getName(),
                            new cn.game.games.net.game.module.currency.CurrencyModule());
                    processorField.set(context, f.processor);
                    var players = cn.game.games.net.game.manager.PlayerManager.getInstance().getAllPlayer();
                    Assertions.assertNull(players.putIfAbsent(f.player.getPlayerId(), f.player));
                    Promise<io.vertx.core.eventbus.Message<Object>> rpcResult = Promise.promise();
                    io.vertx.core.eventbus.EventBus bus = (io.vertx.core.eventbus.EventBus) Proxy.newProxyInstance(
                            io.vertx.core.eventbus.EventBus.class.getClassLoader(), new Class<?>[] {io.vertx.core.eventbus.EventBus.class},
                            (proxy, method, args) -> {
                                Assertions.assertEquals("request", method.getName()); Assertions.assertEquals("Login", args[0]);
                                f.request = (PaymentOrderCreateRequest_7d000020) args[1]; return rpcResult.future();
                            });
                    cn.game.core.net.vertx.VxHolder.vertx = (io.vertx.core.Vertx) Proxy.newProxyInstance(io.vertx.core.Vertx.class.getClassLoader(),
                            new Class<?>[] {io.vertx.core.Vertx.class}, (proxy, method, args) -> {
                                Assertions.assertEquals("eventBus", method.getName()); return bus;
                            });
                    try {
                        int[] cost = {2, 12, 2002};
                        Future<Boolean> payment = await(f.processor.process(f.player.getPlayerId(),
                                () -> f.player.pay(PayType.FirstCharge, 73, cost, 17), null));
                        cost[1] = 99; cost[2] = 9999;
                        Assertions.assertNotNull(f.request); Assertions.assertFalse(payment.isComplete());
                        Assertions.assertEquals(oneYuan ? 100 : 1200, f.request.getGoodsPrice());
                        Assertions.assertEquals(oneYuan ? "1007" : "2002", f.request.getItemId());
                        Assertions.assertEquals("initial", f.request.getSessionId());
                        rpcResult.fail("controlled Login failure"); Assertions.assertFalse(await(payment));
                        f.assertUnpublished(); Assertions.assertNull(f.player.getPlayerModule().getPayItems(91));
                    } finally { players.remove(f.player.getPlayerId(), f.player); }
                }
            }
        } finally {
            processorField.set(context, oldProcessor); cn.game.core.net.vertx.VxHolder.vertx = oldVertx;
            managerField.set(facade, oldManager);
            restoreProperty("AllRecharge1", oldOneYuan); restoreProperty("DisableRecharge", oldDisabled);
        }
    }

    /** @param name 本例变更的开关 @param value 原始值，null 表示原先未设置 */
    private static void restoreProperty(String name, String value) {
        if (value == null) { System.clearProperty(name); } else { System.setProperty(name, value); }
    }

    /** 空、无效以及失败 RPC 都不能进入保存或留下未决支付。 */
    @Test void invalidNullAndFailedRpcNeverSaveOrPublish() throws Exception {
        for (int mode = 0; mode < 3; mode++) {
            try (Fixture f = new Fixture()) {
                Future<Boolean> payment = f.start();
                if (mode == 0) { f.rpc.complete(PaymentOrderCreateResponse_7d000021.getDefaultInstance()); }
                if (mode == 1) { f.rpc.complete(null); }
                if (mode == 2) { f.rpc.fail("Login unavailable"); }
                Assertions.assertFalse(await(payment)); f.assertUnpublished();
                Assertions.assertNull(f.player.getPlayerModule().getPayItems(91)); Assertions.assertFalse(f.captured.future().isComplete());
            }
        }
    }

    /** 保存确认前零推单，确认后仍等待真正的付款回调；关闭 mutation 后捕获没有假脏状态。 */
    @Test void commitPrecedesPublicationAndPaymentRemainsPending() throws Exception {
        try (Fixture f = new Fixture()) {
            Future<Boolean> payment = f.start();
            Assertions.assertNull(f.player.getPlayerModule().getPayItems(91));
            Assertions.assertFalse(payment.isComplete());
            f.order(); f.captured();
            Assertions.assertNotNull(f.player.getPlayerModule().getPayItems(91));
            Assertions.assertEquals(0, f.client.published());
            f.commit(); f.flush();
            Assertions.assertEquals(1, f.client.published());
            Assertions.assertFalse(payment.isComplete());
            Assertions.assertFalse(f.saves.status(f.player.getPlayerId()).observedDirty());
            await(f.processor.processAsync(f.player.getPlayerId(), () -> { Assertions.assertTrue(f.player.getPlayerModule().execPayCallback(91)); f.player.getPlayerModule().confirmPayCallback(91, null); }, false));
            Assertions.assertTrue(await(payment));
        }
    }

    /** 在保存期间到达的发货不能越过当前邮箱任务，也不会因等待保存而自锁。 */
    @Test void callbackDuringSaveWaitsBehindPublication() throws Exception {
        try (Fixture f = new Fixture()) {
            Future<Boolean> payment = f.start(); f.order(); f.captured();
            Future<Void> delivery = f.processor.processAsync(f.player.getPlayerId(), () -> {
                Assertions.assertEquals(1, f.client.published());
                Assertions.assertTrue(f.player.getPlayerModule().execPayCallback(91));
                Assertions.assertFalse(payment.isComplete());
                f.player.getPlayerModule().confirmPayCallback(91, null);
            }, false);
            Assertions.assertFalse(delivery.isComplete()); f.commit(); await(delivery); Assertions.assertTrue(await(payment));
        }
    }

    /** 早于订单关联的回调没有可消费记录，不能把它声称为自动恢复成功。 */
    @Test void callbackBeforeAssociationHasNoPaymentToComplete() throws Exception {
        try (Fixture f = new Fixture()) {
            Future<Boolean> payment = f.start();
            await(f.processor.processAsync(f.player.getPlayerId(), () -> Assertions.assertFalse(f.player.getPlayerModule().execPayCallback(91)), false));
            Assertions.assertFalse(payment.isComplete()); f.rpc.fail("create failed"); Assertions.assertFalse(await(payment));
        }
    }

    /** 明确未写入和结果不明都保留内存关联，禁止推单或残留已完成 false 的回调。 */
    @Test void failedAndUnknownWritesRetainAssociationWithoutPublication() throws Exception {
        for (var outcome : List.of(PlayerSaveCoordinator.Outcome.NOT_WRITTEN, PlayerSaveCoordinator.Outcome.UNKNOWN)) {
            try (Fixture f = new Fixture()) {
                Future<Boolean> payment = f.start(); f.order(); f.captured();
                f.write.complete(new PlayerSaveCoordinator.WriteResult(outcome, new IllegalStateException("transaction failed")));
                Assertions.assertFalse(await(payment)); f.assertUnpublished();
                Assertions.assertNotNull(f.player.getPlayerModule().getPayItems(91));
                if (outcome == PlayerSaveCoordinator.Outcome.UNKNOWN) { Assertions.assertTrue(f.saves.status(f.player.getPlayerId()).writeProtected()); }
            }
        }
    }

    /** 超时后真实事务迟到成功也不能重新发布已经失败的订单请求。 */
    @Test void lateCommitAfterTimeoutNeverRepublishes() throws Exception {
        try (Fixture f = new Fixture()) {
            Future<Boolean> payment = f.start(); f.order(); f.captured(); f.deadlines.getFirst().run();
            Assertions.assertFalse(await(payment)); f.assertUnpublished();
            Assertions.assertFalse(f.saves.status(f.player.getPlayerId()).workerExited());
            f.commit(); f.flush(); Assertions.assertEquals(0, f.client.published());
            Assertions.assertFalse(await(payment)); Assertions.assertNotNull(f.player.getPlayerModule().getPayItems(91));
        }
    }

    /** RPC 返回时原玩家、连接、会话或退出状态失效，不能写入或推送给后继对象。 */
    @Test void staleRpcChecksOwnerConnectionSessionAndLogout() throws Exception {
        for (int change = 0; change < 4; change++) {
            try (Fixture f = new Fixture()) {
                Future<Boolean> payment = f.start();
                if (change == 0) { f.owner = new Player(); }
                if (change == 1) { f.player.setGameClient(new RecordingClient()); }
                if (change == 2) { f.client.setSessionId("new-session"); }
                if (change == 3) { f.player.setIslogouting(true); }
                f.order(); Assertions.assertFalse(await(payment)); f.assertUnpublished();
                Assertions.assertNull(f.player.getPlayerModule().getPayItems(91));
            }
        }
    }

    /** 保存完成时再次校验对象、连接、会话及退出状态，已持久关联仍予以保留。 */
    @Test void staleAfterCommitCannotPublishToReplacement() throws Exception {
        for (int change = 0; change < 4; change++) {
            try (Fixture f = new Fixture()) {
                Future<Boolean> payment = f.start(); f.order(); f.captured();
                RecordingClient replacement = new RecordingClient();
                if (change == 0) { f.owner = new Player(); }
                if (change == 1) { f.player.setGameClient(replacement); }
                if (change == 2) { f.client.setSessionId("new-session"); }
                if (change == 3) { f.player.setIslogouting(true); }
                f.commit(); Assertions.assertFalse(await(payment)); f.assertUnpublished();
                Assertions.assertEquals(0, replacement.sent.size());
                Assertions.assertNotNull(f.player.getPlayerModule().getPayItems(91));
            }
        }
    }

    /** 重复订单拒绝新请求，但不能覆盖已有记录或移除原付款回调。 */
    @Test void duplicateOrderPreservesOriginalRecordAndCallback() throws Exception {
        try (Fixture f = new Fixture()) {
            PayItem original = new PayItem(); original.setOrderId(91); original.setPayId(777);
            Promise<Boolean> old = Promise.promise();
            f.player.getPlayerModule().addPayItems(original); f.player.getPlayerModule().addPayCallback(91, old);
            cn.game.games.net.game.module.recharge.PaymentCompletion.reward(f.player, old.future(), () -> null);
            Future<Boolean> payment = f.start(); f.order(); Assertions.assertFalse(await(payment)); f.flush();
            Assertions.assertSame(original, f.player.getPlayerModule().getPayItems(91));
            await(f.processor.processAsync(f.player.getPlayerId(), () -> { Assertions.assertTrue(f.player.getPlayerModule().execPayCallback(91)); f.player.getPlayerModule().confirmPayCallback(91, null); }, false));
            Assertions.assertTrue(await(old.future())); Assertions.assertEquals(0, f.client.published());
        }
    }

    /** 发布同步拒绝或抛错都清理本次 callback，不影响已保存关联及后续离线恢复。 */
    @Test void sendRejectionAndExceptionRemoveOnlyThisCallback() throws Exception {
        for (int mode : List.of(1, 2)) {
            try (Fixture f = new Fixture()) {
                Future<Boolean> payment = f.start(); f.order(); f.captured(); f.client.sendFailure = mode; f.commit();
                Assertions.assertFalse(await(payment)); f.assertUnpublished(); Assertions.assertNotNull(f.player.getPlayerModule().getPayItems(91));
            }
        }
    }

    /** 同连接后续请求推进序号，不得改变订单响应及缓存归属；复制业务参数。 */
    @Test void publicationKeepsOriginalSequenceAndCopiedArguments() throws Exception {
        try (Fixture f = new Fixture()) {
            int[] extras = {17, 18};
            Future<Boolean> payment = f.start(extras); extras[0] = 999;
            Assertions.assertTrue(f.client.needProcess(new ByteArrayProtocol(123, new byte[0], 42, 0)));
            f.order(); f.captured(); f.commit(); f.flush();
            Assertions.assertEquals(1200, f.request.getGoodsPrice()); Assertions.assertEquals("1007", f.request.getItemId());
            Assertions.assertEquals(List.of(17, 18), f.player.getPlayerModule().getPayItems(91).getPaySubIds());
            Sent sent = f.client.sent.getFirst(); Assertions.assertEquals(41, sent.sequence());
            Assertions.assertEquals("1007", PaymentOrderPush_15010020.parseFrom(sent.data()).getSdkGoodsId());
            Assertions.assertTrue(f.client.getRecentMessages().containsKey(41)); Assertions.assertFalse(f.client.getRecentMessages().containsKey(42));
            Assertions.assertFalse(payment.isComplete());
        }
    }

    /** 独立表模式在 Login 创单前拒绝；不能用任意 DAO 成功冒充关联保存。 */
    @Test void standaloneModeRejectsBeforeCreatingOrder() throws Exception {
        try (Fixture f = new Fixture()) {
            f.single = false; Assertions.assertFalse(await(f.start())); Assertions.assertNull(f.request); f.assertUnpublished();
        }
    }

    /** 邮箱关闭的开始前拒绝必须终结原支付等待，不产生关联。 */
    @Test void mailboxRejectionSettlesPaymentWithoutAssociation() throws Exception {
        try (Fixture f = new Fixture()) {
            Future<Boolean> payment = f.start(); f.executor.close(); f.order(); Assertions.assertFalse(await(payment));
            Assertions.assertEquals(0, f.client.published()); Assertions.assertNull(f.player.getPlayerModule().getPayItems(91));
        }
    }

    /** 捕获准备抛错或不含提交回执的成功都不能放行可支付推送。 */
    @Test void preparationFailureAndUnconfirmedSuccessDoNotPublish() throws Exception {
        for (int mode : List.of(1, 2)) {
            try (Fixture f = new Fixture()) {
                f.saveFailure = mode; Future<Boolean> payment = f.start(); f.order(); Assertions.assertFalse(await(payment));
                f.assertUnpublished(); Assertions.assertNotNull(f.player.getPlayerModule().getPayItems(91));
            }
        }
    }

    /** @param future 待执行结果 @return 有界等待结果，断言不以固定 sleep 推断先后 */
    static <T> T await(Future<T> future) throws Exception { return future.toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS); }

    /** 生产发布器共用夹具，真实库用例只替换事务写入器和专用玩家行。 */
    static final class Fixture implements AutoCloseable {
        /** 真实玩家任务执行器及处理器。 */
        final TaskExecutorService executor = SaveMailboxFixture.create();
        final IdVirtualThreadProcessor processor = new IdVirtualThreadProcessor(executor);
        /** 当前测试玩家及权威查找结果。 */
        final Player player;
        volatile Player owner;
        /** 原请求连接，保留实际 GameClient 序号与缓存逻辑。 */
        final RecordingClient client = new RecordingClient();
        /** 外部 RPC 与真实写入结果控制点。 */
        final Promise<PaymentOrderCreateResponse_7d000021> rpc = Promise.promise();
        final Promise<PlayerSaveCoordinator.WriteResult> write = Promise.promise();
        /** 协调器的超时动作与完成捕获屏障。 */
        final List<Runnable> deadlines = new CopyOnWriteArrayList<>();
        final Promise<Void> captured = Promise.promise();
        final PlayerSaveCoordinator saves;
        /** 实际交给 Login 的不可变请求。 */
        volatile PaymentOrderCreateRequest_7d000020 request;
        /** 模式与保存异常注入；不替代发布器分支。 */
        boolean single = true;
        int saveFailure;

        /** 默认在内存控制真实事务终态。 */
        Fixture() { this(testPlayer(), null); }
        /** @param player 存储夹具玩家 @param writer 可选真实数据库写入器 */
        Fixture(Player player, PlayerSaveCoordinator.Writer writer) {
            this.player = player; owner = player;
            player.setAccount(new Account()); player.setGameClient(client); client.setPlayerId(player.getPlayerId()); client.setSessionId("initial");
            Assertions.assertTrue(client.needProcess(new ByteArrayProtocol(123, new byte[0], 41, 0)));
            saves = new PlayerSaveCoordinator(4, 30_000, writer == null ? (row, insert) -> write.future() : writer,
                    (action, delay) -> { deadlines.add(action); return () -> {}; }, DateUtil::currentTimeMillis);
            saves.register(player.getPlayerId(), player, () -> {});
        }
        /** @return 本例实际付款结果 */
        Future<Boolean> start() throws Exception { return start(new int[] {17, 18}); }
        /** @param extras 调用方参数 @return 原购买的支付 Future，外层邮箱任务仅执行创单入口 */
        Future<Boolean> start(int[] extras) throws Exception {
            Future<Boolean> paymentResult = await(processor.process(player.getPlayerId(), () -> PaymentOrderPublisher.create(player, PayType.FirstCharge, 73, 12, 1007, extras,
                    new PaymentOrderPublisher.Services(() -> single, id -> owner, processor, saves,
                            value -> { request = value; return rpc.future(); }, id -> {
                                if (saveFailure == 1) { throw new IllegalStateException("capture failed"); }
                                if (saveFailure == 2) { return Future.succeededFuture(List.of()); }
                                Future<List<Object>> result = PlayerHelper.saveClientCache(id, ignored -> owner, () -> single, saves,
                                        tasks -> { throw new AssertionError("禁止独立表旁路"); });
                                captured.complete(); return result;
                            }, () -> "test-server")), null));
            cn.game.games.net.game.module.recharge.PaymentCompletion.reward(player, paymentResult, () -> null);
            return paymentResult;
        }
        /** 推进真实 RPC 返回边界。 */
        void order() { rpc.complete(PaymentOrderCreateResponse_7d000021.newBuilder().setOrderId(91).build()); }
        /** 等待保存捕获实际发生。 */
        void captured() throws Exception { await(captured.future()); }
        /** 明确确认实际写入。 */
        void commit() { write.complete(new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.COMMITTED, null)); }
        /** 排在发布任务之后的屏障，不根据 Future 回调触发时机猜测任务已退出。 */
        void flush() throws Exception { await(processor.processAsync(player.getPlayerId(), () -> {}, false)); }
        /** 无可支付推送，无残留失败回调；检查在玩家邮箱内进行。 */
        void assertUnpublished() throws Exception {
            flush(); Assertions.assertEquals(0, client.published());
            await(processor.processAsync(player.getPlayerId(), () -> Assertions.assertFalse(player.getPlayerModule().execPayCallback(91)), false));
        }
        /** @return 只装配 PlayerModule 的实际 Player，不启动业务环境 */
        private static Player testPlayer() {
            PlayerData data = new PlayerData(); data.setPlayerId(8191); data.setUid(8191); data.setModules("{}");
            Player player = new Player() { @Override public PlayerData getData() { return data; } };
            player.setPlayerId(8191); player.getModules().put(PlayerModule.class.getName(), new PlayerModule()); return player;
        }
        @Override public void close() {
            write.tryComplete(new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.NOT_WRITTEN, new IllegalStateException("fixture closing")));
            executor.close();
        }
    }

    /** 实际发送参数，用于核对缓存序号与推送内容。 */
    record Sent(int sequence, byte[] data, int error) {}
    /** 只替代物理 socket，保留生产编码及响应缓存。 */
    static final class RecordingClient extends GameClient {
        /** 已同步接受的推送。 */
        final List<Sent> sent = new CopyOnWriteArrayList<>();
        /** 零为正常，一为同步拒绝，二为同步抛错。 */
        volatile int sendFailure;
        /** 初始化无外部网络的连接。 */
        RecordingClient() { super(socket()); }
        @Override public boolean sendProtocol(int messageId, int sequence, byte[] data, int error, boolean record) {
            if (error == 0 && sendFailure == 1) { return false; }
            if (error == 0 && sendFailure == 2) { throw new IllegalStateException("socket write failed"); }
            boolean accepted = super.sendProtocol(messageId, sequence, data, error, record);
            if (accepted) { sent.add(new Sent(sequence, data, error)); } return accepted;
        }
        /** @return 已接受且无错误码的可支付推送数量 */
        long published() { return sent.stream().filter(value -> value.error() == 0).count(); }
        /** @return 仅模拟网络设备，不模拟客户端协议状态 */
        private static ServerWebSocket socket() {
            return (ServerWebSocket) Proxy.newProxyInstance(ServerWebSocket.class.getClassLoader(), new Class<?>[] {ServerWebSocket.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "isClosed" -> false;
                        case "writeBinaryMessage" -> Future.succeededFuture();
                        case "binaryHandlerID", "toString" -> "order-fixture";
                        default -> throw new AssertionError("非预期 socket 调用: " + method.getName());
                    });
        }
    }
}
