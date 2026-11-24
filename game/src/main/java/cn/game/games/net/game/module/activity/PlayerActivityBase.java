package cn.game.games.net.game.module.activity;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.Message;

import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.cache.id.IdCache;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.core.net.protocol.object.ObjectProtocol;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.core.event.PlayerEventHandler;
import cn.game.games.core.event.server.ServerEvent;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.net.common.module.activity.DefaultActivityScopeResolver;
import cn.game.games.net.cross.activity.CrossActivityDirectory;
import cn.game.games.net.cross.activity.RedisCrossActivityDirectory;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.module.quest.Quest;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.ActivityMsg.ActivityStatePush_11100006;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.ServerType;
import io.vertx.core.Future;

/**
 * 玩家自己的活动
 * 
 * 2021年6月9日 下午12:15:20
 * @author shuyongqiang
 */
public abstract class PlayerActivityBase extends ActivityBase implements PlayerEventHandler {
	@JsonIgnore
	protected transient Player player;

	/** 跨服活动唯一实例 ID 绑定 */
	protected long crossActivityUid;

	public long getCrossActivityUid() {
		return crossActivityUid;
	}

	public void setCrossActivityUid(long crossActivityUid) {
		this.crossActivityUid = crossActivityUid;
	}

	/**
	 * 确保跨服活动 UID 绑定（覆盖登录恢复、展示构建、不查询直接写协议 3 类入口）
	 */
	public void ensureCrossActivityBinding() {
		ActivityConfig config = ActivityManager.instance().getNullable(id);
		if (config == null || !config.isCross) {
			return;
		}
		String serverId = player != null ? player.getServerId() : null;
		String scopeKey = DefaultActivityScopeResolver.getInstance().resolveScopeKey(config, serverId);
		CrossActivityDirectory directory = RedisCrossActivityDirectory.getInstance();
		Optional<Long> activeOpt = directory.getActiveUid(config.ID, scopeKey);
		if (activeOpt.isPresent()) {
			long activeUid = activeOpt.get();
			if (crossActivityUid == 0L) {
				crossActivityUid = activeUid;
				onCrossActivityBound(activeUid);
			} else if (crossActivityUid != activeUid) {
				long oldUid = crossActivityUid;
				crossActivityUid = activeUid;
				onCrossPeriodChanged(oldUid, activeUid);
			}
		}
	}

	/**
	 * 跨服路由失败或活动不存在时的失效与自愈重试
	 */
	public void handleCrossRouteFailure(Exception e) {
		log.warn("Cross activity route failed: id={}, currentUid={}, err={}", id, crossActivityUid, e != null ? e.getMessage() : "null");
		if (crossActivityUid <= 0) {
			return;
		}
		ActivityConfig config = ActivityManager.instance().getNullable(id);
		if (config == null || !config.isCross) {
			return;
		}
		String serverId = player != null ? player.getServerId() : null;
		String scopeKey = DefaultActivityScopeResolver.getInstance().resolveScopeKey(config, serverId);
		CrossActivityDirectory directory = RedisCrossActivityDirectory.getInstance();
		Optional<Long> activeOpt = directory.getActiveUid(config.ID, scopeKey);
		if (activeOpt.isPresent()) {
			long activeUid = activeOpt.get();
			if (activeUid != crossActivityUid) {
				long oldUid = crossActivityUid;
				crossActivityUid = activeUid;
				onCrossPeriodChanged(oldUid, activeUid);
			} else {
				// 目录仍为该 uid，但路由失败，说明本地 IdCache L1 路由缓存失效，主动驱逐本地缓存
				var idManager = IdCache.getManager(DistributedObjectType.CROSS_ACTIVITY);
				if (idManager != null) {
					idManager.invalidateServerId(crossActivityUid);
				}
			}
		} else {
			long oldUid = crossActivityUid;
			crossActivityUid = 0L;
			onCrossActivityExpired(oldUid);
		}
	}

	/**
	 * 首次绑定跨服活动实例钩子
	 */
	protected void onCrossActivityBound(long activeUid) {
	}

	/**
	 * 跨服活动换期钩子（清理/归档旧期个人状态）
	 */
	protected void onCrossPeriodChanged(long oldUid, long newUid) {
	}

	/**
	 * 跨服活动到期销毁钩子
	 */
	protected void onCrossActivityExpired(long oldUid) {
	}

	/** 
	 * 当前活动是否有红点显示
	 * @return
	 */
	public boolean hasRed() {
		return false;
	}

	public abstract Message buildActivityShowInfo();

	public Message buildActivityShowInfo(int id) {
		ensureCrossActivityBinding();
		return buildActivityShowInfo();
	}

	public abstract List<RewardInfo> receive(int id);

	@Deprecated
	public Future<List<RewardInfo>> asyncReceive(int id) {
		return Future.succeededFuture(receive(id));
	}

	/**
	 * 是否能够领取活动奖励
	 * @param ids 要领取的id集合
	 * @return 错误码
	 */
	@Deprecated
	public int canReceive(List<Integer> ids) {
		return ErrorMsgEnum.ok.ID;
	}

	@Deprecated
	public int canReceive(List<Integer> ids, List<Integer> rewardIdList) {
		int failSize = 0;
		int errCode = 0;
		QuestModule questModule = player.getQuestModule();
		for (int taskId : ids) {
			if (rewardIdList.contains(taskId)) {
				failSize++;
				errCode = ErrorMsgEnum.activity_lei_chong_has_reward.ID;
				continue;
			}
			Quest quest = questModule.get(taskId);
			if (quest.getState() != QuestHelper.CAN_GIVEWARD) {
				failSize++;
				errCode = ErrorMsgEnum.activity_task_not_finish.ID;
				continue;
			}
		}
		if (failSize == ids.size()) {
			return errCode;
		}
		return ErrorMsgEnum.ok.ID;
	}

	public void syncActivityInfo() {
		player.getGameClient().sendProtocol(ActivityStatePush_11100006.newBuilder().setActivity(buildActivityInfo()).build());
	}

	@Override
	public void init(int id, Object owner, boolean isNew) {
		this.player = (Player) owner;
		super.init(id, owner, isNew);
		ensureCrossActivityBinding();
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
	public void registerEvent() {
		player.getPlayerEventBus().register(this);
	}

	@Override
	public void unregisterEvent() {
		if (player != null) {
			player.getPlayerEventBus().unregister(this);
		}
	}

	@Override
	public void handleEvent(PlayerEvent event) {
	}
}
