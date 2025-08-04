package cn.game.games.net.game.module.develop;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cn.game.games.net.cross.zongmen.ZongMenHelper;
import cn.game.games.net.game.module.zongmen.ZongMenHandler;
import cn.game.protocol.protobuf.ZongMenMsg;
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
	/** 武将属性 */
	@JsonIgnore
	private Map<Long, IntMapWrapper> heroAttrs = new HashMap<Long, IntMapWrapper>();
	/** 作用所有上阵的武将，也称为外围属性 */
	@JsonIgnore
	private Map<AttrCalcType, PlayerAttrCalc> playerAttrCalcMap = new HashMap<AttrCalcType, PlayerAttrCalc>();

	/** 战斗力 */
	private int power;

	/** 
	 * 计算所有属性，给客户端战斗时使用。
	 */
	public void calcAllAttr() {
	
		calcHeroAttr();

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

		for (Entry<Long, IntMapWrapper> entry : heroAttrs.entrySet()) {
			builder.addHeroAttrs(HeroAttr.newBuilder().setHeroUid(entry.getKey().toString()).putAllHeroAttrs(entry.getValue().getMap()));
		}

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

	@Override
	public EventTypeEnum[] getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

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

	public Map<Long, IntMapWrapper> getHeroAttrs() {
		return heroAttrs;
	}
}
