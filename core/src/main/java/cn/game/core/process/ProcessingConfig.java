package cn.game.core.process;

import java.util.concurrent.ExecutorService;


public class ProcessingConfig {

	private ProcessingMode mode = ProcessingMode.SEQUENTIAL;
	private BatchCompletionMode batchCompletionMode = BatchCompletionMode.ALL_COMPLETE;
	private int parallelism = Runtime.getRuntime().availableProcessors();
	private int batchSize = 100;
	private boolean continueOnError = false;
	private ExecutorService executor = null;


	public static ProcessingConfig defaultConfig() {
		return new ProcessingConfig();
	}

	public ProcessingMode getMode() {
		return mode;
	}

	public ProcessingConfig setMode(ProcessingMode mode) {
		this.mode = mode;
		return this;
	}

	public BatchCompletionMode getBatchCompletionMode() {
		return batchCompletionMode;
	}

	public ProcessingConfig setBatchCompletionMode(BatchCompletionMode batchCompletionMode) {
		this.batchCompletionMode = batchCompletionMode;
		return this;
	}

	public int getParallelism() {
		return parallelism;
	}

	public ProcessingConfig setParallelism(int parallelism) {
		this.parallelism = parallelism;
		return this;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public boolean isContinueOnError() {
		return continueOnError;
	}

	public ProcessingConfig setContinueOnError(boolean continueOnError) {
		this.continueOnError = continueOnError;
		return this;
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	public ProcessingConfig setExecutor(ExecutorService executor) {
		this.executor = executor;
		return this;
	}

}
