package cn.game.simulation.test.gen;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.config.GemConfig;
import cn.game.protocol.generated.manager.GemManager;
import cn.game.protocol.protobuf.BaseMsg.EquipPartInfo;
import cn.game.protocol.protobuf.BaseMsg.GemInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class GemWearRequest_10000001Test extends ServerTest<cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001, cn.game.protocol.protobuf.GemMsg.GemWearResponse_10000002> {

	@Override
	public cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001 buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001.Builder builder = cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001.newBuilder() ; 
		List<GemInfo> gemsList = client.getPlayerAllInfo().getGemsList();

		builder.setUid(Rnd.randomElement(gemsList).getUid());
		builder.setPos(Rnd.get(0, 4));
		
		return builder.build() ; 
	}
	
	@Override
	public cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001 tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001.Builder builder = cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001.newBuilder() ; 
		
		List<GemInfo> gemsList = client.getPlayerAllInfo().getGemsList();
		List<EquipPartInfo> equipPartsList = client.getPlayerAllInfo().getEquipPartsList();
		loop: for (GemInfo gemInfo : gemsList) {
			for (EquipPartInfo equipPartInfo : equipPartsList) {
				GemConfig gemConfig = GemManager.instance().get(gemInfo.getConfigId());
				if (gemConfig.pos == equipPartInfo.getType()) {
					if (!equipPartInfo.getGemMapMap().containsKey(gemInfo.getUid())) {
						builder.setUid(gemInfo.getUid());
						break loop;
					}
				}
			}
        }
		if (StringUtils.isEmpty(builder.getUid())) {
			return null ; 
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
	public String verifyResponse(Client client, cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001 requestMessage, cn.game.protocol.protobuf.GemMsg.GemWearResponse_10000002 responseMessage) {
		
		return null; 
	}

	
	public static void main(String args[]) throws Exception {
	    GemWearRequest_10000001Test instance = new GemWearRequest_10000001Test();
	    instance.start();
	}

}