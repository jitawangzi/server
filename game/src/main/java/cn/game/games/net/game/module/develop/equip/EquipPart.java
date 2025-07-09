package cn.game.games.net.game.module.develop.equip;

import java.util.HashMap;
import java.util.Map;

import cn.game.protocol.protobuf.BaseMsg.EquipPartInfo;

public class EquipPart {

	private int pos; // 部位类型，1-6
	private int strength = 1; // 强化等级
	/** 这个装备部位上安装的装备唯一id  */
	private long equipUid;
	/** 这个装备部位上安装的宝石唯一id -> 位置 0 - 5 */
	private Map<Long, Integer> gemPosMap = new HashMap<>(); // 属性列表

	public EquipPart(int pos) {
		this.pos = pos;
	}

	public EquipPart() {
	}

	public int getStrength() {
		return strength;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}


	public Map<Long, Integer> getGemPosMap() {
		return gemPosMap;
	}

	public void setGemPosMap(Map<Long, Integer> gemPosMap) {
		this.gemPosMap = gemPosMap;
	}

	public long getEquipUid() {
		return equipUid;
	}

	public void setEquipUid(long equipUid) {
		this.equipUid = equipUid;
	}

	public EquipPartInfo toEquipPartInfo() {
		EquipPartInfo.Builder builder = EquipPartInfo.newBuilder();
		builder.setType(pos);
		builder.setStrength(strength);
		builder.setEquipUid(equipUid + "");
		for (Map.Entry<Long, Integer> entry : gemPosMap.entrySet()) {
			builder.putGemMap(entry.getKey().toString(), entry.getValue());
		}
		return builder.build();
	}

}
