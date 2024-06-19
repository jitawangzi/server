package cn.game.games.cache.entity;

import java.io.Serializable;

import cn.game.games.cache.base.DbEntity;
import cn.game.protocol.protobuf.BaseMsg.HCHeroInfo;

public class HCHero extends ItemNoStack implements Serializable, DbEntity {


	private static final long serialVersionUID = 8767123478983476636L;

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}


	public HCHeroInfo toHCHeroInfo() {
		HCHeroInfo.Builder builder = HCHeroInfo.newBuilder();
		builder.setUid(id + "");
		builder.setConfigId(configId);
		builder.setStar(star);
		builder.setLevel(level);
		return builder.build();
	}

	public HCHeroInfo toHCHeroLevelInfo() {
		HCHeroInfo.Builder builder = HCHeroInfo.newBuilder();
		builder.setUid(id + "");
		builder.setLevel(level);
		return builder.build();
	}
}