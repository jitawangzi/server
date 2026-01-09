package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.protocol.generated.manager.ItemManager;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleHandCardRequest_13000600Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleHandCardRequest_13000600.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleHandCardRequest_13000600.newBuilder() ; 
		builder.setId(215001); 
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleHandCardRequest_13000600.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleHandCardRequest_13000600.newBuilder() ; 
		builder.setId(215001); 
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleHandCardRequest_13000600Test instance = new BattleHandCardRequest_13000600Test();
	    instance.start();
	}

}