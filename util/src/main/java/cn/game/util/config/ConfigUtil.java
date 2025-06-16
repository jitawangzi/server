package cn.game.util.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置工具类，支持从系统属性和环境变量获取配置
 * 支持多种命名风格的自动转换和缓存
 * 
 * @version 1.0
 */
public class ConfigUtil {

	/**
	 * 配置缓存，避免重复转换和查找
	 */
	private static final Map<String, String> CONFIG_CACHE = new ConcurrentHashMap<>();

	/**
	 * 是否启用缓存，默认启用
	 */
	private static volatile boolean cacheEnabled = true;

	/**
	 * 配置来源枚举
	 */
	public enum ConfigSource {
		SYSTEM_PROPERTY("系统属性"), ENVIRONMENT_VARIABLE("环境变量"), DEFAULT_VALUE("默认值"), NOT_FOUND("未找到");

		private final String description;

		ConfigSource(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}

	/**
	 * 配置结果类，包含值和来源信息
	 */
	public static class ConfigResult {
		private final String value;
		private final ConfigSource source;
		private final String actualKey;

		public ConfigResult(String value, ConfigSource source, String actualKey) {
			this.value = value;
			this.source = source;
			this.actualKey = actualKey;
		}

		public String getValue() {
			return value;
		}

		public ConfigSource getSource() {
			return source;
		}

		public String getActualKey() {
			return actualKey;
		}

		public boolean isFound() {
			return source != ConfigSource.NOT_FOUND;
		}

		@Override
		public String toString() {
			return String.format("ConfigResult{value='%s', source=%s, actualKey='%s'}", value, source.getDescription(), actualKey);
		}
	}

	/**
	 * 获取配置值，支持智能命名风格转换
	 * @param key 配置key（支持点分隔格式，如：log4j2.config.path）
	 * @param defaultValue 默认值
	 * @return 配置值
	 */
	public static String getConfig(String key, String defaultValue) {
		ConfigResult result = getConfigWithSource(key, defaultValue);
		return result.getValue();
	}

	/**
	 * 获取配置值，无默认值
	 * @param key 配置key
	 * @return 配置值，如果未找到返回null
	 */
	public static String getConfig(String key) {
		return getConfig(key, null);
	}

	/**
	 * 获取配置值及其来源信息
	 * @param key 配置key
	 * @param defaultValue 默认值
	 * @return 配置结果，包含值和来源信息
	 */
	public static ConfigResult getConfigWithSource(String key, String defaultValue) {
		if (key == null || key.trim().isEmpty()) {
			throw new IllegalArgumentException("配置key不能为空");
		}

		String cacheKey = key + ":" + defaultValue;

		// 检查缓存
		if (cacheEnabled && CONFIG_CACHE.containsKey(cacheKey)) {
			String cachedValue = CONFIG_CACHE.get(cacheKey);
			ConfigSource source = cachedValue.equals(defaultValue) ? ConfigSource.DEFAULT_VALUE
					: (System.getProperty(key) != null ? ConfigSource.SYSTEM_PROPERTY : ConfigSource.ENVIRONMENT_VARIABLE);
			return new ConfigResult(cachedValue, source, key);
		}

		ConfigResult result = findConfig(key, defaultValue);

		// 缓存结果
		if (cacheEnabled && result.isFound()) {
			CONFIG_CACHE.put(cacheKey, result.getValue());
		}

		return result;
	}

	/**
	 * 查找配置值
	 */
	private static ConfigResult findConfig(String key, String defaultValue) {
		// 生成所有可能的key变体
		List<String> keyVariants = generateKeyVariants(key);

		// 1. 优先从系统属性查找
		for (String variant : keyVariants) {
			String value = System.getProperty(variant);
			if (isValidValue(value)) {
				return new ConfigResult(value.trim(), ConfigSource.SYSTEM_PROPERTY, variant);
			}
		}

		// 2. 从环境变量查找
		for (String variant : keyVariants) {
			String value = System.getenv(variant);
			if (isValidValue(value)) {
				return new ConfigResult(value.trim(), ConfigSource.ENVIRONMENT_VARIABLE, variant);
			}
		}

		// 3. 使用默认值
		if (defaultValue != null) {
			return new ConfigResult(defaultValue, ConfigSource.DEFAULT_VALUE, key);
		}

		return new ConfigResult(null, ConfigSource.NOT_FOUND, key);
	}

	/**
	 * 生成key的各种命名风格变体
	 * @param originalKey 原始key
	 * @return key变体列表，按优先级排序
	 */
	private static List<String> generateKeyVariants(String originalKey) {
		List<String> variants = new ArrayList<>();

		// 1. 原始key（最高优先级）
		variants.add(originalKey);

		// 2. 大写下划线风格（环境变量常用）
		variants.add(originalKey.toUpperCase().replace('.', '_').replace('-', '_'));

		// 3. 小写下划线风格
		variants.add(originalKey.toLowerCase().replace('.', '_').replace('-', '_'));

		// 4. 横线分隔风格
		variants.add(originalKey.toLowerCase().replace('.', '-').replace('_', '-'));

		// 5. 大写横线分隔风格
		variants.add(originalKey.toUpperCase().replace('.', '-').replace('_', '-'));

		// 6. 去除所有分隔符的小写版本
		variants.add(originalKey.toLowerCase().replaceAll("[._-]", ""));

		// 7. 去除所有分隔符的大写版本
		variants.add(originalKey.toUpperCase().replaceAll("[._-]", ""));

		// 去重，保持顺序
		return new ArrayList<>(new LinkedHashSet<>(variants));
	}

