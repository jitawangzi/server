package cn.game.games.net.game.helper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.net.game.module.activity.ActivityBase;
import cn.game.games.net.game.module.activity.ActivityModule;
import cn.game.games.net.game.module.activity.PlayerActivityBase;
import cn.game.games.net.game.module.activity.PlayerActivityManager;
import cn.game.games.net.game.module.activity.impl.player.FirstChargeActivity;
import cn.game.protocol.generated.config.FirstChargeConfig;
import cn.game.protocol.generated.manager.FirstChargeManager;
import cn.game.util.CborSnappyUtil;
import io.vertx.core.Future;

/**
 * 同一在线 FirstCharge 回调分别执行计数替身与正式 buy，校准提交、重复和异常契约。
 * 配置为隔离合成数据，存储写入仍受控；不覆盖平台下单、领取发奖或数据库恢复。
 */
@Isolated
class FirstChargeCalibrationTest {
    /** 保存生成配置管理器的原始表；本类结束后恢复，避免污染其他用例。 */
    private static Object originalConfigs;
    /** 仅替换首充数据，不启动 XML 监视或外部服务。 */
    @BeforeAll static void metadata() throws Exception {
        PaymentDeliveryLifecycleTest.metadata();
        Field field = configField(); originalConfigs = field.get(FirstChargeManager.instance());
        field.set(FirstChargeManager.instance(), Map.of(73, config(73, 17, true),
                74, config(74, 17, false), 75, config(75, 18, false)));
    }
    /** 恢复配置与统计依赖。 */
    @AfterAll static void restore() throws Exception {
        try { configField().set(FirstChargeManager.instance(), originalConfigs); }
        finally { PaymentDeliveryLifecycleTest.restore(); }
    }
    /** @return 首充表字段；仅用于安装隔离配置，不替换业务方法 */
    private static Field configField() throws Exception {
        Field field = FirstChargeManager.class.getDeclaredField("firstcharges"); field.setAccessible(true); return field;
    }
    /** @param id 商品配置 ID @param activity 活动 ID @param paid 是否付费条目 @return 合成业务前置配置 */
    private static FirstChargeConfig config(int id, int activity, boolean paid) throws Exception {
        var element = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("FirstCharge");
        element.setAttribute("ID", String.valueOf(id)); element.setAttribute("ActivityiDIndex", String.valueOf(activity));
        element.setAttribute("ActivityiD", "1"); element.setAttribute("Price", paid ? "1;12" : "");
        element.setAttribute("Order", paid ? "1" : "2"); element.setAttribute("Item", "100001;60");
        return new FirstChargeConfig(element);
    }
    /** 正式 buy 解锁付费项及关联免费项但不发钻石；与替身共享提交与重复处理契约。 */
    @Test void realAndSubstituteWaitForCommitAndDeduplicate() throws Exception {
        Assertions.assertEquals(observe(false, "commit"), observe(true, "commit"));
    }
    /** 首充配置缺失的同步异常必须与失败替身一样拒绝成功，重复请求不重执行业务。 */
    @Test void realConfigurationErrorMatchesFailureSubstitute() throws Exception {
        Assertions.assertEquals(observe(false, "business-error"), observe(true, "business-error"));
    }
    /** 业务执行成功而保存未写入时，两种依赖均不得发布成功凭证。 */
    @Test void realAndSubstituteRejectUncommittedSave() throws Exception {
        Assertions.assertEquals(observe(false, "save-error"), observe(true, "save-error"));
    }
    /** @param real 是否调用正式首充买入 @param mode 提交或故障点 @return 两种实现可比较的外部结果 */
    private static Outcome observe(boolean real, String mode) throws Exception {
        try (var f = new PaymentDeliveryLifecycleTest.Fixture(true)) {
            FirstChargeActivity activity = attach(f);
            AtomicInteger calls = new AtomicInteger();
            f.player.getCurrencyModule().setCount(100001, 100);
            f.reward = () -> {
                calls.incrementAndGet();
                if (real) { activity.buy(mode.equals("business-error") ? 999 : 73); }
                else if (mode.equals("business-error")) { throw new NullPointerException("首充配置缺失替身"); }
                else { f.award(); }
                return Future.succeededFuture();
            };
            f.ship();
            if (mode.equals("business-error")) {
                f.flush(); Assertions.assertFalse(f.captured.future().isComplete());
                Assertions.assertTrue(activity.getChargeMap().isEmpty());
            } else {
                f.captured();
                Assertions.assertTrue(f.replies.isEmpty()); Assertions.assertFalse(f.purchase.isComplete());
                Assertions.assertFalse(f.player.getPlayerModule().getPayItems(91).hasCommittedCompletionProof(f.id(), "sdk-91"));
                if (real) {
                    Assertions.assertEquals(Set.of(73, 74), activity.getChargeMap().keySet());
                    Map<String, BasePlayerModule> snapshot = CborSnappyUtil.decompress(f.writtenSnapshot.getModules());
                    ActivityModule restored = (ActivityModule) snapshot.get(ActivityModule.class.getName());
                    FirstChargeActivity saved = restored.get(17);
                    Assertions.assertNotSame(activity, saved);
                    Assertions.assertEquals(Set.of(73, 74), saved.getChargeMap().keySet(), "正式快照必须包含解锁资格，不把内存状态当成持久化证据");
                    Assertions.assertEquals(0, f.awards.get(), "正式实现不得调用计数替身");
                } else { Assertions.assertEquals(1, f.awards.get()); }
                f.ship();
                if (mode.equals("commit")) { f.commit(); }
                else { f.write.complete(new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.NOT_WRITTEN,
                        new IllegalStateException("隔离写入器拒绝"))); }
                f.flush();
            }
            f.ship(); f.flush();
            boolean committed = mode.equals("commit");
            Assertions.assertEquals(committed ? List.of(true, true, true) : mode.equals("business-error") ? List.of(false, false) : List.of(false, false, false), f.replies);
            Assertions.assertEquals(1, calls.get());
            Assertions.assertEquals(mode.equals("business-error") ? 0 : 1, f.charges.get());
            Assertions.assertEquals(100, f.player.getCurrencyModule().getCount(100001), "buy 只解锁资格，领取发奖不属于本回调");
            boolean proof = f.player.getPlayerModule().getPayItems(91).hasCommittedCompletionProof(f.id(), "sdk-91");
            Assertions.assertEquals(committed, proof);
            Assertions.assertTrue(f.purchase.isComplete(), "失败也必须终结购买承诺，不能只检查未成功");
            Assertions.assertEquals(committed, f.purchase.succeeded());
            Assertions.assertEquals(!committed, f.purchase.failed());
            return new Outcome(List.copyOf(f.replies), calls.get(), f.charges.get(), proof);
        }
    }
    /** @param f 本例玩家与保存装配 @return 正式活动，字段注入只设置前置状态，buy 未被重写 */
    private static FirstChargeActivity attach(PaymentDeliveryLifecycleTest.Fixture f) throws Exception {
        FirstChargeActivity activity = new FirstChargeActivity();
        Field owner = PlayerActivityBase.class.getDeclaredField("player"); owner.setAccessible(true); owner.set(activity, f.player);
        Field id = ActivityBase.class.getDeclaredField("id"); id.setAccessible(true); id.set(activity, 17);
        PlayerActivityManager manager = new PlayerActivityManager(f.player); manager.setActivities(Map.of(17, activity));
        ActivityModule module = new ActivityModule();
        Field field = ActivityModule.class.getDeclaredField("playerActivityManager"); field.setAccessible(true); field.set(module, manager);
        f.player.getModules().put(ActivityModule.class.getName(), module);
        return activity;
    }
    /** 只比较两种依赖都承诺的调用、事件、响应和凭证，不将计数替身等同于真实活动状态。 */
    private record Outcome(List<Boolean> replies, int calls, int charges, boolean proof) {}
}
