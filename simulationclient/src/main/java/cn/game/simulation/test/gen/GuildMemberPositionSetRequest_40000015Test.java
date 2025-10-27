package cn.game.simulation.test.gen;

import com.google.protobuf.Message;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.games.net.cross.guild.GuildConstants;
import cn.game.protocol.generated.config.GuildPermissionsConfig;
import cn.game.protocol.generated.manager.GuildPermissionsManager;
import cn.game.protocol.protobuf.GuildMsg.GuildMemberInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildPersonalInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class GuildMemberPositionSetRequest_40000015Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildMemberPositionSetRequest_40000015.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildMemberPositionSetRequest_40000015.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildMemberPositionSetRequest_40000015.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildMemberPositionSetRequest_40000015.newBuilder() ; 
		
		GuildMemberInfo member = client.guildMember; 
		if (member == null) {
			return null ; 
		}
		// 检查权限
		GuildPermissionsConfig guildPermissionsConfig = GuildPermissionsManager.instance().get(member.getPosition()); 
		if (!guildPermissionsConfig.Posts) {
			return null; 
		}
		List<GuildMemberInfo> membersList = client.guildAllInfo.getShowInfo().getMembersList(); 
		if (membersList.isEmpty()) {
			return null;
		}
		GuildMemberInfo randomElement = Rnd.randomElement(membersList); 
		if (randomElement.getSimplePlayer().getId().equals(client.getPlayerId()+"")) {
			return null;
		}
		builder.setTargetPid(Integer.parseInt(randomElement.getSimplePlayer().getId())); 
		builder.setPosition(4); 
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildMemberPositionSetRequest_40000015Test instance = new GuildMemberPositionSetRequest_40000015Test();
	    instance.start();
	}

}