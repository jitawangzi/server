package cn.game.games.net.game.module.develop;

import java.util.Collection;
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
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.module.develop.attr.AttrCalcType;
import cn.game.games.net.game.module.develop.attr.PlayerAttrCalc;
import cn.game.protocol.protobuf.BattleMsg.HeroAttr;
import cn.game.protocol.protobuf.BattleMsg.PlayerBattleAttrs;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.FloatMapWrapper;
import cn.game.util.IntMapWrapper;
import cn.game.util.reflect.ClassHelper;

/**    
 * 各种属性的计算
 * 2024年4月15日 下午6:29:59
 * @author SYQ
 */
@JsonIgnoreType
public class AttrModule extends BasePlayerModule {

	private static EventTypeEnum[] events = new EventTypeEnum[] {EventTypeEnum.PLAYER_CREATE, EventTypeEnum.LoginFinish };
	/** 武将属性 */
	@JsonIgnore
	private Map<Long, FloatMapWrapper> heroAttrs = new HashMap<Long, FloatMapWrapper>();
	/** 作用所有上阵的武将，也称为外围属性 */
	@JsonIgnore
	private Map<AttrCalcType, PlayerAttrCalc> playerAttrCalcMap = new HashMap<AttrCalcType, PlayerAttrCalc>();

	/** 战斗力 */
	private long power;

	/** 
	 * 计算所有属性，给客户端战斗时使用。
	 */
	public void calcAllAttr() {
	
//		calcHeroAttr();

		playerAttrCalcMap.forEach((k, v) -> {
			v.reCalcAttr();
		});
		logAllAttr();
	}

	private void logAllAttr() {
		if (log.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append("heroAttrs=").append(heroAttrs).append(" playerAttrs=").append(playerAttrCalcMap);
			log.debug("calcAllAttr ： " + sb.toString());
		}
	}

	public PlayerBattleAttrs buildBattleAttrs() {
		cn.game.protocol.protobuf.BattleMsg.PlayerBattleAttrs.Builder builder = PlayerBattleAttrs.newBuilder();

		for (Entry<Long, FloatMapWrapper> entry : heroAttrs.entrySet()) {
			Map<Integer, Float> map = entry.getValue().getMap(); 
			Map<Integer, Integer> mapIntMap = new HashMap<>();
			map.forEach((k, v) -> mapIntMap.put(k, v.intValue()));
			builder.addHeroAttrs(HeroAttr.newBuilder().setHeroUid(entry.getKey().toString()).putAllHeroAttrs(mapIntMap));
		}

		FloatMapWrapper playerMap = getPlayerAttrMap();
		IntMapWrapper intMapWrapper = new IntMapWrapper(); 
		playerMap.getMap().forEach((k, v) -> intMapWrapper.add(k, v.intValue()));
		
		builder.putAllPlayerAttrs(intMapWrapper.getMap());

		return builder.build();
	}

	public FloatMapWrapper getPlayerAttrMap() {
		FloatMapWrapper playerMap = new FloatMapWrapper();
//		playerMap.addAll(swordAttr.getMap());
//		playerMap.addAll(alchemyAttr.getMap());
		calcAllAttr();
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

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
		case PLAYER_CREATE: {
			initPlayerAttrMapInstance(); 
			calcPower();
			break;
		}
//		case LoginFinish: {
//			calcAllAttr();
//			break;
//		}
		default:
			break;
		}
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public int processOrder() {
		return EVENT_PROCESS_ORDER_LOW;
	}

	@Override
	public void initFromDbAfter() {
		initPlayerAttrMapInstance();
	}

	private void initPlayerAttrMapInstance() {
		if (!playerAttrCalcMap.isEmpty()) {
			return ; 
		}
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

	public long getPower() {
		return power;
	}
	public Map<AttrCalcType, PlayerAttrCalc> getPlayerAttrCalcMap() {
		return playerAttrCalcMap;
	}

	public Map<Long, FloatMapWrapper> getHeroAttrs() {
		return heroAttrs;
	}
	
	/** 
	 * 主角攻击力=攻击力之和*（1+攻击力加成之和）

1.装备：6件装备的基础属性攻击力、附加属性中的攻击力、强化攻击力
2.宝石：镶嵌在装备上的宝石中，带有攻击力的部分
3.图鉴：图鉴增加的攻击属性
	 */
	public long calcPower() {
		FloatMapWrapper playerAttrMap = getPlayerAttrMap(); 
		float baseAttack = playerAttrMap.getValue(4);
		float attackAdd = playerAttrMap.getValue(5);
		long allCombat = (long) (baseAttack * (1 + attackAdd/10000f));

		this.power = allCombat;
		return allCombat; 
	}
	
}
