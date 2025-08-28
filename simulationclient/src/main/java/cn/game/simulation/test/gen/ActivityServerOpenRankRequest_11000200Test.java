package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityServerOpenRankRequest_11000200Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankRequest_11000200.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankRequest_11000200.newBuilder() ; 
		builder.setId(42); 
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankRequest_11000200.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankRequest_11000200.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ActivityServerOpenRankRequest_11000200Test instance = new ActivityServerOpenRankRequest_11000200Test();
	    instance.start();
	}

}