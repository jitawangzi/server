package cn.game.games.net.game.module.develop.skill;

import java.io.Serializable;

import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.ItemNoStack;

public class DragonSkill extends ItemNoStack implements Serializable, DbEntity {

	/**
	 * 等级
	 * @mbg.generated
	 */
	private int level;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}

}