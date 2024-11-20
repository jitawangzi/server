package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class QuestListRequest_20000001Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.QuestMsg.QuestListRequest_20000001.Builder builder = cn.game.protocol.protobuf.QuestMsg.QuestListRequest_20000001.newBuilder() ; 
		builder.setType(1);
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.QuestMsg.QuestListRequest_20000001.Builder builder = cn.game.protocol.protobuf.QuestMsg.QuestListRequest_20000001.newBuilder() ; 
		builder.setType(1);
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    QuestListRequest_20000001Test instance = new QuestListRequest_20000001Test();
	    instance.start();
	}

}