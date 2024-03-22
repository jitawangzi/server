package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class RoleAction implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 角色id
	 * @mbg.generated
	 */
	private Integer roleId;
	/**
	 * 行为类型
	 * @mbg.generated
	 */
	private Integer type;
	/**
	 * 子类型
	 * @mbg.generated
	 */
	private Integer subType;
	/**
	 * 计数
	 * @mbg.generated
	 */
	private Integer count;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

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
	public Integer getRoleId() {
		return roleId;
	}

	/**
	 * @mbg.generated
	 */
	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * @mbg.generated
	 */
	public void setType(Integer type) {
		this.type = type;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getSubType() {
		return subType;
	}

	/**
	 * @mbg.generated
	 */
	public void setSubType(Integer subType) {
		this.subType = subType;
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
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.RoleActionMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, roleId, type, subType };
	}
}