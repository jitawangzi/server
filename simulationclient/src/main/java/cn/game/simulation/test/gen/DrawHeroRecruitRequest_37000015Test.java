package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class DrawHeroRecruitRequest_37000015Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.DrawMsg.DrawHeroRecruitRequest_37000015.Builder builder = cn.game.protocol.protobuf.DrawMsg.DrawHeroRecruitRequest_37000015.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.DrawMsg.DrawHeroRecruitRequest_37000015.Builder builder = cn.game.protocol.protobuf.DrawMsg.DrawHeroRecruitRequest_37000015.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    DrawHeroRecruitRequest_37000015Test instance = new DrawHeroRecruitRequest_37000015Test();
	    instance.start();
	}

}