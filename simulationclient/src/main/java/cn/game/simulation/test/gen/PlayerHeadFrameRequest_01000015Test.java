package cn.game.simulation.test.gen;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.config.HeadBoxConfig;
import cn.game.protocol.generated.manager.HeadBoxManager;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class PlayerHeadFrameRequest_01000015Test extends ServerTest {

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerHeadFrameRequest_01000015.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerHeadFrameRequest_01000015
				.newBuilder();

		builder.setHeadFrame(1);

		return builder.build();
	}

	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerHeadFrameRequest_01000015.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerHeadFrameRequest_01000015
				.newBuilder();

		List<HeadBoxConfig> list = HeadBoxManager.instance().list();
		HeadBoxConfig randomOne = Rnd.randomOne(list);
		builder.setHeadFrame(randomOne.ID);
		return builder.build();
	}

	public static void main(String args[]) throws Exception {
	    PlayerHeadFrameRequest_01000015Test instance = new PlayerHeadFrameRequest_01000015Test();
	    instance.start();
	}

}