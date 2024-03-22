package cn.game.games.cache.op.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import cn.game.games.cache.entity.Role;
import cn.game.games.cache.entity.Skill;
import cn.game.games.cache.op.face.ISkillOp;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.SkillMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.OldSkillConfig;
import cn.game.protocol.generated.manager.OldSkillManager;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

public class SkillOp extends BasePlayerModule implements ISkillOp {

	/** 通用技能,或者主角职业技能 */
	private Map<Integer, Skill> skills;

	@Override
	public void init() {
		if (skills == null) {
			skills = new HashMap<Integer, Skill>();
		}
	}

	@Override
	public void initLoadData(List<Skill> list) {

		if (list != null) {
			for (Skill skill : list) {
				this.skills.put(skill.getId(), skill);
			}
		}

	}

	@Override
	public Skill newSkill(int id, int roleId) {

		OldSkillConfig skillConfig = OldSkillManager.getInstance().getSkillConfig(id);

		if (skillConfig == null) {
			throw new IllegalArgumentException("skill id not exist" + id);
		}
		if (this.skills.containsKey(id)) {
			return null;
		}
		Skill skill = new Skill();
		skill.setPlayerId(playerId);
		skill.setLevel(1);
		skill.setHeroId(roleId);
		skill.setId(id);
		insert(skill);

		
		player.handleEvent(new GameEvent(EventTypeEnum.Skill, null, skill.getId()));

		return skill;
	}

	@Override
	public void insert(Skill skill) {

		DAO.execute(SkillMapper.class, MapperConstant.insert, skill);
		this.skills.put(skill.getId(), skill);
	}

	@Override
	public void update(Skill skill) {

		DAO.execute(SkillMapper.class, MapperConstant.updateByPrimaryKey, skill);
	}

	@Override
	public Collection<Skill> list() {

		return this.skills.values();
	}

	@Override
	public Skill get(int id) {

		return this.skills.get(id);
	}

	@Override
	public int getSkillCountByLevel(int level) {
		int count = 0;
		for (Skill e : this.skills.values()) {
			if (e.getLevel() >= level) {
				count++;
			}
		}
		return count;
	}

	@Override
	public void resetRole(int id) {
		Skill skill = get(id);
		if (skill != null) {
			skill.setHeroId(0);
			update(skill);
		}
	}

	@Override
	public boolean levelUp(int id, int level) {
		Skill skill = get(id);
		if (skill == null) {
			return false;
		}
		skill.setLevel(level);
		update(skill);

		
		player.handleEvent(new GameEvent(EventTypeEnum.SkillUp, null, skill.getId()));
		return true;
	}

	@Override
	public boolean addSkill(Role role, int skillId) {

		int skillSize = 0;
		Collection<Skill> values = this.skills.values();
		Skill skill = null;
		for (Skill e : values) {
			if (e.getHeroId().equals(role.getDictId())) {
				skillSize++;
			}
			if (e.getId() == skillId) {
				skill = e;
			}
		}
//		if (skillSize >= role.getSlot()) { return false; }

		if (skill == null) { // 新增加一个技能
			skill = newSkill(skillId, role.getDictId());
		} else {

			if (skill.getHeroId() != 0) {
				return false;
			}
			skill.setHeroId(role.getDictId());
			update(skill);
		}
		return true;
	}

	@Override
	public List<Skill> list(int roleId) {
		List<Skill> list = new ArrayList<>();
		for (Skill e : this.skills.values()) {
			if (e.getHeroId() == roleId) {
				list.add(e);
			}
		}
		return list;
	}

	@Override
	public void copyProf(Role curRole, Role copyRole) {
		for (Skill e : this.skills.values()) {
			if (e.getHeroId().equals(curRole.getDictId())) {
				e.setHeroId(copyRole.getDictId());
				update(e);
			}
		}
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleEvent(GameEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		// TODO Auto-generated method stub

	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub
	}
}
