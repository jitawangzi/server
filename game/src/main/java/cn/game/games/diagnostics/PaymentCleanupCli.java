package cn.game.games.diagnostics;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cn.game.games.net.game.helper.PaymentDedupStore;
import cn.game.games.net.game.module.recharge.PayItem;

/**
 * 显式逐单只读清理预检：每次认证真实归档并冷读 Game/Login 和 Game 持久凭证。
 * 多连接观察存在 TOCTOU，局部一致不代表平台终态、旧写入者隔离或 UNKNOWN 已排除；没有删除及补发入口。
 * @author shuyongqiang
 */
public final class PaymentCleanupCli {
    /** 永远缺失的外部证明必须显式列出，不能由 CLI 标记或本地凭证替代。 */
    private static final List<String> EXTERNAL = List.of("PLATFORM_TERMINAL_NOT_PROVEN", "OLD_WRITERS_AND_UNKNOWN_NOT_FENCED");
    /** 购买身份与完整完成证明的固定字段，不接受未来字段或省略字段的默认值推断。 */
    private static final java.util.Set<String> PROOF_FIELDS = java.util.Set.of("version", "playerId", "orderId", "sdkOrderId", "rmb",
            "payType", "payId", "sdkGoodsId", "paySubIds", "addId", "addCount");
    /** 运维测试仅注入存储边界，不提供任意 SQL、表名或业务回调。 */
    public interface Reader extends PaymentEvidenceCli.Reader {
        /** @param player 玩家 @param archive 认证且严格验证的归档订单 @return Game 新连接的有界两表观察 */
        PaymentDedupStore.Lookup durable(long player, PayItem archive) throws Exception;
    }

    /** 工具类不创建实例。 */
    private PaymentCleanupCli() {}

    /** @param args playerId orderId absoluteArchiveRoot absoluteExternalKey；始终非零退出，不授予清理权限 */
    public static void main(String[] args) throws Exception {
        Map<String, Object> result = run(args);
        PaymentArchiveEvidence.JSON.writeValue(new java.io.FileOutputStream(java.io.FileDescriptor.out), result);
        System.exit(exitCode(result));
    }

    /** @param result 脱敏报告 @return 1非法输入；2局部一致但外部阻断；3局部证据阻断（包括读取失败） */
    public static int exitCode(Map<String, Object> result) {
        return "INVALID_INPUT".equals(result.get("status")) ? 1 : Boolean.TRUE.equals(result.get("localEvidenceReady")) ? 2 : 3;
    }

    /** @param args 四个显式参数；配置仅由 S2_DB_CONFIG 提供 @return 安全诊断，无路径、单号或业务正文 */
    public static Map<String, Object> run(String[] args) {
        return runWithFactory(args, JdbcReader::new);
    }

    /** @param args 与生产 CLI 完全相同的参数 @param reader 只读测试边界 @return 使用同一输入与总超时门禁的报告 */
    public static Map<String, Object> run(String[] args, Reader reader) {
        return runWithFactory(args, () -> reader);
    }

    /** @return 与生产完全相同的固定两库读取器 @throws Exception 本轮配置不可用 */
    public static Reader jdbcReader() throws Exception { return new JdbcReader(); }

    /** 可抛配置异常的读取器工厂，仅在有界守护线程内创建。 */
    private interface ReaderFactory {
        /** @return 本次读取器 @throws Exception 配置失败 */
        Reader open() throws Exception;
    }

    /** @param args 显式参数 @param factory 只读依赖工厂 @return 有界报告 */
    private static Map<String, Object> runWithFactory(String[] args, ReaderFactory factory) {
        if (args == null || args.length != 4 || !positive(args[0]) || !positive(args[1])) {
            return report(List.of("EXPLICIT_POSITIVE_PLAYER_ORDER_REQUIRED"), "NOT_READ", "NOT_OBSERVED", true);
        }
        try {
            return inspect(Path.of(args[2]), Path.of(args[3]), Long.parseLong(args[0]), Long.parseLong(args[1]), factory.open());
        } catch (Exception failure) {
            PaymentArchiveStore.safeWarn(failure);
            if (failure instanceof InterruptedException) { Thread.currentThread().interrupt(); }
            return report(List.of(failure instanceof java.util.concurrent.TimeoutException ? "OBSERVATION_TIMEOUT" : "CONFIG_OR_READER_ERROR"),
                    "NOT_VERIFIED", "NOT_OBSERVED", false);
        }
    }

    /** @param value 显式输入 @return 是否无符号、无溢出的正 long */
    private static boolean positive(String value) {
        return value != null && value.matches("[1-9][0-9]{0,18}")
                && (value.length() < 19 || value.compareTo("9223372036854775807") <= 0);
    }

