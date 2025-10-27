package cn.game.simulation.test.gen;

import com.google.protobuf.Message;

import java.util.Collection;

import org.springframework.stereotype.Component;

import cn.game.protocol.generated.config.GuildDonateConfig;
import cn.game.protocol.generated.manager.GuildDonateManager;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class GuildDonateRequest_40000067Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildDonateRequest_40000067.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildDonateRequest_40000067.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildDonateRequest_40000067.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildDonateRequest_40000067.newBuilder() ; 
		Collection<GuildDonateConfig> list = GuildDonateManager.instance().list(); 
		GuildDonateConfig randomElement = Rnd.randomElement(list); 
		builder.setId(randomElement.ID); 
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildDonateRequest_40000067Test instance = new GuildDonateRequest_40000067Test();
	    instance.start();
	}

}