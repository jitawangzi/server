package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.OldConditionTypeEnum;

/**
 * @Description 卡牌升级次数,玩家升级一次卡牌的行为记为+1,无论行为是否令玩家升级
 * @date 2021年1月14日 下午3:48:55
 * @author SYQ
 */
@ConditionType(type = OldConditionTypeEnum.CardUpGradeTimes)
public class CardUpGradeTimes extends AbstractCondition {

	public CardUpGradeTimes() {

	}

	@Override
	public void setEvents() {
		super.events = new EventTypeEnum[] { EventTypeEnum.CardUpGrade };
	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		return true;
	}

}
