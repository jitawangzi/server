package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class OfflineResourceAdd implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Integer id;
	/**
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 待增加的资源id
	 * @mbg.generated
	 */
	private Integer itemId;
	/**
	 * 数量
	 * @mbg.generated
	 */
	private Integer count;
	/**
	 * 操作类型：增加 1，扣除 0
	 * @mbg.generated
	 */
	private Boolean type;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @mbg.generated
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @mbg.generated
	 */
	public Long getPlayerId() {
		return playerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getItemId() {
		return itemId;
	}

	/**
	 * @mbg.generated
	 */
	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getCount() {
		return count;
	}

	/**
	 * @mbg.generated
	 */
	public void setCount(Integer count) {
		this.count = count;
	}

	/**
	 * @mbg.generated
	 */
	public Boolean getType() {
		return type;
	}

	/**
	 * @mbg.generated
	 */
	public void setType(Boolean type) {
		this.type = type;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.OfflineResourceAddMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}
}