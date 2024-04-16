package cn.game.games.cache.entity;

import java.io.Serializable;

import cn.game.games.cache.base.DbEntity;

/**
 * t_equip
 * @author
 */
public class Equip extends ItemNoStack implements Serializable, DbEntity {

	/**
	 * 不同类型的东西，关联的其他功能的id
	 * @mbg.generated
	 */
	private Integer relatedId;
	/**
	 * 一个int型扩展字段
	 * @mbg.generated
	 */
	private Integer extId;
	/**
	 * 扩展参数
	 * @mbg.generated
	 */
	private String extParam;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public Integer getRelatedId() {
		return relatedId;
	}

	/**
	 * @mbg.generated
	 */
	public void setRelatedId(Integer relatedId) {
		this.relatedId = relatedId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getExtId() {
		return extId;
	}

	/**
	 * @mbg.generated
	 */
	public void setExtId(Integer extId) {
		this.extId = extId;
	}

	/**
	 * @mbg.generated
	 */
	public String getExtParam() {
		return extParam;
	}

	/**
	 * @mbg.generated
	 */
	public void setExtParam(String extParam) {
		this.extParam = extParam;
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

	
}