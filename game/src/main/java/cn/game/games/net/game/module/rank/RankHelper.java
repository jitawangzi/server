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
		RankModule rankModule = player.getModule(RankModule.class);
		long playerId = player.getPlayerId();

		CompletionStage<RankEntry> rankAsync = RankService.getInstance().getRankEntryAsync(serverId, rankType, playerId);

		CompletionStage<List<RankEntry>> pageAsync = RankService.getInstance().getPageAsync(serverId, rankType, page, pageSize);

		CompletionStage<List<PlayerRank>> playerRankAsync = RankService.getInstance().convertToPlayerRankEntries(pageAsync);

		return rankAsync.thenCombine(playerRankAsync, (rank, rankEntries) -> {
			RankInfo.Builder rankInfo = RankInfo.newBuilder();
			for (PlayerRank entry : rankEntries) {
				PlayerRankInfo.Builder rb = PlayerRankInfo.newBuilder();
				rb.setRank(entry.getRankEntry().getRank());
				rb.setPlayer(entry.getPlayer().toSimplePlayerInfo());
				long score = entry.getRankEntry().getScore();
				rb.setScore((score < 0 ? 0 : score) + "");
				rankInfo.addPlayers(rb);
			}
			rankInfo.setRank(rank == null ? -1 : rank.getRank());
			rankInfo.setScore(rank != null ? rank.getScore() + "" : rankModule.getScore(rankType));
			return rankInfo.build();
		});
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
