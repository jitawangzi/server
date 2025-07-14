package cn.game.simulation.test.gen;

import cn.game.games.net.game.module.develop.gem.Gem;
import cn.game.protocol.generated.config.GemConfig;
import cn.game.protocol.generated.manager.GemManager;
import cn.game.protocol.protobuf.BaseMsg;
import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GemComposeRequest_10000007Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GemMsg.GemComposeRequest_10000007.Builder builder = cn.game.protocol.protobuf.GemMsg.GemComposeRequest_10000007.newBuilder() ;
		List<BaseMsg.GemInfo> gemsList = client.getPlayerAllInfo().getGemsList();
		if (gemsList.isEmpty()) {
			return null; // No gems available to compose
		}
		builder.addAllUids(gemsList.stream()
				.map(BaseMsg.GemInfo::getUid)
				.toList());
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GemMsg.GemComposeRequest_10000007.Builder builder = cn.game.protocol.protobuf.GemMsg.GemComposeRequest_10000007.newBuilder() ;
		List<BaseMsg.GemInfo> gemsList = client.getPlayerAllInfo().getGemsList();
		if (gemsList.isEmpty()) {
			return null; // No gems available to compose
		}
		builder.addAllUids(gemsList.stream()
				.map(BaseMsg.GemInfo::getUid)
				.toList());
		return builder.build() ;
	}
	
	public static void main(String args[]) throws Exception {
	    GemComposeRequest_10000007Test instance = new GemComposeRequest_10000007Test();
	    instance.start();
	}

}