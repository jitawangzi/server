package cn.game.games.diagnostics;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import cn.game.games.net.game.helper.PaymentDedupStore;
import cn.game.games.net.game.module.recharge.PayItem;

/** 真实认证文件、完整冷读树与可控存储边界；永远断言缺失的独立证明仍阻断操作。 */
class PaymentCleanupTest {
    /** 每例独享私有路径与随机密钥，源对象和字节作为无副作用对照。 */
    private static final class Fixture implements AutoCloseable, PaymentCleanupCli.Reader {
        /** 本轮私有目录、归档根与根外密钥。 */
        final Path base, root, key;
        /** 原始冷读对象，仅测试准备时创建，不由预检初始化。 */
        final PayItem item = PaymentEvidenceTest.completed();
        /** 当前冷读可变边界及查询计数。 */
        String raw = PaymentEvidenceTest.snapshot(item);
        /** 最小 Login 字段，不能单独作为终态。 */
        PaymentEvidenceCli.LoginOrder login = new PaymentEvidenceCli.LoginOrder(41,1200,2,1,"secret-sdk");
        /** 持久边界结果；SQL 真实性由独立数据库测试验证。 */
        PaymentDedupStore.Lookup durable = PaymentDedupStore.Lookup.COMPLETED;
        /** 查询故障仅在测试读取器注入。 */
        Exception error;
        /** 源查询次数，证明按单有界。 */
        int gameReads, loginReads, durableReads;
        /** @throws Exception 私有随机归档准备失败 */
        Fixture() throws Exception {
            base = Files.createTempDirectory(Path.of(System.getenv("S3_ARCHIVE_TEST_ROOT")), "cleanup-unit-", PaymentArchiveStore.attributes(true));
            root = base.resolve("archive"); key = Files.createFile(base.resolve("key.bin"), PaymentArchiveStore.attributes(false));
            byte[] secret = new byte[32]; new java.security.SecureRandom().nextBytes(secret); Files.write(key,secret); java.util.Arrays.fill(secret,(byte)0);
            PaymentArchiveStore.archive(root,key,41,91,this); gameReads=0; loginReads=0;
        }
        /** @return 同一CLI入口参数 */
        String[] args() { return new String[]{"41","91",root.toString(),key.toString()}; }
        /** @return 本次有界观察报告 */
        Map<String,Object> check() { return PaymentCleanupCli.run(args(),this); }
        @Override public String game(long id) throws Exception { Assertions.assertEquals(41,id); gameReads++; if(error!=null) { throw error; } return raw; }
        @Override public PaymentEvidenceCli.LoginOrder login(long id) throws Exception { Assertions.assertEquals(91,id); loginReads++; if(error!=null) { throw error; } return login; }
        @Override public PaymentDedupStore.Lookup durable(long id, PayItem archived) throws Exception {
            durableReads++; Assertions.assertEquals(41,id);
            Assertions.assertEquals(cn.game.util.JsonUtil.toJsonString(item),cn.game.util.JsonUtil.toJsonString(archived));
            if(error!=null) { throw error; } return durable;
        }
        @Override public void close() throws Exception {
            try(var paths=Files.walk(base)) { for(Path p:paths.sorted(java.util.Comparator.reverseOrder()).toList()) { Files.delete(p); } }
        }
    }

    /** @param result 报告；任何失败或局部一致都不可删除或补发 */
    private static void blocked(Map<String,Object> result) {
        Assertions.assertEquals(false,result.get("canDelete")); Assertions.assertEquals(false,result.get("canResend"));
        Assertions.assertEquals(List.of("PLATFORM_TERMINAL_NOT_PROVEN","OLD_WRITERS_AND_UNKNOWN_NOT_FENCED"),result.get("externalBlockers"));
        Assertions.assertEquals("NOT_PROVEN",result.get("platformTerminal")); Assertions.assertEquals("NOT_PROVEN",result.get("writerAndUnknownFencing"));
        Assertions.assertEquals("MULTI_CONNECTION_NON_ATOMIC_TOCTOU_NOT_AUTHORIZATION",result.get("observation"));
    }

    /** 所有局部证据一致仍保留两项外部阻断，重复读取不改变源字节或对象，不触发业务 hook。 */
    @Test void consistentLocalEvidenceNeverAuthorizesDeletionOrResend() throws Exception {
        try(var f=new Fixture()) {
            byte[] before=Files.readAllBytes(PaymentArchiveStore.artifact(f.root,41,91)); String source=f.raw;
            String item=cn.game.util.JsonUtil.toJsonString(f.item);
            for(int i=0;i<2;i++) { var result=f.check(); blocked(result); Assertions.assertEquals(true,result.get("localEvidenceReady"),result.toString()); Assertions.assertEquals(2,PaymentCleanupCli.exitCode(result)); }
            Assertions.assertEquals(2,f.gameReads); Assertions.assertEquals(2,f.loginReads); Assertions.assertEquals(2,f.durableReads);
            Assertions.assertEquals(source,f.raw); Assertions.assertEquals(item,cn.game.util.JsonUtil.toJsonString(f.item));
            Assertions.assertArrayEquals(before,Files.readAllBytes(PaymentArchiveStore.artifact(f.root,41,91)));
        }
    }

