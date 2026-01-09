package cn.game.simulation.test.gen;

import cn.game.protocol.protobuf.BaseMsg;
import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GmMailServerSendRequest_77000048Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmMailServerSendRequest_77000048.Builder builder = cn.game.protocol.protobuf.GmMsg.GmMailServerSendRequest_77000048.newBuilder() ; 

		int now = (int) (System.currentTimeMillis()/1000L);
		builder.setTitle("邮件测试");
		builder.setContent("邮件测试1111111111111111");
		builder.setSendStartTime(now);
		builder.setSendEndTime(now + 100000);
		builder.setLevelStart(1);
		builder.setLevelEnd(99);
		builder.setTimeCheckType(0);
		builder.addAttachments(BaseMsg.GoodsInfo.newBuilder().setId(100001).setCount(100).build());
		builder.addAttachments(BaseMsg.GoodsInfo.newBuilder().setId(100002).setCount(1000).build());
		builder.addAttachments(BaseMsg.GoodsInfo.newBuilder().setId(100003).setCount(1000).build());
		builder.addAttachments(BaseMsg.GoodsInfo.newBuilder().setId(100004).setCount(1000).build());

		
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmMailServerSendRequest_77000048.Builder builder = cn.game.protocol.protobuf.GmMsg.GmMailServerSendRequest_77000048.newBuilder() ; 

		int now = (int) (System.currentTimeMillis()/1000L);
		builder.setTitle("邮件测试");
		builder.setContent("邮件测试1111111111111111");
		builder.setSendStartTime(now);
		builder.setSendEndTime(now + 100000);
		builder.setLevelStart(1);
		builder.setLevelEnd(99);
		builder.setTimeCheckType(0);
		builder.addAttachments(BaseMsg.GoodsInfo.newBuilder().setId(100001).setCount(100).build());
		builder.addAttachments(BaseMsg.GoodsInfo.newBuilder().setId(100002).setCount(1000).build());
		builder.addAttachments(BaseMsg.GoodsInfo.newBuilder().setId(100003).setCount(1000).build());
		builder.addAttachments(BaseMsg.GoodsInfo.newBuilder().setId(100004).setCount(1000).build());

		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GmMailServerSendRequest_77000048Test instance = new GmMailServerSendRequest_77000048Test();
	    instance.start();
	}

}