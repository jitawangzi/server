package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class setZongMenSettingRequest_40000013Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.setZongMenSettingRequest_40000013.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.setZongMenSettingRequest_40000013.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.setZongMenSettingRequest_40000013.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.setZongMenSettingRequest_40000013.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    setZongMenSettingRequest_40000013Test instance = new setZongMenSettingRequest_40000013Test();
	    instance.start();
	}

}