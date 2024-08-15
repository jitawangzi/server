package cn.game.games.net.game.module.develop.fairyfriend;

import cn.game.games.cache.entity.Item;
import cn.game.protocol.protobuf.FairyFriendMsg.FairyFriendInfo;

public class FairyFriend extends Item {
	private int exp;
	private int level = 1;

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public FairyFriendInfo toProto() {
		FairyFriendInfo.Builder builder = FairyFriendInfo.newBuilder();
		builder.setId(getConfigId());
		builder.setExp(exp);
		builder.setLevel(level);
		return builder.build();
	}

}
