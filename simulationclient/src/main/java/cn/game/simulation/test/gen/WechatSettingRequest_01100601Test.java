package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class WechatSettingRequest_01100601Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.WechatSettingRequest_01100601.Builder builder = cn.game.protocol.protobuf.PlayerMsg.WechatSettingRequest_01100601.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.WechatSettingRequest_01100601.Builder builder = cn.game.protocol.protobuf.PlayerMsg.WechatSettingRequest_01100601.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    WechatSettingRequest_01100601Test instance = new WechatSettingRequest_01100601Test();
	    instance.start();
	}

}