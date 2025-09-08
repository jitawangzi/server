package cn.game.games.net.game.module.shop.limitedtimegift;

import java.util.function.Consumer;

import com.google.common.primitives.Ints;

import cn.game.games.net.game.module.quest.Condition;
import cn.game.games.net.game.module.quest.ConditionContainer;
import cn.game.protocol.generated.config.LimitedTimeGiftConfig;
import cn.game.protocol.generated.manager.LimitedTimeGiftManager;

public class LimitedTimeGift {
	private int id ; 
	private long playerId; //玩家id
	
	private transient ConditionContainer conditionContainer;

	public void initCondition(Consumer<Condition> finishAction) {
		if (conditionContainer == null) {
			conditionContainer = new ConditionContainer();
		}
		LimitedTimeGiftConfig limitedTimeGiftConfig = LimitedTimeGiftManager.instance().get(id); 

		conditionContainer.create(playerId, Ints.asList(limitedTimeGiftConfig.UnlockCondition), false, null,
				null, finishAction, null);
		regEvent();
	}

	public void regEvent() {
		conditionContainer.regEvent();
	}
	public void unregEvent() {
		conditionContainer.unregEvent();
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}
	
}
