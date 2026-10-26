package cn.game.games.net.game.helper;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.log.GameSSLogger;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.player.PlayerModule;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.games.net.game.module.recharge.PayType;
import cn.game.util.JsonUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/** 使用实际发货、回调绑定和保存协调器，外部奖励/事务终态可控；不复制被测流程。 */
@Isolated
class PaymentCompletionTest {
    /** 恢复统计入口，测试不连接外部统计系统。 */
    private static Object originalStatistics;

    /** 初始化模块元数据，仅替换外部统计输出。 */
    @BeforeAll static void metadata() throws Exception {
        ClassManager.getInstance().init();
        var field = GameSSLogger.class.getDeclaredField("instance"); field.setAccessible(true); originalStatistics = field.get(null);
        field.set(null, new GameSSLogger() { @Override public void logRecharge(Player player, String step, PayItem item) {} });
    }

    /** 恢复全局依赖。 */
    @AfterAll static void restore() throws Exception {
        var field = GameSSLogger.class.getDeclaredField("instance"); field.setAccessible(true); field.set(null, originalStatistics);
    }

    /** 异步奖励未决时不能标记、Charge、保存、回购买成功；完成后再等真实保存回执。 */
    @Test void delayedRewardPrecedesFinishSnapshotAndBothAcknowledgements() throws Exception {
        try (var f = new PaymentDeliveryLifecycleTest.Fixture(true)) {
            Promise<Void> entered = Promise.promise(), reward = Promise.promise();
            f.reward = () -> { f.award(); entered.complete(); return reward.future(); };
            f.ship(); PaymentDeliveryLifecycleTest.await(entered.future());
            PayItem item = f.player.getPlayerModule().getPayItems(91);
            Assertions.assertFalse(item.isFinish()); Assertions.assertEquals("EXECUTING", item.getCompletionState());
            Assertions.assertEquals(0, f.charges.get()); Assertions.assertFalse(f.captured.future().isComplete());
            Assertions.assertFalse(f.purchase.isComplete()); Assertions.assertTrue(f.replies.isEmpty());
            reward.complete(); f.captured();
            Assertions.assertTrue(item.isFinish()); Assertions.assertEquals("REWARDS_COMPLETED", item.getCompletionState());
            Assertions.assertEquals(1, f.charges.get()); Assertions.assertFalse(f.purchase.isComplete());
            java.util.Map<String, cn.game.games.core.BasePlayerModule> snapshot = JsonUtil.parseObjectWithType(f.writtenSnapshot.getModules());
            PlayerModule stored = (PlayerModule) snapshot.get(PlayerModule.class.getName());
            Assertions.assertTrue(stored.getPayItems(91).isFinish());
            Assertions.assertEquals("REWARDS_COMPLETED", stored.getPayItems(91).getCompletionState());
            Assertions.assertEquals(1, stored.getGuideMap().get(700));
            Assertions.assertTrue(f.replies.isEmpty()); f.commit(); f.flush();
            Assertions.assertTrue(f.purchase.succeeded()); Assertions.assertEquals(List.of(true), f.replies);
            f.ship(); f.flush(); Assertions.assertEquals(1, f.awards.get()); Assertions.assertEquals(List.of(true, true), f.replies);
        }
    }

    /** 同步抛错及异步失败会到达发货入口；部分资源已变化时不得回退 finish 后重发。 */
    @Test void partialRewardFailureQuarantinesInsteadOfReplaying() throws Exception {
        for (boolean async : List.of(false, true)) {
            try (var f = new PaymentDeliveryLifecycleTest.Fixture(true)) {
                f.reward = () -> {
                    f.award();
                    if (async) { return Future.failedFuture("async award fault"); }
                    throw new IllegalStateException("synchronous award fault");
                };
                f.ship(); f.flush(); f.ship(); f.flush();
                PayItem item = f.player.getPlayerModule().getPayItems(91);
                Assertions.assertFalse(item.isFinish()); Assertions.assertEquals("REVIEW_REQUIRED", item.getCompletionState());
                Assertions.assertEquals(1, f.awards.get()); Assertions.assertEquals(0, f.charges.get());
                Assertions.assertTrue(f.purchase.failed()); Assertions.assertEquals(List.of(false, false), f.replies);
                Assertions.assertFalse(f.captured.future().isComplete()); Assertions.assertSame(f.player, f.owner);
                Assertions.assertTrue(f.saves.status(f.id()).observedDirty());
            }
        }
    }

