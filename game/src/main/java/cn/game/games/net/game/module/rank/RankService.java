package cn.game.games.net.game.module.rank;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.redisson.api.BatchResult;
import org.redisson.api.RBatch;
import org.redisson.api.RFuture;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RScoredSortedSetAsync;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.protocol.ScoredEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.core.process.OffsetBatchQuery;
import cn.game.core.task.SchedulerService;
import cn.game.core.util.BatchQueryUtil;
import cn.game.games.core.SimplePlayer;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.cross.guild.SimpleGuild;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.mail.MailRankInfo;
import cn.game.games.net.game.module.mail.MailType;
import cn.game.protocol.generated.config.DaShengNPCConfig;
import cn.game.protocol.generated.config.RankConfig;
import cn.game.protocol.generated.config.RankRewardConfig;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.DaShengNPCManager;
import cn.game.protocol.generated.manager.RankManager;
import cn.game.protocol.generated.manager.RankRewardManager;
import cn.game.protocol.protobuf.BaseMsg;
import cn.game.util.BinarySearchUtil;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;
import cn.game.util.LockUtil;
import cn.game.util.LuaScriptUtil;
import cn.game.util.RedisUtil;
import io.vertx.core.Future;

/**
 * 排行榜服务类，提供了各种操作排行榜的方法。
 * 2024年9月6日 下午8:53:27
 * @author SYQ
 */
public class RankService {
	private static final Logger log = LoggerFactory.getLogger(RankService.class);

	private static final RankService INSTANCE = new RankService();
	/** 缩放次要分数 */
	private static final double SECONDARY_SCORE_FACTOR = 1e-15;
	private static final long TIME_END = 4093726323l;//2099-09-22 10:12:03


	private static final int DEFAULT_PAGE_SIZE = 50;

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
	public String getKey(String serverId, RankType type) {
		return CacheType.SET_RANK.key("{" + serverId + "}", type.name());
	}

	/**
	 * 设置玩家分数（仅主要分数）。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param playerId 玩家ID
	 * @param score 分数
	 */
	public void setScore(String serverId, RankType type, long playerId, long score) {
		setScore(serverId, type, playerId, score, TIME_END - DateUtil.currentTimeSeconds());
	}

	/**
	 * 设置玩家分数（主要分数和次要分数）。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param playerId 玩家ID
	 * @param primaryScore 主要分数
	 * @param secondaryScore 次要分数
	 */
	public void setScore(String serverId, RankType type, long playerId, long primaryScore, long secondaryScore) {
		double combinedScore = primaryScore + secondaryScore * SECONDARY_SCORE_FACTOR;
		RScoredSortedSet<Long> rank = getRankSet(serverId, type);
		rank.add(combinedScore, playerId);
	}

	/**
	 * 异步设置玩家分数（仅主要分数）。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param playerId 玩家ID
	 * @param score 分数
	 * @return 异步操作的Future
	 */
	public CompletionStage<Boolean> setScoreAsync(String serverId, RankType type, long playerId, long score) {
		return setScoreAsync(serverId, type, playerId, score, TIME_END - DateUtil.currentTimeSeconds());
	}

	/**
	 * 异步设置玩家分数（主要分数和次要分数）。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param playerId 玩家ID
	 * @param primaryScore 主要分数
	 * @param secondaryScore 次要分数
	 * @return 异步操作的Future
	 */
	public CompletionStage<Boolean> setScoreAsync(String serverId, RankType type, long playerId, long primaryScore, long secondaryScore) {
		double combinedScore = primaryScore + secondaryScore * SECONDARY_SCORE_FACTOR;
		RScoredSortedSet<Long> rank = getRankSet(serverId, type);
		return rank.addAsync(combinedScore, playerId).whenComplete((k, v) -> {
			//log.info("setScoreAsync: {}, {}, {}, {}, {}", serverId, type, playerId, primaryScore, secondaryScore);
			if (v != null) {
				v.printStackTrace();
			}
		});
	}

	/**
	 * 增加或减少玩家某排行分数
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param playerId 玩家ID
	 * @param score 更新的分数，正数为增加，负数为减少
	 * @return 返回更新后的最终分数
	 */
	public double updateScore(String serverId, RankType type, long playerId, long score) {
		RScoredSortedSet<Long> rank = getRankSet(serverId, type);
		return rank.addScore(playerId, score);
	}

	/**
	 * 异步增加或减少玩家某排行分数
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param playerId 玩家ID
	 * @param score 更新的分数，正数为增加，负数为减少
	 * @return 返回更新后的最终分数
	 */
	public CompletionStage<Double> updateScoreAsync(String serverId, RankType type, long playerId, long score) {
		RScoredSortedSet<Long> rank = getRankSet(serverId, type);
		return rank.addScoreAsync(playerId, score);
	}
	
