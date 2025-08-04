package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattlePVEVPChallengeRequest_13000552Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattlePVEVPChallengeRequest_13000552.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattlePVEVPChallengeRequest_13000552.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattlePVEVPChallengeRequest_13000552.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattlePVEVPChallengeRequest_13000552.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattlePVEVPChallengeRequest_13000552Test instance = new BattlePVEVPChallengeRequest_13000552Test();
	    instance.start();
	}

}