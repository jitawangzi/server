package cn.game.games.net.game.module.quest.require;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.op.impl.RoleOp;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.OldConditionTypeEnum;

/**   
 * @Description 伙伴星级、数量（不含主）
 * @date 2019年1月8日 下午3:38:41
 * @author SYQ
 */
@ConditionType(type = OldConditionTypeEnum.CumulativeFirstAttackDie)
public class HeroStarLvNCondition extends AbstractCondition {

	public HeroStarLvNCondition() {

	}

	@Override
	public void setEvents() {
		super.events = new EventTypeEnum[] { EventTypeEnum.Role, EventTypeEnum.RoleStarUp };
	}

	@Override
	@JsonIgnore
	public long getFinishCount() {
		RoleOp heroOp = player.getModule(RoleOp.class);
		return heroOp.getCountByStar(getParam(0));
	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		return true;
	}
}
