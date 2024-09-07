package cn.game.games.net.game.module.rank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import org.redisson.api.RFuture;
import org.redisson.api.RScoredSortedSet;
import org.redisson.client.protocol.ScoredEntry;

import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.games.core.SimplePlayer;
import cn.game.protocol.generated.enume.RankType;
import cn.game.util.RedisUtil;
import io.vertx.core.Future;

/**    
 * 排行榜服务类，提供了各种操作排行榜的方法。
 * 2024年9月6日 下午8:53:27
 * @author SYQ
 */
public class RankService {

	private static final RankService INSTANCE = new RankService();
	/** 缩放次要分数 */
	private static final double SECONDARY_SCORE_FACTOR = 1e-15;

	private RankService() {
	}

	public static RankService getInstance() {
		return INSTANCE;
	}

	/**
	 * 根据服务器ID和排行榜类型生成Redis键。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @return Redis键
	 */
	private String getKey(String serverId, RankType type) {
		return CacheType.SET_RANK.key(serverId, type.name());
	}

	/**
	 * 更新玩家分数（仅主要分数）。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param playerId 玩家ID
	 * @param score 分数
	 */
	public void updateScore(String serverId, RankType type, long playerId, long score) {
		updateScore(serverId, type, playerId, score, 0);
	}

	/**
	 * 更新玩家分数（主要分数和次要分数）。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param playerId 玩家ID
	 * @param primaryScore 主要分数
	 * @param secondaryScore 次要分数
	 */
	public void updateScore(String serverId, RankType type, long playerId, long primaryScore, long secondaryScore) {
		double combinedScore = primaryScore + secondaryScore * SECONDARY_SCORE_FACTOR;
		RScoredSortedSet<Long> rank = RedisUtil.getRedis().getScoredSortedSet(getKey(serverId, type));
		rank.add(combinedScore, playerId);
	}

	/**
	 * 异步更新玩家分数（仅主要分数）。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param playerId 玩家ID
	 * @param score 分数
	 * @return 异步操作的Future
	 */
	public CompletionStage<Boolean> updateScoreAsync(String serverId, RankType type, long playerId, long score) {
		return updateScoreAsync(serverId, type, playerId, score, 0);
	}

	/**
	 * 异步更新玩家分数（主要分数和次要分数）。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param playerId 玩家ID
	 * @param primaryScore 主要分数
	 * @param secondaryScore 次要分数
	 * @return 异步操作的Future
	 */
	public CompletionStage<Boolean> updateScoreAsync(String serverId, RankType type, long playerId, long primaryScore, long secondaryScore) {
		double combinedScore = primaryScore + secondaryScore * SECONDARY_SCORE_FACTOR;
		RScoredSortedSet<Long> rank = RedisUtil.getRedis().getScoredSortedSet(getKey(serverId, type));
		return rank.addAsync(combinedScore, playerId).whenComplete((k, v) -> {
			if (v != null) {
                v.printStackTrace();
            }
		});
	}

	/**
	 * 获取排行榜前N名的玩家信息。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param n 获取的玩家数量
	 * @return 前N名玩家的排行信息列表
	 */
	public List<RankEntry> getTopN(String serverId, RankType type, int n) {
		RScoredSortedSet<Long> rank = RedisUtil.getRedis().getScoredSortedSet(getKey(serverId, type));
		Collection<ScoredEntry<Long>> entrys = rank.entryRangeReversed(0, n - 1);
		return convertToRankEntries(entrys, 1, n);
	}

	/**
	 * 异步获取排行榜前N名的玩家信息。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param n 获取的玩家数量
	 * @return 异步操作的Future，包含前N名玩家的RankEntry集合
	 */
	public CompletionStage<List<RankEntry>> getTopNAsync(String serverId, RankType type, int n) {
		RScoredSortedSet<Long> rank = RedisUtil.getRedis().getScoredSortedSet(getKey(serverId, type));
		return rank.entryRangeReversedAsync(0, n - 1).thenApply(entrys -> convertToRankEntries(entrys, 1, n));
	}

	/**
	 * 同步获取排行榜的指定页面。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param page 页码
	 * @param pageSize 每页大小
	 * @return 指定页面的玩家排行信息列表
	 */
	public List<RankEntry> getPage(String serverId, RankType type, int page, int pageSize) {
		RScoredSortedSet<Long> rank = RedisUtil.getRedis().getScoredSortedSet(getKey(serverId, type));
		int start = (page - 1) * pageSize;
		int end = start + pageSize - 1;
		Collection<ScoredEntry<Long>> players = rank.entryRangeReversed(start, end);
		return convertToRankEntries(players, page, pageSize);
	}

	/**
	 * 异步获取排行榜的指定页面。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param page 页码
	 * @param pageSize 每页大小
	 * @return 异步操作的Future，包含指定页面的玩家RankEntry集合
	 */
	public CompletionStage<List<RankEntry>> getPageAsync(String serverId, RankType type, int page, int pageSize) {
		RScoredSortedSet<Long> rank = RedisUtil.getRedis().getScoredSortedSet(getKey(serverId, type));
		int start = (page - 1) * pageSize;
		int end = start + pageSize - 1;
		return rank.entryRangeReversedAsync(start, end).thenApply(players -> convertToRankEntries(players, page, pageSize));
	}

