package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PlayerAdBIInfoRequest_01100603Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerAdBIInfoRequest_01100603.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerAdBIInfoRequest_01100603.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerAdBIInfoRequest_01100603.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerAdBIInfoRequest_01100603.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    PlayerAdBIInfoRequest_01100603Test instance = new PlayerAdBIInfoRequest_01100603Test();
	    instance.start();
	}

}