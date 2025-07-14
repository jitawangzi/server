package cn.game.simulation.test.gen;

import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.druid.util.StringUtils;
import com.google.protobuf.Message;

import cn.game.protocol.protobuf.BaseMsg.EquipPartInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class EquipTeardownRequest_09000003Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.EquipMsg.EquipTeardownRequest_09000003.Builder builder = cn.game.protocol.protobuf.EquipMsg.EquipTeardownRequest_09000003.newBuilder() ;
		builder.setUid("301619302048487206");

		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.EquipMsg.EquipTeardownRequest_09000003.Builder builder = cn.game.protocol.protobuf.EquipMsg.EquipTeardownRequest_09000003.newBuilder() ; 
		
		List<EquipPartInfo> equipsList = client.getPlayerAllInfo().getEquipPartsList();
		if (equipsList == null || equipsList.isEmpty()) {
			return null;
		}
		for (EquipPartInfo equipPartInfo : equipsList) {
			if (!StringUtils.isEmpty(equipPartInfo.getEquipUid())) {
				builder.setUid(equipPartInfo.getEquipUid());
				break;
			}
		}
		if (StringUtils.isEmpty(builder.getUid())) {
			return null; // 没有可拆卸的装备
		}
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    EquipTeardownRequest_09000003Test instance = new EquipTeardownRequest_09000003Test();
	    instance.start();
	}

}