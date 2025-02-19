package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ZongMenBountyBattleEndRequest_40000078Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyBattleEndRequest_40000078.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyBattleEndRequest_40000078.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyBattleEndRequest_40000078.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyBattleEndRequest_40000078.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ZongMenBountyBattleEndRequest_40000078Test instance = new ZongMenBountyBattleEndRequest_40000078Test();
	    instance.start();
	}

}