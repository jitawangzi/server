package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PlayerGuideRequest_01000060Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerGuideRequest_01000060.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerGuideRequest_01000060.newBuilder() ; 

		int guideType = client.guideType;
		int guideStep = client.guideStep++;
		if (client.guideStep >= 10) {
			client.guideType++;
			client.guideStep = 1;
		}
		builder.setType(guideType > 10 ? 10 : guideType);
		builder.setStep(guideStep > 10 ? 10 : guideStep);
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerGuideRequest_01000060.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerGuideRequest_01000060.newBuilder() ; 

		int guideType = client.guideType;
		int guideStep = client.guideStep++;
		if (client.guideStep >= 10) {
			client.guideType++;
			client.guideStep = 1;
		}
		builder.setType(guideType > 10 ? 10 : guideType);
		builder.setStep(guideStep > 10 ? 10 : guideStep);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    PlayerGuideRequest_01000060Test instance = new PlayerGuideRequest_01000060Test();
	    instance.start();
	}

}