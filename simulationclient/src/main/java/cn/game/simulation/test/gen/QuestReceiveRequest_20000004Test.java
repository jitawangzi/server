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
public class QuestReceiveRequest_20000004Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.QuestMsg.QuestReceiveRequest_20000004.Builder builder = cn.game.protocol.protobuf.QuestMsg.QuestReceiveRequest_20000004.newBuilder() ; 
		List<QuestGroupInfo> questGroupsList = client.getPlayerAllInfo().getQuestGroupsList();
		if (!questGroupsList.isEmpty()) {
			QuestGroupInfo groupInfo = Rnd.randomOne(questGroupsList);
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
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new QuestReceiveRequest_20000004Test().getMessage(client));

		
	}

}