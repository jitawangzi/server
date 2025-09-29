package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.protocol.generated.enume.RankType;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityServerOpenRankListRequest_11000203Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankListRequest_11000203.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankListRequest_11000203.newBuilder() ; 
		builder.setType(RankType.LevelServerOpenActivity.ID); 
		builder.setPage(1); 
		builder.setPageSize(20);
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankListRequest_11000203.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankListRequest_11000203.newBuilder() ; 
		builder.setType(RankType.LevelServerOpenActivity.ID); 
		builder.setPage(1); 
		builder.setPageSize(10);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ActivityServerOpenRankListRequest_11000203Test instance = new ActivityServerOpenRankListRequest_11000203Test();
	    instance.start();
	}

}