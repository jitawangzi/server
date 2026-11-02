package cn.game.games.net.game.helper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.game.module.player.PlayerModule;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.testsupport.PaymentCapacityProbeModule;
import cn.game.util.JsonUtil;
import cn.game.util.CborSnappyUtil;
import cn.game.games.core.BasePlayerModule;
import io.vertx.core.Promise;

/** 真实封存与提交回调结合可控时钟，验证容量不改变保存、保留和 UNKNOWN 语义。 */
class PaymentCapacityMonitorTest {
    /** 仅加载本地模块元数据，不启动数据库或业务服务。 */
    @BeforeAll static void metadata() { ClassManager.getInstance().init(); }

    /** 使用标准编码器作为独立判据，覆盖多字节、代理对和孤立代理字符。 */
    @Test void utf8MeasurementMatchesEncoderWithoutCharacterApproximation() {
        for (String value : List.of("", "ASCII", "支付é😀", "\uD800", "\uDC00", "a\uD800中\uDC00😀")) {
            Assertions.assertEquals(value.getBytes(StandardCharsets.UTF_8).length, PaymentCapacityMonitor.utf8Bytes(value));
        }
        Assertions.assertEquals(12, PaymentCapacityMonitor.utf8Bytes("支付é😀"));
    }

    /** 默认预算及整组回退边界；配置错误不得禁用告警或输出配置正文。 */
    @Test void policyConfigurationRejectsInvalidAndUsesConservativeDefaults() {
        var defaults = PaymentCapacityMonitor.Policy.defaults();
        Assertions.assertEquals(defaults, PaymentCapacityMonitor.Policy.from(new Properties()));
        for (String[] bad : new String[][] {{"orders.warn", "0"}, {"orders.critical", "1000"},
                {"modulesBytes.critical", "1"}, {"alertIntervalMillis", "999"}, {"alertIntervalMillis", "86400001"},
                {"trackedPlayers", "65537"}, {"alertsPerInterval", "1025"}, {"orders.warn", "secret-not-a-number"}}) {
            Properties p = new Properties(); p.setProperty("payment.capacity." + bad[0], bad[1]);
            Assertions.assertEquals(defaults, PaymentCapacityMonitor.Policy.from(p));
        }
        Properties valid = new Properties(); valid.setProperty("payment.capacity.orders.warn", "2");
        valid.setProperty("payment.capacity.orders.critical", "3");
        Assertions.assertEquals(2, PaymentCapacityMonitor.Policy.from(valid).ordersWarn());
        Assertions.assertEquals(3, PaymentCapacityMonitor.Policy.from(valid).ordersCritical());
    }

    /** 数量与字节分别在相等边界触发，critical 不依赖另一个维度超预算。 */
    @Test void countAndBytesHaveIndependentInclusiveWarnAndCriticalBoundaries() {
        for (long[] sample : new long[][] {{9, 99, 0}, {10, 0, 1}, {20, 0, 1}, {21, 0, 1},
                {0, 100, 1}, {0, 200, 1}, {0, 201, 1}, {10, 200, 2}}) {
            List<PaymentCapacityMonitor.Alert> alerts = new ArrayList<>();
            var monitor = monitor(new AtomicLong(0), alerts, 8, 16);
            monitor.observe(new PaymentCapacityMonitor.Sample(1, (int) sample[0], sample[1]));
            Assertions.assertEquals(sample[2], alerts.size());
            for (var alert : alerts) {
                long critical = alert.dimension().equals("orders") ? 20 : 200;
                Assertions.assertEquals(alert.value() >= critical ? "critical" : "warn", alert.severity());
            }
        }
    }

