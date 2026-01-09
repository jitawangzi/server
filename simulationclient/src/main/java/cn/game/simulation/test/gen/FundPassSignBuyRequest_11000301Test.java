package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class FundPassSignBuyRequest_11000301Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.FundPassSignBuyRequest_11000301.Builder builder = cn.game.protocol.protobuf.ActivityMsg.FundPassSignBuyRequest_11000301.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.FundPassSignBuyRequest_11000301.Builder builder = cn.game.protocol.protobuf.ActivityMsg.FundPassSignBuyRequest_11000301.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    FundPassSignBuyRequest_11000301Test instance = new FundPassSignBuyRequest_11000301Test();
	    instance.start();
	}

}