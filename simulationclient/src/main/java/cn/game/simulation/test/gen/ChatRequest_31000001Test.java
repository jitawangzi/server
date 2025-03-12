package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.ChatMsg.ChatType;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ChatRequest_31000001Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ChatMsg.ChatRequest_31000001.Builder builder = cn.game.protocol.protobuf.ChatMsg.ChatRequest_31000001.newBuilder() ; 
		
		builder.setChatType(ChatType.WORLD_CHAT);
		builder.setContent("总书记恩情永不忘");
//		builder.setTargetPlayerId(251220055 + "");
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ChatMsg.ChatRequest_31000001.Builder builder = cn.game.protocol.protobuf.ChatMsg.ChatRequest_31000001.newBuilder() ; 
		
		builder.setChatType(ChatType.WORLD_CHAT);
		builder.setContent("你好啊");
//		builder.setTargetPlayerId(251220055 + "");
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ChatRequest_31000001Test instance = new ChatRequest_31000001Test();
	    instance.start();
	}

}