    /** 同玩家各维度独立，窗口内升级也限频，回拨不能重新发射；精确窗口边界恢复。 */
    @Test void repeatedSamplesAndClockRollbackKeepIndependentRateLimits() {
        AtomicLong time = new AtomicLong(2_000), wall = new AtomicLong(2_000);
        List<PaymentCapacityMonitor.Alert> alerts = new ArrayList<>();
        var monitor = monitor(time, alerts, 8, 16);
        monitor.observe(new PaymentCapacityMonitor.Sample(1, 10, 0));
        monitor.observe(new PaymentCapacityMonitor.Sample(1, 20, 100));
        Assertions.assertEquals(2, alerts.size());
        wall.set(Long.MAX_VALUE / 2); monitor.observe(new PaymentCapacityMonitor.Sample(1, 20, 200));
        wall.set(-1_000); time.set(2_999); monitor.observe(new PaymentCapacityMonitor.Sample(1, 20, 200));
        Assertions.assertEquals(2, alerts.size());
        time.set(3_000); monitor.observe(new PaymentCapacityMonitor.Sample(1, 20, 200));
        Assertions.assertEquals(4, alerts.size());
    }

    /** 轮换大量玩家不驱逐尚有效的名额，表和全局输出均严格有界，到期后新玩家可观测。 */
    @Test void playerChurnAndGlobalAlertBudgetRemainBounded() {
        AtomicLong time = new AtomicLong(0);
        List<PaymentCapacityMonitor.Alert> alerts = new ArrayList<>();
        var monitor = monitor(time, alerts, 2, 16);
        for (int i = 0; i < 10_000; i++) { monitor.observe(new PaymentCapacityMonitor.Sample(i, 20, 200)); }
        Assertions.assertEquals(2, monitor.trackedPlayers()); Assertions.assertEquals(4, alerts.size());
        monitor.observe(new PaymentCapacityMonitor.Sample(0, 20, 200)); Assertions.assertEquals(4, alerts.size());
        time.set(1_000); monitor.observe(new PaymentCapacityMonitor.Sample(10_001, 20, 200));
        Assertions.assertEquals(6, alerts.size()); Assertions.assertEquals(2, monitor.trackedPlayers());
        alerts.clear(); monitor = monitor(time, alerts, 100, 3);
        for (int i = 0; i < 100; i++) { monitor.observe(new PaymentCapacityMonitor.Sample(i, 20, 200)); }
        Assertions.assertEquals(3, alerts.size());
    }