    /** Charge 链失败发生在 finish 前，已发奖励保留但不允许自动重发。 */
    @Test void chargeFailureNeverFinishesOrRepeatsRewards() throws Exception {
        try (var f = new PaymentDeliveryLifecycleTest.Fixture(true)) {
            f.player.registerEventHandler(EventTypeEnum.Charge, event -> {
                Assertions.assertFalse(f.player.getPlayerModule().getPayItems(91).isFinish());
                throw new IllegalStateException("Charge downstream fault");
            });
            f.ship(); f.flush(); f.ship(); f.flush();
            Assertions.assertFalse(f.player.getPlayerModule().getPayItems(91).isFinish());
            Assertions.assertEquals("REVIEW_REQUIRED", f.player.getPlayerModule().getPayItems(91).getCompletionState());
            Assertions.assertEquals(1, f.awards.get()); Assertions.assertTrue(f.purchase.failed());
            Assertions.assertEquals(List.of(false, false), f.replies);
        }
    }

    /** 已注册 Boolean 承诺却没有完整奖励绑定时明确拒绝，不消费旧成功监听器，也不离线重发。 */
    @Test void bareBooleanCallbackCannotClaimBusinessCompletion() throws Exception {
        try (var f = new PaymentDeliveryLifecycleTest.Fixture(true)) {
            Promise<Boolean> legacy = Promise.promise(); AtomicInteger legacyAwards = new AtomicInteger();
            legacy.future().onSuccess(paid -> legacyAwards.incrementAndGet());
            f.player.getPlayerModule().addPayCallback(91, legacy);
            f.ship(); f.flush();
            Assertions.assertTrue(legacy.future().failed()); Assertions.assertEquals(0, legacyAwards.get());
            Assertions.assertEquals(0, f.awards.get()); Assertions.assertFalse(f.player.getPlayerModule().getPayItems(91).isFinish());
            Assertions.assertEquals(List.of(false), f.replies);
        }
    }

    /** 离线真实 PayType 异常同样保守阻断，不能将异常订单作为下次补单的新订单。 */
    @Test void offlineRewardFailureBlocksFurtherDelivery() throws Exception {
        try (var f = new PaymentDeliveryLifecycleTest.Fixture(false)) {
            // 首充的缺失活动参数在实际 offlinePay 中抛错，验证其确实回传发货入口。
            PayItem item = f.player.getPlayerModule().getPayItems(91); item.getPaySubIds().clear();
            f.ship(); f.flush(); f.ship(); f.flush();
            Assertions.assertFalse(item.isFinish()); Assertions.assertEquals("REVIEW_REQUIRED", item.getCompletionState());
            Assertions.assertEquals(List.of(false, false), f.replies); Assertions.assertSame(f.player, f.owner);
        }
    }

    /** 保存未写入、UNKNOWN 和等待超时都不能发布购买成功，迟到提交及通知不重发奖励。 */
    @Test void saveFailureUnknownAndTimeoutNeverReleasePurchaseSuccess() throws Exception {
        for (int mode = 0; mode < 3; mode++) {
            try (var f = new PaymentDeliveryLifecycleTest.Fixture(true)) {
                f.ship(); f.captured(); Assertions.assertFalse(f.purchase.isComplete());
                if (mode == 2) { f.deadlines.getFirst().run(); }
                else { f.write.complete(new PlayerSaveCoordinator.WriteResult(mode == 0
                        ? PlayerSaveCoordinator.Outcome.NOT_WRITTEN : PlayerSaveCoordinator.Outcome.UNKNOWN,
                        new IllegalStateException("controlled commit failure"))); }
                f.flush(); Assertions.assertTrue(f.purchase.failed()); Assertions.assertEquals(List.of(false), f.replies);
                Assertions.assertSame(f.player, f.owner); Assertions.assertEquals(0, f.removes.get());
                if (mode == 2) { f.commit(); f.flush(); }
                f.ship(); f.flush(); Assertions.assertEquals(1, f.awards.get()); Assertions.assertEquals(List.of(false, false), f.replies);
            }
        }
    }

