package cn.game.simulation.test.gen;

import java.util.Collection;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivitySevenDaysSigninRequest_11000026Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninRequest_11000026.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninRequest_11000026.newBuilder() ;

		Collection<ActivityConfig> list = ActivityManager.instance().list();
		if (!list.isEmpty()) {
			for (ActivityConfig activityConfig : list) {
				if (activityConfig.type == 3) {
					builder.setId(activityConfig.ID);
					break;
				}
			}
		} else {

			builder.setId(26);
		}
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninRequest_11000026.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninRequest_11000026.newBuilder() ;

		Collection<ActivityConfig> list = ActivityManager.instance().list();
		if (!list.isEmpty()) {
			for (ActivityConfig activityConfig : list) {
				if (activityConfig.type == 3) {
					builder.setId(activityConfig.ID);
					break;
				}
			}
		} else {

			builder.setId(12);
		}
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ActivitySevenDaysSigninRequest_11000026Test instance = new ActivitySevenDaysSigninRequest_11000026Test();
	    instance.start();
	}

}