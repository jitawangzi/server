package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class FundPassSignAllRequest_15000073Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ShopMsg.FundPassSignAllRequest_15000073.Builder builder = cn.game.protocol.protobuf.ShopMsg.FundPassSignAllRequest_15000073.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ShopMsg.FundPassSignAllRequest_15000073.Builder builder = cn.game.protocol.protobuf.ShopMsg.FundPassSignAllRequest_15000073.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    FundPassSignAllRequest_15000073Test instance = new FundPassSignAllRequest_15000073Test();
	    instance.start();
	}

}