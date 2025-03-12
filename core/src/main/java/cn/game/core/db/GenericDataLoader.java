package cn.game.core.db;

import java.util.List;

public interface GenericDataLoader<T> {

	long getTotal();

	List<T> getBatch(long lastId, int limit);

	Object getMapper();

	void processData(List<T> data);

}