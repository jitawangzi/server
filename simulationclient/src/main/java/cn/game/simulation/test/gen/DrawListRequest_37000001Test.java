package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class DrawListRequest_37000001Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.DrawMsg.DrawListRequest_37000001.Builder builder = cn.game.protocol.protobuf.DrawMsg.DrawListRequest_37000001.newBuilder() ; 
		
		builder.setId(Rnd.get(1, 2));
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.DrawMsg.DrawListRequest_37000001.Builder builder = cn.game.protocol.protobuf.DrawMsg.DrawListRequest_37000001.newBuilder() ; 
		
		builder.setId(Rnd.get(1, 2));
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    DrawListRequest_37000001Test instance = new DrawListRequest_37000001Test();
	    instance.start();
	}

}