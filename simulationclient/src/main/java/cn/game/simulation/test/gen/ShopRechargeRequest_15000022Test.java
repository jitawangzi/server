package cn.game.simulation.test.gen;

import java.util.Collection;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.config.RechargeConfig;
import cn.game.protocol.generated.manager.RechargeManager;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class ShopRechargeRequest_15000022Test extends ServerTest {

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopRechargeRequest_15000022.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopRechargeRequest_15000022
				.newBuilder();
		builder.setId(1);

		return builder.build();
	}

	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopRechargeRequest_15000022.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopRechargeRequest_15000022
				.newBuilder();
		Collection<RechargeConfig> list = RechargeManager.instance().list(); 
		builder.setId(Rnd.randomElement(list).ID);

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		ShopRechargeRequest_15000022Test instance = new ShopRechargeRequest_15000022Test();
		instance.start();
	}

}