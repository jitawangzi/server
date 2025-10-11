package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class FundPassSignReceiveRequest_11000305Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.FundPassSignReceiveRequest_11000305.Builder builder = cn.game.protocol.protobuf.ActivityMsg.FundPassSignReceiveRequest_11000305.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.FundPassSignReceiveRequest_11000305.Builder builder = cn.game.protocol.protobuf.ActivityMsg.FundPassSignReceiveRequest_11000305.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    FundPassSignReceiveRequest_11000305Test instance = new FundPassSignReceiveRequest_11000305Test();
	    instance.start();
	}

}