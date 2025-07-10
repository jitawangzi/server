package cn.game.games.cache.entity;

import java.io.Serializable;

import cn.game.games.cache.base.DbEntity;
import cn.game.protocol.protobuf.BaseMsg.EquipInfo;

/**
 * t_equip
 * @author
 */
public class Equip extends ItemNoStack implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private long id;
	/**
	 * @mbg.generated
	 */
	private long playerId;
	/**
	 * @mbg.generated
	 */
	private int configId;
	/**
	 * @mbg.generated
	 */
	private java.util.HashMap<Integer, Integer> equipAttrs = new java.util.HashMap<Integer, Integer>();
	/**
	 * @mbg.generated
	 */
	private String ext;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public long getId() {
		return id;
	}

	/**
	 * @mbg.generated
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @mbg.generated
	 */
	public long getPlayerId() {
		return playerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	/**
	 * @mbg.generated
	 */
	public int getConfigId() {
		return configId;
	}

	/**
	 * @mbg.generated
	 */
	public void setConfigId(int configId) {
		this.configId = configId;
	}

	/**
	 * @mbg.generated
	 */
	public java.util.HashMap<Integer, Integer> getEquipAttrs() {
		return equipAttrs;
	}

	/**
	 * @mbg.generated
	 */
	public void setEquipAttrs(java.util.HashMap<Integer, Integer> equipAttrs) {
		this.equipAttrs = equipAttrs;
	}

	/**
	 * @mbg.generated
	 */
	public String getExt() {
		if (ext == null || ext.isEmpty()) {
			beforeSave();
		}
		return ext;
	}

	/**
	 * @mbg.generated
	 */
	public void setExt(String ext) {
		this.equipAttrs = com.alibaba.fastjson.JSON.parseObject(ext,
				new com.alibaba.fastjson.TypeReference<java.util.HashMap<Integer, Integer>>() {
				});
		if (this.equipAttrs == null) {
			this.equipAttrs = new java.util.HashMap<Integer, Integer>();
		}
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public void beforeSave() {
		this.ext = com.alibaba.fastjson.JSON.toJSONString(this.equipAttrs,
				com.alibaba.fastjson.serializer.SerializerFeature.WriteNonStringKeyAsString);
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