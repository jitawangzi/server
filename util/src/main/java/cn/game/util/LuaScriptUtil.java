package cn.game.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;

import org.apache.xmlbeans.impl.xb.xsdschema.ListDocument.List;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.x.protobuf.MysqlxDatatypes.Scalar.String;

public class LuaScriptUtil {
	private static final Logger logger = LoggerFactory.getLogger(LuaScriptUtil.class);
	private final RedissonClient redisson;

	public LuaScriptUtil(RedissonClient redisson) {
		this.redisson = redisson;
	}

	public enum LuaScript {
		UPDATE_IF_GREATER("update_if_greater.lua", "更新值如果新值更大"), INCREMENT_WITH_MAX("increment_with_max.lua", "增加值但不超过最大值"),
		;

		private final String filename;
		private final String description;
		private String content;

		LuaScript(String filename, String description) {
			this.filename = filename;
			this.description = description;
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
	}

	static {
		for (LuaScript script : LuaScript.values()) {
			try (InputStream inputStream = LuaScriptUtil.class.getResourceAsStream("/lua/" + script.getFilename())) {
				if (inputStream != null) {
					script.content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
				} else {
					logger.error("Cannot find Lua script file: {}", script.getFilename());
				}
			} catch (IOException e) {
				logger.error("Error reading Lua script file: {}", script.getFilename(), e);
			}
		}
	}

	/**
	 * 执行 Lua 脚本
	 *
	 * @param script 要执行的脚本
	 * @param keys Redis 键列表
	 * @param values 脚本参数列表
	 * @param <T> 返回值类型
	 * @return 脚本执行结果
	 */
	public <T> CompletionStage<T> executeLuaScript(LuaScript script, List<Object> keys, Object... values) {
		RScript rScript = redisson.getScript();
		return rScript.evalAsync(RScript.Mode.READ_WRITE, script.getContent(), RScript.ReturnType.VALUE, keys, values);
	}

	/**
	 * 更新值如果新值更大
	 *
	 * @param key Redis 键
	 * @param newValue 新值
	 * @return 更新后的值
	 */
	public CompletionStage<Long> updateIfGreater(String key, long newValue) {
		return executeLuaScript(LuaScript.UPDATE_IF_GREATER, List.of(key), newValue);
	}

	/**
	 * 增加值但不超过最大值
	 *
	 * @param key Redis 键
	 * @param increment 增加的值
	 * @param maxValue 最大值
	 * @return 更新后的值
	 */
	public CompletionStage<Long> incrementWithMax(String key, long increment, long maxValue) {
		return executeLuaScript(LuaScript.INCREMENT_WITH_MAX, List.of(key), increment, maxValue);
	}

}