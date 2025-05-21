package cn.game.core.performance.metric;

/**
 * 指标收集器接口，所有具体指标收集器都需实现此接口
 */
public interface MetricCollector {

	/**
	 * 获取指标名称
	 * @return 指标名称
	 */
	String getName();

	/**
	 * 获取指标类型
	 * @return 指标类型
	 */
	MetricType getType();

	/**
	 * 收集指标
	 * @return 当前值，范围在0.0-1.0之间
	 */
	double collect();

	/**
	 * 获取最新指标值
	 * @return 最新收集的值，不进行新的采集
	 */
	double getLatestValue();

	/**
	 * 获取警告阈值
	 * @return 警告阈值
	 */
	double getWarningThreshold();

	/**
	 * 获取严重阈值
	 * @return 严重阈值
	 */
	double getCriticalThreshold();

	/**
	 * 初始化收集器
	 */
	void init();

	/**
	 * 关闭收集器
	 */
	void shutdown();
}