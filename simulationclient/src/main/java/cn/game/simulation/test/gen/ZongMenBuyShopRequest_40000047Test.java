package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ZongMenBuyShopRequest_40000047Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenBuyShopRequest_40000047.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenBuyShopRequest_40000047.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenBuyShopRequest_40000047.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenBuyShopRequest_40000047.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ZongMenBuyShopRequest_40000047Test instance = new ZongMenBuyShopRequest_40000047Test();
	    instance.start();
	}

}