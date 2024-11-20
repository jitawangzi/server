package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class UnionTitleChangeRequest_33000012Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.UnionMsg.UnionTitleChangeRequest_33000012.Builder builder = cn.game.protocol.protobuf.UnionMsg.UnionTitleChangeRequest_33000012.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.UnionMsg.UnionTitleChangeRequest_33000012.Builder builder = cn.game.protocol.protobuf.UnionMsg.UnionTitleChangeRequest_33000012.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    UnionTitleChangeRequest_33000012Test instance = new UnionTitleChangeRequest_33000012Test();
	    instance.start();
	}

}