package cn.game.games.net.game.helper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import org.apache.ibatis.session.SqlSessionFactory;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.games.net.game.module.recharge.PayType;
import cn.game.util.JsonUtil;
import cn.game.util.SpringContextLoader;

/**
 * Game 同库不可覆盖履约凭证。先持久占位再发奖，完成凭证只随奖励聚合事务 INSERT。
 * 占位无租约、不以时间或进程代次过期；崩溃、部分奖励和 UNKNOWN 均需人工核对。
 * 唯一键约束订单和平台单号，不能替代玩家跨节点写入权；本组件不授权热记录清理。
 */
public final class PaymentDedupStore {
    /** 生产表名固定，不接受业务输入。 */
    private static final String CLAIM = "t_payment_fulfillment_claim", RECEIPT = "t_payment_fulfillment_receipt";
    /** 首次使用才解析现有 Game 工厂，关闭开关时不访问新表。 */
    private static final PaymentDedupStore shared = new PaymentDedupStore(
            () -> SpringContextLoader.getContext().getBean(SqlSessionFactory.class), CLAIM, RECEIPT);
    /** 同一 Game 数据源，不访问 Login 或本地归档。 */
    private final Supplier<SqlSessionFactory> factories;
    /** 只允许固定生产名或包内隔离测试名。 */
    private final String claimTable, receiptTable;
    /** 固定错误上下文，不输出凭据或完成正文。 */
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentDedupStore.class);

    /** @return 固定生产存储；是否启用由发货入口的启动开关决定 */
    public static PaymentDedupStore shared() { return shared; }

    /** @param factories Game 工厂 @param suffix 仅测试设施调用的随机十六进制后缀 */
    PaymentDedupStore(Supplier<SqlSessionFactory> factories, String suffix) {
        this(factories, testTable("claim", suffix), testTable("receipt", suffix));
    }

    /** @param factories Game 工厂 @param claimTable 已验证占位表名 @param receiptTable 已验证凭证表名 */
    private PaymentDedupStore(Supplier<SqlSessionFactory> factories, String claimTable, String receiptTable) {
        this.factories = Objects.requireNonNull(factories); this.claimTable = claimTable; this.receiptTable = receiptTable;
    }

    /** @param kind 固定测试表种类 @param suffix 随机后缀 @return 受控 SQL 标识符 */
    private static String testTable(String kind, String suffix) {
        if (suffix == null || !suffix.matches("[0-9a-f]{32}")) { throw new IllegalArgumentException("PAYMENT_TEST_TABLE_INVALID"); }
        return "s3d_" + kind + "_" + suffix;
    }

    /** 完整购买参数的版本化快照，子参数保序且不借用可变集合。 */
    public record Intent(int version, long playerId, long orderId, String sdkOrderId, int rmb, PayType payType,
            int payId, int sdkGoodsId, List<Integer> paySubIds, int addId, int addCount) {
        /** 只接受当前版本和完整身份；金额单位仍为原 PayItem 的元。 */
        public Intent {
            if (version != 1 || playerId <= 0 || orderId <= 0 || sdkOrderId == null || sdkOrderId.isBlank()
                    || sdkOrderId.length() > 191 || rmb <= 0 || payType == null || paySubIds == null) {
                throw new IllegalArgumentException("PAYMENT_DURABLE_INTENT_INVALID");
            }
            paySubIds = List.copyOf(paySubIds);
        }
    }

    /** 不可变占位身份，token 只属于本次成功 INSERT，不从旧记录接管。 */
    public record Claim(PaymentDedupStore store, Intent intent, String token) {}
    /** 只随封存参数传给写入器；不序列化到 Player 的持久模块图。 */
    public record Completion(Claim claim, String proof, String proofSha256) {}
    /** 查询结果明确区分不存在和已占位；查询异常不转换为 ABSENT。 */
    public enum Lookup { ABSENT, CLAIMED, COMPLETED }

    /** 只读诊断识别未来receipt版本；继承原失败类型，发货和清理仍拒绝未知版本。 */
    public static final class UnsupportedReceiptVersion extends IllegalStateException {
        /** 固定脱敏错误，不包含存储正文。 */
        private UnsupportedReceiptVersion() { super("PAYMENT_DURABLE_UNSUPPORTED_RECEIPT_VERSION"); }
    }

    /** @param playerId 权威玩家 @param item 可信热订单 @param sdk 本次平台单号 @return 全参数身份 */
    public static Intent intent(long playerId, PayItem item, String sdk) {
        return new Intent(1, playerId, item.getOrderId(), sdk, item.getRmb(), item.getPayType(), item.getPayId(),
                item.getSdkGoodsId(), item.getPaySubIds(), item.getAddId(), item.getAddCount());
    }

    /**
     * 新连接冷读完成凭证和占位，两表均必须可用；完成正文必须有匹配 V1 原证明。
     * @param playerId 权威玩家 @param orderId 通知订单 @param sdk 通知平台单号
     * @param hot 可选热订单，存在时必须全参数一致 @return 可判定状态；冲突、损坏及未知版本抛错
     * @throws SQLException 存储不可用，调用方必须拒绝发货
     */
    public Lookup lookup(long playerId, long orderId, String sdk, PayItem hot) throws SQLException {
        if (playerId <= 0 || orderId <= 0 || sdk == null || sdk.isBlank() || sdk.length() > 191) {
            throw new IllegalArgumentException("PAYMENT_DURABLE_IDENTITY_INVALID");
        }
        try (Connection c = connection()) {
            String claimIntent = null, token = null;
            try (var s = c.prepareStatement("SELECT intent,token,player_id,order_id,sdk_order_id FROM " + claimTable + " WHERE order_id=? OR sdk_order_id=?")) {
                s.setLong(1, orderId); s.setString(2, sdk); s.setQueryTimeout(5);
                try (var r = s.executeQuery()) {
                    if (r.next()) {
                        claimIntent = r.getString(1); token = r.getString(2);
                        validateIntent(claimIntent, playerId, orderId, sdk, hot);
                        if (r.getLong(3) != playerId || r.getLong(4) != orderId || !sdk.equals(r.getString(5))
                                || token == null || !token.matches("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}") || r.next()) { throw invalid(); }
                    }
                }
            }
            try (var s = c.prepareStatement("SELECT version,player_id,order_id,sdk_order_id,intent,token,proof,proof_sha256 FROM "
                    + receiptTable + " WHERE order_id=? OR sdk_order_id=?")) {
                s.setLong(1, orderId); s.setString(2, sdk); s.setQueryTimeout(5);
                try (var r = s.executeQuery()) {
                    if (!r.next()) { return claimIntent == null ? Lookup.ABSENT : Lookup.CLAIMED; }
                    if (r.getInt(1) != 1 || r.getLong(2) != playerId || r.getLong(3) != orderId || !sdk.equals(r.getString(4))
                            || claimIntent == null || !claimIntent.equals(r.getString(5)) || !token.equals(r.getString(6))) { throw invalid(); }
                    Intent intent = validateIntent(r.getString(5), playerId, orderId, sdk, hot);
                    String proof = r.getString(7);
                    if (proof == null || !digest(proof).equals(r.getString(8))) { throw invalid(); }
                    validateProof(intent, proof);
                    if (r.next()) { throw invalid(); }
                    return Lookup.COMPLETED;
                }
            }
        }
    }

    /** @param json 存储身份正文 @param playerId 当前玩家 @param orderId 本单 @param sdk 本平台单 @param hot 可选热记录
     * @return 已严格核对的当前版本身份 */
    private static Intent validateIntent(String json, long playerId, long orderId, String sdk, PayItem hot) {
        Intent value = JsonUtil.parseObject(json, Intent.class);
        if (value.playerId() != playerId || value.orderId() != orderId || !value.sdkOrderId().equals(sdk)
                || !JsonUtil.toJsonString(value).equals(json) || (hot != null && !value.equals(intent(playerId, hot, sdk)))) { throw invalid(); }
        return value;
    }

    /**
     * 运维冷读复用发货链路的严格身份及完成正文校验；只执行两条有界 SELECT，不调用 Spring、Player 或写入器。
     * @param c 调用方拥有的 Game 只读连接 @param playerId 显式玩家 @param expected 已认证归档的完整 PayItem
     * @return 两表局部观察状态，不代表原子快照、平台终态或清理授权
     * @throws SQLException 查询失败，不得解释为不存在
     */
    public static Lookup observe(Connection c, long playerId, PayItem expected) throws SQLException {
        return observe(c, CLAIM, RECEIPT, playerId, expected);
    }

    /** @param c 隔离测试 Game 连接 @param suffix 本轮随机表后缀 @param playerId 玩家 @param expected 归档订单 @return 真实只读观察 */
    static Lookup observe(Connection c, String suffix, long playerId, PayItem expected) throws SQLException {
        return observe(c, testTable("claim", suffix), testTable("receipt", suffix), playerId, expected);
    }

    /** @param playerId 玩家 @param item 普通 JSON 映射的订单，不执行初始化 @return 与发货存储完全相同的完整身份校验 */
    public static Intent validateCompletedItem(long playerId, PayItem item) {
        Intent intent = intent(playerId, item, item.getSdkOrderId());
        validateProof(intent, JsonUtil.toJsonString(item));
        return intent;
    }

    /** @param c Game只读连接 @param claims 固定或测试表 @param receipts 固定或测试表 @param playerId 玩家 @param expected 归档订单 @return 严格局部观察 */
    private static Lookup observe(Connection c, String claims, String receipts, long playerId, PayItem expected) throws SQLException {
        Intent wanted = validateCompletedItem(playerId, expected);
        String json = null, token = null;
        // 在服务端限制 LONGTEXT 读取量；LIMIT 3 足够识别订单键和平台键的歧义，不扫描或回填。
        try (var s = c.prepareStatement("SELECT CASE WHEN OCTET_LENGTH(intent)<=4194304 THEN intent END,token,player_id,order_id,sdk_order_id FROM "
                + claims + " WHERE order_id=? OR sdk_order_id=? LIMIT 3")) {
            s.setLong(1, wanted.orderId()); s.setString(2, wanted.sdkOrderId()); s.setQueryTimeout(2); s.setMaxRows(3);
            try (var r = s.executeQuery()) {
                if (r.next()) {
                    json = r.getString(1); token = r.getString(2);
                    if (json == null || !wanted.equals(validateIntent(json, playerId, wanted.orderId(), wanted.sdkOrderId(), expected))
                            || r.getLong(3) != playerId || r.getLong(4) != wanted.orderId() || !wanted.sdkOrderId().equals(r.getString(5))
                            || token == null || !token.matches("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}") || r.next()) { throw invalid(); }
                }
            }
        }
        try (var s = c.prepareStatement("SELECT version,player_id,order_id,sdk_order_id,CASE WHEN OCTET_LENGTH(intent)<=4194304 THEN intent END,token,"
                + "CASE WHEN OCTET_LENGTH(proof)<=4194304 THEN proof END,proof_sha256 FROM " + receipts + " WHERE order_id=? OR sdk_order_id=? LIMIT 3")) {
            s.setLong(1, wanted.orderId()); s.setString(2, wanted.sdkOrderId()); s.setQueryTimeout(2); s.setMaxRows(3);
            try (var r = s.executeQuery()) {
                if (!r.next()) { return json == null ? Lookup.ABSENT : Lookup.CLAIMED; }
                if (r.getInt(1) > 1) { throw new UnsupportedReceiptVersion(); }
                if (r.getInt(1) != 1 || r.getLong(2) != playerId || r.getLong(3) != wanted.orderId() || !wanted.sdkOrderId().equals(r.getString(4))
                        || json == null || !json.equals(r.getString(5)) || !token.equals(r.getString(6))) { throw invalid(); }
                String proof = r.getString(7);
                if (proof == null || !digest(proof).equals(r.getString(8))) { throw invalid(); }
                validateProof(wanted, proof);
                // 发货去重只需购买身份一致；清理预检还必须与归档的完成时间、阶段和全部证明完全一致。
                if (!JsonUtil.toJsonString(expected).equals(proof) || r.next()) { throw invalid(); }
                return Lookup.COMPLETED;
            }
        }
    }

    /**
     * 先提交不可覆盖占位，再允许奖励；唯一键竞争失败不能借用胜者 token 重试。
     * @param intent 完整购买身份 @return 本次占位；异常或超时均不可执行奖励
     * @throws SQLException INSERT 或提交未确认
     */
    public Claim claim(Intent intent) throws SQLException {
        String token = UUID.randomUUID().toString();
        try (Connection c = connection()) {
            c.setAutoCommit(false);
            try (var s = c.prepareStatement("INSERT INTO " + claimTable + " (order_id,sdk_order_id,player_id,intent,token) VALUES (?,?,?,?,?)")) {
                s.setLong(1, intent.orderId()); s.setString(2, intent.sdkOrderId()); s.setLong(3, intent.playerId());
                s.setString(4, JsonUtil.toJsonString(intent)); s.setString(5, token); s.setQueryTimeout(5);
                if (s.executeUpdate() != 1) { throw invalid(); }
                c.commit();
                return new Claim(this, intent, token);
            } catch (SQLException | RuntimeException failure) {
                log.warn("支付占位未确认，禁止奖励 orderId={}", intent.orderId(), failure);
                try { c.rollback(); }
                catch (SQLException rollbackFailure) {
                    log.warn("支付占位回滚未确认，不允许重放 orderId={}", intent.orderId(), rollbackFailure);
                    failure.addSuppressed(rollbackFailure);
                }
                throw failure;
            }
        }
    }

    /** @param claim 本次占位 @param item 本次完整奖励后的热订单 @return 不可变完成正文；尚不代表已提交 */
    public static Completion completed(Claim claim, PayItem item) {
        String proof = JsonUtil.toJsonString(item);
        validateProof(claim.intent(), proof);
        return new Completion(claim, proof, digest(proof));
    }

    /** @param intent 原完整身份 @param proof 原 PayItem 及 V1 完成证明；拒绝历史 finish 和不完整阶段 */
    private static void validateProof(Intent intent, String proof) {
        PayItem item = JsonUtil.parseObject(proof, PayItem.class);
        if (!intent.equals(intent(intent.playerId(), item, item.getSdkOrderId()))
                || item.getFinishTime() <= 0 || !JsonUtil.toJsonString(item).equals(proof)
                || !item.hasCommittedCompletionProof(intent.playerId(), intent.sdkOrderId())) { throw invalid(); }
    }

    /**
     * 仅由专用快照写入器在同一 Game 事务、玩家 UPDATE 后调用；不自行提交。
     * @param c 玩家 Mapper 的同一非自动提交连接 @param factory 该连接的工厂 @param completion 封存完成正文
     * @throws SQLException 任意占位/唯一键/插入故障必须使整个玩家事务失败
     */
    void insertCompletion(Connection c, SqlSessionFactory factory, Completion completion) throws SQLException {
        if (c.getAutoCommit() || factory.getConfiguration().getEnvironment().getDataSource()
                != factories.get().getConfiguration().getEnvironment().getDataSource() || completion.claim().store() != this) { throw invalid(); }
        Claim claim = completion.claim(); Intent intent = claim.intent();
        // 占位只追加且无租约，读取身份无需 UPDATE 权限；完成表唯一 INSERT 才是提交竞争的裁决。
        try (var s = c.prepareStatement("SELECT intent,token,player_id,sdk_order_id FROM " + claimTable + " WHERE order_id=?")) {
            s.setLong(1, intent.orderId()); s.setQueryTimeout(5);
            try (var r = s.executeQuery()) {
                if (!r.next() || !JsonUtil.toJsonString(intent).equals(r.getString(1)) || !claim.token().equals(r.getString(2))
                        || r.getLong(3) != intent.playerId() || !intent.sdkOrderId().equals(r.getString(4))) { throw invalid(); }
            }
        }
        validateProof(intent, completion.proof());
        if (!digest(completion.proof()).equals(completion.proofSha256())) { throw invalid(); }
        try (var s = c.prepareStatement("INSERT INTO " + receiptTable
                + " (order_id,sdk_order_id,player_id,version,intent,token,proof,proof_sha256) VALUES (?,?,?,?,?,?,?,?)")) {
            s.setLong(1, intent.orderId()); s.setString(2, intent.sdkOrderId()); s.setLong(3, intent.playerId()); s.setInt(4, 1);
            s.setString(5, JsonUtil.toJsonString(intent)); s.setString(6, claim.token()); s.setString(7, completion.proof());
            s.setString(8, completion.proofSha256()); s.setQueryTimeout(5);
            if (s.executeUpdate() != 1) { throw invalid(); }
        }
    }

    /** @return 新的 Game 连接，生命周期只属于本次查询或占位 @throws SQLException 建连失败 */
    private Connection connection() throws SQLException {
        Connection c = PaymentDatabaseBoundary.open(factories.get().getConfiguration().getEnvironment().getDataSource(), PaymentDatabaseBoundary.Limits.configured());
        try {
            verifySchema(c);
            return c;
        } catch (SQLException | RuntimeException failure) {
            log.warn("支付持久凭证依赖不可用，拒绝发货", failure);
            try { c.close(); }
            catch (SQLException closeFailure) { log.warn("支付依赖检查连接关闭失败", closeFailure); failure.addSuppressed(closeFailure); }
            throw failure;
        }
    }

    /** @param c 调用方拥有的Game只读连接 @return 固定生产三表的V1结构观察，不授予启用或清理权限 @throws SQLException 元数据不可读 */
    public static PaymentDedupSchema.Report inspectSchema(Connection c) throws SQLException {
        return PaymentDedupSchema.inspect(c, "t_player_data", CLAIM, RECEIPT);
    }

    /** @param c Game 连接；奖励前逐次检查完整V1持久化契约，无成功缓存 @throws SQLException 依赖缺失或错误 */
    private void verifySchema(Connection c) throws SQLException {
        PaymentDedupSchema.inspect(c, "t_player_data", claimTable, receiptTable).requireValid();
    }

    /** @param value 原始完成正文 @return 原字节 SHA-256；摘要不提供独立真实性授权 */
    private static String digest(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException failure) { log.error("JVM缺少支付凭证摘要算法", failure); throw new ExceptionInInitializerError(failure); }
    }

    /** @return 不含订单正文或连接凭据的固定错误 */
    private static IllegalStateException invalid() { return new IllegalStateException("PAYMENT_DURABLE_EVIDENCE_CONFLICT"); }
}
