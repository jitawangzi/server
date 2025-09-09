package cn.game.simulation.test.gen;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleFieldStartRequest_13000001Test extends ServerTest {

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleFieldStartRequest_13000001.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleFieldStartRequest_13000001
				.newBuilder();
		builder.setType(1);

		builder.setTypeId(110011);
		builder.setFieldId(1); 

		return builder.build();
	}

	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleFieldStartRequest_13000001.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleFieldStartRequest_13000001
				.newBuilder();
		builder.setType(1);

		List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(1);
		if (battleTypeList != null) {
			for (BattleConfig battleConfig : battleTypeList) {
				if (battleConfig.preBattle == 0) {
					builder.setTypeId(battleConfig.ID);
					break;
				}
			}
		}

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
	    BattleFieldStartRequest_13000001Test instance = new BattleFieldStartRequest_13000001Test();
	    instance.start();
	}

}