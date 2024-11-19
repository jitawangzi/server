package cn.game.simulation.test.gen;

import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

import java.util.Collection;

@Component
public class ActivitySevenDaysSigninInfoRequest_11000024Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninInfoRequest_11000024.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninInfoRequest_11000024.newBuilder() ;
		Collection<ActivityConfig> list = ActivityManager.instance().list();
		if (list != null) {
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
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninInfoRequest_11000024.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninInfoRequest_11000024.newBuilder() ;
		Collection<ActivityConfig> list = ActivityManager.instance().list();
		if (list != null) {
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
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new ActivitySevenDaysSigninInfoRequest_11000024Test().getMessage(client));

		
	}

}