package cn.game.protocol.generated.helper;

import cn.game.protocol.generated.manager.HeroBUFFManager;
import cn.game.protocol.generated.manager.HeroBeamManager;
import cn.game.protocol.generated.manager.ShopItemManager;
import cn.game.protocol.generated.manager.HeroBookManager;
import cn.game.protocol.generated.manager.HeroSkillGroupManager;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.generated.manager.EquipManager;
import cn.game.protocol.generated.manager.AttributeVlalueManager;
import cn.game.protocol.generated.manager.RandomAttributeManager;
import cn.game.protocol.generated.manager.HeishiManager;
import cn.game.protocol.generated.manager.RechargeStoreManager;
import cn.game.protocol.generated.manager.GamePlayRandomBuffManager;
import cn.game.protocol.generated.manager.HeroBreakManager;
import cn.game.protocol.generated.manager.HeroSkillManager;
import cn.game.protocol.generated.manager.AssetRestoreManager;
import cn.game.protocol.generated.manager.AttrEffectCoefficientManager;
import cn.game.protocol.generated.manager.HeroLvManager;
import cn.game.protocol.generated.manager.HeadPortraitManager;
import cn.game.protocol.generated.manager.ShopManager;
import cn.game.protocol.generated.manager.ConsumeManager;
import cn.game.protocol.generated.manager.HeadBoxManager;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.generated.manager.HeroBulletManager;
import cn.game.protocol.generated.manager.RandomGroupManager;
import cn.game.protocol.generated.manager.AttrEffectConfigManager;
import cn.game.protocol.generated.manager.RandomGivenManager;
import cn.game.protocol.generated.manager.RandomNameManager;
import cn.game.protocol.generated.config.GlobalConst;


/**
 * 
 * 工具生成的，不要手动修改
 */
public class ManagerHelper {

	public static void init() {
		HeroBUFFManager.instance().load();
		HeroBeamManager.instance().load();
		ShopItemManager.instance().load();
		HeroBookManager.instance().load();
		HeroSkillGroupManager.instance().load();
		ItemManager.instance().load();
		EquipManager.instance().load();
		AttributeVlalueManager.instance().load();
		RandomAttributeManager.instance().load();
		HeishiManager.instance().load();
		RechargeStoreManager.instance().load();
		GamePlayRandomBuffManager.instance().load();
		HeroBreakManager.instance().load();
		HeroSkillManager.instance().load();
		AssetRestoreManager.instance().load();
		AttrEffectCoefficientManager.instance().load();
		HeroLvManager.instance().load();
		HeadPortraitManager.instance().load();
		ShopManager.instance().load();
		ConsumeManager.instance().load();
		HeadBoxManager.instance().load();
		BattleManager.instance().load();
		HeroManager.instance().load();
		HeroBulletManager.instance().load();
		RandomGroupManager.instance().load();
		AttrEffectConfigManager.instance().load();
		RandomGivenManager.instance().load();
		RandomNameManager.instance().load();
		GlobalConst.instance().load();
		
	}
}
