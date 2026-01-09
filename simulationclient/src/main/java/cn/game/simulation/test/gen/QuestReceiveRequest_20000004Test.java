package cn.game.simulation.test.gen;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.QuestMsg.QuestGroupInfo;
import cn.game.protocol.protobuf.QuestMsg.QuestInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class QuestReceiveRequest_20000004Test extends ServerTest<cn.game.protocol.protobuf.QuestMsg.QuestReceiveRequest_20000004, cn.game.protocol.protobuf.QuestMsg.QuestReceiveResponse_20000005> {

	@Override
	public cn.game.protocol.protobuf.QuestMsg.QuestReceiveRequest_20000004 buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.QuestMsg.QuestReceiveRequest_20000004.Builder builder = cn.game.protocol.protobuf.QuestMsg.QuestReceiveRequest_20000004
				.newBuilder();
		List<QuestGroupInfo> questGroupsList = client.getPlayerAllInfo().getQuestGroupsList();
		if (!questGroupsList.isEmpty()) {
			QuestGroupInfo groupInfo = Rnd.randomElement(questGroupsList);
			List<QuestInfo> questsList = groupInfo.getQuestsList();
			for (QuestInfo questInfo : questsList) {
				if (questInfo.getState() == 4) {
					builder.addIds(questInfo.getId());
					break;
				}
			}
		} else {
			builder.addIds(10301);
		}
		return builder.build();
	}

	@Override
	public cn.game.protocol.protobuf.QuestMsg.QuestReceiveRequest_20000004 tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.QuestMsg.QuestReceiveRequest_20000004.Builder builder = cn.game.protocol.protobuf.QuestMsg.QuestReceiveRequest_20000004
				.newBuilder();
		List<QuestGroupInfo> questGroupsList = client.getPlayerAllInfo().getQuestGroupsList();
		if (!questGroupsList.isEmpty()) {
			QuestGroupInfo groupInfo = Rnd.randomElement(questGroupsList);
			List<QuestInfo> questsList = groupInfo.getQuestsList();
			for (QuestInfo questInfo : questsList) {
				if (questInfo.getState() == 4) {
					builder.addIds(questInfo.getId());
					break;
				}
			}
		} else {
			builder.addIds(10301);
		}
		if (builder.getIdsCount() == 0) {
			return null;
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
	public String verifyResponse(Client client, cn.game.protocol.protobuf.QuestMsg.QuestReceiveRequest_20000004 requestMessage, cn.game.protocol.protobuf.QuestMsg.QuestReceiveResponse_20000005 responseMessage) {
		
		return null; 
	}


	public static void main(String args[]) throws Exception {
		QuestReceiveRequest_20000004Test instance = new QuestReceiveRequest_20000004Test();
		instance.start();
	}

}