package cn.game.games.net.game.gm.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.net.cross.activity.CrossActivityBase;
import cn.game.games.net.cross.activity.CrossGlobalActivityManager;
import cn.game.games.net.cross.activity.service.CrossActivityService;
import cn.game.games.net.game.gm.GmSecurityPolicy;
import cn.game.games.net.game.module.rank.RankEntry;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;

/**
 * 跨服与集群只读视角及非生产联调诊断服务（支持无 Redis/ZK 单机环境优雅降级）
 *
 * @author shuyongqiang
 */
public class CrossClusterDiagnosticsService {

	private static final Logger log = LoggerFactory.getLogger(CrossClusterDiagnosticsService.class);

	/**
	 * 获取跨服活动、排行榜类型与节点运行拓扑只读概览
	 *
	 * @return 集群与跨服概览 Map
	 */
	public Map<String, Object> getClusterOverview() {
		RunMode mode = GmSecurityPolicy.currentRunMode();
		String serverId = ServerContext.getInstance().getServerId();

		Collection<CrossActivityBase> crossActs = CrossGlobalActivityManager.getInstance().getAllActivities();
		List<Map<String, Object>> crossActList = new ArrayList<>(crossActs.size());
		for (CrossActivityBase act : crossActs) {
			if (act == null) {
				continue;
			}
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("uid", act.getUid());
			item.put("configId", act.getId());
			item.put("scopeKey", act.getScopeKey() != null ? act.getScopeKey() : "");
			item.put("state", act.getState());
			item.put("stateName", resolveActivityStateName(act.getState()));
			item.put("startTime", act.getStartTime());
			item.put("endTime", act.getEndTime());
			item.put("destroyTime", act.getDestroyTime());
			crossActList.add(item);
		}

		List<Map<String, Object>> rankTypes = new ArrayList<>();
		for (RankType rt : RankType.values()) {
			Map<String, Object> rtRow = new LinkedHashMap<>();
			rtRow.put("id", rt.ID);
			rtRow.put("name", rt.name());
			rtRow.put("desc", rt.Desc != null ? rt.Desc : "");
			rankTypes.add(rtRow);
		}

		Map<String, Object> overview = new LinkedHashMap<>();
		overview.put("serverId", serverId != null ? serverId : "");
		overview.put("runMode", mode.name());
		overview.put("production", mode.isProduction());
		overview.put("vertxReady", VxHolder.vertx != null);
		overview.put("crossActivityCount", crossActList.size());
		overview.put("crossActivities", crossActList);
		overview.put("rankTypes", rankTypes);
		return overview;
	}

	/**
	 * 执行非生产跨服/排行榜联调只读/调试动作
	 *
	 * @param op 操作名（如 rank.top / cross.activity.newday）
	 * @param params 参数 Map
	 * @return 执行结果 Map
	 */
	public Map<String, Object> executeDevAction(String op, Map<String, Object> params) {
		if (GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new IllegalStateException("生产环境严禁执行跨服联调动作");
		}
		String normalizedOp = op != null ? op.trim() : "";
		if ("rank.top".equalsIgnoreCase(normalizedOp)) {
			int rankTypeId = params != null && params.get("rankType") instanceof Number n ? n.intValue() : 1;
			int limit = params != null && params.get("limit") instanceof Number n ? Math.max(1, Math.min(50, n.intValue())) : 20;
			String serverId = params != null && params.get("serverId") != null ? String.valueOf(params.get("serverId")).trim() : ServerContext.getInstance().getServerId();
			RankType rankType = findRankType(rankTypeId);
			if (rankType == null) {
				return Map.of("ok", false, "message", "未知 RankType ID: " + rankTypeId);
			}
			try {
				List<RankEntry> entries = RankService.getInstance().getTopN(serverId != null ? serverId : "", rankType, limit);
				return Map.of("ok", true, "rankType", rankType.name(), "serverId", serverId != null ? serverId : "", "entries", entries != null ? entries : List.of());
			} catch (Throwable t) {
				log.warn("查询排行榜TopN失败（可能未连接Redis）: rankType={}", rankType, t);
				return Map.of("ok", true, "degraded", true, "message", "排行榜查询已降级（当前环境可能未连接Redis）: " + t.getMessage(), "entries", List.of());
			}
		}
		if ("cross.activity.newday".equalsIgnoreCase(normalizedOp)) {
			try {
				CrossActivityService.getInstance().init();
				return Map.of("ok", true, "message", "已触发 CrossActivityService 初始化与检查");
			} catch (Throwable t) {
				log.warn("触发 CrossActivityService 检查失败", t);
				return Map.of("ok", false, "message", "CrossActivityService 调用异常: " + t.getMessage());
			}
		}
		return Map.of("ok", false, "message", "不支持的跨服调试动作: " + normalizedOp);
	}

	private RankType findRankType(int id) {
		for (RankType rt : RankType.values()) {
			if (rt.ID == id) {
				return rt;
			}
		}
		return null;
	}

	private String resolveActivityStateName(int state) {
		switch (state) {
		case ActivityState.VIEW_VALUE:
			return "VIEW";
		case ActivityState.START_VALUE:
			return "START";
		case ActivityState.CLOSE_VALUE:
			return "CLOSE";
		default:
			return "NONE";
		}
	}
}
