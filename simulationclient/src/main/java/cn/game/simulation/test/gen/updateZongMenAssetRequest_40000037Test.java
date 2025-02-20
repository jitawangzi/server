package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class updateZongMenAssetRequest_40000037Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.updateZongMenAssetRequest_40000037.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.updateZongMenAssetRequest_40000037.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.updateZongMenAssetRequest_40000037.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.updateZongMenAssetRequest_40000037.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    updateZongMenAssetRequest_40000037Test instance = new updateZongMenAssetRequest_40000037Test();
	    instance.start();
	}

}