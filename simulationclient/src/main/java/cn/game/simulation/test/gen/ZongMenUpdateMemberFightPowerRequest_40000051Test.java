package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ZongMenUpdateMemberFightPowerRequest_40000051Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenUpdateMemberFightPowerRequest_40000051.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenUpdateMemberFightPowerRequest_40000051.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenUpdateMemberFightPowerRequest_40000051.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenUpdateMemberFightPowerRequest_40000051.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ZongMenUpdateMemberFightPowerRequest_40000051Test instance = new ZongMenUpdateMemberFightPowerRequest_40000051Test();
	    instance.start();
	}

}