package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleSpiritualInfoRequest_13000090Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleSpiritualInfoRequest_13000090.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleSpiritualInfoRequest_13000090.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleSpiritualInfoRequest_13000090.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleSpiritualInfoRequest_13000090.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleSpiritualInfoRequest_13000090Test instance = new BattleSpiritualInfoRequest_13000090Test();
	    instance.start();
	}

}