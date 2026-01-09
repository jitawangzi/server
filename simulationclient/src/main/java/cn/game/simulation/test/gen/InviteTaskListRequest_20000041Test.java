package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class InviteTaskListRequest_20000041Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.QuestMsg.InviteTaskListRequest_20000041.Builder builder = cn.game.protocol.protobuf.QuestMsg.InviteTaskListRequest_20000041.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.QuestMsg.InviteTaskListRequest_20000041.Builder builder = cn.game.protocol.protobuf.QuestMsg.InviteTaskListRequest_20000041.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    InviteTaskListRequest_20000041Test instance = new InviteTaskListRequest_20000041Test();
	    instance.start();
	}

}