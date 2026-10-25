package cn.game.games.net.game.module;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import com.google.protobuf.Message;
import cn.game.core.net.client.NetClient;
import cn.game.core.exception.LogicException;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.net.game.module.account.Account;
import cn.game.core.net.protocol.bytes.ByteArrayProtocol;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.activity.ActivityBase;
import cn.game.games.net.game.module.activity.ActivityHandler;
import cn.game.games.net.game.module.activity.ActivityModule;
import cn.game.games.net.game.module.activity.impl.player.DayGiftActivity;
import cn.game.games.net.game.module.activity.impl.player.FundPassSignActivity;
import cn.game.games.net.game.module.recharge.PayType;
import cn.game.games.net.game.module.shop.ShopHandler;
import cn.game.games.net.game.module.shop.xianshilibao.XianShiLiBaoModule;
import cn.game.protocol.generated.config.ActivityMeiRiTeHuiConfig;
import cn.game.protocol.generated.config.ActivityXianShiLiBaoConfig;
import cn.game.protocol.generated.config.FundPassConfig;
import cn.game.protocol.generated.manager.ActivityMeiRiTeHuiManager;
import cn.game.protocol.generated.manager.ActivityXianShiLiBaoManager;
import cn.game.protocol.generated.manager.FundPassManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.ActivityMsg;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.ShopMsg;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.DateUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.ServerWebSocket;

/** 调用实际购买 Handler，支付结果和奖励模块受控；不把回包测试当作充值落库或平台履约验收。 */
@Isolated
class PaymentFailureResponseTest {
    /** 只初始化玩家类型元数据，不连接服务。 */
    @BeforeAll static void metadata() { ClassManager.getInstance().init(); }

    /** 普通每日礼包和打包礼包在支付 false 时都不能执行发奖。 */
    @Test void dayGiftFalseNeverAwardsSingleOrPackage() throws Exception {
        for (int gift : List.of(1, 99)) {
            try (Fixture f = new Fixture("day", gift)) {
                f.invoke(); f.assertPending(); f.player.payment.complete(false);
                f.assertFailed(ErrorMsgEnum.unknown.getId());
            }
        }
    }

    /** 成功对照证明守卫没有关闭原来的普通和打包购买功能。 */
    @Test void dayGiftSuccessAwardsOnlyAfterPayment() throws Exception {
        for (int gift : List.of(1, 99)) {
            try (Fixture f = new Fixture("day", gift)) {
                f.invoke(); f.assertPending(); f.player.payment.complete(true); f.assertSucceeded();
            }
        }
    }

    /** 限时礼包失败必须返回既有错误码，不返回错误码为零的空奖励。 */
    @Test void limitedGiftFalseReturnsOnlyFailure() throws Exception {
        try (Fixture f = new Fixture("limited", 1)) {
            f.invoke(); f.assertPending(); f.player.payment.complete(false);
            f.assertFailed(ErrorMsgEnum.xian_shi_li_bao_buy_fail.getId());
        }
    }

    /** 限时礼包成功仍调用购买模块且只发送一次成功结果。 */
    @Test void limitedGiftSuccessKeepsOriginalRewardPath() throws Exception {
        try (Fixture f = new Fixture("limited", 1)) {
            f.invoke(); f.assertPending(); f.player.payment.complete(true); f.assertSucceeded();
        }
    }

    /** 基金签到购买不能在 Promise 未决时提前成功，失败只回一次错误。 */
    @Test void fundPendingAndFalseNeverSendSuccess() throws Exception {
        try (Fixture f = new Fixture("fund", 15)) {
            f.invoke(); f.assertPending(); f.player.payment.complete(false); f.assertFailed(ErrorMsgEnum.unknown.getId());
        }
    }

    /** 基金购买在支付成功后只调用一次解锁并发送一次结果。 */
    @Test void fundSuccessRepliesExactlyOnce() throws Exception {
        try (Fixture f = new Fixture("fund", 15)) {
            f.invoke(); f.assertPending(); f.player.payment.complete(true); f.assertSucceeded();
        }
    }

    /** 本次触达入口的异常支付不能拆箱 null，也不能误发成功。 */
    @Test void failedPaymentFutureNeverAwardsOrSendsSuccess() throws Exception {
        for (String type : List.of("day", "limited", "fund")) {
            try (Fixture f = new Fixture(type, 1)) {
                f.invoke(); f.assertPending(); f.player.payment.fail(new IllegalStateException("payment unavailable"));
                f.assertFailed(type.equals("limited") ? ErrorMsgEnum.xian_shi_li_bao_buy_fail.getId() : ErrorMsgEnum.unknown.getId());
            }
        }
    }

