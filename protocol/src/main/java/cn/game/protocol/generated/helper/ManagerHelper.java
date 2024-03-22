package cn.game.protocol.generated.helper;

import cn.game.protocol.generated.manager.ConditionManager;
import cn.game.protocol.generated.manager.MonthCardManager;
import cn.game.protocol.generated.manager.ShopItemGroupManager;
import cn.game.protocol.generated.manager.HeadPortraitManager;
import cn.game.protocol.generated.manager.ShopItemManager;
import cn.game.protocol.generated.manager.MoneyRecoveryManager;
import cn.game.protocol.generated.manager.ParameterConsumeManager;
import cn.game.protocol.generated.manager.Test1Manager;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.generated.manager.ShopManager;
import cn.game.protocol.generated.manager.ConsumeManager;
import cn.game.protocol.generated.manager.HeadBoxManager;
import cn.game.protocol.generated.manager.TestManager;
import cn.game.protocol.generated.manager.UserUpgradeManager;
import cn.game.protocol.generated.manager.ShopGiftManager;
import cn.game.protocol.generated.manager.HeroAttributeManager;
import cn.game.protocol.generated.manager.ExpManager;
import cn.game.protocol.generated.manager.HeroConflateManager;
import cn.game.protocol.generated.manager.RandomNameManager;
import cn.game.protocol.generated.manager.HeroResourceManager;
import cn.game.protocol.generated.config.GlobalConst;


/**
 * 
 * 工具生成的，不要手动修改
 */
public class ManagerHelper {

	public static void init() {
		ConditionManager.instance().load();
		MonthCardManager.instance().load();
		ShopItemGroupManager.instance().load();
		HeadPortraitManager.instance().load();
		ShopItemManager.instance().load();
		MoneyRecoveryManager.instance().load();
		ParameterConsumeManager.instance().load();
		Test1Manager.instance().load();
		ItemManager.instance().load();
		ShopManager.instance().load();
		ConsumeManager.instance().load();
		HeadBoxManager.instance().load();
		TestManager.instance().load();
		UserUpgradeManager.instance().load();
		ShopGiftManager.instance().load();
		HeroAttributeManager.instance().load();
		ExpManager.instance().load();
		HeroConflateManager.instance().load();
		RandomNameManager.instance().load();
		HeroResourceManager.instance().load();
		GlobalConst.instance().load();
		
	}
}
