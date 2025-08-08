package cn.game.games.net.game.module.rank;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import cn.game.games.cache.entity.Player;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.protobuf.BaseMsg.PlayerRankInfo;
import cn.game.protocol.protobuf.RankMsg.RankInfo;

public class RankHelper {

	public static CompletionStage<RankInfo> getRankInfo(Player player, RankType rankType, int page, int pageSize) {
		String serverId = player.getServerId();
//		RankModule rankModule = player.getModule(RankModule.class);
		long playerId = player.getPlayerId();

		CompletionStage<RankEntry> myRankEntryAsync= RankService.getInstance().getRankEntryAsync(serverId, rankType, playerId);
		CompletionStage<PlayerRank> myPlayerRankAsync = RankService.getInstance().convertToPlayerRankEntry(myRankEntryAsync);
		CompletionStage<List<RankEntry>> rankEntryAsync = RankService.getInstance().getPageAsync(serverId, rankType, page, pageSize);
		CompletionStage<List<PlayerRank>> playerRankAsync = RankService.getInstance().convertToPlayerRankEntries(rankEntryAsync);

		return myPlayerRankAsync.thenCombine(playerRankAsync, (myPlayerRank, rankEntries) -> {
			RankInfo.Builder rankInfo = RankInfo.newBuilder();
			for (PlayerRank playerRank : rankEntries) {
				rankInfo.addPlayers(toRankInfo(playerRank));
			}
			rankInfo.setMyRankInfo(toRankInfo(myPlayerRank)); 
			return rankInfo.build();
		});
	}
	public static PlayerRankInfo toRankInfo(PlayerRank entry) {
		PlayerRankInfo.Builder rb = PlayerRankInfo.newBuilder();
		rb.setRank(entry.getRankEntry().getRank());
		rb.setPlayer(entry.getPlayer().toSimplePlayerInfo());
		long score = entry.getRankEntry().getScore();
		rb.setScore((score < 0 ? 0 : score) + "");
		return rb.build();
	}

	public static CompletionStage<List<PlayerRankInfo>> getRankPagePlayerInfos(String serverId, RankType rankType, int page, int pageSize) {

		CompletionStage<List<RankEntry>> pageAsync = RankService.getInstance().getPageAsync(serverId, rankType, page, pageSize);

		return RankService.getInstance().convertToPlayerRankEntries(pageAsync).thenApply(rankEntries -> {
			List<PlayerRankInfo> list = new ArrayList<>();
			for (PlayerRank entry : rankEntries) {
				PlayerRankInfo.Builder rb = PlayerRankInfo.newBuilder();
				rb.setRank(entry.getRankEntry().getRank());
				rb.setPlayer(entry.getPlayer().toSimplePlayerInfo());
				long score = entry.getRankEntry().getScore();
				rb.setScore((score < 0 ? 0 : score) + "");
				list.add(rb.build());
			}
			return list;
		});
	}
}
