package cn.game.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.redisson.api.RScript;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**    
 * redis lua脚本工具类
 * 2024年9月12日 20:00:45
 * @author SYQ
 */
public class LuaScriptUtil {
	private static final Logger logger = LoggerFactory.getLogger(LuaScriptUtil.class);

	public enum LuaScript {
		UPDATE_SET_SCORE_IF_GREATER("update_set_score_if_greater.lua", "更新值set分数如果新值更大", true),
		SUBTRACT_HASH_IF_NON_NEGATIVE("subtract_hash_if_non_negative.lua", "减少hash表中指定字段的数值，确保结果不为负数", true),
		UPDATE_HASH_CONDITIONAL("update_hash_conditional.lua", "只有当hash表中当前值等于期望值时才进行加减操作", true),
		INCREMENT_WITH_MAX("increment_with_max.lua", "增加值但不超过最大值", true),
		ADD_SET_WITH_LIMIT("add_set_with_limit.lua", "向集合添加元素，但限制集合大小不超过指定值", true),
		ADD_SET_BATCH_WITH_LIMIT("add_set_batch_with_limit.lua", "批量向集合添加元素，但限制集合大小不超过指定值", true),
		ADD_LIST_WITH_FIFO_LIMIT("add_list_with_fifo_limit.lua", "向列表添加元素，如果超过大小限制则移除最老的元素", true),
		ADD_LIST_BATCH_WITH_FIFO_LIMIT("add_list_batch_with_fifo_limit.lua", "向列表批量添加元素，如果超过大小限制则移除最老的元素", true),
		TRY_SET_WITH_EXPECT("try_set_with_expect.lua", "条件设置键值对并指定过期时间，仅当键不存在或当前值与预期值相同时才设置", true),
		ZSET_COPY_TOPN("zset_copy_topN.lua", "将源ZSET的前N名复制到目标ZSET，默认不清空目标", true),;

		private final String filename;
		private final String description;
		private String content;
		private final boolean useSha1;
		private String sha1;

		LuaScript(String filename, String description, boolean useSha1) {
			this.filename = filename;
			this.description = description;
			this.useSha1 = useSha1;
		}

		public String getFilename() {
			return filename;
		}

		public String getDescription() {
			return description;
		}

		public String getContent() {
			return content;
		}

		public String getSha1() {
			return sha1;
		}

	}

	static {
		for (LuaScript script : LuaScript.values()) {
			try (InputStream inputStream = LuaScriptUtil.class.getResourceAsStream("/lua/" + script.getFilename())) {
				if (inputStream != null) {
					script.content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
					if (script.useSha1) {
						script.sha1 = loadScript(script);
					}
				} else {
					logger.error("Cannot find Lua script file: {}", script.getFilename());
				}
			} catch (IOException e) {
				logger.error("Error reading Lua script file: {}", script.getFilename(), e);
			}
		}
	}

	/** 
	 * 预加载Lua脚本，并返回脚本的SHA1哈希值
	 * @param script
	 * @return
	 */
	public static String loadScript(LuaScript script) {
		RScript rScript = RedisUtil.getRedis().getScript();
		logger.info("Script content: " + script.getContent());
		return rScript.scriptLoad(script.getContent());
	}

	/**
	 * 异步执行 Lua 脚本
	 *
	 * @param script 要执行的脚本
	 * @param codec Redis 编解码器
	 * @param keys Redis 键列表
	 * @param values 脚本参数列表
	 * @param <T> 返回值类型
	 * @return 脚本执行结果
	 */
	public static <T> CompletionStage<T> executeLuaScriptAsync(LuaScript script, Codec codec, List<Object> keys, Object... values) {
		RScript rScript = codec == null ? RedisUtil.getRedis().getScript() : RedisUtil.getRedis().getScript(codec);
		if (script.useSha1) {
			return rScript.evalShaAsync(RScript.Mode.READ_WRITE, script.sha1, RScript.ReturnType.VALUE, keys, values);
		}
		return rScript.evalAsync(RScript.Mode.READ_WRITE, script.getContent(), RScript.ReturnType.VALUE, keys, values);
	}

	public static <T> T executeLuaScript(LuaScript script, Codec codec, List<Object> keys, Object... values) {
		RScript rScript = codec == null ? RedisUtil.getRedis().getScript() : RedisUtil.getRedis().getScript(codec);
		if (script.useSha1) {
			return rScript.evalSha(RScript.Mode.READ_WRITE, script.sha1, RScript.ReturnType.VALUE, keys, values);
		}
		return rScript.eval(RScript.Mode.READ_WRITE, script.getContent(), RScript.ReturnType.VALUE, keys, values);
	}

	/** 
	 * RScoredSortedSet中如果新值更大则更新值
	 * @param key
	 * @param member
	 * @param newScore
	 * @return
	 */
	public static CompletionStage<Double> updateScoreIfGreater(String key, long member, double newScore) {
		return executeLuaScriptAsync(LuaScript.UPDATE_SET_SCORE_IF_GREATER, LongCodec.INSTANCE, List.of(key), member, newScore)
				.thenApply(result -> {
					if (result instanceof Number) {
						return ((Number) result).doubleValue();
					}
					throw new IllegalStateException("Unexpected result type: " + result.getClass());
				});
	}

