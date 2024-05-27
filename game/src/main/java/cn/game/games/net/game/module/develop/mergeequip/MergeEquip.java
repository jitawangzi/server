package cn.game.games.net.game.module.develop.mergeequip;

import cn.game.games.cache.entity.Item;
import cn.game.protocol.protobuf.BaseMsg.MergeEquipmentInfo;

public class MergeEquip extends Item {
	private int level;

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public MergeEquip() {
	}

	public MergeEquip(int id, long count) {
		super();
		setConfigId(id);
		setCount(count);
	}

	public MergeEquipmentInfo toMergeEquipProto() {
		return MergeEquipmentInfo.newBuilder().setId(this.configId).setLevel(level).build();
	}

}
