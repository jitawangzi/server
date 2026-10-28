package cn.game.games.diagnostics;

import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.JsonNode;
import cn.game.games.net.game.helper.PaymentDedupStore;
import cn.game.games.net.game.module.recharge.PayItem;

/**
 * 历史订单显式清单盘点。只读取指定主键，不发现玩家、不初始化Player、不执行任何业务回调。
 * 普通树及完成证明复用封存/清理的严格校验；跨库和逐页观察没有原子性，所有分类仅供人工核查。
 * 不接收归档输入；缺失热订单不能从归档或完成回执推断历史内容，更不授予补建、清理或重发权限。
 */
public final class PaymentLegacyInventoryCli {
    /** 输入清单最多100项；每页最多10项，每项正文仍受既有4MiB边界约束。 */
    public static final int MAX_SCOPE = 100, MAX_PAGE = 10;
    /** 与既有只读数据库工具相同的共享总预算，配置/解析/连接/查询均消耗该预算。 */
    public static final int TOTAL_MILLIS = 12000;
    /** 分类固定枚举，禁止把异常正文、SDK单号或原始字段作为报告键。 */
    public enum Classification {
        VALID_V1_COMPLETED_HOT_WITHOUT_NEW_RECEIPT, HISTORICAL_FINISH_WITHOUT_V1_PROOF,
        UNFINISHED_PENDING_OR_UNKNOWN, CONFLICTING_IDENTITY, UNSUPPORTED_FUTURE_VERSION,
        VALID_CURRENT_DURABLE_COMPLETED_RECEIPT_AND_CLAIM, DURABLE_CLAIM_WITHOUT_COMPLETED_RECEIPT,
        CORRUPT_OR_UNREADABLE, MISSING_DEPENDENCY
    }
    /** 单个显式主键组合，仅在内存用于查询，不输出明细。 */
    private record Entry(long player, long order) {}
    /** 有界清单及绑定清单摘要的偏移，偏移指下一条尚未成功观察的输入。 */
    private record Request(List<Entry> entries, int size, int offset, String digest) {}
    /** 完整观察和失败尝试分开；故障项不消耗游标，重试不会遗漏。 */
    private record Finding(Classification classification, String reason) {}
    /** 工具类不创建实例。 */
    private PaymentLegacyInventoryCli() {}

    /** @param args pairs pageSize cursor；输出固定聚合JSON，0页完成、1非法输入、2读取不完整 */
    public static void main(String[] args) throws Exception {
        Map<String, Object> result = run(args);
        PaymentArchiveEvidence.JSON.writeValue(new java.io.FileOutputStream(java.io.FileDescriptor.out), result);
        System.exit(exitCode(result));
    }

    /** @param result 本工具报告 @return 0仅表示本页完成，不表示业务资格；1输入错误；2部分或读取失败 */
    public static int exitCode(Map<String, Object> result) {
        return "INVALID_INPUT".equals(result.get("status")) ? 1 : "PAGE_COMPLETE".equals(result.get("status")) ? 0 : 2;
    }

    /** @param args 最多100个player:order逗号清单、页大小、初始-或报告的nextCursor @return 恒无写入授权的聚合报告 */
    public static Map<String, Object> run(String[] args) { return runWithFactory(args, PaymentLegacyInventoryCli::jdbcReader, System::nanoTime); }

    /** @param args 与生产相同参数 @param reader 只读边界 @return 同一校验和分页链路的聚合报告 */
    public static Map<String, Object> run(String[] args, PaymentCleanupCli.Reader reader) {
        return runWithFactory(args, started -> reader, System::nanoTime);
    }

    /** 配置读取也在本轮截止点之内，测试可注入建连失败而不初始化业务环境。 */
    interface Factory {
        /** @param started 整轮单调起点 @return 只读读取器 @throws Exception 配置或连接设施失败 */
        PaymentCleanupCli.Reader open(long started) throws Exception;
    }

