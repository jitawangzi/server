package cn.game.games.net.game.helper;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.util.JsonUtil;

/** 只读持久证据适配的对抗单测；内存JDBC行只替代查询结果，复用真实身份及完成证明校验。 */
class PaymentCleanupDurableTest {
    /** 完成订单及对应真实序列化claim/receipt行。 */
    private static final class Rows {
        /** 期待完整订单，不含业务hook。 */
        final PayItem expected=PaymentDurableDedupTest.item();
        /** 占位和完成行分别保存，可独立损坏或删除。 */
        final List<Object[]> claim=new ArrayList<>(),receipt=new ArrayList<>();
        /** 固定格式测试token，仅在内存使用。 */
        final String token="aabbccdd-1122-4333-8444-123456789abc";
        /** 查询数及边界设置数量。 */
        int reads,timeouts,limits;
        /** 查询失败注入，禁止转换成ABSENT。 */
        boolean fail;
        /** 构造真实当前V1完成证明。 */
        Rows()throws Exception {
            expected.setSdkOrderId("sdk");expected.finish();expected.setCompletionState("REWARDS_COMPLETED");expected.prepareCompletionProof(7);expected.confirmCompletionProof();
            String intent=JsonUtil.toJsonString(PaymentDedupStore.intent(7,expected,"sdk")),proof=JsonUtil.toJsonString(expected);
            claim.add(new Object[]{intent,token,7L,91L,"sdk"});receipt.add(new Object[]{1,7L,91L,"sdk",intent,token,proof,digest(proof)});
        }
        /** @return 仅实现生产读取器实际所需的JDBC方法，其余调用直接失败 */
        Connection connection() {
            return (Connection)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Connection.class},(p,m,a)-> {
                if(m.getName().equals("prepareStatement")) {
                    String sql=(String)a[0];Assertions.assertTrue(sql.startsWith("SELECT "));Assertions.assertTrue(sql.contains("LIMIT 3"));
                    Assertions.assertTrue(sql.contains("OCTET_LENGTH(intent)<=4194304"));
                    boolean completion=sql.contains("t_payment_fulfillment_receipt");
                    return Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{java.sql.PreparedStatement.class},(sp,sm,sa)-> {
                        switch(sm.getName()) {
                            case "setQueryTimeout": Assertions.assertEquals(2,sa[0]);timeouts++;return null;
                            case "setMaxRows": Assertions.assertEquals(3,sa[0]);limits++;return null;
                            case "setLong": Assertions.assertEquals(91L,sa[1]);return null;
                            case "setString": Assertions.assertEquals("sdk",sa[1]);return null;
                            case "close": return null;
                            case "executeQuery":
                                reads++;if(fail) {throw new java.sql.SQLTimeoutException("private-jdbc-url");}
                                List<Object[]> data=completion?receipt:claim;int[] index={-1};
                                return Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{java.sql.ResultSet.class},(rp,rm,ra)-> {
                                    if(rm.getName().equals("next")) {return ++index[0]<data.size();}
                                    if(rm.getName().equals("close")) {return null;}
                                    Object value=data.get(index[0])[(Integer)ra[0]-1];
                                    return switch(rm.getName()) {case "getString"->value==null?null:value.toString();case "getInt"->((Number)value).intValue();case "getLong"->((Number)value).longValue();default->throw new AssertionError(rm.getName());};
                                });
                            default: throw new AssertionError(sm.getName());
                        }
                    });
                }throw new AssertionError(m.getName());
            });
        }
        /** @return 生产固定表名的真实校验结果 */
        PaymentDedupStore.Lookup check()throws Exception {return PaymentDedupStore.observe(connection(),7,expected);}
    }
    /** @param proof 完成正文 @return 独立计算摘要，用于证明正确SHA并不能覆盖身份冲突 */
    private static String digest(String proof)throws Exception {return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(proof.getBytes(java.nio.charset.StandardCharsets.UTF_8)));}

    /** 完成行单独存在无效；有claim无receipt明确CLAIMED；全部不存在明确ABSENT，任何状态都不是清理权限。 */
    @Test void boundedReadsDistinguishMissingClaimReceiptAndCompleted()throws Exception {
        var r=new Rows();Assertions.assertEquals(PaymentDedupStore.Lookup.COMPLETED,r.check());Assertions.assertEquals(2,r.reads);Assertions.assertEquals(2,r.timeouts);Assertions.assertEquals(2,r.limits);
        r.claim.clear();Assertions.assertThrows(RuntimeException.class,r::check);
        r.receipt.clear();Assertions.assertEquals(PaymentDedupStore.Lookup.ABSENT,r.check());
        var pending=new Rows();pending.receipt.clear();Assertions.assertEquals(PaymentDedupStore.Lookup.CLAIMED,pending.check());
    }
    /** receipt元数据、版本、SDK、claim身份、token和重复行全部严格拒绝。 */
    @Test void corruptVersionsIdentityTokensAndDuplicateRowsAreRejected()throws Exception {
        for(int column=0;column<8;column++) {
            var r=new Rows();r.receipt.get(0)[column]=switch(column) {case 0->99;case 1,2->999L;default->"corrupt";};Assertions.assertThrows(RuntimeException.class,r::check);
        }
        for(int column=0;column<5;column++) {
            var r=new Rows();r.claim.get(0)[column]=column==2||column==3?999L:"corrupt";Assertions.assertThrows(RuntimeException.class,r::check);
        }
        var duplicate=new Rows();duplicate.claim.add(duplicate.claim.get(0).clone());Assertions.assertThrows(RuntimeException.class,duplicate::check);
        duplicate=new Rows();duplicate.receipt.add(duplicate.receipt.get(0).clone());Assertions.assertThrows(RuntimeException.class,duplicate::check);
    }
    /** 即便重新计算SHA，金额、SDK、全部购买参数和proof版本改变也不通过归档绑定。 */
    @Test void recomputedDigestCannotHideAlteredPurchaseOrProof()throws Exception {
        var base=new Rows();String proof=(String)base.receipt.get(0)[6];
        for(String changed:List.of(proof.replace("\"version\":1","\"version\":99"),proof.replace("sdk","other"),proof.replace("\"rmb\":12","\"rmb\":13"),
                proof.replace("\"payId\":73","\"payId\":74"),proof.replace("\"sdkGoodsId\":55","\"sdkGoodsId\":56"),proof.replace("\"addId\":100","\"addId\":101"),
                proof.replace("\"addCount\":2","\"addCount\":3"),proof.replace("FirstCharge","Recharge"),proof.replace("[1,2]","[2,1]"),
                proof.replace("\"finishTime\":"+base.expected.getFinishTime(),"\"finishTime\":1"))) {
            var r=new Rows();r.receipt.get(0)[6]=changed;r.receipt.get(0)[7]=digest(changed);Assertions.assertThrows(RuntimeException.class,r::check,changed);
        }
    }
    /** 超时按原异常传播，旧finish、UNKNOWN、缺证明不得被解析为当前完成V1。 */
    @Test void queryFailureAndLegacyCompletionNeverBecomeAbsentOrCompleted()throws Exception {
        var r=new Rows();r.fail=true;Assertions.assertThrows(java.sql.SQLTimeoutException.class,r::check);
        PayItem legacy=PaymentDurableDedupTest.item();legacy.setSdkOrderId("sdk");legacy.finish();
        Assertions.assertThrows(RuntimeException.class,()->PaymentDedupStore.validateCompletedItem(7,legacy));
        r=new Rows();r.expected.setCompletionState("UNKNOWN");Assertions.assertThrows(RuntimeException.class,r::check);
    }
}
