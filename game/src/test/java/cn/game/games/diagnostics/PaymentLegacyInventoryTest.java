package cn.game.games.diagnostics;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cn.game.games.net.game.helper.PaymentDedupStore;
import cn.game.games.net.game.module.recharge.PayItem;

/** 清单、游标、严格证明、脱敏和部分结果的回归；真实数据库和子JVM另由隔离验收覆盖。 */
class PaymentLegacyInventoryTest {
    /** 可控单条源，只记录读主键；从不构造Player或调用业务hook。 */
    private static final class Source implements PaymentCleanupCli.Reader {
        /** 普通持久树和最小Login字段。 */
        String raw = snapshot(item());
        PaymentEvidenceCli.LoginOrder row = new PaymentEvidenceCli.LoginOrder(41,1200,2,1,"secret-sdk");
        /** 存储状态及注入错误。 */
        PaymentDedupStore.Lookup lookup = PaymentDedupStore.Lookup.ABSENT;
        Exception gameError, loginError, durableError;
        /** 请求轨迹只留测试内存。 */
        final java.util.ArrayList<Long> games = new java.util.ArrayList<>(), orders = new java.util.ArrayList<>();
        int durableReads;
        @Override public String game(long player) throws Exception { games.add(player); if (gameError != null) { throw gameError; } return raw; }
        @Override public PaymentEvidenceCli.LoginOrder login(long order) throws Exception { orders.add(order); if (loginError != null) { throw loginError; } return row; }
        @Override public PaymentDedupStore.Lookup durable(long player, PayItem item) throws Exception {
            durableReads++; if (durableError != null) { throw durableError; } return lookup;
        }
    }

    /** @return 与生产完成链相同的V1普通树，不加载PlayerModule */
    private static ObjectNode item() {
        try { return (ObjectNode)PaymentArchiveEvidence.JSON.readTree(cn.game.util.JsonUtil.toJsonString(PaymentEvidenceTest.completed())); }
        catch (Exception failure) { PaymentArchiveStore.safeWarn(failure); throw new AssertionError(failure); }
    }

    /** @param item 普通订单树 @return 无类加载的原始聚合格式 */
    private static String snapshot(ObjectNode item) {
        ObjectNode items = PaymentArchiveEvidence.JSON.createObjectNode(); items.set("91",item);
        ObjectNode module = PaymentArchiveEvidence.JSON.createObjectNode(); module.set("payItems",items);
        ObjectNode root = PaymentArchiveEvidence.JSON.createObjectNode(); root.set(PaymentArchiveEvidence.MODULE,module); return root.toString();
    }

    /** @param source 固定边界 @return 单页查询 */
    private static Map<String,Object> run(Source source) { return PaymentLegacyInventoryCli.run(new String[]{"41:91","1","-"},source); }

    /** @param result 报告 @param classification 唯一分类 @param reason 固定原因 */
    private static void expect(Map<String,Object> result, String classification, String reason) {
        Assertions.assertEquals("PAGE_COMPLETE",result.get("status"),result.toString()); Assertions.assertEquals(1,result.get("observed"));
        Assertions.assertEquals(1,((Map<?,?>)result.get("counts")).get(classification),result.toString());
        Assertions.assertEquals(Map.of(reason,1),result.get("reasons")); blocked(result);
    }

    /** @param result 所有分类均保留外部证明及写入授权边界 */
    private static void blocked(Map<String,Object> result) {
        for (String flag : List.of("canDelete","canResend","canBackfill")) { Assertions.assertEquals(false,result.get(flag)); }
        Assertions.assertEquals("NOT_PROVEN",result.get("platformTerminal")); Assertions.assertEquals("NOT_PROVEN",result.get("writerAndUnknownFencing"));
        String json=cn.game.util.JsonUtil.toJsonString(result); Assertions.assertTrue(json.length()<12000);
        for (String secret : List.of("secret-sdk","jdbc:","payItems","completionProof","dbpass")) { Assertions.assertFalse(json.contains(secret),secret); }
    }

    /** 缺省、重复、溢出、全库扫描、额外参数和过大页均在读取前失败。 */
    @Test void rejectsInvalidOrDuplicateUnboundedScopeBeforeAnyRead() {
        var source = new Source();
        for (String[] args : new String[][]{null,{}, {"*","1","-"},{"41:91","0","-"},{"41:91","11","-"},
                {"41:91,41:91","1","-"},{"41:91,42:91","1","-"},{"0:91","1","-"},
                {"9223372036854775808:91","1","-"},{"41:91,","1","-"},{"41:91","1","-","archive"},
                {"41:91","1","garbage"},{"41:91",null,"-"},{null,"1","-"}}) {
            var out=PaymentLegacyInventoryCli.run(args,source); Assertions.assertEquals(1,PaymentLegacyInventoryCli.exitCode(out));blocked(out);
        }
        String scope=java.util.stream.LongStream.rangeClosed(1,101).mapToObj(i->i+":"+i).collect(java.util.stream.Collectors.joining(","));
        Assertions.assertEquals(1,PaymentLegacyInventoryCli.exitCode(PaymentLegacyInventoryCli.run(new String[]{scope,"10","-"},source)));
        Assertions.assertTrue(source.games.isEmpty()); Assertions.assertTrue(source.orders.isEmpty());
    }

