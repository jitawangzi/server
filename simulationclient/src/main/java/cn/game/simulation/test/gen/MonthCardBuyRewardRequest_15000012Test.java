package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class MonthCardBuyRewardRequest_15000012Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRewardRequest_15000012.Builder builder = cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRewardRequest_15000012.newBuilder() ; 
		builder.setId(1);
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRewardRequest_15000012.Builder builder = cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRewardRequest_15000012.newBuilder() ; 
		builder.setId(1);
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    MonthCardBuyRewardRequest_15000012Test instance = new MonthCardBuyRewardRequest_15000012Test();
	    instance.start();
	}

}