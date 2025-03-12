package cn.game.core.manager;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 分片存储实现基类
 */
public abstract class AbstractShardedManager<ID, T> extends AbstractManagerTemplate<ID, T> {
	private final ConcurrentHashMap<String, Object> shardedStorage = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<ID, T> idMapping = new ConcurrentHashMap<>();

	@Override
	protected void doAdd(ID id, T obj, String... labels) {
		// 分片存储通用实现
		ConcurrentHashMap<String, Object> current = shardedStorage;
		for (int i = 0; i < labels.length - 1; i++) {
			current = (ConcurrentHashMap<String, Object>) current.computeIfAbsent(labels[i], k -> new ConcurrentHashMap<>());
		}

		ConcurrentHashMap<ID, T> leaf = (ConcurrentHashMap<ID, T>) current.computeIfAbsent(labels[labels.length - 1],
				k -> new ConcurrentHashMap<>());

		leaf.put(id, obj);
		idMapping.put(id, obj);
	}

	@Override
	protected T doGet(ID id) {
		return idMapping.get(id);
	}

	// ... 其他分片实现方法
}