    /** 数字排序绑定清单，末页和空页恰好覆盖输入，游标不能跨清单复用。 */
    @Test void deterministicPagesBindScopeAndEmptyTailDoesNotConnect() {
        var source = new Source();source.raw=null;source.row=null;
        String pairs="3:30,1:10,2:20";
        var first=PaymentLegacyInventoryCli.run(new String[]{pairs,"2","-"},source);
        Assertions.assertEquals(List.of(1L,2L),source.games); Assertions.assertEquals(2,first.get("nextOffset"));Assertions.assertEquals(false,first.get("scopeExhausted"));
        var second=PaymentLegacyInventoryCli.run(new String[]{"2:20,3:30,1:10","2",first.get("nextCursor").toString()},source);
        Assertions.assertEquals(List.of(1L,2L,3L),source.games);Assertions.assertEquals(1,second.get("observed"));Assertions.assertEquals(true,second.get("scopeExhausted"));
        var empty=PaymentLegacyInventoryCli.runWithFactory(new String[]{pairs,"1",second.get("nextCursor").toString()},started->{throw new AssertionError("empty page must not open source");},System::nanoTime);
        Assertions.assertEquals(0,empty.get("observed"));Assertions.assertEquals(0,PaymentLegacyInventoryCli.exitCode(empty));
        Assertions.assertEquals(1,PaymentLegacyInventoryCli.exitCode(PaymentLegacyInventoryCli.run(new String[]{"1:10,2:20","2",first.get("nextCursor").toString()},source)));
        String digest=first.get("nextCursor").toString().substring(0,65);
        Assertions.assertEquals(1,PaymentLegacyInventoryCli.exitCode(PaymentLegacyInventoryCli.run(new String[]{pairs,"1",digest+"4"},source)));
    }

    /** 三种持久观察明确分开，完整V1热证明不是自动补建资格。 */
    @Test void strictV1AbsentClaimedAndCompletedAreDistinctReadOnlyCandidates() {
        var source=new Source();String before=source.raw;
        String[] categories={"VALID_V1_COMPLETED_HOT_WITHOUT_NEW_RECEIPT","DURABLE_CLAIM_WITHOUT_COMPLETED_RECEIPT","VALID_CURRENT_DURABLE_COMPLETED_RECEIPT_AND_CLAIM"};
        String[] reasons={"CURRENT_DURABLE_TABLES_OBSERVED_NO_ROWS","CLAIM_ONLY_REQUIRES_MANUAL_EVIDENCE","STRICT_CURRENT_DURABLE_COMPLETION_OBSERVED"};
        for(int i=0;i<3;i++) {source.lookup=PaymentDedupStore.Lookup.values()[i];expect(run(source),categories[i],reasons[i]);}
        Assertions.assertEquals(3,source.durableReads);Assertions.assertEquals(before,source.raw);
    }

    /** 历史finish、未完成、执行中、UNKNOWN和Login待交付不会被当作完成证明。 */
    @Test void historicalFinishAndPendingUnknownStatesStaySeparate() {
        var source=new Source();var tree=item();tree.remove("completionProof");tree.remove("completionState");source.raw=snapshot(tree);
        expect(run(source),"HISTORICAL_FINISH_WITHOUT_V1_PROOF","FINISH_ALONE_NOT_V1_EVIDENCE");
        for(String state:List.of("EXECUTING","REVIEW_REQUIRED","UNKNOWN","FUTURE_PHASE")) {
            tree.put("completionState",state);source.raw=snapshot(tree);expect(run(source),"UNFINISHED_PENDING_OR_UNKNOWN","NON_TERMINAL_OR_UNKNOWN_LOCAL_STATE");
        }
        tree.remove("completionState");tree.put("isFinish",false);source.raw=snapshot(tree);expect(run(source),"UNFINISHED_PENDING_OR_UNKNOWN","NON_TERMINAL_OR_UNKNOWN_LOCAL_STATE");
        source.raw=snapshot(item());
        for(var row:List.of(new PaymentEvidenceCli.LoginOrder(41,1200,99,1,"secret-sdk"),new PaymentEvidenceCli.LoginOrder(41,1200,1,0,"secret-sdk"),new PaymentEvidenceCli.LoginOrder(41,1200,2,null,"secret-sdk"))) {
            source.row=row;expect(run(source),"UNFINISHED_PENDING_OR_UNKNOWN","NON_TERMINAL_OR_UNKNOWN_LOCAL_STATE");
        }
        Assertions.assertEquals(0,source.durableReads);
    }

