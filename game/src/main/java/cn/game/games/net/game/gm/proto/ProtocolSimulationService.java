package cn.game.games.net.game.gm.proto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.exception.LogicException;
import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.core.net.socket.controller.Dispatcher;
import cn.game.core.net.socket.controller.DispatcherImpl;
import cn.game.games.cache.entity.Player;
import cn.game.util.HexUtil;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.gm.GmSecurityPolicy;
import cn.game.games.net.game.gm.GmStateDiffUtil;
import cn.game.games.net.game.gm.PlayerInspectService;
import cn.game.games.net.game.gm.ProtoCatalogService;
import cn.game.games.net.game.gm.RequestTraceService;
import cn.game.games.net.game.gm.RequestTraceService.TraceBuilder;
import cn.game.games.net.game.gm.web.FeatureWorkbenchService;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;

/**
 * 协议 Mini 客户端模拟执行与双通道捕获服务 (DC-04, DR-03, BR-05, EX-02..EX-06, AC-03..AC-04)
 * 在玩家虚拟线程专属邮箱内模拟分发业务请求，无侵入双通道捕获 Response 与 Push，集成 Trace 与状态 Diff。
 *
 * @author shuyongqiang
 */
public class ProtocolSimulationService {

	private static final Logger log = LoggerFactory.getLogger(ProtocolSimulationService.class);
	private static final ProtocolSimulationService INSTANCE = new ProtocolSimulationService();

	public static ProtocolSimulationService getInstance() {
		return INSTANCE;
	}

	private final Processor explicitProcessor;
	private final PlayerInspectService explicitInspectService;
	private final ProtoCatalogService explicitCatalogService;
	private final Dispatcher explicitDispatcher;

	public ProtocolSimulationService() {
		this(null, null, null, null);
	}

	public ProtocolSimulationService(Processor processor, PlayerInspectService inspectService, ProtoCatalogService catalogService) {
		this(processor, inspectService, catalogService, null);
	}

	public ProtocolSimulationService(Processor processor, PlayerInspectService inspectService, ProtoCatalogService catalogService, Dispatcher dispatcher) {
		this.explicitProcessor = processor;
		this.explicitInspectService = inspectService != null ? inspectService : PlayerInspectService.getInstance();
		this.explicitCatalogService = catalogService != null ? catalogService : new ProtoCatalogService();
		this.explicitDispatcher = dispatcher != null ? dispatcher : new DispatcherImpl();
	}