	/**
	 * 批量增加或减少一组玩家的排行榜分数 (同步)
	 * <p>
	 * 将所有玩家的分数更新命令打包，一次性发送给Redis执行。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param playerIds 玩家ID集合
	 * @param score 要增加或减少的分数 (正数为增加，负数为减少)
	 */
	public void batchUpdateScore(String serverId, RankType type, Collection<Long> playerIds, long score) {
	    if (playerIds == null || playerIds.isEmpty()) {
	        return;
	    }
	    String rankKey = getKey(serverId, type);
	    
	    // 1. 从Redisson客户端创建RBatch实例
	    RBatch batch = RedisUtil.getRedis().createBatch();
	    
	    // 2. 从batch实例中获取异步的ScoredSortedSet
	    // 在批处理模式下，所有命令都使用其异步接口
	    RScoredSortedSetAsync<Long> rankSet = batch.getScoredSortedSet(rankKey, LongCodec.INSTANCE);

	    // 3. 将所有更新分数的命令添加到batch中
	    for (Long playerId : playerIds) {
	        // 这里调用的是异步方法，但它不会立即执行，而是被添加到批处理队列中
	        rankSet.addScoreAsync(playerId, score);
	    }
	    // 4. 同步执行批处理中的所有命令
	    // Redisson会将队列中的所有命令一次性发送到Redis
	    batch.execute();
	}

	/**
	 * 异步批量增加或减少一组玩家的排行榜分数
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param playerIds 玩家ID集合
	 * @param score 要增加或减少的分数 (正数为增加，负数为减少)
	 * @return 返回一个RFuture，当批量操作完成时，可以通过它获取结果
	 */
	public RFuture<BatchResult<?>> batchUpdateScoreAsync(String serverId, RankType type, Collection<Long> playerIds, long score) {
	    // 1. 创建 RBatch 实例
	    RBatch batch = RedisUtil.getRedis().createBatch();

	    // 2. 检查 playerIds 是否为空
	    if (playerIds == null || playerIds.isEmpty()) {
	        // 如果没有玩家ID，直接执行这个空的batch。
	        // 这会立即返回一个已完成的RFuture，其结果是一个空的BatchResult。
	        // 这是在旧版本中返回一个“已完成的Future”的最佳实践。
	        return batch.executeAsync();
	    }
	    
	    // 3. 如果playerIds不为空，则添加命令
	    String rankKey = getKey(serverId, type);
	    RScoredSortedSetAsync<Long> rankSet = batch.getScoredSortedSet(rankKey, LongCodec.INSTANCE);

	    for (Long playerId : playerIds) {
	        rankSet.addScoreAsync(playerId, score);
	    }
	    // 4. 异步执行批处理
	    return batch.executeAsync();
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
		RScoredSortedSet<Long> rank = getRankSet(serverId, type);
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
		RScoredSortedSet<Long> rank = getRankSet(serverId, type);
		return rank.entryRangeReversedAsync(0, n - 1).thenApply(entrys -> convertToRankEntries(entrys, 1, n));
	}
	
	/**
	 * 将 Redisson 返回的 entries 转换为 RankEntry，名次从 startRank 开始递增。
	 */
	public List<RankEntry> convertToRankEntriesByStartRank(Collection<ScoredEntry<Long>> entries, int startRank) {
	    int rank = Math.max(1, startRank);
	    List<RankEntry> result = new ArrayList<>(entries.size());
	    for (ScoredEntry<Long> e : entries) {
	        result.add(new RankEntry(rank++, e.getValue(), (long) e.getScore().doubleValue()));
	    }
	    return result;
	}

	/**
	 * 获取排行榜指定名次区间的玩家信息（包含端点），按名次从高到低。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param startRank 起始名次（1-based，>=1）
	 * @param endRank 结束名次（1-based，>= startRank）
	 * @return 指定区间的排行信息列表
	 */
	public List<RankEntry> getRange(String serverId, RankType type, int startRank, int endRank) {
	    if (startRank < 1 || endRank < startRank) {
	        return Collections.emptyList();
	    }
	    RScoredSortedSet<Long> rank = getRankSet(serverId, type);

	    int fromIndex = startRank - 1; // 0-based
	    int toIndex = endRank - 1;     // 0-based

	    Collection<ScoredEntry<Long>> entries = rank.entryRangeReversed(fromIndex, toIndex);
	    return convertToRankEntriesByStartRank(entries, startRank);
	}
	/**
	 * 异步获取排行榜指定名次区间的玩家信息（包含端点），按名次从高到低。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param startRank 起始名次（1-based，>=1）
	 * @param endRank 结束名次（1-based，>= startRank）
	 * @return 异步操作的Future，包含指定区间的RankEntry集合
	 */
	public CompletionStage<List<RankEntry>> getRangeAsync(String serverId, RankType type, int startRank, int endRank) {
	    if (startRank < 1 || endRank < startRank) {
	        return CompletableFuture.completedFuture(Collections.emptyList());
	    }
	    RScoredSortedSet<Long> rank = getRankSet(serverId, type);

	    int fromIndex = startRank - 1;
	    int toIndex = endRank - 1;

	    return rank.entryRangeReversedAsync(fromIndex, toIndex)
	               .thenApply(entries -> convertToRankEntriesByStartRank(entries, startRank));
	}
	/**
	 * 同步获取指定排名的玩家信息。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param rankId 排名
	 * @return 玩家信息
	 */
	public RankEntry getRankEntry(String serverId, RankType type, int rankId) {
		RScoredSortedSet<Long> rank = getRankSet(serverId, type);
		return convertToRankEntries(rank.entryRangeReversed(rankId-1, rankId-1) , 1, 1).get(0);
	}
	/**
	 * 异步获取指定排名的玩家信息。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param rankId 排名
	 * @return 异步操作的Future，包含指定排名的玩家信息
	 */
	public CompletionStage <RankEntry> getRankEntryAsync(String serverId, RankType type, int rankId) {
		RScoredSortedSet<Long> rank = getRankSet(serverId, type);
		return rank.entryRangeReversedAsync(rankId-1, rankId-1).thenApply(
				entrys ->
						convertToRankEntries(entrys , rankId)).thenApply(
								list -> list.get(0));
	}
	/**
	 * 同步获取排行榜的最后一个玩家信息。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @return 最后一个玩家信息
	 */
	public List<RankEntry> getLastN(String serverId, RankType type,int n) {
		RScoredSortedSet<Long> rank = getRankSet(serverId, type);
		int size = rank.size();
		if(size<=n) {
			n=size  ;
		}
		Collection<ScoredEntry<Long>> entrys = rank.entryRangeReversed(size-n-1,  size-1);
		return convertToRankEntries(entrys, 1, n);
	}
	/**
	 * 异步获取排行榜的最后N个玩家信息（排名最低的玩家）。
	 *
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @param n 获取的玩家数量
	 * @return 异步操作的Future，包含最后N个玩家信息
	 */
	public CompletionStage<List<RankEntry>> getLastNAsync(String serverId, RankType type, int n) {
		if (n <= 0) {
			return CompletableFuture.completedFuture(new ArrayList<>());
		}
		RScoredSortedSet<Long> rank = getRankSet(serverId, type);
		return rank.sizeAsync().thenCompose(size -> {
			if (size <= 0) {
				return CompletableFuture.completedFuture(new ArrayList<>());
			}

			int actualN = Math.min(n, size);
			int startIndex = size - actualN;
			int endIndex = size - 1;

			return rank.entryRangeReversedAsync(startIndex, endIndex)
					.thenApply(
							entries ->
							{
								List<RankEntry> rankEntries = new ArrayList<>();
								int rankBegin =startIndex + 1; // 从当前页的起始排名开始
								for (ScoredEntry<Long> entry : entries) {
									rankEntries.add(new RankEntry(rankBegin++, entry.getValue(), (long) entry.getScore().doubleValue()));
								}
								return rankEntries;
							}
					);
		});
	}

