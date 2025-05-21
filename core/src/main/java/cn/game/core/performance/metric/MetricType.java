package cn.game.core.performance.metric;

/**
 * 指标类型枚举
 */
public enum MetricType {
	SYSTEM, // 系统基础指标（CPU、内存、磁盘）
	VERTX, // Vertx相关指标（事件循环、工作线程池）
	DATABASE, // 数据库相关指标（连接池、查询性能）
	CACHE, // 缓存相关指标（命中率、使用率）
	NETWORK, // 网络相关指标（带宽使用、连接数）
	JVM, // JVM相关指标（GC、线程）
	MESSAGE, // 消息系统指标（队列深度、处理率）
	BUSINESS, // 业务指标（在线用户、请求率）
	CUSTOM // 自定义指标
}