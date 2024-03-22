package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class RoleActionBattle implements Serializable, DbEntity {
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
	 * 敌人类别
	 * @mbg.generated
	 */
	private Integer roleType;
	/**
	 * 敌人状态
	 * @mbg.generated
	 */
	private Integer roleState;
	/**
	 * 敌人血量
	 * @mbg.generated
	 */
	private Integer roleHp;
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
	public Integer getRoleType() {
		return roleType;
	}

	/**
	 * @mbg.generated
	 */
	public void setRoleType(Integer roleType) {
		this.roleType = roleType;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getRoleState() {
		return roleState;
	}

	/**
	 * @mbg.generated
	 */
	public void setRoleState(Integer roleState) {
		this.roleState = roleState;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getRoleHp() {
		return roleHp;
	}

	/**
	 * @mbg.generated
	 */
	public void setRoleHp(Integer roleHp) {
		this.roleHp = roleHp;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.RoleActionBattleMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, roleId, type, subType };
	}

	public RoleActionBattle() {
	}

//	public RoleActionBattle(long playerId, int roleId, int type, int subType, int... args) {
//		super(playerId, roleId, type, subType, args);
//		this.roleType = args[0];
//		this.roleState = args[1];
//		this.roleHp = args[2];
//	}
}