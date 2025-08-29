package cn.game.games.net.game.module.rank;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.Player;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.protobuf.BaseMsg.PlayerRankInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildRankInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildRankList;
import cn.game.protocol.protobuf.RankMsg.RankInfo;
import cn.game.util.LuaScriptUtil;
import cn.game.util.LuaScriptUtil.CopyResult;

public class RankHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(RankHelper.class);
	
	public static CompletionStage<RankInfo> getRankInfo(Player player, RankType rankType, int page, int pageSize) {
		String key = RankService.getInstance().getKey(player.getServerId(), rankType); 
		return getRankInfo(player, key, page, pageSize);
	}
	public static CompletionStage<RankInfo> getRankInfo(Player player, String rankKey, int page, int pageSize) {
		String serverId = player.getServerId();
//		RankModule rankModule = player.getModule(RankModule.class);
		long playerId = player.getPlayerId();
		
		CompletionStage<RankEntry> myRankEntryAsync = RankService.getInstance().getRankEntryAsync(rankKey, playerId);
		CompletionStage<PlayerRank> myPlayerRankAsync = RankService.getInstance().convertToPlayerRankEntry(myRankEntryAsync);
		CompletionStage<List<RankEntry>> rankEntryAsync = RankService.getInstance().getPageAsync(rankKey, page, pageSize);
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
		if (entry.getPlayer() == null) {
			LOGGER.warn("排行榜玩家信息为空, rankEntry={}", entry.getRankEntry());
		}else {
			rb.setPlayer(entry.getPlayer().toSimplePlayerInfo());
		}
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

	public static CompletionStage<GuildRankList> getGuildRankList(Player player, int page, int pageSize) {
		String serverId = player.getServerId();
		long guildId = player.getGuildId();
		RankType rankType = RankType.Guild;
		CompletionStage<RankEntry> myRankEntryAsync = RankService.getInstance().getRankEntryAsync(serverId, rankType, guildId);
		CompletionStage<GuildRank> myGuildRankAsync = RankService.getInstance().convertToGuildRankEntry(myRankEntryAsync);
		CompletionStage<List<RankEntry>> rankEntryAsync = RankService.getInstance().getPageAsync(serverId, rankType, page, pageSize);
		CompletionStage<List<GuildRank>> guildRankAsync = RankService.getInstance().convertToGuildRankEntries(rankEntryAsync);

		return myGuildRankAsync.thenCombine(guildRankAsync, (myGuildRank, rankEntries) -> {
			GuildRankList.Builder rankList = GuildRankList.newBuilder();
			for (GuildRank guildRank : rankEntries) {
				rankList.addGuildRanks(toGuildRankInfo(guildRank));
			}
			if (myGuildRank != null) {
				rankList.setMyRankInfo(toGuildRankInfo(myGuildRank));
			}
			return rankList.build();
		});
	}

	public static GuildRankInfo toGuildRankInfo(GuildRank entry) {
		GuildRankInfo.Builder builder = GuildRankInfo.newBuilder();
		if (entry.getGuild()!=null) {
			builder.setGuild(entry.getGuild().toProto());
		}
		builder.setRank(entry.getRankEntry().getRank());
		long score = entry.getRankEntry().getScore();
		builder.setScore((score < 0 ? 0 : score) + "");
		return builder.build();
	}
	public static CompletionStage<CopyResult> copyRank(String serverId, RankType sourceRankType,RankType targetRankType,int topN) {
		String sourceKey = RankService.getInstance().getKey(serverId, sourceRankType); 
		String targetKey = RankService.getInstance().getKey(serverId, targetRankType); 
		return LuaScriptUtil.copyZSetTopNAsync(sourceKey, targetKey, topN, null, 0).exceptionally(ex -> {
			LOGGER.error("复制排行榜失败，serverId={}, sourceRankType={}, targetRankType={}, topN={}, error={}", serverId, sourceRankType, targetRankType, topN, ex.getMessage());
			return null;
		});
	}
}
