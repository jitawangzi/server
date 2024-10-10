package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class DrawRequest_37000003Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.DrawMsg.DrawRequest_37000003.Builder builder = cn.game.protocol.protobuf.DrawMsg.DrawRequest_37000003.newBuilder() ; 
		builder.setId(Rnd.get(1, 2));
//		builder.setId(301);
		builder.setTen(true);
//		builder.setFreeOnce(true);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new DrawRequest_37000003Test().getMessage(client));

		
	}

}