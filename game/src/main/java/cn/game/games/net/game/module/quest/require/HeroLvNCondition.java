package cn.game.games.net.game.module.quest.require;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.op.impl.RoleOp;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.OldConditionTypeEnum;

/**   
 * @Description 伙伴等级、数量
 * @date 2019年1月8日 下午3:38:05
 * @author SYQ
 */
@ConditionType(type = OldConditionTypeEnum.CampLimit)
public class HeroLvNCondition extends AbstractCondition {

	public HeroLvNCondition() {

	}

	@Override
	public void setEvents() {
		super.events = new EventTypeEnum[] { EventTypeEnum.RoleLevelUp, EventTypeEnum.Role };
	}

	@Override
	@JsonIgnore
	public long getFinishCount() {

		RoleOp heroOp = player.getModule(RoleOp.class);
		return heroOp.getCountByLevel(getParam(0));
	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		return true;
	}
}
