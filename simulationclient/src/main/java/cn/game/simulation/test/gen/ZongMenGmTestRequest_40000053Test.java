package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ZongMenGmTestRequest_40000053Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenGmTestRequest_40000053.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenGmTestRequest_40000053.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenGmTestRequest_40000053.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenGmTestRequest_40000053.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ZongMenGmTestRequest_40000053Test instance = new ZongMenGmTestRequest_40000053Test();
	    instance.start();
	}

}