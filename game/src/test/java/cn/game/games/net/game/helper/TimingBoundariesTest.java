package cn.game.games.net.game.helper;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import cn.game.core.execute.TimingMetrics;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.net.game.module.player.PlayerModule;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** 复用现有故障夹具核对实际边界计数，独立Vert.x只用于本方法工作线程路径。 */
class TimingBoundariesTest {
    /** 恢复默认关闭，不污染其他回归。 */
    @AfterEach void reset() { TimingMetrics.reset(false); }
    /** 连接、实际SQL及commit分别采集，异常仍使原连接废弃。 */
    @Test void connectionSqlAndFailureKeepOriginalDiscardSemantics() throws Exception {
        TimingMetrics metrics = TimingMetrics.reset(true);
        PaymentDatabaseBoundaryTest.Fake source = new PaymentDatabaseBoundaryTest.Fake();
        try (var connection = PaymentDatabaseBoundary.open(source, new PaymentDatabaseBoundary.Limits(1, 1000, 1000, 2000))) {
            try (var statement = connection.prepareStatement("SELECT 1")) { statement.execute(); }
            connection.setAutoCommit(false); connection.commit();
        }
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.CONNECTION, TimingMetrics.Outcome.SUCCESS));
        Assertions.assertEquals(2, count(metrics, TimingMetrics.Stage.SQL, TimingMetrics.Outcome.SUCCESS));
        source.failExecute = true;
        try (var connection = PaymentDatabaseBoundary.open(source, new PaymentDatabaseBoundary.Limits(1, 1000, 1000, 2000));
                var statement = connection.prepareStatement("SELECT 1")) {
            Assertions.assertThrows(SQLException.class, statement::execute);
        }
        Assertions.assertEquals(1, source.discards);
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.SQL, TimingMetrics.Outcome.FAILURE));
    }
    /** 获取失败无SQL样本，保持原异常身份与未连接事实。 */
    @Test void acquisitionFailureIsNotSqlWork() {
        TimingMetrics metrics = TimingMetrics.reset(true);
        IllegalStateException expected = new IllegalStateException("synthetic acquisition");
        var source = new FailingSource(expected);
        Assertions.assertSame(expected, Assertions.assertThrows(IllegalStateException.class,
                () -> PaymentDatabaseBoundary.open(source, new PaymentDatabaseBoundary.Limits(1, 1000, 1000, 2000))));
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.CONNECTION, TimingMetrics.Outcome.FAILURE));
        Assertions.assertEquals(0, count(metrics, TimingMetrics.Stage.SQL, TimingMetrics.Outcome.SUCCESS));
    }
    /** 序列化走真实模块JSON，原保存参数隔离仍成立，不把模块内容写入指标。 */
    @Test void snapshotSerializationObservesRealJsonWithoutPlayerLabels() {
        cn.game.games.core.clazz.ClassManager.getInstance().init();
        TimingMetrics metrics = TimingMetrics.reset(true);
        PlayerData data = new PlayerData(); data.setPlayerId(17);
        Player player = new Player() { @Override public PlayerData getData() { return data; } };
        player.getModules().put(PlayerModule.class.getName(), new PlayerModule());
        var snapshot = PlayerDataSnapshot.capture(player);
        Assertions.assertNotSame(data, snapshot);
        Assertions.assertNotEquals("{}", snapshot.getModules());
        Assertions.assertEquals("{}", data.getModules());
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.SNAPSHOT, TimingMetrics.Outcome.SUCCESS));
    }
    /** 真实worker执行失败保留原原因，观测只附着原Future，不使用共享集群。 */
    @Test void databaseWorkerCountsSuccessAndFailureWithoutReplacingCause() throws Exception {
        TimingMetrics metrics = TimingMetrics.reset(true);
        Vertx previous = VxHolder.vertx;
        Vertx owned = Vertx.vertx(); VxHolder.vertx = owned;
        try {
            Assertions.assertEquals(7, VxHolder.executeBlockingWithTimeout(() -> 7).toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS));
            IllegalStateException expected = new IllegalStateException("synthetic work");
            var failed = VxHolder.executeBlockingWithTimeout(() -> { throw expected; });
            Assertions.assertThrows(Exception.class, () -> failed.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS));
            Assertions.assertSame(expected, failed.cause());
            Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.DB_WORKER, TimingMetrics.Outcome.SUCCESS));
            Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.DB_WORKER, TimingMetrics.Outcome.FAILURE));
            Assertions.assertEquals(2, count(metrics, TimingMetrics.Stage.DB_WORKER_QUEUE, TimingMetrics.Outcome.SUCCESS));
        } finally { VxHolder.vertx = previous; owned.close().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS); }
    }
    /** 等待超时不停止实际worker，放行真实工作后迟到成功单独记一次。 */
    @Test void databaseTimeoutLeavesActualWorkerOwnedUntilLateCompletion() throws Exception {
        TimingMetrics metrics = TimingMetrics.reset(true);
        Vertx previous = VxHolder.vertx;
        Vertx owned = Vertx.vertx(); VxHolder.vertx = owned;
        var entered = new java.util.concurrent.CountDownLatch(1);
        var release = new java.util.concurrent.CountDownLatch(1);
        try {
            var waited = VxHolder.executeBlockingWithTimeout(() -> { entered.countDown(); Assertions.assertTrue(release.await(5, TimeUnit.SECONDS)); return 7; }, 10, false);
            Assertions.assertTrue(entered.await(5, TimeUnit.SECONDS));
            Assertions.assertThrows(Exception.class, () -> waited.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS));
            Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.DB_WAIT, TimingMetrics.Outcome.TIMEOUT));
            Assertions.assertEquals(0, count(metrics, TimingMetrics.Stage.DB_WORKER, TimingMetrics.Outcome.SUCCESS));
        } finally {
            release.countDown(); VxHolder.vertx = previous;
            owned.close().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        }
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.DB_WORKER, TimingMetrics.Outcome.SUCCESS));
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.DB_WAIT, TimingMetrics.Outcome.LATE_SUCCESS));
    }
    /** 同步提交失败不改变原异常，也不能遗留一个看似正在执行的观测。 */
    @Test void synchronousWorkerSubmissionFailureIsRecordedAsRejected() {
        TimingMetrics metrics = TimingMetrics.reset(true);
        Vertx previous = VxHolder.vertx; VxHolder.vertx = null;
        try {
            Assertions.assertThrows(NullPointerException.class, () -> VxHolder.executeBlockingWithTimeout(() -> 7));
            Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.DB_WAIT, TimingMetrics.Outcome.REJECTED));
            Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.DB_WORKER_QUEUE, TimingMetrics.Outcome.REJECTED));
            Assertions.assertEquals(0, count(metrics, TimingMetrics.Stage.DB_WORKER, TimingMetrics.Outcome.SUCCESS));
        } finally { VxHolder.vertx = previous; }
    }
    /** @param metrics 窗口 @param stage 阶段 @param outcome 结果 @return 已记录精确计数 */
    private static long count(TimingMetrics metrics, TimingMetrics.Stage stage, TimingMetrics.Outcome outcome) {
        return metrics.snapshot().rows().stream().filter(r -> r.stage() == stage && r.outcome() == outcome).mapToLong(TimingMetrics.Row::count).sum();
    }
    /** 声明有限获取能力，但获取时抛出指定故障，未发出任何连接。 */
    private static final class FailingSource extends org.springframework.jdbc.datasource.AbstractDataSource implements PaymentDatabaseBoundary.Source {
        /** 本用例指定原异常。 */
        private final RuntimeException failure;
        /** @param failure 获取故障 */
        FailingSource(RuntimeException failure) { this.failure = failure; }
        @Override public int acquisitionMillis() { return 1; }
        @Override public void discard(java.sql.Connection connection) { throw new AssertionError("no connection issued"); }
        @Override public java.sql.Connection getConnection() { throw failure; }
        @Override public java.sql.Connection getConnection(String user, String password) { throw failure; }
    }
}
