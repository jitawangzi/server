package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class UnionListRequest_33000001Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.UnionMsg.UnionListRequest_33000001.Builder builder = cn.game.protocol.protobuf.UnionMsg.UnionListRequest_33000001.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.UnionMsg.UnionListRequest_33000001.Builder builder = cn.game.protocol.protobuf.UnionMsg.UnionListRequest_33000001.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    UnionListRequest_33000001Test instance = new UnionListRequest_33000001Test();
	    instance.start();
	}

}