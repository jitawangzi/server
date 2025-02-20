package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ZongMenGetMyApplyZongMenIdListRequest_40000055Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenGetMyApplyZongMenIdListRequest_40000055.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenGetMyApplyZongMenIdListRequest_40000055.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenGetMyApplyZongMenIdListRequest_40000055.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenGetMyApplyZongMenIdListRequest_40000055.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ZongMenGetMyApplyZongMenIdListRequest_40000055Test instance = new ZongMenGetMyApplyZongMenIdListRequest_40000055Test();
	    instance.start();
	}

}