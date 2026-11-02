package cn.game.games.net.game.helper;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import cn.game.core.base.ServerContext;
import cn.game.core.net.process.Processor;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.clazz.ClassManager;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.Future;

/** 从真实 Player 定时器注册和取消入口验证已排队回调，调度替身只控制触发时点。 */
@Isolated
class PlayerTimerCancellationTest {
    /** 只加载玩家类需要的本地元数据。 */
    @BeforeAll static void metadata() { ClassManager.getInstance().init(); }

    /** cancelTimer 必须让已经离开 Vertx、进入玩家邮箱的同一 token 失效。 */
    @Test void individualCancellationInvalidatesAlreadyQueuedCallback() throws Exception {
        for (boolean periodic : List.of(false, true)) {
            try (TimerFixture fixture = new TimerFixture()) {
                AtomicInteger calls = new AtomicInteger();
                long id = periodic ? fixture.player.setPeriodicTask(100, p -> calls.incrementAndGet())
                        : fixture.player.setTimerTask(100, p -> calls.incrementAndGet());
                fixture.fire(id);
                Assertions.assertEquals(1, fixture.queued.size());
                fixture.player.cancelTimer(id);
                fixture.queued.getFirst().run();
                Assertions.assertEquals(0, calls.get());
                Assertions.assertTrue(fixture.player.getTimerTask().isEmpty());
            }
        }
    }

    /** 取消全部定时器的代次检查也覆盖没有 Vertx timerId 的立即入队动作。 */
    @Test void cancelAllInvalidatesImmediateAndDelayedCallbacks() throws Exception {
        try (TimerFixture fixture = new TimerFixture()) {
            AtomicInteger calls = new AtomicInteger();
            fixture.player.setTimerTask(0, p -> calls.incrementAndGet());
            long id = fixture.player.setTimerTask(100, p -> calls.incrementAndGet());
            fixture.fire(id);
            Assertions.assertEquals(2, fixture.queued.size());
            fixture.player.cancelAllTimer();
            fixture.queued.forEach(Runnable::run);
            Assertions.assertEquals(0, calls.get());
            Assertions.assertTrue(fixture.player.getTimerTask().isEmpty());
        }
    }

    /** 真正进入保护延期队列后，单 timer 取消仍生效；未取消对照在恢复后仅执行一次。 */
    @Test void cancellationAfterDeferralSurvivesRecoveryAndUncancelledControlRunsOnce() throws Exception {
        for (boolean cancel : List.of(false, true)) {
            try (TimerFixture fixture = new TimerFixture()) {
                fixture.protect();
                AtomicInteger calls = new AtomicInteger();
                long id = fixture.player.setTimerTask(100, p -> calls.incrementAndGet());
                fixture.fire(id);
                fixture.queued.getFirst().run();
                Assertions.assertEquals(1, fixture.saves.status(fixture.player.getPlayerId()).deferredTimers());
                Assertions.assertEquals(0, calls.get());
                if (cancel) { fixture.player.cancelTimer(id); }
                fixture.recover();
                for (int i = 0; i < 3; i++) {
                    fixture.saves.sweep((playerId, action) -> action.get(), (playerId, owner) -> {
                        throw new AssertionError("恢复后的新动作尚未达到自动保存阈值");
                    });
                }
                Assertions.assertEquals(cancel ? 0 : 1, calls.get());
                Assertions.assertEquals(0, fixture.saves.status(fixture.player.getPlayerId()).deferredTimers());
            }
        }
    }

    /** 保存并恢复全局调度入口；不会初始化 Apollo、Spring 或数据库。 */
    private static final class TimerFixture implements AutoCloseable {
        /** 实际被测玩家。 */
        final Player player = new Player();
        /** 已被处理器受理的真实业务包装动作。 */
        final List<Runnable> queued = new ArrayList<>();
        /** Vertx 注册的真实回调。 */
        final List<Handler<Long>> callbacks = new ArrayList<>();
        /** 原进程 Vertx 引用。 */
        final Vertx originalVertx = VxHolder.vertx;
        /** 仅测试内替换私有处理器，不新增生产测试接口。 */
        final Field processorField = ServerContext.class.getDeclaredField("processor");
        /** 原处理器引用。 */
        final Object originalProcessor;
        /** 生产共享协调器，测试期间只替换写入器并使用独有玩家 ID。 */
        final PlayerSaveCoordinator saves = PlayerSaveCoordinator.shared();
        /** 保存实际写入器，结束时恢复。 */
        final Field writerField = PlayerSaveCoordinator.class.getDeclaredField("writer");
        /** 原写入器引用。 */
        final Object originalWriter;
        /** Apollo 已存在的门面，不调用其初始化入口。 */
        final Object configFacade;
        /** 门面的配置管理器依赖。 */
        final Field configManagerField = ConfigService.class.getDeclaredField("m_configManager");
        /** 原配置管理器，可为 null。 */
        final Object originalConfigManager;
        /** 控制首笔结果不明，后续恢复写入成功。 */
        boolean unknown;

