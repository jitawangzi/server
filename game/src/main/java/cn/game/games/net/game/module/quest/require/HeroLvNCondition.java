package cn.game.games.net.game.module.quest.require;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.op.impl.RoleOp;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

/**   
 * @Description 伙伴等级、数量
 * @date 2019年1月8日 下午3:38:05
 * @author SYQ
 */
@ConditionType(type = ConditionTypeEnum.HeroLevel)
public class HeroLvNCondition extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.Hero };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public HeroLvNCondition() {

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
