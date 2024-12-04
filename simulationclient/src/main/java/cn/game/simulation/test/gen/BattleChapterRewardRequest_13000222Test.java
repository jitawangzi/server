package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleChapterRewardRequest_13000222Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleChapterRewardRequest_13000222.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleChapterRewardRequest_13000222.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleChapterRewardRequest_13000222.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleChapterRewardRequest_13000222.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleChapterRewardRequest_13000222Test instance = new BattleChapterRewardRequest_13000222Test();
	    instance.start();
	}

}