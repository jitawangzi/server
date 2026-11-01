package cn.game.games.net.game.module.mail;

import java.util.Map;
import cn.game.core.execute.SaveMailboxFixture;
import cn.game.core.execute.TaskExecutorService;
import cn.game.core.net.process.IdVirtualThreadProcessor;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.core.net.socket.controller.DispatcherImpl;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.MailMsg.MailListRequest_12000001;

/** 自有真实dispatcher/processor/玩家邮箱；测试只注入资源，不替换处理方法。 */
public final class MailFilterRoute implements AutoCloseable {
    /** 自有执行服务，退出时关闭。 */
    public final TaskExecutorService tasks=SaveMailboxFixture.create(32);
    /** 实际排队入口，保留RequestTiming调用。 */
    public final IdVirtualThreadProcessor processor=new IdVirtualThreadProcessor(tasks);
    /** 真实注册的邮件handler。 */
    private final MailHandler handler=new MailHandler();

    /** 注册真实协议分发，不能覆盖已有Handler。 */
    public MailFilterRoute() throws Exception {
        var dispatcher=new DispatcherImpl();
        var field=cn.game.core.net.socket.handler.BaseHandler.class.getDeclaredField("dispatcher");field.setAccessible(true);field.set(handler,dispatcher);
        field=cn.game.core.net.process.AbstractProcessor.class.getDeclaredField("dispatcher");field.setAccessible(true);field.set(processor,dispatcher);
        handler.init();
    }

    /** @param fixture 已装配的当前会话 @param request 正式请求 @param seq 唯一请求序号 @return 实际编码解码后的响应 */
    public MailFilterFixture.Reply send(MailFilterFixture fixture,MailListRequest_12000001 request,int seq) throws Exception {
        var protocol=new ProtobufProtocol();protocol.setMsgID(PbProtocol.MailListRequest_12000001);protocol.setSeq(seq);
        protocol.deserializeData(request.toByteArray());processor.process(fixture.client,protocol);return fixture.client.await();
    }

    @Override public void close() throws Exception {
        tasks.close();
        var field=DispatcherImpl.class.getDeclaredField("MODULE_HANDLERS");field.setAccessible(true);
        ((Map<Integer,?>)field.get(null)).remove(0x12,handler);
    }
}
