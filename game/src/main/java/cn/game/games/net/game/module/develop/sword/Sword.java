package cn.game.games.net.game.module.develop.sword;

import java.io.Serializable;

import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.ItemNoStack;
import cn.game.protocol.protobuf.BaseMsg.HeroSwordInfo;

public class Sword extends ItemNoStack implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}

	public HeroSwordInfo toHeroSwordInfo() {
		return HeroSwordInfo.newBuilder().setUid(getId().toString()).setConfigId(getConfigId()).setStar(getStar()).build();
	}
}