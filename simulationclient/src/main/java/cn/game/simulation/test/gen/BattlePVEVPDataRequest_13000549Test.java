package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattlePVEVPDataRequest_13000549Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattlePVEVPDataRequest_13000549.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattlePVEVPDataRequest_13000549.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattlePVEVPDataRequest_13000549.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattlePVEVPDataRequest_13000549.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattlePVEVPDataRequest_13000549Test instance = new BattlePVEVPDataRequest_13000549Test();
	    instance.start();
	}

}