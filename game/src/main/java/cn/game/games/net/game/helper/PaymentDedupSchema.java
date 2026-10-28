package cn.game.games.net.game.helper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付 V1 持久化结构契约。只读取有界元数据，在查询和占位前逐次检查，不缓存成功结论。
 * 严格采用迁移字段、默认值和唯一约束，避免奖后才发现凭证不可写；观察签名不能隔离并发 DDL。
 */
public final class PaymentDedupSchema {
    /** 本报告格式及结构规则版本，不代表数据库迁移已执行。 */
    public static final int CONTRACT_VERSION = 1;
    /** 单次元数据查询最多允许128行，额外一行用于识别截断。 */
    private static final int MAX_ROWS = 128;
    /** 同迁移文件的精确字段定义；整数显示宽度不影响值域，允许驱动返回旧式宽度。 */
    private static final Map<String, String> CLAIM = Map.of(
            "order_id", "bigint", "sdk_order_id", "varchar(191):utf8mb4:utf8mb4_bin",
            "player_id", "bigint", "intent", "mediumtext:utf8mb4:utf8mb4_bin", "token", "char(36):ascii:ascii_bin");
    /** 完成凭证额外字段；正文不降容为 TEXT，摘要和 token 使用 ASCII 二进制比较。 */
    private static final Map<String, String> RECEIPT;
    static {
        var fields = new LinkedHashMap<>(CLAIM);
        fields.put("version", "int"); fields.put("proof", "mediumtext:utf8mb4:utf8mb4_bin");
        fields.put("proof_sha256", "char(64):ascii:ascii_bin"); RECEIPT = Map.copyOf(fields);
    }

    /** 只输出固定角色、字段及规则，不输出原始默认值、表达式或连接信息。 */
    public record Report(int contractVersion, String signature, List<String> blockers) {
        /** @param blockers 不可变固定诊断 */
        public Report { blockers = List.copyOf(blockers); }
        /** @return 仅本次元数据是否符合结构契约，不代表允许启用 */
        public boolean valid() { return blockers.isEmpty(); }
        /** @throws SQLException 结构不兼容，调用方必须在奖励前拒绝 */
        public void requireValid() throws SQLException {
            if (!valid()) { throw new SQLException("PAYMENT_SCHEMA_V" + contractVersion + ":" + String.join(",", blockers)
                    + ":signature=" + signature, "S1000"); }
        }
    }

    /** 工具类不创建实例。 */
    private PaymentDedupSchema() {}

