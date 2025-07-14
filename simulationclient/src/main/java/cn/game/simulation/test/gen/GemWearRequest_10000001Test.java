package cn.game.simulation.test.gen;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.config.GemConfig;
import cn.game.protocol.generated.manager.GemManager;
import cn.game.protocol.protobuf.BaseMsg.EquipPartInfo;
import cn.game.protocol.protobuf.BaseMsg.GemInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class GemWearRequest_10000001Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001.Builder builder = cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001.newBuilder() ; 
		List<GemInfo> gemsList = client.getPlayerAllInfo().getGemsList();

		builder.setUid(Rnd.randomElement(gemsList).getUid());
		builder.setPos(Rnd.get(0, 4));
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001.Builder builder = cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001.newBuilder() ; 
		
		List<GemInfo> gemsList = client.getPlayerAllInfo().getGemsList();
		List<EquipPartInfo> equipPartsList = client.getPlayerAllInfo().getEquipPartsList();
		loop: for (GemInfo gemInfo : gemsList) {
			for (EquipPartInfo equipPartInfo : equipPartsList) {
				GemConfig gemConfig = GemManager.instance().get(gemInfo.getConfigId());
				if (gemConfig.pos == equipPartInfo.getType()) {
					if (!equipPartInfo.getGemMapMap().containsKey(gemInfo.getUid())) {
						builder.setUid(gemInfo.getUid());
						break loop;
					}
				}
			}
        }
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GemWearRequest_10000001Test instance = new GemWearRequest_10000001Test();
	    instance.start();
	}

}