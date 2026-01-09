package cn.game.simulation.test.gen;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.BaseMsg;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class GemLockRequest_10000005Test extends ServerTest<cn.game.protocol.protobuf.GemMsg.GemLockRequest_10000005, cn.game.protocol.protobuf.GemMsg.GemLockResponse_10000006> {

	@Override
	public cn.game.protocol.protobuf.GemMsg.GemLockRequest_10000005 buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GemMsg.GemLockRequest_10000005.Builder builder = cn.game.protocol.protobuf.GemMsg.GemLockRequest_10000005.newBuilder() ;
		List<BaseMsg.GemInfo> list = client.getPlayerAllInfo().getGemsList();
		BaseMsg.GemInfo gemInfo = Rnd.randomElement(list);
		if (gemInfo == null		){
			return null ;
		}
		builder.addUid(gemInfo.getUid());
		builder.setLock(!gemInfo.getIsLock()) ;
		return builder.build() ; 
	}
	
	@Override
	public cn.game.protocol.protobuf.GemMsg.GemLockRequest_10000005 tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GemMsg.GemLockRequest_10000005.Builder builder = cn.game.protocol.protobuf.GemMsg.GemLockRequest_10000005.newBuilder() ;
		List<BaseMsg.GemInfo> list = client.getPlayerAllInfo().getGemsList();
		BaseMsg.GemInfo gemInfo = Rnd.randomElement(list);
		if (gemInfo == null) {
			return null ;
		}
		builder.addUid(gemInfo.getUid());
		builder.setLock(!gemInfo.getIsLock()) ;
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
	public String verifyResponse(Client client, cn.game.protocol.protobuf.GemMsg.GemLockRequest_10000005 requestMessage, cn.game.protocol.protobuf.GemMsg.GemLockResponse_10000006 responseMessage) {
		
		return null; 
	}

	
	public static void main(String args[]) throws Exception {
	    GemLockRequest_10000005Test instance = new GemLockRequest_10000005Test();
	    instance.start();
	}

}