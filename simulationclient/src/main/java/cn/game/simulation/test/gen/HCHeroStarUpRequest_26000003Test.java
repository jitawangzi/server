package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.BaseMsg.HCHeroInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class HCHeroStarUpRequest_26000003Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.HCHeroMsg.HCHeroStarUpRequest_26000003.Builder builder = cn.game.protocol.protobuf.HCHeroMsg.HCHeroStarUpRequest_26000003.newBuilder() ; 
		
		HCHeroInfo hcHeros = client.getPlayerAllInfo().getHcHeros(0);
		builder.setUid(hcHeros.getUid());
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.HCHeroMsg.HCHeroStarUpRequest_26000003.Builder builder = cn.game.protocol.protobuf.HCHeroMsg.HCHeroStarUpRequest_26000003.newBuilder() ; 
		
		HCHeroInfo hcHeros = client.getPlayerAllInfo().getHcHeros(0);
		builder.setUid(hcHeros.getUid());
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HCHeroStarUpRequest_26000003Test instance = new HCHeroStarUpRequest_26000003Test();
	    instance.start();
	}

}