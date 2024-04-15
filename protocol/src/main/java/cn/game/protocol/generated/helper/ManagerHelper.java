package cn.game.protocol.generated.helper;

import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.manager.AccomplishmentManager;
import cn.game.protocol.generated.manager.ArtResourceManager;
import cn.game.protocol.generated.manager.AssetRestoreManager;
import cn.game.protocol.generated.manager.AttrEffectCoefficientManager;
import cn.game.protocol.generated.manager.AttrEffectConfigManager;
import cn.game.protocol.generated.manager.BUFFManager;
import cn.game.protocol.generated.manager.BattleFieldManager;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.BeamManager;
import cn.game.protocol.generated.manager.BulletManager;
import cn.game.protocol.generated.manager.ConditionManager;
import cn.game.protocol.generated.manager.ConsumeManager;
import cn.game.protocol.generated.manager.CoordinateGroupManager;
import cn.game.protocol.generated.manager.DisplacementManager;
import cn.game.protocol.generated.manager.DragonManager;
import cn.game.protocol.generated.manager.DragonSkillManager;
import cn.game.protocol.generated.manager.ExpManager;
import cn.game.protocol.generated.manager.HeadBoxManager;
import cn.game.protocol.generated.manager.HeadPortraitManager;
import cn.game.protocol.generated.manager.HeroBUFFManager;
import cn.game.protocol.generated.manager.HeroBeamManager;
import cn.game.protocol.generated.manager.HeroBulletManager;
import cn.game.protocol.generated.manager.HeroDisplacementManager;
import cn.game.protocol.generated.manager.HeroFashionManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.generated.manager.HeroResourceManager;
import cn.game.protocol.generated.manager.HeroSkillGroupManager;
import cn.game.protocol.generated.manager.HeroSkillManager;
import cn.game.protocol.generated.manager.HeroSwordManager;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.generated.manager.LotteryManager;
import cn.game.protocol.generated.manager.MailManager;
import cn.game.protocol.generated.manager.MonsterAppearManager;
import cn.game.protocol.generated.manager.MonsterManager;
import cn.game.protocol.generated.manager.MonthCardManager;
import cn.game.protocol.generated.manager.PacksChoiceManager;
import cn.game.protocol.generated.manager.ParameterConsumeManager;
import cn.game.protocol.generated.manager.QuestCompletionConditionManager;
import cn.game.protocol.generated.manager.RandomGivenManager;
import cn.game.protocol.generated.manager.RandomNameManager;
import cn.game.protocol.generated.manager.RogueEnergyManager;
import cn.game.protocol.generated.manager.RoguelikeManager;
import cn.game.protocol.generated.manager.SevenDaysCarnivalManager;
import cn.game.protocol.generated.manager.ShopGiftManager;
import cn.game.protocol.generated.manager.ShopItemGroupManager;
import cn.game.protocol.generated.manager.ShopItemManager;
import cn.game.protocol.generated.manager.ShopManager;
import cn.game.protocol.generated.manager.SingleChargeActivityManager;
import cn.game.protocol.generated.manager.SkillGroupManager;
import cn.game.protocol.generated.manager.SkillManager;
import cn.game.protocol.generated.manager.UserUpgradeManager;
import cn.game.protocol.generated.manager.WallManager;


/**
 * 
 * 工具生成的，不要手动修改
 */
public class ManagerHelper {

	public static void init() {
		HeroBUFFManager.instance().load();
		SkillGroupManager.instance().load();
		HeroBeamManager.instance().load();
		ShopItemManager.instance().load();
		ItemManager.instance().load();
		PacksChoiceManager.instance().load();
		UserUpgradeManager.instance().load();
		DragonSkillManager.instance().load();
		HeroSkillManager.instance().load();
		AssetRestoreManager.instance().load();
		AttrEffectCoefficientManager.instance().load();
		HeadPortraitManager.instance().load();
		ShopManager.instance().load();
		ConsumeManager.instance().load();
		HeadBoxManager.instance().load();
		MonsterAppearManager.instance().load();
		HeroManager.instance().load();
		HeroBulletManager.instance().load();
		RandomGivenManager.instance().load();
		CoordinateGroupManager.instance().load();
		RandomNameManager.instance().load();
		RogueEnergyManager.instance().load();
		ArtResourceManager.instance().load();
		WallManager.instance().load();
		ShopItemGroupManager.instance().load();
		RoguelikeManager.instance().load();
		HeroSwordManager.instance().load();
		BUFFManager.instance().load();
		DragonManager.instance().load();
		ParameterConsumeManager.instance().load();
		HeroSkillGroupManager.instance().load();
		BeamManager.instance().load();
		HeroDisplacementManager.instance().load();
		MonsterManager.instance().load();
		BattleFieldManager.instance().load();
		HeroFashionManager.instance().load();
		ShopGiftManager.instance().load();
		HeroResourceManager.instance().load();
		ConditionManager.instance().load();
		MonthCardManager.instance().load();
		BulletManager.instance().load();
		AccomplishmentManager.instance().load();
		QuestCompletionConditionManager.instance().load();
		DisplacementManager.instance().load();
		SkillManager.instance().load();
		MailManager.instance().load();
		BattleManager.instance().load();
		LotteryManager.instance().load();
		SingleChargeActivityManager.instance().load();
		AttrEffectConfigManager.instance().load();
		SevenDaysCarnivalManager.instance().load();
		ExpManager.instance().load();
		GlobalConst.instance().load();
		
	}
}