    /** 空结果不能通过自动拆箱进入成功或无响应状态。 */
    @Test void nullPaymentResultIsExplicitFailure() throws Exception {
        for (String type : List.of("day", "limited", "fund")) {
            try (Fixture f = new Fixture(type, 1)) {
                f.invoke(); f.player.payment.complete(null);
                f.assertFailed(type.equals("limited") ? ErrorMsgEnum.xian_shi_li_bao_buy_fail.getId() : ErrorMsgEnum.unknown.getId());
            }
        }
    }

    /** 业务错误即使被包装，也应保留其错误码和本购买协议。 */
    @Test void businessPaymentFailurePreservesErrorCode() throws Exception {
        for (String type : List.of("day", "limited", "fund")) {
            try (Fixture f = new Fixture(type, 1)) {
                f.invoke(); f.player.payment.fail(new IllegalStateException("wrapped", new LogicException(ErrorMsgEnum.times_limit)));
                f.assertFailed(ErrorMsgEnum.times_limit.getId());
            }
        }
    }

    /** 支付成功后的业务异常被派生 Future 捕获；这里只证明失败回复，不证明部分奖励可回滚。 */
    @Test void rewardCallbackFailureReturnsOneError() throws Exception {
        for (String type : List.of("day", "limited", "fund")) {
            for (boolean business : List.of(false, true)) {
                try (Fixture f = new Fixture(type, 1)) {
                    f.awardFailure = business ? new LogicException(ErrorMsgEnum.times_limit) : new IllegalStateException("award failed");
                    f.invoke(); f.player.payment.complete(true);
                    f.assertFailed(business ? ErrorMsgEnum.times_limit.getId()
                            : type.equals("limited") ? ErrorMsgEnum.xian_shi_li_bao_buy_fail.getId() : ErrorMsgEnum.unknown.getId());
                }
            }
        }
    }

    /** 旧请求失败只发送到原连接，不能改用玩家重连后的连接。 */
    @Test void lateFailureDoesNotReplyToReplacementConnection() throws Exception {
        for (String type : List.of("day", "limited", "fund")) {
            try (Fixture f = new Fixture(type, 1)) {
                f.invoke(); RecordingClient replacement = new RecordingClient(); f.player.setGameClient(replacement);
                f.player.payment.fail(new IllegalStateException("late payment failure"));
                f.assertFailed(type.equals("limited") ? ErrorMsgEnum.xian_shi_li_bao_buy_fail.getId() : ErrorMsgEnum.unknown.getId());
                Assertions.assertTrue(replacement.errors.isEmpty());
            }
        }
    }

    /** 同连接处理新请求后，迟到成功/失败仍使用原序号，缓存及重放也不得串到新请求。 */
    @Test void delayedRepliesKeepOriginalSequenceAndCache() throws Exception {
        for (String type : List.of("day", "limited", "fund")) {
            for (int outcome = 0; outcome < 4; outcome++) {
                try (Fixture f = new Fixture(type, 1)) {
                    if (outcome == 3) { f.awardFailure = new IllegalStateException("late award failure"); }
                    f.invoke(); f.assertPending();
                    Assertions.assertTrue(f.client.needProcess(new ByteArrayProtocol(123, new byte[0], 42, 0)));
                    if (outcome == 2) { f.player.payment.fail(new IllegalStateException("late failure")); }
                    else { f.player.payment.complete(outcome != 0); }
                    Assertions.assertEquals(List.of(41), f.client.sequences);
                    Assertions.assertEquals(1, f.client.getRecentMessages().get(41).size());
                    Assertions.assertFalse(f.client.getRecentMessages().containsKey(42));
                    Assertions.assertFalse(f.client.needProcess(new ByteArrayProtocol(123, new byte[0], 41, 0)));
                    Assertions.assertEquals(List.of(41, 41), f.client.sequences);
                    Assertions.assertEquals(1, f.client.getRecentMessages().get(41).size(), "重放不得再次写缓存");
                }
            }
        }
    }

    /** 隔离单个玩家和当前配置快照，所有静态入口在 close 恢复。 */
    private static final class Fixture implements AutoCloseable {
        /** 实际发送协议的记录器。 */
        final RecordingClient client = new RecordingClient();
        /** 发奖或解锁调用计数，不复制 Handler 的支付判定。 */
        final AtomicInteger awards = new AtomicInteger();
        /** 业务入口开始后、模拟资源写入前抛出的异常。 */
        RuntimeException awardFailure;
        /** 被测玩家。 */
        final TestPlayer player = new TestPlayer(client);
        /** 场景类型。 */
        final String type;
        /** 业务商品 ID。 */
        final int id;
        /** 本次替换的配置管理器。 */
        final Object manager;
        /** 配置 map 字段。 */
        final Field mapField;
        /** 还原前的配置引用。 */
        final Object originalMap;

