package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PlayerQuestionnaireRewardRequest_01000302Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerQuestionnaireRewardRequest_01000302.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerQuestionnaireRewardRequest_01000302.newBuilder() ; 
		
		builder.setId(2);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new PlayerQuestionnaireRewardRequest_01000302Test().getMessage(client));

		
	}

}