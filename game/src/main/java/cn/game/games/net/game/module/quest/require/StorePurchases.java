package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.ShopConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.ShopManager;

/**    
 * 商店购买商品
 * 2024年5月8日 下午5:50:06
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
	public boolean checkEventParam(PlayerEvent event) {
		int param = getParam();
		if (param == 0) {
			return true; 
		}
		int shopId = event.getIntParameter(0);
		ShopConfig shopConfig = ShopManager.instance().get(shopId); 
		return shopConfig.Type == param;
	}

}