    /** 未来版本保留不支持状态，零、缺失及类型错误拒绝作为V1完成。 */
    @Test void futureAndVersionGapsNeverBecomeCurrentProof() {
        var source=new Source();
        for(int version:List.of(2,99,Integer.MAX_VALUE)) {
            var tree=item();((ObjectNode)tree.path("completionProof")).put("version",version);source.raw=snapshot(tree);
            expect(run(source),"UNSUPPORTED_FUTURE_VERSION","HOT_PROOF_FUTURE_VERSION");
        }
        for(String version:List.of("0","-1","null","\"1\"")) {
            source.raw=snapshot(item()).replace("\"version\":1","\"version\":"+version);
            Assertions.assertEquals(1,((Map<?,?>)run(source).get("counts")).get("CORRUPT_OR_UNREADABLE"));
        }
        Assertions.assertEquals(0,source.durableReads);
    }

    /** 身份和精确金额冲突优先阻断，不把错误关联当成旧订单。 */
    @Test void conflictingIdentityAmountSdkAndProofAreNotHistoricalSuccess() {
        var source=new Source();
        for(var row:List.of(new PaymentEvidenceCli.LoginOrder(42,1200,2,1,"secret-sdk"),new PaymentEvidenceCli.LoginOrder(41,1201,2,1,"secret-sdk"),new PaymentEvidenceCli.LoginOrder(41,1200,2,1,"other"))) {
            source.row=row;expect(run(source),"CONFLICTING_IDENTITY","LOGIN_IDENTITY_OR_AMOUNT_CONFLICT");
        }
        source=new Source();var tree=item();tree.put("orderId",92);source.raw=snapshot(tree);expect(run(source),"CONFLICTING_IDENTITY","HOT_ORDER_ID_CONFLICT");
        tree=item();((ObjectNode)tree.path("completionProof")).put("playerId",42);source.raw=snapshot(tree);expect(run(source),"CONFLICTING_IDENTITY","PROOF_IDENTITY_CONFLICT");
    }

    /** 必须复用严格购买参数/完整字段校验，不能用弱树比较接受未知字段或证明缺项。 */
    @Test void strictProofRejectsMissingExtraAndChangedPurchaseParameters() {
        var source=new Source();
        for(String field:List.of("payType","paySubIds","payId","sdkGoodsId","addId","addCount","finishTime")) {
            var tree=item();tree.remove(field);source.raw=snapshot(tree);Assertions.assertEquals(1,((Map<?,?>)run(source).get("counts")).get("CORRUPT_OR_UNREADABLE"),field);
        }
        var tree=item();tree.put("futureSecret","must-not-leak");source.raw=snapshot(tree);expect(run(source),"CORRUPT_OR_UNREADABLE","ORDER_SCHEMA_OR_PROOF_INVALID");
        tree=item();tree.put("payId",72);source.raw=snapshot(tree);expect(run(source),"CORRUPT_OR_UNREADABLE","STRICT_V1_COMPLETION_INVALID");
    }

    /** 缺行、缺模块、缺热单、缺依赖和查询错误分别报告，错误不等于空。 */
    @Test void missingDependenciesAndSqlFailureHaveDifferentCoverage() {
        var source=new Source();source.raw=null;expect(run(source),"MISSING_DEPENDENCY","GAME_PLAYER_MISSING");
        source.raw="{}";expect(run(source),"MISSING_DEPENDENCY","GAME_MODULE_MISSING");
        source.raw="{\""+PaymentArchiveEvidence.MODULE+"\":{\"payItems\":{}}}";expect(run(source),"MISSING_DEPENDENCY","HOT_ORDER_MISSING_NOT_INFERRED");
        source.raw=snapshot(item());source.row=null;expect(run(source),"MISSING_DEPENDENCY","LOGIN_ORDER_MISSING");
        source=new Source();source.durableError=new java.sql.SQLException("hidden","42S02",1146);expect(run(source),"MISSING_DEPENDENCY","DURABLE_TABLE_MISSING_DEFAULT_OFF_EXPECTED");
        source.durableError=new java.sql.SQLException("credentials-hidden","42000",1142);var failed=run(source);
        Assertions.assertEquals(2,PaymentLegacyInventoryCli.exitCode(failed));Assertions.assertEquals(0,failed.get("observed"));Assertions.assertEquals(0,failed.get("nextOffset"));blocked(failed);
    }

