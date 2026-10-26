package cn.game.games.diagnostics;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** 验证诊断尺寸和读取边界，不将诊断限额当成持久化限制或归档许可。 */
class PaymentCapacityDiagnosticsTest {
    /** 实际 JDBC 查询装配保持主键/只读/限时/限行，SQL 明确 UTF-8 且超限绝不读取正文。 */
    @Test void jdbcProjectionIsUtf8BoundedAndDoesNotReadOversizedBody() throws Exception {
        for (long bytes : new long[] {12, PaymentEvidenceCli.MAX_BYTES + 1L}) {
            var calls = new java.util.HashMap<String, Object>();
            var rows = (java.sql.ResultSet) java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[] {java.sql.ResultSet.class}, (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "next": calls.merge("next", 1, (a, b) -> (int) a + (int) b); return true;
                            case "getLong": Assertions.assertEquals(1, args[0]); return bytes;
                            case "getString":
                                Assertions.assertTrue(bytes <= PaymentEvidenceCli.MAX_BYTES, "超限禁止正文读取");
                                Assertions.assertEquals(2, args[0]); calls.put("body", true); return "支付é😀";
                            case "close": calls.put("rowsClosed", true); return null;
                            default: throw new AssertionError("未授权结果集操作:" + method.getName());
                        }
                    });
            var statement = (java.sql.PreparedStatement) java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[] {java.sql.PreparedStatement.class}, (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "setQueryTimeout": calls.put("timeout", args[0]); return null;
                            case "setMaxRows": calls.put("maxRows", args[0]); return null;
                            case "setLong": Assertions.assertEquals(1, args[0]); calls.put("player", args[1]); return null;
                            case "executeQuery": return rows;
                            case "close": calls.put("statementClosed", true); return null;
                            default: throw new AssertionError("未授权语句操作:" + method.getName());
                        }
                    });
            var connection = (java.sql.Connection) java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[] {java.sql.Connection.class}, (proxy, method, args) -> {
                        if (!method.getName().equals("prepareStatement")) { throw new AssertionError("未授权连接操作:" + method.getName()); }
                        String sql = (String) args[0];
                        Assertions.assertTrue(sql.startsWith("SELECT OCTET_LENGTH(CONVERT(modules USING utf8mb4))"));
                        Assertions.assertTrue(sql.contains("CASE WHEN OCTET_LENGTH(CONVERT(modules USING utf8mb4))<=4194304"));
                        Assertions.assertTrue(sql.endsWith("FROM t_player_data WHERE player_id=? LIMIT 1"));
                        return statement;
                    });
            var result = PaymentEvidenceCli.readGame(connection, 41);
            Assertions.assertEquals(bytes, result.utf8Bytes());
            Assertions.assertEquals(41L, calls.get("player")); Assertions.assertEquals(2, calls.get("timeout"));
            Assertions.assertEquals(1, calls.get("maxRows")); Assertions.assertEquals(1, calls.get("next"));
            Assertions.assertEquals(true, calls.get("rowsClosed")); Assertions.assertEquals(true, calls.get("statementClosed"));
            if (bytes > PaymentEvidenceCli.MAX_BYTES) { Assertions.assertNull(result.modules()); Assertions.assertFalse(calls.containsKey("body")); }
            else { Assertions.assertEquals("支付é😀", result.modules()); }
        }
    }

    /** 完整 modules UTF-8 尺寸及订单数量来自同一正文，重复诊断无业务写入。 */
    @Test void utf8CapacityAndOrdersAreReadOnlyAndRedacted() {
        var item = PaymentEvidenceTest.completed(); item.setSdkOrderId("secret-支付😀");
        String raw = PaymentEvidenceTest.snapshot(item);
        for (int i = 0; i < 2; i++) {
            Map<String, Object> result = PaymentEvidenceCli.inspect(41, 91, PaymentEvidenceTest.reader(raw));
            Assertions.assertEquals((long) raw.getBytes(StandardCharsets.UTF_8).length, result.get("modulesUtf8Bytes"));
            Assertions.assertEquals(1, result.get("retainedOrders"));
            Assertions.assertEquals("FULL_MODULES_UTF8", result.get("modulesSizeScope"));
            Assertions.assertEquals("NOT_PROVEN", result.get("transactionResolution"));
            Assertions.assertFalse(result.toString().contains("secret")); Assertions.assertEquals(false, result.get("canResend"));
        }
        Assertions.assertEquals(raw, PaymentEvidenceTest.snapshot(item));
    }

    /** 4 MiB 相等可解析，超过一个 UTF-8 字节仅报告输入受限，不能伪装无订单。 */
    @Test void diagnosticByteBoundaryIsInclusiveAndDoesNotLimitSaving() {
        String raw = PaymentEvidenceTest.snapshot(PaymentEvidenceTest.completed());
        String exact = raw + " ".repeat(PaymentEvidenceCli.MAX_BYTES - raw.getBytes(StandardCharsets.UTF_8).length);
        var accepted = PaymentEvidenceCli.inspect(41, 91, PaymentEvidenceTest.reader(exact));
        Assertions.assertEquals("PERSISTED_V1_PROOF_CONSISTENT", accepted.get("game"));
        Assertions.assertEquals((long) PaymentEvidenceCli.MAX_BYTES, accepted.get("modulesUtf8Bytes"));
        var limited = PaymentEvidenceCli.inspect(41, 91, PaymentEvidenceTest.reader(exact + " "));
        Assertions.assertEquals("INPUT_LIMIT", limited.get("game"));
        Assertions.assertEquals((long) PaymentEvidenceCli.MAX_BYTES + 1, limited.get("modulesUtf8Bytes"));
        Assertions.assertEquals("NOT_READ_INPUT_LIMIT", limited.get("retainedOrders"));
    }

    /** 超限时读取器只提供标量，正文入口不能被调用；仍读 Login 单主键作人工核查。 */
    @Test void oversizedStoredRowReturnsExactBoundedSizeWithoutLoadingBody() {
        AtomicInteger reads = new AtomicInteger(), logins = new AtomicInteger();
        var reader = new PaymentEvidenceCli.Reader() {
            @Override public String game(long id) { throw new AssertionError("禁止超限正文读取"); }
            @Override public PaymentEvidenceCli.ModulesSnapshot gameSnapshot(long id) {
                Assertions.assertEquals(41, id); reads.incrementAndGet();
                return new PaymentEvidenceCli.ModulesSnapshot(null, 123_456_789);
            }
            @Override public PaymentEvidenceCli.LoginOrder login(long id) {
                Assertions.assertEquals(91, id); logins.incrementAndGet(); return null;
            }
        };
        var result = PaymentEvidenceCli.inspect(41, 91, reader);
        Assertions.assertEquals(1, reads.get()); Assertions.assertEquals(1, logins.get());
        Assertions.assertEquals(123_456_789L, result.get("modulesUtf8Bytes"));
        Assertions.assertEquals("INPUT_LIMIT", result.get("game"));
        Assertions.assertEquals(PaymentEvidenceCli.MAX_BYTES, result.get("diagnosticLimitBytes"));
        Assertions.assertEquals("NOT_READ_INPUT_LIMIT", result.get("retainedOrders"));
        Assertions.assertTrue(result.toString().length() < 2_000); Assertions.assertEquals(false, result.get("canResend"));
    }
}
