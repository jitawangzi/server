package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PlayerAssetDataRequest_01000200Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerAssetDataRequest_01000200.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerAssetDataRequest_01000200.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerAssetDataRequest_01000200.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerAssetDataRequest_01000200.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    PlayerAssetDataRequest_01000200Test instance = new PlayerAssetDataRequest_01000200Test();
	    instance.start();
	}

}