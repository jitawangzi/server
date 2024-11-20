package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class QuestListAllRequest_20000051Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.QuestMsg.QuestListAllRequest_20000051.Builder builder = cn.game.protocol.protobuf.QuestMsg.QuestListAllRequest_20000051.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.QuestMsg.QuestListAllRequest_20000051.Builder builder = cn.game.protocol.protobuf.QuestMsg.QuestListAllRequest_20000051.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    QuestListAllRequest_20000051Test instance = new QuestListAllRequest_20000051Test();
	    instance.start();
	}

}