package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleLingShanEndRequest_13000519Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleLingShanEndRequest_13000519.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleLingShanEndRequest_13000519.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleLingShanEndRequest_13000519.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleLingShanEndRequest_13000519.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleLingShanEndRequest_13000519Test instance = new BattleLingShanEndRequest_13000519Test();
	    instance.start();
	}

}