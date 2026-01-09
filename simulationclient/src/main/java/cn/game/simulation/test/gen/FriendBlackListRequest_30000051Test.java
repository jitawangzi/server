package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class FriendBlackListRequest_30000051Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.FriendMsg.FriendBlackListRequest_30000051.Builder builder = cn.game.protocol.protobuf.FriendMsg.FriendBlackListRequest_30000051.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.FriendMsg.FriendBlackListRequest_30000051.Builder builder = cn.game.protocol.protobuf.FriendMsg.FriendBlackListRequest_30000051.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    FriendBlackListRequest_30000051Test instance = new FriendBlackListRequest_30000051Test();
	    instance.start();
	}

}