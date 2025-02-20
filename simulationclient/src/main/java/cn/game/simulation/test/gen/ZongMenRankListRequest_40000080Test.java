package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ZongMenRankListRequest_40000080Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenRankListRequest_40000080.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenRankListRequest_40000080.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenRankListRequest_40000080.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenRankListRequest_40000080.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ZongMenRankListRequest_40000080Test instance = new ZongMenRankListRequest_40000080Test();
	    instance.start();
	}

}