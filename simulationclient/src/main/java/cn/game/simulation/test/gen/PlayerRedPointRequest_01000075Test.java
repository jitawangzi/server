package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.enume.InitialUI;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PlayerRedPointRequest_01000075Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerRedPointRequest_01000075.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerRedPointRequest_01000075.newBuilder() ; 
		
		builder.addType(InitialUI.YaoWangBiePao.ID);
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerRedPointRequest_01000075.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerRedPointRequest_01000075.newBuilder() ; 
		
		builder.addType(InitialUI.YaoWangBiePao.ID);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    PlayerRedPointRequest_01000075Test instance = new PlayerRedPointRequest_01000075Test();
	    instance.start();
	}

}