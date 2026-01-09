package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PlayerImageRequest_01000019Test extends ServerTest<cn.game.protocol.protobuf.PlayerMsg.PlayerImageRequest_01000019, cn.game.protocol.protobuf.PlayerMsg.PlayerImageResponse_0100001a> {

	@Override
	public cn.game.protocol.protobuf.PlayerMsg.PlayerImageRequest_01000019 buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerImageRequest_01000019.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerImageRequest_01000019
				.newBuilder();

		builder.setImage(1);

		return builder.build();
	}

	@Override
	public cn.game.protocol.protobuf.PlayerMsg.PlayerImageRequest_01000019 tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerImageRequest_01000019.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerImageRequest_01000019
				.newBuilder();

		builder.setImage(1);

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
	public String verifyResponse(Client client, cn.game.protocol.protobuf.PlayerMsg.PlayerImageRequest_01000019 requestMessage, cn.game.protocol.protobuf.PlayerMsg.PlayerImageResponse_0100001a responseMessage) {
		
		return null; 
	}


	public static void main(String args[]) throws Exception {
		PlayerImageRequest_01000019Test instance = new PlayerImageRequest_01000019Test();
		instance.start();
	}

}