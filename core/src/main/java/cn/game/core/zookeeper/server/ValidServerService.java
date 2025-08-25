package cn.game.core.zookeeper.server;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import cn.game.core.base.VirtualServerRegistry.VirtualServerView;
import cn.game.core.task.SchedulerService;
import cn.game.core.zookeeper.CacheChangeListener;
import cn.game.core.zookeeper.NodeChangeType;
import cn.game.core.zookeeper.ZkBackedCache;

/**
 * 业务侧的“有效服务器视图 + 开服调度器”：
 * - 有效服务器：开服时间 <= now 的服务器
 * - 当服务器到达开服时间时，触发一次 open 事件
 *
 * 职责边界：
 * - 仅依赖底层 ZkBackedCache<String, VirtualServerView>，不修改框架
 * - 使用 SchedulerService 进行时间调度
 *
 * 使用方法：
 *  1) 构造并注册监听：new ValidServerService(cache, zoneId)；建议在 cache.startAndWarmup() 之后
 *  2) service.initFromSnapshot()：重建定时器（重启后恢复）
 *  3) 注册开服监听：service.addOpenListener(s -> {...})
 *  4) 查询有效服：service.getValidServers()、getValidServerIdsByType("Game")
 *  5) 关闭：service.close()
 */
public class ValidServerService implements AutoCloseable {

	private final ZkBackedCache<String, VirtualServerView> cache;
	private final ZoneId zoneId;
	private final SchedulerService scheduler = SchedulerService.getInstance();

	// serverId -> 已安排的定时任务
	private final Map<String, ScheduledFuture<?>> openTimers = new ConcurrentHashMap<>();
	// serverId -> 已触发通知的 openTime（用来做幂等；若开服时间修改，允许再次触发）
	private final Map<String, LocalDateTime> firedOpenTime = new ConcurrentHashMap<>();
	// 业务方注册的开服监听
	private final List<Consumer<VirtualServerView>> openListeners = new ArrayList<>();

	private final CacheChangeListener<VirtualServerView> cacheListener = this::onCacheChange;

	/**
	 * @param cache  ZK 驱动的虚拟服视图缓存（键=serverId）
	 * @param zoneId 业务时间语义的时区（openTime 若是本地时间字符串，需要明确以哪个时区解释）
	 */
	public ValidServerService(ZkBackedCache<String, VirtualServerView> cache, ZoneId zoneId) {
		this.cache = Objects.requireNonNull(cache, "cache");
		this.zoneId = zoneId == null ? ZoneId.systemDefault() : zoneId;
		this.cache.addListener(cacheListener);
	}

	/**
	 * 从当前快照初始化定时器。
	 * 在 cache.startAndWarmup() 之后调用一次。
	 */
	public void initFromSnapshot() {
		LocalDateTime now = nowLocal();
		for (VirtualServerView s : cache.getAll()) {
			if (s == null)
				continue;
			LocalDateTime openTime = s.openTime;
			if (!isValidOpenTime(openTime))
				continue;

			if (openTime.isAfter(now)) {
				scheduleOpen(s.ID, openTime);
			} else {
				// 已经过了开服时间：可视为有效服
				// 若业务需要“补发一次开服事件”，可按需调用 emitOpen(s)；此处默认不补发，避免重复
				// 可以启用下行代码：
				// maybeEmitOpenOnce(s, openTime);
			}
		}
	}