    /**
     * @param root 显式绝对归档根 @param key 根外部私有 key @param player 玩家 @param order 单笔订单 @param reader 有界只读源
     * @return 每个阻断原因及下一步所需证据；读取不提供跨库原子性或任何写入授权
     */
    public static Map<String, Object> inspect(Path root, Path key, long player, long order, Reader reader) {
        JsonNode archive;
        try { archive = PaymentArchiveStore.readAuthenticated(root, key, player, order); }
        catch (Exception failure) {
            PaymentArchiveStore.safeWarn(failure);
            return report(List.of("ARCHIVE_AUTHENTICATION_OR_FORMAT_FAILED"), "NOT_VERIFIED", "NOT_OBSERVED", false);
        }
        List<String> reasons = new ArrayList<>();
        PayItem expected;
        try {
            expected = completedItem(player, archive.path("payItem"));
            if (!"PERSISTED_V1_PROOF_CONSISTENT".equals(archive.path("assessment").path("game").asText())
                    || archive.path("loginOrder").path("payState").asInt(-1) != 2
                    || archive.path("loginOrder").path("delivered").asInt(-1) != 1) {
                reasons.add("ARCHIVE_RISK_OR_NON_COMPLETED_LOGIN");
            }
        } catch (Exception failure) {
            PaymentArchiveStore.safeWarn(failure);
            return report(List.of("ARCHIVE_LEGACY_UNKNOWN_OR_INVALID_COMPLETION"), "HMAC_SHA256_VERIFIED", "NOT_OBSERVED", false);
        }

        // 只捕获一次两库冷读，随后在同一内存观察上复用归档结构校验，不重新查询混合数据。
        PaymentEvidenceCli.ModulesSnapshot[] game = new PaymentEvidenceCli.ModulesSnapshot[1];
        PaymentEvidenceCli.LoginOrder[] login = new PaymentEvidenceCli.LoginOrder[1];
        Map<String, Object> observed = PaymentEvidenceCli.inspect(player, order, new PaymentEvidenceCli.Reader() {
            @Override public String game(long id) throws Exception { return reader.game(id); }
            @Override public PaymentEvidenceCli.ModulesSnapshot gameSnapshot(long id) throws Exception { return game[0] = reader.gameSnapshot(id); }
            @Override public PaymentEvidenceCli.LoginOrder login(long id) throws Exception { return login[0] = reader.login(id); }
        });
        String hot = "NOT_OBSERVED";
        if ("ORDER_NOT_FOUND".equals(observed.get("game"))) {
            hot = "ALREADY_NOT_PRESENT_OBSERVED"; reasons.add("HOT_ITEM_ALREADY_NOT_PRESENT_NO_DELETION_PERFORMED");
        } else if (!"PERSISTED_V1_PROOF_CONSISTENT".equals(observed.get("game"))) {
            reasons.add("CURRENT_GAME_" + observed.get("game"));
        } else { hot = "PRESENT_OBSERVED"; }
        if (!"OBSERVED".equals(observed.get("login"))) { reasons.add("CURRENT_LOGIN_" + observed.get("login")); }
        else if (login[0].payState() != 2 || !Integer.valueOf(1).equals(login[0].delivered())) { reasons.add("CURRENT_LOGIN_NOT_COMPLETED"); }
        if ("PRESENT_OBSERVED".equals(hot) && "OBSERVED".equals(observed.get("login"))) {
            try {
                ObjectNode current = PaymentArchiveEvidence.collect(player, order, new PaymentEvidenceCli.Reader() {
                    @Override public String game(long id) { return game[0].modules(); }
                    @Override public PaymentEvidenceCli.ModulesSnapshot gameSnapshot(long id) { return game[0]; }
                    @Override public PaymentEvidenceCli.LoginOrder login(long id) { return login[0]; }
                });
                completedItem(player, current.path("payItem"));
                // V1规范字节消除内存LongNode与磁盘小整数IntNode的表示差异，仍逐字段保留精确整数和数组次序。
                if (!same(current.path("payItem"), archive.path("payItem"))) { reasons.add("CURRENT_HOT_ITEM_CHANGED_FROM_ARCHIVE"); }
                if (!same(current.path("loginOrder"), archive.path("loginOrder"))) { reasons.add("CURRENT_LOGIN_CHANGED_FROM_ARCHIVE"); }
            } catch (Exception failure) { PaymentArchiveStore.safeWarn(failure); reasons.add("CURRENT_IDENTITY_PARAMETERS_OR_FORMAT_CONFLICT"); }
        }
        try {
            PaymentDedupStore.Lookup durable = reader.durable(player, expected);
            if (durable != PaymentDedupStore.Lookup.COMPLETED) { reasons.add(durable == PaymentDedupStore.Lookup.CLAIMED
                    ? "DURABLE_CLAIMED_WITHOUT_COMPLETED_RECEIPT" : "DURABLE_CLAIM_OR_RECEIPT_MISSING"); }
        } catch (Exception failure) {
            PaymentArchiveStore.safeWarn(failure);
            reasons.add(failure instanceof java.sql.SQLTimeoutException ? "DURABLE_QUERY_TIMEOUT"
                    : failure instanceof java.sql.SQLException ? "DURABLE_QUERY_OR_TABLE_ERROR" : "DURABLE_IDENTITY_VERSION_OR_PROOF_CONFLICT");
        }
        return report(reasons, "HMAC_SHA256_VERIFIED", hot, false);
    }