    /** @param args 显式清单参数 @param factory 只读依赖 @param clock 单调纳秒时钟 @return 有界、安全报告 */
    static Map<String, Object> runWithFactory(String[] args, Factory factory, java.util.function.LongSupplier clock) {
        long started = clock.getAsLong();
        Request request;
        try { request = parse(args); }
        catch (Exception failure) {
            PaymentArchiveStore.safeWarn(failure);
            var invalid = base(); invalid.put("status", "INVALID_INPUT"); invalid.put("reason", "EXPLICIT_BOUNDED_UNIQUE_SCOPE_AND_VALID_CURSOR_REQUIRED");
            return invalid;
        }
        var counts = new LinkedHashMap<String, Integer>();
        for (Classification value : Classification.values()) { counts.put(value.name(), 0); }
        var reasons = new LinkedHashMap<String, Integer>();
        int observed = 0, attempted = 0;
        int end = Math.min(request.entries().size(), request.offset() + request.size());
        String failureReason = null;
        // 空清单非法；已到清单末尾的游标是合法空页，不能为它连接数据库。
        if (request.offset() < end) {
            try {
                checkTime(started, clock);
                PaymentCleanupCli.Reader reader = factory.open(started);
                for (int index = request.offset(); index < end; index++) {
                    checkTime(started, clock); attempted++;
                    Finding finding = inspect(request.entries().get(index), reader, started, clock);
                    checkTime(started, clock);
                    counts.merge(finding.classification().name(), 1, Integer::sum);
                    reasons.merge(finding.reason(), 1, Integer::sum); observed++;
                }
            } catch (Exception failure) {
                PaymentArchiveStore.safeWarn(failure);
                if (failure instanceof InterruptedException) { Thread.currentThread().interrupt(); }
                failureReason = failure instanceof SQLTimeoutException ? "OBSERVATION_TIMEOUT"
                        : failure instanceof SQLException ? "CONNECTION_OR_QUERY_FAILED" : "CONFIG_OR_READER_FAILED";
                // 失败尝试单独计数，不把未读到的记录计为缺失或空页，不推进游标。
                reasons.merge(failureReason, 1, Integer::sum);
            }
        }
        var result = base();
        int next = request.offset() + observed;
        result.put("status", failureReason == null ? "PAGE_COMPLETE" : "PARTIAL_READ");
        result.put("scopeEntries", request.entries().size()); result.put("pageStart", request.offset());
        result.put("pageSize", request.size()); result.put("pageRequested", end - request.offset());
        result.put("attempted", attempted); result.put("observed", observed);
        result.put("unreadableAttempts", failureReason == null || attempted == observed ? 0 : 1);
        result.put("dependencyOpenFailed", failureReason != null && attempted == 0);
        result.put("counts", counts); result.put("reasons", reasons);
        result.put("nextOffset", next); result.put("scopeExhausted", next == request.entries().size());
        result.put("nextCursor", request.digest() + ":" + next);
        result.put("coverage", "SORTED_EXPLICIT_INPUT_POSITIONS_ONLY_NO_DATABASE_COMPLETENESS");
        return result;
    }

    /** @param started 整轮起点 @param clock 单调时钟 @throws SQLTimeoutException 预算不足，不启动后续读取 */
    private static void checkTime(long started, java.util.function.LongSupplier clock) throws SQLTimeoutException {
        if (clock.getAsLong() - started >= TimeUnit.MILLISECONDS.toNanos(TOTAL_MILLIS)) { throw new SQLTimeoutException("INVENTORY_TOTAL_EXPIRED"); }
        if (Thread.currentThread().isInterrupted()) { throw new SQLTimeoutException("INVENTORY_INTERRUPTED"); }
    }

