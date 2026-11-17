package cn.game.simulation.test.ai;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.GmClientResult;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 黑白盒一体化测试 SDK 工具包
 * 提供 Arrange 原语、Trace 录制、TracedResponse 断言、模块自省与冷重载验证
 *
 * @author shuyongqiang
 */
public class WhiteBoxTestKit {

	private static final Logger logger = LoggerFactory.getLogger(WhiteBoxTestKit.class);

	private final Client client;
	private final Runnable syncDataCallback;

	private boolean traceActive;
	private boolean clockModified;
	private boolean rngModified;

	public WhiteBoxTestKit(Client client, Runnable syncDataCallback) {
		this.client = client;
		this.syncDataCallback = syncDataCallback;
	}

	public boolean isTraceActive() {
		return traceActive;
	}

	public boolean isClockModified() {
		return clockModified;
	}

	public boolean isRngModified() {
		return rngModified;
	}

	/**
	 * 重置指定模块或全部模块
	 *
	 * @param modules 待重置模块名称列表；若未提供则默认重置全部模块 ("all")
	 */
	public void resetModules(String... modules) {
		if (modules == null || modules.length == 0) {
			resetModules(Collections.emptyList());
			return;
		}
		resetModules(List.of(modules));
	}

	/**
	 * 重置指定模块集合
	 *
	 * @param modules 待重置模块名称列表；若为空则默认重置全部模块 ("all")
	 */
	public void resetModules(List<String> modules) {
		String moduleParam = (modules == null || modules.isEmpty()) ? "all" : String.join(",", modules);
		client.gm("gm.resetModule", Map.of("modules", moduleParam)).requireOk();
	}

	/**
	 * 保存当前测试快照
	 *
	 * @param snapshotName 快照标识
	 */
	public void saveSnapshot(String snapshotName) {
		client.gm("gm.snapshot.save", Map.of("snapshotName", snapshotName)).requireOk();
	}

	/**
	 * 恢复指定快照并刷新客户端本地缓存
	 *
	 * @param snapshotName 快照标识
	 */
	public void restoreSnapshot(String snapshotName) {
		client.gm("gm.snapshot.restore", Map.of("snapshotName", snapshotName)).requireOk();
		if (syncDataCallback != null) {
			syncDataCallback.run();
		}
	}