    /** @param player 玩家 @param tree 普通树，无多态类加载 @return 与持久去重相同校验链核对的完成订单 */
    private static PayItem completedItem(long player, JsonNode tree) throws Exception {
        var fields = new java.util.HashSet<>(PROOF_FIELDS); fields.remove("version"); fields.remove("playerId");
        fields.addAll(List.of("isFinish", "finishTime", "completionState", "completionProof"));
        PaymentArchiveEvidence.require(tree.isObject() && PaymentArchiveEvidence.names(tree).equals(fields)
                && PaymentArchiveEvidence.names(tree.path("completionProof")).equals(PROOF_FIELDS));
        ObjectNode plain = tree.deepCopy();
        for (String field : List.of("payType", "paySubIds")) {
            plain.set(field, PaymentArchiveEvidence.unwrap(plain.path(field)));
            ((ObjectNode) plain.path("completionProof")).set(field, PaymentArchiveEvidence.unwrap(plain.path("completionProof").path(field)));
        }
        // 项目持久格式使用私有字段（isFinish/completionProof），不能用默认getter/setter映射丢失完成证据。
        // 只指定固定PayItem类型，绝不使用带类型反序列化或归档提供的类名。
        PayItem item = cn.game.util.JsonUtil.parseObject(plain.toString(), PayItem.class);
        PaymentDedupStore.validateCompletedItem(player, item);
        return item;
    }

    /** @param left 当前普通树 @param right 已认证树 @return V1规范整数与全部内容是否精确一致 */
    private static boolean same(JsonNode left, JsonNode right) throws Exception {
        return java.security.MessageDigest.isEqual(PaymentArchiveStore.canonical(left),PaymentArchiveStore.canonical(right));
    }

    /** @param local 局部阻断 @param integrity 认证状态 @param hot 热订单观察 @param invalid 是否非法输入 @return 脱敏、恒无授权的操作员报告 */
    private static Map<String, Object> report(List<String> local, String integrity, String hot, boolean invalid) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", invalid ? "INVALID_INPUT" : local.isEmpty() ? "BLOCKED_EXTERNAL_PROOFS" : "BLOCKED_LOCAL_EVIDENCE");
        out.put("localEvidenceReady", !invalid && local.isEmpty()); out.put("canDelete", false); out.put("canResend", false);
        out.put("archiveIntegrity", integrity); out.put("hotItem", hot); out.put("localBlockers", List.copyOf(local));
        out.put("externalBlockers", EXTERNAL); out.put("platformTerminal", "NOT_PROVEN"); out.put("writerAndUnknownFencing", "NOT_PROVEN");
        out.put("observation", "MULTI_CONNECTION_NON_ATOMIC_TOCTOU_NOT_AUTHORIZATION");
        out.put("retention", "RETAIN_HOT_ITEM_ARCHIVE_CLAIM_AND_RECEIPT");
        out.put("requiredNextEvidence", List.of("INDEPENDENT_AUTHENTICATED_PLATFORM_TERMINAL_SOURCE",
                "TRUSTED_OLD_WRITER_FENCING_AND_UNKNOWN_EXCLUSION", "FUTURE_ATOMIC_REVALIDATION_DESIGN"));
        return out;
    }

    /** 固定 Game 数据源读取凭证，不初始化 Spring 或 Player，不接受生产表名覆盖。 */
    private static final class JdbcReader implements Reader {
        /** 复用两库配置、主键查询与连接超时。 */
        private final PaymentEvidenceCli.JdbcReader source;
        /** @throws Exception 配置缺失或读取失败 */
        JdbcReader() throws Exception { source = new PaymentEvidenceCli.JdbcReader(); }
        @Override public String game(long player) throws Exception { return source.game(player); }
        @Override public PaymentEvidenceCli.ModulesSnapshot gameSnapshot(long player) throws Exception { return source.gameSnapshot(player); }
        @Override public PaymentEvidenceCli.LoginOrder login(long order) throws Exception { return source.login(order); }
        @Override public PaymentDedupStore.Lookup durable(long player, PayItem archive) throws Exception {
            try (var c = source.connect("")) { return PaymentDedupStore.observe(c, player, archive); }
        }
    }
}
