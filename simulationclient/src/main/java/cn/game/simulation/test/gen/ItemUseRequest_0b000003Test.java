package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.ItemMsg.ItemUseInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ItemUseRequest_0b000003Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ItemMsg.ItemUseRequest_0b000003.Builder builder = cn.game.protocol.protobuf.ItemMsg.ItemUseRequest_0b000003.newBuilder() ; 
		ItemUseInfo itemUseInfo = ItemUseInfo.newBuilder().setId(204011).setCount(2).setParam(0).build();
		builder.addItemUse(itemUseInfo);
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ItemMsg.ItemUseRequest_0b000003.Builder builder = cn.game.protocol.protobuf.ItemMsg.ItemUseRequest_0b000003.newBuilder() ; 

		ItemUseInfo itemUseInfo = ItemUseInfo.newBuilder().setId(200000).setCount(1).build();
		builder.addItemUse(itemUseInfo);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ItemUseRequest_0b000003Test instance = new ItemUseRequest_0b000003Test();
	    instance.start();
	}

}