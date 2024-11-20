package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleSpiritualGetTimesRequest_13000094Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleSpiritualGetTimesRequest_13000094.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleSpiritualGetTimesRequest_13000094.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleSpiritualGetTimesRequest_13000094.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleSpiritualGetTimesRequest_13000094.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleSpiritualGetTimesRequest_13000094Test instance = new BattleSpiritualGetTimesRequest_13000094Test();
	    instance.start();
	}

}