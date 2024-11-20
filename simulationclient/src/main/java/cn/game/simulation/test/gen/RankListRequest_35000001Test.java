package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.enume.RankType;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class RankListRequest_35000001Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.RankMsg.RankListRequest_35000001.Builder builder = cn.game.protocol.protobuf.RankMsg.RankListRequest_35000001.newBuilder() ; 
		// 随机一个排行榜
//		RankType[] values = RankType.values();
//		int nextInt = Rnd.nextInt(values.length);
//		RankType rankType = values[nextInt];
//		builder.setType(rankType.ID);

		// 指定排行榜
		builder.setType(RankType.Battle.ID);
		builder.setPage(1);
		builder.setPageSize(100);
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.RankMsg.RankListRequest_35000001.Builder builder = cn.game.protocol.protobuf.RankMsg.RankListRequest_35000001.newBuilder() ; 
		// 随机一个排行榜
//		RankType[] values = RankType.values();
//		int nextInt = Rnd.nextInt(values.length);
//		RankType rankType = values[nextInt];
//		builder.setType(rankType.ID);

		// 指定排行榜
		builder.setType(RankType.Battle.ID);
		builder.setPage(1);
		builder.setPageSize(100);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    RankListRequest_35000001Test instance = new RankListRequest_35000001Test();
	    instance.start();
	}

}