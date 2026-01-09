package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.protocol.protobuf.BattleMsg.LineupInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class BattleLineupRequest_13000048Test extends ServerTest {

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleLineupRequest_13000048.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleLineupRequest_13000048
				.newBuilder();

		LineupInfo.Builder lineupBuilder = LineupInfo.newBuilder();
		lineupBuilder.setSeq(0);
		lineupBuilder.addHeroUid(Rnd.randomElement(client.getPlayerAllInfo().getHerosList()).getUid());
		builder.setBattleType(1);
		builder.setLineup(lineupBuilder.build());

		return builder.build();
	}

	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleLineupRequest_13000048.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleLineupRequest_13000048
				.newBuilder();

		LineupInfo.Builder lineupBuilder = LineupInfo.newBuilder();
		lineupBuilder.setSeq(0);
		lineupBuilder.addHeroUid(Rnd.randomElement(client.getPlayerAllInfo().getHerosList()).getUid());
		builder.setBattleType(1);
		builder.setLineup(lineupBuilder.build());

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		BattleLineupRequest_13000048Test instance = new BattleLineupRequest_13000048Test();
		instance.start();
	}

}