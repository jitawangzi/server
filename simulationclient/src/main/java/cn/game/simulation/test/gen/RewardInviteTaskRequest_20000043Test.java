package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class RewardInviteTaskRequest_20000043Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.QuestMsg.RewardInviteTaskRequest_20000043.Builder builder = cn.game.protocol.protobuf.QuestMsg.RewardInviteTaskRequest_20000043.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.QuestMsg.RewardInviteTaskRequest_20000043.Builder builder = cn.game.protocol.protobuf.QuestMsg.RewardInviteTaskRequest_20000043.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    RewardInviteTaskRequest_20000043Test instance = new RewardInviteTaskRequest_20000043Test();
	    instance.start();
	}

}