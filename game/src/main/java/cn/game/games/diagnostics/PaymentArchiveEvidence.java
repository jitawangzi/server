package cn.game.games.diagnostics;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 从既有有界读取器提取单笔订单，保留普通 JSON 树而不构造 Player 或加载包装类型。
 * 封存证明本地观察内容未变，不证明平台真实性、事务终态或删除/补发安全。
 */
final class PaymentArchiveEvidence {
    /** 固定模块名只作为树键，不用于反射或多态加载。 */
    static final String MODULE = "cn.game.games.net.game.module.player.PlayerModule";
    /** 独立解析器限制深度和数值长度，重复键及尾随 JSON 一律失败。 */
    static final ObjectMapper JSON = new ObjectMapper(JsonFactory.builder().streamReadConstraints(
            StreamReadConstraints.builder().maxNestingDepth(64).maxNumberLength(32)
                    .maxStringLength(PaymentEvidenceCli.MAX_BYTES).build()).build())
            .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
            .enable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    /** 原诊断中只有这些读取结果可形成风险封存，读取故障及歧义不能成为成功回执。 */
    private static final Set<String> GAME_STATES = Set.of("PERSISTED_V1_PROOF_CONSISTENT", "NO_COMPLETION_PROOF",
            "UNKNOWN_PROOF_VERSION", "MALFORMED_PROOF", "PROOF_OR_IDENTITY_CONFLICT");

    /** 工具类不创建实例。 */
    private PaymentArchiveEvidence() {}

    /** @param player 显式玩家 @param order 显式订单 @param reader 单次有界读取器 @return 仅单订单的版本化内容 */
    static ObjectNode collect(long player, long order, PaymentEvidenceCli.Reader reader) throws Exception {
        PaymentEvidenceCli.ModulesSnapshot[] game = new PaymentEvidenceCli.ModulesSnapshot[1];
        PaymentEvidenceCli.LoginOrder[] login = new PaymentEvidenceCli.LoginOrder[1];
        // 由原诊断统一执行一次两库观察及分类，捕获同一次读取的正文，禁止二次读取混合证据。
        var observed = PaymentEvidenceCli.inspect(player, order, new PaymentEvidenceCli.Reader() {
            @Override public String game(long id) throws Exception { return reader.game(id); }
            @Override public PaymentEvidenceCli.ModulesSnapshot gameSnapshot(long id) throws Exception {
                return game[0] = reader.gameSnapshot(id);
            }
            @Override public PaymentEvidenceCli.LoginOrder login(long id) throws Exception { return login[0] = reader.login(id); }
        });
        if (!GAME_STATES.contains(String.valueOf(observed.get("game")))
                || !Set.of("OBSERVED", "UNKNOWN_STATE", "ORDER_NOT_FOUND").contains(String.valueOf(observed.get("login")))) {
            throw new IllegalArgumentException("SOURCE_NOT_ARCHIVABLE");
        }
        JsonNode root = unwrap(JSON.readTree(game[0].modules()));
        JsonNode item = unwrap(unwrap(unwrap(root.path(MODULE)).path("payItems")).path(Long.toString(order)));
        ObjectNode content = JSON.createObjectNode();
        content.put("playerId", player); content.put("orderId", order);
        content.put("source", "OFFLINE_STORAGE_NON_ATOMIC");
        content.put("gameSource", "GAME.t_player_data.modules/PlayerModule.payItems");
        content.put("loginSource", "LOGIN.t_pay_order");
        content.put("platformAuthenticity", "NOT_PROVEN"); content.put("transactionResolution", "NOT_PROVEN");
        content.put("canDelete", false); content.put("canResend", false);
        for (String time : new String[] {"gameReadStartedAt", "gameReadEndedAt", "loginReadStartedAt", "loginReadEndedAt"}) {
            content.put(time, ((Number) observed.get(time)).longValue());
        }
        content.set("payItem", item.deepCopy());
        content.set("loginOrder", JSON.valueToTree(login[0]));
        content.set("assessment", assess(player, order, item, login[0]));
        validate(content, player, order);
        return content;
    }

