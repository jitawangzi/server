package cn.game.games.net.game.module.develop.pet;

import java.io.Serializable;

import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.Item;
import cn.game.protocol.protobuf.BaseMsg.HeroInfo;

public class Pet extends Item implements Serializable, DbEntity {

	private static final long serialVersionUID = 1L;
	private int level;

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}


	public HeroInfo toPetInfo() {
		HeroInfo.Builder builder = HeroInfo.newBuilder();


		return builder.build();
	}

}