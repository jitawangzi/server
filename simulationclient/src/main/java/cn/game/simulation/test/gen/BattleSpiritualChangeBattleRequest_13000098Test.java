package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleSpiritualChangeBattleRequest_13000098Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleSpiritualChangeBattleRequest_13000098.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleSpiritualChangeBattleRequest_13000098.newBuilder() ; 
		
		builder.setBattleId(31002);
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleSpiritualChangeBattleRequest_13000098.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleSpiritualChangeBattleRequest_13000098.newBuilder() ; 
		
		builder.setBattleId(31002);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new BattleSpiritualChangeBattleRequest_13000098Test().getMessage(client));

		
	}

}