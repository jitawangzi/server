package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.StoreConfig;
import cn.game.protocol.generated.enume.OldConditionTypeEnum;
import cn.game.protocol.generated.manager.StoreManager;

/**
 * 商店购买商品
 * 
 * @date 2021年4月28日 下午3:19:54
 * @author SYQ
 */
@ConditionType(type = OldConditionTypeEnum.BuyItems)
public class StoreBuy extends AbstractCondition {

	public StoreBuy() {
	};
	@Override
	public void setEvents() {
		super.events = new EventTypeEnum[] { EventTypeEnum.BuyItems };
	}

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
