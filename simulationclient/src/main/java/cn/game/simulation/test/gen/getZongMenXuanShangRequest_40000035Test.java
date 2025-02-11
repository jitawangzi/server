package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class getZongMenXuanShangRequest_40000035Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.getZongMenXuanShangRequest_40000035.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.getZongMenXuanShangRequest_40000035.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.getZongMenXuanShangRequest_40000035.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.getZongMenXuanShangRequest_40000035.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    getZongMenXuanShangRequest_40000035Test instance = new getZongMenXuanShangRequest_40000035Test();
	    instance.start();
	}

}