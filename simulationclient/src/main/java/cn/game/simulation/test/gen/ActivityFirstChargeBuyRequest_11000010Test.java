package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityFirstChargeBuyRequest_11000010Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeBuyRequest_11000010.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeBuyRequest_11000010.newBuilder() ;

		builder.setId(31);
		builder.setChargeId(35);
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeBuyRequest_11000010.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeBuyRequest_11000010.newBuilder() ;

		builder.setId(10);
		builder.setChargeId(1);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ActivityFirstChargeBuyRequest_11000010Test instance = new ActivityFirstChargeBuyRequest_11000010Test();
	    instance.start();
	}

}