package cn.game.games.diagnostics;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import cn.game.games.net.game.helper.PaymentDedupSchema;

/** 预检报告只能表达局部结构观察，异常及超时不能转为启用权限。 */
class PaymentEnablementTest {
    /** 局部结构符合仍保留所有外部阻断，退出码也不能驱动自动启用。 */
    @Test void validSchemaNeverAuthorizesEnablementCleanupOrReplay() {
        var report = PaymentEnablementCli.run(new String[0], () -> new PaymentDedupSchema.Report(1, "a".repeat(64), List.of()));
        Assertions.assertEquals(true, report.get("localSchemaReady")); Assertions.assertEquals(2, PaymentEnablementCli.exitCode(report));
        Assertions.assertEquals("BLOCKED_EXTERNAL_PREREQUISITES", report.get("status"));
        Assertions.assertEquals(3, ((List<?>) report.get("externalBlockers")).size());
        for (String flag : List.of("canEnable", "canDelete", "canResend")) { Assertions.assertEquals(false, report.get(flag)); }
        Assertions.assertEquals("NON_ATOMIC_METADATA_NO_DDL_FENCE", report.get("observation"));
    }

    /** 精确规则诊断和版本/签名传到操作员，结构问题不能被其他局部成功覆盖。 */
    @Test void incompatibleSchemaKeepsActionableContractDiagnostics() {
        var report = PaymentEnablementCli.run(new String[0], () -> new PaymentDedupSchema.Report(1, "b".repeat(64), List.of("receipt:proof:MISSING_COLUMN")));
        Assertions.assertEquals(false, report.get("localSchemaReady")); Assertions.assertEquals(3, PaymentEnablementCli.exitCode(report));
        Assertions.assertEquals(List.of("receipt:proof:MISSING_COLUMN"), report.get("localBlockers"));
        Assertions.assertEquals("b".repeat(64), report.get("schemaSignature")); Assertions.assertEquals(1, report.get("contractVersion"));
    }

    /** 参数错误不接触配置或数据库，不支持force、表名、开关或玩家范围。 */
    @Test void explicitNoArgumentBoundaryRejectsAllOverrides() {
        for (String[] args : List.of(new String[]{"--force"}, new String[]{"t_player_data"}, new String[]{"true"})) {
            var report = PaymentEnablementCli.run(args, () -> { throw new AssertionError("参数未过不能查询"); });
            Assertions.assertEquals(1, PaymentEnablementCli.exitCode(report)); Assertions.assertEquals("INVALID_INPUT", report.get("status"));
        }
        Assertions.assertEquals(1, PaymentEnablementCli.exitCode(PaymentEnablementCli.run(null)));
    }

    /** 配置/驱动异常正文可能含秘密，stdout报告及固定诊断不得携带它们。 */
    @Test void readerFailureIsRedactedAndNeverReady() {
        var report = PaymentEnablementCli.run(new String[0], () -> { throw new java.sql.SQLException("jdbc:secret-password-private-host"); });
        Assertions.assertEquals(3, PaymentEnablementCli.exitCode(report)); Assertions.assertNull(report.get("schemaSignature"));
        Assertions.assertFalse(report.toString().contains("secret")); Assertions.assertFalse(report.toString().contains("jdbc:"));
        Assertions.assertEquals(List.of("SCHEMA_CONFIG_OR_METADATA_UNAVAILABLE"), report.get("localBlockers"));
    }

    /** 任意注入读取器的等待超时不能取消实际工作或伪装观察退出。 */
    @Test void totalTimeoutCancelsObservationAndFailsClosed() throws Exception {
        CountDownLatch entered = new CountDownLatch(1), stopped = new CountDownLatch(1), release = new CountDownLatch(1);
        try {
            var report = PaymentEnablementCli.run(new String[0], () -> {
                entered.countDown();
                try { release.await(); return new PaymentDedupSchema.Report(1, "c".repeat(64), List.of()); }
                finally { stopped.countDown(); }
            }, 100);
            Assertions.assertTrue(entered.await(1, TimeUnit.SECONDS)); Assertions.assertEquals(1, stopped.getCount());
            Assertions.assertEquals(false, report.get("readerExited"));
            release.countDown(); Assertions.assertTrue(stopped.await(1, TimeUnit.SECONDS));
            Assertions.assertEquals(List.of("SCHEMA_OBSERVATION_TIMEOUT"), report.get("localBlockers"));
            Assertions.assertEquals(false, report.get("localSchemaReady")); Assertions.assertEquals(3, PaymentEnablementCli.exitCode(report));
        } finally { release.countDown(); }
    }
}
