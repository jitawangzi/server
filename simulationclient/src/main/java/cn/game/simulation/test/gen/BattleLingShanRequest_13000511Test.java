package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleLingShanRequest_13000511Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleLingShanRequest_13000511.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleLingShanRequest_13000511.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleLingShanRequest_13000511.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleLingShanRequest_13000511.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleLingShanRequest_13000511Test instance = new BattleLingShanRequest_13000511Test();
	    instance.start();
	}

}