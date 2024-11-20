package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GmNoticeAddRequest_77000060Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmNoticeAddRequest_77000060.Builder builder = cn.game.protocol.protobuf.GmMsg.GmNoticeAddRequest_77000060.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmNoticeAddRequest_77000060.Builder builder = cn.game.protocol.protobuf.GmMsg.GmNoticeAddRequest_77000060.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GmNoticeAddRequest_77000060Test instance = new GmNoticeAddRequest_77000060Test();
	    instance.start();
	}

}