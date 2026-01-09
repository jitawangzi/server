package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleBuyTicketRequest_13000554Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleBuyTicketRequest_13000554.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleBuyTicketRequest_13000554.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleBuyTicketRequest_13000554.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleBuyTicketRequest_13000554.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleBuyTicketRequest_13000554Test instance = new BattleBuyTicketRequest_13000554Test();
	    instance.start();
	}

}