package cn.game.games.net.game.module.shop.firstcharge;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

/**    
 * 首冲礼包
 * @date 2024年4月22日 下午6:50:48
 * @author SYQ
 */
public class FirstChargeModule extends BasePlayerModule {
	private List<Integer> selectItemList = new ArrayList<>();
	/** 当前可以购买的首冲礼包id */
	private int curId;


	@Override
	public EventTypeEnum[] getEventTypes() {
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
	public void initFromDbAfter() {
		// TODO Auto-generated method stub

	}

}
