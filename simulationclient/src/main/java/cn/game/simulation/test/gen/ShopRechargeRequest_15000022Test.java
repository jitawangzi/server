package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ShopRechargeRequest_15000022Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopRechargeRequest_15000022.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopRechargeRequest_15000022.newBuilder() ; 
		builder.setId(3);
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopRechargeRequest_15000022.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopRechargeRequest_15000022.newBuilder() ; 
		builder.setId(3);
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new ShopRechargeRequest_15000022Test().getMessage(client));

		
	}

}