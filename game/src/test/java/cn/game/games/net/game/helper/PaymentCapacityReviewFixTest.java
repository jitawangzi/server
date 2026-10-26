package cn.game.games.net.game.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.clazz.ClassManager;
import cn.game.testsupport.PaymentCapacityProbeModule;
import cn.game.games.net.game.module.player.PlayerModule;
import cn.game.games.net.game.module.recharge.PayItem;
import io.vertx.core.Promise;

/** 使用闩锁和单调时钟验证观测故障域，不连接数据库或支付平台。 */
class PaymentCapacityReviewFixTest {
    /** 加载真实封存所需模块元数据。 */
    @BeforeAll static void metadata() { ClassManager.getInstance().init(); }

    /** 等前一用例的共享后台任务退出，避免把允许丢弃的观测当作可靠队列。 */
    @BeforeEach void drainPreviousObservation() throws Exception { awaitIdle(); }

    /** 慢观察者仍持有闩锁时，多代次和另一玩家均已完成；到期动作不能改写已提交结果。 */
    @Test void blockedObserverCannotDelayReceiptsOrFollowingSaves() throws Exception {
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        var writes = new ArrayList<Promise<PlayerSaveCoordinator.WriteResult>>();
        var deadlines = new ArrayList<Runnable>();
        var notifications = new CopyOnWriteArrayList<Thread>();
        var saves = new PlayerSaveCoordinator(16, 30_000, (data, insert) -> {
            var write = Promise.<PlayerSaveCoordinator.WriteResult>promise(); writes.add(write); return write.future();
        }, (action, delay) -> { deadlines.add(action); return () -> {}; }, () -> 100,
                PlayerSaveCoordinator.RiskPolicy.defaults(), sample -> {
                    calls.incrementAndGet(); entered.countDown();
                    Assertions.assertDoesNotThrow(() -> { Assertions.assertTrue(release.await(5, TimeUnit.SECONDS)); });
                });
        var first = snapshot(41); var other = snapshot(42);
        Object owner = new Object(), otherOwner = new Object();
        var a = saves.submit(owner, first, false).result();
        var b = saves.submit(owner, snapshot(41), false).result();
        var c = saves.submit(otherOwner, other, false).result();
        a.onComplete(done -> notifications.add(Thread.currentThread()));
        b.onComplete(done -> notifications.add(Thread.currentThread()));
        c.onComplete(done -> notifications.add(Thread.currentThread()));
        try {
            writes.get(0).complete(committed());
            Assertions.assertTrue(entered.await(5, TimeUnit.SECONDS));
            Assertions.assertEquals(41, a.toCompletionStage().toCompletableFuture().get(1, TimeUnit.SECONDS).playerId());
            // 后继先于通知启动，第三个写入对应同玩家的第二代。
            writes.get(1).complete(committed()); writes.get(2).complete(committed());
            Assertions.assertEquals(41, b.toCompletionStage().toCompletableFuture().get(1, TimeUnit.SECONDS).playerId());
            Assertions.assertEquals(42, c.toCompletionStage().toCompletableFuture().get(1, TimeUnit.SECONDS).playerId());
            for (int i = 0; i < 32; i++) {
                var next = saves.submit(owner, snapshot(41), false).result();
                next.onComplete(done -> notifications.add(Thread.currentThread()));
                writes.getLast().complete(committed());
                next.toCompletionStage().toCompletableFuture().get(1, TimeUnit.SECONDS);
            }
            // 回执可早于 publish 调度返回；先等待全部通知线程退出，确定所有投递尝试均已完成再释放观察者。
            await(() -> notifications.size() == 35);
            for (Thread thread : notifications) { thread.join(5_000); Assertions.assertFalse(thread.isAlive()); }
            for (Runnable deadline : deadlines) { deadline.run(); }
            Assertions.assertTrue(a.succeeded()); Assertions.assertTrue(b.succeeded()); Assertions.assertTrue(c.succeeded());
            Assertions.assertEquals(1, calls.get()); Assertions.assertEquals(1, release.getCount());
            Assertions.assertEquals("IDLE", saves.status(41).state());
            Assertions.assertFalse(saves.status(41).observedDirty()); Assertions.assertFalse(saves.status(41).writeProtected());
            Assertions.assertNotNull(saves.status(41).confirmedModulesUtf8Bytes());
            var executor = executor("publisher");
            Assertions.assertEquals(1, executor.getLargestPoolSize());
            Assertions.assertEquals(1, executor.getQueue().size());
        } finally { release.countDown(); awaitIdle(); }
        Assertions.assertEquals(2, calls.get());
    }

