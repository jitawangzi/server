package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleLingShanFloorSkipRequest_13000517Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleLingShanFloorSkipRequest_13000517.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleLingShanFloorSkipRequest_13000517.newBuilder() ; 
		builder.setFloor(2); 
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleLingShanFloorSkipRequest_13000517.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleLingShanFloorSkipRequest_13000517.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleLingShanFloorSkipRequest_13000517Test instance = new BattleLingShanFloorSkipRequest_13000517Test();
	    instance.start();
	}

}