package cn.game.games.net.game.module.activity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;

/**
 * 玩家自己的活动
 * 
 * @date 2021年6月9日 下午12:15:20
 * @author SYQ
 */
public abstract class PlayerActivityBase extends ActivityBase {
	@JsonIgnore
	protected transient Player player;

	public void syncActivityState(int id) {
		ActivityInfo.Builder builder = ActivityInfo.newBuilder();
		builder.setId(id);

		builder.setStateValue(state);
		if (state == ActivityState.VIEW_VALUE) {
			int openTimeRemaining = ActivityStateManager.getInstance().getOpenTimeRemaining(id);
			builder.setStartTime(openTimeRemaining);
		}

	}

	@Override
	public void init(int id, Player player, boolean isNew) {
		this.player = player;
		super.init(id, null, isNew);
	}

	@Override
	public void handleEvent(GameEvent event) {
	}
}
