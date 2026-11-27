package cn.game.games.net.game.gm.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.game.core.base.ServerContext;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.core.exception.LogicException;
import cn.game.core.net.process.Processor;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.net.cross.activity.CrossActivityBase;
import cn.game.games.net.cross.activity.CrossGlobalActivityManager;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.gm.GmAction;
import cn.game.games.net.game.gm.GmGroup;
import cn.game.games.net.game.gm.GmParam;
import cn.game.games.net.game.gm.GmScope;
import cn.game.games.net.game.gm.GmSecurityTier;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.activity.ActivityBase;
import cn.game.games.net.game.module.activity.ActivityModule;
import cn.game.games.net.game.module.activity.PlayerActivityManager;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.util.DateUtil;
import cn.game.util.SpringContextLoader;

/**
 * 活动生命周期与全服定时周期事件调试 GM 指令分组
 *
 * @author shuyongqiang
 */
@Component
@GmGroup(name = "activity", description = "活动生命周期与定时周期事件调试指令")
public class ActivityGm extends AbstractGm {

	private static final Logger log = LoggerFactory.getLogger(ActivityGm.class);

	/**
	 * 查询全服活动配置与实时运行状态（合并全局状态、跨服实例与指定玩家个人状态）
	 *
	 * @param playerId 目标玩家 ID（0 表示仅查看全局与跨服状态）
	 * @param keyword 可选关键字（按活动 ID 或类型名称过滤）
	 * @param onlyActive 是否仅显示当前非 NONE 状态的活动
	 * @return 活动状态总表
	 */
	@GmAction(name = "status", description = "查询全服活动状态总表（含全局/跨服/玩家个人状态）", security = GmSecurityTier.OPS_SAFE, scope = GmScope.GLOBAL, idempotent = true, readOnly = true, example = "activity.status playerId=0 onlyActive=false")
	public Map<String, Object> status(
			@GmParam(description = "可选目标在线玩家ID", required = false, defaultValue = "0") long playerId,
			@GmParam(description = "按活动ID或类型名关键字过滤", required = false, defaultValue = "") String keyword,
			@GmParam(description = "是否仅返回开启或展示中的活动", required = false, defaultValue = "false") boolean onlyActive) {
		Player targetPlayer = playerId > 0 ? PlayerManager.getInstance().getPlayer(playerId) : null;
		Map<Integer, long[]> playerActivitySnapshot = capturePlayerActivityStates(targetPlayer);

		ActivityStateManager globalStateMgr = ActivityStateManager.getInstance();
		Collection<CrossActivityBase> crossActs = CrossGlobalActivityManager.getInstance().getAllActivities();
		Map<Integer, Integer> crossStateByConfigId = new LinkedHashMap<>();
		for (CrossActivityBase crossAct : crossActs) {
			if (crossAct != null) {
				crossStateByConfigId.put(crossAct.getId(), crossAct.getState());
			}
		}

		Collection<ActivityConfig> configs = ActivityManager.instance() != null ? ActivityManager.instance().list() : List.of();
		String kw = keyword != null ? keyword.trim().toLowerCase(Locale.ROOT) : "";

		List<Map<String, Object>> rows = new ArrayList<>();
		int activePlayerCount = 0;
		int activeGlobalCount = 0;
		int activeCrossCount = 0;

		for (ActivityConfig cfg : configs) {
			if (cfg == null) {
				continue;
			}
			String typeName = resolveActivityTypeName(cfg.type);
			if (!kw.isEmpty()) {
				String hay = (cfg.ID + " " + cfg.type + " " + typeName).toLowerCase(Locale.ROOT);
				if (!hay.contains(kw)) {
					continue;
				}
			}

			int globalState = globalStateMgr.getState(cfg.ID);
			int crossState = crossStateByConfigId.getOrDefault(cfg.ID, ActivityState.NONE_VALUE);
			long[] pInfo = playerActivitySnapshot != null ? playerActivitySnapshot.get(cfg.ID) : null;
			int playerState = pInfo != null ? (int) pInfo[0] : ActivityState.NONE_VALUE;
			long playerStartTime = pInfo != null ? pInfo[1] : 0L;
			long playerEndTime = pInfo != null ? pInfo[2] : 0L;

			if (playerState != ActivityState.NONE_VALUE) {
				activePlayerCount++;
			}
			if (globalState != ActivityState.NONE_VALUE) {
				activeGlobalCount++;
			}
			if (crossState != ActivityState.NONE_VALUE) {
				activeCrossCount++;
			}

			if (onlyActive && playerState == ActivityState.NONE_VALUE
					&& globalState == ActivityState.NONE_VALUE
					&& crossState == ActivityState.NONE_VALUE) {
				continue;
			}

			Map<String, Object> row = new LinkedHashMap<>();
			row.put("id", cfg.ID);
			row.put("type", cfg.type);
			row.put("typeName", typeName);
			row.put("disable", cfg.disable);
			row.put("isCross", cfg.isCross);
			row.put("isMultiplayer", cfg.isMultiplayer);
			row.put("openType", cfg.openType);
			row.put("openParam", cfg.openParam);
			row.put("resetType", cfg.resetType);
			row.put("globalState", globalState);
			row.put("globalStateName", stateLabel(globalState));
			row.put("crossState", crossState);
			row.put("crossStateName", stateLabel(crossState));
			row.put("playerState", playerState);
			row.put("playerStateName", playerActivitySnapshot != null ? stateLabel(playerState) : "N/A");
			row.put("playerStartTime", playerStartTime);
			row.put("playerEndTime", playerEndTime);
			rows.add(row);
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("nowStr", DateUtil.getStringDate());
		result.put("playerId", playerId);
		result.put("playerOnline", targetPlayer != null);
		result.put("total", rows.size());
		result.put("activeGlobalCount", activeGlobalCount);
		result.put("activeCrossCount", activeCrossCount);
		result.put("activePlayerCount", activePlayerCount);
		result.put("activities", rows);
		return result;
	}

	private Map<Integer, long[]> capturePlayerActivityStates(Player targetPlayer) {
		if (targetPlayer == null) {
			return null;
		}
		if (SpringContextLoader.getContext() != null) {
			try {
				Processor processor = ServerContext.getInstance().getProcessor();
				if (processor != null) {
					CompletableFuture<Map<Integer, long[]>> fut = new CompletableFuture<>();
					processor.process(targetPlayer.getPlayerId(), () -> {
						try {
							fut.complete(readActivityStatesDirect(targetPlayer));
						} catch (Throwable t) {
							fut.completeExceptionally(t);
						}
					});
					return fut.get(3, TimeUnit.SECONDS);
				}
			} catch (Throwable t) {
				log.warn("通过玩家队列读取活动状态失败，降级直读 playerId={}", targetPlayer.getPlayerId(), t);
			}
		}
		return readActivityStatesDirect(targetPlayer);
	}

	private Map<Integer, long[]> readActivityStatesDirect(Player targetPlayer) {
		ActivityModule module = targetPlayer.getActivityModule();
		PlayerActivityManager mgr = module != null ? module.getPlayerActivityManager() : null;
		if (mgr == null) {
			return null;
		}
		Map<Integer, long[]> snapshot = new LinkedHashMap<>();
		Collection<ActivityConfig> configs = ActivityManager.instance() != null ? ActivityManager.instance().list() : List.of();
		for (ActivityConfig cfg : configs) {
			if (cfg == null) {
				continue;
			}
			int state = mgr.getState(cfg.ID);
			ActivityBase act = mgr.get(cfg.ID);
			snapshot.put(cfg.ID, new long[] { state, act != null ? act.getStartTime() : 0L, act != null ? act.getEndTime() : 0L });
		}
		return snapshot;
	}

	/**
	 * 为目标玩家开启指定活动（force=true 时会清除一次性活动屏蔽记录并尝试重开）
	 *
	 * @param player 目标在线玩家
	 * @param id 活动配置 ID
	 * @param force 是否忽略一次性领取记录强制重开
	 * @return 开启前后状态摘要
	 */
	@GmAction(name = "open", description = "为目标玩家开启指定活动", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER, sideEffects = "直接修改玩家活动状态", example = "activity.open id=1001 force=true")
	public Map<String, Object> open(
			Player player,
			@GmParam(description = "活动配置ID") int id,
			@GmParam(description = "是否清理一次性记录并强制重开", required = false, defaultValue = "true") boolean force) {
		ActivityModule module = requireActivityModule(player, id);
		PlayerActivityManager mgr = module.getPlayerActivityManager();
		int before = mgr.getState(id);
		if (force) {
			mgr.clearDisposableIdForGm(id);
		}
		module.open(id, player.getGameClient() != null);
		int after = mgr.getState(id);

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("playerId", player.getPlayerId());
		result.put("activityId", id);
		result.put("beforeState", before);
		result.put("afterState", after);
		result.put("afterStateName", stateLabel(after));
		result.put("opened", after != ActivityState.NONE_VALUE);
		return result;
	}

	/**
	 * 结束玩家指定活动（进入领奖/展示期 CLOSE）
	 *
	 * @param player 目标在线玩家
	 * @param id 活动配置 ID
	 * @return 操作前后状态摘要
	 */
	@GmAction(name = "end", description = "结束目标玩家的指定活动（进入CLOSE态）", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER, sideEffects = "直接结束玩家活动并保留数据")
	public Map<String, Object> end(Player player, @GmParam(description = "活动配置ID") int id) {
		ActivityModule module = requireActivityModule(player, id);
		PlayerActivityManager mgr = module.getPlayerActivityManager();
		int before = mgr.getState(id);
		module.end(id);
		int after = mgr.getState(id);
		return Map.of("playerId", player.getPlayerId(), "activityId", id, "beforeState", before, "afterState", after, "afterStateName", stateLabel(after));
	}

	/**
	 * 彻底销毁玩家指定活动（移除数据并进入 NONE）
	 *
	 * @param player 目标在线玩家
	 * @param id 活动配置 ID
	 * @return 操作前后状态摘要
	 */
	@GmAction(name = "destroy", description = "销毁目标玩家的指定活动并删除活动数据", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER, sideEffects = "彻底销毁玩家活动数据")
	public Map<String, Object> destroy(Player player, @GmParam(description = "活动配置ID") int id) {
		ActivityModule module = requireActivityModule(player, id);
		PlayerActivityManager mgr = module.getPlayerActivityManager();
		int before = mgr.getState(id);
		module.destroy(id, player.getGameClient() != null);
		int after = mgr.getState(id);
		return Map.of("playerId", player.getPlayerId(), "activityId", id, "beforeState", before, "afterState", after, "afterStateName", stateLabel(after));
	}

	/**
	 * 重置玩家指定活动（清空历史与一次性记录后重新开启）
	 *
	 * @param player 目标在线玩家
	 * @param id 活动配置 ID
	 * @return 重置结果摘要
	 */
	@GmAction(name = "reset", description = "重置目标玩家的指定活动并重新开启", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER, sideEffects = "销毁并重开目标玩家活动")
	public Map<String, Object> reset(Player player, @GmParam(description = "活动配置ID") int id) {
		ActivityModule module = requireActivityModule(player, id);
		PlayerActivityManager mgr = module.getPlayerActivityManager();
		int before = mgr.getState(id);
		mgr.clearDisposableIdForGm(id);
		module.reset(id);
		int after = mgr.getState(id);
		return Map.of("playerId", player.getPlayerId(), "activityId", id, "beforeState", before, "afterState", after, "afterStateName", stateLabel(after));
	}

	/**
	 * 手动触发全服定时周期事件（NewDay/NewWeek/NewMonth/活动开启结束销毁事件）
	 *
	 * @param eventType 服务器事件枚举名
	 * @param activityId 可选活动 ID（触发 Activity*Time 事件时必传）
	 * @return 触发结果摘要
	 */
	@GmAction(name = "triggerEvent", description = "手动触发全服定时周期或活动生命周期事件", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL, sideEffects = "向全服事件总线广播周期事件", example = "activity.triggerEvent eventType=NewDay")
	public Map<String, Object> triggerEvent(
			@GmParam(description = "事件类型: NewDay/NewWeek/NewMonth/ActivityOpenTime/ActivityShutDownTime/ActivityDestoryTime") String eventType,
			@GmParam(description = "可选活动ID(活动类事件使用)", required = false, defaultValue = "0") int activityId) {
		if (eventType == null || eventType.isBlank()) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "eventType 不能为空");
		}
		ServerEventTypeEnum targetEnum = null;
		for (ServerEventTypeEnum item : ServerEventTypeEnum.values()) {
			if (item.name().equalsIgnoreCase(eventType.trim())) {
				targetEnum = item;
				break;
			}
		}
		if (targetEnum == null) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "未知 ServerEventTypeEnum: " + eventType);
		}
		if ((targetEnum == ServerEventTypeEnum.ActivityOpenTime
				|| targetEnum == ServerEventTypeEnum.ActivityShutDownTime
				|| targetEnum == ServerEventTypeEnum.ActivityDestoryTime) && activityId <= 0) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "活动生命周期事件需要提供大于0的 activityId");
		}

		try {
			if (ServerContext.getInstance().getEventBus() != null) {
				if (activityId > 0) {
					ServerContext.getInstance().fireEvent(targetEnum, activityId);
				} else {
					ServerContext.getInstance().fireEvent(targetEnum);
				}
			} else {
				if (activityId > 0) {
					ServerEventBus.getInstance().dispatch(targetEnum, activityId);
				} else {
					ServerEventBus.getInstance().dispatch(targetEnum);
				}
			}
		} catch (Throwable t) {
			log.error("触发全服周期事件失败: eventType={}, activityId={}", targetEnum, activityId, t);
			throw new LogicException(ErrorMsgEnum.unknown.ID, "触发周期事件异常: " + t.getMessage());
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("eventType", targetEnum.name());
		result.put("eventDesc", targetEnum.getDesc());
		result.put("activityId", activityId);
		result.put("dispatched", true);
		return result;
	}

	private ActivityModule requireActivityModule(Player player, int activityId) {
		if (player == null) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "目标玩家不存在");
		}
		ActivityConfig cfg = ActivityManager.instance() != null ? ActivityManager.instance().getNullable(activityId) : null;
		if (cfg == null) {
			throw new LogicException(ErrorMsgEnum.config_data_not_found.ID, "活动配置不存在: " + activityId);
		}
		ActivityModule module = player.getActivityModule();
		if (module == null || module.getPlayerActivityManager() == null) {
			throw new LogicException(ErrorMsgEnum.unknown.ID, "玩家活动模块尚未初始化");
		}
		return module;
	}

	private String resolveActivityTypeName(int type) {
		for (ActivityTypeEnum en : ActivityTypeEnum.values()) {
			if (en.ID == type) {
				return en.name();
			}
		}
		return "Type#" + type;
	}

	private String stateLabel(int state) {
		switch (state) {
		case ActivityState.VIEW_VALUE:
			return "VIEW(预告)";
		case ActivityState.START_VALUE:
			return "START(进行中)";
		case ActivityState.CLOSE_VALUE:
			return "CLOSE(结束/领奖)";
		default:
			return "NONE(未开启)";
		}
	}
}
