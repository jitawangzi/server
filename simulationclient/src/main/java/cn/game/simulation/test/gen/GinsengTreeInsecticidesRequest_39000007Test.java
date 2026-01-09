package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GinsengTreeInsecticidesRequest_39000007Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeInsecticidesRequest_39000007.Builder builder = cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeInsecticidesRequest_39000007.newBuilder() ; 
		
		builder.setCount(2);
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeInsecticidesRequest_39000007.Builder builder = cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeInsecticidesRequest_39000007.newBuilder() ; 
		
		builder.setCount(2);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GinsengTreeInsecticidesRequest_39000007Test instance = new GinsengTreeInsecticidesRequest_39000007Test();
	    instance.start();
	}

}