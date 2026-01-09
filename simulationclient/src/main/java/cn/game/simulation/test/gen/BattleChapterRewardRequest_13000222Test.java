package cn.game.simulation.test.gen;

import com.google.protobuf.Message;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class BattleChapterRewardRequest_13000222Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleChapterRewardRequest_13000222.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleChapterRewardRequest_13000222.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleChapterRewardRequest_13000222.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleChapterRewardRequest_13000222.newBuilder() ; 
		List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(DungeonTypeEnum.BattleChapter.getId()); 
		BattleConfig randomElement = Rnd.randomElement(battleTypeList); 
		builder.setId(randomElement.ID); 
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleChapterRewardRequest_13000222Test instance = new BattleChapterRewardRequest_13000222Test();
	    instance.start();
	}

}