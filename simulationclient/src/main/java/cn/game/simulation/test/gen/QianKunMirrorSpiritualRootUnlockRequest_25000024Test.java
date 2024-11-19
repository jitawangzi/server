package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class QianKunMirrorSpiritualRootUnlockRequest_25000024Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorSpiritualRootUnlockRequest_25000024.Builder builder = cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorSpiritualRootUnlockRequest_25000024.newBuilder() ; 
		
		builder.setId(1001002);
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorSpiritualRootUnlockRequest_25000024.Builder builder = cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorSpiritualRootUnlockRequest_25000024.newBuilder() ; 
		
		builder.setId(1001002);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new QianKunMirrorSpiritualRootUnlockRequest_25000024Test().getMessage(client));

		
	}

}