package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ItemSellRequest_0b000007Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.ItemMsg.ItemSellRequest_0b000007.Builder builder = cn.game.protocol.protobuf.ItemMsg.ItemSellRequest_0b000007.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.ItemMsg.ItemSellRequest_0b000007.Builder builder = cn.game.protocol.protobuf.ItemMsg.ItemSellRequest_0b000007.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ItemSellRequest_0b000007Test instance = new ItemSellRequest_0b000007Test();
	    instance.start();
	}

}