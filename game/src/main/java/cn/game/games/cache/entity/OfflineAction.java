package cn.game.games.cache.entity;

import cn.game.games.cache.base.DbEntity;
import java.io.Serializable;

/**
 * 玩家的离线行为保存，上线时处理
 * @mbg.generated
 */
public class OfflineAction implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private long id;
	/**
	 * @mbg.generated
	 */
	private long playerId;
	/**
	 * 类型
	 * @mbg.generated
	 */
	private int type;
	/**
	 * @mbg.generated
	 */
	private String params;
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
	public int getType() {
		return type;
	}

	/**
	 * @mbg.generated
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @mbg.generated
	 */
	public String getParams() {
		return params;
	}

	/**
	 * @mbg.generated
	 */
	public void setParams(String params) {
		this.params = params;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.OfflineActionMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}
}