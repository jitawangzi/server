package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ZongMenQuickJoinRequest_40000065Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenQuickJoinRequest_40000065.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenQuickJoinRequest_40000065.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenQuickJoinRequest_40000065.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenQuickJoinRequest_40000065.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ZongMenQuickJoinRequest_40000065Test instance = new ZongMenQuickJoinRequest_40000065Test();
	    instance.start();
	}

}