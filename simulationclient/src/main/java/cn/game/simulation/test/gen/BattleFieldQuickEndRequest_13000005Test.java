package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleFieldQuickEndRequest_13000005Test extends ServerTest {

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleFieldQuickEndRequest_13000005.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleFieldQuickEndRequest_13000005
				.newBuilder();
		builder.setType(22);
		builder.setTypeId(220002);
		builder.setSubId(1);

		return builder.build();
	}

	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleFieldQuickEndRequest_13000005.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleFieldQuickEndRequest_13000005
				.newBuilder();
		builder.setType(20);
		builder.setTypeId(40010);
		builder.setSubId(1);

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		BattleFieldQuickEndRequest_13000005Test instance = new BattleFieldQuickEndRequest_13000005Test();
		instance.start();
	}

}