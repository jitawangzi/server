package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleBuyPvPTimeRequest_13000121Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleBuyPvPTimeRequest_13000121.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleBuyPvPTimeRequest_13000121.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleBuyPvPTimeRequest_13000121.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleBuyPvPTimeRequest_13000121.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleBuyPvPTimeRequest_13000121Test instance = new BattleBuyPvPTimeRequest_13000121Test();
	    instance.start();
	}

}