    /** 生产默认告警出口真实阻塞日志 appender 时，回执仍完成；释放后恢复观测。 */
    @Test void productionLoggingIsBehindTheSameBoundedReceiptBoundary() throws Exception {
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        Logger logger = (Logger) LogManager.getLogger(PaymentCapacityMonitor.class);
        Level previous = logger.getLevel();
        AbstractAppender appender = new AbstractAppender("capacity-blocking-test", null,
                PatternLayout.createDefaultLayout(), false, Property.EMPTY_ARRAY) {
            @Override public void append(LogEvent event) {
                if (event.getMessage().getFormattedMessage().contains("COMMITTED_MODULES")) {
                    entered.countDown();
                    Assertions.assertDoesNotThrow(() -> { Assertions.assertTrue(release.await(5, TimeUnit.SECONDS)); });
                }
            }
        };
        appender.start(); logger.addAppender(appender); logger.setLevel(Level.WARN);
        try {
            var player = new FixturePlayer(9_814_201);
            for (int i = 1; i <= 5_000; i++) { PayItem item = new PayItem(); item.setOrderId(i); player.module.addPayItems(item); }
            var data = PlayerDataSnapshot.capture(player);
            var write = Promise.<PlayerSaveCoordinator.WriteResult>promise();
            var saves = new PlayerSaveCoordinator(16, 30_000, (d, insert) -> write.future(),
                    (action, delay) -> () -> {}, () -> 100);
            var result = saves.submit(new Object(), data, false).result(); write.complete(committed());
            Assertions.assertTrue(entered.await(5, TimeUnit.SECONDS));
            Assertions.assertEquals(data.getPlayerId(), result.toCompletionStage().toCompletableFuture().get(1, TimeUnit.SECONDS).playerId());
            Assertions.assertEquals(1, release.getCount()); Assertions.assertFalse(saves.status(data.getPlayerId()).writeProtected());
        } finally { release.countDown(); awaitIdle(); logger.removeAppender(appender); logger.setLevel(previous); appender.stop(); }
    }

