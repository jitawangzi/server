package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class QuestReceiveActivePointRequest_20000008Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.QuestMsg.QuestReceiveActivePointRequest_20000008.Builder builder = cn.game.protocol.protobuf.QuestMsg.QuestReceiveActivePointRequest_20000008.newBuilder() ; 
		builder.setType(Rnd.nextInt(1, 2));
		builder.addIndex(Rnd.get(0, 3));
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new QuestReceiveActivePointRequest_20000008Test().getMessage(client));

		
	}

}