package cn.game.simulation.test.gen;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleDaoHeartSweepRequest_13000066Test extends ServerTest {

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepRequest_13000066.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepRequest_13000066
				.newBuilder();

		int type = 2;
		builder.setType(type);

		List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(type);
		if (battleTypeList != null) {
			builder.setId(battleTypeList.get(0).ID);
		} else {
			builder.setId(20001);
		}

		return builder.build();
	}

	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepRequest_13000066.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepRequest_13000066
				.newBuilder();

		int type = 2;
		builder.setType(type);

		List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(type);
		if (battleTypeList != null) {
			builder.setId(battleTypeList.get(0).ID);
		} else {
			builder.setId(20001);
		}

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		BattleDaoHeartSweepRequest_13000066Test instance = new BattleDaoHeartSweepRequest_13000066Test();
		instance.start();
	}

}