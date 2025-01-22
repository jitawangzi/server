package cn.game.core.process;

import cn.game.core.task.BatchProcessResult;

public interface DataProcessor<T> {
	/**
	 * 处理一批数据
	 * @param dataProvider 数据提供者
	 * @param config 处理配置
	 * @return 处理结果
	 */
	BatchProcessResult process(DataProvider<T> dataProvider, ProcessingConfig config);
}