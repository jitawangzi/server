package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleLingShanBuyTimesRequest_13000513Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleLingShanBuyTimesRequest_13000513.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleLingShanBuyTimesRequest_13000513.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleLingShanBuyTimesRequest_13000513.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleLingShanBuyTimesRequest_13000513.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleLingShanBuyTimesRequest_13000513Test instance = new BattleLingShanBuyTimesRequest_13000513Test();
	    instance.start();
	}

}