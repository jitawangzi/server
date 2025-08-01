package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleMountainMapResetRequest_13000545Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleMountainMapResetRequest_13000545.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleMountainMapResetRequest_13000545.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleMountainMapResetRequest_13000545.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleMountainMapResetRequest_13000545.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleMountainMapResetRequest_13000545Test instance = new BattleMountainMapResetRequest_13000545Test();
	    instance.start();
	}

}