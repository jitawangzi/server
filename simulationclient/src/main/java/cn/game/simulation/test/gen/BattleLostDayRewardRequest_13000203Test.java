package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleLostDayRewardRequest_13000203Test extends ServerTest {

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleLostDayRewardRequest_13000203.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleLostDayRewardRequest_13000203
				.newBuilder();

		return builder.build();
	}

	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleLostDayRewardRequest_13000203.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleLostDayRewardRequest_13000203
				.newBuilder();

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		BattleLostDayRewardRequest_13000203Test instance = new BattleLostDayRewardRequest_13000203Test();
		instance.start();
	}

}