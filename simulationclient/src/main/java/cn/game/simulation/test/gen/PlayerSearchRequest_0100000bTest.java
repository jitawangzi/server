package cn.game.simulation.test.gen;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class PlayerSearchRequest_0100000bTest extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerSearchRequest_0100000b.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerSearchRequest_0100000b.newBuilder() ; 
		
		List<SimplePlayerInfo> recommendList = client.recommendList;
		if (recommendList != null && recommendList.size() > 0) {
			SimplePlayerInfo playerInfo = Rnd.randomOne(recommendList);
			if (Rnd.nextBoolean()) {
				builder.setPlayerId(playerInfo.getId());
			} else {
				builder.setPlayerName(playerInfo.getName());
			}
			builder.setPlayerId(playerInfo.getId());
		} else {
			builder.setPlayerName("浩瀚青龙剑侠");
		}
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerSearchRequest_0100000b.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerSearchRequest_0100000b.newBuilder() ; 
		
		List<SimplePlayerInfo> recommendList = client.recommendList;
		if (recommendList != null && recommendList.size() > 0) {
			SimplePlayerInfo playerInfo = Rnd.randomOne(recommendList);
			if (Rnd.nextBoolean()) {
				builder.setPlayerId(playerInfo.getId());
			} else {
				builder.setPlayerName(playerInfo.getName());
			}
			builder.setPlayerId(playerInfo.getId());
		} else {
			builder.setPlayerName("浩瀚青龙剑侠");
		}
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    PlayerSearchRequest_0100000bTest instance = new PlayerSearchRequest_0100000bTest();
	    instance.start();
	}

}