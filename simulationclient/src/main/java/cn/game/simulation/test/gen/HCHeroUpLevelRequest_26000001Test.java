package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.BaseMsg.HCHeroInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class HCHeroUpLevelRequest_26000001Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.HCHeroMsg.HCHeroUpLevelRequest_26000001.Builder builder = cn.game.protocol.protobuf.HCHeroMsg.HCHeroUpLevelRequest_26000001.newBuilder() ; 
		
		HCHeroInfo hcHeros = client.getPlayerAllInfo().getHcHeros(0);
		builder.setUid(hcHeros.getUid());
		builder.setAds(true);
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.HCHeroMsg.HCHeroUpLevelRequest_26000001.Builder builder = cn.game.protocol.protobuf.HCHeroMsg.HCHeroUpLevelRequest_26000001.newBuilder() ; 
		
		HCHeroInfo hcHeros = client.getPlayerAllInfo().getHcHeros(0);
		builder.setUid(hcHeros.getUid());
		builder.setAds(true);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HCHeroUpLevelRequest_26000001Test instance = new HCHeroUpLevelRequest_26000001Test();
	    instance.start();
	}

}