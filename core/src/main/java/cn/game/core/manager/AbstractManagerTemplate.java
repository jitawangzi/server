package cn.game.core.manager;

import java.util.Collection;

/**
 * 抽象管理器模板基类
 */
public abstract class AbstractManagerTemplate<ID, T> {
	// === 存储操作抽象方法 ===
	protected abstract void doAdd(ID id, T obj, String... labels);

	protected abstract T doGet(ID id);

	protected abstract Collection<T> doGetByLabels(String... labels);

	protected abstract Collection<T> doGetAll();

	protected abstract boolean doRemove(ID id);

	protected abstract void doClear();

	// === 基础操作模板方法 ===
	public final void add(ID id, T obj, String... labels) {
		if (obj == null || id == null) {
			throw new IllegalArgumentException("Object or id cannot be null");
		}
		beforeAdd(obj);
		doAdd(id, obj, labels);
		afterAdd(obj);
	}

	public final T get(ID id) {
		return doGet(id);
	}

	public final Collection<T> getByLabels(String... labels) {
		return doGetByLabels(labels);
	}

	public final Collection<T> getAll() {
		return doGetAll();
	}

	public final boolean remove(ID id) {
		T obj = get(id);
		if (obj != null) {
			beforeRemove(obj);
			boolean result = doRemove(id);
			if (result) {
				afterRemove(obj);
			}
			return result;
		}
		return false;
	}

	// === 钩子方法 ===
	protected void beforeAdd(T obj) {
	}

	protected void afterAdd(T obj) {
	}

	protected void beforeRemove(T obj) {
	}

	protected void afterRemove(T obj) {
	}
}