    /** 实际文件内容、MAC、密钥和期望身份每次重新核验，不能缓存 VERIFIED 或仅信任文件名。 */
    @Test void authenticatedBytesKeyVersionAndIdentityAreRecheckedEveryTime() throws Exception {
        try(var f=new Fixture()) {
            var first=f.check(); Assertions.assertEquals(true,first.get("localEvidenceReady"),first.toString());
            Path artifact=PaymentArchiveStore.artifact(f.root,41,91); String original=Files.readString(artifact);
            for(String bad:List.of(original.replace("\"schemaVersion\":1","\"schemaVersion\":9"),original.replace("secret-sdk","changed-sdk"),"{",original+"{}")) {
                Files.writeString(artifact,bad); var result=f.check(); blocked(result); Assertions.assertEquals(false,result.get("localEvidenceReady"));
                Assertions.assertEquals(List.of("ARCHIVE_AUTHENTICATION_OR_FORMAT_FAILED"),result.get("localBlockers"));
            }
            Files.writeString(artifact,original); Files.write(f.key,new byte[32]); Assertions.assertEquals(false,f.check().get("localEvidenceReady"));
            Assertions.assertEquals(1,f.gameReads,"认证失败不得使用已缓存的先前通过状态");
        }
    }

    /** 当前字段的金额、SDK、全部购买参数、完成时间与proof版本变化都会阻断。 */
    @Test void currentSnapshotParametersAndProofChangesAreBlocked() throws Exception {
        try(var f=new Fixture()) {
            String original=f.raw;
            for(String changed:List.of(original.replace("secret-sdk","other-sdk"),original.replace("\"rmb\":12","\"rmb\":13"),
                    original.replace("\"version\":1","\"version\":99"),original.replace("\"payId\":0","\"payId\":9"),
                    original.replace("\"sdkGoodsId\":0","\"sdkGoodsId\":9"),original.replace("\"addId\":0","\"addId\":9"),
                    original.replace("\"addCount\":0","\"addCount\":9"),original.replace("FirstCharge","Recharge"),
                    original.replace("\"paySubIds\":[","\"paySubIds\":[9,"),original.replace("\"finishTime\":"+f.item.getFinishTime(),"\"finishTime\":1"))) {
                f.raw=changed; var result=f.check(); blocked(result); Assertions.assertEquals(false,result.get("localEvidenceReady"),changed);
                Assertions.assertFalse(((List<?>)result.get("localBlockers")).isEmpty());
            }
        }
    }

    /** 玩家行缺失、订单缺失、UNKNOWN/执行中/旧finish、Login缺失或仅payState完成均不授予权限。 */
    @Test void absentColdDataPendingUnknownAndLegacyRemainBlocked() throws Exception {
        try(var f=new Fixture()) {
            String original=f.raw;
            for(String raw:new String[]{null,"{}",original.replace("\"91\":","\"92\":"),original.replace("REWARDS_COMPLETED","UNKNOWN"),
                    original.replace("REWARDS_COMPLETED","EXECUTING"),original.replace("\"completionProof\":{","\"completionProof\":null,\"badProof\":{")}) {
                f.raw=raw; var result=f.check(); blocked(result); Assertions.assertEquals(false,result.get("localEvidenceReady"));
                if(raw!=null&&raw.contains("\"92\":")) { Assertions.assertEquals("ALREADY_NOT_PRESENT_OBSERVED",result.get("hotItem")); }
            }
            f.raw=original;
            for(var login:new PaymentEvidenceCli.LoginOrder[]{null,new PaymentEvidenceCli.LoginOrder(41,1200,2,0,"secret-sdk"),
                    new PaymentEvidenceCli.LoginOrder(41,1200,1,1,"secret-sdk"),new PaymentEvidenceCli.LoginOrder(42,1200,2,1,"secret-sdk"),
                    new PaymentEvidenceCli.LoginOrder(41,1300,2,1,"secret-sdk"),new PaymentEvidenceCli.LoginOrder(41,1200,2,1,"other-sdk")}) {
                f.login=login; Assertions.assertEquals(false,f.check().get("localEvidenceReady"));
            }
        }
    }

    /** 缺失/占位、超时/异常和当前完成凭证单独存在，都不替代归档及外部证明。 */
    @Test void missingReceiptClaimedAndQueryErrorsFailClosed() throws Exception {
        try(var f=new Fixture()) {
            for(var lookup:List.of(PaymentDedupStore.Lookup.ABSENT,PaymentDedupStore.Lookup.CLAIMED)) {
                f.durable=lookup; var result=f.check(); blocked(result); Assertions.assertEquals(false,result.get("localEvidenceReady"));
            }
            for(Exception failure:List.of(new java.sql.SQLTimeoutException("private-url-key"),new java.sql.SQLException("private-url-key"),new IllegalStateException("private-url-key"))) {
                f.error=failure; var result=f.check(); blocked(result); Assertions.assertEquals(false,result.get("localEvidenceReady"));
                Assertions.assertFalse(result.toString().contains("private-url-key"));
            }
        }
    }

    /** 输入严格正数且不开放force/批量；输出不泄露业务对象、SDK、路径或key，错误码明确。 */
    @Test void explicitInputAndRedactedOutputConvention() throws Exception {
        for(String id:List.of("0","-1","+1","01","9223372036854775808","*","41,42")) {
            var result=PaymentCleanupCli.run(new String[]{id,"91","root","key"}); blocked(result); Assertions.assertEquals(1,PaymentCleanupCli.exitCode(result));
        }
        try(var f=new Fixture()) {
            String text=PaymentArchiveEvidence.JSON.writeValueAsString(f.check());
            for(String secret:List.of("secret-sdk",f.root.toString(),f.key.toString(),"payItem","loginOrder","jdbc:")) { Assertions.assertFalse(text.contains(secret)); }
            Assertions.assertEquals(1,PaymentCleanupCli.exitCode(PaymentCleanupCli.run(new String[]{"41","91",f.root.toString(),f.key.toString(),"--force"})));
        }
    }
}
