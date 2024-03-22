package cn.game.games.cache.entity;

import java.io.Serializable;

import cn.game.games.cache.base.DbEntity;

public class ItemNoStack extends Item implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	protected Boolean isStack;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public Boolean getIsStack() {
		return isStack;
	}

	/**
	 * @mbg.generated
	 */
	public void setIsStack(Boolean isStack) {
		this.isStack = isStack;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.ItemNoStackMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return isStack;
	}

}