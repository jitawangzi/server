package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleLostInfoRequest_13000201Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleLostInfoRequest_13000201.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleLostInfoRequest_13000201.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleLostInfoRequest_13000201.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleLostInfoRequest_13000201.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleLostInfoRequest_13000201Test instance = new BattleLostInfoRequest_13000201Test();
	    instance.start();
	}

}