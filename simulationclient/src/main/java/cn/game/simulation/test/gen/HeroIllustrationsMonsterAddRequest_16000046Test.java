package cn.game.simulation.test.gen;

import com.google.protobuf.Message;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;
import io.netty.handler.codec.quic.QuicPathEvent.New;

@Component
public class HeroIllustrationsMonsterAddRequest_16000046Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsMonsterAddRequest_16000046.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsMonsterAddRequest_16000046.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsMonsterAddRequest_16000046.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsMonsterAddRequest_16000046.newBuilder() ; 
		int id = Rnd.nextInt(100); 
		List<Integer> list = (List<Integer>) client.dataMap.computeIfAbsent("HeroIllustrationsMonsterAddRequest_16000046_id", k -> new ArrayList<Integer>());
		list.add(id);
        builder.addId(id);		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HeroIllustrationsMonsterAddRequest_16000046Test instance = new HeroIllustrationsMonsterAddRequest_16000046Test();
	    instance.start();
	}

}