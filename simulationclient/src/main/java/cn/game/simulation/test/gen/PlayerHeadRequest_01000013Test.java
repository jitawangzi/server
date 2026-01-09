package cn.game.simulation.test.gen;

import java.util.Collection;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.config.HeadPortraitConfig;
import cn.game.protocol.generated.manager.HeadPortraitManager;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PlayerHeadRequest_01000013Test extends ServerTest<cn.game.protocol.protobuf.PlayerMsg.PlayerHeadRequest_01000013, cn.game.protocol.protobuf.PlayerMsg.PlayerHeadResponse_01000014> {

	@Override
	public cn.game.protocol.protobuf.PlayerMsg.PlayerHeadRequest_01000013 buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerHeadRequest_01000013.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerHeadRequest_01000013
				.newBuilder();
		builder.setHead(1);
		return builder.build();
	}

	@Override
	public cn.game.protocol.protobuf.PlayerMsg.PlayerHeadRequest_01000013 tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerHeadRequest_01000013.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerHeadRequest_01000013
				.newBuilder();
		Collection<HeadPortraitConfig> list = HeadPortraitManager.instance().list();
		for (HeadPortraitConfig headBoxConfig : list) {
			builder.setHead(headBoxConfig.ID);
			break;
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
	public String verifyResponse(Client client, cn.game.protocol.protobuf.PlayerMsg.PlayerHeadRequest_01000013 requestMessage, cn.game.protocol.protobuf.PlayerMsg.PlayerHeadResponse_01000014 responseMessage) {
		
		return null; 
	}


	public static void main(String args[]) throws Exception {
	    PlayerHeadRequest_01000013Test instance = new PlayerHeadRequest_01000013Test();
	    instance.start();
	}

}