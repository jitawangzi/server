package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class FairyFriendFightRequest_27000003Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.FairyFriendMsg.FairyFriendFightRequest_27000003.Builder builder = cn.game.protocol.protobuf.FairyFriendMsg.FairyFriendFightRequest_27000003.newBuilder() ; 
		
		builder.setId(1);
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.FairyFriendMsg.FairyFriendFightRequest_27000003.Builder builder = cn.game.protocol.protobuf.FairyFriendMsg.FairyFriendFightRequest_27000003.newBuilder() ; 
		
		builder.setId(1);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    FairyFriendFightRequest_27000003Test instance = new FairyFriendFightRequest_27000003Test();
	    instance.start();
	}

}