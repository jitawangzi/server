package cn.game.simulation.test.gen;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PlayerNameRequest_01000011Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerNameRequest_01000011.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerNameRequest_01000011.newBuilder() ; 
		
		builder.setName(UUID.randomUUID().toString());
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerNameRequest_01000011.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerNameRequest_01000011.newBuilder() ; 
		
		builder.setName(UUID.randomUUID().toString());
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    PlayerNameRequest_01000011Test instance = new PlayerNameRequest_01000011Test();
	    instance.start();
	}

}