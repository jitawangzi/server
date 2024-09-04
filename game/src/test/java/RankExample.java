import java.util.Collection;
import java.util.List;

import org.redisson.api.RFuture;

public class RankExample {
	public static void main(String[] args) throws Exception {
		RankService rankService = new RankService();
		String serverId = "server1";

		// 同步调用
		rankService.updateScore(serverId, RankType.LEVEL, 1001, 50);
		rankService.updateScore(serverId, RankType.LEVEL, 1002, 75);
		rankService.updateScore(serverId, RankType.LEVEL, 1003, 60);

		// 异步调用
		RFuture<Boolean> updateFuture = rankService.updateScoreAsync(serverId, RankType.ACTIVITY, 1001, 1000);
		updateFuture.thenAccept(result -> System.out.println("Async update result: " + result));

		// 获取前3名玩家（同步）
		System.out.println("Top 3 players in level rank:");
		List<RankService.RankEntry> topPlayers = rankService.getTopN(serverId, RankType.LEVEL, 3);
		topPlayers.forEach(System.out::println);

		// 获取前3名玩家（异步）
		RFuture<Collection<Long>> topPlayersFuture = rankService.getTopNAsync(serverId, RankType.LEVEL, 3);
		topPlayersFuture.thenAccept(players -> {
			System.out.println("Async top 3 players in level rank:");
			players.forEach(System.out::println);
		});

		// 查询玩家排名（同步）
		long playerId = 1002;
		int rank = rankService.getRank(serverId, RankType.LEVEL, playerId);
		System.out.println("Rank of player " + playerId + " in level rank: " + rank);

		// 查询玩家排名（异步）
		RFuture<Integer> rankFuture = rankService.getRankAsync(serverId, RankType.LEVEL, playerId);
		rankFuture.thenAccept(asyncRank -> System.out.println("Async rank of player " + playerId + " in level rank: " + asyncRank));

		// 等待所有异步操作完成
		updateFuture.join();
		topPlayersFuture.join();
		rankFuture.join();
	}
}