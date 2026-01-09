package cn.game.simulation.test.gen;

import java.util.Collection;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.config.HeadPortraitConfig;
import cn.game.protocol.generated.manager.HeadPortraitManager;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PlayerHeadRequest_01000013Test extends ServerTest {

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerHeadRequest_01000013.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerHeadRequest_01000013
				.newBuilder();
		builder.setHead(1);
		return builder.build();
	}

	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerHeadRequest_01000013.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerHeadRequest_01000013
				.newBuilder();
		Collection<HeadPortraitConfig> list = HeadPortraitManager.instance().list();
		for (HeadPortraitConfig headBoxConfig : list) {
			builder.setHead(headBoxConfig.ID);
			break;
		}

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
	    PlayerHeadRequest_01000013Test instance = new PlayerHeadRequest_01000013Test();
	    instance.start();
	}

}