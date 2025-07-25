package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleTowerDataRequest_13000521Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleTowerDataRequest_13000521.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleTowerDataRequest_13000521.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleTowerDataRequest_13000521.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleTowerDataRequest_13000521.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleTowerDataRequest_13000521Test instance = new BattleTowerDataRequest_13000521Test();
	    instance.start();
	}

}