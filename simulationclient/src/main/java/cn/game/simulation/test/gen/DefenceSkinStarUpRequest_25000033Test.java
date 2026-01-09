package cn.game.simulation.test.gen;

import com.google.protobuf.Message;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.protocol.protobuf.BaseMsg.DefenceSkinInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class DefenceSkinStarUpRequest_25000033Test extends ServerTest<cn.game.protocol.protobuf.DevelopMsg.DefenceSkinStarUpRequest_25000033, cn.game.protocol.protobuf.DevelopMsg.DefenceSkinStarUpResponse_25000034> {

	@Override
	public cn.game.protocol.protobuf.DevelopMsg.DefenceSkinStarUpRequest_25000033 buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.DevelopMsg.DefenceSkinStarUpRequest_25000033.Builder builder = cn.game.protocol.protobuf.DevelopMsg.DefenceSkinStarUpRequest_25000033.newBuilder() ; 
		
		List<DefenceSkinInfo> defenceSkinsList = client.getPlayerAllInfo().getDefenceSkinsList(); 
		
		builder.setStarUpUid(defenceSkinsList.get(0).getUid());
		
		for (int i = 1; i < defenceSkinsList.size(); i++) {
			builder.addConsumedUids(defenceSkinsList.get(i).getUid());
		}
		
		return builder.build() ; 
	}
	
	@Override
	public cn.game.protocol.protobuf.DevelopMsg.DefenceSkinStarUpRequest_25000033 tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.DevelopMsg.DefenceSkinStarUpRequest_25000033.Builder builder = cn.game.protocol.protobuf.DevelopMsg.DefenceSkinStarUpRequest_25000033.newBuilder() ; 
		
		List<DefenceSkinInfo> defenceSkinsList = client.getPlayerAllInfo().getDefenceSkinsList(); 
		
		if (defenceSkinsList.isEmpty()) {
			return null; 
		}
		builder.setStarUpUid(defenceSkinsList.get(0).getUid());
		
		for (int i = 1; i < defenceSkinsList.size(); i++) {
			builder.addConsumedUids(defenceSkinsList.get(i).getUid());
		}
		
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
	public String verifyResponse(Client client, cn.game.protocol.protobuf.DevelopMsg.DefenceSkinStarUpRequest_25000033 requestMessage, cn.game.protocol.protobuf.DevelopMsg.DefenceSkinStarUpResponse_25000034 responseMessage) {
		
		return null; 
	}

	
	public static void main(String args[]) throws Exception {
	    DefenceSkinStarUpRequest_25000033Test instance = new DefenceSkinStarUpRequest_25000033Test();
	    instance.start();
	}

}