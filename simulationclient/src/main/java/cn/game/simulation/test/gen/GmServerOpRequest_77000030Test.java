package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GmServerOpRequest_77000030Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmServerOpRequest_77000030.Builder builder = cn.game.protocol.protobuf.GmMsg.GmServerOpRequest_77000030.newBuilder() ; 
		builder.setServerId("game_test");
		builder.setOpType(2);
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmServerOpRequest_77000030.Builder builder = cn.game.protocol.protobuf.GmMsg.GmServerOpRequest_77000030.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GmServerOpRequest_77000030Test instance = new GmServerOpRequest_77000030Test();
	    instance.start();
	}

}