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
public class HeroBattleRequest_16000005Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroBattleRequest_16000005.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroBattleRequest_16000005.newBuilder() ; 
		
		PlayerAllInfo playerAllInfo = client.getPlayerAllInfo();
		List<HeroInfo> herosList = playerAllInfo.getHerosList();
		builder.setUid(Rnd.randomElement(herosList).getUid());
//		builder.setUid("7707204458765123072");
		builder.setPos(Rnd.get(1, 5));
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroBattleRequest_16000005.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroBattleRequest_16000005.newBuilder() ; 
		
		PlayerAllInfo playerAllInfo = client.getPlayerAllInfo();
		List<HeroInfo> herosList = playerAllInfo.getHerosList();
		builder.setUid(Rnd.randomElement(herosList).getUid());
//		builder.setUid("7707204458765123072");
		builder.setPos(Rnd.get(1, 5));
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HeroBattleRequest_16000005Test instance = new HeroBattleRequest_16000005Test();
	    instance.start();
	}

}