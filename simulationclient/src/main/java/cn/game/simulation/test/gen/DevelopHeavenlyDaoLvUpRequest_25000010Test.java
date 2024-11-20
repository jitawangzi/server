package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class DevelopHeavenlyDaoLvUpRequest_25000010Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.DevelopMsg.DevelopHeavenlyDaoLvUpRequest_25000010.Builder builder = cn.game.protocol.protobuf.DevelopMsg.DevelopHeavenlyDaoLvUpRequest_25000010.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.DevelopMsg.DevelopHeavenlyDaoLvUpRequest_25000010.Builder builder = cn.game.protocol.protobuf.DevelopMsg.DevelopHeavenlyDaoLvUpRequest_25000010.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    DevelopHeavenlyDaoLvUpRequest_25000010Test instance = new DevelopHeavenlyDaoLvUpRequest_25000010Test();
	    instance.start();
	}

}