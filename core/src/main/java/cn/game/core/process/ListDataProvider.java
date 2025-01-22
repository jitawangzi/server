package cn.game.core.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ListDataProvider<T> implements DataProvider<T> {
	private final Iterator<List<T>> batchIterator;

	public ListDataProvider(List<T> data, int batchSize) {
		List<List<T>> batches = new ArrayList<>();
		for (int i = 0; i < data.size(); i += batchSize) {
			batches.add(data.subList(i, Math.min(i + batchSize, data.size())));
		}
		this.batchIterator = batches.iterator();
	}

	@Override
	public List<T> nextBatch() {
		return batchIterator.hasNext() ? batchIterator.next() : Collections.emptyList();
	}
}