package cn.game.simulation.test.gen;

import java.util.Collection;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleRewardRequest_13000022Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleRewardRequest_13000022.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleRewardRequest_13000022.newBuilder() ; 
		
		Collection<BattleConfig> list = BattleManager.instance().getBattleTypeList(1);
		if (list != null) {
			for (BattleConfig battleConfig : list) {
				if (battleConfig.preBattle == 0) {
					builder.addId(battleConfig.ID);
					builder.addIndex(0);
					break;
				}
			}
		} else {
			builder.addId(10101);
			builder.addIndex(0);
		}
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new BattleRewardRequest_13000022Test().getMessage(client));

		
	}

}