	/**
	 * 增加值但不超过最大值
	 *
	 * @param key Redis 键
	 * @param increment 增加的值
	 * @param maxValue 最大值
	 * @return 更新后的值
	 */
	public static CompletionStage<Long> incrementWithMax(String key, long increment, long maxValue) {
		return executeLuaScriptAsync(LuaScript.INCREMENT_WITH_MAX, LongCodec.INSTANCE, List.of(key), increment, maxValue);
	}

	/** 
	 * 只有当hash表中当前值等于期望值时才进行加减操作
	 * @param key
	 * @param expectedValue 预期值
	 * @param addValue  增加或减少的值
	 * @return 更新后的值
	 */
	public static CompletionStage<Long> updateHashConditional(String key, long expectedValue, long addValue) {
		return executeLuaScriptAsync(LuaScript.UPDATE_HASH_CONDITIONAL, LongCodec.INSTANCE, List.of(key), expectedValue, addValue);
	}

	/**
	 * 设置某个key的值，只有当key不存在，或者值等于预期值时设置  并指定过期时间
	 * @param key Redis键
	 * @param value 要设置的值
	 * @param expireSeconds 过期时间（秒）
	 * @return true表示设置成功，false表示失败（被其他值占用）
	 */
	public static boolean trySetWithExpect(String key, String value, int expireSeconds) {
		Long result = executeLuaScript(LuaScript.TRY_SET_WITH_EXPECT, null, List.of(key), value, expireSeconds);
		return result != null && result == 1;
	}

	/**
	 * 设置某个key的值，只有当key不存在，或者值等于预期值时设置  并指定过期时间（异步版本）
	 * @param key Redis键
	 * @param value 要设置的值
	 * @param expireSeconds 过期时间（秒）
	 * @return CompletionStage<Boolean>
	 */
	public static CompletionStage<Boolean> trySetWithExpectAsync(String key, String value, int expireSeconds) {
		return executeLuaScriptAsync(LuaScript.TRY_SET_WITH_EXPECT, null, List.of(key), value, expireSeconds).thenApply(result -> {
			if (result instanceof Number) {
				return ((Number) result).longValue() == 1;
			}
			return false;
		});
	}

	/**
	 * 将源ZSET的前 topN 名复制到目标ZSET（异步）
	 * @param sourceKey 源ZSET
	 * @param destKey   目标ZSET
	 * @param topN      前N名(>0)
	 * @param clearDest 是否清空目标(默认清空，传 null 则走默认 1)
	 * @param expireSeconds 目标key过期秒数(<=0 不设置)
	 * @return CompletionStage<CopyResult> 复制结果
	 */
	public static CompletionStage<CopyResult> copyZSetTopNAsync(String sourceKey, String destKey, int topN, Boolean clearDest,
			int expireSeconds) {
		int clear = (clearDest == null ? 1 : (clearDest ? 1 : 0));
		return executeLuaScriptAsync(LuaScript.ZSET_COPY_TOPN, StringCodec.INSTANCE, Arrays.asList(sourceKey, destKey), topN, clear,
				expireSeconds).thenApply(LuaScriptUtil::parseCopyResult);
	}

	/**
	 * 将源ZSET的前 topN 名复制到目标ZSET（同步）
	 */
	public static CopyResult copyZSetTopN(String sourceKey, String destKey, int topN, Boolean clearDest, int expireSeconds) {
		int clear = (clearDest == null ? 1 : (clearDest ? 1 : 0));
		Object ret = executeLuaScript(LuaScript.ZSET_COPY_TOPN, StringCodec.INSTANCE, Arrays.asList(sourceKey, destKey), topN, clear,
				expireSeconds);
		return parseCopyResult(ret);
	}

	/**
	 * 解析 {copiedCount, totalSourceSize} 返回
	 */
	@SuppressWarnings("unchecked")
	private static CopyResult parseCopyResult(Object ret) {
		if (ret == null) {
			throw new IllegalStateException("Null result from zset_copy_topN");
		}
		if (ret instanceof List) {
			List<Object> list = (List<Object>) ret;
			if (list.size() >= 2 && list.get(0) instanceof Number && list.get(1) instanceof Number) {
				long copied = ((Number) list.get(0)).longValue();
				long total = ((Number) list.get(1)).longValue();
				return new CopyResult(copied, total);
			}
		}
		throw new IllegalStateException("Unexpected result structure: " + ret);
	}

	/**
	 * 复制结果
	 */
	public static class CopyResult {
		public final long copiedCount;
		public final long totalSourceSize;

		public CopyResult(long copiedCount, long totalSourceSize) {
			this.copiedCount = copiedCount;
			this.totalSourceSize = totalSourceSize;
		}

		@Override
		public String toString() {
			return "CopyResult{copiedCount=" + copiedCount + ", totalSourceSize=" + totalSourceSize + '}';
		}
	}

}