	/**
	 * 检查值是否有效（非空且非纯空白）
	 */
	private static boolean isValidValue(String value) {
		return value != null && !value.trim().isEmpty();
	}

	/**
	 * 获取整型配置
	 * @param key 配置key
	 * @param defaultValue 默认值
	 * @return 整型值
	 */
	public static int getIntConfig(String key, int defaultValue) {
		String value = getConfig(key);
		if (value == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			System.err.println("警告: 配置项 " + key + " 的值 '" + value + "' 不是有效的整数，使用默认值: " + defaultValue);
			return defaultValue;
		}
	}

	public static int getIntConfig(String key) {
		return getIntConfig(key, 0);
	}

	/**
	 * 获取长整型配置
	 */
	public static long getLongConfig(String key, long defaultValue) {
		String value = getConfig(key);
		if (value == null) {
			return defaultValue;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			System.err.println("警告: 配置项 " + key + " 的值 '" + value + "' 不是有效的长整数，使用默认值: " + defaultValue);
			return defaultValue;
		}
	}

	public static long getLongConfig(String key) {
		return getLongConfig(key, 0L);
	}

	/**
	 * 获取布尔型配置
	 * @param key 配置key
	 * @param defaultValue 默认值
	 * @return 布尔值，支持 true/false, yes/no, 1/0, on/off（不区分大小写）
	 */
	public static boolean getBooleanConfig(String key, boolean defaultValue) {
		String value = getConfig(key);
		if (value == null) {
			return defaultValue;
		}

		String normalizedValue = value.trim().toLowerCase();
		switch (normalizedValue) {
		case "true":
		case "yes":
		case "1":
		case "on":
		case "enabled":
			return true;
		case "false":
		case "no":
		case "0":
		case "off":
		case "disabled":
			return false;
		default:
			System.err.println("警告: 配置项 " + key + " 的值 '" + value + "' 不是有效的布尔值，使用默认值: " + defaultValue);
			return defaultValue;
		}
	}

	public static boolean getBooleanConfig(String key) {
		return getBooleanConfig(key, false);
	}

	/**
	 * 获取双精度浮点型配置
	 */
	public static double getDoubleConfig(String key, double defaultValue) {
		String value = getConfig(key);
		if (value == null) {
			return defaultValue;
		}
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			System.err.println("警告: 配置项 " + key + " 的值 '" + value + "' 不是有效的浮点数，使用默认值: " + defaultValue);
			return defaultValue;
		}
	}

	/**
	 * 检查配置是否存在
	 * @param key 配置key
	 * @return 是否存在
	 */
	public static boolean hasConfig(String key) {
		return getConfigWithSource(key, null).isFound();
	}

	/**
	 * 获取所有匹配的配置项（用于调试）
	 * @param keyPrefix key前缀
	 * @return 匹配的配置项映射
	 */
	public static Map<String, String> getAllConfigs(String keyPrefix) {
		Map<String, String> result = new HashMap<>();

		// 从系统属性获取
		Properties systemProps = System.getProperties();
		for (String key : systemProps.stringPropertyNames()) {
			if (key.startsWith(keyPrefix)) {
				result.put(key, systemProps.getProperty(key));
			}
		}

		// 从环境变量获取
		Map<String, String> envVars = System.getenv();
		for (Map.Entry<String, String> entry : envVars.entrySet()) {
			String key = entry.getKey();
			if (key.startsWith(keyPrefix) || key.startsWith(keyPrefix.toUpperCase().replace('.', '_'))) {
				result.put(key, entry.getValue());
			}
		}

		return result;
	}

	/**
	 * 打印配置信息（用于调试）
	 * @param key 配置key
	 */
	public static void printConfigInfo(String key) {
		ConfigResult result = getConfigWithSource(key, null);
		System.out.println("配置项: " + key);
		System.out.println("  值: " + (result.getValue() != null ? result.getValue() : "未设置"));
		System.out.println("  来源: " + result.getSource().getDescription());
		System.out.println("  实际key: " + result.getActualKey());

		if (!result.isFound()) {
			System.out.println("  尝试的key变体:");
			List<String> variants = generateKeyVariants(key);
			for (String variant : variants) {
				System.out.println("    - " + variant);
			}
		}
	}

	/**
	 * 清除配置缓存
	 */
	public static void clearCache() {
		CONFIG_CACHE.clear();
	}

	/**
	 * 设置是否启用缓存
	 * @param enabled 是否启用
	 */
	public static void setCacheEnabled(boolean enabled) {
		cacheEnabled = enabled;
		if (!enabled) {
			clearCache();
		}
	}

	/**
	 * 获取缓存大小
	 * @return 缓存项数量
	 */
	public static int getCacheSize() {
		return CONFIG_CACHE.size();
	}
}