package cn.game.games.net.game.helper;

import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.game.core.base.ServerContext;
import cn.game.core.net.process.Processor;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.AsyncUtils;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.games.net.game.module.recharge.PayType;
import cn.game.games.net.game.module.recharge.PaymentReply;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderCreateRequest_7d000020;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderCreateResponse_7d000021;
import cn.game.protocol.protobuf.ShopMsg.PaymentOrderPush_15010020;
import cn.game.util.ServerType;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * 现金订单先保存玩家关联，再尝试发布；仅支持聚合表，不把独立表保存当作关联已落库。
 * 保存等待占用玩家邮箱，SDK 发货只能随后执行；保存完成由独立线程通知，不回投同一邮箱。
 * 失败保留关联供后续恢复，不自动重放创单或发布，也不处理 Login 孤儿订单与最终发奖幂等。
 */
public final class PaymentOrderPublisher {
    /** 记录阶段失败与玩家身份，不记录支付凭据。 */
    private static final Logger log = LoggerFactory.getLogger(PaymentOrderPublisher.class);

    /** 工具类不实例化。 */
    private PaymentOrderPublisher() {}

    /**
     * 必须在玩家任务内、完成代金券等前置分支后调用；复制购买参数并固定原连接及序号。
     * @param player 当前权威玩家 @param type 购买类型 @param id 商品业务 ID
     * @param rmb 实际支付元数 @param sku 实际向 SDK 请求的商品 ID @param others 额外业务参数
     * @return 发货回调结果；发布订单不会提前完成 true，失败成功完成 false
     */
    public static Future<Boolean> create(Player player, PayType type, int id, int rmb, int sku, int[] others) {
        return create(player, type, id, rmb, sku, others, new Services(ServerContext.getInstance()::isSinglePlayerTable,
                PlayerManager.getInstance()::getPlayer, ServerContext.getInstance().getProcessor(), PlayerSaveCoordinator.shared(),
                request -> VxHolder.requestRemoteServer(ServerType.Login, request), PlayerHelper::saveClientCache,
                ServerContext.getInstance()::getServerId));
    }

    /** 外部边界的依赖集合；生产与测试共用下面的创单、关联、等待及发布流程。 */
    record Services(BooleanSupplier single, LongFunction<Player> players, Processor processor, PlayerSaveCoordinator saves,
            Function<PaymentOrderCreateRequest_7d000020, Future<PaymentOrderCreateResponse_7d000021>> request,
            LongFunction<Future<List<Object>>> save, Supplier<String> serverId) {}

    /** @param player 原玩家 @param type 购买类型 @param id 业务 ID @param rmb 元数 @param sku SDK 商品
     * @param others 可变入参，立即复制 @param services 外部依赖 @return 实际付款结果，不是创单结果 */
    static Future<Boolean> create(Player player, PayType type, int id, int rmb, int sku, int[] others, Services services) {
        Promise<Boolean> payment = Promise.promise();
        GameClient client = player.getGameClient();
        PaymentReply reply = new PaymentReply(client);
        String session = client == null ? null : client.getSessionId();
        int[] copied = others == null ? new int[0] : others.clone();
        try {
            requireCurrent(player, client, session, services);
            PaymentOrderCreateRequest_7d000020 request = PaymentOrderCreateRequest_7d000020.newBuilder()
                    .setPlayerId(player.getPlayerId()).setUserId(player.getData().getUid())
                    .setPlatform(Integer.toString(player.getAccount().platform)).setSessionId(session)
                    .setGoodsPrice(Math.multiplyExact(rmb, 100)).setItemId(Integer.toString(sku)).build();
            services.request.apply(request).compose(order -> services.processor.processAsync(player.getPlayerId(),
                    () -> associateAndPublish(player, client, session, type, id, rmb, sku, copied, order, payment, reply, services), false))
                    .onFailure(failure -> fail(player, reply, payment, failure));
        } catch (Exception failure) {
            fail(player, reply, payment, failure);
        }
        return payment.future();
    }

