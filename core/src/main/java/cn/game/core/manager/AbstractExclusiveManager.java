package cn.game.core.manager;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 独占存储实现基类
 */
public abstract class AbstractExclusiveManager<ID, T> extends AbstractManagerTemplate<ID, T> {
	private final ConcurrentHashMap<ID, T> storage = new ConcurrentHashMap<>();

	@Override
	protected void doAdd(ID id, T obj, String... labels) {
		storage.put(id, obj);
	}

	@Override
	protected T doGet(ID id) {
		return storage.get(id);
	}

	// ... 其他独占实现方法
}