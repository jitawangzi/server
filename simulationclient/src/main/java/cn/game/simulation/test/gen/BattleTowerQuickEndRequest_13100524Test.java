package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleTowerQuickEndRequest_13100524Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleTowerQuickEndRequest_13100524.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleTowerQuickEndRequest_13100524.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleTowerQuickEndRequest_13100524.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleTowerQuickEndRequest_13100524.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleTowerQuickEndRequest_13100524Test instance = new BattleTowerQuickEndRequest_13100524Test();
	    instance.start();
	}

}