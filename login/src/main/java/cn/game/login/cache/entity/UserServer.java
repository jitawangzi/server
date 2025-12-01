package cn.game.login.cache.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 玩家服务器角色映射表
 * @mbg.generated
 */
public class UserServer implements Serializable {

	/**
	 * 分布式唯一ID(如雪花算法生成)
	 * @mbg.generated
	 */
	private Long id;
	/**
	 * 账号唯一id
	 * @mbg.generated
	 */
	private Long userId;
	/**
	 * 服务器id
	 * @mbg.generated
	 */
	private String serverId;
	/**
	 * 该服务器内的玩家id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 玩家名
	 * @mbg.generated
	 */
	private String playerName;
	/**
	 * 玩家等级
	 * @mbg.generated
	 */
	private Integer playerLevel;
	/**
	 * 最后更新时间
	 * @mbg.generated
	 */
	private Date updatedAt;
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
	public Long getUserId() {
		return userId;
	}

	/**
	 * @mbg.generated
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * @mbg.generated
	 */
	public String getServerId() {
		return serverId;
	}

	/**
	 * @mbg.generated
	 */
	public void setServerId(String serverId) {
		this.serverId = serverId;
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
	public String getPlayerName() {
		return playerName;
	}

	/**
	 * @mbg.generated
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getPlayerLevel() {
		return playerLevel;
	}

	/**
	 * @mbg.generated
	 */
	public void setPlayerLevel(Integer playerLevel) {
		this.playerLevel = playerLevel;
	}

	/**
	 * @mbg.generated
	 */
	public Date getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * @mbg.generated
	 */
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
}