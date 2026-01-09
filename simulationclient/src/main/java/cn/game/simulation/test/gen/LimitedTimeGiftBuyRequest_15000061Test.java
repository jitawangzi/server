package cn.game.simulation.test.gen;

import com.google.protobuf.Message;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.protocol.protobuf.ShopMsg.LimitedTimeGiftInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class LimitedTimeGiftBuyRequest_15000061Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.ShopMsg.LimitedTimeGiftBuyRequest_15000061.Builder builder = cn.game.protocol.protobuf.ShopMsg.LimitedTimeGiftBuyRequest_15000061.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.ShopMsg.LimitedTimeGiftBuyRequest_15000061.Builder builder = cn.game.protocol.protobuf.ShopMsg.LimitedTimeGiftBuyRequest_15000061.newBuilder() ; 
		
		List<LimitedTimeGiftInfo> limitedTimeGiftList = client.getPlayerAllInfo().getLimitedTimeGiftList(); 
		if (limitedTimeGiftList.isEmpty()) {
			return null; 
		}
		builder.setId(Rnd.randomElement(limitedTimeGiftList).getId()); 
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    LimitedTimeGiftBuyRequest_15000061Test instance = new LimitedTimeGiftBuyRequest_15000061Test();
	    instance.start();
	}

}