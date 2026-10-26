package cn.game.login.net.clientpacket.vertx.wechat;

import cn.game.login.cache.entity.PayOrder;
import cn.game.login.mapper.PayOrderMapper;
import cn.game.util.DateUtil;

/** 通过数据库条件合并回执；各入口不得用读取时的旧订单回写完成状态。 */
public final class PaymentOrderState {
    /** 工具类不创建实例。 */
    private PaymentOrderState() {}

    /** @param order 待发货订单 @return 是否具有有效身份、金额及已知支付状态；商品参数由 Game 持久订单权威校验 */
    public static boolean canShip(PayOrder order) {
        return order != null && order.getId() != null && order.getId() > 0
                && order.getPlayerId() != null && order.getPlayerId() > 0
                && order.getPrice() != null && order.getPrice() > 0
                && (Byte.valueOf((byte) 1).equals(order.getPayState()) || Byte.valueOf((byte) 2).equals(order.getPayState()));
    }

    /** @param order 已校验订单 @return 创单绑定的 SDK 身份；没有绑定时固定为空，不以微信交易流水替换预支付单号 */
    public static String sdkOrderId(PayOrder order) {
        return org.apache.commons.lang3.StringUtils.isBlank(order.getThirdOrderId()) ? "" : order.getThirdOrderId();
    }

    /** @param bound 已有流水 @param received 本次流水 @return 是否允许首次绑定或重放同一流水 */
    public static boolean matches(String bound, String received) {
        return org.apache.commons.lang3.StringUtils.isEmpty(bound) || java.util.Objects.equals(bound, received);
    }

    /** @param mapper 当前订单存储 @param order 已校验回执及原订单身份 @param delivered Game 已提交成功，不代表平台已接收通知 */
    public static void record(PayOrderMapper mapper, PayOrder order, boolean delivered) {
        order.setPayDate(DateUtil.nowDateStr()); order.setPayTime(DateUtil.nowTimeStr());
        order.setCompleteDate(DateUtil.nowDateStr()); order.setCompleteTime(DateUtil.nowTimeStr());
        if (mapper.mergePaymentReceipt(order, delivered) == 0) {
            // MySQL 可将相同值更新报告为零，必须冷查身份和目标状态，不能把零行当成功。
            PayOrder stored = mapper.selectByPrimaryKey(order.getId());
            if (stored == null || !java.util.Objects.equals(stored.getPlayerId(), order.getPlayerId())
                    || !java.util.Objects.equals(stored.getPrice(), order.getPrice())
                    || !java.util.Objects.equals(stored.getThirdUid(), order.getThirdUid())
                    || !java.util.Objects.equals(stored.getThirdOrderId(), order.getThirdOrderId())
                    || !java.util.Objects.equals(stored.getTransactionId(), order.getTransactionId())
                    || !java.util.Objects.equals(stored.getMchOrderNo(), order.getMchOrderNo())
                    || (delivered && !Boolean.TRUE.equals(stored.getIsDeliver()))
                    || !Byte.valueOf((byte) 2).equals(stored.getPayState())) {
                throw new IllegalStateException("PAYMENT_RECEIPT_CONFLICT orderId=" + order.getId());
            }
        }
    }
}
