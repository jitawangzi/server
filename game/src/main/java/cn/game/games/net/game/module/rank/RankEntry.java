package cn.game.games.net.game.module.rank;

/**    
 * 排行榜条目类
 * 2024年9月7日 上午11:07:37
 * @author SYQ
 */
public class RankEntry {

	private final int rank;
	private final long playerId;
	private final long score;


	public RankEntry(int rank, long playerId, long score) {
		this.rank = rank;
		this.playerId = playerId;
		this.score = score;
	}

	public long getPlayerId() {
		return playerId;
	}

	public long getScore() {
		return score;
	}

	public int getRank() {
		return rank;
	}

	@Override
	public String toString() {
		return "RankEntry [rank=" + rank + ", playerId=" + playerId + ", score=" + score + "]";
	}
}
