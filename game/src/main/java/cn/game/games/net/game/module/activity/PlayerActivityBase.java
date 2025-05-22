package cn.game.games.net.game.module.activity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.core.event.ServerEventTypeEnum;
import cn.game.core.net.protocol.object.ObjectProtocol;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.core.event.server.ServerEvent;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.protobuf.ActivityMsg.ActivityStatePush_11100006;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.ServerType;

/**
 * 玩家自己的活动
 * 
 * 2021年6月9日 下午12:15:20
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
	public void init(int id, Object owner, boolean isNew) {
		this.player = (Player) owner;
		player.registerEventHandler(this);
		super.init(id, null, isNew);
	}

	/** 
	 * 发送给全体活动，或者跨服活动
	 */
	protected void publishEvent(PlayerEvent event) {
		ActivityConfig activityConfig = ActivityManager.instance().get(id);
		if (!activityConfig.isMultiplayer) {
			return;
		}
		// 如果跨服
		if (activityConfig.isCross) {
			if (event.isSendToCross()) {
				return;
			}
			event.setSendToCross(true);
			// TODO 找到目标服务器
			VxHolder.sendRemoteServer(ServerType.Cross, new ObjectProtocol(PbProtocol.GamePlayerEventPush_7d010100, event));
		} else {
			ServerEventBus.getInstance().dispatch(new ServerEvent(ServerEventTypeEnum.PlayerEvent, event.getParams()));
		}
	}

	@Override
	public void unregisterEvent() {
		player.getPlayerEventBus().unregister(this);
	}
	@Override
	public void handleEvent(PlayerEvent event) {
	}
}
