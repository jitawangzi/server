package cn.game.simulation.test.gen;

import com.google.protobuf.Message;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.protocol.protobuf.BaseMsg.DefenceSkinInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class DefenceSkinChangeRequest_25000035Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.DevelopMsg.DefenceSkinChangeRequest_25000035.Builder builder = cn.game.protocol.protobuf.DevelopMsg.DefenceSkinChangeRequest_25000035.newBuilder() ; 
		List<DefenceSkinInfo> defenceSkinsList = client.getPlayerAllInfo().getDefenceSkinsList(); 
		DefenceSkinInfo randomElement = Rnd.randomElement(defenceSkinsList); 
		if (randomElement == null) {
			return null ; 
		}
		builder.setUid(randomElement.getUid());
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.DevelopMsg.DefenceSkinChangeRequest_25000035.Builder builder = cn.game.protocol.protobuf.DevelopMsg.DefenceSkinChangeRequest_25000035.newBuilder() ; 
		List<DefenceSkinInfo> defenceSkinsList = client.getPlayerAllInfo().getDefenceSkinsList(); 
		DefenceSkinInfo randomElement = Rnd.randomElement(defenceSkinsList); 
		if (randomElement == null) {
			return null ; 
		}
		builder.setUid(randomElement.getUid());
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    DefenceSkinChangeRequest_25000035Test instance = new DefenceSkinChangeRequest_25000035Test();
	    instance.start();
	}

}