package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import com.ubird.astar.ui.UFrame;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildMemberInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class GuildMemberAuthRequest_40000041Test extends ServerTest<cn.game.protocol.protobuf.GuildMsg.GuildMemberAuthRequest_40000041, cn.game.protocol.protobuf.GuildMsg.GuildMemberAuthResponse_40000042> {

	@Override
	public cn.game.protocol.protobuf.GuildMsg.GuildMemberAuthRequest_40000041 buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildMemberAuthRequest_40000041.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildMemberAuthRequest_40000041.newBuilder() ; 
		
		builder.setOptType(1);
		builder.addTargetPidList(240200326); 
		
		
		return builder.build() ; 
	}
	
	@Override
	public cn.game.protocol.protobuf.GuildMsg.GuildMemberAuthRequest_40000041 tryBuildSimulationRequest(Client client) {
		if (client.guildAllInfo == null) {
			return null; 
		}
		cn.game.protocol.protobuf.GuildMsg.GuildMemberAuthRequest_40000041.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildMemberAuthRequest_40000041.newBuilder() ; 
		if (Rnd.nextBoolean()) {// 处理申请
			List<SimplePlayerInfo> applyPlayerListList = client.guildAllInfo.getSharedInfo().getApplyPlayerListList(); 
			if (applyPlayerListList.isEmpty()) {
				return null; 
			}
			SimplePlayerInfo randomElement = Rnd.randomElement(applyPlayerListList); 
			builder.addTargetPidList(Integer.parseInt(randomElement.getId())) ; 
			if (Rnd.nextBoolean()) {
				builder.setOptType(1);
			}else {
				builder.setOptType(2);
			}
			
		} else { // 踢人
			List<GuildMemberInfo> memberListList = client.guildAllInfo.getShowInfo().getMembersList();
			if (memberListList.isEmpty()) {
				return null;
			}
			GuildMemberInfo randomElement = Rnd.randomElement(memberListList);
			// 不能踢自己
			if (randomElement.getSimplePlayer().getId().equals(String.valueOf(client.getPlayerAllInfo().getPlayer().getId()))) {
				return null;
			}
			builder.addTargetPidList(Integer.parseInt(randomElement.getSimplePlayer().getId()));
			builder.setOptType(3);
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
	public String verifyResponse(Client client, cn.game.protocol.protobuf.GuildMsg.GuildMemberAuthRequest_40000041 requestMessage, cn.game.protocol.protobuf.GuildMsg.GuildMemberAuthResponse_40000042 responseMessage) {
		
		return null; 
	}

	
	public static void main(String args[]) throws Exception {
	    GuildMemberAuthRequest_40000041Test instance = new GuildMemberAuthRequest_40000041Test();
	    instance.start();
	}

}