    /** 并发预留不能重复发射；出口中等待另一线程访问状态，证明告警在锁外执行。 */
    @Test void concurrentCallbacksReserveOnceAndNeverHoldLockDuringAlert() throws Exception {
        AtomicLong time = new AtomicLong(0);
        var alerts = new CopyOnWriteArrayList<PaymentCapacityMonitor.Alert>();
        var holder = new PaymentCapacityMonitor[1];
        holder[0] = new PaymentCapacityMonitor(policy(8, 16), time::get, s -> {}, a -> {
            Assertions.assertDoesNotThrow(() -> {
                var unlocked = new java.util.concurrent.CompletableFuture<Integer>();
                Thread.startVirtualThread(() -> unlocked.complete(holder[0].trackedPlayers()));
                Assertions.assertTrue(unlocked.get(2, TimeUnit.SECONDS) > 0);
                alerts.add(a);
            });
        });
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            threads.add(Thread.startVirtualThread(() -> {
                Assertions.assertDoesNotThrow(() -> { start.await(); });
                holder[0].observe(new PaymentCapacityMonitor.Sample(1, 20, 200));
            }));
        }
        start.countDown(); for (Thread thread : threads) { thread.join(5_000); Assertions.assertFalse(thread.isAlive()); }
        Assertions.assertEquals(2, alerts.size());
    }

    /** 保存边界只序列化一次并读取一次数量，回调不读在线对象，完整 modules 含非订单内容。 */
    @Test void capturedScalarsMatchActualFullSnapshotAndIgnoreLaterMutation() throws Exception {
        FixturePlayer player = new FixturePlayer();
        player.getData().setName("中😀"); player.add(1, "secret-支付😀");
        var data = PlayerDataSnapshot.capture(player);
        var sample = PlayerDataSnapshot.capacity(data);
        Assertions.assertEquals(data.getModules().length, sample.modulesUtf8Bytes());
        Assertions.assertEquals(1, sample.orders()); Assertions.assertEquals(1, player.module.countReads.get());
        byte[] raw = data.getModules();
        player.add(2, "later"); player.getData().setModules("new online value".getBytes(StandardCharsets.UTF_8));
        List<PaymentCapacityMonitor.Sample> observed = new CopyOnWriteArrayList<>();
        Promise<PlayerSaveCoordinator.WriteResult> write = Promise.promise();
        var coordinator = coordinator(write, observed::add);
        var result = coordinator.submit(player, data, false).result();
        Assertions.assertTrue(observed.isEmpty()); Assertions.assertFalse(result.isComplete());
        Assertions.assertNull(coordinator.status(41).confirmedPaymentOrders());
        Assertions.assertNull(coordinator.status(41).confirmedModulesUtf8Bytes());
        write.complete(new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.COMMITTED, null));
        result.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        awaitObservation(() -> observed.size() == 1);
        Assertions.assertEquals(List.of(sample), observed);
        Assertions.assertEquals(1, coordinator.status(41).confirmedPaymentOrders());
        Assertions.assertEquals(sample.modulesUtf8Bytes(), coordinator.status(41).confirmedModulesUtf8Bytes());
        Assertions.assertEquals(1, player.module.countReads.get()); Assertions.assertArrayEquals(raw, data.getModules());
        Assertions.assertArrayEquals("new online value".getBytes(StandardCharsets.UTF_8), player.getData().getModules());
    }

    /** 大订单集超过诊断阈值仍原样提交，不删单、不截断，告警出口不包含 SDK 原文。 */
    @Test void largeOrderSetIsCommittedWithoutTruncationOrEviction() throws Exception {
        FixturePlayer player = new FixturePlayer();
        byte[] buf = new byte[200]; var rnd = java.util.concurrent.ThreadLocalRandom.current(); var hex = java.util.HexFormat.of();
        for (int i = 1; i <= 12_000; i++) { rnd.nextBytes(buf); player.add(i, "secret-sdk-" + hex.formatHex(buf)); }
        var data = PlayerDataSnapshot.capture(player);
        var sample = PlayerDataSnapshot.capacity(data);
        Assertions.assertEquals(12_000, sample.orders()); Assertions.assertTrue(sample.modulesUtf8Bytes() > 4_194_304);
        List<PaymentCapacityMonitor.Alert> alerts = new CopyOnWriteArrayList<>();
        var monitor = new PaymentCapacityMonitor(PaymentCapacityMonitor.Policy.defaults(), () -> 0, s -> {}, alerts::add);
        Promise<PlayerSaveCoordinator.WriteResult> write = Promise.promise();
        var result = coordinator(write, monitor::observe).submit(player, data, false).result();
        write.complete(new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.COMMITTED, null));
        result.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        awaitObservation(() -> alerts.size() == 2);
        Assertions.assertEquals(2, alerts.size()); Assertions.assertFalse(alerts.toString().contains("secret-sdk"));
        var restored = (PlayerModule) CborSnappyUtil.<java.util.Map<String, BasePlayerModule>>decompress(data.getModules()).get(PlayerModule.class.getName());
        Assertions.assertEquals(12_000, restored.retainedPaymentOrderCount()); Assertions.assertNotNull(restored.getPayItems(12_000));
    }

    /** 真实 NOT_WRITTEN、UNKNOWN 与失败 Future 都不发布已提交容量，监控不得解除写保护。 */
    @Test void failedAndUnknownSavesNeverPublishCommittedCapacityOrLoseProtection() throws Exception {
        for (int outcome = 0; outcome < 3; outcome++) {
            FixturePlayer player = new FixturePlayer(); player.add(1, "secret");
            var observed = new CopyOnWriteArrayList<PaymentCapacityMonitor.Sample>();
            Promise<PlayerSaveCoordinator.WriteResult> write = Promise.promise();
            var coordinator = coordinator(write, observed::add);
            var result = coordinator.submit(player, PlayerDataSnapshot.capture(player), false).result();
            if (outcome == 2) { write.fail(new IllegalStateException("unknown")); }
            else { write.complete(new PlayerSaveCoordinator.WriteResult(outcome == 0 ? PlayerSaveCoordinator.Outcome.NOT_WRITTEN
                    : PlayerSaveCoordinator.Outcome.UNKNOWN, new IllegalStateException("not confirmed"))); }
            Assertions.assertThrows(java.util.concurrent.ExecutionException.class,
                    () -> result.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS));
            Assertions.assertTrue(observed.isEmpty()); Assertions.assertEquals(0, coordinator.status(41).lastConfirmedAt());
            Assertions.assertNull(coordinator.status(41).confirmedModulesUtf8Bytes());
            Assertions.assertTrue(coordinator.status(41).observedDirty());
            if (outcome != 0) {
                Assertions.assertTrue(coordinator.status(41).writeProtected());
                Assertions.assertEquals("UNKNOWN_BLOCKED", ((PlayerSaveCoordinator.SaveFailure) coordinator.submit(player,
                        PlayerDataSnapshot.capture(player), false).result().cause()).code);
            }
        }
    }

    /** 指标、告警及整个观察者抛异常，已确认保存仍成功；某一出口失败不阻断另一出口。 */
    @Test void throwingMetricsAlertsAndObserverCannotChangeSaveReceipt() throws Exception {
        AtomicInteger metricCalls = new AtomicInteger(), alertCalls = new AtomicInteger();
        var monitor = new PaymentCapacityMonitor(policy(8, 16), () -> 0,
                s -> { metricCalls.incrementAndGet(); throw new IllegalStateException("secret"); },
                a -> { alertCalls.incrementAndGet(); throw new IllegalStateException("secret"); });
        monitor.observe(new PaymentCapacityMonitor.Sample(41, 20, 200));
        monitor.observe(new PaymentCapacityMonitor.Sample(41, 20, 200));
        Assertions.assertEquals(2, metricCalls.get()); Assertions.assertEquals(2, alertCalls.get());
        FixturePlayer player = new FixturePlayer(); player.add(1, "secret");
        Promise<PlayerSaveCoordinator.WriteResult> write = Promise.promise();
        var coordinator = coordinator(write, s -> { throw new AssertionError("observer failure"); });
        var result = coordinator.submit(player, PlayerDataSnapshot.capture(player), false).result();
        write.complete(new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.COMMITTED, null));
        Assertions.assertEquals(41, result.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS).playerId());
        Assertions.assertFalse(coordinator.status(41).writeProtected()); Assertions.assertFalse(coordinator.status(41).observedDirty());
        Assertions.assertNotNull(coordinator.status(41).confirmedModulesUtf8Bytes());
    }

    /** 数量探针失败也不改变原封存和保存，监控缺失不能伪造零容量。 */
    @Test void captureProbeFailureDoesNotPreventSnapshotOrCommit() throws Exception {
        FixturePlayer player = new FixturePlayer();
        player.module.failCount = true;
        var data = PlayerDataSnapshot.capture(player);
        Assertions.assertNotNull(data.getModules()); Assertions.assertNull(PlayerDataSnapshot.capacity(data));
        Promise<PlayerSaveCoordinator.WriteResult> write = Promise.promise();
        var result = coordinator(write, s -> Assertions.fail("缺失探针不得伪造样本")).submit(player, data, false).result();
        write.complete(new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.COMMITTED, null));
        Assertions.assertEquals(41, result.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS).playerId());
    }

    /** 实际 Micrometer 出口对大量玩家只建立两个固定指标，不把 ID 或支付字段放入标签。 */
    @Test void productionMetricsKeepFixedCardinalityAcrossPlayers() {
        var registry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        try {
            for (int i = 1; i <= 10_000; i++) { PaymentCapacityMonitor.metrics(registry, new PaymentCapacityMonitor.Sample(i, 2, 12)); }
            Assertions.assertEquals(2, registry.getMeters().size());
            for (var meter : registry.getMeters()) { Assertions.assertTrue(meter.getId().getTags().isEmpty()); }
            Assertions.assertEquals(10_000, registry.get("payment.capacity.committed.orders").summary().count());
            Assertions.assertEquals(20_000, registry.get("payment.capacity.committed.orders").summary().totalAmount());
            Assertions.assertEquals(120_000, registry.get("payment.capacity.committed.modules.bytes").summary().totalAmount());
        } finally { registry.close(); }
    }

    /** 等待超时不产生已提交样本；真实晚提交才更新容量，原等待失败不被改为成功。 */
    @Test void timeoutDoesNotInventCommitAndLateCommitKeepsOriginalFailure() throws Exception {
        FixturePlayer player = new FixturePlayer(); player.add(1, "secret");
        Promise<PlayerSaveCoordinator.WriteResult> write = Promise.promise();
        List<Runnable> deadlines = new ArrayList<>();
        var observed = new CopyOnWriteArrayList<PaymentCapacityMonitor.Sample>();
        CountDownLatch published = new CountDownLatch(1);
        var saves = new PlayerSaveCoordinator(16, 30_000, (data, insert) -> write.future(),
                (action, delay) -> { deadlines.add(action); return () -> {}; }, () -> 100,
                PlayerSaveCoordinator.RiskPolicy.defaults(), s -> { observed.add(s); published.countDown(); });
        var result = saves.submit(player, PlayerDataSnapshot.capture(player), false).result();
        deadlines.getFirst().run();
        Assertions.assertThrows(java.util.concurrent.ExecutionException.class,
                () -> result.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS));
        Throwable original = result.cause();
        Assertions.assertEquals("WAIT_TIMEOUT", ((PlayerSaveCoordinator.SaveFailure) original).code);
        Assertions.assertTrue(observed.isEmpty()); Assertions.assertNull(saves.status(41).confirmedModulesUtf8Bytes());
        write.complete(new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.COMMITTED, null));
        Assertions.assertTrue(published.await(5, TimeUnit.SECONDS));
        Assertions.assertEquals(1, observed.size()); Assertions.assertNotNull(saves.status(41).confirmedModulesUtf8Bytes());
        Assertions.assertSame(original, result.cause()); Assertions.assertFalse(saves.status(41).writeProtected());
    }

    /** @param ready 异步观测完成条件；使用真实经过时间限定测试等待 */
    private static void awaitObservation(java.util.function.BooleanSupplier ready) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!ready.getAsBoolean() && System.nanoTime() < deadline) { Thread.sleep(1); }
        Assertions.assertTrue(ready.getAsBoolean());
    }

    /** @param players 状态上限 @param alerts 输出上限 @return 小型确定性预算 */
    private static PaymentCapacityMonitor.Policy policy(int players, int alerts) {
        return new PaymentCapacityMonitor.Policy(10, 20, 100, 200, 1_000, players, alerts);
    }
    /** @param time 可控时钟 @param alerts 输出容器 @param players 状态上限 @param limit 输出上限 @return 监控器 */
    private static PaymentCapacityMonitor monitor(AtomicLong time, List<PaymentCapacityMonitor.Alert> alerts, int players, int limit) {
        return new PaymentCapacityMonitor(policy(players, limit), time::get, s -> {}, alerts::add);
    }
    /** @param write 可控实际事务 @param observer 标量观察者 @return 真实保存状态机 */
    private static PlayerSaveCoordinator coordinator(Promise<PlayerSaveCoordinator.WriteResult> write,
            java.util.function.Consumer<PaymentCapacityMonitor.Sample> observer) {
        return new PlayerSaveCoordinator(16, 30_000, (data, insert) -> write.future(), (action, delay) -> () -> {},
                () -> 100, PlayerSaveCoordinator.RiskPolicy.defaults(), observer);
    }
    /** 本地玩家夹具，未启动业务和数据库。 */
    private static final class FixturePlayer extends Player {
        /** 真实聚合字段。 */
        private final PlayerData data = new PlayerData();
        /** 使用真实订单集合，仅在测试子类增加读取计数。 */
        private final PaymentCapacityProbeModule module = new PaymentCapacityProbeModule();
        /** 在模块表使用正式模块 key。 */
        FixturePlayer() { data.setPlayerId(41); getModules().put(PlayerModule.class.getName(), module); }
        @Override public PlayerData getData() { return data; }
        @Override public long getPlayerId() { return 41; }
        /** @param id 订单 ID @param sdk 私有哨兵正文 */
        void add(long id, String sdk) { PayItem item = new PayItem(); item.setOrderId(id); item.setSdkOrderId(sdk); module.addPayItems(item); }
    }
}
