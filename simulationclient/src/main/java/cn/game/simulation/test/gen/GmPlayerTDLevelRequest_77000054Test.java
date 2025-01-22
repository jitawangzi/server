package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GmPlayerTDLevelRequest_77000054Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmPlayerTDLevelRequest_77000054.Builder builder = cn.game.protocol.protobuf.GmMsg.GmPlayerTDLevelRequest_77000054.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmPlayerTDLevelRequest_77000054.Builder builder = cn.game.protocol.protobuf.GmMsg.GmPlayerTDLevelRequest_77000054.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GmPlayerTDLevelRequest_77000054Test instance = new GmPlayerTDLevelRequest_77000054Test();
	    instance.start();
	}

}