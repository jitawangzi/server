package cn.game.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.redisson.api.RScript;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
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
		UPDATE_SCORE_IF_GREATER("update_score_if_greater.lua", "更新值如果新值更大", true),
		INCREMENT_WITH_MAX("increment_with_max.lua", "增加值但不超过最大值", true),;

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
		System.out.println("Script content: " + script.getContent());
		return rScript.scriptLoad(script.getContent());
	}

	/**
	 * 执行 Lua 脚本
	 *
	 * @param script 要执行的脚本
	 * @param codec Redis 编解码器
	 * @param keys Redis 键列表
	 * @param values 脚本参数列表
	 * @param <T> 返回值类型
	 * @return 脚本执行结果
	 */
	public static <T> CompletionStage<T> executeLuaScript(LuaScript script, Codec codec, List<Object> keys, Object... values) {
		RScript rScript = codec == null ? RedisUtil.getRedis().getScript() : RedisUtil.getRedis().getScript(codec);
		if (script.useSha1) {
			return rScript.evalShaAsync(RScript.Mode.READ_WRITE, script.sha1, RScript.ReturnType.VALUE, keys, values);
		}
		return rScript.evalAsync(RScript.Mode.READ_WRITE, script.getContent(), RScript.ReturnType.VALUE, keys, values);
	}

	/** 
	 * RScoredSortedSet中如果新值更大则更新值
	 * @param key
	 * @param member
	 * @param newScore
	 * @return
	 */
	public static CompletionStage<Double> updateScoreIfGreater(String key, long member, double newScore) {
		return executeLuaScript(LuaScript.UPDATE_SCORE_IF_GREATER, LongCodec.INSTANCE, List.of(key), member, newScore).thenApply(result -> {
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
		return executeLuaScript(LuaScript.INCREMENT_WITH_MAX, LongCodec.INSTANCE, List.of(key), increment, maxValue);
	}

}