	/**
	 * 同步获取玩家在排行榜中的排名。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param playerId 玩家ID
	 * @return 玩家的排名，如果不在排行榜中返回-1
	 */
	public int getRank(String serverId, RankType type, long playerId) {
		RScoredSortedSet<Long> rank = RedisUtil.getRedis().getScoredSortedSet(getKey(serverId, type));
		Integer playerRank = rank.revRank(playerId);
		return playerRank != null ? playerRank + 1 : -1;
	}

	/**
	 * 异步获取玩家在排行榜中的排名。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param playerId 玩家ID
	 * @return 异步操作的Future，包含玩家的排名
	 */
	public CompletionStage<Integer> getRankAsync(String serverId, RankType type, long playerId) {
		RScoredSortedSet<Long> rank = RedisUtil.getRedis().getScoredSortedSet(getKey(serverId, type));
		return rank.revRankAsync(playerId).thenApply(r -> r != null ? r + 1 : -1);
	}

	/** 
	 * 异步获取某人的当前排行分数
	 * @param serverId
	 * @param type
	 * @param playerId
	 * @return
	 */
	public CompletionStage<Long> getRankEntry(String serverId, RankType type, long playerId) {
		RScoredSortedSet<Long> rank = RedisUtil.getRedis().getScoredSortedSet(getKey(serverId, type));
		return rank.getScoreAsync(playerId).thenApply(score -> score == null ? 0 : (long) score.doubleValue());
	}

	/** 
	 * 获取某人的当前排行分数
	 * @param serverId
	 * @param type
	 * @param playerId
	 * @return
	 */
	public long getRankEntryAsync(String serverId, RankType type, long playerId) {
		RScoredSortedSet<Long> rank = RedisUtil.getRedis().getScoredSortedSet(getKey(serverId, type));
		Double score = rank.getScore(playerId);
		return score != null ? score.longValue() : 0;
	}

	/**
	 * 从排行榜中移除玩家。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param playerId 玩家ID
	 */
	public void removePlayer(String serverId, RankType type, long playerId) {
		RScoredSortedSet<Long> rank = RedisUtil.getRedis().getScoredSortedSet(getKey(serverId, type));
		rank.remove(playerId);
	}

	/**
	 * 异步从排行榜中移除玩家。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param playerId 玩家ID
	 * @return 异步操作的Future
	 */
	public RFuture<Boolean> removePlayerAsync(String serverId, RankType type, long playerId) {
		RScoredSortedSet<Long> rank = RedisUtil.getRedis().getScoredSortedSet(getKey(serverId, type));
		return rank.removeAsync(playerId);
	}

	/**
	 * 转成自定义的RankEntry对象,不包含排名。
	 * @param entrys 排行数据
	 * @return 自定义的RankEntry
	 */
	public List<RankEntry> convertToRankEntriesWithoutRank(Collection<ScoredEntry<Long>> entrys) {
		return entrys.stream().map(entry -> new RankEntry(0, entry.getValue(), (long) entry.getScore().doubleValue())).collect(Collectors.toList());
	}

	/** 
	 * 转成自定义的RankEntry对象
	 * @param entrys
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public List<RankEntry> convertToRankEntries(Collection<ScoredEntry<Long>> entrys, int page, int pageSize) {
		int rankOffset = (page - 1) * pageSize; // 计算当前页的排名偏移
		List<RankEntry> rankEntries = new ArrayList<>();
		int rank = rankOffset + 1; // 从当前页的起始排名开始
		for (ScoredEntry<Long> entry : entrys) {
			rankEntries.add(new RankEntry(rank++, entry.getValue(), (long) entry.getScore().doubleValue()));
		}
		return rankEntries;
	}

	/** 
	 * RankEntry 类型转成PlayerRank类型
	 * @param entryAsync
	 * @return
	 */
	public CompletionStage<List<PlayerRank>> convertToPlayerRankEntries(CompletionStage<List<RankEntry>> entryAsync) {
		CompletionStage<List<PlayerRank>> playerRankAsync = entryAsync.thenCompose(rankEntries -> {
			String[] playerIds = rankEntries.stream().map(RankEntry::getPlayerId).map(String::valueOf).toArray(String[]::new);
			Future<List<SimplePlayer>> ret = RedisLocalCache.getInstance().multiGetAsync(CacheType.PLAYER_SIMPLE, playerIds);
			return ret.toCompletionStage().thenApply(simplePlayers -> {
				List<PlayerRank> retList = new ArrayList<>();
				for (int i = 0; i < rankEntries.size(); i++) {
					RankEntry rankEntry = rankEntries.get(i);
					SimplePlayer simplePlayer = simplePlayers.get(i);
					retList.add(new PlayerRank(rankEntry, simplePlayer));
				}
				return retList;
			});
		});
		return playerRankAsync;
	}

	/** 
	 * 
	 * 根据分数查找符合条件的玩家id
	 * @param serverId
	 * @param type
	 * @param scoreStart
	 * @param scoreEnd
	 * @param count 查找数量
	 * @return
	 */
	public CompletionStage<Collection<Long>> searchRankEntryByScoreAsync(String serverId, RankType type, long scoreStart, long scoreEnd, int count) {
		RScoredSortedSet<Long> scoredSortedSet = RedisUtil.getRedis().getScoredSortedSet(getKey(serverId, type));
		return scoredSortedSet.valueRangeAsync(scoreStart, true, scoreEnd, true, 0, count);
	}

}