        /** @param type 购买入口 @param id 商品 ID；只构造本轮需要的配置 */
        Fixture(String type, int id) throws Exception {
            this.type = type; this.id = id;
            Element element = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("fixture");
            element.setAttribute("ID", Integer.toString(id));
            element.setAttribute("Quota", "10"); element.setAttribute("PurchasesNum", "10"); element.setAttribute("Group", "1");
            Object config;
            String field;
            if (type.equals("day")) {
                manager = ActivityMeiRiTeHuiManager.instance(); field = "activitymeiritehuis";
                config = new ActivityMeiRiTeHuiConfig(element);
                player.activity = new DayGiftActivity() {
                    @Override public List<RewardInfo> buy(int giftId) { award(); return List.of(RewardInfo.getDefaultInstance()); }
                    @Override public List<RewardInfo> packageBuy() { award(); return List.of(RewardInfo.getDefaultInstance()); }
                };
            } else if (type.equals("limited")) {
                manager = ActivityXianShiLiBaoManager.instance(); field = "activityxianshilibaos";
                config = new ActivityXianShiLiBaoConfig(element);
                XianShiLiBaoModule module = new XianShiLiBaoModule() {
                    @Override public Iterable<RewardInfo> addBuyId(ActivityXianShiLiBaoConfig value) {
                        award(); return List.of(RewardInfo.getDefaultInstance());
                    }
                    @Override public ShopMsg.XianShiLiBaoInfo buildXianShiLiBao(int group) { return ShopMsg.XianShiLiBaoInfo.getDefaultInstance(); }
                };
                module.getGroupMap().put(1, DateUtil.currentTimeMillis() + DateUtil.DAY_MILLIS);
                player.getModules().put(XianShiLiBaoModule.class.getName(), module);
            } else {
                manager = FundPassManager.instance(); field = "fundpasss";
                config = new FundPassConfig(element);
                player.activity = new FundPassSignActivity() {
                    @Override public Message buy(int giftId) { award(); return ActivityMsg.FundPassSignBuyResponse_11000302.getDefaultInstance(); }
                };
            }
            mapField = manager.getClass().getDeclaredField(field); mapField.setAccessible(true);
            originalMap = mapField.get(manager); mapField.set(manager, Map.of(id, config));
            player.setPlayerId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
            client.setPlayerId(player.getPlayerId());
            player.setGameClient(client);
            Assertions.assertTrue(client.needProcess(new ByteArrayProtocol(123, new byte[0], 41, 0)));
            Assertions.assertNull(PlayerManager.getInstance().getAllPlayer().putIfAbsent(player.getPlayerId(), player));
        }

        /** 直接调用实际注册的业务方法，跳过网络门禁但不复制目标处理逻辑。 */
        void invoke() throws Exception {
            Object handler = type.equals("limited") ? new ShopHandler() : new ActivityHandler();
            String method = type.equals("day") ? "dayGiftBuy" : type.equals("limited") ? "BuyXianShiLiBao" : "fundPassSignBuy";
            Object request = type.equals("day") ? ActivityMsg.ActivityDayGiftBuyRequest_11000102.newBuilder().setId(100).setGiftId(id).build()
                    : type.equals("limited") ? ShopMsg.BuyXianShiLiBaoRequest_15000052.newBuilder().setId(id).build()
                    : ActivityMsg.FundPassSignBuyRequest_11000301.newBuilder().setId(id).setActivityId(100).build();
            var target = handler.getClass().getDeclaredMethod(method, NetClient.class, Object.class);
            target.setAccessible(true);
            try { target.invoke(handler, client, request); }
            catch (InvocationTargetException failure) {
                LoggerFactory.getLogger(getClass()).warn("实际 Handler 调用异常 type={}", type, failure.getCause());
                throw new AssertionError(failure.getCause());
            }
        }