    /** 指标阻塞与观察者异常均释放在有界出口内，不阻塞回执或永久占用后续观测资源。 */
    @Test void blockedMetricsAndThrowingObserverKeepReceiptsAndReleaseResources() throws Exception {
        // 就绪状态可能只在一次轮询中成立，成功后不能重新读取并推翻已观察到的结果。
        AtomicInteger readinessChecks = new AtomicInteger();
        await(() -> readinessChecks.incrementAndGet() == 1);
        Assertions.assertEquals(1, readinessChecks.get());
        // 注入接近回绕点的单调读数，验证跨越 long 边界时仍会轮询并严格执行超时。
        AtomicLong nanos = new AtomicLong(Long.MAX_VALUE - 5);
        AtomicInteger wrapChecks = new AtomicInteger();
        await(() -> { nanos.incrementAndGet(); return wrapChecks.incrementAndGet() == 3; }, nanos::get, 10);
        Assertions.assertEquals(3, wrapChecks.get());
        AtomicLong expiredNanos = new AtomicLong(Long.MAX_VALUE - 5);
        Assertions.assertThrows(AssertionError.class,
                () -> await(() -> false, () -> expiredNanos.getAndAdd(10), 10));
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1), recovered = new CountDownLatch(1);
        var monitor = new PaymentCapacityMonitor(PaymentCapacityMonitor.Policy.defaults(), () -> 0,
                sample -> {
                    entered.countDown();
                    Assertions.assertDoesNotThrow(() -> { Assertions.assertTrue(release.await(5, TimeUnit.SECONDS)); });
                    throw new IllegalStateException("metrics failure");
                }, alert -> {});
        try {
            var write = Promise.<PlayerSaveCoordinator.WriteResult>promise();
            var saves = new PlayerSaveCoordinator(16, 30_000, (data, insert) -> write.future(),
                    (action, delay) -> () -> {}, () -> 100, PlayerSaveCoordinator.RiskPolicy.defaults(), monitor::observe);
            var result = saves.submit(new Object(), snapshot(41), false).result(); write.complete(committed());
            Assertions.assertTrue(entered.await(5, TimeUnit.SECONDS));
            Assertions.assertTrue(result.succeeded()); Assertions.assertEquals(1, release.getCount());
        } finally { release.countDown(); awaitIdle(); }
        PaymentCapacityMonitor.publish(new PaymentCapacityMonitor.Sample(41, 0, 0), sample -> { throw new AssertionError("observer failure"); });
        awaitIdle();
        PaymentCapacityMonitor.publish(new PaymentCapacityMonitor.Sample(41, 0, 0), sample -> recovered.countDown());
        Assertions.assertTrue(recovered.await(5, TimeUnit.SECONDS)); awaitIdle();
    }

    /** 墙钟快进再回归不冻结玩家、全局窗口或满表回收；只按真实经过毫秒恢复。 */
    @Test void wallClockJumpsCannotFreezeAlertWindowOrPlayerReclamation() {
        AtomicLong wall = new AtomicLong(10_000), elapsed = new AtomicLong(0);
        var alerts = new CopyOnWriteArrayList<PaymentCapacityMonitor.Alert>();
        var monitor = new PaymentCapacityMonitor(new PaymentCapacityMonitor.Policy(1, 2, 10, 20, 1_000, 1, 1),
                elapsed::get, sample -> {}, alerts::add);
        monitor.observe(new PaymentCapacityMonitor.Sample(1, 1, 0));
        wall.set(Long.MAX_VALUE / 2); monitor.observe(new PaymentCapacityMonitor.Sample(1, 2, 0));
        wall.set(-10_000); elapsed.set(999); monitor.observe(new PaymentCapacityMonitor.Sample(2, 2, 0));
        Assertions.assertEquals(1, alerts.size());
        elapsed.set(1_000); monitor.observe(new PaymentCapacityMonitor.Sample(2, 2, 0));
        Assertions.assertEquals(2, alerts.size()); Assertions.assertEquals(2, alerts.getLast().playerId());
        elapsed.set(2_000); monitor.observe(new PaymentCapacityMonitor.Sample(2, 2, 0));
        Assertions.assertEquals(3, alerts.size()); Assertions.assertEquals(1, monitor.trackedPlayers());
        Assertions.assertTrue(PaymentCapacityMonitor.elapsedMillis() >= 0);
    }

    /** 故障日志使用相同单调契约，并发只预留一次；日志自身抛错不能逃逸或重入。 */
    @Test void failureRateLimitUsesElapsedTimeAndContainsLoggerExceptions() throws Exception {
        AtomicLong wall = new AtomicLong(10_000), elapsed = new AtomicLong(0);
        AtomicInteger calls = new AtomicInteger();
        var reporter = new PaymentCapacityMonitor.FailureReporter(elapsed::get, failure -> {
            calls.incrementAndGet(); throw new AssertionError("logger failure");
        });
        Assertions.assertDoesNotThrow(() -> reporter.report(new IllegalStateException("probe")));
        wall.set(Long.MAX_VALUE / 2); reporter.report(new IllegalStateException("probe"));
        wall.set(-10_000); elapsed.set(299_999); reporter.report(new IllegalStateException("probe"));
        Assertions.assertEquals(1, calls.get());
        elapsed.set(300_000);
        CountDownLatch start = new CountDownLatch(1);
        var threads = new ArrayList<Thread>();
        for (int i = 0; i < 64; i++) {
            threads.add(Thread.startVirtualThread(() -> {
                Assertions.assertDoesNotThrow(() -> { start.await(); reporter.report(new IllegalStateException("probe")); });
            }));
        }
        start.countDown();
        for (Thread thread : threads) { thread.join(5_000); Assertions.assertFalse(thread.isAlive()); }
        Assertions.assertEquals(2, calls.get());
        elapsed.set(600_000); reporter.report(new IllegalStateException("probe"));
        Assertions.assertEquals(3, calls.get());
    }

    /** 真实探针失败且故障日志 appender 阻塞/抛错，封存及后续保存不受影响。 */
    @Test void probeFailureLoggingCannotBlockSnapshotOrSave() throws Exception {
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        Logger logger = (Logger) LogManager.getLogger(PaymentCapacityMonitor.class);
        Level previous = logger.getLevel();
        var field = PaymentCapacityMonitor.class.getDeclaredField("failures"); field.setAccessible(true);
        var reporter = field.get(null);
        var lastField = reporter.getClass().getDeclaredField("last"); lastField.setAccessible(true);
        AtomicLong last = (AtomicLong) lastField.get(reporter);
        long oldLast = last.getAndSet(Long.MIN_VALUE);
        AbstractAppender appender = new AbstractAppender("capacity-failing-log-test", null,
                PatternLayout.createDefaultLayout(), false, Property.EMPTY_ARRAY) {
            @Override public void append(LogEvent event) {
                entered.countDown();
                Assertions.assertDoesNotThrow(() -> { Assertions.assertTrue(release.await(5, TimeUnit.SECONDS)); });
                throw new IllegalStateException("appender failure");
            }
        };
        appender.start(); logger.addAppender(appender); logger.setLevel(Level.WARN);
        try {
            Player player = new FixturePlayer(43);
            PaymentCapacityProbeModule module = new PaymentCapacityProbeModule(); module.failCount = true;
            player.getModules().put(PlayerModule.class.getName(), module);
            // 旧实现直接调用日志，会卡在封存阶段；在受控线程封存以便 finally 必定释放闩锁。
            var capture = java.util.concurrent.CompletableFuture.supplyAsync(() -> PlayerDataSnapshot.capture(player));
            Assertions.assertTrue(entered.await(5, TimeUnit.SECONDS));
            PlayerData data = capture.get(1, TimeUnit.SECONDS);
            Assertions.assertNull(PlayerDataSnapshot.capacity(data)); Assertions.assertNotNull(data.getModules());
            var write = Promise.<PlayerSaveCoordinator.WriteResult>promise();
            var saves = new PlayerSaveCoordinator(16, 30_000, (d, insert) -> write.future(),
                    (action, delay) -> () -> {}, () -> 100);
            var result = saves.submit(player, data, false).result(); write.complete(committed());
            Assertions.assertEquals(43, result.toCompletionStage().toCompletableFuture().get(1, TimeUnit.SECONDS).playerId());
            Assertions.assertEquals(1, release.getCount());
        } finally {
            release.countDown(); awaitIdle(); logger.removeAppender(appender); logger.setLevel(previous); appender.stop(); last.set(oldLast);
        }
    }

    /** @param id 玩家主键 @return 真实封存且带容量标量的独占参数 */
    private static PlayerData snapshot(long id) {
        return PlayerDataSnapshot.capture(new FixturePlayer(id));
    }
    /** @return 受控写入器的提交确认 */
    private static PlayerSaveCoordinator.WriteResult committed() {
        return new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.COMMITTED, null);
    }
    /** @param fieldName 生产共享执行器字段 @return 实际执行器，仅用于检查资源上界 */
    private static java.util.concurrent.ThreadPoolExecutor executor(String fieldName) throws ReflectiveOperationException {
        var field = PaymentCapacityMonitor.class.getDeclaredField(fieldName); field.setAccessible(true);
        var publisher = field.get(null);
        var executor = publisher.getClass().getDeclaredField("executor"); executor.setAccessible(true);
        return (java.util.concurrent.ThreadPoolExecutor) executor.get(publisher);
    }
    /** 等待共享后台任务退出，不修改进程资源或其他测试的线程。 */
    private static void awaitIdle() throws Exception {
        await(() -> {
            try {
                var observations = executor("publisher"); var failures = executor("failurePublisher");
                return observations.getActiveCount() == 0 && observations.getQueue().isEmpty()
                        && failures.getActiveCount() == 0 && failures.getQueue().isEmpty();
            } catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
        });
    }
    /** @param ready 确定性完成条件；超时后失败，不能跳过断言 */
    private static void await(BooleanSupplier ready) throws Exception {
        await(ready, System::nanoTime, TimeUnit.SECONDS.toNanos(5));
    }
    /** @param ready 完成条件，首次成功后不再读取 @param nanoClock 单调纳秒时钟 @param timeoutNanos 超时纳秒，必须为正且小于半个计数周期 */
    private static void await(BooleanSupplier ready, LongSupplier nanoClock, long timeoutNanos) throws Exception {
        long start = nanoClock.getAsLong();
        // 时差允许 nanoTime 回绕；成功后直接退出，避免异步状态变化把首次成功反转为失败。
        while (!ready.getAsBoolean()) {
            Assertions.assertTrue(nanoClock.getAsLong() - start < timeoutNanos, "等待条件超时");
            Thread.sleep(1);
        }
    }
    /** 只提供封存所需对象，不调用生产加载/初始化 hook。 */
    private static final class FixturePlayer extends Player {
        /** 独立参数对象。 */
        private final PlayerData data = new PlayerData();
        /** 真实订单容器及可控探针。 */
        private final PaymentCapacityProbeModule module = new PaymentCapacityProbeModule();
        /** @param id 本地夹具主键 */
        FixturePlayer(long id) { data.setPlayerId(id); getModules().put(PlayerModule.class.getName(), module); }
        @Override public PlayerData getData() { return data; }
        @Override public long getPlayerId() { return data.getPlayerId(); }
    }

}