        /** 安装可控调度替身；注册和 token 逻辑仍运行生产 Player。 */
        @SuppressWarnings("unchecked")
        TimerFixture() throws Exception {
            player.setPlayerId(java.util.UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
            Field facadeField = ConfigService.class.getDeclaredField("s_instance");
            facadeField.setAccessible(true);
            configFacade = facadeField.get(null);
            configManagerField.setAccessible(true);
            originalConfigManager = configManagerField.get(configFacade);
            Config config = (Config) Proxy.newProxyInstance(Config.class.getClassLoader(), new Class<?>[] {Config.class},
                    (proxy, method, args) -> {
                        if (method.getName().equals("getBooleanProperty") && args[0].equals("player_db_single_table")) { return true; }
                        throw new AssertionError("非预期配置读取: " + method.getName());
                    });
            configManagerField.set(configFacade, Proxy.newProxyInstance(configManagerField.getType().getClassLoader(),
                    new Class<?>[] {configManagerField.getType()}, (proxy, method, args) -> {
                        if (method.getName().equals("getConfig") && args[0].equals("application")) { return config; }
                        throw new AssertionError("非预期配置管理入口: " + method.getName());
                    }));
            writerField.setAccessible(true);
            originalWriter = writerField.get(saves);
            writerField.set(saves, (PlayerSaveCoordinator.Writer) (data, insert) -> Future.succeededFuture(
                    new PlayerSaveCoordinator.WriteResult(unknown ? PlayerSaveCoordinator.Outcome.UNKNOWN : PlayerSaveCoordinator.Outcome.COMMITTED, null)));
            processorField.setAccessible(true);
            originalProcessor = processorField.get(ServerContext.getInstance());
            processorField.set(ServerContext.getInstance(), Proxy.newProxyInstance(Processor.class.getClassLoader(),
                    new Class<?>[] {Processor.class}, (proxy, method, args) -> {
                        if (method.getName().equals("process") && args.length == 3 && args[1] instanceof Runnable task) {
                            queued.add(task); return null;
                        }
                        throw new AssertionError("非预期处理器入口: " + method.getName());
                    }));
            VxHolder.vertx = (Vertx) Proxy.newProxyInstance(Vertx.class.getClassLoader(), new Class<?>[] {Vertx.class},
                    (proxy, method, args) -> {
                        if (method.getName().equals("setTimer") || method.getName().equals("setPeriodic")) {
                            callbacks.add((Handler<Long>) args[1]); return (long) callbacks.size();
                        }
                        if (method.getName().equals("cancelTimer")) { return false; }
                        throw new AssertionError("非预期 Vertx 入口: " + method.getName());
                    });
        }

        /** @param id 已登记 timerId，模拟 Vertx 已投递但玩家邮箱尚未消费 */
        void fire(long id) { callbacks.get((int) id - 1).handle(id); }

        /** 通过受控实际终态进入 UNKNOWN，不靠篡改保护标记模拟延期。 */
        void protect() {
            saves.register(player.getPlayerId(), player, () -> {});
            unknown = true;
            saves.submit(player, data(), false);
            Assertions.assertTrue(saves.status(player.getPlayerId()).writeProtected());
        }

        /** 测试写入器没有数据库命令，显式核对后解除 UNKNOWN，不声称真实库恢复。 */
        void recover() {
            var status = saves.status(player.getPlayerId());
            saves.reconcile(status.coordinatorId(), player.getPlayerId(), status.generation(), PlayerSaveCoordinator.Resolution.COMMITTED,
                    new PlayerSaveCoordinator.RecoveryEvidence("timer-fixture", "no-database", "timer-cancel",
                            "controlled writer already returned without database IO", "controlled committed snapshot"));
            unknown = false;
        }

        /** @return 与测试玩家绑定的最小聚合参数 */
        PlayerData data() {
            PlayerData data = new PlayerData(); data.setPlayerId(player.getPlayerId()); data.setModules(null); return data;
        }

        @Override public void close() throws IllegalAccessException {
            try {
                player.cancelAllTimer();
                var status = saves.status(player.getPlayerId());
                if (status != null) {
                    if (status.state().equals("UNKNOWN")) { recover(); }
                    unknown = false;
                    saves.submit(player, data(), false);
                    Assertions.assertTrue(saves.release(player.getPlayerId(), player, () -> true));
                }
            } finally {
                VxHolder.vertx = originalVertx;
                processorField.set(ServerContext.getInstance(), originalProcessor);
                writerField.set(saves, originalWriter);
                configManagerField.set(configFacade, originalConfigManager);
            }
        }
    }
}
