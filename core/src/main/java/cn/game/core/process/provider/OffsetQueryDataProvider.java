package cn.game.core.process.provider;

import java.util.List;

import cn.game.core.process.OffsetBatchQuery;


public class OffsetQueryDataProvider<T> implements DataProvider<T> {
	private final OffsetBatchQuery<T> batchQuery;
	private final int batchSize;
	private int currentOffset;

	public OffsetQueryDataProvider(OffsetBatchQuery<T> batchQuery, int batchSize) {
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