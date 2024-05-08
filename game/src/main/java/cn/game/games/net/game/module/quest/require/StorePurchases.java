package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.StoreConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.StoreManager;

/**    
 * 商店购买商品
 * @date 2024年5月8日 下午5:50:06
 * @author SYQ
 */
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

		int goodsId = event.getIntParameter(0);
		StoreConfig storeConfig = StoreManager.getInstance().getStoreConfig(goodsId);
		int firstTabType = storeConfig.getFirstTabType();
		int itemId = storeConfig.getItemId();
		if ((getRequireId() == 0 || itemId == getRequireId()) && (getParam(0) == 0 || getParam(0) == firstTabType))
			return true;
		return false;
	}

	@Override
	public void updateRequireCount(GameEvent event) {
		int count = event.getIntParameter(1);
		finishCount += count;
	}

}
