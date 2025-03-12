package cn.game.core.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 独占模式实现（管理所有对象）
 */
@Component
@Profile("exclusive")
class ExclusiveGenericManager<ID, T> implements GenericManager<ID, T> {

	// 主存储结构：ID到对象的映射
	private final Map<ID, T> storage = new ConcurrentHashMap<>();

	@Override
	public void initialize() {
		// 示例：从数据库加载数据
		// storage.putAll(database.loadAll());
	}

	@Override
	public void addWithLabels(ID id, T obj, String... labelValues) {
		if (labelValues.length > 0) {
			throw new UnsupportedOperationException("独占模式不支持标签存储");
		}
		storage.put(id, obj);
	}

	@Override
	public Collection<T> getByExactLabels(String... labelValues) {
		if (labelValues.length > 0) {
			throw new UnsupportedOperationException("独占模式不支持标签查询");
		}
		return getAll();
	}

	@Override
	public Collection<T> getByPartialLabels(String... labelValues) {
		if (labelValues.length > 0) {
			throw new UnsupportedOperationException("独占模式不支持标签查询");
		}
		return getAll();
	}

	@Override
	public T get(ID id) {
		return storage.get(id);
	}

	@Override
	public Collection<T> getAll() {
		return Collections.unmodifiableCollection(storage.values());
	}

	@Override
	public boolean remove(ID id) {
		return storage.remove(id) != null;
	}

	@Override
	public void clear() {
		storage.clear();
	}
}