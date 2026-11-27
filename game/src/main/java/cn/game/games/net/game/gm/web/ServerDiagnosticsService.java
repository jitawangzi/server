package cn.game.games.net.game.gm.web;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.ConfigQueryService;
import cn.game.games.net.game.gm.GmAuditLogService;
import cn.game.games.net.game.gm.GmDispatcher;
import cn.game.games.net.game.gm.GmSecurityPolicy;
import cn.game.games.net.game.gm.RequestTraceService;
import cn.game.games.net.game.gm.VirtualClockService;
import cn.game.games.net.game.gm.acceptance.ModuleAcceptanceEngine;
import cn.game.games.net.game.gm.proto.VirtualGameClient;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.util.DateUtil;
import cn.game.util.SpringContextLoader;
import cn.game.util.file.WatchServiceManager;

/**
 * 服务端生产级只读运行态诊断服务：聚合 JVM 内存/GC/线程、在线客户端构成、配表版本与审计统计
 *
 * @author shuyongqiang
 */
public class ServerDiagnosticsService {

	private static final Logger log = LoggerFactory.getLogger(ServerDiagnosticsService.class);

	/**
	 * 采集当前服务器只读运行态健康快照
	 *
	 * @return 运行态健康指标 Map
	 */
	public Map<String, Object> buildHealthSnapshot() {
		RunMode mode = GmSecurityPolicy.currentRunMode();
		Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("serverId", ServerContext.getInstance().getServerId());
		snapshot.put("runMode", mode.name());
		snapshot.put("production", mode.isProduction());
		snapshot.put("physicalTime", VirtualClockService.getInstance().getStatus().getPhysicalTimeText());
		snapshot.put("virtualClock", VirtualClockService.getInstance().getStatusMap());
		snapshot.put("resourceVersion", WatchServiceManager.getInstance().resourceVersion());

		// 1. JVM 与运行时指标
		RuntimeMXBean runtimeMx = ManagementFactory.getRuntimeMXBean();
		MemoryMXBean memMx = ManagementFactory.getMemoryMXBean();
		ThreadMXBean threadMx = ManagementFactory.getThreadMXBean();
		MemoryUsage heap = memMx.getHeapMemoryUsage();
		MemoryUsage nonHeap = memMx.getNonHeapMemoryUsage();

		Map<String, Object> jvm = new LinkedHashMap<>();
		jvm.put("uptimeMs", runtimeMx.getUptime());
		jvm.put("availableProcessors", Runtime.getRuntime().availableProcessors());
		jvm.put("heapUsedMb", toMb(heap.getUsed()));
		jvm.put("heapCommittedMb", toMb(heap.getCommitted()));
		jvm.put("heapMaxMb", heap.getMax() > 0 ? toMb(heap.getMax()) : -1L);
		jvm.put("nonHeapUsedMb", toMb(nonHeap.getUsed()));
		jvm.put("threadCount", threadMx.getThreadCount());
		jvm.put("peakThreadCount", threadMx.getPeakThreadCount());
		jvm.put("daemonThreadCount", threadMx.getDaemonThreadCount());

		List<Map<String, Object>> gcList = new ArrayList<>();
		for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
			Map<String, Object> gcRow = new LinkedHashMap<>();
			gcRow.put("name", gc.getName());
			gcRow.put("count", gc.getCollectionCount());
			gcRow.put("timeMs", gc.getCollectionTime());
			gcList.add(gcRow);
		}
		jvm.put("gc", gcList);
		snapshot.put("jvm", jvm);

		// 2. 玩家与会话构成
		int totalManaged = 0;
		int onlineCount = 0;
		int realWsCount = 0;
		int virtualClientCount = 0;
		int logoutingCount = 0;
		for (Player p : PlayerManager.getInstance().getAllPlayer().values()) {
			if (p == null) {
				continue;
			}
			totalManaged++;
			if (p.isIslogouting()) {
				logoutingCount++;
			}
			if (p.isOnline() && !p.isIslogouting()) {
				onlineCount++;
				if (p.getGameClient() instanceof VirtualGameClient) {
					virtualClientCount++;
				} else if (p.getGameClient() != null) {
					realWsCount++;
				}
			}
		}
		Map<String, Object> playersSummary = new LinkedHashMap<>();
		playersSummary.put("managedCount", totalManaged);
		playersSummary.put("onlineCount", onlineCount);
		playersSummary.put("realWsClientCount", realWsCount);
		playersSummary.put("virtualClientCount", virtualClientCount);
		playersSummary.put("logoutingCount", logoutingCount);
		snapshot.put("players", playersSummary);

		// 3. 平台组件就绪态与统计
		Map<String, Object> components = new LinkedHashMap<>();
		boolean springReady = SpringContextLoader.getContext() != null;
		boolean processorReady = false;
		if (springReady) {
			try {
				processorReady = ServerContext.getInstance().getProcessor() != null;
			} catch (Throwable t) {
				log.debug("检查Processor状态异常", t);
			}
		}
		components.put("springReady", springReady);
		components.put("vertxReady", VxHolder.vertx != null);
		components.put("processorReady", processorReady);
		components.put("gmActionCount", GmDispatcher.getInstance().getSchemaCatalog(mode).getActions().size());
		try {
			components.put("loadedConfigTableCount", new ConfigQueryService().listTables().size());
		} catch (Throwable t) {
			log.warn("统计已加载配置表数量异常", t);
			components.put("loadedConfigTableCount", 0);
		}
		components.put("acceptanceSuiteCount", ModuleAcceptanceEngine.getInstance().listSuiteSummaries().size());
		components.put("auditBufferedCount", GmAuditLogService.getInstance().bufferedCount());
		components.put("auditFile", GmAuditLogService.getInstance().resolveAuditFilePath().toString());
		snapshot.put("components", components);

		// 4. 非生产 Trace 状态摘要（生产模式保持短路仅展示关闭态）
		if (mode.isProduction()) {
			snapshot.put("trace", Map.of("enabled", false, "productionMuted", true));
		} else {
			snapshot.put("trace", RequestTraceService.getInstance().status());
		}

		return snapshot;
	}

	private long toMb(long bytes) {
		return Math.max(0L, bytes / (1024L * 1024L));
	}
}