    /** 错误JSON、重复键、尾随文档和过深树失败，输入预算不制造假空订单。 */
    @Test void malformedDuplicateTrailingDeepAndLargeJsonRemainUnreadable() {
        var source=new Source();
        for(String raw:List.of("{","null","{}{}","{\"duplicate\":1,\"duplicate\":2}","[".repeat(66)+"0"+"]".repeat(66),"x".repeat(PaymentEvidenceCli.MAX_BYTES+1))) {
            source.raw=raw;var out=run(source);Assertions.assertEquals(1,((Map<?,?>)out.get("counts")).get("CORRUPT_OR_UNREADABLE"));blocked(out);
        }
        Assertions.assertEquals(0,source.durableReads);
    }

    /** 只剥离包装字符串，伪造的模块类名不会初始化或执行hook。 */
    @Test void persistedTypeWrappersNeverInitializeTheirNamedClass() {
        var source=new Source();source.raw="[\"java.util.HashMap\",{\""+PaymentArchiveEvidence.MODULE+"\":[\"cn.game.games.net.game.module.DoesNotExistHook\",{\"payItems\":{\"91\":"+item()+"}}]}]";
        expect(run(source),"VALID_V1_COMPLETED_HOT_WITHOUT_NEW_RECEIPT","CURRENT_DURABLE_TABLES_OBSERVED_NO_ROWS");
    }

    /** 先成功的覆盖保留，连接失败项停止并可从同一游标重试，后续输入不被读取。 */
    @Test void partialConnectionFailurePreservesProgressWithoutSkippingFailedEntry() {
        var games=new java.util.ArrayList<Long>();
        PaymentCleanupCli.Reader reader=new PaymentCleanupCli.Reader() {
            @Override public String game(long id)throws Exception {games.add(id);if(id==2){throw new java.sql.SQLNonTransientConnectionException("secret-url");}return null;}
            @Override public PaymentEvidenceCli.LoginOrder login(long id){return null;}
            @Override public PaymentDedupStore.Lookup durable(long id,PayItem item){throw new AssertionError();}
        };
        var out=PaymentLegacyInventoryCli.run(new String[]{"1:10,2:20,3:30","3","-"},reader);
        Assertions.assertEquals(List.of(1L,2L),games);Assertions.assertEquals(1,out.get("observed"));Assertions.assertEquals(2,out.get("attempted"));Assertions.assertEquals(1,out.get("nextOffset"));Assertions.assertEquals(false,out.get("scopeExhausted"));
        var retry=PaymentLegacyInventoryCli.run(new String[]{"1:10,2:20,3:30","3",out.get("nextCursor").toString()},reader);
        Assertions.assertEquals(1,retry.get("nextOffset"));Assertions.assertEquals(0,retry.get("observed"));blocked(retry);
    }

    /** 配置失败、SQL超时及总预算耗尽均非空页成功；超时后不启动新查询。 */
    @Test void timeoutAndConfigurationFailuresAreHonestAndBoundFurtherWork() {
        var source=new Source();source.gameError=new java.sql.SQLTimeoutException("private");var out=run(source);
        Assertions.assertEquals(Map.of("OBSERVATION_TIMEOUT",1),out.get("reasons"));Assertions.assertTrue(source.orders.isEmpty());
        out=PaymentLegacyInventoryCli.runWithFactory(new String[]{"41:91","1","-"},start->{throw new java.io.IOException("private");},System::nanoTime);
        Assertions.assertEquals(0,out.get("observed"));Assertions.assertEquals(0,out.get("attempted"));Assertions.assertEquals(2,PaymentLegacyInventoryCli.exitCode(out));
        var nanos=new java.util.concurrent.atomic.AtomicLong();source=new Source();Source fixed=source;
        out=PaymentLegacyInventoryCli.runWithFactory(new String[]{"41:91","1","-"},start->{nanos.set(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(12001));return fixed;},nanos::get);
        Assertions.assertTrue(source.games.isEmpty());Assertions.assertEquals(0,out.get("nextOffset"));Assertions.assertEquals(2,PaymentLegacyInventoryCli.exitCode(out));
    }

    /** 最大合法清单只读取一页，所有分类/原因和覆盖字段均为固定大小聚合输出。 */
    @Test void maximumScopeStillReadsOnlyTenEntriesAndBoundsAggregateOutput() {
        var source=new Source();source.raw=null;source.row=null;
        String scope=java.util.stream.LongStream.rangeClosed(1,100).mapToObj(i->i+":"+i).collect(java.util.stream.Collectors.joining(","));
        var out=PaymentLegacyInventoryCli.run(new String[]{scope,"10","-"},source);
        Assertions.assertEquals(10,source.games.size());Assertions.assertEquals(10,source.orders.size());Assertions.assertEquals(10,out.get("nextOffset"));Assertions.assertEquals(100,out.get("scopeEntries"));blocked(out);
    }
}
