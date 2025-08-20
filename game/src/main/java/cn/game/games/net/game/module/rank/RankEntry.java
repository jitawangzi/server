package cn.game.games.net.game.module.rank;

/**    
 * 排行榜条目类
 * 2024年9月7日 上午11:07:37
 * @author SYQ
 */
public class RankEntry {

	private final int rank;
	/** 参与排行的对象id，例如玩家id，工会id等 */
	private final long id;
	private final long score;


	public RankEntry(int rank, long id, long score) {
		this.rank = rank;
		this.id = id;
		this.score = score;
	}

	public long getId() {
		return id;
	}

	public long getScore() {
		return score;
	}

	public int getRank() {
		return rank;
	}

	@Override
	public String toString() {
		return "RankEntry [rank=" + rank + ", id=" + id + ", score=" + score + "]";
	}
}
