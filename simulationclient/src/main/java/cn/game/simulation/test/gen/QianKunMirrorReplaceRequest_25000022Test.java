package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class QianKunMirrorReplaceRequest_25000022Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorReplaceRequest_25000022.Builder builder = cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorReplaceRequest_25000022.newBuilder() ; 
		
		builder.setReplace(true);
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorReplaceRequest_25000022.Builder builder = cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorReplaceRequest_25000022.newBuilder() ; 
		
		builder.setReplace(true);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    QianKunMirrorReplaceRequest_25000022Test instance = new QianKunMirrorReplaceRequest_25000022Test();
	    instance.start();
	}

}