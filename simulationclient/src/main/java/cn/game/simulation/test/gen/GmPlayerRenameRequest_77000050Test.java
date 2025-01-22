package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GmPlayerRenameRequest_77000050Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmPlayerRenameRequest_77000050.Builder builder = cn.game.protocol.protobuf.GmMsg.GmPlayerRenameRequest_77000050.newBuilder() ; 
		
		builder.setPlayerId("240200006");
		builder.setName("LLLLL");
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmPlayerRenameRequest_77000050.Builder builder = cn.game.protocol.protobuf.GmMsg.GmPlayerRenameRequest_77000050.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GmPlayerRenameRequest_77000050Test instance = new GmPlayerRenameRequest_77000050Test();
	    instance.start();
	}

}