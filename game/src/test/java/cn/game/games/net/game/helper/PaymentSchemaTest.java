package cn.game.games.net.game.helper;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** 元数据故障单测：严格限制SQL边界，不以mock写入成功代替真实库验收。 */
class PaymentSchemaTest {
    /** 每次查询均使用限定表、行数和超时；缺表报告只含固定角色和规则。 */
    @Test void missingTablesAreBoundedReadonlyAndHaveStableSignature() throws Exception {
        var rows = new Rows(); var report = rows.inspect();
        Assertions.assertFalse(report.valid()); Assertions.assertTrue(report.blockers().contains("player:REQUIRE_INNODB_BASE_TABLE"));
        Assertions.assertTrue(report.blockers().contains("receipt:proof:MISSING_COLUMN"));
        Assertions.assertEquals(11, rows.reads); Assertions.assertEquals(rows.reads, rows.limits); Assertions.assertEquals(rows.reads, rows.timeouts);
        Assertions.assertTrue(report.signature().matches("[0-9a-f]{64}")); Assertions.assertEquals(report.signature(), new Rows().inspect().signature());
        Assertions.assertThrows(SQLException.class, report::requireValid);
    }

    /** 查询失败不能变成空元数据成功；保留SQL异常供调用者拒绝。 */
    @Test void metadataQueryFaultPropagatesWithoutPartialSuccess() {
        var rows = new Rows(); rows.fail = true;
        Assertions.assertThrows(java.sql.SQLTimeoutException.class, rows::inspect); Assertions.assertEquals(1, rows.reads);
    }

    /** 不信任JDBC悄悄截断，超限一行必须失败；签名不接受不完整元数据。 */
    @Test void excessiveMetadataFailsInsteadOfSigningTruncatedRows() {
        var rows = new Rows(); rows.excessive = true;
        var failure = Assertions.assertThrows(SQLException.class, rows::inspect);
        Assertions.assertEquals("PAYMENT_SCHEMA_METADATA_LIMIT", failure.getMessage());
    }

    /** 报告脱离调用者可变列表，后续篡改不能把已拒绝的检查变成valid。 */
    @Test void reportCopiesBlockersAndCarriesSafeVersionedFailure() {
        var blockers = new ArrayList<>(List.of("receipt:proof:MISSING_COLUMN"));
        var report = new PaymentDedupSchema.Report(1, "d".repeat(64), blockers); blockers.clear();
        Assertions.assertFalse(report.valid()); Assertions.assertThrows(UnsupportedOperationException.class, () -> report.blockers().clear());
        Assertions.assertTrue(Assertions.assertThrows(SQLException.class, report::requireValid).getMessage().contains("PAYMENT_SCHEMA_V1:receipt:proof:MISSING_COLUMN"));
    }

    /** 函数非唯一索引可能通过隐藏生成表达式使INSERT报错，不能被普通BTREE索引容差误放行。 */
    @Test void expressionNonUniqueIndexIsNotToleratedAsOrdinaryIndex() throws Exception {
        var rows = new Rows(); rows.expressionIndex = true;
        Assertions.assertTrue(rows.inspect().blockers().contains("receipt:NON_ORDINARY_INDEX_UNSUPPORTED"));
    }

    /** 仅模拟查询失败/空集合/超限，其他JDBC动作直接断言失败。 */
    private static final class Rows {
        /** 读取及边界设置计数。 */
        int reads, limits, timeouts;
        /** 当前查询故障及元数据超限注入。 */
        boolean fail, excessive, expressionIndex;

        /** @return 调用完整生产检查 @throws SQLException 元数据读取故障 */
        PaymentDedupSchema.Report inspect() throws SQLException {
            return PaymentDedupStore.inspectSchema(connection());
        }

        /** @return 只允许固定information_schema SELECT的连接 */
        Connection connection() {
            return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class}, (p, m, a) -> {
                if (!m.getName().equals("prepareStatement")) { throw new AssertionError(m.getName()); }
                String sql = (String) a[0];
                Assertions.assertTrue(sql.startsWith("SELECT ")); Assertions.assertTrue(sql.contains("information_schema."));
                Assertions.assertTrue(sql.contains("=DATABASE()")); Assertions.assertTrue(sql.endsWith(" LIMIT 129"));
                return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{java.sql.PreparedStatement.class}, (sp, sm, sa) -> {
                    switch (sm.getName()) {
                        case "setString": Assertions.assertEquals(1, sa[0]); Assertions.assertTrue(List.of("t_player_data", "t_payment_fulfillment_claim", "t_payment_fulfillment_receipt").contains(sa[1])); return null;
                        case "setQueryTimeout": Assertions.assertEquals(2, sa[0]); timeouts++; return null;
                        case "setMaxRows": Assertions.assertEquals(129, sa[0]); limits++; return null;
                        case "close": return null;
                        case "executeQuery":
                            reads++; if (fail) { throw new java.sql.SQLTimeoutException("private-driver-error"); }
                            int[] position = {-1};
                            return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{java.sql.ResultSet.class}, (rp, rm, ra) -> {
                                return switch (rm.getName()) {
                                    case "next" -> ++position[0] < (excessive ? 129 : expressionIndex && sql.contains("information_schema.statistics") ? 1 : 0);
                                    case "getString" -> expressionIndex && sql.contains("information_schema.statistics")
                                            ? new String[]{"expression_index", null, "1", null, "BTREE", "1"}[(Integer) ra[0] - 1] : "InnoDB";
                                    case "getMetaData" -> Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{java.sql.ResultSetMetaData.class},
                                            (mp, mm, ma) -> { Assertions.assertEquals("getColumnCount", mm.getName()); return expressionIndex && sql.contains("information_schema.statistics") ? 6 : 2; });
                                    case "close" -> null;
                                    default -> throw new AssertionError(rm.getName());
                                };
                            });
                        default: throw new AssertionError(sm.getName());
                    }
                });
            });
        }
    }
}
