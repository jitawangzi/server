package cn.game.simulation.test.gen;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.BaseMsg.HeroInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class HeroQualityResetRequest_16000011Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroQualityResetRequest_16000011.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroQualityResetRequest_16000011.newBuilder() ; 
		
		PlayerAllInfo playerAllInfo = client.getPlayerAllInfo();
		List<HeroInfo> herosList = playerAllInfo.getHerosList();
		builder.setUid(Rnd.randomElement(herosList).getUid());
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroQualityResetRequest_16000011.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroQualityResetRequest_16000011.newBuilder() ; 
		
		PlayerAllInfo playerAllInfo = client.getPlayerAllInfo();
		List<HeroInfo> herosList = playerAllInfo.getHerosList();
		builder.setUid(Rnd.randomElement(herosList).getUid());
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HeroQualityResetRequest_16000011Test instance = new HeroQualityResetRequest_16000011Test();
	    instance.start();
	}

}