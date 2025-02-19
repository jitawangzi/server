package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ZongMenBountyBattleReportRequest_4000007aTest extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyBattleReportRequest_4000007a.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyBattleReportRequest_4000007a.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyBattleReportRequest_4000007a.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyBattleReportRequest_4000007a.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ZongMenBountyBattleReportRequest_4000007aTest instance = new ZongMenBountyBattleReportRequest_4000007aTest();
	    instance.start();
	}

}