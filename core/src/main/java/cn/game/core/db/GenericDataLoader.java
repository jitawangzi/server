package cn.game.core.db;

import java.util.List;

public interface GenericDataLoader<T> {

	long getLastId(T t);

	List<T> getBatch(long lastId, int limit);

	Object getMapper();

	void processData(List<T> data);

}