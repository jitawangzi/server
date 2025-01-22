package cn.game.core.process;

import java.util.List;

import cn.game.core.util.BatchQueryUtil.BatchQuery;

public class PageQueryDataProvider<T> implements DataProvider<T> {
	private final BatchQuery<T> batchQuery;
	private final int batchSize;
	private int currentOffset;

	public PageQueryDataProvider(BatchQuery<T> batchQuery, int batchSize) {
		this.batchQuery = batchQuery;
		this.batchSize = batchSize;
		this.currentOffset = 0;
	}

	@Override
	public List<T> nextBatch() throws Exception {
		List<T> batch = batchQuery.query(currentOffset, batchSize);
		currentOffset += batchSize;
		return batch;
	}
}