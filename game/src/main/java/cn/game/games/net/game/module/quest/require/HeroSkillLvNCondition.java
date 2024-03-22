package cn.game.games.net.game.module.quest.require;

import cn.game.games.cache.op.impl.RoleOp;
import cn.game.games.cache.op.impl.SkillOp;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.OldConditionTypeEnum;

/**   
 * @Description 技能等级、数量
 * @date 2019年1月8日 下午3:38:23
 * @author SYQ
 */
@ConditionType(type = OldConditionTypeEnum.SwitchAllCharacterSkills)
public class HeroSkillLvNCondition extends AbstractCondition {

	public HeroSkillLvNCondition() {

	}

	@Override
	public void setEvents() {
		super.events = new EventTypeEnum[] { EventTypeEnum.Skill, EventTypeEnum.SkillUp };
	}

	@Override
	public long getFinishCount() {
		RoleOp heroOp = player.getModule(RoleOp.class);
		SkillOp skillOp = player.getModule(SkillOp.class);
		int param = getParam(0);

		return heroOp.getCountByLevel(param) + skillOp.getSkillCountByLevel(param);
	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		return true;
	}
}