    /** @param args 显式参数 @return 排序后清单及绑定输入的游标；拒绝重复订单及溢出 */
    private static Request parse(String[] args) throws Exception {
        if (args == null || args.length != 3 || args[0] == null || args[0].length() > 4000
                || args[1] == null || !args[1].matches("[1-9][0-9]?")) { throw new IllegalArgumentException("SCOPE_INVALID"); }
        String[] pairs = args[0].split(",", -1);
        int size = Integer.parseInt(args[1]);
        if (pairs.length == 0 || pairs.length > MAX_SCOPE || size > MAX_PAGE) { throw new IllegalArgumentException("SCOPE_INVALID"); }
        var entries = new ArrayList<Entry>(); var orders = new java.util.HashSet<Long>();
        for (String pair : pairs) {
            if (!pair.matches("[1-9][0-9]{0,18}:[1-9][0-9]{0,18}")) { throw new IllegalArgumentException("SCOPE_INVALID"); }
            String[] ids = pair.split(":"); long player = Long.parseLong(ids[0]), order = Long.parseLong(ids[1]);
            if (!orders.add(order)) { throw new IllegalArgumentException("SCOPE_DUPLICATE_ORDER"); }
            entries.add(new Entry(player, order));
        }
        entries.sort(Comparator.comparingLong(Entry::player).thenComparingLong(Entry::order));
        String canonical = entries.stream().map(e -> e.player() + ":" + e.order()).collect(java.util.stream.Collectors.joining(","));
        String digest = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                .digest(("PAYMENT-INVENTORY-V1\n" + canonical).getBytes(java.nio.charset.StandardCharsets.US_ASCII)));
        int offset = 0;
        if (!"-".equals(args[2])) {
            if (args[2] == null || !args[2].matches("[0-9a-f]{64}:(0|[1-9][0-9]{0,2})") || !args[2].startsWith(digest + ":")) {
                throw new IllegalArgumentException("CURSOR_INVALID");
            }
            offset = Integer.parseInt(args[2].substring(65));
            if (offset > entries.size()) { throw new IllegalArgumentException("CURSOR_INVALID"); }
        }
        return new Request(List.copyOf(entries), size, offset, digest);
    }

    /** @param entry 明确主键 @param reader 有界只读源 @param started 共享起点 @param clock 时钟 @return 完整观察；读取错误抛出不当作缺失 */
    private static Finding inspect(Entry entry, PaymentCleanupCli.Reader reader, long started, java.util.function.LongSupplier clock) throws Exception {
        var game = reader.gameSnapshot(entry.player()); checkTime(started, clock);
        var login = reader.login(entry.order()); checkTime(started, clock);
        if (game == null) { return finding(Classification.MISSING_DEPENDENCY, "GAME_PLAYER_MISSING"); }
        if (game.utf8Bytes() > PaymentEvidenceCli.MAX_BYTES) { return finding(Classification.CORRUPT_OR_UNREADABLE, "GAME_INPUT_LIMIT"); }
        JsonNode item;
        try {
            String raw = game.modules();
            PaymentArchiveEvidence.require(raw != null && game.utf8Bytes() >= 0 && raw.length() <= PaymentEvidenceCli.MAX_BYTES
                    && cn.game.games.net.game.helper.PaymentCapacityMonitor.utf8Bytes(raw) == game.utf8Bytes());
            JsonNode root = PaymentArchiveEvidence.unwrap(PaymentArchiveEvidence.JSON.readTree(raw));
            PaymentArchiveEvidence.require(root.isObject());
            JsonNode module = PaymentArchiveEvidence.unwrap(root.path(PaymentArchiveEvidence.MODULE));
            if (module.isMissingNode()) { return finding(Classification.MISSING_DEPENDENCY, "GAME_MODULE_MISSING"); }
            PaymentArchiveEvidence.require(module.isObject());
            JsonNode items = PaymentArchiveEvidence.unwrap(module.path("payItems"));
            PaymentArchiveEvidence.require(items.isObject());
            item = PaymentArchiveEvidence.unwrap(items.path(Long.toString(entry.order())));
            if (item.isMissingNode()) { return finding(Classification.MISSING_DEPENDENCY, "HOT_ORDER_MISSING_NOT_INFERRED"); }
            PaymentArchiveEvidence.require(item.isObject() && item.path("orderId").isIntegralNumber()
                    && item.path("orderId").canConvertToLong() && item.path("rmb").isInt() && item.path("rmb").intValue() > 0
                    && item.path("isFinish").isBoolean() && item.path("finishTime").isIntegralNumber() && item.path("finishTime").canConvertToLong());
        } catch (Exception failure) {
            PaymentArchiveStore.safeWarn(failure); return finding(Classification.CORRUPT_OR_UNREADABLE, "GAME_FORMAT_INVALID");
        }
        if (item.path("orderId").longValue() != entry.order()) { return finding(Classification.CONFLICTING_IDENTITY, "HOT_ORDER_ID_CONFLICT"); }
        if (login == null) { return finding(Classification.MISSING_DEPENDENCY, "LOGIN_ORDER_MISSING"); }
        if (login.playerId() != entry.player() || login.price() != item.path("rmb").longValue() * 100
                || (login.sdkOrderId() != null && !login.sdkOrderId().isBlank() && item.path("sdkOrderId").isTextual()
                && !login.sdkOrderId().equals(item.path("sdkOrderId").textValue()))) {
            return finding(Classification.CONFLICTING_IDENTITY, "LOGIN_IDENTITY_OR_AMOUNT_CONFLICT");
        }
        JsonNode proof = item.path("completionProof");
        if (proof.isObject() && proof.path("version").isIntegralNumber() && proof.path("version").bigIntegerValue().compareTo(java.math.BigInteger.ONE) > 0) {
            return finding(Classification.UNSUPPORTED_FUTURE_VERSION, "HOT_PROOF_FUTURE_VERSION");
        }
        if (proof.isObject() && ((proof.has("playerId") && (!proof.path("playerId").isIntegralNumber()
                || !proof.path("playerId").canConvertToLong() || proof.path("playerId").longValue() != entry.player()))
                || (proof.has("orderId") && !proof.path("orderId").equals(item.path("orderId"))))) {
            return finding(Classification.CONFLICTING_IDENTITY, "PROOF_IDENTITY_CONFLICT");
        }
        try {
            // 复用归档的严格字段、数字、购买参数和身份校验，使用已经捕获的观察，绝不再读两库。
            PaymentArchiveEvidence.collect(entry.player(), entry.order(), new PaymentEvidenceCli.Reader() {
                @Override public String game(long id) { return game.modules(); }
                @Override public PaymentEvidenceCli.ModulesSnapshot gameSnapshot(long id) { return game; }
                @Override public PaymentEvidenceCli.LoginOrder login(long id) { return login; }
            });
        } catch (Exception failure) {
            PaymentArchiveStore.safeWarn(failure); return finding(Classification.CORRUPT_OR_UNREADABLE, "ORDER_SCHEMA_OR_PROOF_INVALID");
        }
        String state = item.path("completionState").asText("");
        if (!item.path("isFinish").booleanValue() || (login.payState() != 1 && login.payState() != 2)
                || login.delivered() == null || (login.delivered() != 0 && login.delivered() != 1)
                || (!state.isEmpty() && !"REWARDS_COMPLETED".equals(state)) || login.payState() != 2 || login.delivered() != 1) {
            return finding(Classification.UNFINISHED_PENDING_OR_UNKNOWN, "NON_TERMINAL_OR_UNKNOWN_LOCAL_STATE");
        }
        if (proof.isMissingNode() || proof.isNull()) { return finding(Classification.HISTORICAL_FINISH_WITHOUT_V1_PROOF, "FINISH_ALONE_NOT_V1_EVIDENCE"); }
        PayItem completed;
        try { completed = PaymentCleanupCli.completedItem(entry.player(), item); }
        catch (Exception failure) {
            PaymentArchiveStore.safeWarn(failure); return finding(Classification.CORRUPT_OR_UNREADABLE, "STRICT_V1_COMPLETION_INVALID");
        }
        checkTime(started, clock);
        try {
            var durable = reader.durable(entry.player(), completed); checkTime(started, clock);
            if (durable == null) { throw new SQLException("INVALID_DURABLE_READER_RESULT"); }
            return switch (durable) {
                case ABSENT -> finding(Classification.VALID_V1_COMPLETED_HOT_WITHOUT_NEW_RECEIPT, "CURRENT_DURABLE_TABLES_OBSERVED_NO_ROWS");
                case CLAIMED -> finding(Classification.DURABLE_CLAIM_WITHOUT_COMPLETED_RECEIPT, "CLAIM_ONLY_REQUIRES_MANUAL_EVIDENCE");
                case COMPLETED -> finding(Classification.VALID_CURRENT_DURABLE_COMPLETED_RECEIPT_AND_CLAIM, "STRICT_CURRENT_DURABLE_COMPLETION_OBSERVED");
            };
        } catch (PaymentDedupStore.UnsupportedReceiptVersion failure) {
            PaymentArchiveStore.safeWarn(failure); return finding(Classification.UNSUPPORTED_FUTURE_VERSION, "DURABLE_RECEIPT_FUTURE_VERSION");
        } catch (SQLException failure) {
            if (failure.getErrorCode() == 1146 && "42S02".equals(failure.getSQLState())) {
                PaymentArchiveStore.safeWarn(failure); return finding(Classification.MISSING_DEPENDENCY, "DURABLE_TABLE_MISSING_DEFAULT_OFF_EXPECTED");
            }
            throw failure;
        } catch (IllegalArgumentException | IllegalStateException failure) {
            PaymentArchiveStore.safeWarn(failure); return finding(Classification.CORRUPT_OR_UNREADABLE, "DURABLE_IDENTITY_VERSION_OR_PROOF_CONFLICT");
        }
    }

    /** @param classification 固定分类 @param reason 固定原因 @return 无原始正文的内部结果 */
    private static Finding finding(Classification classification, String reason) { return new Finding(classification, reason); }

    /** @return 每条报告均包含外部阻断，局部一致也不产生可补建或可清理名单 */
    private static LinkedHashMap<String, Object> base() {
        var out = new LinkedHashMap<String, Object>();
        out.put("canDelete", false); out.put("canResend", false); out.put("canBackfill", false);
        out.put("candidateMeaning", "NEEDS_MANUAL_EVIDENCE_REVIEW_ONLY");
        out.put("observation", "MULTI_CONNECTION_NON_ATOMIC_TOCTOU_NOT_AUTHORIZATION");
        out.put("platformTerminal", "NOT_PROVEN"); out.put("writerAndUnknownFencing", "NOT_PROVEN");
        out.put("requiredNextEvidence", List.of("AUTHENTICATED_PLATFORM_TERMINAL_EVIDENCE", "OLD_WRITER_AND_UNKNOWN_FENCING",
                "FUTURE_ATOMIC_REVALIDATION_AND_SEPARATE_WRITE_AUTHORIZATION"));
        out.put("limits", Map.of("scope", MAX_SCOPE, "page", MAX_PAGE, "totalMillis", TOTAL_MILLIS,
                "modulesBytesPerEntry", PaymentEvidenceCli.MAX_BYTES, "maxConnectionsPerPage", MAX_PAGE * 3,
                "maxSelectsPerPage", MAX_PAGE * 4, "reportBytes", 12000));
        return out;
    }

    /** @param started 整轮起点 @return 复用现有URL、超时、只读和严格持久观察；配置必须是当前身份私有绝对无链接文件 */
    private static PaymentCleanupCli.Reader jdbcReader(long started) throws Exception {
        byte[] bytes = PaymentArchiveStore.boundedRead(Path.of(System.getenv("S2_DB_CONFIG")), 65536);
        var properties = new Properties();
        try (var input = new java.io.ByteArrayInputStream(bytes)) { properties.load(input); }
        finally { java.util.Arrays.fill(bytes, (byte) 0); }
        var source = new PaymentEvidenceCli.JdbcReader(properties, started);
        return new PaymentCleanupCli.Reader() {
            @Override public String game(long id) throws Exception { return source.game(id); }
            @Override public PaymentEvidenceCli.ModulesSnapshot gameSnapshot(long id) throws Exception { return source.gameSnapshot(id); }
            @Override public PaymentEvidenceCli.LoginOrder login(long id) throws Exception { return source.login(id); }
            @Override public PaymentDedupStore.Lookup durable(long player, PayItem item) throws Exception {
                try (var connection = source.connect("")) { return PaymentDedupStore.observe(connection, player, item); }
            }
        };
    }
}
