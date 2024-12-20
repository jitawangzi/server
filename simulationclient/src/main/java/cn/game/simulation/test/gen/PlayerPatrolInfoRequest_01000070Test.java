package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PlayerPatrolInfoRequest_01000070Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerPatrolInfoRequest_01000070.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerPatrolInfoRequest_01000070.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerPatrolInfoRequest_01000070.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerPatrolInfoRequest_01000070.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    PlayerPatrolInfoRequest_01000070Test instance = new PlayerPatrolInfoRequest_01000070Test();
	    instance.start();
	}

}