    /** 发奖等待中失去权威身份，返回后不能继续 Charge 或保存旧实例。 */
    @Test void ownerReplacementDuringAsyncRewardCannotFinish() throws Exception {
        try (var f = new PaymentDeliveryLifecycleTest.Fixture(true)) {
            Promise<Void> entered = Promise.promise(), reward = Promise.promise();
            f.reward = () -> { f.award(); entered.complete(); return reward.future(); };
            f.ship(); PaymentDeliveryLifecycleTest.await(entered.future()); f.owner = new Player(); reward.complete(); f.flush();
            Assertions.assertFalse(f.player.getPlayerModule().getPayItems(91).isFinish());
            Assertions.assertEquals(0, f.charges.get()); Assertions.assertFalse(f.captured.future().isComplete());
            Assertions.assertTrue(f.purchase.failed()); Assertions.assertEquals(List.of(false), f.replies);
        }
    }

    /** 阻断状态纳入既有 JSON 字段兼容链，旧 finish 不生成新的完成证据。此用例不是数据库验收。 */
    @Test void persistedReviewStateAndLegacyFinishRemainConservative() throws Exception {
        PayItem item = new PayItem(); item.setOrderId(91); item.setCompletionState("REVIEW_REQUIRED");
        String json = JsonUtil.toJsonString(item);
        PayItem restored = JsonUtil.parseObject(json, PayItem.class);
        Assertions.assertFalse(restored.isFinish()); Assertions.assertEquals("REVIEW_REQUIRED", restored.getCompletionState());
        try (var f = new PaymentDeliveryLifecycleTest.Fixture(true)) {
            f.player.getPlayerModule().addPayItems(restored); f.ship(); f.flush(); f.assertRejectedBeforeBusiness();
        }
        PayItem legacy = JsonUtil.parseObject("{\"orderId\":91,\"isFinish\":true}", PayItem.class);
        Assertions.assertTrue(legacy.isFinish()); Assertions.assertNull(legacy.getCompletionState());
    }

    /** 真实月卡离线购买等待邮件插入结果；不等待同邮箱 RPC，失败不推进领取时间或重发邮件。 */
    @Test void monthCardOfflinePurchaseWaitsForActualMailResult() throws Exception {
        var manager = cn.game.protocol.generated.manager.MonthCardManager.instance();
        var field = manager.getClass().getDeclaredField("monthcards"); field.setAccessible(true); Object original = field.get(manager);
        var element = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("fixture");
        element.setAttribute("ID", "1"); element.setAttribute("DailyRewards", "101;2");
        field.set(manager, java.util.Map.of(1, new cn.game.protocol.generated.config.MonthCardConfig(element)));
        try {
            for (boolean success : List.of(false, true)) {
                try (var f = new PaymentDeliveryLifecycleTest.Fixture(false)) {
                    Promise<Void> entered = Promise.promise(); Promise<Object> mail = Promise.promise(); AtomicInteger mailCalls = new AtomicInteger();
                    var monthCard = new cn.game.games.net.game.module.shop.monthcard.MonthCardModule() {
                        @Override protected Future<Object> sendPaymentMail(List<cn.game.games.net.game.module.award.Goods> attachments, boolean notify) {
                            Assertions.assertFalse(notify); Assertions.assertEquals(1, attachments.size());
                            Assertions.assertEquals(2, attachments.getFirst().getCount());
                            f.businessThread = Thread.currentThread(); mailCalls.incrementAndGet(); entered.complete(); return mail.future();
                        }
                    };
                    monthCard.initDefault(f.player);
                    f.player.getModules().put(cn.game.games.net.game.module.shop.monthcard.MonthCardModule.class.getName(), monthCard);
                    PayItem item = f.player.getPlayerModule().getPayItems(91); item.setPayType(PayType.MonthCard); item.setPayId(1);
                    f.ship(); PaymentDeliveryLifecycleTest.await(entered.future());
                    long previous = monthCard.getMonthCard(1).getLastRewardTime();
                    Assertions.assertFalse(item.isFinish()); Assertions.assertFalse(f.captured.future().isComplete()); Assertions.assertTrue(f.replies.isEmpty());
                    if (success) {
                        mail.complete(1); f.captured();
                        Assertions.assertTrue(monthCard.getMonthCard(1).getLastRewardTime() > previous);
                        f.commit(); f.flush(); Assertions.assertEquals(List.of(true), f.replies);
                    } else {
                        mail.fail("mail insert failed"); f.flush();
                        Assertions.assertEquals(previous, monthCard.getMonthCard(1).getLastRewardTime());
                        Assertions.assertEquals("REVIEW_REQUIRED", item.getCompletionState());
                        f.ship(); f.flush(); Assertions.assertEquals(List.of(false, false), f.replies);
                    }
                    Assertions.assertEquals(1, mailCalls.get());
                }
            }
        } finally { field.set(manager, original); }
    }

