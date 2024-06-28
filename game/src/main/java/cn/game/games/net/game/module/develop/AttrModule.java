package cn.game.games.net.game.module.develop;

import java.util.Collection;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import cn.game.games.cache.entity.Hero;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.develop.dragon.Dragon;
import cn.game.games.net.game.module.develop.hero.HeroModule;
import cn.game.games.net.game.module.develop.skill.DragonSkill;
import cn.game.games.net.game.module.develop.sword.Sword;
import cn.game.games.net.game.module.develop.sword.SwordModule;
import cn.game.protocol.generated.config.AttributeVlalueConfig;
import cn.game.protocol.generated.config.DragonConfig;
import cn.game.protocol.generated.config.DragonSkillConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeavenlyDaoConfig;
import cn.game.protocol.generated.config.HeroBookConfig;
import cn.game.protocol.generated.config.HeroBreakConfig;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.HeroSwordConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.AttributeVlalueManager;
import cn.game.protocol.generated.manager.DragonManager;
import cn.game.protocol.generated.manager.DragonSkillManager;
import cn.game.protocol.generated.manager.HeavenlyDaoManager;
import cn.game.protocol.generated.manager.HeroBookManager;
import cn.game.protocol.generated.manager.HeroBreakManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.generated.manager.HeroSwordManager;
import cn.game.protocol.protobuf.BattleMsg.HeroAttr;
import cn.game.protocol.protobuf.BattleMsg.PlayerBattleAttrs;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.IntMapWrapper;

/**    
 * 各种属性的计算
 * @date 2024年4月15日 下午6:29:59
 * @author SYQ
 */
public class AttrModule extends BasePlayerModule {

	private IntMapWrapper wallAttr = new IntMapWrapper();

	private Map<Long, IntMapWrapper> heroAttrs = new HashMap<Long, IntMapWrapper>();

	private IntMapWrapper dragonAttr = new IntMapWrapper();
	private IntMapWrapper dragonSkillAttr = new IntMapWrapper();

	private IntMapWrapper swordAttr = new IntMapWrapper();
	private IntMapWrapper fashionAttr = new IntMapWrapper();
	private IntMapWrapper equipAttr = new IntMapWrapper();
	private IntMapWrapper gemAttr = new IntMapWrapper();
	private IntMapWrapper alchemyAttr = new IntMapWrapper();
	private IntMapWrapper bookAttr = new IntMapWrapper();

	private IntMapWrapper heavenlyDaoAttr = new IntMapWrapper();

	private IntMapWrapper potentialAttr = new IntMapWrapper();

	/** 战斗力 */
	private int power;

	/** 
	 * 计算所有属性，给客户端战斗时使用。
	 */
	public void calcAllAttr() {
		calcAlchemyAttr();
		calcDragonAttr();
		calcDragonSkillAttr();
		calcEquipAttr();
		calcFashionAttr();
		calcGemAttr();
		calcHeroAttr();
		calcSwordAttr();
		calcWallAttr();
		calcBookAttr();
		calcHeavenlyDaoAttr();
		calcPotentialAttrAttr();
		log.info("calcAllAttr ： " + toString());
	}

	public PlayerBattleAttrs buildBattleAttrs() {
		cn.game.protocol.protobuf.BattleMsg.PlayerBattleAttrs.Builder builder = PlayerBattleAttrs.newBuilder();
		builder.putAllWallAttrs(wallAttr.getMap());

		for (Entry<Long, IntMapWrapper> entry : heroAttrs.entrySet()) {
			builder.addHeroAttrs(HeroAttr.newBuilder().setHeroUid(entry.getKey().toString()).putAllHeroAttrs(entry.getValue().getMap()));
		}

		IntMapWrapper dragon = new IntMapWrapper();
		builder.putAllDragonAttrs(dragon.addAll(dragonAttr.getMap()).addAll(dragonSkillAttr.getMap()).getMap());

		IntMapWrapper playerMap = new IntMapWrapper();
		playerMap.addAll(swordAttr.getMap());
		playerMap.addAll(fashionAttr.getMap());
		playerMap.addAll(equipAttr.getMap());
		playerMap.addAll(gemAttr.getMap());
		playerMap.addAll(alchemyAttr.getMap());
		playerMap.addAll(bookAttr.getMap());
		playerMap.addAll(heavenlyDaoAttr.getMap());
		playerMap.addAll(potentialAttr.getMap());

		builder.putAllPlayerAttrs(playerMap.getMap());

		return builder.build();
	}

