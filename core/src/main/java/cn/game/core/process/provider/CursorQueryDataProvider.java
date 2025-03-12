package cn.game.core.process.provider;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CursorQueryDataProvider<T> implements DataProvider<T> {
	private final BiFunction<Object[], Integer, List<T>> queryFunction;
	private final Function<T, Object[]> extractor;
	private Object[] currentCursor;
	private final int batchSize;

	/**
	 * 构造函数
	 * @param queryFunction 查询函数，接收参数数组（包含游标和批次大小），返回数据列表
	 * @param extractor 从数据对象中提取下次查询参数的函数
	 * @param initialCursor 初始游标数组，用于第一次查询
	 * @param batchSize 每批数据的大小
	 */
	public CursorQueryDataProvider(BiFunction<Object[], Integer, List<T>> queryFunction, Function<T, Object[]> extractor, int batchSize,
			Object... initialCursor) {
		this.queryFunction = queryFunction;
		this.extractor = extractor;
		this.batchSize = batchSize;
		this.currentCursor = initialCursor;
	}

	@Override
	public List<T> nextBatch() throws Exception {
		if (currentCursor == null) {
			return Collections.emptyList(); // 如果游标为null，表示没有更多数据
		}

		// 使用查询参数获取下一批数据
		List<T> results = queryFunction.apply(currentCursor, batchSize);

		if (results.isEmpty()) {
			// 没有更多数据了
			currentCursor = null;
		} else {
			// 从最后一条数据中提取下一个游标数组
			T lastItem = results.get(results.size() - 1);
			currentCursor = extractor.apply(lastItem);

			// 如果提取的游标为null或空数组，表示没有更多数据
			if (currentCursor == null || currentCursor.length == 0) {
				currentCursor = null;
			}
		}

		return results;
	}
}