	/**
	 * 执行协议模拟调用
	 *
	 * @param req 请求参数对象
	 * @return 结构化模拟执行结果 Map
	 */
	public Map<String, Object> invoke(ProtoInvokeRequest req) {
		if (req == null) {
			return failResult(0L, 0, null, 0, ErrorMsgEnum.request_parameter_error.getId(), "请求参数不能为空", null, 0L);
		}
		long startNs = System.nanoTime();

		// 1. 生产环境硬阻断 (BR-01, EX-01, INV-PROD-SIMULATION-BLOCKED)
		if (GmSecurityPolicy.currentRunMode().isProduction()) {
			return failResult(req.getPlayerId(), req.resolveMsgId(), req.getMsgName(), req.getSeq(),
					ErrorMsgEnum.production_gm_not_allow.getId(), "生产环境严禁通过Web控制台模拟协议发包", null, elapsedMs(startNs));
		}

		// 2. 协议号与消息名校验及黑名单拦截 (EX-02)
		int msgId = req.resolveMsgId();
		String msgName = req.getMsgName();
		Map<Integer, String> idNameMap = PbProtocol.idNameMap != null ? PbProtocol.idNameMap : Collections.emptyMap();

		if (msgId <= 0 && (msgName == null || msgName.isBlank())) {
			return failResult(req.getPlayerId(), 0, null, req.getSeq(),
					ErrorMsgEnum.request_parameter_error.getId(), "必须提供 msgId 或 msgName", null, elapsedMs(startNs));
		}
		if (msgId <= 0) {
			Integer resolvedId = PbProtocol.getInstance().getMsgIdOrNull(msgName.trim());
			if (resolvedId == null || resolvedId == 0) {
				return failResult(req.getPlayerId(), 0, msgName, req.getSeq(),
						ErrorMsgEnum.request_parameter_error.getId(), "未知协议名称: " + msgName, null, elapsedMs(startNs));
			}
			msgId = resolvedId;
		}
		String mappedName = idNameMap.get(msgId);
		if (mappedName == null) {
			return failResult(req.getPlayerId(), msgId, msgName, req.getSeq(),
					ErrorMsgEnum.request_parameter_error.getId(), "未知协议ID: " + HexUtil.toHexString(msgId), null, elapsedMs(startNs));
		}
		if (msgName != null && !msgName.isBlank() && !mappedName.equalsIgnoreCase(msgName.trim())) {
			return failResult(req.getPlayerId(), msgId, msgName, req.getSeq(),
					ErrorMsgEnum.request_parameter_error.getId(), "传入的 msgId [" + HexUtil.toHexString(msgId) + "] 与 msgName [" + msgName + "] 不匹配", null, elapsedMs(startNs));
		}
		msgName = mappedName;

		if (!msgName.contains("Request")) {
			return failResult(req.getPlayerId(), msgId, msgName, req.getSeq(),
					ErrorMsgEnum.request_parameter_error.getId(), "仅支持模拟客户端 Request 协议，不允许直接模拟: " + msgName, null, elapsedMs(startNs));
		}

		Set<Integer> registeredCmds = ProtoCatalogService.collectRegisteredHandlerCmds();
		boolean isRegistered = registeredCmds.contains(msgId) || this.explicitDispatcher != null;
		if (!FeatureWorkbenchService.isSimulatable(msgId) || !isRegistered) {
			String reason = FeatureWorkbenchService.getUnsupportedReason(msgId, isRegistered);
			return failResult(req.getPlayerId(), msgId, msgName, req.getSeq(),
					ErrorMsgEnum.request_parameter_error.getId(), reason != null ? reason : "该协议不支持模拟", null, elapsedMs(startNs));
		}

		// 3. Protobuf 解析校验 (EX-03)
		Parser<?> parser = PbProtocol.parsersMap.get(msgId);
		Descriptors.Descriptor descriptor = ProtoCatalogService.resolveDescriptor(parser);
		if (parser == null || descriptor == null) {
			return failResult(req.getPlayerId(), msgId, msgName, req.getSeq(),
					ErrorMsgEnum.request_parameter_error.getId(), "无法获取协议解析器或Descriptor: " + msgName, null, elapsedMs(startNs));
		}

		Message requestMessage;
		try {
			requestMessage = ProtoJsonCodec.fromJson(parser, descriptor, req.getPayload());
		} catch (IllegalArgumentException e) {
			return failResult(req.getPlayerId(), msgId, msgName, req.getSeq(),
					ErrorMsgEnum.request_parameter_error.getId(), "参数反序列化失败: " + e.getMessage(), null, elapsedMs(startNs));
		} catch (Exception e) {
			log.warn("Protobuf JSON 解析异常: msgName={}", msgName, e);
			return failResult(req.getPlayerId(), msgId, msgName, req.getSeq(),
					ErrorMsgEnum.request_parameter_error.getId(), "JSON结构异常: " + e.getMessage(), null, elapsedMs(startNs));
		}

		// 4. 目标玩家在线状态与连接校验 (EX-04, INV-ONLINE-PLAYER-REQUIRED)
		long playerId = req.getPlayerId();
		if (playerId <= 0) {
			return failResult(playerId, msgId, msgName, req.getSeq(),
					ErrorMsgEnum.request_parameter_error.getId(), "目标 playerId 必须大于 0", requestMessage, elapsedMs(startNs));
		}
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (player == null || player.isIslogouting() || !player.isOnline()) {
			return failResult(playerId, msgId, msgName, req.getSeq(),
					ErrorMsgEnum.player_not_online.getId(), "目标玩家不存在、未在线或正在登出中: " + playerId, requestMessage, elapsedMs(startNs));
		}
		// 5. 调度到玩家虚拟线程邮箱内执行并阻塞等待 (BR-05, DC-04)
		Processor processor = this.explicitProcessor != null ? this.explicitProcessor : ServerContext.getInstance().getProcessor();
		if (processor == null) {
			return failResult(playerId, msgId, msgName, req.getSeq(),
					ErrorMsgEnum.unknown.getId(), "服务端邮箱处理器未初始化", requestMessage, elapsedMs(startNs));
		}

		int timeoutMs = Math.max(500, Math.min(30000, req.getTimeoutMs()));
		CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

		final int finalMsgId = msgId;
		final String finalMsgName = msgName;
		final Message finalReqMessage = requestMessage;

		processor.process(playerId, () -> {
			long invokeStartNs = System.nanoTime();
			Map<String, Object> beforeSnapshot = null;
			TraceBuilder traceBuilder = null;
			ProtocolCaptureSession captureSession = null;
			Throwable executionException = null;
			int finalErrorCode = 0;
			List<String> warnings = new ArrayList<>();

			// 确保虚拟客户端安全挂载 (在玩家邮箱线程内安全兜底)
			GameClient effectiveClient = player.getGameClient();
			if (effectiveClient == null) {
				effectiveClient = new VirtualGameClient(playerId);
				player.setGameClient(effectiveClient);
			}
			final GameClient finalClient = effectiveClient;

			try {
				// (1) 采集前置快照
				if (req.isWithDiff()) {
					try {
						beforeSnapshot = explicitInspectService.captureModulesSnapshot(player, req.getDiffModules());
					} catch (Throwable t) {
						log.warn("模拟发包前置Diff快照采集异常 playerId={}", playerId, t);
						warnings.add("前置Diff快照采集异常: " + t.getMessage());
					}
				}

				// (2) 开启隔离 Trace
				if (req.isWithTrace()) {
					try {
						traceBuilder = RequestTraceService.getInstance().beginForcedTrace(playerId, finalMsgId, finalMsgName, req.getSeq());
					} catch (Throwable t) {
						log.warn("开启强制Trace录制异常 playerId={}", playerId, t);
						warnings.add("开启强制Trace失败: " + t.getMessage());
					}
				}

				// (3) 挂载 ThreadLocal 捕获探针
				captureSession = new ProtocolCaptureSession(playerId, finalClient, req.isMirrorToRealClient());
				GameClient.beginThreadLocalCapture(captureSession);

				// (4) 构造正式协议对象并经由 Dispatcher 分发
				IProtocol<?> protocolObj = new ProtobufProtocol(finalMsgId, finalReqMessage, req.getSeq());
				Dispatcher dispatcher = this.explicitDispatcher != null ? this.explicitDispatcher : new DispatcherImpl();
				dispatcher.dispatch(finalClient, protocolObj);

			} catch (LogicException le) {
				executionException = le;
				finalErrorCode = le.getErrorCode();
			} catch (Throwable t) {
				executionException = t;
				finalErrorCode = ErrorMsgEnum.unknown.getId();
				log.warn("协议模拟业务执行抛出异常 playerId={} msg={}", playerId, finalMsgName, t);
			} finally {
				// 严格无条件清理探针与 Trace 上下文 (EX-06, INV-CAPTURE-HOOK-CLEANUP)
				GameClient.endThreadLocalCapture();

				Object traceRecord = null;
				if (traceBuilder != null) {
					try {
						traceRecord = RequestTraceService.getInstance().endForcedTrace(traceBuilder, finalErrorCode);
					} catch (Throwable t) {
						log.warn("结束强制Trace清理异常 playerId={}", playerId, t);
					}
				}

				// 采集后置快照并计算 Diff
				List<Map<String, Object>> diffList = null;
				if (req.isWithDiff() && beforeSnapshot != null) {
					try {
						Map<String, Object> afterSnapshot = explicitInspectService.captureModulesSnapshot(player, req.getDiffModules());
						diffList = GmStateDiffUtil.computeNestedDiff(beforeSnapshot, afterSnapshot);
					} catch (Throwable t) {
						log.warn("模拟发包后置Diff快照比对异常 playerId={}", playerId, t);
						warnings.add("后置Diff快照比对异常: " + t.getMessage());
					}
				}

				// 整理捕获包结果
				Map<String, Object> outcome = assembleOutcome(playerId, finalMsgId, finalMsgName, req.getSeq(),
						finalReqMessage, captureSession, finalErrorCode, executionException, traceRecord, diffList, warnings, elapsedMs(invokeStartNs));
				future.complete(outcome);
			}
		});

		try {
			return future.get(timeoutMs, TimeUnit.MILLISECONDS);
		} catch (TimeoutException te) {
			log.warn("协议模拟等待邮箱执行超时 playerId={} msgId={}", playerId, HexUtil.toHexString(msgId));
			return failResult(playerId, msgId, msgName, req.getSeq(),
					ErrorMsgEnum.unknown.getId(), "协议在玩家邮箱内执行超时 (" + timeoutMs + "ms)", requestMessage, elapsedMs(startNs));
		} catch (Exception e) {
			log.warn("协议模拟等待执行中断或异常 playerId={}", playerId, e);
			return failResult(playerId, msgId, msgName, req.getSeq(),
					ErrorMsgEnum.unknown.getId(), "执行中断: " + e.getMessage(), requestMessage, elapsedMs(startNs));
		}
	}

