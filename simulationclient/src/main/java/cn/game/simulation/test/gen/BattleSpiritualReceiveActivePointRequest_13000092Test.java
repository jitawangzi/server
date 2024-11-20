package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleSpiritualReceiveActivePointRequest_13000092Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleSpiritualReceiveActivePointRequest_13000092.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleSpiritualReceiveActivePointRequest_13000092.newBuilder() ; 
		
		builder.addIndex(-1);
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleSpiritualReceiveActivePointRequest_13000092.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleSpiritualReceiveActivePointRequest_13000092.newBuilder() ; 
		
		builder.addIndex(-1);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleSpiritualReceiveActivePointRequest_13000092Test instance = new BattleSpiritualReceiveActivePointRequest_13000092Test();
	    instance.start();
	}

}