package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.enume.InitialUI;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PlayerRedPointRequest_01000075Test extends ServerTest<cn.game.protocol.protobuf.PlayerMsg.PlayerRedPointRequest_01000075, cn.game.protocol.protobuf.PlayerMsg.PlayerRedPointResponse_01000076> {

	@Override
	public cn.game.protocol.protobuf.PlayerMsg.PlayerRedPointRequest_01000075 buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerRedPointRequest_01000075.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerRedPointRequest_01000075
				.newBuilder();

		builder.addType(InitialUI.Shop.ID);

		return builder.build();
	}

	@Override
	public cn.game.protocol.protobuf.PlayerMsg.PlayerRedPointRequest_01000075 tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerRedPointRequest_01000075.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerRedPointRequest_01000075
				.newBuilder();

		builder.addType(InitialUI.Letter.ID);
		builder.addType(InitialUI.Shop.ID);
		builder.addType(InitialUI.RSGTree.ID);
		builder.addType(InitialUI.Guild.ID);

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
	public String verifyResponse(Client client, cn.game.protocol.protobuf.PlayerMsg.PlayerRedPointRequest_01000075 requestMessage, cn.game.protocol.protobuf.PlayerMsg.PlayerRedPointResponse_01000076 responseMessage) {
		
		return null; 
	}


	public static void main(String args[]) throws Exception {
		PlayerRedPointRequest_01000075Test instance = new PlayerRedPointRequest_01000075Test();
		instance.start();
	}

}