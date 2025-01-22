package cn.game.core.process;

public enum ProcessingMode {
	SEQUENTIAL, // 串行处理
	PARALLEL_BATCH, // 并行处理，一批完成后处理下一批
	/** 完全并行处理，所有批次同时处理 */
	PARALLEL_ALL,
	/** 异步处理，需要异步处理器， 一批完成后处理下一批 */
	ASYNC_BATCH
}