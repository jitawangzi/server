package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class FundPassSignAllRequest_11000303Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.FundPassSignAllRequest_11000303.Builder builder = cn.game.protocol.protobuf.ActivityMsg.FundPassSignAllRequest_11000303.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.FundPassSignAllRequest_11000303.Builder builder = cn.game.protocol.protobuf.ActivityMsg.FundPassSignAllRequest_11000303.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    FundPassSignAllRequest_11000303Test instance = new FundPassSignAllRequest_11000303Test();
	    instance.start();
	}

}