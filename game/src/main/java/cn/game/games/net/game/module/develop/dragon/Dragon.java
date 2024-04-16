package cn.game.games.net.game.module.develop.dragon;

import java.io.Serializable;

import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.ItemNoStack;
import cn.game.protocol.protobuf.BaseMsg.DragonInfo;

public class Dragon extends ItemNoStack implements Serializable, DbEntity {

	/**
	 * 等级
	 * @mbg.generated
	 */
	private int level;
	/**
	 * 星级
	 * @mbg.generated
	 */
	private int star;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getStar() {
		return star;
	}

	public void setStar(int star) {
		this.star = star;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}

	public DragonInfo toDragonInfo() {
		DragonInfo.Builder builder = DragonInfo.newBuilder();
//		builder.setUid(id.toString());
		builder.setId(configId);
		builder.setStar(star);

		return builder.build();
	}
}