package cn.game.simulation.test.gen;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.config.HeadPortraitConfig;
import cn.game.protocol.generated.manager.HeadPortraitManager;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class PlayerHeadRequest_01000013Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerHeadRequest_01000013.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerHeadRequest_01000013.newBuilder() ; 
		List<HeadPortraitConfig> list = HeadPortraitManager.instance().list(); 
		if (list != null) {
			HeadPortraitConfig randomOne = Rnd.randomOne(list); 
			builder.setHead(randomOne.ID); 
		}else {
			builder.setHead(1);
		}
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new PlayerHeadRequest_01000013Test().getMessage(client));

		
	}

}