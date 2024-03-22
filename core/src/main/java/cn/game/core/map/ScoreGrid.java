package cn.game.core.map;


/**    
 * 带分数的格子
 * @date 2021年12月30日 下午5:20:22
 * @author SYQ
 */
public class ScoreGrid extends Grid implements Comparable<ScoreGrid> {

	public int score;

	public ScoreGrid() {
	}
	public ScoreGrid(int x, int y, int score) {
		super(x, y);
		this.score = score;
	}

	@Override
	public int compareTo(ScoreGrid o) {
		return o.score - this.score;
	}
	@Override
	public String toString() {
		return this.x + " " + this.y + " : " + score;
	}
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}

