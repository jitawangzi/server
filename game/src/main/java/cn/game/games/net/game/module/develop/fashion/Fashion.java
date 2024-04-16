package cn.game.games.net.game.module.develop.fashion;

import java.io.Serializable;

import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.ItemNoStack;
import cn.game.protocol.protobuf.BaseMsg.HeroFashionInfo;

public class Fashion extends ItemNoStack implements Serializable, DbEntity {

	private static final long serialVersionUID = -7805217016493863921L;

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}

	public HeroFashionInfo toFashionInfo() {
		return HeroFashionInfo.newBuilder().setUid(getId().toString()).setConfigId(getConfigId()).setStar(getStar()).build();
	}
}