package cn.game.games.net.game.module.mail;

import java.util.ArrayList;
import java.util.List;
import cn.game.core.exception.LogicException;
import cn.game.games.cache.entity.Mail;
import cn.game.protocol.protobuf.MailMsg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/** 对真实分页、资格与协议兼容逐项断言；不使用数据库，也不跳过坏输入。 */
@Isolated
class MailFilterTest {
    /** 真实注册、编解码、邮箱及handler路径：旧空请求完整返回，新ALL默认50封，且没有实体或模块变化。 */
    @Test void actualRoutePreservesLegacyAndBoundsNewDefaultWithoutEffects() throws Exception {
        try(var f=new MailFilterFixture(91234001); var route=new MailFilterRoute()){
            var rows=rows(105);f.load(rows);int modules=f.player.getModules().size();
            var legacy=route.send(f,MailMsg.MailListRequest_12000001.getDefaultInstance(),1);
            Assertions.assertEquals(0,legacy.error());Assertions.assertEquals(105,((MailMsg.MailListResponse_12000002)legacy.message()).getMailsCount());
            var request=MailMsg.MailListRequest_12000001.newBuilder().setFilter(MailMsg.MailListFilter.ALL).build();
            var reply=route.send(f,request,2);Assertions.assertEquals(0,reply.error());
            Assertions.assertEquals(50,((MailMsg.MailListResponse_12000002)reply.message()).getMailsCount());
            Assertions.assertEquals(modules,f.player.getModules().size());
            var field=Mail.class.getDeclaredField("attachmentList");field.setAccessible(true);
            for(var row:rows){Assertions.assertFalse(row.getSee());Assertions.assertFalse(row.getReceive());Assertions.assertNull(field.get(row));}
            Assertions.assertTrue(f.client.replies.isEmpty());
        }
    }
    /** 明确错误原因经真实响应返回，未知筛选和显式0页大小不能变成成功空页。 */
    @Test void actualRouteRejectsUnknownAndInvalidPageWithReasons() throws Exception {
        try(var f=new MailFilterFixture(91234001); var route=new MailFilterRoute()){
            int seq=1;
            for(var request:List.of(MailMsg.MailListRequest_12000001.newBuilder().setFilterValue(99).build(),
                    MailMsg.MailListRequest_12000001.newBuilder().setPageSize(0).build(),
                    MailMsg.MailListRequest_12000001.newBuilder().setCursor("x".repeat(10000)).build())){
                var reply=route.send(f,request,seq++);Assertions.assertEquals(52,reply.error());
                Assertions.assertTrue(((cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099)reply.message()).getError().startsWith("MAIL_"));
            }
        }
    }
    /** 失效连接即使保留相同玩家ID也不能查询，未认证状态及退出状态分别拒绝。 */
    @Test void actualRouteRejectsUnauthenticatedAndReplacedSessions() throws Exception {
        try(var f=new MailFilterFixture(91234001);var route=new MailFilterRoute()){
            f.load(rows(1));
            Assertions.assertEquals(0,route.send(f,MailMsg.MailListRequest_12000001.getDefaultInstance(),1).error());
            route.processor.processAsync(f.player.getPlayerId(),()->{},false).toCompletionStage().toCompletableFuture().get(10,java.util.concurrent.TimeUnit.SECONDS);
            f.player.setGameClient(new MailFilterFixture.Client());
            var replay=route.send(f,MailMsg.MailListRequest_12000001.getDefaultInstance(),1);
            Assertions.assertEquals(5,replay.error());Assertions.assertEquals(1,replay.sequence());
            Assertions.assertTrue(f.client.replies.isEmpty());f.player.setGameClient(f.client);
            f.client.state=cn.game.games.net.client.GameClient.GameClientState.CONNECTED;
            var fresh=route.send(f,MailMsg.MailListRequest_12000001.getDefaultInstance(),2);
            Assertions.assertEquals(5,fresh.error());Assertions.assertEquals(2,fresh.sequence());
            f.client.state=cn.game.games.net.client.GameClient.GameClientState.IN_GAME;f.player.setGameClient(new MailFilterFixture.Client());
            Assertions.assertEquals(5,route.send(f,MailMsg.MailListRequest_12000001.getDefaultInstance(),3).error());
            f.player.setGameClient(f.client);f.player.setIslogouting(true);
            var logout=route.send(f,MailMsg.MailListRequest_12000001.getDefaultInstance(),4);
            Assertions.assertEquals(22,logout.error());Assertions.assertEquals(4,logout.sequence());
        }
    }
    /** 已排队的写状态必须先于后续列表生效，继续页按领取和已读新状态筛选。 */
    @Test void actualMailboxOrdersStatusBeforeList() throws Exception {
        try(var f=new MailFilterFixture(91234001);var route=new MailFilterRoute()){
            var rows=rows(4);for(var row:rows){row.setAttachment("[{\"id\":100001,\"count\":1}]");}f.load(rows);
            var first=f.module.page(2,1,"");
            route.processor.processAsync(f.player.getPlayerId(),()->{rows.get(2).setReceive(true);rows.get(1).setIsDeleted(true);rows.getFirst().setSee(true);},false);
            var request=MailMsg.MailListRequest_12000001.newBuilder().setFilterValue(2).setPageSize(10).setCursor(first.nextCursor()).build();
            var reply=route.send(f,request,1);Assertions.assertEquals(0,reply.error());
            Assertions.assertEquals(List.of("1"),((MailMsg.MailListResponse_12000002)reply.message()).getMailsList().stream().map(MailMsg.MailInfo::getUid).toList());
        }
    }
    /** 三种筛选独立，未读只取see=false，已领取仍可出现在ALL。 */
    @Test void allUnreadAndClaimableAreIndependent() throws Exception {
        try (var f = new MailFilterFixture(91234001)) {
            Mail a=MailFilterFixture.mail(3,91234001,100), b=MailFilterFixture.mail(2,91234001,100), c=MailFilterFixture.mail(1,91234001,100);
            a.setAttachment("[{\"id\":100001,\"count\":1}]"); b.setSee(true); c.setReceive(true);
            var rows=List.of(a,b,c);
            Assertions.assertEquals(List.of(3L,2L,1L),ids(page(rows,0,10,"")));
            Assertions.assertEquals(List.of(3L,1L),ids(page(rows,1,10,"")));
            Assertions.assertEquals(List.of(3L),ids(page(rows,2,10,"")));
        }
    }
    /** 倒序同秒按ID，重复页稳定且遍历不重复。 */
    @Test void tiedTimesAndRepeatedPagesAreDeterministic() {
        var rows=rows(7); var first=page(rows,0,2,""); var second=page(rows,0,2,first.nextCursor());
        Assertions.assertEquals(List.of(7L,6L),ids(first)); Assertions.assertEquals(List.of(5L,4L),ids(second));
        Assertions.assertEquals(second,page(rows,0,2,first.nextCursor()));
        var seen=new java.util.HashSet<Long>(); String cursor="";
        do { var p=page(rows,0,2,cursor); for(long id:ids(p)){ Assertions.assertTrue(seen.add(id)); } cursor=p.nextCursor(); } while(!cursor.isEmpty());
        Assertions.assertEquals(7,seen.size());
    }
    /** 页大小包含1和100边界，0、负值及101必须拒绝。 */
    @Test void pageBoundsAreExplicit() {
        Assertions.assertEquals(1,page(rows(105),0,1,"").mails().size());
        Assertions.assertEquals(100,page(rows(105),0,100,"").mails().size());
        for(int size:new int[]{0,-1,101,Integer.MAX_VALUE}){ Assertions.assertThrows(LogicException.class,()->page(rows(1),0,size,"")); }
    }
    /** 未知与负枚举不会被悄悄解释为ALL。 */
    @Test void unknownFilterValuesAreRejected() {
        for(int filter:new int[]{-1,3,Integer.MAX_VALUE}){ Assertions.assertThrows(LogicException.class,()->page(rows(1),filter,1,"")); }
    }
    /** 超长、非规范和签名篡改游标都在扫描前拒绝。 */
    @Test void malformedAndTamperedCursorsAreRejected() {
        String valid=page(rows(2),0,1,"").nextCursor();
        for(String bad:List.of("!", "x".repeat(10000),"=".repeat(92),valid.substring(0,10)+(valid.charAt(10)=='A'?'B':'A')+valid.substring(11))){
            Assertions.assertThrows(LogicException.class,()->page(rows(2),0,1,bad));
        }
    }
    /** 有效签名也不能跨玩家或筛选使用。 */
    @Test void cursorsAreBoundToPlayerAndFilter() {
        var p=page(rows(2),0,1,"");
        Assertions.assertThrows(LogicException.class,()->MailPage.select(rows(2),91234002,0,1,p.nextCursor(),100));
        Assertions.assertThrows(LogicException.class,()->page(rows(2),1,1,p.nextCursor()));
    }
    /** 起始上界前的新到邮件留给刷新；游标后的新邮件可见，不承诺历史快照。 */
    @Test void arrivalsRespectUpperBoundaryWithLiveTail() {
        var rows=rows(3); var first=page(rows,0,1,"");
        rows.add(MailFilterFixture.mail(9,91234001,100)); rows.add(MailFilterFixture.mail(8,91234001,99));
        Assertions.assertEquals(List.of(2L,1L,8L),ids(page(rows,0,10,first.nextCursor())));
    }
    /** 页间已读/已领/删除按新状态过滤，游标锚点删除仍可继续。 */
    @Test void statusChangesAndDeletedAnchorUseLiveState() {
        var rows=rows(5); var first=page(rows,1,1,"");
        rows.get(0).setSee(true); rows.get(1).setIsDeleted(true); rows.removeLast();
        Assertions.assertEquals(List.of(4L,3L),ids(page(rows,1,10,first.nextCursor())));
    }
    /** 即使内存混入其他玩家或删除标记，也不能返回。 */
    @Test void foreignAndDeletedRowsNeverLeak() {
        var rows=rows(2); rows.get(0).setPlayerId(91234002); rows.get(1).setIsDeleted(true);
        Assertions.assertTrue(page(rows,0,10,"").mails().isEmpty());
    }
    /** 一万行仍只返回有限页；每个候选创建时间仅查询有限次，避免平方遍历。 */
    @Test void largeMailboxHasFiniteLinearScanAndBoundedPage() {
        var reads=new java.util.concurrent.atomic.AtomicInteger(); var rows=new ArrayList<Mail>();
        for(int i=1;i<=10000;i++){ Mail mail=new Mail(){ @Override public int getCreateTime(){reads.incrementAndGet();return 100;} }; mail.setId(i);mail.setPlayerId(91234001);rows.add(mail); }
        var result=page(rows,0,100,""); Assertions.assertEquals(100,result.mails().size());
        Assertions.assertEquals(10000L,result.mails().getFirst().getId()); Assertions.assertTrue(reads.get()<10000*40);
    }
    /** 原空字节可解析为ALL，显式0页大小仍保留presence，旧响应字段号不变。 */
    @Test void legacyProtobufAndUnknownEnumRoundTrip() throws Exception {
        var old=MailMsg.MailListRequest_12000001.parseFrom(new byte[0]);
        Assertions.assertEquals(0,old.getFilterValue()); Assertions.assertFalse(old.hasFilter());Assertions.assertFalse(old.hasPageSize());
        var unknown=MailMsg.MailListRequest_12000001.newBuilder().setFilterValue(99).setPageSize(0).build();
        var copy=MailMsg.MailListRequest_12000001.parseFrom(unknown.toByteArray());
        Assertions.assertEquals(99,copy.getFilterValue());Assertions.assertTrue(copy.hasFilter()); Assertions.assertTrue(copy.hasPageSize());
        Assertions.assertEquals(1,MailMsg.MailListResponse_12000002.getDescriptor().findFieldByName("mails").getNumber());
        Assertions.assertThrows(com.google.protobuf.InvalidProtocolBufferException.class,()->MailMsg.MailListRequest_12000001.parseFrom(new byte[]{18,(byte)255}));
    }
    /** 显式附件优先，配置奖励支持一行多组ID/数量，不借用mailId存在作资格。 */
    @Test void explicitAndConfiguredRewardsMatchClaimGuard() throws Exception {
        try(var f=new MailFilterFixture(91234001)){
            f.config(901,2,0,"100001;1;100002;2"); Mail mail=rows(1).getFirst();mail.setMailId(901);
            Assertions.assertTrue(MailRewards.claimable(mail,100)); mail.setAttachment("[{\"id\":100001,\"count\":2}]");
            Assertions.assertTrue(MailRewards.claimable(mail,100));
            mail.setReceive(true); Assertions.assertFalse(MailRewards.claimable(mail,100));
        }
    }
    /** 缺失、空及畸形配置均不展示可领取。 */
    @Test void missingEmptyAndMalformedConfigAreNotClaimable() throws Exception {
        try(var f=new MailFilterFixture(91234001)){
            Mail mail=rows(1).getFirst();
            for(String reward:List.of("","100001","100001;0","100001;1;100002","100001;-1")){
                f.config(901,2,0,reward);mail.setMailId(901);Assertions.assertFalse(MailRewards.claimable(mail,100));
            }
            mail.setMailId(902);mail.setAttachment("[{\"id\":100001,\"count\":1}]");Assertions.assertFalse(MailRewards.claimable(mail,100));
        }
    }
    /** 空显式附件可回退配置；非法附件不得借配表绕过。 */
    @Test void malformedAttachmentsCannotFallbackOrMutateCache() throws Exception {
        try(var f=new MailFilterFixture(91234001)){
            f.config(901,2,0,"100001;1");Mail mail=rows(1).getFirst();mail.setMailId(901);
            for(String raw:List.of("[]","null"," ")){mail.setAttachment(raw);Assertions.assertTrue(MailRewards.claimable(mail,100));}
            for(String raw:List.of("{", "{}","[null]","[{\"id\":1,\"count\":0}]","[{\"id\":1.5,\"count\":1}]","[{\"id\":1,\"id\":2,\"count\":1}]","[] true","x".repeat(65537))){
                mail.setAttachment(raw);Assertions.assertFalse(MailRewards.claimable(mail,100));Assertions.assertEquals(raw,mail.getAttachment());
            }
            var field=Mail.class.getDeclaredField("attachmentList");field.setAccessible(true);Assertions.assertNull(field.get(mail));
        }
    }
    /** 截止秒仍有效，下一秒失效；加法用long避免2038附近溢出。 */
    @Test void expiryBoundaryOverflowFutureAndNotice() throws Exception {
        try(var f=new MailFilterFixture(91234001)){
            f.config(901,2,10,"100001;1");Mail mail=rows(1).getFirst();mail.setMailId(901);
            Assertions.assertFalse(MailRewards.claimable(mail,99)); Assertions.assertTrue(MailRewards.claimable(mail,110));Assertions.assertFalse(MailRewards.claimable(mail,111));
            mail.setCreateTime(Integer.MAX_VALUE-2);Assertions.assertTrue(MailRewards.claimable(mail,(long)Integer.MAX_VALUE+2));
            mail.setType((byte)1);Assertions.assertFalse(MailRewards.claimable(mail,(long)Integer.MAX_VALUE+2));
            mail.setType((byte)2);f.config(901,1,0,"100001;1");Assertions.assertFalse(MailRewards.claimable(mail,(long)Integer.MAX_VALUE+2));
        }
    }
    /** 真实领取入口对不合格项不标记、不发奖且不尝试DAO。 */
    @Test void invalidReceiveIsReadOnlyAndAgreesWithFilter() throws Exception {
        try(var f=new MailFilterFixture(91234001)){
            Mail mail=rows(1).getFirst();mail.setMailId(900); f.load(List.of(mail));
            Assertions.assertTrue(f.module.receive(mail.getId()).isEmpty());Assertions.assertFalse(mail.getReceive());Assertions.assertFalse(mail.getSee());
            Assertions.assertTrue(f.module.page(2,10,"").mails().isEmpty());
        }
    }
    /** @param count 邮件数 @return 同秒有序ID的独立实体 */
    private static ArrayList<Mail> rows(int count){var rows=new ArrayList<Mail>();for(int i=1;i<=count;i++){rows.add(MailFilterFixture.mail(i,91234001,100));}return rows;}
    /** @param rows 邮件 @param filter 筛选 @param size 页大小 @param cursor 游标 @return 实际分页结果 */
    private static MailPage.Result page(List<Mail> rows,int filter,int size,String cursor){return MailPage.select(rows,91234001,filter,size,cursor,100);}
    /** @param page 真实页 @return 有序邮件主键 */
    private static List<Long> ids(MailPage.Result page){return page.mails().stream().map(Mail::getId).toList();}
}
