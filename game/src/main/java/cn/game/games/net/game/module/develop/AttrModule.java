package cn.game.games.net.game.module.develop;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;

import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.module.develop.attr.AttrCalcType;
import cn.game.games.net.game.module.develop.attr.PlayerAttrCalc;
import cn.game.games.net.game.module.develop.dragon.Dragon;
import cn.game.games.net.game.module.develop.skill.DragonSkill;
import cn.game.games.net.game.module.develop.sword.Sword;
import cn.game.games.net.game.module.develop.sword.SwordModule;
import cn.game.protocol.generated.config.DragonConfig;
import cn.game.protocol.generated.config.DragonSkillConfig;
import cn.game.protocol.generated.config.HeroSwordConfig;
import cn.game.protocol.generated.manager.DragonManager;
import cn.game.protocol.generated.manager.DragonSkillManager;
import cn.game.protocol.generated.manager.HeroSwordManager;
import cn.game.protocol.protobuf.BattleMsg.HeroAttr;
import cn.game.protocol.protobuf.BattleMsg.PlayerBattleAttrs;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.IntMapWrapper;
import cn.game.util.reflect.ClassHelper;

/**    
 * 各种属性的计算
 * 2024年4月15日 下午6:29:59
 * @author SYQ
 */
@JsonIgnoreType
public class AttrModule extends BasePlayerModule {

	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.LoginFinish };

	// 这里先保留不删除，只是暂时不想修复数据库数据
	@JsonIgnore
	private IntMapWrapper wallAttr = new IntMapWrapper();

	@JsonIgnore
	private Map<Long, IntMapWrapper> heroAttrs = new HashMap<Long, IntMapWrapper>();

	@JsonIgnore
	private IntMapWrapper dragonAttr = new IntMapWrapper();
	@JsonIgnore
	private IntMapWrapper dragonSkillAttr = new IntMapWrapper();

	@JsonIgnore
	private IntMapWrapper swordAttr = new IntMapWrapper();
	@JsonIgnore
	private IntMapWrapper fashionAttr = new IntMapWrapper();
	@JsonIgnore
	private IntMapWrapper equipAttr = new IntMapWrapper();
	@JsonIgnore
	private IntMapWrapper gemAttr = new IntMapWrapper();
	@JsonIgnore
	private IntMapWrapper alchemyAttr = new IntMapWrapper();
	@JsonIgnore
	private IntMapWrapper bookAttr = new IntMapWrapper();
	@JsonIgnore
	private IntMapWrapper heavenlyDaoAttr = new IntMapWrapper();
	@JsonIgnore
	private IntMapWrapper potentialAttr = new IntMapWrapper();
	@JsonIgnore
	private Map<AttrCalcType, PlayerAttrCalc> playerAttrCalcMap = new HashMap<AttrCalcType, PlayerAttrCalc>();

	/** 战斗力 */
	private int power;

	/** 
	 * 计算所有属性，给客户端战斗时使用。
	 */
	public void calcAllAttr() {
		calcAlchemyAttr();
		calcDragonAttr();
		calcDragonSkillAttr();
		calcSwordAttr();
		calcWallAttr();

		calcHeroAttr();

		playerAttrCalcMap.forEach((k, v) -> {
			v.reCalcAttr();
		});
		logAllAttr();
//		log.info("calcAllAttr ： " + toString());
	}

	private void logAllAttr() {
		StringBuilder sb = new StringBuilder();
		sb.append("heroAttrs=").append(heroAttrs).append(" playerAttrs=").append(playerAttrCalcMap);
		log.debug("calcAllAttr ： " + sb.toString());
	}

	public PlayerBattleAttrs buildBattleAttrs() {
		cn.game.protocol.protobuf.BattleMsg.PlayerBattleAttrs.Builder builder = PlayerBattleAttrs.newBuilder();
		builder.putAllWallAttrs(wallAttr.getMap());

		for (Entry<Long, IntMapWrapper> entry : heroAttrs.entrySet()) {
			builder.addHeroAttrs(HeroAttr.newBuilder().setHeroUid(entry.getKey().toString()).putAllHeroAttrs(entry.getValue().getMap()));
		}

		IntMapWrapper dragon = new IntMapWrapper();
		builder.putAllDragonAttrs(dragon.addAll(dragonAttr.getMap()).addAll(dragonSkillAttr.getMap()).getMap());

		IntMapWrapper playerMap = getPlayerAttrMap();

		builder.putAllPlayerAttrs(playerMap.getMap());

		return builder.build();
	}

	public IntMapWrapper getPlayerAttrMap() {
		IntMapWrapper playerMap = new IntMapWrapper();
//		playerMap.addAll(swordAttr.getMap());
//		playerMap.addAll(alchemyAttr.getMap());

		playerAttrCalcMap.forEach((k, v) -> {
			playerMap.addAll(v.getAttrMap().getMap());
		});
		return playerMap;

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
			heroAttrs.put(uid, BattleHelper.makeHeroAttr(hero));
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
	public void initFromDbAfter() {
		Set<Class<?>> allModuleClass = ClassHelper.findSubclasses("cn.game.games", PlayerAttrCalc.class);
		for (Class<?> class1 : allModuleClass) {
			try {
				PlayerAttrCalc newInstance = (PlayerAttrCalc) class1.getDeclaredConstructor(Player.class).newInstance(player);
				PlayerAttrCalc put = playerAttrCalcMap.put(newInstance.getAttrCalcType(), newInstance);
				if (put != null) {
					log.error("重复的模块：" + newInstance.getAttrCalcType());
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	protected int getInitOrder() {
		return INIT_PRIORITY_LOW;
	}

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public Map<AttrCalcType, PlayerAttrCalc> getPlayerAttrCalcMap() {
		return playerAttrCalcMap;
	}
}
