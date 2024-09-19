package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattlePvPEndRequest_13000115Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattlePvPEndRequest_13000115.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattlePvPEndRequest_13000115.newBuilder() ;

		if (client.isInPvPBattle()){
			builder.setWin(RandomUtils.nextBoolean());
			builder.setBattleTime(RandomUtils.nextInt(100));
			builder.setEndType(0);
			builder.setTargetId(client.getinPvPBattlePid());
			client.setInPvPBattle(0);
		}

		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new BattlePvPEndRequest_13000115Test().getMessage(client));

		
	}

}