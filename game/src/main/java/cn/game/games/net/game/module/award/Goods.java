package cn.game.games.net.game.module.award;

import java.util.ArrayList;
import java.util.List;

import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.protobuf.BaseMsg.GoodsInfo;

/**
 * 物品id和数量的封装
 * 2020年10月15日 上午10:51:01
 * @author SYQ
 */
public class Goods {

	/** 配置表id ，物品id，可能是({@link Asset#ID}) 或者是({@link ItemConfig#ID}) ({@link HeroConfig#ID})等等的id*/
	private int id;
	/** 数量 */
	private int count;

	public Goods(int id, int count) {
		this.id = id;
		this.count = count;
	}

	public static Goods valueOf(int[] goods) {
		return new Goods(goods[0], goods[1]);
	}
	public static List<Goods> valueOf(int[][] drops) {
		List<Goods> result = new ArrayList<>();
		for(int i = 0; i < drops.length; i++) {
			result.add(new Goods(drops[0][0], drops[0][1]));
		}
		return result;
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

	public GoodsInfo toGoodsInfo() {
		return GoodsInfo.newBuilder().setId(id).setCount(count).build();
	}

	@Override
	public String toString() {
		return "Goods [id=" + id + ", count=" + count + "]";
	}

}
