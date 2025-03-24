package cn.game.core.manager;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理器配置类，用于控制启用哪些功能
 */
public class ManagerConfig {
	// 容量控制
	private boolean capacityLimitEnabled = false;
	private int maxCapacity = Integer.MAX_VALUE;

	// 过期控制
	private boolean expiryEnabled = false;

	// 事件通知
	private boolean eventNotificationEnabled = false;

	// 淘汰策略
	private boolean evictionEnabled = false;
	private AbstractManagerTemplate.EvictionPolicy evictionPolicy = AbstractManagerTemplate.EvictionPolicy.LEAST_RECENTLY_USED;

	// 访问统计
	private boolean accessStatsEnabled = false;

	// 额外特性
	private final Map<String, Boolean> extraFeatures = new HashMap<>();

	// 构建器模式
	public static class Builder {
		private final ManagerConfig config = new ManagerConfig();

		public Builder withCapacityLimit(int maxCapacity) {
			config.capacityLimitEnabled = true;
			config.maxCapacity = maxCapacity;
			return this;
		}

		public Builder withExpiry() {
			config.expiryEnabled = true;
			return this;
		}

		public Builder withEventNotification() {
			config.eventNotificationEnabled = true;
			return this;
		}

		public Builder withEviction(AbstractManagerTemplate.EvictionPolicy policy) {
			config.evictionEnabled = true;
			config.evictionPolicy = policy;
			return this;
		}

		public Builder withAccessStats() {
			config.accessStatsEnabled = true;
			return this;
		}

		public Builder withExtraFeature(String featureName, boolean enabled) {
			config.extraFeatures.put(featureName, enabled);
			return this;
		}

		public ManagerConfig build() {
			// 如果启用了淘汰策略，默认也需要启用访问统计
			if (config.evictionEnabled && !config.accessStatsEnabled) {
				if (config.evictionPolicy != AbstractManagerTemplate.EvictionPolicy.RANDOM
						&& config.evictionPolicy != AbstractManagerTemplate.EvictionPolicy.FIRST_IN_FIRST_OUT) {
					config.accessStatsEnabled = true;
				}
			}
			return config;
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	// 预定义的配置
	public static ManagerConfig minimal() {
		return new Builder().build();
	}

	public static ManagerConfig standard() {
		return new Builder().withCapacityLimit(10000)
				.withExpiry()
				.withEviction(AbstractManagerTemplate.EvictionPolicy.LEAST_RECENTLY_USED)
				.build();
	}

	public static ManagerConfig full() {
		return new Builder().withCapacityLimit(10000)
				.withExpiry()
				.withEventNotification()
				.withEviction(AbstractManagerTemplate.EvictionPolicy.LEAST_RECENTLY_USED)
				.withAccessStats()
				.build();
	}

	// Getters
	public boolean isCapacityLimitEnabled() {
		return capacityLimitEnabled;
	}

	public int getMaxCapacity() {
		return maxCapacity;
	}

	public boolean isExpiryEnabled() {
		return expiryEnabled;
	}

	public boolean isEventNotificationEnabled() {
		return eventNotificationEnabled;
	}

	public boolean isEvictionEnabled() {
		return evictionEnabled;
	}

	public AbstractManagerTemplate.EvictionPolicy getEvictionPolicy() {
		return evictionPolicy;
	}

	public boolean isAccessStatsEnabled() {
		return accessStatsEnabled;
	}

	public boolean isFeatureEnabled(String featureName) {
		return extraFeatures.getOrDefault(featureName, false);
	}
}