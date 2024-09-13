package cn.game.games.net.game.module.rank;

import java.util.List;
import java.util.concurrent.CompletionStage;

import cn.game.protocol.generated.enume.RankType;
import cn.game.util.LuaScriptUtil;
import cn.game.util.RedisUtil;

public class RankExample {
	public static void main(String[] args) throws Exception {

		RedisUtil.getInstance().init();
		RankService rankService = RankService.getInstance();
		String serverId = "server4";

		// 同步调用
//		rankService.setScore(serverId, RankType.DaDaoZhengFengDay, 240200711, 50);
//		rankService.setScore(serverId, RankType.DaDaoZhengFengDay, 240200712, 75);
//		rankService.setScore(serverId, RankType.DaDaoZhengFengDay, 240200713, 90);
//		rankService.setScore(serverId, RankType.DaDaoZhengFengDay, 240200714, 20);
//		rankService.setScore(serverId, RankType.Level, 240200711, 60);
//		rankService.setScore(serverId, RankType.Level, 240200712, 160);
//		rankService.setScore(serverId, RankType.Level, 240200713, 260);

//		rankService.getRankAsync("server4", RankType.Level, 240200731);
		
		CompletionStage<Double> updateScoreIfGreater = LuaScriptUtil.updateScoreIfGreater("SET_RANK_server4_Level", 240200713, 550);
		Double join = updateScoreIfGreater.toCompletableFuture().join();
		System.out.println(join);
		// 异步调用
		CompletionStage<Boolean> updateFuture = rankService.setScoreAsync(serverId, RankType.Battle, 240200711, 1000);
		updateFuture.thenAccept(result -> System.out.println("Async update result: " + result));

		CompletionStage<Double> updateScoreAsync = rankService.updateScoreAsync("server4", RankType.Level, 240200711, 2222);
		updateScoreAsync.thenAccept(result -> System.out.println("Async update resul : " + result));
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
		List<RankEntry> entrys = rankService.getPage(serverId, RankType.Battle, page, pageSize);
		System.out.println("ranks in page :" + page + " pageSize:" + pageSize);
		entrys.forEach(System.out::println);

		Thread.currentThread().join();
	}
}