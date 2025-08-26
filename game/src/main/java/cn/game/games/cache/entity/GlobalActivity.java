package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class GlobalActivity implements Serializable, DbEntity {

	/**
	 * 唯一id
	 * @mbg.generated
	 */
	private long id;
	/**
	 * @mbg.generated
	 */
	private String serverId;
	/**
	 * 活动id
	 * @mbg.generated
	 */
	private int configId;
	/**
	 * 活动状态
	 * @mbg.generated
	 */
	private byte state;
	/**
	 * 创建时间
	 * @mbg.generated
	 */
	private long createTime;
	/**
	 * 活动参数
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
	public byte getState() {
		return state;
	}

	/**
	 * @mbg.generated
	 */
	public void setState(byte state) {
		this.state = state;
	}

	/**
	 * @mbg.generated
	 */
	public long getCreateTime() {
		return createTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
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
		return cn.game.games.net.data.mapper.GlobalActivityMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}


}