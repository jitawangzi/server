package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleDaoHeartRequest_13000055Test extends ServerTest {

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartRequest_13000055.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartRequest_13000055
				.newBuilder();

		builder.setType(3);

		return builder.build();
	}

	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartRequest_13000055.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartRequest_13000055
				.newBuilder();

		builder.setType(3);

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		BattleDaoHeartRequest_13000055Test instance = new BattleDaoHeartRequest_13000055Test();
		instance.start();
	}

}