    /** @param content 已认证或待封存内容 @param player 期望玩家 @param order 期望订单；任何身份冲突都拒绝 */
    static void validate(JsonNode content, long player, long order) throws Exception {
        Set<String> fields = Set.of("playerId", "orderId", "source", "gameSource", "loginSource", "platformAuthenticity",
                "transactionResolution", "canDelete", "canResend", "gameReadStartedAt", "gameReadEndedAt",
                "loginReadStartedAt", "loginReadEndedAt", "payItem", "loginOrder", "assessment");
        require(content.isObject() && names(content).equals(fields));
        require(player > 0 && order > 0 && integer(content.path("playerId"), player) && integer(content.path("orderId"), order));
        require(content.path("source").asText().equals("OFFLINE_STORAGE_NON_ATOMIC")
                && content.path("gameSource").asText().equals("GAME.t_player_data.modules/PlayerModule.payItems")
                && content.path("loginSource").asText().equals("LOGIN.t_pay_order")
                && content.path("platformAuthenticity").asText().equals("NOT_PROVEN")
                && content.path("transactionResolution").asText().equals("NOT_PROVEN")
                && content.path("canDelete").isBoolean() && !content.path("canDelete").booleanValue()
                && content.path("canResend").isBoolean() && !content.path("canResend").booleanValue());
        long previous = 0;
        for (String field : new String[] {"gameReadStartedAt", "gameReadEndedAt", "loginReadStartedAt", "loginReadEndedAt"}) {
            JsonNode time = content.path(field);
            require(time.isIntegralNumber() && time.canConvertToLong() && time.longValue() >= previous);
            previous = time.longValue();
        }
        JsonNode item = content.path("payItem");
        require(item.isObject() && integer(item.path("orderId"), order) && item.path("rmb").isInt() && item.path("rmb").intValue() > 0);
        // 未知字段可能包含未来语义或额外隐私，不能按旧 schema 静默封存为已理解的订单。
        require(Set.of("orderId", "sdkOrderId", "rmb", "payType", "payId", "sdkGoodsId", "paySubIds", "addId", "addCount",
                "isFinish", "finishTime", "completionState", "completionProof").containsAll(names(item)));
        validateParameters(item);
        require(item.path("isFinish").isBoolean() && item.path("finishTime").isIntegralNumber() && item.path("finishTime").canConvertToLong());
        if (item.has("completionState")) { require(item.path("completionState").isNull() || item.path("completionState").isTextual()); }
        JsonNode login = content.path("loginOrder");
        PaymentEvidenceCli.LoginOrder row = null;
        if (!login.isNull()) {
            require(login.isObject() && names(login).equals(Set.of("playerId", "price", "payState", "delivered", "sdkOrderId")));
            require(integer(login.path("playerId"), player) && login.path("price").isInt()
                    && login.path("price").longValue() == item.path("rmb").longValue() * 100
                    && login.path("payState").isInt() && (login.path("delivered").isNull() || login.path("delivered").isInt())
                    && (login.path("sdkOrderId").isNull() || login.path("sdkOrderId").isTextual()));
            row = JSON.treeToValue(login, PaymentEvidenceCli.LoginOrder.class);
            if (row.sdkOrderId() != null && !row.sdkOrderId().isBlank() && item.path("sdkOrderId").isTextual()) {
                require(row.sdkOrderId().equals(item.path("sdkOrderId").textValue()));
            }
        }
        JsonNode proof = item.path("completionProof");
        if (proof.isObject()) {
            require(Set.of("version", "playerId", "orderId", "sdkOrderId", "rmb", "payType", "payId", "sdkGoodsId",
                    "paySubIds", "addId", "addCount").containsAll(names(proof)));
            validateParameters(proof);
            if (proof.has("version")) { require(proof.path("version").isInt()); }
            if (proof.has("orderId")) { require(integer(proof.path("orderId"), order)); }
            if (proof.has("rmb")) { require(proof.path("rmb").equals(item.path("rmb"))); }
        }
        require(proof.isMissingNode() || proof.isNull() || proof.isObject());
        if (proof.isObject() && proof.has("playerId")) { require(integer(proof.path("playerId"), player)); }
        require(content.path("assessment").equals(assess(player, order, item, row)));
    }

    /** @param node 订单或凭证，仅允许既有购买字段的标量/整数列表，拒绝将异常嵌套对象当隐私载荷归档 */
    private static void validateParameters(JsonNode node) {
        for (String field : new String[] {"rmb", "payId", "sdkGoodsId", "addId", "addCount"}) {
            if (node.has(field)) { require(node.path(field).isInt()); }
        }
        if (node.has("sdkOrderId")) { require(node.path("sdkOrderId").isNull() || node.path("sdkOrderId").isTextual()); }
        if (node.has("payType")) { JsonNode type = unwrap(node.path("payType")); require(type.isTextual() || type.isNull()); }
        if (node.has("paySubIds")) {
            JsonNode ids = unwrap(node.path("paySubIds")); require(ids.isArray() || ids.isNull());
            for (JsonNode id : ids) { require(id.isInt()); }
        }
    }

    /** @param player 玩家 @param order 订单 @param item 单订单树 @param login 最小关联字段 @return 可重算的保守诊断 */
    private static JsonNode assess(long player, long order, JsonNode item, PaymentEvidenceCli.LoginOrder login) throws Exception {
        var items = JSON.createObjectNode(); items.set(Long.toString(order), item);
        var module = JSON.createObjectNode(); module.set("payItems", items);
        var modules = JSON.createObjectNode(); modules.set(MODULE, module);
        String raw = JSON.writeValueAsString(modules);
        Map<String, Object> result = PaymentEvidenceCli.inspect(player, order, new PaymentEvidenceCli.Reader() {
            @Override public String game(long id) { return raw; }
            @Override public PaymentEvidenceCli.LoginOrder login(long id) { return login; }
        });
        require(GAME_STATES.contains(String.valueOf(result.get("game"))));
        Map<String, Object> assessment = new TreeMap<>();
        for (String field : new String[] {"game", "login", "association", "sdkBinding", "historicalFinish", "completionState", "proofVersion"}) {
            if (result.containsKey(field)) { assessment.put(field, result.get(field)); }
        }
        return JSON.valueToTree(assessment);
    }

    /** @param node 普通树 @return 仅剥离既有包装，不实例化包装中的类名 */
    static JsonNode unwrap(JsonNode node) {
        return node.isArray() && node.size() == 2 && node.get(0).isTextual()
                && (node.get(0).textValue().startsWith("java.util.") || node.get(0).textValue().startsWith("cn.game.games.net.game.module."))
                ? node.get(1) : node;
    }

    /** @param node 对象 @return 精确字段集合 */
    static Set<String> names(JsonNode node) {
        Set<String> names = new java.util.HashSet<>(); node.fieldNames().forEachRemaining(names::add); return names;
    }

    /** @param node 数值节点 @param value 期望值 @return 是否没有截断、字符串转换或溢出 */
    private static boolean integer(JsonNode node, long value) {
        return node.isIntegralNumber() && node.canConvertToLong() && node.longValue() == value;
    }

    /** @param condition 必须满足的结构或语义约束 */
    static void require(boolean condition) {
        if (!condition) { throw new IllegalArgumentException("ARCHIVE_INVALID_OR_CONFLICT"); }
    }
}
