package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GinsengTreeHangUpRequest_39000015Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHangUpRequest_39000015.Builder builder = cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHangUpRequest_39000015.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHangUpRequest_39000015.Builder builder = cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHangUpRequest_39000015.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GinsengTreeHangUpRequest_39000015Test instance = new GinsengTreeHangUpRequest_39000015Test();
	    instance.start();
	}

}