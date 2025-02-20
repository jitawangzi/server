package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ZongMenBountyTargetRefreshRequest_40000074Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyTargetRefreshRequest_40000074.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyTargetRefreshRequest_40000074.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyTargetRefreshRequest_40000074.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyTargetRefreshRequest_40000074.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ZongMenBountyTargetRefreshRequest_40000074Test instance = new ZongMenBountyTargetRefreshRequest_40000074Test();
	    instance.start();
	}

}