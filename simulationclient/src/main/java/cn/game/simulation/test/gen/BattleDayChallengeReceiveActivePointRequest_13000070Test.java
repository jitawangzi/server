package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class BattleDayChallengeReceiveActivePointRequest_13000070Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleDayChallengeReceiveActivePointRequest_13000070.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleDayChallengeReceiveActivePointRequest_13000070.newBuilder() ; 
		
		builder.setIndex(Rnd.nextInt(3));
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleDayChallengeReceiveActivePointRequest_13000070.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleDayChallengeReceiveActivePointRequest_13000070.newBuilder() ; 
		
		builder.setIndex(Rnd.nextInt(3));
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleDayChallengeReceiveActivePointRequest_13000070Test instance = new BattleDayChallengeReceiveActivePointRequest_13000070Test();
	    instance.start();
	}

}