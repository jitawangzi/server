package cn.game.games.net.game.helper;

import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.parallel.Isolated;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.util.JsonUtil;
import io.vertx.core.Promise;

/** 通过生产发货入口验证新凭证仅在原提交确认后生效，历史和不明状态保持拒绝。 */
@Isolated
class PaymentIdempotencyTest {
    /** 初始化模块和隔离统计依赖。 */
    @BeforeAll static void metadata() throws Exception { PaymentDeliveryLifecycleTest.metadata(); }
    /** 恢复统计依赖。 */
    @AfterAll static void restore() throws Exception { PaymentDeliveryLifecycleTest.restore(); }

    /** 重复和并发通知共享玩家邮箱，仅一次奖励、Charge 和保存，两个独立 RPC 都可成功。 */
    @Test void concurrentAndRepeatedNotificationsSucceedOnlyAfterCommit() throws Exception {
        try (var f = new PaymentDeliveryLifecycleTest.Fixture(true)) {
            Promise<Void> entered = Promise.promise(), reward = Promise.promise();
            f.reward = () -> { f.award(); entered.complete(); return reward.future(); };
            f.ship(); PaymentDeliveryLifecycleTest.await(entered.future()); f.ship();
            Assertions.assertTrue(f.replies.isEmpty()); reward.complete(); f.captured();
            PayItem item = f.player.getPlayerModule().getPayItems(91);
            Assertions.assertFalse(item.hasCommittedCompletionProof(f.id(), "sdk-91"));
            Assertions.assertTrue(f.replies.isEmpty()); f.commit(); f.flush(); f.ship(); f.flush();
            Assertions.assertEquals(List.of(true, true, true), f.replies);
            Assertions.assertEquals(1, f.awards.get()); Assertions.assertEquals(1, f.charges.get());
            Assertions.assertTrue(item.hasCommittedCompletionProof(f.id(), "sdk-91"));
            Assertions.assertEquals(1, f.deadlines.size(), "去重不再保存，也不通过第二次保存发布凭证");
        }
    }

    /** 单纯 finish 或阶段名、在途、失败与人工核对均不能推断成功或重发。 */
    @Test void legacyAndUnprovenStatesRemainBlocked() throws Exception {
        for (String state : new String[] {null, "EXECUTING", "REWARDS_COMPLETED", "REVIEW_REQUIRED", "UNKNOWN", "FAILED"}) {
            try (var f = new PaymentDeliveryLifecycleTest.Fixture(true)) {
                PayItem item = f.player.getPlayerModule().getPayItems(91);
                item.setCompletionState(state); item.setFinish(true);
                f.ship(); f.flush(); f.assertRejectedBeforeBusiness();
            }
        }
    }

    /** 凭证绑定全部持久订单参数；玩家、单号、商品或金额冲突都不能走成功去重。 */
    @Test void proofRejectsIdentityProductAndAmountMismatch() throws Exception {
        try (var f = new PaymentDeliveryLifecycleTest.Fixture(true)) {
            f.ship(); f.captured(); f.commit(); f.flush();
            PayItem item = f.player.getPlayerModule().getPayItems(91);
            Assertions.assertFalse(item.hasCommittedCompletionProof(f.id() + 1, "sdk-91"));
            Assertions.assertFalse(item.hasCommittedCompletionProof(f.id(), "another-sdk-order"));
            int rmb = item.getRmb(); item.setRmb(rmb + 1); f.ship(); f.flush(); item.setRmb(rmb);
            int product = item.getPayId(); item.setPayId(product + 1); f.ship(); f.flush(); item.setPayId(product);
            item.getPaySubIds().add(999); f.ship(); f.flush(); item.getPaySubIds().removeLast();
            item.setSdkOrderId("another-sdk-order"); f.ship(); f.flush();
            Assertions.assertEquals(List.of(true, false, false, false, false), f.replies);
            Assertions.assertEquals(1, f.awards.get()); Assertions.assertEquals(1, f.charges.get());
        }
    }

    /** JSON 兼容仅验证字段恢复，真实数据库冷读另由 IT 证明。 */
    @Test void versionedProofRoundTripAndOldJsonCompatibility() throws Exception {
        try (var f = new PaymentDeliveryLifecycleTest.Fixture(true)) {
            f.ship(); f.captured();
            PayItem item = f.player.getPlayerModule().getPayItems(91);
            String json = JsonUtil.toJsonString(item);
            Assertions.assertFalse(json.contains("proofAwaitingCommit"));
            Assertions.assertFalse(item.hasCommittedCompletionProof(f.id(), "sdk-91"));
            PayItem restored = JsonUtil.parseObject(json, PayItem.class);
            Assertions.assertTrue(restored.hasCommittedCompletionProof(f.id(), "sdk-91"));
            Assertions.assertTrue(JsonUtil.parseObject(json.replace("\"version\":1", "\"version\":1,\"futureProofField\":true")
                    .replaceFirst("\\{", "{\"futureItemField\":123,"), PayItem.class).hasCommittedCompletionProof(f.id(), "sdk-91"));
            Assertions.assertFalse(JsonUtil.parseObject(json.replace("\"version\":1", "\"version\":2,\"futureProofField\":true").replaceFirst("\\{", "{\"futureItemField\":123,"), PayItem.class)
                    .hasCommittedCompletionProof(f.id(), "sdk-91"));
            PayItem legacy = JsonUtil.parseObject("{\"orderId\":91,\"isFinish\":true}", PayItem.class);
            Assertions.assertFalse(legacy.hasCommittedCompletionProof(f.id(), null));
            f.commit(); f.flush();
        }
    }

    /** 用基线字段模式确认旧 Jackson 会拒绝新字段；发布必须停旧写入器，写入后禁止直接回滚旧版本。 */
    @Test void baselineJsonSchemaRejectsNewProofAndRequiresStoppedUpgrade() throws Exception {
        try (var f = new PaymentDeliveryLifecycleTest.Fixture(true)) {
            PayItem item = f.player.getPlayerModule().getPayItems(91);
            Assertions.assertNotNull(JsonUtil.parseObject(JsonUtil.toJsonString(item), BaselinePayItemSchema.class));
            f.ship(); f.captured(); f.commit(); f.flush();
            RuntimeException failure = Assertions.assertThrows(RuntimeException.class,
                    () -> JsonUtil.parseObject(JsonUtil.toJsonString(item), BaselinePayItemSchema.class));
            var unknown = Assertions.assertInstanceOf(com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException.class, failure.getCause());
            Assertions.assertEquals("completionProof", unknown.getPropertyName());
        }
    }

    /** de2fff86 中 PayItem 的持久字段模式，仅验证旧读者对新字段的实际拒绝行为。 */
    private record BaselinePayItemSchema(long orderId, String sdkOrderId, int rmb,
            cn.game.games.net.game.module.recharge.PayType payType, int payId, int sdkGoodsId,
            List<Integer> paySubIds, int addId, int addCount, boolean isFinish, long finishTime, String completionState) {}
}