	/**
	 * 查询所有“有效服务器”（开服时间 <= now）。
	 */
	public Map<String, VirtualServerView> getValidServers() {
		LocalDateTime now = nowLocal();
		return cache.getAll()
				.stream()
				.filter(Objects::nonNull)
				.filter(s -> isValidOpenTime(s.openTime) && !s.openTime.isAfter(now))
				.collect(Collectors.toMap(VirtualServerView::getID, s -> s, (a, b) -> a, LinkedHashMap::new));
	}

	
	@Deprecated
	public Set<String> getValidServerIdsByType(String serverType) {
		LocalDateTime now = nowLocal();
		return cache.getAll()
				.stream()
				.filter(Objects::nonNull)
//				.filter(s -> serverType.equalsIgnoreCase(getServerType(s)))
				.filter(s -> isValidOpenTime(s.openTime) && !s.openTime.isAfter(now))
				.map(VirtualServerView::getID)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * 注册开服监听。
	 * 触发时机：当某个服务器从“未到时间”到“到达开服时间”这一刻，会回调一次。
	 * 幂等：对相同的 serverId + openTime 只触发一次；若后续 openTime 改变，新的时间点会再次触发。
	 */
	public synchronized void addOpenListener(Consumer<VirtualServerView> l) {
		if (l != null)
			openListeners.add(l);
	}

	/**
	 * 可选：移除监听
	 */
	public synchronized void removeOpenListener(Consumer<VirtualServerView> l) {
		openListeners.remove(l);
	}

	@Override
	public void close() {
		cache.removeListener(cacheListener);
		// 取消已排定的任务
		for (ScheduledFuture<?> f : openTimers.values()) {
			try {
				f.cancel(false);
			} catch (Exception ignore) {
			}
		}
		openTimers.clear();
	}

	// ================= 内部：监听/调度 =================

	private void onCacheChange(NodeChangeType type, Object key, VirtualServerView s) {
		String serverId = String.valueOf(key);
		switch (type) {
		case NODE_DELETED -> {
			cancelTimer(serverId);
			firedOpenTime.remove(serverId);
		}
		case NODE_CREATED, NODE_CHANGED -> {
			if (s == null)
				return;
			LocalDateTime openTime = s.openTime;
			// 任意变更都重新安排：先取消旧的，再根据最新 openTime 决定是否排定或直接判定为有效
			cancelTimer(serverId);
			if (!isValidOpenTime(openTime)) {
				// 没有有效的开服时间，视为不可调度；也从 fired 表里清掉，避免后续设定时不触发
				firedOpenTime.remove(serverId);
				return;
			}
			LocalDateTime now = nowLocal();
			if (openTime.isAfter(now)) {
				scheduleOpen(serverId, openTime);
			} else {
				// 已到达开服时间：考虑幂等后触发一次
				maybeEmitOpenOnce(s, openTime);
			}
		}
		}
	}

	private void scheduleOpen(String serverId, LocalDateTime openTime) {
		long delay = Math.max(0L, toEpochMillis(openTime) - System.currentTimeMillis());
		ScheduledFuture<?> future = scheduler.scheduleTask(() -> {
			// 触发前再次读取最新值，确保一致性与幂等
			cache.getByKey(serverId).ifPresent(latest -> {
				LocalDateTime latestOpen = latest.openTime;
				LocalDateTime now = nowLocal();
				if (isValidOpenTime(latestOpen) && !latestOpen.isAfter(now)) {
					maybeEmitOpenOnce(latest, latestOpen);
				}
			});
		}, delay, TimeUnit.MILLISECONDS);
		openTimers.put(serverId, future);
	}

	private void cancelTimer(String serverId) {
		ScheduledFuture<?> f = openTimers.remove(serverId);
		if (f != null) {
			try {
				f.cancel(false);
			} catch (Exception ignore) {
			}
		}
	}

	private void maybeEmitOpenOnce(VirtualServerView s, LocalDateTime openTime) {
		String id = s.getID();
		LocalDateTime fired = firedOpenTime.get(id);
		if (fired != null && Objects.equals(fired, openTime)) {
			return; // 已对该开服时间触发过
		}
		firedOpenTime.put(id, openTime);
		// 回调
		List<Consumer<VirtualServerView>> listeners;
		synchronized (this) {
			listeners = new ArrayList<>(openListeners);
		}
		for (Consumer<VirtualServerView> l : listeners) {
			try {
				l.accept(s);
			} catch (Exception ignore) {
			}
		}
	}

	// ===== 时间工具 =====

	private LocalDateTime nowLocal() {
		return LocalDateTime.now(zoneId);
	}

	private boolean isValidOpenTime(LocalDateTime t) {
		return t != null;
	}

	private long toEpochMillis(LocalDateTime t) {
		ZonedDateTime zdt = t.atZone(zoneId);
		return zdt.toInstant().toEpochMilli();
	}
}