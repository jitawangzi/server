package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityDayGiftRequest_11000100Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityDayGiftRequest_11000100.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityDayGiftRequest_11000100.newBuilder() ; 
		
		builder.setId(38);
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityDayGiftRequest_11000100.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityDayGiftRequest_11000100.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ActivityDayGiftRequest_11000100Test instance = new ActivityDayGiftRequest_11000100Test();
	    instance.start();
	}

}