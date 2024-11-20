package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class MailSeeRequest_12000003Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.MailMsg.MailSeeRequest_12000003.Builder builder = cn.game.protocol.protobuf.MailMsg.MailSeeRequest_12000003.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.MailMsg.MailSeeRequest_12000003.Builder builder = cn.game.protocol.protobuf.MailMsg.MailSeeRequest_12000003.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    MailSeeRequest_12000003Test instance = new MailSeeRequest_12000003Test();
	    instance.start();
	}

}