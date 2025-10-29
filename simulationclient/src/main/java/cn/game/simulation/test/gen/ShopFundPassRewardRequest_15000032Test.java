package cn.game.simulation.test.gen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.config.FundPassRewardsConfig;
import cn.game.protocol.generated.manager.FundPassRewardsManager;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class ShopFundPassRewardRequest_15000032Test extends ServerTest {

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopFundPassRewardRequest_15000032.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopFundPassRewardRequest_15000032
				.newBuilder();
		builder.addId(1);

		return builder.build();
	}

	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopFundPassRewardRequest_15000032.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopFundPassRewardRequest_15000032
				.newBuilder();
		List<Integer> buyList = (List<Integer>) client.dataMap.computeIfAbsent("fundPassId", k -> new ArrayList<>());
		if (buyList.isEmpty()) {
			return null;  
		}
		Collection<FundPassRewardsConfig> list = FundPassRewardsManager.instance().list(); 
//		for (FundPassRewardsConfig fundPassRewardsConfig : list) {
//			if (buyList.contains(fundPassRewardsConfig.Index)) {
//				builder.addId(fundPassRewardsConfig.ID);
//				break;
//			}
//		}
		FundPassRewardsConfig randomElement = Rnd.randomElement(list); 
		builder.addId(randomElement.ID); 
		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		ShopFundPassRewardRequest_15000032Test instance = new ShopFundPassRewardRequest_15000032Test();
		instance.start();
	}

}