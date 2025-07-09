package cn.game.games.cache.entity;

import java.util.HashMap;
import java.util.Map;

import cn.game.protocol.protobuf.BaseMsg.EquipInfo;

/**
 * t_equip
 * @author
 */
public class Equip extends ItemNoStack {

	private Map<Integer, Integer> equipAttrs = new HashMap<Integer, Integer>();

	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;


	public Map<Integer, Integer> getEquipAttrs() {
		return equipAttrs;
	}

	public void setEquipAttrs(Map<Integer, Integer> equipAttrs) {
		this.equipAttrs = equipAttrs;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.EquipMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}

	public EquipInfo toEquipInfo() {
		return EquipInfo.newBuilder().setConfigId(this.configId).
				setUid(id + "").putAllAttrs(equipAttrs).build();
	}
	
}