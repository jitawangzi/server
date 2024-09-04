package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PetBondsUpLevelRequest_19000015Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.PetMsg.PetBondsUpLevelRequest_19000015.Builder builder = cn.game.protocol.protobuf.PetMsg.PetBondsUpLevelRequest_19000015.newBuilder() ; 
		
		builder.setId(1);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new PetBondsUpLevelRequest_19000015Test().getMessage(client));

		
	}

}