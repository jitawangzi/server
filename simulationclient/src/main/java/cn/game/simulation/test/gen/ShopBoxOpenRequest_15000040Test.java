package cn.game.simulation.test.gen;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

//@Component
public class ShopBoxOpenRequest_15000040Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopBoxOpenRequest_15000040.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopBoxOpenRequest_15000040.newBuilder() ; 
		
		builder.setWatchAds(false);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new ShopBoxOpenRequest_15000040Test().getMessage(client));

		
	}

}