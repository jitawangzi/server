package cn.game.protocol.generated.helper;

import cn.game.protocol.generated.manager.HeroBUFFManager;
import cn.game.protocol.generated.manager.HeroLvManager;
import cn.game.protocol.generated.manager.HeroBeamManager;
import cn.game.protocol.generated.manager.ShopItemManager;
import cn.game.protocol.generated.manager.HeadPortraitManager;
import cn.game.protocol.generated.manager.HeroBookManager;
import cn.game.protocol.generated.manager.HeroSkillGroupManager;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.generated.manager.ShopManager;
import cn.game.protocol.generated.manager.AttributeVlalueManager;
import cn.game.protocol.generated.manager.RandomAttributeManager;
import cn.game.protocol.generated.manager.ConsumeManager;
import cn.game.protocol.generated.manager.HeadBoxManager;
import cn.game.protocol.generated.manager.HeishiManager;
import cn.game.protocol.generated.manager.RechargeStoreManager;
import cn.game.protocol.generated.manager.HeroBreakManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.generated.manager.HeroBulletManager;
import cn.game.protocol.generated.manager.AttrEffectConfigManager;
import cn.game.protocol.generated.manager.HeroSkillManager;
import cn.game.protocol.generated.manager.RandomNameManager;
import cn.game.protocol.generated.manager.AssetRestoreManager;
import cn.game.protocol.generated.manager.AttrEffectCoefficientManager;
import cn.game.protocol.generated.config.GlobalConst;


/**
 * 
 * 工具生成的，不要手动修改
 */
public class ManagerHelper {

	public static void init() {
		HeroBUFFManager.instance().load();
		HeroLvManager.instance().load();
		HeroBeamManager.instance().load();
		ShopItemManager.instance().load();
		HeadPortraitManager.instance().load();
		HeroBookManager.instance().load();
		HeroSkillGroupManager.instance().load();
		ItemManager.instance().load();
		ShopManager.instance().load();
		AttributeVlalueManager.instance().load();
		RandomAttributeManager.instance().load();
		ConsumeManager.instance().load();
		HeadBoxManager.instance().load();
		HeishiManager.instance().load();
		RechargeStoreManager.instance().load();
		HeroBreakManager.instance().load();
		HeroManager.instance().load();
		HeroBulletManager.instance().load();
		AttrEffectConfigManager.instance().load();
		HeroSkillManager.instance().load();
		RandomNameManager.instance().load();
		AssetRestoreManager.instance().load();
		AttrEffectCoefficientManager.instance().load();
		GlobalConst.instance().load();
		
	}
}
