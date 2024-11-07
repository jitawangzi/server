package cn.game.util.db;

import java.util.List;
import java.util.function.Consumer;

public class BatchQueryUtil {

	public static <T> void processBatch(BatchQuery<T> batchQuery, Consumer<T> processor) {
		int batchSize = 100;
		int offset = 0;

		while (true) {
			List<T> batch = batchQuery.query(offset, batchSize);
			if (batch.isEmpty()) {
				break;
			}
//			for (T item : batch) {
//				processor.accept(item);
//			}
			batch.parallelStream().forEach(processor);

			offset += batchSize;
		}
	}

	public interface BatchQuery<T> {
		List<T> query(int offset, int limit);
	}
}