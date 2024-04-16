package cn.game.games.net.game.module.develop;

import java.util.ListIterator;

import cn.game.games.cache.entity.Hero;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.develop.dragon.Dragon;
import cn.game.games.net.game.module.develop.skill.DragonSkill;
import cn.game.games.net.game.module.develop.sword.Sword;
import cn.game.games.net.game.module.develop.sword.SwordModule;
import cn.game.games.net.game.module.player.VarConstant;
import cn.game.protocol.generated.config.DragonConfig;
import cn.game.protocol.generated.config.DragonSkillConfig;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.HeroSwordConfig;
import cn.game.protocol.generated.config.WallConfig;
import cn.game.protocol.generated.manager.DragonManager;
import cn.game.protocol.generated.manager.DragonSkillManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.generated.manager.HeroSwordManager;
import cn.game.protocol.generated.manager.WallManager;
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
	private IntMapWrapper heroAttr = new IntMapWrapper();
	private IntMapWrapper dragonAttr = new IntMapWrapper();
	private IntMapWrapper dragonSkillAttr = new IntMapWrapper();

	private IntMapWrapper swordAttr = new IntMapWrapper();
	private IntMapWrapper fashionAttr = new IntMapWrapper();
	private IntMapWrapper equipAttr = new IntMapWrapper();
	private IntMapWrapper gemAttr = new IntMapWrapper();
	private IntMapWrapper alchemyAttr = new IntMapWrapper();

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
		log.info("calcAllAttr ： " + toString());
	}

	public PlayerBattleAttrs buildBattleAttrs() {
		cn.game.protocol.protobuf.BattleMsg.PlayerBattleAttrs.Builder builder = PlayerBattleAttrs.newBuilder();
		builder.putAllDragonAttrs(wallAttr.getMap());
		builder.putAllHeroAttrs(heroAttr.getMap());

		IntMapWrapper dragon = new IntMapWrapper();
		builder.putAllDragonAttrs(dragon.addAll(dragonAttr.getMap()).addAll(dragonSkillAttr.getMap()).getMap());

		IntMapWrapper playerMap = new IntMapWrapper();
		playerMap.addAll(swordAttr.getMap());
		playerMap.addAll(fashionAttr.getMap());
		playerMap.addAll(equipAttr.getMap());
		playerMap.addAll(gemAttr.getMap());
		playerMap.addAll(alchemyAttr.getMap());

		builder.putAllPlayerAttrs(playerMap.getMap());

		return builder.build();
	}

	public void calcHeroAttr() {
		heroAttr.clear();
		Hero hero = player.getHeroModule().getCurHero();
		HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
		heroAttr.addAll(heroConfig.InitialAttribute);
	}

	public void calcDragonAttr() {
		dragonAttr.clear();
		Dragon o = player.getDragonModule().getCurDragon();
		DragonConfig dragonConfig = DragonManager.instance().get(o.getConfigId());
		heroAttr.addAll(dragonConfig.DragonStarValve);
		// TODO
	}

	public void calcDragonSkillAttr() {
		dragonSkillAttr.clear();
		DragonSkill o = player.getDragonSkillModule().getCurDragonSkill();
		DragonSkillConfig dragonSkillConfig = DragonSkillManager.instance().get(o.getConfigId());
		heroAttr.add(dragonSkillConfig.UpgradeAttributeAward[0], dragonSkillConfig.UpgradeAttributeAward[1] * o.getLevel());
	}

	public void calcWallAttr() {
		wallAttr.clear();
		int level = player.getVarModule().getVar(VarConstant.WALL_LEVEL);
		WallConfig config = WallManager.instance().get(level);
		wallAttr.add(config.WallAttribute[0], config.WallAttribute[1] * level);
	}

	public void calcSwordAttr() {
		swordAttr.clear();
		SwordModule module = player.getModule(SwordModule.class);
		Sword curSword = module.getCurSword();
		if (curSword != null) {
			HeroSwordConfig config = HeroSwordManager.instance().get(curSword.getConfigId());
			swordAttr.add(config.SwordValve);
			// TODO 星级属性
		}
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

	@Override
	public String toString() {
		return "AttrModule [heroAttr=" + heroAttr + ", dragonAttr=" + dragonAttr + ", dragonSkillAttr=" + dragonSkillAttr + ", wallAttr=" + wallAttr
				+ ", swordAttr=" + swordAttr + ", fashionAttr=" + fashionAttr + ", equipAttr=" + equipAttr + ", gemAttr=" + gemAttr + ", alchemyAttr="
				+ alchemyAttr + "]";
	}

}
