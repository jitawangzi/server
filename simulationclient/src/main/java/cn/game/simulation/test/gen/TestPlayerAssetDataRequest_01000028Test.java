package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class TestPlayerAssetDataRequest_01000028Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestPlayerAssetDataResponse_6f000029.Builder builder = cn.game.protocol.protobuf.TestMsg.TestPlayerAssetDataResponse_6f000029
				.newBuilder();
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestPlayerAssetDataResponse_6f000029.Builder builder = cn.game.protocol.protobuf.TestMsg.TestPlayerAssetDataResponse_6f000029
				.newBuilder();
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    TestPlayerAssetDataRequest_01000028Test instance = new TestPlayerAssetDataRequest_01000028Test();
	    instance.start();
	}

}