	/**
	 * 同步获取排行榜的指定页面。
	 *
	 * @param serverId 服务器ID
	 * @param page 页码
	 * @param pageSize 每页大小
	 * @return 指定页面的玩家排行信息列表
	 */
	public List<RankEntry> getPage(String serverId, RankType rankType, int page, int pageSize) {
		RScoredSortedSet<Long> rank = getRankSet(serverId,rankType);
		int start = (page - 1) * pageSize;
		int end = start + pageSize - 1;
		Collection<ScoredEntry<Long>> players = rank.entryRangeReversed(start, end);
		return convertToRankEntries(players, page, pageSize);
	}



	/**
	 * 获取排行榜的总数据量
	 * @param serverId 服务器ID
	 * @param type 排行榜类型
	 * @return 排行榜数据量
	 */
	public CompletionStage<Integer> getRankSizeAsync(String serverId, RankType type){
		RScoredSortedSet<Long> rank = getRankSet(serverId, type);
		return rank.sizeAsync();
	}

	private RScoredSortedSet<Long> getRankSet(String serverId, RankType type) {
		return getRankSet(getKey(serverId, type));
	}
	private RScoredSortedSet<Long> getRankSet(String rankKey) {
		RScoredSortedSet<Long> rank = RedisUtil.getRedis().getScoredSortedSet(rankKey, LongCodec.INSTANCE);
		return rank;
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
		return getPageAsync(getKey(serverId, type), page, pageSize);
	}
	public CompletionStage<List<RankEntry>> getPageAsync(String rankKey, int page, int pageSize) {
		RScoredSortedSet<Long> rank = getRankSet(rankKey);
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
		RScoredSortedSet<Long> rank = getRankSet(serverId, type);
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
		return getRankAsync(getKey(serverId, type), playerId); 
	}
	public CompletionStage<Integer> getRankAsync(String rankKey, long playerId) {
		RScoredSortedSet<Long> rank = getRankSet(rankKey);
		return rank.revRankAsync(playerId).thenApply(r -> r != null ? r + 1 : -1);
	}

	/**
	 * 异步获取某人的当前排行分数
	 * @param serverId
	 * @param type
	 * @param playerId
	 * @return
	 */
	public CompletionStage<Long> getScoreAsync(String serverId, RankType type, long playerId) {
		return getScoreAsync(getKey(serverId, type), playerId); 
	}
	public CompletionStage<Long> getScoreAsync(String rankKey, long playerId) {
		RScoredSortedSet<Long> rank = getRankSet(rankKey);
		return rank.getScoreAsync(playerId).thenApply(score -> score == null ? 0 : (long) score.doubleValue());
	}

	/**
	 * 获取某人的当前排行分数
	 * @param serverId
	 * @param type
	 * @param playerId
	 * @return
	 */
	public long getScore(String serverId, RankType type, long playerId) {
		RScoredSortedSet<Long> rank = getRankSet(serverId, type);
		Double score = rank.getScore(playerId);
		return score != null ? score.longValue() : 0;
	}

	/**
	 * 异步获取某人的当前排行数据
	 * @param serverId
	 * @param type
	 * @param playerId
	 * @return
	 */
	public CompletionStage<RankEntry> getRankEntryAsync(String serverId, RankType type, long playerId) {
		return getRankEntryAsync(getKey(serverId, type), playerId); 
	}
	public CompletionStage<RankEntry> getRankEntryAsync(String rankKey, long playerId) {
		CompletionStage<Long> scoreAsync = getScoreAsync(rankKey, playerId);
		CompletionStage<Integer> rankAsync = getRankAsync(rankKey, playerId);
		return scoreAsync.thenCombine(rankAsync, (score, rank) -> new RankEntry(rank, playerId, score));
	}

