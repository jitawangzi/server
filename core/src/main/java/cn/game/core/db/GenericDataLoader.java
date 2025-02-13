package cn.game.core.db;

import java.util.List;

public interface GenericDataLoader<T> {

	int getTotal();

	List<T> getBatch(int offset, int limit);

	Object getMapper();

	void processData(List<T> data);

}