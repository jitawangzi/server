package cn.game.games.net.game.module.award;


/**
 * @Description 物品id和数量的封装
 * @date 2020年10月15日 上午10:51:01
 * @author SYQ
 */
public class Goods {

	/** 配置表id */
	private int id;
	/** 数量 */
	private int count;

	public Goods(int id, int count) {
		this.id = id;
		this.count = count;
	}

	public Goods() {
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}


	@Override
	public String toString() {
		return "Goods [id=" + id + ", count=" + count + "]";
	}

}
