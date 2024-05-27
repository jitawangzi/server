package cn.game.games.net.game.module.activity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.GameEvent;
import cn.game.protocol.protobuf.ActivityMsg.ActivityStatePush_11100006;

/**
 * 玩家自己的活动
 * 
 * @date 2021年6月9日 下午12:15:20
 * @author SYQ
 */
public abstract class PlayerActivityBase extends ActivityBase {
	@JsonIgnore
	protected transient Player player;

	@Override
	public void syncActivityInfo() {
		player.getGameClient().sendProtocol(ActivityStatePush_11100006.newBuilder().setActivity(buildActivityInfo()).build());
	}

	@Override
	public void init(int id, Player player, boolean isNew) {
		this.player = player;
		player.registerEventHandler(getEventTypes(), this);
		super.init(id, null, isNew);
	}

	@Override
	public void handleEvent(GameEvent event) {
	}
}
