package cn.game.games.net.game.module.develop.defenceline;

import java.io.Serializable;

import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.ItemNoStack;
import cn.game.protocol.protobuf.BaseMsg.DefenceSkinInfo;

public class DefenceSkin extends ItemNoStack implements Serializable, DbEntity {

	private static final long serialVersionUID = 1L;

	public DefenceSkinInfo toProto() {
		DefenceSkinInfo.Builder builder = DefenceSkinInfo.newBuilder();
		builder.setUid(id+"");
		builder.setConfigId(configId);
		builder.setStar(star);
		return builder.build();
	}
}