package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattlePvPStartRequest_13000113Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattlePvPStartRequest_13000113.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattlePvPStartRequest_13000113.newBuilder() ;

		long targetPid = 1;
		if (client.getTargetListResponse() != null && client.getTargetListResponse().getTargetListCount() > 0){
			int index = RandomUtils.nextInt(client.getTargetListResponse().getTargetListCount());
			targetPid = Long.parseLong(client.getTargetListResponse().getTargetList(index).getId());
			client.setInPvPBattle(targetPid);
		}
		builder.setTargetId(targetPid+"");

		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattlePvPStartRequest_13000113.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattlePvPStartRequest_13000113.newBuilder() ;

		long targetPid = 1;
		if (client.getTargetListResponse() != null && client.getTargetListResponse().getTargetListCount() > 0){
			int index = RandomUtils.nextInt(client.getTargetListResponse().getTargetListCount());
			targetPid = Long.parseLong(client.getTargetListResponse().getTargetList(index).getId());
			client.setInPvPBattle(targetPid);
		}
		builder.setTargetId(targetPid+"");

		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new BattlePvPStartRequest_13000113Test().getMessage(client));

		
	}

}