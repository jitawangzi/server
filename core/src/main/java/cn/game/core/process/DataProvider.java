package cn.game.core.process;

import java.util.List;

public interface DataProvider<T> {
	/**
	 * 获取下一批数据
	 * @return 数据列表，如果没有更多数据返回空列表
	 */
	List<T> nextBatch() throws Exception;
}