	private Map<String, Object> assembleOutcome(long playerId, int reqMsgId, String reqMsgName, int seq,
			Message reqMessage, ProtocolCaptureSession session, int explicitErrorCode, Throwable executionException,
			Object traceRecord, List<Map<String, Object>> diffList, List<String> warnings, long elapsedMs) {

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("schemaVersion", "1.0");

		Map<String, Object> reqInfo = new LinkedHashMap<>();
		reqInfo.put("msgId", reqMsgId);
		reqInfo.put("msgIdHex", HexUtil.toHexString(reqMsgId));
		reqInfo.put("msgName", reqMsgName);
		reqInfo.put("seq", seq);
		reqInfo.put("payload", reqMessage != null ? ProtoJsonCodec.toMap(reqMessage) : Collections.emptyMap());
		result.put("request", reqInfo);
		result.put("playerId", playerId);
		result.put("elapsedMs", elapsedMs);

		List<ProtocolCaptureSession.CapturedPacket> captured = session != null ? session.getCapturedPackets() : Collections.emptyList();
		ProtocolCaptureSession.CapturedPacket mainRespPacket = null;
		ProtocolCaptureSession.CapturedPacket errorPushPacket = null;
		List<Map<String, Object>> pushes = new ArrayList<>();

		Integer pairedRespId = ProtoCatalogService.resolvePairedResponseId(reqMsgId, PbProtocol.idNameMap);

		for (ProtocolCaptureSession.CapturedPacket packet : captured) {
			if (packet.getMsgId() == PbProtocol.PlayerErrorPush_01000099) {
				errorPushPacket = packet;
				continue;
			}
			if (mainRespPacket == null) {
				if (pairedRespId != null && packet.getMsgId() == pairedRespId) {
					mainRespPacket = packet;
					continue;
				} else if (packet.getMsgName().contains("Response")) {
					mainRespPacket = packet;
					continue;
				}
			}
			pushes.add(formatPacket(packet));
		}

		int finalErrCode = 0;
		if (explicitErrorCode != 0) {
			finalErrCode = explicitErrorCode;
		} else if (mainRespPacket != null && mainRespPacket.getErrorCode() != 0) {
			finalErrCode = mainRespPacket.getErrorCode();
		} else if (errorPushPacket != null && errorPushPacket.getErrorCode() != 0) {
			finalErrCode = errorPushPacket.getErrorCode();
		} else {
			for (ProtocolCaptureSession.CapturedPacket packet : captured) {
				if (packet.getErrorCode() != 0) {
					finalErrCode = packet.getErrorCode();
					break;
				}
			}
		}

		boolean ok = finalErrCode == 0 && executionException == null;
		result.put("ok", ok);
		result.put("response", mainRespPacket != null ? formatPacket(mainRespPacket) : null);
		result.put("errorPush", errorPushPacket != null ? formatPacket(errorPushPacket) : null);
		result.put("pushes", pushes);

		result.put("errorCode", finalErrCode);
		ErrorMsgEnum errEnum = findErrorMsgEnum(finalErrCode);
		if (finalErrCode == 0) {
			result.put("errorName", "ok");
			result.put("errorDesc", "成功");
		} else if (errEnum != null) {
			result.put("errorName", errEnum.name());
			result.put("errorDesc", errEnum.getDesc());
		} else {
			result.put("errorName", "error_" + finalErrCode);
			result.put("errorDesc", executionException != null ? executionException.getMessage() : "业务返回非零错误码");
		}

		if (traceRecord instanceof RequestTraceService.TraceRecord tr) {
			Map<String, Object> traceMap = new LinkedHashMap<>();
			traceMap.put("traceId", tr.getTraceId());
			traceMap.put("playerId", tr.getPlayerId());
			traceMap.put("msgId", tr.getMsgId());
			traceMap.put("msgIdHex", HexUtil.toHexString(tr.getMsgId()));
			traceMap.put("msgName", tr.getMsgName());
			traceMap.put("seq", tr.getSeq());
			traceMap.put("startTimeMs", tr.getStartTimeMs());
			traceMap.put("elapsedMs", tr.getElapsedMs());
			traceMap.put("errorCode", tr.getErrorCode());
			traceMap.put("resourceEvents", tr.getResourceEvents());
			traceMap.put("errorEvents", tr.getErrorEvents());
			result.put("trace", traceMap);
		} else {
			result.put("trace", traceRecord);
		}
		result.put("diff", diffList);

		if (mainRespPacket == null && executionException == null && pushes.isEmpty()) {
			warnings.add("本次请求未在同步分发窗口内捕获到回包，若目标协议采用异步Future回调则实际响应与Diff可能延迟发生");
		}
		result.put("warnings", warnings);
		return result;
	}

