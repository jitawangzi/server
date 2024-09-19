package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityFirstChargeRewardRequest_11000012Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardRequest_11000012.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardRequest_11000012.newBuilder() ;

		builder.setId(10);
		builder.setChargeId(1);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new ActivityFirstChargeRewardRequest_11000012Test().getMessage(client));

		
	}

}