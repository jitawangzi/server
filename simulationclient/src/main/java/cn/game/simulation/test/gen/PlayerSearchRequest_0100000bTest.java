package cn.game.simulation.test.gen;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class PlayerSearchRequest_0100000bTest extends ServerTest<cn.game.protocol.protobuf.PlayerMsg.PlayerSearchRequest_0100000b, cn.game.protocol.protobuf.PlayerMsg.PlayerSearchResponse_0100000c> {

	@Override
	public cn.game.protocol.protobuf.PlayerMsg.PlayerSearchRequest_0100000b buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerSearchRequest_0100000b.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerSearchRequest_0100000b
				.newBuilder();

		List<SimplePlayerInfo> recommendList = client.recommendList;
		if (recommendList != null && recommendList.size() > 0) {
			SimplePlayerInfo playerInfo = Rnd.randomElement(recommendList);
			if (Rnd.nextBoolean()) {
				builder.setPlayerId(playerInfo.getId());
			} else {
				builder.setPlayerName(playerInfo.getName());
			}
			builder.setPlayerId(playerInfo.getId());
		} else {
			builder.setPlayerName("240201770");
		}
		return builder.build();
	}

	@Override
	public cn.game.protocol.protobuf.PlayerMsg.PlayerSearchRequest_0100000b tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerSearchRequest_0100000b.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerSearchRequest_0100000b
				.newBuilder();

		List<SimplePlayerInfo> recommendList = client.recommendList;
		if (recommendList != null && recommendList.size() > 0) {
			SimplePlayerInfo playerInfo = Rnd.randomElement(recommendList);
			if (Rnd.nextBoolean()) {
				builder.setPlayerId(playerInfo.getId());
			} else {
				builder.setPlayerName(playerInfo.getName());
			}
			builder.setPlayerId(playerInfo.getId());
		} else {
			builder.setPlayerName("浩瀚青龙剑侠");
		}
		return builder.build();
	}
	/**
	 * 校验响应数据的正确性,这里默认服务器已经成功返回消息，并且消息没有错误
	 * <p>
	 * 作用：检查服务器返回的 response 中的数据值是否符合预期。
	 * 检查逻辑：
	 *      1. 如果某字段必须有值，那么是否有值？
	 *      2. 如果字段有值，那么这个值是否在合理范围内？
	 *
	 * @param client   当前客户端对象（包含最新状态）
	 * @param request  刚才发送的请求消息（用于上下文对比）,必定不是null
	 * @param response 服务器返回的响应消息,必定不是null
	 * @return 错误描述，如果为null或者空字符串表示没有错误
	 */
	public String verifyResponse(Client client, cn.game.protocol.protobuf.PlayerMsg.PlayerSearchRequest_0100000b requestMessage, cn.game.protocol.protobuf.PlayerMsg.PlayerSearchResponse_0100000c responseMessage) {
		
		return null; 
	}


	public static void main(String args[]) throws Exception {
		PlayerSearchRequest_0100000bTest instance = new PlayerSearchRequest_0100000bTest();
		instance.start();
	}

}