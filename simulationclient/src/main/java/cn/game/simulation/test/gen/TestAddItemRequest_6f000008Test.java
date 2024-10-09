package cn.game.simulation.test.gen;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

//@Component
public class TestAddItemRequest_6f000008Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestAddItemRequest_6f000008.Builder builder = cn.game.protocol.protobuf.TestMsg.TestAddItemRequest_6f000008.newBuilder() ; 

//		builder.setId(Asset.playerExp.ID);
//		builder.setCount(Integer.MAX_VALUE / 2);
//		
		builder.setId(205000);
		builder.setCount(1);

		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new TestAddItemRequest_6f000008Test().getMessage(client));


	}

}