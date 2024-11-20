package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GmNoticeDeleteRequest_77000064Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmNoticeDeleteRequest_77000064.Builder builder = cn.game.protocol.protobuf.GmMsg.GmNoticeDeleteRequest_77000064.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmNoticeDeleteRequest_77000064.Builder builder = cn.game.protocol.protobuf.GmMsg.GmNoticeDeleteRequest_77000064.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GmNoticeDeleteRequest_77000064Test instance = new GmNoticeDeleteRequest_77000064Test();
	    instance.start();
	}

}