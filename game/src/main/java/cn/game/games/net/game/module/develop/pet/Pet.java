package cn.game.games.net.game.module.develop.pet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.Item;
import cn.game.protocol.protobuf.PetMsg.PetInfo;

public class Pet extends Item implements Serializable, DbEntity {

	private static final long serialVersionUID = 1L;
	private int level;
	private int breakLevel;
	private List<Integer> skillsList = new ArrayList<>();;

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}


	public PetInfo toPetInfo() {
		PetInfo.Builder builder = PetInfo.newBuilder();
		builder.setLevel(level);
		builder.setBreakLevelMax(breakLevel);
		builder.addAllSkills(skillsList);
		builder.setId(configId);
		return builder.build();
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getBreakLevel() {
		return breakLevel;
	}

	public void setBreakLevel(int breakLevel) {
		this.breakLevel = breakLevel;
	}

	public List<Integer> getSkillsList() {
		return skillsList;
	}

	public void setSkillsList(List<Integer> skillsList) {
		this.skillsList = skillsList;
	}

}