	private Map<String, Object> formatPacket(ProtocolCaptureSession.CapturedPacket packet) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("index", packet.getIndex());
		map.put("msgId", packet.getMsgId());
		map.put("msgIdHex", packet.getMsgIdHex());
		map.put("msgName", packet.getMsgName());
		map.put("seq", packet.getSeq());
		map.put("errorCode", packet.getErrorCode());

		Object payloadObj = packet.getPayload();
		if (payloadObj == null && packet.getData() != null) {
			Parser<?> parser = PbProtocol.parsersMap.get(packet.getMsgId());
			if (parser != null) {
				try {
					Message parsedMsg = (Message) parser.parseFrom(packet.getData());
					payloadObj = ProtoJsonCodec.toMap(parsedMsg);
				} catch (Exception e) {
					log.warn("反序列化捕获协议包为Map失败, 降级展示rawLength msgId={}", packet.getMsgId(), e);
					payloadObj = Collections.singletonMap("_rawLength", packet.getData().length);
				}
			} else {
				payloadObj = Collections.singletonMap("_rawLength", packet.getData().length);
			}
			packet.setPayload(payloadObj);
		}
		map.put("payload", payloadObj != null ? payloadObj : Collections.emptyMap());
		return map;
	}

	private Map<String, Object> failResult(long playerId, int msgId, String msgName, int seq, int errCode, String msg, Message reqMsg, long elapsedMs) {
		Map<String, Object> res = new LinkedHashMap<>();
		res.put("schemaVersion", "1.0");
		res.put("ok", false);
		res.put("playerId", playerId);
		res.put("elapsedMs", elapsedMs);

		Map<String, Object> reqInfo = new LinkedHashMap<>();
		reqInfo.put("msgId", msgId);
		reqInfo.put("msgIdHex", HexUtil.toHexString(msgId));
		reqInfo.put("msgName", msgName != null ? msgName : (msgId > 0 && PbProtocol.idNameMap != null && PbProtocol.idNameMap.containsKey(msgId) ? PbProtocol.idNameMap.get(msgId) : "Unknown"));
		reqInfo.put("seq", seq);
		reqInfo.put("payload", reqMsg != null ? ProtoJsonCodec.toMap(reqMsg) : Collections.emptyMap());
		res.put("request", reqInfo);

		res.put("response", null);
		res.put("errorPush", null);
		res.put("pushes", Collections.emptyList());
		res.put("errorCode", errCode);
		ErrorMsgEnum errEnum = findErrorMsgEnum(errCode);
		res.put("errorName", errEnum != null ? errEnum.name() : "error_" + errCode);
		res.put("errorDesc", msg != null ? msg : (errEnum != null ? errEnum.getDesc() : ""));
		res.put("trace", null);
		res.put("diff", null);
		res.put("warnings", Collections.emptyList());
		return res;
	}

	private static ErrorMsgEnum findErrorMsgEnum(int errorCode) {
		for (ErrorMsgEnum err : ErrorMsgEnum.values()) {
			if (err.getId() == errorCode) {
				return err;
			}
		}
		return null;
	}

	private static long elapsedMs(long startNs) {
		return Math.max(0L, (System.nanoTime() - startNs) / 1_000_000L);
	}

	/**
	 * 模拟发包请求参数
	 */
	public static class ProtoInvokeRequest {
		private long playerId;
		private Integer msgId;
		private String msgName;
		private Object payload;
		private int seq = 1;
		private boolean withTrace = true;
		private boolean withDiff = false;
		private List<String> diffModules;
		private boolean mirrorToRealClient = false;
		private int timeoutMs = 5000;

		public int resolveMsgId() {
			if (msgId != null && msgId != 0) {
				return msgId;
			}
			return 0;
		}

		public long getPlayerId() { return playerId; }
		public void setPlayerId(long playerId) { this.playerId = playerId; }
		public Integer getMsgId() { return msgId; }
		public void setMsgId(Integer msgId) { this.msgId = msgId; }
		public String getMsgName() { return msgName; }
		public void setMsgName(String msgName) { this.msgName = msgName; }
		public Object getPayload() { return payload; }
		public void setPayload(Object payload) { this.payload = payload; }
		public int getSeq() { return seq; }
		public void setSeq(int seq) { this.seq = seq; }
		public boolean isWithTrace() { return withTrace; }
		public void setWithTrace(boolean withTrace) { this.withTrace = withTrace; }
		public boolean isWithDiff() { return withDiff; }
		public void setWithDiff(boolean withDiff) { this.withDiff = withDiff; }
		public List<String> getDiffModules() { return diffModules; }
		public void setDiffModules(List<String> diffModules) { this.diffModules = diffModules; }
		public boolean isMirrorToRealClient() { return mirrorToRealClient; }
		public void setMirrorToRealClient(boolean mirrorToRealClient) { this.mirrorToRealClient = mirrorToRealClient; }
		public int getTimeoutMs() { return timeoutMs; }
		public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
	}
}
