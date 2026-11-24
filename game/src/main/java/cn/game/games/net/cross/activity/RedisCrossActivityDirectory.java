package cn.game.games.net.cross.activity;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.redisson.api.RBucket;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.cache.CacheType;
import cn.game.util.LuaScriptUtil;
import cn.game.util.RedisUtil;

/**
 * 基于 Redis 与 Lua CAS 的跨服活动目录实现
 * 强制使用 StringCodec.INSTANCE 保持纯字符串对齐
 * 
 * @author shuyongqiang
 */
public class RedisCrossActivityDirectory implements CrossActivityDirectory {

	private static final Logger log = LoggerFactory.getLogger(RedisCrossActivityDirectory.class);

	private static final RedisCrossActivityDirectory INSTANCE = new RedisCrossActivityDirectory();

	/** 本地测试/无 Redis 降级内存存储 */
	private final Map<String, Long> localMemoryStore = new ConcurrentHashMap<>();

	public static RedisCrossActivityDirectory getInstance() {
		return INSTANCE;
	}

	private String buildKey(int configId, String scopeKey) {
		return CacheType.CROSS_ACTIVITY_ACTIVE_UID.key(configId, scopeKey);
	}

	@Override
	public Optional<Long> getActiveUid(int configId, String scopeKey) {
		String key = buildKey(configId, scopeKey);
		if (RedisUtil.getRedis() == null) {
			Long uid = localMemoryStore.get(key);
			return Optional.ofNullable(uid);
		}
		try {
			RBucket<String> bucket = RedisUtil.getRedis().getBucket(key, StringCodec.INSTANCE);
			String val = bucket.get();
			if (val == null || val.trim().isEmpty()) {
				return Optional.empty();
			}
			return Optional.of(Long.parseLong(val.trim()));
		} catch (Exception e) {
			log.error("Failed to get activeUid from Redis key={}", key, e);
			return Optional.empty();
		}
	}

	@Override
	public void putActive(int configId, String scopeKey, long activityUid) {
		String key = buildKey(configId, scopeKey);
		if (RedisUtil.getRedis() != null) {
			try {
				RBucket<String> bucket = RedisUtil.getRedis().getBucket(key, StringCodec.INSTANCE);
				bucket.set(String.valueOf(activityUid));
			} catch (Exception e) {
				log.error("Failed to put activeUid to Redis key={}, uid={}", key, activityUid, e);
			}
		} else {
			localMemoryStore.put(key, activityUid);
		}
	}

	@Override
	public boolean removeIfMatch(int configId, String scopeKey, long expectedUid) {
		String key = buildKey(configId, scopeKey);
		if (RedisUtil.getRedis() != null) {
			try {
				return LuaScriptUtil.deleteIfMatch(key, String.valueOf(expectedUid));
			} catch (Exception e) {
				log.error("Failed to removeIfMatch from Redis key={}, expectedUid={}", key, expectedUid, e);
				return false;
			}
		}
		Long current = localMemoryStore.get(key);
		if (current != null && current == expectedUid) {
			return localMemoryStore.remove(key, expectedUid);
		}
		return false;
	}

	/** 仅供测试重置 */
	public void clearLocalMemory() {
		localMemoryStore.clear();
	}
}