	/**
	 * 获取某人的当前排行分数
	 * @param serverId
	 * @param type
	 * @param playerId
	 * @return
	 */
	public RankEntry getRankEntry(String serverId, RankType type, long playerId) {
		long score = getScore(serverId, type, playerId);
		int rank = getRank(serverId, type, playerId);
		return new RankEntry(rank, playerId, score);
	}

	/**
	 * 删除某个排行榜
	 *
	 * @param type 排行榜类型
	 * @return 异步操作的Future
	 */
	public void removeRank(RankType type) {
		String[] serverIds = ServerHelper.getServerIds();
		for (int i = 0; i < serverIds.length; i++) {
			String serverId = serverIds[i];
			String key = getKey(serverId, type);
			RedisUtil.delete(key);
		}
	}

	/**
	 * 异步删除某个排行榜
	 *
	 * @param type 排行榜类型
	 * @return 异步操作的Future
	 */
	public CompletableFuture<Void> removeRankAsync(RankType type) {
		String[] serverIds = ServerHelper.getServerIds();
		CompletableFuture<Boolean>[] futures = new CompletableFuture[serverIds.length];
		for (int i = 0; i < futures.length; i++) {
			String serverId = serverIds[i];
			futures[i] = removeRankAsync(type, serverId).toCompletableFuture();
		}
		return CompletableFuture.allOf(futures);
	}

	public CompletableFuture<Boolean> removeRankAsync(RankType type, String serverId) {
		String key = getKey(serverId, type);

		RFuture<Boolean> rank = RedisUtil.deleteAsync(key);
		return rank.toCompletableFuture();
	}

	/**
	 * 异步从排行榜中移除玩家。
	 * @param type 排行榜类型
	 * @param serverId 服务器ID 
	 * @param targetId 目标ID
	 * @return
	 */
	public RFuture<Boolean> removeRankAsync(RankType type, String serverId, long targetId) {
		RScoredSortedSet<Long> rank = getRankSet(serverId, type);
		return rank.removeAsync(targetId);
	}