    /** 在真实玩家邮箱执行；关闭变更作用域后才捕获快照，保存期间不允许 SDK 回调越过本任务。
     * @param player 原玩家 @param client 原连接 @param session 原会话 @param type 业务类型 @param id 业务 ID
     * @param rmb 元数 @param sku SDK 商品 @param others 已复制参数 @param order Login 创单结果
     * @param payment 付款承诺 @param reply 原请求响应 @param services 外部依赖 */
    private static void associateAndPublish(Player player, GameClient client, String session, PayType type, int id, int rmb,
            int sku, int[] others, PaymentOrderCreateResponse_7d000021 order, Promise<Boolean> payment,
            PaymentReply reply, Services services) {
        boolean callbackRegistered = false;
        try {
            requireCurrent(player, client, session, services);
            if (order.getOrderId() <= 0 || player.getPlayerModule().getPayItems(order.getOrderId()) != null) {
                throw new IllegalStateException("PAYMENT_ORDER_INVALID_OR_DUPLICATE");
            }
            PayItem item = new PayItem();
            item.setOrderId(order.getOrderId()); item.setRmb(rmb); item.setPayType(type); item.setPayId(id);
            item.setSdkGoodsId(sku); item.addPaySubIds(others);
            try (var mutation = services.saves.beginMutation(player.getPlayerId(), player)) {
                player.getPlayerModule().addPayItems(item);
            }

            // 只等待保存，不等待支付承诺；后者必须等未来的 SDK 发货任务才能完成。
            requireCurrent(player, client, session, services);
            List<Object> saved = AsyncUtils.await(services.save.apply(player.getPlayerId()));
            if (saved == null || saved.stream().noneMatch(value -> value instanceof PlayerSaveCoordinator.Receipt receipt
                    && receipt.playerId() == player.getPlayerId())) {
                throw new IllegalStateException("PAYMENT_ASSOCIATION_NOT_CONFIRMED");
            }
            requireCurrent(player, client, session, services);
            if (player.getPlayerModule().getPayItems(order.getOrderId()) != item || item.isFinish()) {
                throw new IllegalStateException("PAYMENT_ASSOCIATION_CHANGED");
            }
            player.getPlayerModule().addPayCallback(order.getOrderId(), payment);
            callbackRegistered = true;
            boolean sent = reply.trySend(PaymentOrderPush_15010020.newBuilder().setOrder(order.getOrder())
                    .setOrderId(Long.toString(order.getOrderId())).setSdkGoodsId(Integer.toString(sku))
                    .setServerId(services.serverId.get()).build(), 0);
            if (!sent) { throw new IllegalStateException("PAYMENT_ORDER_SEND_REJECTED"); }
        } catch (Exception failure) {
            // 只移除本次注册的回调；已保存或可能保存的关联必须保留，重复订单不能影响旧回调。
            if (callbackRegistered) { player.getPlayerModule().removePayCallback(order.getOrderId(), payment); }
            fail(player, reply, payment, failure);
        }
    }

    /** @param player 原玩家 @param client 原连接 @param session 原会话 @param services 权威查找与存储模式 */
    private static void requireCurrent(Player player, GameClient client, String session, Services services) {
        if (!services.single.getAsBoolean()) { throw new IllegalStateException("PAYMENT_REQUIRES_AGGREGATE_STORAGE"); }
        if (services.players.apply(player.getPlayerId()) != player || !player.isActive() || !player.isOnline()
                || player.isIslogouting() || client == null || player.getGameClient() != client || !client.isActive()
                || !Objects.equals(session, client.getSessionId())) {
            throw new IllegalStateException("PAYMENT_REQUEST_OWNER_OR_CONNECTION_CHANGED");
        }
    }

    /** @param player 原玩家 @param reply 原请求 @param payment 付款承诺 @param failure 真实原因；发送失败也终结等待 */
    private static void fail(Player player, PaymentReply reply, Promise<Boolean> payment, Throwable failure) {
        log.warn("订单关联保存或发布失败 playerId={}，保留已有关联", player.getPlayerId(), failure);
        try { reply.send(PaymentOrderPush_15010020.getDefaultInstance(), ErrorMsgEnum.payment_order_create_fail.getId()); }
        catch (Exception sendFailure) { log.warn("订单失败响应无法发送 playerId={}", player.getPlayerId(), sendFailure); }
        finally { payment.tryComplete(false); }
    }
}