        /** 未决支付不能产生成功、失败或发奖副作用。 */
        void assertPending() { Assertions.assertTrue(client.errors.isEmpty()); Assertions.assertEquals(0, awards.get()); }
        /** @param error 既有错误码；失败只回一次且没有业务写入 */
        void assertFailed(int error) {
            Assertions.assertEquals(List.of(error), client.errors); Assertions.assertEquals(0, awards.get()); assertResponseType();
        }
        /** 成功只回一次，并确实走原业务调用。 */
        void assertSucceeded() {
            Assertions.assertEquals(List.of(0), client.errors); Assertions.assertEquals(1, awards.get()); assertResponseType();
            if (type.equals("day")) { Assertions.assertEquals(1, ((ActivityMsg.ActivityDayGiftBuyResponse_11000103) client.messages.getFirst()).getRewardsCount()); }
            if (type.equals("limited")) { Assertions.assertEquals(1, ((ShopMsg.BuyXianShiLiBaoResponse_15000053) client.messages.getFirst()).getRewardsCount()); }
        }
        /** 失败路径也必须返回本购买协议，不能被测试替身改写成固定协议。 */
        void assertResponseType() {
            Class<?> expected = type.equals("day") ? ActivityMsg.ActivityDayGiftBuyResponse_11000103.class
                    : type.equals("limited") ? ShopMsg.BuyXianShiLiBaoResponse_15000053.class : ActivityMsg.FundPassSignBuyResponse_11000302.class;
            Assertions.assertEquals(expected, client.messages.getFirst().getClass());
        }
        /** 模拟业务调用，可在产生可观察副作用前抛出真实异常。 */
        void award() { if (awardFailure != null) { throw awardFailure; } awards.incrementAndGet(); }

        @Override public void close() throws IllegalAccessException {
            PlayerManager.getInstance().getAllPlayer().remove(player.getPlayerId(), player);
            mapField.set(manager, originalMap);
        }
    }

    /** 仅替换支付外部结果及活动数据，不替换 Handler 判断。 */
    private static final class TestPlayer extends Player {
        /** 由用例推进的支付结果。 */
        final Promise<Boolean> payment = Promise.promise();
        /** 请求使用的客户端。 */
        final RecordingClient client;
        /** 日志上下文，不启动账号或资源服务。 */
        final Account account = new Account();
        /** 正向路径日志需要的基本玩家字段。 */
        final PlayerData data = new PlayerData();
        /** 当前场景业务对象。 */
        ActivityBase activity;
        /** @param client 本次请求客户端 */
        TestPlayer(RecordingClient client) { this.client = client; }
        @Override public Account getAccount() { return account; }
        @Override public PlayerData getData() { return data; }
        @Override public int getLevel() { return 1; }
        @Override public long getGuildId() { return 0; }
        @Override public Future<Boolean> pay(PayType type, int id, int[] cost, int... others) { return payment.future(); }
        @Override public ActivityModule getActivityModule() {
            return new ActivityModule() {
                @Override @SuppressWarnings("unchecked") public <T extends ActivityBase> T get(int id) { return (T) activity; }
            };
        }
    }

    /** 记录应用层回包，避免引入真实连接或协议编解码环境。 */
    private static final class RecordingClient extends GameClient {
        /** 顺序记录每次发送的错误码，零表示成功。 */
        final List<Integer> errors = new ArrayList<>();
        /** 保留真实业务协议及 payload，防止仅错误码断言掩盖错误响应类型。 */
        final List<Message> messages = new ArrayList<>();
        /** 实际传入底层发送的序号。 */
        final List<Integer> sequences = new ArrayList<>();
        /** 使用无网络 socket，但保留真实 GameClient 编码、缓存和重放。 */
        RecordingClient() { super(socket()); }
        @Override public boolean sendProtocol(int messageId, int sequence, byte[] data, int error, boolean record) {
            boolean sent = super.sendProtocol(messageId, sequence, data, error, record);
            Assertions.assertTrue(sent);
            try { messages.add((Message) PbProtocol.parsersMap.get(messageId).parseFrom(data)); }
            catch (com.google.protobuf.InvalidProtocolBufferException invalid) {
                LoggerFactory.getLogger(getClass()).warn("实际发送的业务响应无法解码", invalid); throw new AssertionError(invalid);
            }
            errors.add(error); sequences.add(sequence); return true;
        }
        /** @return 仅替代网络写入的 socket，缓存仍由生产发送方法维护 */
        private static ServerWebSocket socket() {
            return (ServerWebSocket) Proxy.newProxyInstance(ServerWebSocket.class.getClassLoader(), new Class<?>[] {ServerWebSocket.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "isClosed" -> false;
                        case "writeBinaryMessage" -> Future.succeededFuture();
                        case "binaryHandlerID", "toString" -> "payment-fixture";
                        default -> throw new AssertionError("非预期 socket 调用: " + method.getName());
                    });
        }
    }
}
