package cn.game.simulation.test.gen;

import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

import java.util.List;

@Component
public class BattleDaoHeartSweepRequest_13000060Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepRequest_13000060.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepRequest_13000060.newBuilder() ;

		int type = 2;
		builder.setType(type);

		List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(type);
		if (battleTypeList != null) {
			builder.setId(battleTypeList.get(0).ID);
		} else {
			builder.setId(20001);
		}
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepRequest_13000060.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepRequest_13000060.newBuilder() ;

		int type = 2;
		builder.setType(type);

		List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(type);
		if (battleTypeList != null) {
			builder.setId(battleTypeList.get(0).ID);
		} else {
			builder.setId(20001);
		}
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new BattleDaoHeartSweepRequest_13000060Test().getMessage(client));

		
	}

}