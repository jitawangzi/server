package cn.game.core.cache.id;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.redisson.client.codec.StringCodec;

import cn.game.util.LuaScriptUtil;
import cn.game.util.LuaScriptUtil.LuaScript;

public class RedissonLeaseRenewer {

	private final LuaScript renewScript; // 脚本封装

	public RedissonLeaseRenewer(LuaScript script) {
		this.renewScript = script;
	}

	// 批量续约：key -> expectedValue，expireSeconds
	public CompletionStage<List<Boolean>> renewBatch(Map<String, String> keyToExpectedValue, int expireSeconds) {
		if (keyToExpectedValue.isEmpty()) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}

		// 将输入拆为 batchedKeys 与 batchedValues，按每个 key 生成一条指令
		List<List<Object>> batchedKeys = new ArrayList<>(keyToExpectedValue.size());
		List<Object[]> batchedValues = new ArrayList<>(keyToExpectedValue.size());

		for (Map.Entry<String, String> e : keyToExpectedValue.entrySet()) {
			String key = e.getKey();
			String expected = e.getValue();
			batchedKeys.add(Collections.singletonList((Object) key));
			// 注意：这里 values 顺序要与脚本参数一致
			batchedValues.add(new Object[] { expected, expireSeconds });
		}

		// 通过 LuaScriptUtil 的 batch 执行，codec 使用 StringCodec
		return LuaScriptUtil.executeLuaScriptBatchAsync(renewScript, StringCodec.INSTANCE, batchedKeys, batchedValues).thenApply(list -> {
			// 将 VALUE(Integer/Long 0/1) 转为 Boolean
			List<Boolean> out = new ArrayList<>(list.size());
			for (Object v : list) {
				boolean ok = (v instanceof Number) && ((Number) v).longValue() == 1L;
				out.add(ok);
			}
			return out;
		});
	}
}
