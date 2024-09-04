import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import org.redisson.Redisson;
import org.redisson.api.RFuture;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RankService {

	private final RedissonClient redissonClient;

	public RankService() {
		Config config = new Config();
		config.useSingleServer().setAddress("redis://127.0.0.1:6379");
		this.redissonClient = Redisson.create(config);
	}

	private String getKey(String serverId, RankType type) {
		return serverId + ":" + type.getKey();
	}

	// 同步方法
	public void updateScore(String serverId, RankType type, long playerId, double score) {
		RScoredSortedSet<Long> rank = redissonClient.getScoredSortedSet(getKey(serverId, type));
		rank.add(score, playerId);
	}

	// 异步方法
	public RFuture<Boolean> updateScoreAsync(String serverId, RankType type, long playerId, double score) {
		RScoredSortedSet<Long> rank = redissonClient.getScoredSortedSet(getKey(serverId, type));
		return rank.addAsync(score, playerId);
	}

	// 同步方法
	public List<RankEntry> getTopN(String serverId, RankType type, int n) {
		RScoredSortedSet<Long> rank = redissonClient.getScoredSortedSet(getKey(serverId, type));
		Collection<Long> topPlayers = rank.valueRange(0, n - 1);
		return convertToRankEntries(serverId, type, topPlayers);
	}

	// 异步方法
	public RFuture<Collection<Long>> getTopNAsync(String serverId, RankType type, int n) {
		RScoredSortedSet<Long> rank = redissonClient.getScoredSortedSet(getKey(serverId, type));
		return rank.valueRangeAsync(0, n - 1);
	}

	// 同步方法
	public List<RankEntry> getPage(String serverId, RankType type, int page, int pageSize) {
		RScoredSortedSet<Long> rank = redissonClient.getScoredSortedSet(getKey(serverId, type));
		int start = (page - 1) * pageSize;
		int end = start + pageSize - 1;
		Collection<Long> players = rank.valueRange(start, end);
		return convertToRankEntries(serverId, type, players);
	}

	// 异步方法
	public RFuture<Collection<Long>> getPageAsync(String serverId, RankType type, int page, int pageSize) {
		RScoredSortedSet<Long> rank = redissonClient.getScoredSortedSet(getKey(serverId, type));
		int start = (page - 1) * pageSize;
		int end = start + pageSize - 1;
		return rank.valueRangeAsync(start, end);
	}

	// 同步方法
	public int getRank(String serverId, RankType type, long playerId) {
		RScoredSortedSet<Long> rank = redissonClient.getScoredSortedSet(getKey(serverId, type));
		Integer playerRank = rank.revRank(playerId);
		return playerRank != null ? playerRank + 1 : -1;
	}

	// 异步方法
	public RFuture<Integer> getRankAsync(String serverId, RankType type, long playerId) {
		RScoredSortedSet<Long> rank = redissonClient.getScoredSortedSet(getKey(serverId, type));
		return rank.revRankAsync(playerId);
	}

	// 同步方法
	public void removePlayer(String serverId, RankType type, long playerId) {
		RScoredSortedSet<Long> rank = redissonClient.getScoredSortedSet(getKey(serverId, type));
		rank.remove(playerId);
	}

	// 异步方法
	public RFuture<Boolean> removePlayerAsync(String serverId, RankType type, long playerId) {
		RScoredSortedSet<Long> rank = redissonClient.getScoredSortedSet(getKey(serverId, type));
		return rank.removeAsync(playerId);
	}

	public CompletionStage<List<RankEntry>> getTopNEntriesAsync(String serverId, RankType type, int n) {
		RScoredSortedSet<Long> rank = redissonClient.getScoredSortedSet(getKey(serverId, type));
		return rank.valueRangeAsync(0, n - 1).thenApply(playerIds -> convertToRankEntries(serverId, type, playerIds));
	}

	public CompletionStage<List<RankEntry>> getPageEntriesAsync(String serverId, RankType type, int page, int pageSize) {
		RScoredSortedSet<Long> rank = redissonClient.getScoredSortedSet(getKey(serverId, type));
		int start = (page - 1) * pageSize;
		int end = start + pageSize - 1;
		return rank.valueRangeAsync(start, end).thenApply(playerIds -> convertToRankEntries(serverId, type, playerIds));
	}

	public CompletionStage<Integer> getPlayerRankAsync(String serverId, RankType type, long playerId) {
		RScoredSortedSet<Long> rank = redissonClient.getScoredSortedSet(getKey(serverId, type));
		return rank.revRankAsync(playerId).thenApply(r -> r != null ? r + 1 : -1);
	}

	public List<RankEntry> convertToRankEntries(String serverId, RankType type, Collection<Long> playerIds) {
		if (playerIds.isEmpty()) {
			return Collections.emptyList();
		}

		RScoredSortedSet<Long> rank = redissonClient.getScoredSortedSet(getKey(serverId, type));
		return playerIds.stream().map(playerId -> new RankEntry(playerId, rank.getScore(playerId).doubleValue())).collect(Collectors.toList());
	}

	public static class RankEntry {
		private final long playerId;
		private final double score;

		public RankEntry(long playerId, double score) {
			this.playerId = playerId;
			this.score = score;
		}

		public long getPlayerId() {
			return playerId;
		}

		public double getScore() {
			return score;
		}

		@Override
		public String toString() {
			return "RankEntry{" + "playerId=" + playerId + ", score=" + score + '}';
		}
	}
}