package cn.game.simulation.test.gen;

import java.util.Collection;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.config.HeadBoxConfig;
import cn.game.protocol.generated.manager.HeadBoxManager;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class PlayerHeadFrameRequest_01000015Test extends ServerTest {

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerHeadFrameRequest_01000015.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerHeadFrameRequest_01000015
				.newBuilder();

		builder.setHeadFrame(1);

		return builder.build();
	}

	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerHeadFrameRequest_01000015.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerHeadFrameRequest_01000015
				.newBuilder();

		Map<Integer, Integer> headBoxMapMap = client.getPlayerAllInfo().getHeadBoxMapMap(); 
		if (headBoxMapMap.isEmpty()) {
			return null; 
		}
		Integer randomElement = Rnd.randomElement(headBoxMapMap.keySet()); 
		builder.setHeadFrame(randomElement);
		
		return builder.build();
	}

	public static void main(String args[]) throws Exception {
	    PlayerHeadFrameRequest_01000015Test instance = new PlayerHeadFrameRequest_01000015Test();
	    instance.start();
	}

}