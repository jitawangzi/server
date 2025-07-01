package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PlayerFigureRequest_01000021Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerFigureRequest_01000021.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerFigureRequest_01000021.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerFigureRequest_01000021.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerFigureRequest_01000021.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    PlayerFigureRequest_01000021Test instance = new PlayerFigureRequest_01000021Test();
	    instance.start();
	}

}