    /** 实际限时礼包离线买入必须返回完整业务结果，免支付分支不能因 null Boolean 拆箱失败。 */
    @Test void actualLimitedGiftOfflinePathCompletesPurchaseBeforeFinish() throws Exception {
        var manager = cn.game.protocol.generated.manager.LimitedTimeGiftManager.instance();
        var field = manager.getClass().getDeclaredField("limitedtimegifts"); field.setAccessible(true); Object original = field.get(manager);
        var element = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("fixture");
        element.setAttribute("ID", "1"); element.setAttribute("PurchasesNum", "2");
        field.set(manager, java.util.Map.of(1, new cn.game.protocol.generated.config.LimitedTimeGiftConfig(element)));
        try (var f = new PaymentDeliveryLifecycleTest.Fixture(false)) {
            var module = new cn.game.games.net.game.module.shop.limitedtimegift.LimitedTimeGiftModule(); module.initDefault(f.player);
            var data = new cn.game.games.net.game.module.shop.limitedtimegift.LimitedTimeGiftData(); data.setId(1);
            var gifts = module.getClass().getDeclaredField("limitedTimeGifts"); gifts.setAccessible(true); gifts.set(module, new java.util.HashMap<>(java.util.Map.of(1, data)));
            f.player.getModules().put(module.getClass().getName(), module);
            PayItem item = f.player.getPlayerModule().getPayItems(91); item.setPayType(PayType.LimitedTimeGift); item.setPayId(1);
            f.player.registerEventHandler(EventTypeEnum.Charge, event -> f.businessThread = Thread.currentThread());
            f.ship(); f.captured(); Assertions.assertEquals(1, data.getBuyCount()); Assertions.assertTrue(item.isFinish());
            Assertions.assertTrue(f.replies.isEmpty()); f.commit(); f.flush(); Assertions.assertEquals(List.of(true), f.replies);
        } finally { field.set(manager, original); }
    }

    /** 非现金支付立即拒绝仍使用各消费者原错误码，不执行奖励；奖励异常不被改写为支付拒绝。 */
    @Test void declinedImmediatePaymentKeepsOriginalConsumerError() throws Exception {
        try (var f = new PaymentDeliveryLifecycleTest.Fixture(true)) {
            for (var error : List.of(cn.game.protocol.manual.ErrorMsgEnum.shop_item_not_exist, cn.game.protocol.manual.ErrorMsgEnum.resource_not_enough)) {
                Future<Void> result = cn.game.games.net.game.module.recharge.PaymentCompletion.reward(f.player, Future.succeededFuture(false),
                        () -> { throw new AssertionError("拒绝支付不能发奖"); }, error);
                var cause = Assertions.assertInstanceOf(cn.game.core.exception.LogicException.class, result.cause());
                Assertions.assertEquals(error.getId(), cause.getErrorCode());
            }
        }
    }
}
