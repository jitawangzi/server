package cn.game.games.net.game.module.player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.ConditionManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.IntMapWrapper;
import cn.game.util.StringMapWrapper;

/**    
 * 独立的计数模块，用来记录各种计数变量
 * 这些计数的生命周期比较独立，是单独计算的，有自己固定的重置周期
 * 例如可以一直累计，也可以按天刷新，按周刷新等。 
 * 通常和条件表配合使用。 
 * 2025年7月24日 17:07:31
 * @author SYQ
 */
public class CountingModule extends BasePlayerModule {
	private static final EventTypeEnum[] events = initEventTypes();
	/** 刷新类型：  类型->数量*/
	private Map<Integer, IntMapWrapper> cumulativeCountMap = new HashMap<Integer, IntMapWrapper>();
	/** 刷新类型：  类型->数量，类型带额外参数的  */
	private Map<Integer, StringMapWrapper> cumulativeCountExtMap = new HashMap<Integer, StringMapWrapper>();
	
    private static EventTypeEnum[] initEventTypes() {
    	List<EventTypeEnum> eventTypeList = new java.util.ArrayList<>();
    	for (ConditionTypeEnum type : ConditionTypeEnum.values()) {
			if (type.countType == 2) {
				AbstractCondition cachedConditionClassInstance = ClassManager.getInstance().getCachedConditionClassInstance(type.ID);
				EventTypeEnum[] eventTypes = cachedConditionClassInstance.getEventTypes(); 
				Collections.addAll(eventTypeList, eventTypes) ; 
			}
		}
    	return eventTypeList.toArray(new EventTypeEnum[0]);
    }
    
	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
		case PLAYER_CREATE: {
			addCumulativeCount(ConditionTypeEnum.CumulativeLogins, 1);
			break;
		}
		case NewDay: {
			reset(1);
			
			addCumulativeCount(ConditionTypeEnum.CumulativeLogins, 1);
			break;
		}
		case NewWeek: {
			reset(2);
			break;
		}
		case NewMonth: {
			reset(3);
			break;
		}
		case WatchAds: {
			addCumulativeCount(ConditionTypeEnum.WatchAdsCumulation, 1);
			break;
		}
		case Charge: {
			addCumulativeCount(ConditionTypeEnum.AccumulatedRecharge, event.getIntParameter(0));
			addCumulativeCount(ConditionTypeEnum.RechargeCnt, 1);
			break;
		}
		case BattleEnd: {
			addCumulativeCount(ConditionTypeEnum.KillMonsters, event.getIntParameter(3));
			addCumulativeCount(ConditionTypeEnum.KillBoss, event.getIntParameter(4));
			break;
		}
		case HeroBreak: {
			int quality = event.getIntParameter(1);
			addCumulativeCount(ConditionTypeEnum.BreakHeroCumulation, 1);
			addCumulativeCount(ConditionTypeEnum.EarnHeroCumulation, 1, quality);
			break;
		}
		case Hero: {
			int id = event.getIntParameter(0);
			HeroConfig heroConfig = HeroManager.instance().get(id);
			addCumulativeCount(ConditionTypeEnum.EarnHeroCumulation, 1, heroConfig.InitialQuality);
			break;
		}
		case CostItem: {
			int id = event.getIntParameter(0);
			int count = event.getIntParameter(1);
			if (id == Asset.diamond.ID) {
				addCumulativeCount(ConditionTypeEnum.ConsumesDiamonds, count);
			}
			break;
		}
		case Patrol: {
			boolean isFast = event.getBoolParameter(0);
			if (isFast) {
				addCumulativeCount(ConditionTypeEnum.QuickHangUpCumulation, 1);
			}
			break;
		}
		case Draw: {
			int count = event.getIntParameter(0);
			int typeId = event.getIntParameter(1);
			if (typeId == 2) {
				addCumulativeCount(ConditionTypeEnum.SupremeGacha, count);
			}
			break;
		}
		default:
			// 其他事件不处理
			break;
		}
	}

	private void reset(int type) {
		IntMapWrapper intMapWrapper = cumulativeCountMap.get(type); 
		if (intMapWrapper != null) {
			intMapWrapper.clear();
		}
		StringMapWrapper stringMapWrapper = cumulativeCountExtMap.get(type); 
		if (stringMapWrapper != null) {
			stringMapWrapper.clear();
		}
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		
	}
	
	public void addCumulativeCount(ConditionTypeEnum type, int count) {
		cumulativeCountMap.forEach((k, v) -> {
			v.add(type.ID, count);
		});
	}

	public void addCumulativeCount(ConditionTypeEnum type, int count, int... ext) {
		if (ext == null || ext.length == 0) {
			addCumulativeCount(type, count);
		} else {
			cumulativeCountExtMap.forEach((k, v) -> {
				v.add(type.ID, count, ext);
			});
		}
	}
	

	/** 
	 * 获取某类型的累计数
	 * @param type
	 * @return
	 */
	public int getCumulativeCount(int condition) {
		ConditionConfig conditionConfig = ConditionManager.instance().get(condition);
		IntMapWrapper map = cumulativeCountMap.computeIfAbsent(conditionConfig.resetType, r -> new IntMapWrapper());
		return map.getValue(conditionConfig.type);
	}

	/** 
	 * 获取某类型的累计数 
	 * @param type
	 * @param ext 额外参数
	 * @return
	 */
	public int getCumulativeCount(int condition, int... ext) {
		
		if (ext == null ||  ext.length == 0) {
			return getCumulativeCount(condition);
		}
		ConditionConfig conditionConfig = ConditionManager.instance().get(condition); 
		StringMapWrapper map = cumulativeCountExtMap.computeIfAbsent(conditionConfig.resetType,r -> new StringMapWrapper()); 
		return map.getValue(conditionConfig.type, ext);
	}
	
	@Override
	public int processOrder() {
		return 100;
	}
}
