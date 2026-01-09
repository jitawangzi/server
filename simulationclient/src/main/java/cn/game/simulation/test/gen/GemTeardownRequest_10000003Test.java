package cn.game.simulation.test.gen;

import cn.game.protocol.generated.config.GemConfig;
import cn.game.protocol.generated.manager.GemManager;
import cn.game.protocol.protobuf.BaseMsg;
import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

import java.util.List;

@Component
public class GemTeardownRequest_10000003Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GemMsg.GemTeardownRequest_10000003.Builder builder = cn.game.protocol.protobuf.GemMsg.GemTeardownRequest_10000003.newBuilder() ;

		List<BaseMsg.EquipPartInfo> equipPartsList = client.getPlayerAllInfo().getEquipPartsList();
		for (BaseMsg.EquipPartInfo equipPartInfo : equipPartsList) {
			equipPartInfo.getGemMapMap().forEach((uid, gemInfo) -> {
				builder.setUid(uid);
				return ;
			});
		}
		return builder.build() ;
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GemMsg.GemTeardownRequest_10000003.Builder builder = cn.game.protocol.protobuf.GemMsg.GemTeardownRequest_10000003.newBuilder() ;

		List<BaseMsg.EquipPartInfo> equipPartsList = client.getPlayerAllInfo().getEquipPartsList();
		for (BaseMsg.EquipPartInfo equipPartInfo : equipPartsList) {
				equipPartInfo.getGemMapMap().forEach((uid, gemInfo) -> {
						builder.setUid(uid);
						return ;
				});
		}
		return builder.build() ;
	}
	
	public static void main(String args[]) throws Exception {
	    GemTeardownRequest_10000003Test instance = new GemTeardownRequest_10000003Test();
	    instance.start();
	}

}