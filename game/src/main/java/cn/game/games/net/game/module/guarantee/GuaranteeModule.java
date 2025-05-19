package cn.game.games.net.game.module.guarantee;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.protocol.generated.config.GuaranteeConfig;
import cn.game.protocol.generated.enume.GuaranteeTypeEnum;
import cn.game.protocol.generated.manager.GuaranteeManager;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

/**    
 * 一些保底相关的计数
 * 2025年5月16日 15:21:58
 * @author SYQ
 */
public class GuaranteeModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.HeroRecruit };

	private Map<Integer, Guarantee> guarantees = new HashMap<>();
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public Guarantee get(GuaranteeTypeEnum type) {
		return guarantees.computeIfAbsent(type.ID, k -> {
			Guarantee guarantee = new Guarantee();
			List<GuaranteeConfig> typeRoundList = GuaranteeManager.instance().getTypeRoundList(type.ID, guarantee.getRound());
			guarantee.setId(typeRoundList.get(0).ID);
			return guarantee;
		});
	}

	public int add(GuaranteeTypeEnum type) {
		return add(type, 1);
	}

	/** 
	 * 
	 * @param type
	 * @param count
	 * @return 如果触发了保底，返回触发保底时的配置表id
	 */
	public int add(GuaranteeTypeEnum type, int count) {
		Guarantee guarantee = get(type);
		return guarantee.addCount(count);
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		case HeroRecruit: {
			add(GuaranteeTypeEnum.DrawRefresh);
			break;
		}
		}
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {

	}

}
