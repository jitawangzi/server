package cn.game.core.task;

import java.util.List;

public class BatchProcessResult {
	private final int processedCount;
	private final List<BatchError> errors;

	public BatchProcessResult(int processedCount, List<BatchError> errors) {
		this.processedCount = processedCount;
		this.errors = errors;
	}

	public int getProcessedCount() {
		return processedCount;
	}

	public List<BatchError> getErrors() {
		return errors;
	}

	@Override
	public String toString() {
		return "BatchProcessResult [processedCount=" + processedCount + ", errors=" + errors + "]";
	}

	public static class BatchError {
		private final int offset;
		private final int itemIndex; // 在batch中的索引位置
		private final Throwable exception;
		private final String message;

		public BatchError(int offset, int itemIndex, Throwable exception, String message) {
			this.offset = offset;
			this.itemIndex = itemIndex;
			this.exception = exception;
			this.message = message;
		}

		public int getOffset() {
			return offset;
		}

		public int getItemIndex() {
			return itemIndex;
		}

		public Throwable getException() {
			return exception;
		}

		public String getMessage() {
			return message;
		}

	}
}