	public void calcHeroAttr() {

		heroAttrs.clear();
//		Hero hero = player.getHeroModule().getCurHero();
//		if (hero == null) {
//			return;
//		}
		Map<Long, Integer> battleHeros = player.getHeroModule().getBattleHeros();
		for (Long uid : battleHeros.keySet()) {
			Hero hero = player.getHeroModule().get(uid);
			HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
			// 初始属性
			AttributeVlalueConfig attributeVlalueConfig = AttributeVlalueManager.instance().get(heroConfig.InitialAttributeId);
			IntMapWrapper heroAttrMap = new IntMapWrapper();
			heroAttrMap.addAll(attributeVlalueConfig.AttributeVlalue);
			// 等级成长属性
			if (hero.getLevel() > 1) {
				attributeVlalueConfig = AttributeVlalueManager.instance().get(heroConfig.GrowthAttributeId);
				attributeVlalueConfig.AttributeVlalue.forEach((k, v) -> {
					heroAttrMap.add(k, v * (hero.getLevel() - 1));
				});
			}
			// 突破属性
//			for (int[] attrArray : heroConfig.BreakActivationAttribute) {
//				if (attrArray[0] == hero.getQuality()) {
//					attributeVlalueConfig = AttributeVlalueManager.instance().get(attrArray[1]);
//					heroAttrMap.addAll(attributeVlalueConfig.AttributeVlalue);
//				}
//			}
			HeroBreakConfig uiInitialQualityStar = HeroBreakManager.instance().getUIInitialQualityStar(hero.getQuality(), hero.getStar());
			attributeVlalueConfig = AttributeVlalueManager.instance().get(uiInitialQualityStar.BreakOneTime);
			heroAttrMap.addAll(attributeVlalueConfig.AttributeVlalue);


			heroAttrs.put(uid, heroAttrMap);
		}
	}

	public void calcDragonAttr() {
		dragonAttr.clear();
		Dragon o = player.getDragonModule().getCurDragon();
		if (o == null) {
			return;
		}
		DragonConfig dragonConfig = DragonManager.instance().get(o.getConfigId());
		dragonAttr.addAll(dragonConfig.DragonStarValve);
		// TODO
	}

	public void calcDragonSkillAttr() {
		dragonSkillAttr.clear();
		DragonSkill o = player.getDragonSkillModule().getCurDragonSkill();
		if (o != null) {
			DragonSkillConfig dragonSkillConfig = DragonSkillManager.instance().get(o.getConfigId());
			dragonSkillAttr.add(dragonSkillConfig.UpgradeAttributeAward[0], dragonSkillConfig.UpgradeAttributeAward[1] * o.getLevel());
		}
	}

	public void calcWallAttr() {
//		wallAttr.clear();
//		int level = player.getVarModule().getVar(VarConstant.WALL_LEVEL);
//		if (level == 0) {
//			return;
//		}
//		WallConfig config = WallManager.instance().get(level);
//		wallAttr.add(config.WallAttribute[0], config.WallAttribute[1] * level);
	}

	public void calcSwordAttr() {
		swordAttr.clear();
		SwordModule module = player.getModule(SwordModule.class);
		Sword curSword = module.getCurSword();
		if (curSword == null) {
			return;
		}
		HeroSwordConfig config = HeroSwordManager.instance().get(curSword.getConfigId());
		swordAttr.add(config.SwordValve);
		// TODO 星级属性
	}

	public void calcFashionAttr() {
		fashionAttr.clear();
//		fashionAttr.add(config.WallAttribute[0], config.WallAttribute[1] * level);
	}

