package cn.game.games.diagnostics;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import cn.game.games.net.game.module.player.PlayerModule;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.games.net.game.module.recharge.PayType;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;

/** 通过实际清理入口、生产 JSON 格式及 CLI 核对入口验证保留和只读边界。 */
class PaymentEvidenceTest {
    /** @return 带当前版本凭证的订单，SDK 单号仅作夹具绑定 */
    static PayItem completed() {
        PayItem item = new PayItem(); item.setOrderId(91); item.setRmb(12); item.setPayType(PayType.FirstCharge);
        item.setSdkOrderId("secret-sdk"); item.finish(); item.setCompletionState("REWARDS_COMPLETED");
        item.prepareCompletionProof(41); item.confirmCompletionProof(); return item;
    }
    /** @param item 指定订单 @return 生产带类型聚合 JSON */
    static String snapshot(PayItem item) {
        PlayerModule module = new PlayerModule(); module.addPayItems(item);
        return JsonUtil.toJsonStringWithType(new HashMap<>(Map.of(PlayerModule.class.getName(), module)));
    }
    /** @param raw 持久 JSON @return 无副作用的固定存储边界 */
    static PaymentEvidenceCli.Reader reader(String raw) {
        return new PaymentEvidenceCli.Reader() {
            @Override public String game(long player) { return raw; }
            @Override public PaymentEvidenceCli.LoginOrder login(long order) {
                return new PaymentEvidenceCli.LoginOrder(41, 1200, 2, 1, "secret-sdk");
            }
        };
    }
    /** 十天前后及旧/新/未知/部分履约全部走真实初始化清理，既不删除也不追认。 */
    @Test void cleanupRetainsEveryEvidenceClassAcrossTenDayBoundary() {
        for (long age : new long[] {10 * DateUtil.DAY_MILLIS - 1, 10 * DateUtil.DAY_MILLIS, 10 * DateUtil.DAY_MILLIS + 1, 365 * DateUtil.DAY_MILLIS}) {
            for (String state : new String[] {null, "EXECUTING", "REVIEW_REQUIRED", "UNKNOWN", "FAILED", "REWARDS_COMPLETED"}) {
                PayItem legacy = new PayItem(); legacy.setOrderId(91); legacy.setFinish(true);
                legacy.setCompletionState(state); legacy.setFinishTime(DateUtil.currentTimeMillis() - age);
                PlayerModule module = new PlayerModule(); module.addPayItems(legacy);
                PayItem proof = completed(); proof.setFinishTime(legacy.getFinishTime());
                PlayerModule proofModule = new PlayerModule(); proofModule.addPayItems(proof);
                module.initFromDbAfter(); module.initFromDbAfter();
                proofModule.initFromDbAfter(); proofModule.initFromDbAfter();
                Assertions.assertSame(legacy, module.getPayItems(91)); Assertions.assertSame(proof, proofModule.getPayItems(91));
                Assertions.assertFalse(legacy.hasCommittedCompletionProof(41, "secret-sdk"));
                Assertions.assertTrue(proof.hasCommittedCompletionProof(41, "secret-sdk"));
            }
        }
    }
    /** 生产聚合 JSON 直接树读，不激活模块或多态加载；重复核对不改变数据或输出敏感单号。 */
    @Test void productionSnapshotInspectionIsReadOnlyAndRedacted() {
        PayItem item = completed(); String raw = snapshot(item);
        for (int i = 0; i < 2; i++) {
            var result = PaymentEvidenceCli.inspect(41, 91, reader(raw));
            Assertions.assertEquals("PERSISTED_V1_PROOF_CONSISTENT", result.get("game"), raw);
            Assertions.assertEquals("MATCH", result.get("sdkBinding"));
            Assertions.assertEquals("NOT_PROVEN", result.get("transactionResolution"));
            Assertions.assertEquals("NOT_OBSERVABLE", result.get("inFlight"));
            Assertions.assertEquals(false, result.get("canResend"));
            Assertions.assertFalse(result.toString().contains("secret-sdk"));
        }
        Assertions.assertEquals(raw, snapshot(item));
    }
    /** 未知字段可忽略；未知版本、身份/金额/商品冲突及缺失凭证均不能判为一致凭证。 */
    @Test void versionsAndConflictsRemainManualReview() {
        String raw = snapshot(completed());
        Assertions.assertEquals("PERSISTED_V1_PROOF_CONSISTENT", PaymentEvidenceCli.inspect(41, 91,
                reader(raw.replace("\"version\":1", "\"version\":1,\"future\":true"))).get("game"));
        Assertions.assertEquals("UNKNOWN_PROOF_VERSION", PaymentEvidenceCli.inspect(41, 91,
                reader(raw.replace("\"version\":1", "\"version\":99"))).get("game"));
        for (String mutated : new String[] {raw.replace("\"playerId\":41", "\"playerId\":42"),
                raw.replaceFirst("\"rmb\":12", "\"rmb\":13"), raw.replaceFirst("\"payId\":0", "\"payId\":1"),
                raw.replace("FirstCharge", "UnknownType"), raw.replaceFirst("\"addId\":0", "\"addId\":\"bad\"")}) {
            Assertions.assertNotEquals("PERSISTED_V1_PROOF_CONSISTENT", PaymentEvidenceCli.inspect(41, 91, reader(mutated)).get("game"));
        }
        Assertions.assertNotEquals("PERSISTED_V1_PROOF_CONSISTENT", PaymentEvidenceCli.inspect(41, 91,
                reader(raw.replace("\"playerId\":41", "\"playerId\":18446744073709551657"))).get("game"));
        PayItem legacy = completed(); legacy = JsonUtil.parseObject("{\"orderId\":91,\"rmb\":12,\"isFinish\":true,\"finishTime\":1}", PayItem.class);
        Assertions.assertEquals("NO_COMPLETION_PROOF", PaymentEvidenceCli.inspect(41, 91, reader(snapshot(legacy))).get("game"));
    }
    /** 无记录、损坏 JSON、重复键、输入和大小限制、查询失败/超时均有独立结果。 */
    @Test void inputMissingMalformedAndQueryFailuresAreExplicit() {
        Assertions.assertEquals("INVALID_INPUT", PaymentEvidenceCli.run(new String[] {"41", "91", "page=2"}).get("status"));
        Assertions.assertEquals("INVALID_INPUT", PaymentEvidenceCli.run(new String[] {"9999999999999999999", "91"}).get("status"));
        Assertions.assertEquals("INVALID_INPUT", PaymentEvidenceCli.run(new String[] {"0", "91"}).get("status"));
        Assertions.assertEquals("PLAYER_NOT_FOUND", PaymentEvidenceCli.inspect(41, 91, reader(null)).get("game"));
        Assertions.assertEquals("MODULE_NOT_FOUND", PaymentEvidenceCli.inspect(41, 91, reader("{}")).get("game"));
        for (String bad : new String[] {"{", "null", "{}{}", "{\"x\":1,\"x\":2}"}) {
            Assertions.assertEquals("MALFORMED_OR_READ_ERROR", PaymentEvidenceCli.inspect(41, 91, reader(bad)).get("game"));
        }
        Assertions.assertEquals("INPUT_LIMIT", PaymentEvidenceCli.inspect(41, 91, reader(" ".repeat(PaymentEvidenceCli.MAX_BYTES + 1))).get("game"));
        for (boolean timeout : new boolean[] {false, true}) {
            var r = new PaymentEvidenceCli.Reader() {
                @Override public String game(long id) throws Exception { throw timeout ? new java.sql.SQLTimeoutException("secret") : new java.sql.SQLException("secret"); }
                @Override public PaymentEvidenceCli.LoginOrder login(long id) throws Exception { throw timeout ? new java.sql.SQLTimeoutException("secret") : new java.sql.SQLException("secret"); }
            };
            var result = PaymentEvidenceCli.inspect(41, 91, r);
            Assertions.assertEquals(timeout ? "TIMEOUT" : "QUERY_ERROR", result.get("game"));
            Assertions.assertEquals(timeout ? "TIMEOUT" : "QUERY_ERROR", result.get("login"));
            Assertions.assertFalse(result.toString().contains("secret"));
        }
    }
    /** Login 缺失、未知状态和归属/金额/流水冲突只分类，不生成重试许可。 */
    @Test void loginUnknownMissingAndConflictingRecordsAreNotSuccess() {
        for (var login : new PaymentEvidenceCli.LoginOrder[] {null, new PaymentEvidenceCli.LoginOrder(42, 1200, 2, 1, "other"),
                new PaymentEvidenceCli.LoginOrder(41, 1201, 7, null, null)}) {
            var result = PaymentEvidenceCli.inspect(41, 91, new PaymentEvidenceCli.Reader() {
                @Override public String game(long id) { return snapshot(completed()); }
                @Override public PaymentEvidenceCli.LoginOrder login(long id) { return login; }
            });
            Assertions.assertNotEquals("OBSERVED", result.get("login")); Assertions.assertEquals(false, result.get("canResend"));
        }
    }
}