	/**
	 * 把一个玩家从所有排行榜中移除
	 * @param playerId
	 * @return
	 */
	public void removeRankAsync(long playerId) {
		PlayerHelper.getServerIdAsync(playerId).map(serverId -> {
			for (RankType rankType : RankType.values()) {
				removeRankAsync(rankType, serverId, playerId);
			}
			return null;
		}).onFailure(e -> {
			log.error("removeRankAsync error " + playerId, e);
		});
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
	 * 转成自定义 制定名次的的RankEntry对象
	 * @param entrys
	 */
	public List<RankEntry> convertToRankEntries(Collection<ScoredEntry<Long>> entrys,int rankId) {
		List<RankEntry> rankEntries = new ArrayList<>();
		for (ScoredEntry<Long> entry : entrys) {
			rankEntries.add(new RankEntry(rankId, entry.getValue(), (long) entry.getScore().doubleValue()));
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
			String[] playerIds = rankEntries.stream().map(RankEntry::getId).map(String::valueOf).toArray(String[]::new);
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
	public CompletionStage<List<GuildRank>> convertToGuildRankEntries(CompletionStage<List<RankEntry>> entryAsync) {
		CompletionStage<List<GuildRank>> rankAsync = entryAsync.thenCompose(rankEntries -> {
			String[] ids = rankEntries.stream().map(RankEntry::getId).map(String::valueOf).toArray(String[]::new);
			Future<List<SimpleGuild>> ret = RedisLocalCache.getInstance().multiGetAsync(CacheType.ZONG_MEN_SIMPLE_DATA, ids);
			return ret.toCompletionStage().thenApply(simpleGuilds -> {
				List<GuildRank> retList = new ArrayList<>();
				for (int i = 0; i < rankEntries.size(); i++) {
					RankEntry rankEntry = rankEntries.get(i);
					SimpleGuild simpleGuild = simpleGuilds.get(i);
					retList.add(new GuildRank(rankEntry, simpleGuild));
				}
				return retList;
			});
		});
		return rankAsync;
	}
	public CompletionStage<PlayerRank> convertToPlayerRankEntry(CompletionStage<RankEntry> entryAsync) {
		CompletionStage<PlayerRank> playerRankAsync = entryAsync.thenCompose(rankEntry -> {
			Future<SimplePlayer> ret = RedisLocalCache.getInstance().getAsync(CacheType.PLAYER_SIMPLE.key(rankEntry.getId()));
			return ret.toCompletionStage().thenApply(simplePlayer -> {
				return new PlayerRank(rankEntry, simplePlayer); 
			});
		});
		return playerRankAsync;
	}
	public CompletionStage<GuildRank> convertToGuildRankEntry(CompletionStage<RankEntry> entryAsync) {
		CompletionStage<GuildRank> rankAsync = entryAsync.thenCompose(rankEntry -> {
			Future<SimpleGuild> ret = RedisLocalCache.getInstance().getAsync(CacheType.ZONG_MEN_SIMPLE_DATA.key(rankEntry.getId()));
			return ret.toCompletionStage().thenApply(simpleGuild -> {
				return new GuildRank(rankEntry, simpleGuild); 
			});
		});
		return rankAsync;
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
		RScoredSortedSet<Long> scoredSortedSet = getRankSet(serverId, type);
		return scoredSortedSet.valueRangeAsync(scoreStart, true, scoreEnd, true, 0, count);
	}

	/**
	 * 如果当前值大于历史值则更新，注意不能并发调用。 
	 * @param serverId
	 * @param type
	 * @param playerId
	 * @param newValue
	 * @return
	 */
	public CompletableFuture<Double> updateMaxValueAsync(String serverId, RankType type, long playerId, double newValue) {
		String key = getKey(serverId, type);
		RScoredSortedSet<Long> sortedSet = RedisUtil.getRedis().getScoredSortedSet(key, LongCodec.INSTANCE);

		CompletableFuture<Double> resultFuture = new CompletableFuture<>();

		sortedSet.getScoreAsync(playerId).thenCompose(currentScore -> {
			if (currentScore == null) {
				return sortedSet.addScoreAsync(playerId, newValue);
			} else {
				if (newValue > currentScore) {
					return sortedSet.addScoreAsync(playerId, newValue - currentScore);
				}
				return CompletableFuture.completedFuture(currentScore);
			}
		}).whenComplete((score, throwable) -> {
			if (throwable != null) {
				resultFuture.completeExceptionally(throwable);
			} else {
				resultFuture.complete(score);
			}
		});

		return resultFuture;
	}

	/**
	 * 如果当前值大于历史值则更新,使用lua脚本实现,保证原子性
	 * @param serverId
	 * @param type
	 * @param playerId
	 * @param newValue
	 * @return
	 */
	public CompletionStage<Double> updateMaxValueAsyncRScript(String serverId, RankType type, long playerId, double newValue) {
		String key = getKey(serverId, type);
		return LuaScriptUtil.updateScoreIfGreater(key, playerId, newValue);
	}


	/**
	 * 初始化排行榜结算任务
	 */
	public void initRewardTask() {
		Collection<RankConfig> list = RankManager.instance().list();
		for (RankConfig rankConfig : list) {
			initRewardTask(rankConfig.ID);
		}
	}

	/**
	 * 结算排行榜
	 * @param rankId
	 */
	public void rewardGm(int rankId) {
		reward(rankId,true);
	}
	private void reward(int rankId,boolean gm) {
		log.info("pre rank reward,rankId[{}] server[{}]", rankId, ServerContext.getInstance().getServerId());

		RankConfig rankConfig = RankManager.instance().get(rankId);
		List<RankRewardConfig> rewardList = RankRewardManager.instance().getTypeList(rankId);
		if (rewardList.isEmpty()) {
			return;
		}
		RankType rankType = RankType.get(rankId);
		boolean acquire = LockUtil.acquire(CacheType.RANK_REWARD_ACQUIRE.key(rankId),30, TimeUnit.MINUTES);
		if (!gm && !acquire) {
			return;
		}
		String[] serverIds = ServerHelper.getServerIds();
		reward(serverIds, rankId);
		if (rankConfig.ResetRank) {
			log.info("removeRank, rankId:{}", rankId);
			if (rankType == RankType.DaShengLeiTaiSeason) {
				long start=DateUtil.currentTimeMillis();
				long end = DateUtil.addWeekBeginTimer(1);
				for (String serverId : serverIds) {
					List<MailRankInfo> playerRank = new ArrayList<>();
					CompletionStage<List<BaseMsg.PlayerRankInfo>> rankPagePlayerInfos = RankHelper.getRankPagePlayerInfos(serverId, RankType.DaShengLeiTaiSeason, 1, 5);

					rankPagePlayerInfos.thenCompose(r -> {
						r.forEach(p -> {
							playerRank.add(new MailRankInfo(p.getPlayer().getName(), p.getRank(), p.getPlayer().getFigure(), p.getPlayer().getId()));
						});
						String content = JsonUtil.toJsonStringWithType(playerRank);
						MailHelper.addGlobalGmMail(content, serverId, start, end, (byte) MailType.DASHENG_XUN_SHAN.getValue());
						log.info("removeRank1");
						CompletableFuture<Void> removeRankFuture = removeRankAsync(RankType.DaShengLeiTaiSeason)
								.thenCompose(v -> {return removeRankAsync(RankType.DaShengLeiTaiDay);}).thenCompose(
										v -> {return setNpcToRankTest(serverId);}
								);
						return removeRankFuture;
					}).exceptionally(t -> {
						log.error("setNpcToRankTest failed: serverId={}, rankType={}", serverId, rankType);
						log.error("e:" ,t);
						return null;
					});
				}
			}else {
				removeRank(rankType);
			}
		}else
		{
			if (rankType==RankType.DaDaoZhengFengDay) {
				if (LocalDate.now().getDayOfWeek().getValue() == 1) {
					removeRank(rankType);
				}
			}
		}
	}
	public void setNpcToRank() {
		// 准备NPC数据
		Map<Long, Long> npcScores = new HashMap<>();
		List<DaShengNPCConfig> list2 = DaShengNPCManager.instance().list();
		for (DaShengNPCConfig config : list2) {
			npcScores.put((long)config.ID, (long) config.Integral);
		}
		String[] serverIds = ServerHelper.getServerIds();
		RankType[] rankTypes = {RankType.DaShengLeiTaiDay, RankType.DaShengLeiTaiSeason};
		for (String serverId : serverIds) {
			for (RankType rankType : rankTypes) {
				var key = RankService.getInstance().getKey(serverId, rankType);
				if (!RedisLocalCache.getInstance().exists(key)) {
					// 异步批量设置
					batchSetScoreAsync(serverId, rankType, npcScores)
							.whenComplete((result, throwable) -> {
								if (throwable != null) {
									log.error("NPC批量初始化失败: serverId={}, rankType={}", serverId, rankType, throwable);
								} else {
									log.info("NPC批量初始化成功: serverId={}, rankType={}, count={}", serverId, rankType, npcScores.size());
								}
							});
				}
			}
		}
	}

	public void setNpcToRank(String serverId, RankType rankType) {
		// 准备NPC数据
		Map<Long, Long> npcScores = new HashMap<>();
		List<DaShengNPCConfig> list2 = DaShengNPCManager.instance().list();
		for (DaShengNPCConfig config : list2) {
			npcScores.put((long)config.ID, (long) config.Integral);
		}
		var key = RankService.getInstance().getKey(serverId, rankType);
		// 异步批量设置
		batchSetScoreAsync(serverId, rankType, npcScores)
				.whenComplete((result, throwable) -> {
					if (throwable != null) {
						log.error("NPC批量初始化失败: serverId={}, rankType={}", serverId, rankType, throwable);
					} else {
						log.info("NPC批量初始化成功: serverId={}, rankType={}, count={}", serverId, rankType, npcScores.size());
					}
				});
	}
	public CompletionStage<Void> setNpcToRankTest(String serverId) {
		// 准备NPC数据
		Map<Long, Long> npcScores = new HashMap<>();
		List<DaShengNPCConfig> list2 = DaShengNPCManager.instance().list();
		for (DaShengNPCConfig config : list2) {
			npcScores.put((long)config.ID, (long) config.Integral);
		}
		Map<RankType, Map<Long, Long>> allScore = new HashMap<>();
		allScore.put(RankType.DaShengLeiTaiDay, npcScores);
		allScore.put(RankType.DaShengLeiTaiSeason, npcScores);
		List<CompletionStage<Boolean>> futures = allScore.entrySet().stream()
				.flatMap(entry -> entry.getValue().entrySet().stream()
						.map(innerEntry -> setScoreAsync(serverId, entry.getKey(), innerEntry.getKey(), innerEntry.getValue())))
				.collect(Collectors.toList());
		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

	}
	/**
	 * 批量设置玩家分数
	 * @param serverId 服务器ID
	 * * @param type 排行榜类型
	 * @param playerScores 玩家ID和分数的映射
	 * @return 异步操作结果
	 */
	public CompletionStage<Void> batchSetScoreAsync(String serverId, RankType type, Map<Long, Long> playerScores) {
		List<CompletionStage<Boolean>> futures = playerScores.entrySet().stream()
				.map(entry -> setScoreAsync(serverId, type, entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
				.thenRun(() -> log.debug("批量设置分数完成: serverId={}, type={}, count={}",
						serverId, type, playerScores.size()));
	}
	public void reward(String[] serverIds, int... rankIds) {
		log.info("start rank reward,rankIds[{}]serverIds[{}] server[{}]", rankIds, serverIds, ServerContext.getInstance().getServerId());

		for (int rankId : rankIds) {
			List<RankRewardConfig> rewardList = RankRewardManager.instance().getTypeList(rankId);
			RankConfig rankConfig = RankManager.instance().get(rankId);
			RankType rankType = RankType.get(rankId);
			for (String serverId : serverIds) {
				long start = System.currentTimeMillis();
				AtomicInteger totalQueryCount = new AtomicInteger();
				AtomicInteger totalProcessCount = new AtomicInteger();
				log.info("exec rank reward,rankId[{}] serverId[{}]", rankId, serverId);
				OffsetBatchQuery<RankEntry> batchQuery = (offset, limit) -> {
					// 将offset转换为page，注意offset从0开始，page从1开始
					int page = (offset / limit) + 1;
					List<RankEntry> entrys = RankService.getInstance().getPage(serverId, rankType, page, limit);
					totalQueryCount.addAndGet(entrys.size());
					return entrys;
				};
				StringBuilder rankContent = new StringBuilder();
				StringBuilder scoreContent = new StringBuilder();
				BatchQueryUtil.processBatchAsync(batchQuery, rankEntry -> {
					RankRewardConfig rankStageConfig = BinarySearchUtil.findFirstGreaterThanOrEqual(rewardList, rankEntry.getRank(),
							r -> r.RewardStage);
					if (PlayerHelper.isRobot(rankEntry.getId())) {
						return CompletableFuture.completedFuture(null) ; 
					}
					rankContent.append(rankEntry.getId()).append(",");
					scoreContent.append(rankEntry.getScore()).append(",");
					List<Goods> goods = PlayerHelper.randomReward(rankStageConfig.Reward);
					return MailHelper.sendMail(rankEntry.getId(), rankConfig.RewardMailId, goods, false).onSuccess(v -> {
						totalProcessCount.incrementAndGet();
					}).onFailure(e -> {
						log.error("serverId[{}]rankId[{}] playerId[{}]rank[{}] rank reward mail error", serverId, rankId,
								rankEntry.getId(), rankEntry.getRank(), e);
					}).toCompletionStage().toCompletableFuture();
				}, true).onFailure(e -> {
					log.error("processBatchAsync rank reward error serverId[{}]rankId[{}] exception[{}]", serverId, rankId, e);
				}).onComplete(v -> {
					if(rankContent.length()>0)
					{
						rankContent.deleteCharAt(rankContent.length() - 1);
					}else {
						rankContent.append("null");
					}
					if(scoreContent.length()>0)
					{
						scoreContent.deleteCharAt(scoreContent.length() - 1);;
					}else {
						scoreContent.append("null");
					}
					GameLogger.rank(rankType.ID, rankContent.toString(), scoreContent.toString());
				}).toCompletionStage().toCompletableFuture().join();

				log.info("serverId[{}]rankId[{}]queryCount[{}]processCount[{}] reward completed, use time[{}] ms", serverId, rankId,
						totalQueryCount.get(), totalProcessCount.get(), (System.currentTimeMillis() - start));
			}
		}

	}
	public void serverOpenActivityReward(String[] serverIds, int... rankIds) {
		log.info("start serverOpenActivity rank reward,rankIds[{}]serverIds[{}] server[{}]", rankIds, serverIds, ServerContext.getInstance().getServerId());
		long start = System.currentTimeMillis(); 
		for (int rankId : rankIds) {
			List<RankRewardConfig> rewardList = RankRewardManager.instance().getTypeList(rankId);
			RankConfig rankConfig = RankManager.instance().get(rankId);
			RankType rankType = RankType.get(rankId);
			for (String serverId : serverIds) {
				for (RankRewardConfig rewardConfig : rewardList) {
					List<RankEntry> range = getRange(serverId, rankType, rewardConfig.RewardStageMin, rewardConfig.RewardStage); 
					for (RankEntry rankEntry : range) {
						List<Goods> goods = PlayerHelper.randomReward(rewardConfig.Reward);
						MailHelper.sendMail(rankEntry.getId(), rankConfig.RewardMailId, goods, true); 
					}
					List<Long> collect = range.stream().map(RankEntry::getId).collect(toList()); 
					// 增加总榜积分
					batchUpdateScore(serverId, RankType.TotalServerOpenActivity, collect, rewardConfig.SpecialReward);
				}
				log.info("serverOpenActivity serverId[{}]rankId[{}] reward completed, use time[{}] ms", serverId, rankId,(System.currentTimeMillis() - start));
			}
		}
	}

	private void initRewardTask(int rankId) {

		RankConfig rankConfig = RankManager.instance().get(rankId);
		if (rankConfig.RewardTime != null) {
			SchedulerService.getInstance().scheduleCronTask(() -> {
				reward(rankConfig.ID,false);
			}, rankConfig.RewardTime.getCronExpression());
		}
	}
	
	// ====================== 实时总榜（A+B）增量维护 BEGIN ======================

    /**
     * 根据传入的 RankType 生成 历史总榜(A) 的 key。
     */
    public String getHistKey(String serverId, RankType histType) {
        return getKey(serverId, histType);
    }

    /**
     * 根据传入的 RankType 生成 实时估分榜(B) 的 key（例如战力预估积分榜）。
     */
    public String getRealKey(String serverId, RankType realType) {
        return getKey(serverId, realType);
    }

    /**
     * 根据传入的 RankType 生成 实时总榜(S=A+B) 的 key（对外展示用）。
     */
    public String getSumKey(String serverId, RankType sumType) {
        return getKey(serverId, sumType);
    }

    /**
     * 初始化/重置某玩家的 A、B、S（一次性写入）。
     * 适用于新玩家或全量校准。
     *
     * @param serverId 服务器ID
     * @param playerId 玩家ID
     * @param histType 历史榜 RankType（A）
     * @param realType 实时估分榜 RankType（B）
     * @param sumType  实时总榜 RankType（S）
     * @param a 历史积分A
     * @param b 预估积分B
     */
    public void initOrResetABAndSum(String serverId, long playerId,
                                    RankType histType, RankType realType, RankType sumType,
                                    long a, long b) {
        // 写 A
        setScore(serverId, histType, playerId, a);
        // 写 B
        setScore(serverId, realType, playerId, b);
        // 写 S = A + B
        setScore(serverId, sumType, playerId, a + b);
    }

    /**
     * 当实时估分 B 变化时，增量同步 S（推荐）。
     * 流程：
     * 1) 读取旧B（没有当0）
     * 2) 写入新B
     * 3) delta = newB - oldB
     * 4) 对 S 执行 addScore(delta)
     *
     * @param serverId 服务器ID
     * @param playerId 玩家ID
     * @param realType 实时估分榜 RankType（B）
     * @param sumType  实时总榜 RankType（S）
     * @param newB 新的预估积分
     * @return 最新 S 分数（long）
     */
    public long onRealScoreChangedUpdateSum(String serverId, long playerId,
                                            RankType realType, RankType sumType,
                                            long newB) {
        String realKey = getRealKey(serverId, realType);
        String sumKey  = getSumKey(serverId, sumType);

        RScoredSortedSet<Long> real = getRankSet(realKey);
        RScoredSortedSet<Long> sum  = getRankSet(sumKey);

        Double oldBObj = real.getScore(playerId);
        long oldB = (oldBObj == null) ? 0L : oldBObj.longValue();

        if (oldB != newB) {
            // 1) 更新 B
            real.add((double) newB, playerId);
            // 2) 增量更新 S
            long delta = newB - oldB;
            double newSumScore = sum.addScore(playerId, (double) delta);
            return (long) newSumScore;
        } else {
            Double s = sum.getScore(playerId);
            return s == null ? 0L : s.longValue();
        }
    }

    /**
     * 异步版：当实时估分 B 变化时，增量同步 S。
     */
    public CompletionStage<Long> onRealScoreChangedUpdateSumAsync(String serverId, long playerId,
                                                                  RankType realType, RankType sumType,
                                                                  long newB) {
        String realKey = getRealKey(serverId, realType);
        String sumKey  = getSumKey(serverId, sumType);

        RScoredSortedSet<Long> real = getRankSet(realKey);
        RScoredSortedSet<Long> sum  = getRankSet(sumKey);

        return real.getScoreAsync(playerId).thenCompose(oldBObj -> {
            long oldB = (oldBObj == null) ? 0L : oldBObj.longValue();
            if (oldB == newB) {
                return sum.getScoreAsync(playerId).thenApply(s -> s == null ? 0L : s.longValue());
            }
            // 先写 B，再增量 S
            return real.addAsync((double) newB, playerId)
                       .thenCompose(added -> sum.addScoreAsync(playerId, (double) (newB - oldB)))
                       .thenApply(newSumScore -> newSumScore == null ? 0L : newSumScore.longValue());
        });
    }

    /**
     * 当历史积分 A 变化时，增量同步 S。
     * 用于赛季结算、补发历史积分等场景。
     *
     * @param serverId 服务器ID
     * @param playerId 玩家ID
     * @param histType 历史榜 RankType（A）
     * @param sumType  实时总榜 RankType（S）
     * @param newA 新的历史积分
     * @return 最新 S 分数
     */
    public long onHistScoreChangedUpdateSum(String serverId, long playerId,
                                            RankType histType, RankType sumType,
                                            long newA) {
        String histKey = getHistKey(serverId, histType);
        String sumKey  = getSumKey(serverId, sumType);

        RScoredSortedSet<Long> hist = getRankSet(histKey);
        RScoredSortedSet<Long> sum  = getRankSet(sumKey);

        Double oldAObj = hist.getScore(playerId);
        long oldA = (oldAObj == null) ? 0L : oldAObj.longValue();

        if (oldA != newA) {
            hist.add((double) newA, playerId);
            long delta = newA - oldA;
            double newSumScore = sum.addScore(playerId, (double) delta);
            return (long) newSumScore;
        } else {
            Double s = sum.getScore(playerId);
            return s == null ? 0L : s.longValue();
        }
    }

    /**
     * 批量更新一批玩家的 B（预估分）并增量同步 S，使用 Redisson RBatch（pipeline）。
     *
     * @param serverId 服务器ID
     * @param realType 实时估分榜 RankType（B）
     * @param sumType  实时总榜 RankType（S）
     * @param newBMap  playerId -> newB
     */
    public void batchUpdateRealScoreAndSum(String serverId,
                                           RankType realType, RankType sumType,
                                           Map<Long, Long> newBMap) {
        if (newBMap == null || newBMap.isEmpty()) {
            return;
        }
        String realKey = getRealKey(serverId, realType);
        String sumKey  = getSumKey(serverId, sumType);

        // 先读旧B（简单实现：逐个get；若QPS更高可做缓存或分片优化）
        Map<Long, Long> oldBMap = new HashMap<>(newBMap.size());
        RScoredSortedSet<Long> real = getRankSet(realKey);
        for (Map.Entry<Long, Long> e : newBMap.entrySet()) {
            Double b = real.getScore(e.getKey());
            oldBMap.put(e.getKey(), b == null ? 0L : b.longValue());
        }

        // 批处理：写B与增量S
        RBatch batch = RedisUtil.getRedis().createBatch();
        RScoredSortedSetAsync<Long> realAsync = batch.getScoredSortedSet(realKey, LongCodec.INSTANCE);
        RScoredSortedSetAsync<Long> sumAsync  = batch.getScoredSortedSet(sumKey,  LongCodec.INSTANCE);

        for (Map.Entry<Long, Long> e : newBMap.entrySet()) {
            long pid = e.getKey();
            long newB = e.getValue();
            long oldB = oldBMap.getOrDefault(pid, 0L);
            if (oldB == newB) continue;

            realAsync.addAsync((double) newB, pid);
            long delta = newB - oldB;
            sumAsync.addScoreAsync(pid, (double) delta);
        }
        batch.execute();
    }

    /**
     * 读取：获取实时总榜 S 的前N名（传入 sumType）
     */
    public List<RankEntry> getRealTimeTotalTopN(String serverId, RankType sumType, int n) {
        return getTopN(serverId, sumType, n);
    }

    /**
     * 读取：获取某玩家在实时总榜 S 的名次与分数（传入 sumType）
     */
    public RankEntry getRealTimeTotalEntry(String serverId, RankType sumType, long playerId) {
        return getRankEntry(serverId, sumType, playerId);
    }

    /**
     * 读取：获取实时估分榜 B 的前N名（传入 realType）
     */
    public List<RankEntry> getRealTimeEstimateTopN(String serverId, RankType realType, int n) {
        return getTopN(serverId, realType, n);
    }

    // ====================== 实时总榜（A+B）增量维护 END ======================
	
	
}