	public void calcEquipAttr() {
		equipAttr.clear();
//		equipAttr.add(config.WallAttribute[0], config.WallAttribute[1] * level);
	}

	public void calcGemAttr() {
		gemAttr.clear();
//		gemAttr.add(config.WallAttribute[0], config.WallAttribute[1] * level);
	}

	public void calcAlchemyAttr() {
		alchemyAttr.clear();
		IntMapWrapper alchemysMap = player.getPlayerModule().getAlchemysMap();

//		alchemyAttr.add(config.WallAttribute[0], config.WallAttribute[1] * level);
	}

	public void calcBookAttr() {
		if (!player.isFuncOpen(InitialUI.CardBook)) {
			return;
		}
		bookAttr.clear();
		Collection<HeroBookConfig> list = HeroBookManager.instance().list();
		HeroModule heroModule = player.getHeroModule();
		for (HeroBookConfig heroBookConfig : list) {

			boolean active = true;
			for (int id : heroBookConfig.HeroBookCardIdGroup) {
				Collection<Hero> heros = heroModule.getByConfigId(id);
				if (heros.isEmpty()) {
					active = false;
					break;
				}
			}
			if (active) {
				for (int id : heroBookConfig.HeroBookCardIdGroup) {
					Collection<Hero> heros = heroModule.getByConfigId(id);
					Hero hero = getMaxQualityHero(heros);
					Integer attrId = GlobalConst.HeroBookStar.get(hero.getQuality());
					if (attrId == null) {
						continue;
					}
					AttributeVlalueConfig attributeVlalueConfig = AttributeVlalueManager.instance().get(attrId);
					attributeVlalueConfig.AttributeVlalue.forEach((k, v) -> {
						bookAttr.add(k, v * hero.getStar());
					});
				}
			}
		}

	}

	public void calcHeavenlyDaoAttr() {
		if (!player.isFuncOpen(InitialUI.HeavenlyDaoCultivation)) {
			return;
		}
		heavenlyDaoAttr.clear();
		int heavenlyDaoLevel = player.getDevelopModule().getHeavenlyDaoLevel();
		HeavenlyDaoConfig heavenlyDaoConfig = HeavenlyDaoManager.instance().get(heavenlyDaoLevel);
		for (int[] att : heavenlyDaoConfig.Attribute) {
			heavenlyDaoAttr.add(att);
		}
	}

	public void calcPotentialAttrAttr() {
		if (!player.isFuncOpen(InitialUI.Consciousness)) {
			return;
		}
		potentialAttr.clear();
//		int heavenlyDaoLevel = player.getDevelopModule().getHeavenlyDaoLevel();
//		HeavenlyDaoConfig heavenlyDaoConfig = HeavenlyDaoManager.instance().get(heavenlyDaoLevel);
//		for (int[] att : heavenlyDaoConfig.Attribute) {
//			heavenlyDaoAttr.add(att);
//		}
	}

	private Hero getMaxQualityHero(Collection<Hero> heros) {
		Hero ret = null;
		for (Hero hero : heros) {
			if (ret == null) {
				ret = hero;
			} else {
				if (ret.getQuality() < hero.getQuality()) {
					ret = hero;
				} else if (ret.getQuality() == hero.getQuality()) {
					if (ret.getStar() < hero.getStar()) {
						ret = hero;
					}
				}
			}
		}
		return ret;

	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleEvent(GameEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initFromDbAfter() {
		// TODO Auto-generated method stub

	}

	public int getPower() {
		return power;
	}

	@Override
	public String toString() {
		return "AttrModule [wallAttr=" + wallAttr + ", heroAttrs=" + heroAttrs + ", dragonAttr=" + dragonAttr + ", dragonSkillAttr=" + dragonSkillAttr
				+ ", swordAttr=" + swordAttr + ", fashionAttr=" + fashionAttr + ", equipAttr=" + equipAttr + ", gemAttr=" + gemAttr + ", alchemyAttr="
				+ alchemyAttr + ", bookAttr=" + bookAttr + "]";
	}

}
