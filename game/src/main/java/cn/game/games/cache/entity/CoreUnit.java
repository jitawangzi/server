package cn.game.games.cache.entity;

import java.io.Serializable;

public class CoreUnit implements Serializable {

	/**
	 * @mbg.generated
	 */
	private Long id;
	/**
	 * 玩家id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 角色id
	 * @mbg.generated
	 */
	private Integer roleId;
	/**
	 * 模板id
	 * @mbg.generated
	 */
	private Integer dictId;
	/**
	 * 位置
	 * @mbg.generated
	 */
	private Byte pos;
	/**
	 * 突破等级
	 * @mbg.generated
	 */
	private Byte breaklevel;
	/**
	 * 强化等级
	 * @mbg.generated
	 */
	private Byte strengthlevel;
	/**
	 * 是否锁定
	 * @mbg.generated
	 */
	private Byte checklock;
	/**
	 * 获取时间
	 * @mbg.generated
	 */
	private Long getTime;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @mbg.generated
	 */
	public void setId(Long id) {
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
	public Integer getDictId() {
		return dictId;
	}

	/**
	 * @mbg.generated
	 */
	public void setDictId(Integer dictId) {
		this.dictId = dictId;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getPos() {
		return pos;
	}

	/**
	 * @mbg.generated
	 */
	public void setPos(Byte pos) {
		this.pos = pos;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getBreaklevel() {
		return breaklevel;
	}

	/**
	 * @mbg.generated
	 */
	public void setBreaklevel(Byte breaklevel) {
		this.breaklevel = breaklevel;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getStrengthlevel() {
		return strengthlevel;
	}

	/**
	 * @mbg.generated
	 */
	public void setStrengthlevel(Byte strengthlevel) {
		this.strengthlevel = strengthlevel;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getChecklock() {
		return checklock;
	}

	/**
	 * @mbg.generated
	 */
	public void setChecklock(Byte checklock) {
		this.checklock = checklock;
	}

	/**
	 * @mbg.generated
	 */
	public Long getGetTime() {
		return getTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setGetTime(Long getTime) {
		this.getTime = getTime;
	}
}