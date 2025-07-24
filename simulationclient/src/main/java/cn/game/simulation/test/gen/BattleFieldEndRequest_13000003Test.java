package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleFieldEndRequest_13000003Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003.newBuilder() ; 
		
		builder.setHpPercent(100);
		builder.setKillMonsterCount(3000);
		builder.setBattleTime(3);
		builder.setWin(false);
		builder.setDamage(500000);
		builder.addRichManItems(980001);
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003.newBuilder() ; 
		
		builder.setHpPercent(100);
		builder.setKillMonsterCount(3000);
		builder.setBattleTime(3);
		builder.setWin(true);
		builder.setDamage(500000);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleFieldEndRequest_13000003Test instance = new BattleFieldEndRequest_13000003Test();
	    instance.start();
	}

}