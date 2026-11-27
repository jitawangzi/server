package cn.game.games.net.cross.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import cn.game.games.cache.entity.GameActivity;

/**
 * 内存跨服活动存储桩（支持计数与全状态断言）
 * 
 * @author shuyongqiang
 */
public class InMemoryCrossActivityStore implements CrossActivityStore {

	private final Map<Long, GameActivity> store = new ConcurrentHashMap<>();
	private final AtomicInteger saveCount = new AtomicInteger();
	private final AtomicInteger deleteCount = new AtomicInteger();
	private final AtomicInteger loadCount = new AtomicInteger();
	/** 故障注入开关：为 true 时强制 saveOrThrow 抛出 IllegalStateException 模拟落盘失败 */
	private volatile boolean failNextSave;

	@Override
	public List<GameActivity> loadAll() {
		loadCount.incrementAndGet();
		return new ArrayList<>(store.values());
	}

	@Override
	public List<GameActivity> selectListByScopeAndConfig(String scopeKey, int configId) {
		return store.values().stream()
				.filter(a -> a.getConfigId() == configId && (scopeKey == null || scopeKey.equals(a.getServerId())))
				.collect(Collectors.toList());
	}

	@Override
	public void save(GameActivity activity) {
		saveCount.incrementAndGet();
		store.put(activity.getId(), activity);
	}

	/**
	 * 模拟强制落盘：当故障注入开关打开时直接抛 {@link IllegalStateException} 且不写入内存存储与计数
	 *
	 * @param activity 待保存的跨服活动实体
	 */
	@Override
	public void saveOrThrow(GameActivity activity) {
		if (failNextSave) {
			long uid = activity != null ? activity.getId() : 0L;
			throw new IllegalStateException("Simulated saveOrThrow failure for uid=" + uid);
		}
		save(activity);
	}

	@Override
	public void delete(long activityUid) {
		deleteCount.incrementAndGet();
		store.remove(activityUid);
	}

	public void clear() {
		store.clear();
		saveCount.set(0);
		deleteCount.set(0);
		loadCount.set(0);
		failNextSave = false;
	}

	/**
	 * 设置下一次 {@link #saveOrThrow(GameActivity)} 是否注入故障抛出异常
	 *
	 * @param failNextSave true 表示触发失败异常，false 表示恢复正常写入
	 */
	public void setFailNextSave(boolean failNextSave) {
		this.failNextSave = failNextSave;
	}

	/**
	 * 获取当前故障注入开关状态
	 *
	 * @return true 表示故障注入开启
	 */
	public boolean isFailNextSave() {
		return failNextSave;
	}

	public int getSaveCount() {
		return saveCount.get();
	}

	public int getDeleteCount() {
		return deleteCount.get();
	}

	public int getLoadCount() {
		return loadCount.get();
	}

	public Map<Long, GameActivity> getStore() {
		return store;
	}
}
