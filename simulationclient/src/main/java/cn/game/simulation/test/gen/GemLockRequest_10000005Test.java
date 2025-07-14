package cn.game.simulation.test.gen;

import cn.game.protocol.protobuf.BaseMsg;
import cn.game.util.Rnd;
import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

import java.util.List;

@Component
public class GemLockRequest_10000005Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GemMsg.GemLockRequest_10000005.Builder builder = cn.game.protocol.protobuf.GemMsg.GemLockRequest_10000005.newBuilder() ;
		List<BaseMsg.GemInfo> list = client.getPlayerAllInfo().getGemsList();
		BaseMsg.GemInfo gemInfo = Rnd.randomElement(list);
		if (gemInfo == null		){
			return null ;
		}
		builder.addUid(gemInfo.getUid());
		builder.setLock(!gemInfo.getIsLock()) ;
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GemMsg.GemLockRequest_10000005.Builder builder = cn.game.protocol.protobuf.GemMsg.GemLockRequest_10000005.newBuilder() ;
		List<BaseMsg.GemInfo> list = client.getPlayerAllInfo().getGemsList();
		BaseMsg.GemInfo gemInfo = Rnd.randomElement(list);
		if (gemInfo == null		){
			return null ;
		}
		builder.addUid(gemInfo.getUid());
		builder.setLock(!gemInfo.getIsLock()) ;
		return builder.build() ;
	}
	
	public static void main(String args[]) throws Exception {
	    GemLockRequest_10000005Test instance = new GemLockRequest_10000005Test();
	    instance.start();
	}

}