    /**
     * @param c 调用方拥有的 Game 连接，本方法仅执行 SELECT 并关闭语句/结果集
     * @param player 固定生产或包内测试玩家表 @param claim 固定生产或受控测试占位表
     * @param receipt 固定生产或受控测试凭证表 @return 带观察签名及精确规则诊断的报告
     * @throws SQLException 元数据读取失败或超限，不能解释成结构兼容
     */
    static Report inspect(Connection c, String player, String claim, String receipt) throws SQLException {
        var blockers = new ArrayList<String>(); var observed = new ArrayList<String>();
        observed.add("payment-schema-contract:" + CONTRACT_VERSION);
        for (var entry : Map.of("player", player, "claim", claim, "receipt", receipt).entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).toList()) {
            String role = entry.getKey(), table = entry.getValue();
            var tables = rows(c, "SELECT table_type,engine FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name=?", table);
            observe(observed, role + ":table", tables);
            if (tables.size() != 1 || !"BASE TABLE".equals(tables.getFirst().get(0))
                    || !"InnoDB".equalsIgnoreCase(tables.getFirst().get(1))) { blockers.add(role + ":REQUIRE_INNODB_BASE_TABLE"); }
            if (role.equals("player")) { continue; }

            var fields = role.equals("claim") ? CLAIM : RECEIPT;
            var columns = rows(c, "SELECT column_name,column_type,character_set_name,collation_name,is_nullable,column_default,extra,generation_expression "
                    + "FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name=? ORDER BY ordinal_position", table);
            observe(observed, role + ":columns", columns);
            var seen = new java.util.HashSet<String>();
            for (var column : columns) {
                String name = column.get(0), expected = fields.get(name);
                if (expected == null) { blockers.add(role + ":EXTRA_COLUMN_UNSUPPORTED"); continue; }
                seen.add(name);
                String actual = column.get(1).toLowerCase(java.util.Locale.ROOT).replaceAll("^(bigint|int)\\([0-9]+\\)$", "$1");
                if (column.get(2) != null || column.get(3) != null) { actual += ":" + column.get(2) + ":" + column.get(3); }
                if (!expected.equals(actual)) { blockers.add(role + ":" + name + ":REQUIRE_" + expected); }
                if (!"NO".equals(column.get(4))) { blockers.add(role + ":" + name + ":REQUIRE_NOT_NULL"); }
                if (column.get(5) != null) { blockers.add(role + ":" + name + ":DEFAULT_UNSUPPORTED"); }
                if ((column.get(6) != null && !column.get(6).isEmpty()) || (column.get(7) != null && !column.get(7).isEmpty())) {
                    blockers.add(role + ":" + name + ":EXTRA_OR_GENERATED_UNSUPPORTED");
                }
            }
            for (String field : fields.keySet().stream().sorted().toList()) {
                if (!seen.contains(field)) { blockers.add(role + ":" + field + ":MISSING_COLUMN"); }
            }

            // 逐索引逐列读取，拒绝前缀、复合唯一键和额外唯一约束，不使用会丢失 SUB_PART 的 GROUP_CONCAT。
            var indexes = rows(c, "SELECT index_name,column_name,seq_in_index,sub_part,index_type,non_unique FROM information_schema.statistics "
                    + "WHERE table_schema=DATABASE() AND table_name=? ORDER BY index_name,seq_in_index", table);
            observe(observed, role + ":indexes", indexes);
            var groups = new LinkedHashMap<String, List<List<String>>>();
            for (var index : indexes) {
                if ("0".equals(index.get(5))) { groups.computeIfAbsent(index.get(0), key -> new ArrayList<>()).add(index); }
                else if (!"1".equals(index.get(5)) || index.get(1) == null || !fields.containsKey(index.get(1)) || !"BTREE".equals(index.get(4))) {
                    // 函数索引的隐藏生成列不一定出现在COLUMNS，不能把所有非唯一索引都当成无副作用。
                    blockers.add(role + ":NON_ORDINARY_INDEX_UNSUPPORTED");
                }
            }
            boolean primary = false, sdk = false;
            for (var group : groups.values()) {
                var index = group.getFirst();
                boolean full = group.size() == 1 && "1".equals(index.get(2)) && index.get(3) == null && "BTREE".equals(index.get(4));
                if (full && "PRIMARY".equals(index.get(0)) && "order_id".equals(index.get(1))) { primary = true; }
                else if (full && !"PRIMARY".equals(index.get(0)) && "sdk_order_id".equals(index.get(1)) && !sdk) { sdk = true; }
                else { blockers.add(role + ":INCOMPATIBLE_UNIQUE_CONSTRAINT"); }
            }
            if (!primary) { blockers.add(role + ":REQUIRE_ORDER_PRIMARY_FULL_LENGTH"); }
            if (!sdk) { blockers.add(role + ":REQUIRE_SDK_UNIQUE_FULL_LENGTH"); }

            var constraints = rows(c, "SELECT constraint_type FROM information_schema.table_constraints "
                    + "WHERE table_schema=DATABASE() AND table_name=? ORDER BY constraint_type,constraint_name", table);
            observe(observed, role + ":constraints", constraints);
            for (var constraint : constraints) {
                if (!List.of("PRIMARY KEY", "UNIQUE").contains(constraint.getFirst())) { blockers.add(role + ":CHECK_OR_FOREIGN_KEY_UNSUPPORTED"); }
            }
            var triggers = rows(c, "SELECT action_timing,event_manipulation FROM information_schema.triggers "
                    + "WHERE trigger_schema=DATABASE() AND event_object_table=? ORDER BY trigger_name", table);
            observe(observed, role + ":triggers", triggers);
            if (!triggers.isEmpty()) { blockers.add(role + ":TRIGGER_UNSUPPORTED"); }
        }
        return new Report(CONTRACT_VERSION, digest(observed), blockers.stream().distinct().toList());
    }

    /** @param c 只读元数据连接 @param sql 固定SELECT @param table 绑定表名 @return 有限元数据行 @throws SQLException 失败或超限 */
    private static List<List<String>> rows(Connection c, String sql, String table) throws SQLException {
        var result = new ArrayList<List<String>>();
        try (var statement = c.prepareStatement(sql + " LIMIT " + (MAX_ROWS + 1))) {
            statement.setString(1, table); statement.setQueryTimeout(2); statement.setMaxRows(MAX_ROWS + 1);
            try (var r = statement.executeQuery()) {
                while (r.next()) {
                    if (result.size() == MAX_ROWS) { throw new SQLException("PAYMENT_SCHEMA_METADATA_LIMIT"); }
                    var row = new ArrayList<String>();
                    for (int i = 1; i <= r.getMetaData().getColumnCount(); i++) { row.add(r.getString(i)); }
                    result.add(row);
                }
            }
        }
        return result;
    }

    /** @param out 签名输入 @param role 固定角色段 @param rows 原始元数据，仅进入摘要不进入诊断 */
    private static void observe(List<String> out, String role, List<List<String>> rows) {
        out.add(role); out.add(Integer.toString(rows.size()));
        for (var row : rows) { for (String value : row) { out.add(value == null ? "N" : "S" + value.length() + ":" + value); } }
    }

    /** @param values 无歧义分隔的元数据 @return 小写SHA256观察签名 */
    private static String digest(List<String> values) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(String.join("\n", values).getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException failure) {
            org.slf4j.LoggerFactory.getLogger(PaymentDedupSchema.class).error("JVM缺少结构签名摘要算法", failure);
            throw new ExceptionInInitializerError(failure);
        }
    }
}