	/**
	 * 查询快照列表
	 *
	 * @return 快照信息列表
	 */
	public List<Map<String, Object>> listSnapshots() {
		GmClientResult result = client.gm("gm.snapshot.list", Collections.emptyMap()).requireOk();
		Object data = result.getData();
		if (data instanceof List) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> list = (List<Map<String, Object>>) data;
			return list;
		}
		return Collections.emptyList();
	}

	/**
	 * 删除快照（默认清除全部快照）
	 */
	public void deleteSnapshots() {
		client.gm("gm.snapshot.delete", Map.of("all", true)).requireOk();
	}

	/**
	 * 删除指定快照
	 *
	 * @param snapshotName 快照标识，"all" 表示清空所有
	 */
	public void deleteSnapshot(String snapshotName) {
		if (snapshotName == null || "all".equalsIgnoreCase(snapshotName.trim())) {
			client.gm("gm.snapshot.delete", Map.of("all", true)).requireOk();
		} else {
			client.gm("gm.snapshot.delete", Map.of("snapshotName", snapshotName.trim())).requireOk();
		}
	}

	/**
	 * 设置玩家虚拟时钟偏移量
	 *
	 * @param offset 偏移量表达式（例如 "1d", "2h", "30m"）
	 * @param allowBackward 是否允许时钟回退
	 * @param kickClients 是否踢出关联客户端
	 */
	public void setVirtualTimeOffset(String offset, boolean allowBackward, boolean kickClients) {
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("offset", offset);
		params.put("allowBackward", allowBackward);
		params.put("kickClients", kickClients);
		client.gm("player.time.offset", params).requireOk();
		this.clockModified = true;
	}

	/**
	 * 设置玩家绝对虚拟时钟
	 *
	 * @param targetTime 目标时间字符串（如 "2026-06-01 12:00:00"）
	 * @param allowBackward 是否允许时钟回退
	 * @param kickClients 是否踢出关联客户端
	 */
	public void setVirtualTime(String targetTime, boolean allowBackward, boolean kickClients) {
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("targetTime", targetTime);
		params.put("allowBackward", allowBackward);
		params.put("kickClients", kickClients);
		client.gm("player.time.set", params).requireOk();
		this.clockModified = true;
	}

	/**
	 * 复位玩家虚拟时钟
	 *
	 * @param allowBackward 是否允许回退
	 * @param kickClients 是否踢出关联客户端
	 */
	public void resetVirtualTime(boolean allowBackward, boolean kickClients) {
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("allowBackward", allowBackward);
		params.put("kickClients", kickClients);
		client.gm("player.time.reset", params).requireOk();
		this.clockModified = false;
	}

	/**
	 * 查询玩家当前虚拟时钟状态
	 *
	 * @return 虚拟时钟状态映射
	 */
	public Map<String, Object> queryVirtualTimeStatus() {
		return client.gm("player.time.status", Collections.emptyMap()).requireOk().getDataAsMap();
	}

	/**
	 * 设定玩家随机数种子
	 *
	 * @param seed 随机种子
	 */
	public void seedRng(long seed) {
		client.gm("gm.rng.seed", Map.of("seed", seed)).requireOk();
		this.rngModified = true;
	}

	/**
	 * 设定玩家确定性伪随机序列
	 *
	 * @param intValues 预设整数序列
	 * @param doubleValues 预设浮点数序列
	 */
	public void sequenceRng(List<Integer> intValues, List<Double> doubleValues) {
		Map<String, Object> params = new LinkedHashMap<>();
		if (intValues != null) {
			params.put("intValues", intValues);
		}
		if (doubleValues != null) {
			params.put("doubleValues", doubleValues);
		}
		client.gm("gm.rng.sequence", params).requireOk();
		this.rngModified = true;
	}

	/**
	 * 复位玩家随机数生成器
	 */
	public void resetRng() {
		client.gm("gm.rng.reset", Collections.emptyMap()).requireOk();
		this.rngModified = false;
	}

	/**
	 * 查询玩家随机数生成器状态
	 *
	 * @return 随机状态映射
	 */
	public Map<String, Object> queryRngStatus() {
		return client.gm("gm.rng.status", Collections.emptyMap()).requireOk().getDataAsMap();
	}

	/**
	 * 黑盒发送协议请求并全程录制白盒 Trace
	 *
	 * @param <T> 预期响应类型
	 * @param request 协议请求实例
	 * @return 包装响应与 Trace 数据的 TracedResponse
	 */
	public <T> TracedResponse<T> sendAndTrace(Message request) {
		return traceExecution(() -> client.sendProtocolAndWait(request));
	}

	/**
	 * 录制指定执行块期间的服务端白盒 Trace 数据
	 *
	 * @param <T> 执行返回值类型
	 * @param action 测试动作
	 * @return 包含动作结果与追踪事件的 TracedResponse
	 */
	public <T> TracedResponse<T> traceExecution(Supplier<T> action) {
		client.gm("gm.trace.start", Map.of(
				"playerId", client.getPlayerId(),
				"maxRecords", 500,
				"clearExisting", true
		)).requireOk();
		this.traceActive = true;

		T response = null;
		Throwable businessError = null;
		List<Map<String, Object>> traceRecords = new ArrayList<>();
		List<Map<String, Object>> resourceEvents = new ArrayList<>();
		List<Map<String, Object>> errorEvents = new ArrayList<>();
		try {
			response = action.get();
		} catch (Throwable t) {
			businessError = t;
		} finally {
			try {
				GmClientResult traceResult = client.gm("gm.trace.query", Map.of(
						"playerId", client.getPlayerId(),
						"limit", 50
				));
				if (traceResult.isOk()) {
					Object data = traceResult.getData();
					List<?> rawRecords = Collections.emptyList();
					if (data instanceof List) {
						rawRecords = (List<?>) data;
					} else if (data instanceof Map) {
						Object recordsObj = ((Map<?, ?>) data).get("records");
						if (recordsObj instanceof List) {
							rawRecords = (List<?>) recordsObj;
						}
					}

					for (Object recObj : rawRecords) {
						if (recObj instanceof Map) {
							@SuppressWarnings("unchecked")
							Map<String, Object> rec = (Map<String, Object>) recObj;
							traceRecords.add(rec);

							Object resObj = rec.get("resourceEvents");
							if (resObj == null) {
								resObj = rec.get("resourceDeltas");
							}
							if (resObj instanceof List) {
								for (Object item : (List<?>) resObj) {
									if (item instanceof Map) {
										@SuppressWarnings("unchecked")
										Map<String, Object> m = (Map<String, Object>) item;
										resourceEvents.add(m);
									}
								}
							}

							Object errObj = rec.get("errorEvents");
							if (errObj == null) {
								errObj = rec.get("errors");
							}
							if (errObj instanceof List) {
								for (Object item : (List<?>) errObj) {
									if (item instanceof Map) {
										@SuppressWarnings("unchecked")
										Map<String, Object> m = (Map<String, Object>) item;
										errorEvents.add(m);
									}
								}
							}
						}
					}
				}
			} catch (Throwable t) {
				logger.warn("查询 Trace 记录发生异常", t);
			}

			try {
				client.gm("gm.trace.stop", Collections.emptyMap());
			} catch (Throwable t) {
				logger.warn("停止 Trace 发生异常", t);
			} finally {
				this.traceActive = false;
			}

			if (businessError != null) {
				if (!errorEvents.isEmpty()) {
					IllegalStateException suppressedErr = new IllegalStateException(
							"服务端在业务执行期间记录了错误事件 (EX-14): " + errorEvents);
					businessError.addSuppressed(suppressedErr);
				}
				if (businessError instanceof RuntimeException) {
					throw (RuntimeException) businessError;
				} else if (businessError instanceof Error) {
					throw (Error) businessError;
				} else {
					throw new RuntimeException(businessError);
				}
			}
		}
		return new TracedResponse<>(response, traceRecords, resourceEvents, errorEvents);
	}

	/**
	 * 自省指定模块数据 (EX-15)
	 *
	 * @param moduleName 模块名称（支持模糊匹配，如 "ItemModule" 或 "item"）
	 * @return 模块内字段键值映射
	 */
	public Map<String, Object> inspectModule(String moduleName) {
		if (moduleName == null || moduleName.trim().isEmpty()) {
			throw new IllegalArgumentException("模块名称不能为空 (EX-15)");
		}
		String targetName = moduleName.trim();
		boolean isSpecific = targetName.endsWith("Module") || targetName.contains(".");
		Map<String, Object> inspectResult = isSpecific ? client.inspectPlayer(targetName) : client.inspectPlayer();

		Object modulesObj = inspectResult.get("modules");
		if (modulesObj == null && inspectResult.get("data") instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> dataMap = (Map<String, Object>) inspectResult.get("data");
			modulesObj = dataMap.get("modules");
		}
		if (!(modulesObj instanceof Map)) {
			throw new IllegalArgumentException("未能从自省响应中解析出 modules 映射 (EX-15)");
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> modules = (Map<String, Object>) modulesObj;

		String targetBase = targetName.endsWith("Module") ? targetName.substring(0, targetName.length() - 6) : targetName;

		for (Map.Entry<String, Object> entry : modules.entrySet()) {
			String key = entry.getKey();
			if (key.equalsIgnoreCase(targetName)) {
				if (entry.getValue() instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<String, Object> valMap = (Map<String, Object>) entry.getValue();
					return valMap;
				}
			}
			String keyBase = key.endsWith("Module") ? key.substring(0, key.length() - 6) : key;
			if (keyBase.equalsIgnoreCase(targetBase)) {
				if (entry.getValue() instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<String, Object> valMap = (Map<String, Object>) entry.getValue();
					return valMap;
				}
			}
		}
		throw new IllegalArgumentException("未找到指定模块或模块数据为空 (EX-15): " + moduleName);
	}

	/**
	 * 根据点分路径深度提取模块字段 (EX-15)
	 *
	 * @param moduleName 模块名称
	 * @param dotPath 点分属性路径（如 "level" 或 "info.ratio"）
	 * @return 字段值
	 */
	public Object inspectModuleField(String moduleName, String dotPath) {
		Map<String, Object> moduleData = inspectModule(moduleName);
		if (dotPath == null || dotPath.trim().isEmpty()) {
			return moduleData;
		}
		String[] segments = dotPath.split("\\.");
		Object current = moduleData;
		for (String seg : segments) {
			String trimmedSeg = seg.trim();
			if (trimmedSeg.isEmpty()) {
				continue;
			}
			if (!(current instanceof Map)) {
				throw new IllegalArgumentException(String.format("点分路径解析失败 (EX-15): 路径段 '%s' 的父级不是 Map 结构, module=%s, fullPath=%s",
						trimmedSeg, moduleName, dotPath));
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> currentMap = (Map<String, Object>) current;
			if (!currentMap.containsKey(trimmedSeg)) {
				throw new IllegalArgumentException(String.format("点分路径字段不存在 (EX-15): module=%s, field=%s, fullPath=%s",
						moduleName, trimmedSeg, dotPath));
			}
			current = currentMap.get(trimmedSeg);
		}
		return current;
	}

	/**
	 * 断言模块点分路径字段等于期望值 (EX-15)
	 *
	 * @param moduleName 模块名称
	 * @param dotPath 点分属性路径
	 * @param expectedValue 期望值（支持跨 Number 类型等价性比较）
	 */
	public void assertModuleFieldEquals(String moduleName, String dotPath, Object expectedValue) {
		Object actualValue = inspectModuleField(moduleName, dotPath);
		if (!areValuesEqual(actualValue, expectedValue)) {
			throw new AssertionError(String.format("模块字段值断言失败 (EX-15): module=%s, path=%s, expected=<%s> (%s), actual=<%s> (%s)",
					moduleName, dotPath,
					expectedValue, expectedValue == null ? "null" : expectedValue.getClass().getSimpleName(),
					actualValue, actualValue == null ? "null" : actualValue.getClass().getSimpleName()));
		}
	}

	private boolean areValuesEqual(Object actual, Object expected) {
		if (actual == null && expected == null) {
			return true;
		}
		if (actual == null || expected == null) {
			return false;
		}
		if (actual.equals(expected)) {
			return true;
		}
		if (actual instanceof Number && expected instanceof Number) {
			Number a = (Number) actual;
			Number e = (Number) expected;
			boolean aIsIntegral = (actual instanceof Byte || actual instanceof Short || actual instanceof Integer || actual instanceof Long);
			boolean eIsIntegral = (expected instanceof Byte || expected instanceof Short || expected instanceof Integer || expected instanceof Long);
			if (aIsIntegral && eIsIntegral) {
				return a.longValue() == e.longValue();
			}
			return Double.compare(a.doubleValue(), e.doubleValue()) == 0;
		}
		return Objects.equals(actual.toString(), expected.toString());
	}

	/**
	 * 强制玩家数据立即落库并执行冷重载 (EX-15)
	 *
	 * @return 冷重载响应数据
	 */
	public Map<String, Object> flushAndColdReload() {
		GmClientResult result = client.gm("gm.flushAndColdReload", Map.of(
				"playerId", client.getPlayerId()
		)).requireOk();
		Map<String, Object> data = result.getDataAsMap();
		Object reloadedObj = data.get("reloadedFromDb");
		boolean reloadedFromDb = (reloadedObj instanceof Boolean) ? (Boolean) reloadedObj : Boolean.parseBoolean(String.valueOf(reloadedObj));
		if (!reloadedFromDb) {
			throw new IllegalStateException("强制落盘与冷重载失败 (EX-15): reloadedFromDb 不为 true, data=" + data);
		}
		Object oldHash = data.get("oldIdentityHash");
		Object newHash = data.get("newIdentityHash");
		if (oldHash != null && newHash != null && Objects.equals(oldHash, newHash)) {
			throw new IllegalStateException(String.format("强制落盘与冷重载失败 (EX-15): 重载前后实体哈希未发生变化, oldIdentityHash=%s, newIdentityHash=%s",
					oldHash, newHash));
		}
		if (syncDataCallback != null) {
			syncDataCallback.run();
		}
		return data;
	}

	/**
	 * 清理所有已修改的调试状态（Trace、可控随机、虚拟时钟）
	 * 异常仅记录警告日志，绝不打断上层流程 (DR-12)
	 */
	public void cleanupAll() {
		if (traceActive) {
			try {
				client.gm("gm.trace.stop", Collections.emptyMap());
			} catch (Throwable t) {
				logger.warn("清理过程中停止 Trace 失败", t);
			} finally {
				traceActive = false;
			}
		}
		if (rngModified) {
			try {
				resetRng();
			} catch (Throwable t) {
				logger.warn("清理过程中复位可控随机失败", t);
			} finally {
				rngModified = false;
			}
		}
		if (clockModified) {
			try {
				resetVirtualTime(true, false);
			} catch (Throwable t) {
				logger.warn("清理过程中复位虚拟时钟失败", t);
			} finally {
				clockModified = false;
			}
		}
	}

	/**
	 * 包装了协议响应结果与服务端白盒 Trace 信息的复合断言上下文
	 *
	 * @param <T> 响应消息类型
	 */
	public static class TracedResponse<T> {

		private final T response;
		private final List<Map<String, Object>> traceRecords;
		private final List<Map<String, Object>> resourceEvents;
		private final List<Map<String, Object>> errorEvents;

		public TracedResponse(T response,
							  List<Map<String, Object>> traceRecords,
							  List<Map<String, Object>> resourceEvents,
							  List<Map<String, Object>> errorEvents) {
			this.response = response;
			this.traceRecords = traceRecords != null ? Collections.unmodifiableList(traceRecords) : Collections.emptyList();
			this.resourceEvents = resourceEvents != null ? Collections.unmodifiableList(resourceEvents) : Collections.emptyList();
			this.errorEvents = errorEvents != null ? Collections.unmodifiableList(errorEvents) : Collections.emptyList();
		}

		public T getResponse() {
			return response;
		}

		public List<Map<String, Object>> getTraceRecords() {
			return traceRecords;
		}

		public List<Map<String, Object>> getResourceEvents() {
			return resourceEvents;
		}

		public List<Map<String, Object>> getErrorEvents() {
			return errorEvents;
		}

		/**
		 * 断言执行期间服务端未产生任何错误事件 (EX-14)
		 *
		 * @return 当前对象以支持链式调用
		 */
		public TracedResponse<T> assertNoServerErrors() {
			if (!errorEvents.isEmpty()) {
				throw new IllegalStateException("服务端存在错误事件记录 (EX-14): " + errorEvents);
			}
			return this;
		}

		/**
		 * 断言指定资源的净变化量
		 *
		 * @param configId 资源配置 ID
		 * @param expectedSignedDelta 期望带符号变化量（增加为正，扣减为负）
		 * @return 当前对象以支持链式调用
		 */
		public TracedResponse<T> assertResourceDelta(int configId, long expectedSignedDelta) {
			return assertResourceDelta(configId, expectedSignedDelta, null);
		}

		/**
		 * 断言指定资源在指定操作类型下的净变化量 (EX-14)
		 *
		 * @param configId 资源配置 ID
		 * @param expectedSignedDelta 期望带符号变化量
		 * @param expectedOpType 操作类型（若为空则匹配所有操作类型）
		 * @return 当前对象以支持链式调用
		 */
		public TracedResponse<T> assertResourceDelta(int configId, long expectedSignedDelta, String expectedOpType) {
			long actualDelta = 0L;
			for (Map<String, Object> event : resourceEvents) {
				Object idObj = event.get("id");
				if (idObj == null) {
					idObj = event.get("configId");
				}
				if (idObj instanceof Number && ((Number) idObj).intValue() == configId) {
					if (expectedOpType != null && !expectedOpType.isEmpty()) {
						Object opTypeObj = event.get("opType");
						if (opTypeObj == null || !expectedOpType.equals(String.valueOf(opTypeObj))) {
							continue;
						}
					}
					Object countObj = event.get("count");
					long count = (countObj instanceof Number) ? ((Number) countObj).longValue() : 0L;
					boolean isAdd = true;
					if (event.containsKey("add")) {
						Object addVal = event.get("add");
						isAdd = (addVal instanceof Boolean) ? (Boolean) addVal : Boolean.parseBoolean(String.valueOf(addVal));
					} else if (event.containsKey("isAdd")) {
						Object addVal = event.get("isAdd");
						isAdd = (addVal instanceof Boolean) ? (Boolean) addVal : Boolean.parseBoolean(String.valueOf(addVal));
					}
					actualDelta += isAdd ? count : -count;
				}
			}
			if (actualDelta != expectedSignedDelta) {
				throw new AssertionError(String.format("资源变化量断言失败 (EX-14): configId=%d, expected=%d, actual=%d, opType=%s",
						configId, expectedSignedDelta, actualDelta, expectedOpType));
			}
			return this;
		}

		/**
		 * 断言是否存在特定增减方向的资源事件
		 *
		 * @param configId 资源配置 ID
		 * @param isAdd 是否为增加操作
		 * @return 当前对象以支持链式调用
		 */
		public TracedResponse<T> assertHasResourceEvent(int configId, boolean isAdd) {
			boolean found = false;
			for (Map<String, Object> event : resourceEvents) {
				Object idObj = event.get("id");
				if (idObj == null) {
					idObj = event.get("configId");
				}
				if (idObj instanceof Number && ((Number) idObj).intValue() == configId) {
					boolean eventIsAdd = true;
					if (event.containsKey("add")) {
						Object addVal = event.get("add");
						eventIsAdd = (addVal instanceof Boolean) ? (Boolean) addVal : Boolean.parseBoolean(String.valueOf(addVal));
					} else if (event.containsKey("isAdd")) {
						Object addVal = event.get("isAdd");
						eventIsAdd = (addVal instanceof Boolean) ? (Boolean) addVal : Boolean.parseBoolean(String.valueOf(addVal));
					}
					if (eventIsAdd == isAdd) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				throw new AssertionError(String.format("未找到符合预期的资源流水事件: configId=%d, isAdd=%s", configId, isAdd));
			}
			return this;
		}
	}
}
