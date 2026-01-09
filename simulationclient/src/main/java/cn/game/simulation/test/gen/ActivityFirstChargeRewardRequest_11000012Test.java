package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityFirstChargeRewardRequest_11000012Test extends ServerTest<cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardRequest_11000012, cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardResponse_11000013> {

	@Override
	public cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardRequest_11000012 buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardRequest_11000012.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardRequest_11000012.newBuilder() ;

		builder.setId(31);
		builder.setChargeId(34);
		
		return builder.build() ; 
	}
	
	@Override
public cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardRequest_11000012 tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardRequest_11000012.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardRequest_11000012.newBuilder() ;

		builder.setId(10);
		builder.setChargeId(1);
		
		return builder.build() ; 
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
	public String verifyResponse(Client client, cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardRequest_11000012 requestMessage, cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardResponse_11000013 responseMessage) {
		
		return null; 
	}

	
	public static void main(String args[]) throws Exception {
	    ActivityFirstChargeRewardRequest_11000012Test instance = new ActivityFirstChargeRewardRequest_11000012Test();
	    instance.start();
	}

}