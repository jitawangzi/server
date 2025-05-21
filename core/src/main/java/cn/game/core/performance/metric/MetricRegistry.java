package cn.game.core.performance.metric;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 指标收集器注册中心，管理所有注册的指标收集器
 */
public class MetricRegistry {

	private static final MetricRegistry INSTANCE = new MetricRegistry();

	private final Map<String, MetricCollector> collectors = new ConcurrentHashMap<>();

	private MetricRegistry() {
	}

	public static MetricRegistry getInstance() {
		return INSTANCE;
	}

	/**
	 * 注册指标收集器
	 * @param collector 指标收集器
	 * @return 返回自身，支持链式调用
	 */
	public MetricRegistry register(MetricCollector collector) {
		collectors.put(collector.getName(), collector);
		collector.init();
		return this;
	}

	/**
	 * 注销指标收集器
	 * @param name 指标名称
	 */
	public void unregister(String name) {
		MetricCollector collector = collectors.remove(name);
		if (collector != null) {
			collector.shutdown();
		}
	}

	/**
	 * 获取指标收集器
	 * @param name 指标名称
	 * @return 指标收集器
	 */
	public MetricCollector getCollector(String name) {
		return collectors.get(name);
	}

	/**
	 * 获取所有指标收集器
	 * @return 所有指标收集器
	 */
	public Map<String, MetricCollector> getCollectors() {
		return collectors;
	}

	/**
	 * 收集所有指标
	 * @return 收集的指标值映射
	 */
	public Map<String, Double> collectAll() {
		Map<String, Double> results = new ConcurrentHashMap<>();
		collectors.forEach((name, collector) -> {
			results.put(name, collector.collect());
		});
		return results;
	}

	/**
	 * 初始化所有收集器
	 */
	public void initAll() {
		collectors.values().forEach(MetricCollector::init);
	}

	/**
	 * 关闭所有收集器
	 */
	public void shutdownAll() {
		collectors.values().forEach(MetricCollector::shutdown);
		collectors.clear();
	}
}