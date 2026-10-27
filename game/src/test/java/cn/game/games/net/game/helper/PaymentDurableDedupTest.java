package cn.game.games.net.game.helper;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.games.net.game.module.recharge.PayType;
import io.vertx.core.Promise;

/** 验证完整身份、隔离测试标识符和支付失败后的普通保存保护；SQL 原子性由真实库用例验证。 */
class PaymentDurableDedupTest {
    /** 可变子参数在占位时封存，所有购买字段都参加身份比较。 */
    @Test void identityBindsAllArgumentsAndCopiesSubIds() {
        PayItem item = item();
        var intent = PaymentDedupStore.intent(7, item, "sdk");
        item.getPaySubIds().add(3);
        Assertions.assertEquals(List.of(1, 2), intent.paySubIds());
        Assertions.assertNotEquals(intent, PaymentDedupStore.intent(7, item, "sdk"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> PaymentDedupStore.intent(0, item, "sdk"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> PaymentDedupStore.intent(7, item, " "));
        Assertions.assertThrows(IllegalArgumentException.class, () -> PaymentDedupStore.intent(7, item, "x".repeat(192)));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> intent.paySubIds().add(9));
    }

    /** 包内测试接缝拒绝任意表名、大小写和 SQL 注入，生产表名不开放配置。 */
    @Test void controlledTableNamesRejectBusinessInput() {
        for (String suffix : new String[] {"", "a", "A".repeat(32), "a".repeat(31) + ";", "t_player_data", "../test"}) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> new PaymentDedupStore(() -> null, suffix));
        }
        Assertions.assertNotNull(new PaymentDedupStore(() -> null, "a".repeat(32)));
        Assertions.assertFalse(cn.game.util.Config.paymentDurableDedupEnabled, "默认部署不得依赖尚未迁移的表");
    }

    /** 历史 finish、未完成阶段和参数漂移均不能转换为完成正文。 */
    @Test void completionRequiresOriginalProofAndMatchingIdentity() {
        PayItem item = item(); item.setSdkOrderId("sdk");
        var claim = new PaymentDedupStore.Claim(new PaymentDedupStore(() -> null, "a".repeat(32)),
                PaymentDedupStore.intent(7, item, "sdk"), "test-token");
        item.finish();
        Assertions.assertThrows(IllegalStateException.class, () -> PaymentDedupStore.completed(claim, item));
        item.setCompletionState("REWARDS_COMPLETED"); item.prepareCompletionProof(7);
        Assertions.assertNotNull(PaymentDedupStore.completed(claim, item));
        item.setAddCount(99);
        Assertions.assertThrows(IllegalStateException.class, () -> PaymentDedupStore.completed(claim, item));
    }

    /** 隔离状态不被迟到确认、保存代次重置或正常自动保存解除。 */
    @Test void freezeRejectsQueuedAndFutureSavesEvenAfterLateCommit() throws Exception {
        Player owner = new Player(); owner.setPlayerId(7);
        Promise<PlayerSaveCoordinator.WriteResult> write = Promise.promise();
        var saves = new PlayerSaveCoordinator(3, 1000, (row, insert) -> write.future(), (r, delay) -> () -> {}, () -> 100L);
        saves.register(7, owner, () -> {});
        PlayerData data = new PlayerData(); data.setPlayerId(7);
        var first = saves.submit(owner, data, false); var second = saves.submit(owner, data, false);
        saves.freezePayment(7, owner, new IllegalStateException("test fault"));
        Assertions.assertTrue(second.result().failed());
        Assertions.assertThrows(PlayerSaveCoordinator.SaveFailure.class, () -> saves.preparing(owner, 7));
        Assertions.assertTrue(saves.submit(owner, data, false).result().failed());
        write.complete(new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.COMMITTED, null));
        PaymentDeliveryLifecycleTest.await(first.result());
        Assertions.assertTrue(saves.status(7).writeProtected());
        Assertions.assertThrows(PlayerSaveCoordinator.SaveFailure.class, () -> saves.requirePaymentReady(7, owner));
        Assertions.assertThrows(PlayerSaveCoordinator.SaveFailure.class, () -> saves.beginMutation(7, owner));
        Assertions.assertFalse(saves.release(7, owner, () -> true));
        List<Long> automatic = new ArrayList<>();
        saves.sweep((id, action) -> { automatic.add(id); return action.get(); }, (id, value) -> { throw new AssertionError(); });
        Assertions.assertTrue(automatic.isEmpty());
    }

    /** @return 覆盖全部购买字段的基础订单 */
    static PayItem item() {
        PayItem item = new PayItem(); item.setOrderId(91); item.setRmb(12); item.setPayType(PayType.FirstCharge);
        item.setPayId(73); item.setSdkGoodsId(55); item.setAddId(100); item.setAddCount(2); item.addPaySubIds(1, 2);
        return item;
    }
}
