package cn.game.simulation.test.ai;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import cn.game.protocol.protobuf.MailMsg.MailInfo;
import cn.game.protocol.protobuf.MailMsg.MailListFilter;
import cn.game.protocol.protobuf.MailMsg.MailListRequest_12000001;
import cn.game.protocol.protobuf.MailMsg.MailListResponse_12000002;

/** 只读邮件客户端场景；同一断言可经正式登录客户端或测试自有loopback传输运行。 */
public final class ClientMailFilterTest extends ClientBaseScenarioTest {
    /** @param args 标准客户端启动参数，账号配置沿用既有入口 */
    public static void main(String[] args) { new ClientMailFilterTest().start(); }

    @Override protected void executeProcess() {
        verify(request -> this.<MailListResponse_12000002>sendAndWait(request));
    }

    /**
     * 顺序发出正式请求，不做GM或状态写入；要求测试期间邮箱不被其他写入者修改。
     * @param exchange 实际请求响应传输，不得在此函数内模拟业务结果
     * @return 三种筛选完整遍历的结果，供自有夹具进一步断言精确UID
     */
    public static Map<MailListFilter, List<MailInfo>> verify(
            Function<MailListRequest_12000001, MailListResponse_12000002> exchange) {
        // 顺序协议也须遵守GameClient每秒十条的既有限流，不修改服务端限制。
        Function<MailListRequest_12000001, MailListResponse_12000002> paced = request -> {
            try { Thread.sleep(120); }
            catch (InterruptedException failure) {
                Thread.currentThread().interrupt();
                org.slf4j.LoggerFactory.getLogger(ClientMailFilterTest.class).warn("邮件场景节流等待中断", failure);
                throw new IllegalStateException("邮件场景中断", failure);
            }
            return exchange.apply(request);
        };
        var result = new EnumMap<MailListFilter,List<MailInfo>>(MailListFilter.class);
        for (var filter : List.of(MailListFilter.ALL, MailListFilter.UNREAD, MailListFilter.HAS_CLAIMABLE_ATTACHMENT)) {
            List<MailInfo> all = new ArrayList<>(); var seen = new HashSet<String>(); String cursor="";
            for (int pages=0; ; pages++) {
                if (pages >= 1000) { throw new AssertionError("邮件翻页未在有限步结束"); }
                var request = MailListRequest_12000001.newBuilder().setFilter(filter).setPageSize(2).setCursor(cursor).build();
                var response = paced.apply(request);
                if (!response.equals(paced.apply(request))) { throw new AssertionError("重复页不一致"); }
                if (response.getMailsCount()>2) { throw new AssertionError("页大小超限"); }
                for(var mail:response.getMailsList()) {
                    if (!seen.add(mail.getUid())) { throw new AssertionError("邮件跨页重复"); }
                    if(filter==MailListFilter.UNREAD && mail.getSee()) { throw new AssertionError("未读筛选包含已读"); }
                    if(filter==MailListFilter.HAS_CLAIMABLE_ATTACHMENT && mail.getReceive()) { throw new AssertionError("可领取筛选包含已领取"); }
                    if(!all.isEmpty()) {
                        var previous=all.getLast();
                        if(previous.getTime()<mail.getTime() || previous.getTime()==mail.getTime()
                                && Long.parseLong(previous.getUid())<=Long.parseLong(mail.getUid())) { throw new AssertionError("邮件顺序错误"); }
                    }
                    all.add(mail);
                }
                cursor=response.getNextCursor(); if(cursor.isEmpty()){break;}
                if(response.getMailsCount()==0){throw new AssertionError("空页不能带后续游标");}
            }
            result.put(filter,List.copyOf(all));
        }
        var legacy=paced.apply(MailListRequest_12000001.getDefaultInstance());
        if(!legacy.getNextCursor().isEmpty() || !legacy.getMailsList().equals(result.get(MailListFilter.ALL))) { throw new AssertionError("旧空请求不再返回完整ALL列表"); }
        var allIds=result.get(MailListFilter.ALL).stream().map(MailInfo::getUid).toList();
        for(var rows:result.values()){for(var row:rows){if(!allIds.contains(row.getUid())){throw new AssertionError("筛选返回邮箱外邮件");}}}
        return result;
    }
}
