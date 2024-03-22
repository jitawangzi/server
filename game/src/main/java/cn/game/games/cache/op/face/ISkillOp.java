package cn.game.games.cache.op.face;

import java.util.Collection;
import java.util.List;

import cn.game.games.cache.entity.Role;
import cn.game.games.cache.entity.Skill;

public interface ISkillOp {

	public void initLoadData(List<Skill> list);

	/**
	 * @Description 新增加一个技能
	 * @param id
	 * @param roleId
	 * @return
	 */
	public Skill newSkill(int id, int roleId);

	/**
	 * @Description 给伙伴增加技能
	 * @param role
	 * @param skillId
	 * @return
	 */
	public boolean addSkill(Role role, int skillId);

	public boolean levelUp(int id, int level);

	public void insert(Skill skill);

	public void update(Skill skill);

	public void resetRole(int id);

	public Collection<Skill> list();

	public Skill get(int id);

	public int getSkillCountByLevel(int level);

	public List<Skill> list(int roleId);

	public void copyProf(Role curRole, Role copyRole);

}
