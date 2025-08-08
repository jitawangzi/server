package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class updateMemberAuthRequest_40000041Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.updateMemberAuthRequest_40000041.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.updateMemberAuthRequest_40000041.newBuilder() ; 
		
		builder.setOptType(1);
		builder.addTargetPidList(240200326); 
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.updateMemberAuthRequest_40000041.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.updateMemberAuthRequest_40000041.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    updateMemberAuthRequest_40000041Test instance = new updateMemberAuthRequest_40000041Test();
	    instance.start();
	}

}