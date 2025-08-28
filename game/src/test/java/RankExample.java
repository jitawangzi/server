
import java.util.List;
import java.util.concurrent.CompletionStage;

import cn.game.games.net.game.module.rank.RankEntry;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.enume.RankType;

public class RankExample {
	public static void main(String[] args) throws Exception {

		RankService rankService = RankService.getInstance();
		String serverId = "server1";

		
		RankService.getInstance().setScoreAsync("server1", RankType.LingShanWenChan, 240200009, 5);
		RankService.getInstance().setScoreAsync("server1", RankType.LingShanWenChan, 240200010, 5);
		
		
		// 同步调用
		rankService.setScore(serverId, RankType.Battle, 1001, 50);
		rankService.setScore(serverId, RankType.Battle, 1002, 75);
		rankService.setScore(serverId, RankType.Battle, 1003, 90);
		rankService.setScore(serverId, RankType.Battle, 1004, 20);
		rankService.setScore(serverId, RankType.Level, 1002, 60);
		rankService.setScore(serverId, RankType.Level, 1003, 160);
		rankService.setScore(serverId, RankType.Level, 1004, 260);

		// 异步调用
		CompletionStage<Boolean> updateFuture = rankService.setScoreAsync(serverId, RankType.Battle, 1001, 1000);
		updateFuture.thenAccept(result -> System.out.println("Async update result: " + result));

		// 获取前3名玩家（同步）
		System.out.println("Top 3 players in level rank:");
		List<RankEntry> topPlayers = rankService.getTopN(serverId, RankType.Level, 3);
		topPlayers.forEach(System.out::println);

		// 获取前3名玩家（异步）
		CompletionStage<List<RankEntry>> topPlayersFuture = rankService.getTopNAsync(serverId, RankType.Level, 3);
		topPlayersFuture.thenAccept(players -> {
			System.out.println("Async top 3 players in level rank:");
			players.forEach(System.out::println);
		});

		// 查询玩家排名（同步）
		long playerId = 1002;
		int rank = rankService.getRank(serverId, RankType.Battle, playerId);
		System.out.println("Rank of player " + playerId + " in Battle rank: " + rank);

		// 查询玩家排名（异步）
		CompletionStage<Integer> rankFuture = rankService.getRankAsync(serverId, RankType.Battle, playerId);
		rankFuture.thenAccept(asyncRank -> System.out.println("Async rank of player " + playerId + " in Battle rank: " + asyncRank));

		// 查询某页排名
		int page = 1;
		int pageSize = 5;
		List<RankEntry> entrys = rankService.getPage(serverId, RankType.Battle.name(), page, pageSize);
		System.out.println("ranks in page :" + page + " pageSize:" + pageSize);
		entrys.forEach(System.out::println);

		Thread.currentThread().join();
	}
}