package cn.game.games.diagnostics;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLTimeoutException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.game.util.DateUtil;
import cn.game.games.net.game.helper.PaymentCapacityMonitor;

/**
 * 显式本地 CLI：只查询一个玩家的一笔订单，不构造 Player、不启用多态反序列化或登录 hook。
 * 两库分别读取，仅提供人工核查证据，不能解除 UNKNOWN、证明事务终止或触发补发。
 */
public final class PaymentEvidenceCli {
    /** 仅记录脱敏异常，不输出驱动消息、配置值或订单正文。 */
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentEvidenceCli.class);
    /** 单次最多读取 4 MiB 玩家模块 JSON 正文；这是诊断上限，不是保存上限。 */
    public static final int MAX_BYTES = 4 * 1024 * 1024;
    /** 单库 SQL 上限两秒，连接及 socket 上限两秒。 */
    private static final int QUERY_SECONDS = 2;
    /** 普通树解析器不解析 JSON 中的类名，也拒绝重复键和尾随文档。 */
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
            .enable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    /** 聚合快照中的固定模块名称。 */
    private static final String MODULE = "cn.game.games.net.game.module.player.PlayerModule";

    /** 工具类不创建实例。 */
    private PaymentEvidenceCli() {}

    /** 仅供边界注入测试；实现必须是按主键的有界只读查询。 */
    public interface Reader {
        /** @param playerId 显式正玩家 ID @return 模块 JSON；无玩家行返回 null */
        String game(long playerId) throws Exception;
        /** @param playerId 单个玩家主键 @return 正文及完整 modules 字节数；超诊断上限仍可只返回尺寸 */
        default ModulesSnapshot gameSnapshot(long playerId) throws Exception {
            String raw = game(playerId);
            return raw == null ? null : new ModulesSnapshot(raw, PaymentCapacityMonitor.utf8Bytes(raw));
        }
        /** @param orderId 显式正订单 ID @return 最小订单字段；无订单返回 null */
        LoginOrder login(long orderId) throws Exception;
    }

    /** 超限时 modules 为 null，字节数仍来自同一主键行，不用占位大字符串伪造大小。 */
    public record ModulesSnapshot(String modules, long utf8Bytes) {}

    /** 不携带平台 receipt、账号、商品正文或原始流水。 */
    public record LoginOrder(long playerId, int price, int payState, Integer delivered, String sdkOrderId) {}

    /**
     * @param args 两个参数：playerId orderId；连接配置仅从 S2_DB_CONFIG 读取
     * @throws Exception 输出失败等基础设施错误；查询异常只输出脱敏状态码
     */
    public static void main(String[] args) throws Exception {
        JSON.writeValue(new java.io.FileOutputStream(java.io.FileDescriptor.out), run(args));
    }

    /** @param args 恰好两个正整数；不接受扫描、页码或任意 SQL @return 脱敏结果 */
    public static Map<String, Object> run(String[] args) {
        if (args.length != 2 || !args[0].matches("[1-9][0-9]{0,18}") || !args[1].matches("[1-9][0-9]{0,18}")) {
            return Map.of("status", "INVALID_INPUT");
        }
        for (String id : args) {
            if (id.length() == 19 && id.compareTo("9223372036854775807") > 0) { return Map.of("status", "INVALID_INPUT"); }
        }
        try {
            long player = Long.parseLong(args[0]), order = Long.parseLong(args[1]);
            return inspect(player, order, new JdbcReader());
        } catch (Exception failure) {
            warn(failure);
            if (failure instanceof InterruptedException) { Thread.currentThread().interrupt(); }
            // 诊断输出禁止携带 JDBC 异常正文、URL 或凭据。
            return Map.of("status", "QUERY_OR_CONFIG_ERROR", "transactionResolution", "NOT_PROVEN");
        }
    }

    /** @param playerId 指定玩家 @param orderId 指定订单 @param reader 有界存储读取器 @return 仅作人工依据的结构化状态 */
    public static Map<String, Object> inspect(long playerId, long orderId, Reader reader) {
        if (playerId <= 0 || orderId <= 0) { return Map.of("status", "INVALID_INPUT"); }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "MANUAL_REVIEW_ONLY");
        result.put("playerId", playerId); result.put("orderId", orderId);
        result.put("source", "OFFLINE_STORAGE_NON_ATOMIC");
        result.put("inFlight", "NOT_OBSERVABLE"); result.put("memoryOnly", "NOT_OBSERVABLE");
        result.put("transactionResolution", "NOT_PROVEN"); result.put("canResend", false);
        result.put("retention", "RETAIN_NO_TRUSTED_ARCHIVE_OR_TERMINAL_PROOF");
        result.put("capacity", "UNBOUNDED_ORDER_COUNT_NO_EVICTION");
        result.put("modulesSizeScope", "FULL_MODULES_UTF8");
        result.put("diagnosticLimitBytes", MAX_BYTES);
        result.put("retainedOrders", "NOT_OBSERVED");
        JsonNode item = null;
        result.put("gameReadStartedAt", DateUtil.currentTimeMillis());
        try {
            ModulesSnapshot snapshot = reader.gameSnapshot(playerId);
            String raw = snapshot == null ? null : snapshot.modules();
            if (snapshot == null) { result.put("game", "PLAYER_NOT_FOUND"); }
            else if (snapshot.utf8Bytes() > MAX_BYTES) {
                result.put("modulesUtf8Bytes", snapshot.utf8Bytes());
                result.put("retainedOrders", "NOT_READ_INPUT_LIMIT");
                result.put("game", "INPUT_LIMIT");
            } else {
                if (raw == null || snapshot.utf8Bytes() < 0 || raw.length() > MAX_BYTES
                        || PaymentCapacityMonitor.utf8Bytes(raw) > MAX_BYTES) { throw new IllegalArgumentException("INVALID_SIZE"); }
                result.put("modulesUtf8Bytes", snapshot.utf8Bytes());
                JsonNode root = unwrap(JSON.readTree(raw));
                if (!root.isObject()) { throw new IllegalArgumentException("INVALID_MODULES"); }
                JsonNode module = unwrap(root.path(MODULE));
                if (module.isMissingNode()) { result.put("game", "MODULE_NOT_FOUND"); }
                else {
                    if (!module.isObject()) { throw new IllegalArgumentException("INVALID_MODULE"); }
                    JsonNode items = unwrap(module.path("payItems"));
                    if (!items.isObject()) { throw new IllegalArgumentException("INVALID_ORDERS"); }
                    result.put("retainedOrders", items.size());
                    item = unwrap(items.path(Long.toString(orderId)));
                    if (item.isMissingNode()) { result.put("game", "ORDER_NOT_FOUND"); }
                    else { classify(result, item, playerId, orderId); }
                }
            }
        } catch (Exception failure) {
            warn(failure);
            item = null;
            result.put("game", failure instanceof SQLTimeoutException ? "TIMEOUT"
                    : failure instanceof java.sql.SQLException ? "QUERY_ERROR" : "MALFORMED_OR_READ_ERROR");
        }
        result.put("gameReadEndedAt", DateUtil.currentTimeMillis());
        result.put("loginReadStartedAt", DateUtil.currentTimeMillis());
        try {
            LoginOrder login = reader.login(orderId);
            if (login == null) { result.put("login", "ORDER_NOT_FOUND"); }
            else {
                result.put("login", login.playerId() != playerId || login.price() <= 0 ? "IDENTITY_OR_AMOUNT_CONFLICT"
                        : (login.payState() != 1 && login.payState() != 2)
                          || login.delivered() == null || (login.delivered() != 0 && login.delivered() != 1)
                            ? "UNKNOWN_STATE" : "OBSERVED");
                result.put("loginPayState", login.payState()); result.put("loginDelivered", String.valueOf(login.delivered()));
                boolean match = item != null && item.isObject() && item.path("rmb").isIntegralNumber()
                        && (long) item.path("rmb").intValue() * 100 == login.price() && login.playerId() == playerId
                        && item.path("orderId").asLong(-1) == orderId;
                result.put("association", match ? "PLAYER_ORDER_AMOUNT_MATCH" : "CONFLICT_OR_UNAVAILABLE");
                result.put("sdkBinding", item == null || login.sdkOrderId() == null || login.sdkOrderId().isBlank()
                        || !item.path("sdkOrderId").isTextual() ? "UNAVAILABLE"
                        : login.sdkOrderId().equals(item.path("sdkOrderId").textValue()) ? "MATCH" : "CONFLICT");
            }
        } catch (Exception failure) {
            warn(failure);
            result.put("login", failure instanceof SQLTimeoutException ? "TIMEOUT" : "QUERY_ERROR");
        }
        result.put("loginReadEndedAt", DateUtil.currentTimeMillis());
        return result;
    }

    /** @param out 仅包含分类的输出 @param item 原始订单树 @param player 玩家归属 @param order 订单归属 */
    private static void classify(Map<String, Object> out, JsonNode item, long player, long order) {
        if (!item.isObject() || !item.path("orderId").isIntegralNumber() || !item.path("orderId").canConvertToLong()
                || item.path("orderId").longValue() != order
                || !item.path("rmb").isInt() || item.path("rmb").intValue() <= 0
                || !item.path("isFinish").isBoolean() || !item.path("finishTime").isIntegralNumber()) {
            out.put("game", "INVALID_OR_CONFLICTING_FIELDS"); return;
        }
        out.put("historicalFinish", item.path("isFinish").booleanValue());
        String state = item.path("completionState").isTextual() ? item.path("completionState").textValue() : "ABSENT_OR_INVALID";
        // 未知阶段不可原样输出，防止异常字段成为数据泄漏通道。
        out.put("completionState", java.util.List.of("EXECUTING", "REWARDS_COMPLETED", "REVIEW_REQUIRED").contains(state) ? state : "ABSENT_OR_UNKNOWN");
        JsonNode proof = item.path("completionProof");
        if (proof.isMissingNode() || proof.isNull()) { out.put("game", "NO_COMPLETION_PROOF"); return; }
        if (!proof.isObject() || !proof.path("version").isInt()) { out.put("game", "MALFORMED_PROOF"); return; }
        out.put("proofVersion", proof.path("version").intValue());
        if (proof.path("version").intValue() != 1) { out.put("game", "UNKNOWN_PROOF_VERSION"); return; }
        boolean matches = proof.path("playerId").isIntegralNumber() && proof.path("playerId").canConvertToLong()
                && proof.path("playerId").longValue() == player
                && item.path("isFinish").booleanValue() && "REWARDS_COMPLETED".equals(state)
                && item.path("sdkOrderId").isTextual() && !item.path("sdkOrderId").textValue().isBlank()
                && unwrap(item.path("payType")).isTextual()
                && java.util.List.of("FirstCharge", "ActivityJQB", "ChapterPacks", "MonthCard", "ShopItem", "FundPass", "Recharge",
                        "VipGift", "XianShiLiBao", "DayGift", "LimitedTimeGift", "FundPassSignActivity", "WestLucky")
                        .contains(unwrap(item.path("payType")).textValue());
        for (String field : new String[] {"orderId", "sdkOrderId", "rmb", "payType", "payId", "sdkGoodsId", "paySubIds", "addId", "addCount"}) {
            matches &= !item.path(field).isMissingNode() && unwrap(item.path(field)).equals(unwrap(proof.path(field)));
        }
        for (String field : new String[] {"payId", "sdkGoodsId", "addId", "addCount"}) {
            matches &= item.path(field).isInt();
        }
        JsonNode subIds = unwrap(item.path("paySubIds"));
        matches &= subIds.isArray();
        for (JsonNode sub : subIds) { matches &= sub.isInt(); }
        out.put("game", matches ? "PERSISTED_V1_PROOF_CONSISTENT" : "PROOF_OR_IDENTITY_CONFLICT");
    }

    /** @param failure 仅保留异常类型与栈帧；不保留可能含凭据或原始字段的消息和原因链 */
    private static void warn(Exception failure) {
        var safe = new IllegalStateException("PAYMENT_EVIDENCE_" + failure.getClass().getSimpleName());
        safe.setStackTrace(failure.getStackTrace());
        log.warn("支付证据只读核对未完成，保留人工核查状态", safe);
    }

    /** @param node 普通树或既有 Jackson 类型包装 @return 不实例化任何类的载荷 */
    private static JsonNode unwrap(JsonNode node) {
        return node.isArray() && node.size() == 2 && node.get(0).isTextual()
                && (node.get(0).textValue().startsWith("java.util.") || node.get(0).textValue().startsWith("cn.game.games.net.game.module."))
                ? node.get(1) : node;
    }

    /**
     * @param connection 调用方拥有的只读连接，本方法仅关闭语句及结果集
     * @param playerId 显式玩家主键，不允许扫描
     * @return 同一行的 UTF-8 尺寸及受限正文，超限不调用 getString 读取正文
     * @throws java.sql.SQLException 查询失败，由上层脱敏分类
     */
    static ModulesSnapshot readGame(java.sql.Connection connection, long playerId) throws java.sql.SQLException {
        // 显式统一为 UTF-8，避免把非 UTF-8 列的存储字节错误标记为快照 UTF-8 字节。
        try (var s = connection.prepareStatement("SELECT OCTET_LENGTH(CONVERT(modules USING utf8mb4)), CASE WHEN OCTET_LENGTH(CONVERT(modules USING utf8mb4))<=4194304 THEN modules ELSE NULL END FROM t_player_data WHERE player_id=? LIMIT 1")) {
            s.setQueryTimeout(QUERY_SECONDS); s.setMaxRows(1); s.setLong(1, playerId);
            try (var rows = s.executeQuery()) {
                if (!rows.next()) { return null; }
                long bytes = rows.getLong(1);
                if (bytes > MAX_BYTES) { return new ModulesSnapshot(null, bytes); }
                return new ModulesSnapshot(rows.getString(2) == null ? "null" : rows.getString(2), bytes);
            }
        }
    }

    /** 私有连接只运行固定 SELECT；配置文件由受控本地运维提供，不自动连接 Apollo。 */
    static final class JdbcReader implements Reader {
        /** 从受限本地文件读取的 Game 与 Login 配置，禁止输出。 */
        private final Properties config = new Properties();
        /** 整条诊断共享单调起点，包括两库连接、结构元数据和凭证读取。 */
        private final long started = System.nanoTime();
        /** @throws Exception 缺少配置时失败，不回退生产缺省配置 */
        JdbcReader() throws Exception {
            try (InputStream input = Files.newInputStream(Path.of(java.lang.System.getenv("S2_DB_CONFIG")))) { config.load(input); }
        }
        /** @param prefix 空字符串为 Game，login. 为 Login @return 私有只读连接 */
        java.sql.Connection connect(String prefix) throws Exception {
            Properties p = new Properties();
            p.setProperty("user", config.getProperty(prefix + "dbuser")); p.setProperty("password", config.getProperty(prefix + "dbpass"));
            p.setProperty("connectTimeout", "2000"); p.setProperty("socketTimeout", "2000"); p.setProperty("autoReconnect", "false");
            String url = config.getProperty(prefix + "dburl");
            cn.game.games.net.game.helper.PaymentDatabaseBoundary.validateUrl(url, p, 2000);
            var source = new org.springframework.jdbc.datasource.DriverManagerDataSource(url, p);
            var connection = cn.game.games.net.game.helper.PaymentDatabaseBoundary.open(source,
                    new cn.game.games.net.game.helper.PaymentDatabaseBoundary.Limits(2000, 2000, 2000, 12000), started);
            try { connection.setReadOnly(true); return connection; }
            catch (Exception failure) { warn(failure); connection.close(); throw failure; }
        }
        @Override public String game(long playerId) throws Exception {
            ModulesSnapshot snapshot = gameSnapshot(playerId);
            return snapshot == null ? null : snapshot.modules();
        }
        @Override public ModulesSnapshot gameSnapshot(long playerId) throws Exception {
            try (var c = connect("")) { return readGame(c, playerId); }
        }
        @Override public LoginOrder login(long orderId) throws Exception {
            try (var c = connect("login."); var s = c.prepareStatement("SELECT player_id,price,pay_state,is_deliver,third_order_id FROM t_pay_order WHERE id=? LIMIT 1")) {
                s.setQueryTimeout(QUERY_SECONDS); s.setMaxRows(1); s.setLong(1, orderId);
                try (var r = s.executeQuery()) {
                    if (!r.next()) { return null; }
                    Object delivered = r.getObject(4);
                    return new LoginOrder(r.getLong(1), r.getInt(2), r.getInt(3), delivered == null ? null : r.getInt(4), r.getString(5));
                }
            }
        }
    }
}
