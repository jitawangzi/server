package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityWestLuckyInfoRequest_11000091Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyInfoRequest_11000091.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyInfoRequest_11000091.newBuilder() ; 
		
		builder.setActivityId(1001); 
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyInfoRequest_11000091.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyInfoRequest_11000091.newBuilder() ; 
		
		builder.setActivityId(activityId(ActivityTypeEnum.ActivityZhuanPan)) ; 
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ActivityWestLuckyInfoRequest_11000091Test instance = new ActivityWestLuckyInfoRequest_11000091Test();
	    instance.start();
	}

}