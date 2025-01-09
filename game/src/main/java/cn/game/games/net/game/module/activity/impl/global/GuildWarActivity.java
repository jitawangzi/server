package cn.game.games.net.game.module.activity.impl.global;

import java.util.List;

import com.google.protobuf.Message;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.MultiPlayerActivityBase;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

@ActivityType(type = ActivityTypeEnum.Test)
public class GuildWarActivity extends MultiPlayerActivityBase {
//	private Map<Integer, GuildWarData> guildDataMap = new ConcurrentHashMap<>();

	@Override
	protected void onPlayerJoin(long playerId) {
//		int guildId = player.getGuildId();
//		guildDataMap.computeIfAbsent(guildId, k -> new GuildWarData());
		// 处理玩家加入逻辑
	}

	@Override
	protected void onPlayerLeave(long playerId) {
		// 处理玩家离开逻辑
	}

//	@Override
//	public boolean canJoin(Player player) {
//		return player.hasGuild() && super.canJoin(player);
//	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleEvent(GameEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public Message buildActivityShowInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unregisterEvent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void syncActivityInfo() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<RewardInfo> receive(int id) {
		// TODO Auto-generated method stub
		return null;
	}
}