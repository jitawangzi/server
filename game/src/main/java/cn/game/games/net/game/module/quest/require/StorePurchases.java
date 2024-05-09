package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

/**    
 * 黑市购买商品
 * @date 2024年5月8日 下午5:50:06
 * @author SYQ
 */
// TODO 
@ConditionType(type = ConditionTypeEnum.StorePurchases)
public class StorePurchases extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.BuyItems };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public StorePurchases() {
	};

	@Override
	public boolean checkEventParam(GameEvent event) {

		return true;
	}

}
