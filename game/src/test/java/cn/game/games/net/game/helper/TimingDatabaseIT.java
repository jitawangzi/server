package cn.game.games.net.game.helper;

import java.util.concurrent.TimeUnit;
import cn.game.core.execute.TimingMetrics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** 复用独有主键和冷清理夹具，验证真实保存事务上的计时，不改变任何生产开关或表。 */
class TimingDatabaseIT {
    /** 真实JDBC获取/语句/提交、实际模块序列化与冷重载均须发生，成功后精确清理并冷查。 */
    @Test void realOwnedSaveReportsConnectionSqlTransactionAndSerialization() throws Exception {
        PlayerSaveDatabaseIT.metadata();
        try (var fixture = new PlayerSaveDatabaseIT.DatabaseFixture(false)) {
            fixture.seed();
            TimingMetrics metrics = TimingMetrics.reset(true);
            try {
                var player = fixture.player();
                var snapshot = PlayerDataSnapshot.capture(player);
                var result = fixture.writer.write(snapshot, false).toCompletionStage().toCompletableFuture().get(20, TimeUnit.SECONDS);
                Assertions.assertEquals(PlayerSaveCoordinator.Outcome.COMMITTED, result.outcome());
                Assertions.assertArrayEquals(snapshot.getModules(), fixture.read().getModules());
                for (var stage : new TimingMetrics.Stage[]{TimingMetrics.Stage.CONNECTION, TimingMetrics.Stage.SQL,
                        TimingMetrics.Stage.TRANSACTION, TimingMetrics.Stage.SNAPSHOT}) {
                    Assertions.assertTrue(metrics.snapshot().rows().stream().anyMatch(r -> r.stage() == stage
                            && r.outcome() == TimingMetrics.Outcome.SUCCESS && r.count() > 0), stage.name());
                }
                Assertions.assertEquals(0, metrics.snapshot().failures());
                Assertions.assertEquals(0, metrics.snapshot().dropped());
                org.slf4j.LoggerFactory.getLogger(TimingDatabaseIT.class).info("TIMING_REAL_DATABASE_OBSERVATIONS {}", metrics.snapshot());
            } finally {
                TimingMetrics.reset(false);
                // 先证明本用例实际写线程退出，再清理唯一归属行；断言失败也不能遗留自有夹具。
                fixture.executor.shutdown();
                Assertions.assertTrue(fixture.executor.awaitTermination(20, TimeUnit.SECONDS));
                fixture.clean();
            }
        }
    }
}
