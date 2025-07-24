package cn.game.games.net.game.module.player;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.protobuf.PlayerMsg;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.IntMapWrapper;
import cn.game.util.StringMapWrapper;

/**    
 * 独立的计数模块，用来记录各种计数变量
 * 可以包含累计的计数，需要重置的计数等
 * 2025年7月24日 17:07:31
 * @author SYQ
 */
public class CountingModule extends BasePlayerModule {
	private static transient EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE };

	/** 一些累计的计数， 类型->数量*/
	private IntMapWrapper cumulativeCountMap = new IntMapWrapper();
	/** 一些累计的计数,类型->数量 ，类型带额外参数的 */
	private StringMapWrapper cumulativeCountExtMap = new StringMapWrapper();
	
	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
		case PLAYER_CREATE: {
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + event);
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
		cumulativeCountMap.add(type.ID, count);
	}

	public void addCumulativeCount(ConditionTypeEnum type, int count, int... ext) {
		if (ext ==null ||  ext.length == 0) {
			addCumulativeCount(type, count);
		} else {
			cumulativeCountExtMap.add(type.ID, count, ext);
		}
	}
	

	/** 
	 * 获取某类型的累计数
	 * @param type
	 * @return
	 */
	public int getCumulativeCount(ConditionTypeEnum type) {
		return this.cumulativeCountMap.getValue(type.ID);
	}

	/** 
	 * 获取某类型的累计数 
	 * @param type
	 * @param ext 额外参数
	 * @return
	 */
	public int getCumulativeCount(ConditionTypeEnum type, int... ext) {
		if (ext!= null &&  ext.length > 0) {
			return this.cumulativeCountExtMap.getValue(type.ID, ext);
		}
		return getCumulativeCount(type);
	}
}
