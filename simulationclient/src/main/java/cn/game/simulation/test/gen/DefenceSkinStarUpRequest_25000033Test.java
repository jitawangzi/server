package cn.game.simulation.test.gen;

import com.google.protobuf.Message;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.protocol.protobuf.BaseMsg.DefenceSkinInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class DefenceSkinStarUpRequest_25000033Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.DevelopMsg.DefenceSkinStarUpRequest_25000033.Builder builder = cn.game.protocol.protobuf.DevelopMsg.DefenceSkinStarUpRequest_25000033.newBuilder() ; 
		
		List<DefenceSkinInfo> defenceSkinsList = client.getPlayerAllInfo().getDefenceSkinsList(); 
		
		builder.setStarUpUid(defenceSkinsList.get(0).getUid());
		
		for (int i = 1; i < defenceSkinsList.size(); i++) {
			builder.addConsumedUids(defenceSkinsList.get(i).getUid());
		}
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.DevelopMsg.DefenceSkinStarUpRequest_25000033.Builder builder = cn.game.protocol.protobuf.DevelopMsg.DefenceSkinStarUpRequest_25000033.newBuilder() ; 
		
		List<DefenceSkinInfo> defenceSkinsList = client.getPlayerAllInfo().getDefenceSkinsList(); 
		
		if (defenceSkinsList.isEmpty()) {
			return null; 
		}
		builder.setStarUpUid(defenceSkinsList.get(0).getUid());
		
		for (int i = 1; i < defenceSkinsList.size(); i++) {
			builder.addConsumedUids(defenceSkinsList.get(i).getUid());
		}
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    DefenceSkinStarUpRequest_25000033Test instance = new DefenceSkinStarUpRequest_25000033Test();
	    instance.start();
	}

}