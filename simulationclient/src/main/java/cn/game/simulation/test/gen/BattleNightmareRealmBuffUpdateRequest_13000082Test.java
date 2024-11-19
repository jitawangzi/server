package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleNightmareRealmBuffUpdateRequest_13000082Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmBuffUpdateRequest_13000082.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmBuffUpdateRequest_13000082.newBuilder() ; 
		builder.putBuffIds(1, 1);
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmBuffUpdateRequest_13000082.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmBuffUpdateRequest_13000082.newBuilder() ; 
		builder.putBuffIds(1, 1);
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new BattleNightmareRealmBuffUpdateRequest_13000082Test().getMessage(client));

		
	}

}