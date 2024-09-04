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
public class BattleSweepRequest_13000024Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleSweepRequest_13000024.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleSweepRequest_13000024.newBuilder() ; 
		Collection<BattleConfig> list = BattleManager.instance().getBattleTypeList(1);
		if (list != null) {
			for (BattleConfig battleConfig : list) {
				if (battleConfig.preBattle == 0) {
					builder.setId(battleConfig.ID);
					break;
				}
			}
		} else {
			builder.setId(10101);
		}
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new BattleSweepRequest_13000024Test().getMessage(client));

		
	}

}