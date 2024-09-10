package cn.game.games.net.game.module.develop.secretscript;

import java.io.Serializable;

import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.Item;
import cn.game.protocol.protobuf.BaseMsg.SecretscriptInfo;

public class Secretscript extends Item implements Serializable, DbEntity {

	private static final long serialVersionUID = 1L;
	private int level = 1;
	private int star = 1;

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}


	public SecretscriptInfo toProtoInfo() {
		SecretscriptInfo.Builder builder = SecretscriptInfo.newBuilder();
		builder.setLevel(level);
		builder.setId(configId);
		builder.setStar(star);
		return builder.build();
	}

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

}