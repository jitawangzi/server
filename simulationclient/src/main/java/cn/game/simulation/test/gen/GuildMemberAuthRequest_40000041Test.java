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
public class GuildMemberAuthRequest_40000041Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildMemberAuthRequest_40000041.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildMemberAuthRequest_40000041.newBuilder() ; 
		
		builder.setOptType(1);
		builder.addTargetPidList(240200326); 
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
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
	
	public static void main(String args[]) throws Exception {
	    GuildMemberAuthRequest_40000041Test instance = new GuildMemberAuthRequest_40000041Test();
	    instance.start();
	}

}