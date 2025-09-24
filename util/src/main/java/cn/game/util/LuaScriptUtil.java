package cn.game.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.redisson.api.RBatch;
import org.redisson.api.RFuture;
import org.redisson.api.RScript;
import org.redisson.api.RScriptAsync;
import org.redisson.api.RedissonClient;
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
	// 慢脚本阈值（毫秒）
	private static volatile long SLOW_THRESHOLD_MS = 50L;
	
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


	// 可运行时调整阈值
	public static void setSlowThresholdMs(long thresholdMs) {
		if (thresholdMs < 0) {
			logger.warn("Slow threshold must be >= 0, got {}. Keep previous: {}", thresholdMs, SLOW_THRESHOLD_MS);
			return;
		}
		SLOW_THRESHOLD_MS = thresholdMs;
	}

	// 统一耗时日志打印
	private static void logDuration(String phase, LuaScript script, List<Object> keys, Object[] values, long elapsedMs, boolean retried,
			Throwable ex) {
		String base = String.format("LuaScript[%s] phase=%s keys=%d args=%d elapsed=%dms retried=%s", script.getFilename(), phase,
				(keys == null ? 0 : keys.size()), (values == null ? 0 : values.length), elapsedMs, retried);
		if (ex != null) {
			// 异常统一打 warn，附带异常
			logger.warn(base + " failed: " + ex.getMessage(), ex);
			return;
		}
		if (elapsedMs >= SLOW_THRESHOLD_MS) {
			logger.warn(base + " SLOW");
		} else {
			logger.debug(base);
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
	 * 遇到 NOSCRIPT 时自动加载脚本，集群主从切换、节点重启时更为健壮。 
	 * @param script 要执行的脚本
	 * @param codec Redis 编解码器
	 * @param keys Redis 键列表
	 * @param values 脚本参数列表
	 * @param <T> 返回值类型
	 * @return 脚本执行结果
	 */
	public static <T> CompletionStage<T> executeLuaScriptAsync(LuaScript script, Codec codec, List<Object> keys, Object... values) {
		final RScript rScript = (codec == null) ? RedisUtil.getRedis().getScript() : RedisUtil.getRedis().getScript(codec);
		final long startNanos = System.nanoTime();

		// 如果不使用 sha1，直接 eval
		if (!script.useSha1) {
			CompletableFuture<T> cf = new CompletableFuture<>();
			rScript.evalAsync(RScript.Mode.READ_WRITE, script.getContent(), RScript.ReturnType.VALUE, keys, values)
					.whenComplete((val, ex) -> {
						long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
						if (ex != null) {
							logDuration("evalAsync(no-sha1)", script, keys, values, elapsedMs, false, ex);
							cf.completeExceptionally(ex);
						} else {
							logDuration("evalAsync(no-sha1)", script, keys, values, elapsedMs, false, null);
							@SuppressWarnings("unchecked")
							T casted = (T) val;
							cf.complete(casted);
						}
					});
			return cf;
		}

		// 使用 EVALSHA，若遇到 NOSCRIPT 则自动 LOAD 并重试一次
		CompletableFuture<T> result = new CompletableFuture<>();

		rScript.evalShaAsync(RScript.Mode.READ_WRITE, script.getSha1(), RScript.ReturnType.VALUE, keys, values).whenComplete((val, ex) -> {
			long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;

			if (ex == null) {
				logDuration("evalShaAsync", script, keys, values, elapsedMs, false, null);
				@SuppressWarnings("unchecked")
				T casted = (T) val;
				result.complete(casted);
				return;
			}

			// 非 NOSCRIPT，直接失败
			if (!isNoScript(ex)) {
				logDuration("evalShaAsync", script, keys, values, elapsedMs, false, ex);
				result.completeExceptionally(ex);
				return;
			}

			// NOSCRIPT：reload 脚本并重试一次
			final long reloadStart = System.nanoTime();
			rScript.scriptLoadAsync(script.getContent()).whenComplete((newSha, loadEx) -> {
				long reloadElapsed = (System.nanoTime() - reloadStart) / 1_000_000L;
				// 先记录 load 的耗时
				logDuration("scriptLoadAsync", script, keys, values, reloadElapsed, true, loadEx);
				if (loadEx != null) {
					result.completeExceptionally(loadEx);
					return;
				}

				// 更新脚本 sha1
				updateScriptSha(script, newSha);

				final long retryStart = System.nanoTime();
				rScript.evalShaAsync(RScript.Mode.READ_WRITE, newSha, RScript.ReturnType.VALUE, keys, values).whenComplete((val2, ex2) -> {
					long retryElapsed = (System.nanoTime() - retryStart) / 1_000_000L;
					// 记录重试的耗时
					logDuration("evalShaAsync(retry)", script, keys, values, retryElapsed, true, ex2);
					if (ex2 != null) {
						result.completeExceptionally(ex2);
					} else {
						@SuppressWarnings("unchecked")
						T casted2 = (T) val2;
						result.complete(casted2);
					}
				});
			});
		});

		return result;
	}

	/* 辅助方法：判断是否为 NOSCRIPT 错误 */
	private static boolean isNoScript(Throwable t) {
		if (t == null)
			return false;
		String msg = t.getMessage();
		if (msg == null && t.getCause() != null) {
			msg = t.getCause().getMessage();
		}
		return msg != null && msg.toUpperCase().contains("NOSCRIPT");
	}

	/* 辅助方法：更新 LuaScript 枚举实例中的 sha1 字段 */
	private static synchronized void updateScriptSha(LuaScript script, String newSha) {
		try {
			java.lang.reflect.Field f = LuaScript.class.getDeclaredField("sha1");
			f.setAccessible(true);
			f.set(script, newSha);
		} catch (Exception e) {
			// 记录日志或抛出运行时异常，视你项目需要
			logger.warn("Failed to update script sha1 for {}: {}", script.getFilename(), e.toString());
		}
	}

	public static <T> T executeLuaScript(LuaScript script, Codec codec, List<Object> keys, Object... values) {
		RScript rScript = codec == null ? RedisUtil.getRedis().getScript() : RedisUtil.getRedis().getScript(codec);
		final long startNanos = System.nanoTime();
		if (!script.useSha1) {
			try {
				T res = rScript.eval(RScript.Mode.READ_WRITE, script.getContent(), RScript.ReturnType.VALUE, keys, values);
				long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
				logDuration("eval(no-sha1)", script, keys, values, elapsedMs, false, null);
				return res;
			} catch (Exception ex) {
				long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
				logDuration("eval(no-sha1)", script, keys, values, elapsedMs, false, ex);
				throw ex;
			}
		}

		try {
			T res = rScript.evalSha(RScript.Mode.READ_WRITE, script.getSha1(), RScript.ReturnType.VALUE, keys, values);
			long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
			logDuration("evalSha", script, keys, values, elapsedMs, false, null);
			return res;
		} catch (Exception ex) {
			long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
			// 如果不是 NOSCRIPT，直接记录并抛出
			if (!isNoScript(ex)) {
				logDuration("evalSha", script, keys, values, elapsedMs, false, ex);
				throw ex;
			}

			// NOSCRIPT：load + retry
			final long loadStart = System.nanoTime();
			String newSha = rScript.scriptLoad(script.getContent());
			long loadElapsed = (System.nanoTime() - loadStart) / 1_000_000L;
			logDuration("scriptLoad", script, keys, values, loadElapsed, true, null);
			updateScriptSha(script, newSha);

			final long retryStart = System.nanoTime();
			T res2 = rScript.evalSha(RScript.Mode.READ_WRITE, newSha, RScript.ReturnType.VALUE, keys, values);
			long retryElapsed = (System.nanoTime() - retryStart) / 1_000_000L;
			logDuration("evalSha(retry)", script, keys, values, retryElapsed, true, null);
			return res2;
		}
	}

	// 批量：每个调用对应一组 keys 和 values，返回每个调用的结果 Object 列表（由调用方再做类型映射）
	public static CompletionStage<List<Object>> executeLuaScriptBatchAsync(LuaScript script, Codec codec, List<List<Object>> batchedKeys,
			List<Object[]> batchedValues) {
		final long startNanos = System.nanoTime();
		RBatch batch = RedisUtil.getRedis().createBatch();
		RScriptAsync batchScript = (codec == null) ? batch.getScript() : batch.getScript(codec);

		List<RFuture<Object>> futures = new ArrayList<>(batchedKeys.size());
		if (script.useSha1) {
			for (int i = 0; i < batchedKeys.size(); i++) {
				futures.add(batchScript.evalShaAsync(RScript.Mode.READ_WRITE, script.getSha1(), RScript.ReturnType.VALUE,
						batchedKeys.get(i), batchedValues.get(i)));
			}
		} else {
			for (int i = 0; i < batchedKeys.size(); i++) {
				futures.add(batchScript.evalAsync(RScript.Mode.READ_WRITE, script.getContent(), RScript.ReturnType.VALUE,
						batchedKeys.get(i), batchedValues.get(i)));
			}
		}

		CompletableFuture<List<Object>> result = new CompletableFuture<>();
		batch.executeAsync().whenComplete((batchRes, batchEx) -> {
			long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;

			if (batchEx != null) {
				// 如果 NOSCRIPT，reload + retry once
				if (script.useSha1 && isNoScript(batchEx)) {
					logDuration("batch.executeAsync NOSCRIPT", script, null, null, elapsedMs, true, null);

					RedissonClient c2 = clientProvider();
					RScript rScript = (codec == null) ? c2.getScript() : c2.getScript(codec);

					final long loadStart = System.nanoTime();
					rScript.scriptLoadAsync(script.getContent()).whenComplete((newSha, loadEx) -> {
						long loadElapsed = (System.nanoTime() - loadStart) / 1_000_000L;
						logDuration("batch.scriptLoadAsync", script, null, null, loadElapsed, true, loadEx);
						if (loadEx != null) {
							result.completeExceptionally(loadEx);
							return;
						}
						updateScriptSha(script, newSha);

						// 重建 batch 再执行一次
						final long retryStart = System.nanoTime();
						executeLuaScriptBatchAsync(script, codec, batchedKeys, batchedValues).whenComplete((v2, ex2) -> {
							long retryElapsed = (System.nanoTime() - retryStart) / 1_000_000L;
							logDuration("batch.retry", script, null, null, retryElapsed, true, ex2);
							if (ex2 != null)
								result.completeExceptionally(ex2);
							else
								result.complete(v2);
						});
					});
				} else {
					logDuration("batch.executeAsync", script, null, null, elapsedMs, false, batchEx);
					result.completeExceptionally(batchEx);
				}
				return;
			}
			try {
				List<Object> out = new ArrayList<>(futures.size());
				for (RFuture<Object> f : futures) {
					out.add(f.getNow());
				}
				logDuration("batch.executeAsync", script, null, null, elapsedMs, false, null);
				result.complete(out);
			} catch (Throwable t) {
				logDuration("batch.collect", script, null, null, elapsedMs, false, t);
				result.completeExceptionally(t);
			}
		});

		return result;
	}

	private static RedissonClient clientProvider() {
		return RedisUtil.getRedis();
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