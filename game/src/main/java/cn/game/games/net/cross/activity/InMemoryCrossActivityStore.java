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
