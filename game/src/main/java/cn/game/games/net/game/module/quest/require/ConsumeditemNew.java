package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.manual.GoodsTypeEnum;

/**
 * 消耗指定类型道具
 * 
 */
@ConditionType(type = ConditionTypeEnum.ConsumeditemNew)
public class ConsumeditemNew extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.CostItem };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public ConsumeditemNew() {

	}

//	@Override
//	public void updateRequireCount(PlayerEvent event) {
//		int count = event.getIntParameter(1);
//		finishCount += count;
//	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		int id = event.getIntParameter(0);
		int goodsType = ItemHelper.getGoodsType(id); 
		if (goodsType != GoodsTypeEnum.Item.getId()) {
			return false; 
		}
		if (getRequireId() > 0 ) {
			return id == getRequireId();
		}
		ItemConfig itemConfig = ItemManager.instance().get(id); 
		return itemConfig.ItemType == getParam(); 
		
	}
}
