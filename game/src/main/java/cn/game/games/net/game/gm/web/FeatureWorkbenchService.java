package cn.game.games.net.game.gm.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Parser;

import cn.game.core.base.RunMode;
import cn.game.games.net.game.gm.GmActionSchema;
import cn.game.games.net.game.gm.GmDispatcher;
import cn.game.games.net.game.gm.GmSchemaCatalog;
import cn.game.games.net.game.gm.GmSecurityPolicy;
import cn.game.games.net.game.gm.ProtoCatalogService;
import cn.game.games.net.game.gm.ProtoCatalogService.ProtoFileMeta;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.HexUtil;

/**
 * 全服功能模块目录自动聚合与检索服务 (DC-02, BR-02, AC-02)
 * 严格依据 xxx.proto 中声明的 //@MessageModule（十六进制模块号）生成模块列表，
 * 并聚合对应模块协议、请求参数Schema、配对响应、伴随推送、错误码、关联快捷GM动作与自定义面板。
 *
 * @author shuyongqiang
 */
public class FeatureWorkbenchService {

	private static final Logger log = LoggerFactory.getLogger(FeatureWorkbenchService.class);

	// 不可模拟系统协议黑名单
	public static final int CMD_LOGIN_REQ = 0x01000001;      // PlayerLoginRequest_01000001 (多阶段登录走专用接口)
	public static final int CMD_LOGOUT_REQ = 0x01000003;     // PlayerLogoutRequest_01000003 (退出登录请求，禁止模拟防误踢)
	public static final int CMD_HEARTBEAT_REQ = 0x01000005;  // PlayerHeartbeatRequest_01000005 (心跳)
	public static final int CMD_RECONNECT_REQ = 0x01000007;  // ReconnectRequest_01000007 (重连握手)
	public static final int MODULE_SERVER_INTERNAL = 0x7d;   // ServerMsg (跨服内部协议)

	private final ProtoCatalogService protoCatalogService;

	/**
	 * 使用默认协议目录服务构造工作台聚合器
	 */
	public FeatureWorkbenchService() {
		this(new ProtoCatalogService());
	}

	/**
	 * 使用指定协议目录服务构造工作台聚合器
	 *
	 * @param protoCatalogService 协议目录服务
	 */
	public FeatureWorkbenchService(ProtoCatalogService protoCatalogService) {
		this.protoCatalogService = protoCatalogService;
	}

	/**
	 * 构建全服功能工作台目录（仅收录显式声明 //@MessageModule 的 .proto 模块）
	 *
	 * @param keyword 可选过滤关键词（支持按模块名、中文标题、Key、协议名、协议号模糊搜索）
	 * @return 结构化目录响应 Map
	 */
	public Map<String, Object> buildCatalog(String keyword) {
		RunMode runMode = GmSecurityPolicy.currentRunMode();
		boolean simulationEnabled = !runMode.isProduction();

		Map<String, ProtoFileMeta> fileMetas = protoCatalogService.getProtoFileMetas();
		Map<String, String> comments = protoCatalogService.getMessageComments();
		Set<Integer> registeredCmds = ProtoCatalogService.collectRegisteredHandlerCmds();
		Map<Integer, String> idNameMap = PbProtocol.idNameMap != null ? PbProtocol.idNameMap : Collections.emptyMap();
		Map<Integer, Parser<?>> parsersMap = PbProtocol.parsersMap != null ? PbProtocol.parsersMap : Collections.emptyMap();

		Map<Integer, ModuleBuilder> moduleMap = new LinkedHashMap<>();

		// 1. 严格仅由含 //@MessageModule 的 .proto 文件初始化模块目录
		for (ProtoFileMeta meta : fileMetas.values()) {
			int modId = meta.getModuleId();
			if (modId > 0 && !moduleMap.containsKey(modId)) {
				moduleMap.put(modId, new ModuleBuilder(
						modId,
						meta.getModuleIdHex(),
						meta.getModuleKey(),
						meta.getTitle(),
						meta.getFunctionName(),
						meta.getHandlerPackage()));
			}
		}

		// 2. 按协议号升序遍历 PbProtocol，仅分配至已声明 //@MessageModule 的所属模块
		List<Integer> sortedMsgIds = new ArrayList<>(idNameMap.keySet());
		Collections.sort(sortedMsgIds);
		for (Integer msgId : sortedMsgIds) {
			if (msgId == null) {
				continue;
			}
			String msgName = idNameMap.get(msgId);
			if (msgName == null) {
				continue;
			}
			int modId = (msgId >>> 24) & 0xFF;
			ModuleBuilder mb = moduleMap.get(modId);
			if (mb == null) {
				continue;
			}

			boolean isRegistered = registeredCmds.contains(msgId);
			if (msgName.contains("Request")) {
				Integer pairedRespId = ProtoCatalogService.resolvePairedResponseId(msgId, idNameMap);
				String pairedRespName = pairedRespId != null ? idNameMap.get(pairedRespId) : null;
				boolean simulatable = isRegistered && isSimulatable(msgId);
				String reason = getUnsupportedReason(msgId, isRegistered);

				Descriptor reqDesc = ProtoCatalogService.resolveDescriptor(parsersMap.get(msgId));
				String msgComment = reqDesc != null
						? ProtoCatalogService.lookupMessageComment(comments, reqDesc.getFullName(), reqDesc.getName())
						: "";
				Map<String, Object> reqSchema = reqDesc != null
						? ProtoCatalogService.describeMessage(reqDesc, comments, 0)
						: null;

				Map<String, Object> reqInfo = new LinkedHashMap<>();
				reqInfo.put("msgId", msgId);
				reqInfo.put("msgIdHex", HexUtil.toHexString(msgId));
				reqInfo.put("msgName", msgName);
				reqInfo.put("comment", msgComment);
				reqInfo.put("registered", isRegistered);
				reqInfo.put("simulatable", simulatable);
				if (!simulatable) {
					reqInfo.put("unsupportedReason", reason);
				}
				reqInfo.put("pairedResponseId", pairedRespId);
				reqInfo.put("pairedResponseHex", pairedRespId != null ? HexUtil.toHexString(pairedRespId) : null);
				reqInfo.put("pairedResponseName", pairedRespName);
				reqInfo.put("reqSchema", reqSchema);
				mb.requests.add(reqInfo);
			} else if (msgName.contains("Push")) {
				Descriptor pushDesc = ProtoCatalogService.resolveDescriptor(parsersMap.get(msgId));
				String pushComment = pushDesc != null
						? ProtoCatalogService.lookupMessageComment(comments, pushDesc.getFullName(), pushDesc.getName())
						: "";
				Map<String, Object> pushInfo = new LinkedHashMap<>();
				pushInfo.put("msgId", msgId);
				pushInfo.put("msgIdHex", HexUtil.toHexString(msgId));
				pushInfo.put("msgName", msgName);
				pushInfo.put("comment", pushComment);
				mb.pushes.add(pushInfo);
			}
		}

		// 3. 关联同模块ErrorMsgEnum错误码
		for (ErrorMsgEnum err : ErrorMsgEnum.values()) {
			int errId = err.getId();
			int modId = (errId >>> 24) & 0xFF;
			ModuleBuilder mb = moduleMap.get(modId);
			if (mb != null) {
				Map<String, Object> errInfo = new LinkedHashMap<>();
				errInfo.put("id", errId);
				errInfo.put("name", err.name());
				errInfo.put("desc", err.getDesc());
				mb.errors.add(errInfo);
			}
		}

		// 4. 关联 GM 动作与自定义面板
		GmSchemaCatalog schemaCatalog = GmDispatcher.getInstance().getSchemaCatalog(runMode);
		List<Map<String, Object>> commonActions = new ArrayList<>();
		List<Map<String, Object>> allGmActions = extractActions(schemaCatalog);
		List<Map<String, Object>> allPanels = GmConsolePanelLoader.listPanels(runMode);

		for (Map<String, Object> act : allGmActions) {
			String actionId = String.valueOf(act.getOrDefault("id", ""));
			String actionName = String.valueOf(act.getOrDefault("action", ""));
			if (actionId.startsWith("time.")
					|| actionId.startsWith("gm.inspect")
					|| actionId.startsWith("gm.trace.")
					|| actionId.startsWith("gm.resetModule")
					|| actionName.startsWith("time.")
					|| actionName.startsWith("gm.inspect")
					|| actionName.startsWith("gm.trace.")
					|| actionName.startsWith("gm.resetModule")) {
				commonActions.add(act);
			}
		}

		for (ModuleBuilder mb : moduleMap.values()) {
			String key = mb.moduleKey.toLowerCase(Locale.ROOT);
			for (Map<String, Object> act : allGmActions) {
				String actionName = String.valueOf(act.get("action")).toLowerCase(Locale.ROOT);
				String groupName = String.valueOf(act.get("group")).toLowerCase(Locale.ROOT);
				if (groupName.equals(key)
						|| actionName.startsWith(key + ".")
						|| actionName.contains("." + key)
						|| (key.length() > 3 && actionName.contains(key))) {
					mb.actions.add(act);
				}
			}
			for (Map<String, Object> panel : allPanels) {
				if (GmConsolePanelLoader.matchesModule(panel, mb.moduleId, mb.moduleIdHex, mb.moduleKey)) {
					mb.matchedPanels.add(panel);
				}
			}
		}

		// 5. 排序与关键词过滤（严格保留所有含 //@MessageModule 声明的模块）
		List<Map<String, Object>> moduleList = new ArrayList<>();
		String normKeyword = keyword != null && !keyword.isBlank() ? keyword.trim().toLowerCase(Locale.ROOT) : null;

		for (ModuleBuilder mb : moduleMap.values()) {
			Map<String, Object> modObj = mb.toMap();
			if (normKeyword != null && !matchesKeyword(modObj, normKeyword)) {
				continue;
			}
			moduleList.add(modObj);
		}

		moduleList.sort(Comparator.comparingInt(m -> (int) m.get("moduleId")));

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("schemaVersion", "1.0");
		response.put("runMode", runMode.name());
		response.put("simulationEnabled", simulationEnabled);
		response.put("modules", moduleList);
		response.put("commonActions", commonActions);
		return response;
	}

	/**
	 * 检查协议是否允许通用模拟
	 *
	 * @param msgId 协议号
	 * @return true 表示允许通用业务模拟
	 */
	public static boolean isSimulatable(int msgId) {
		if (msgId == CMD_LOGIN_REQ || msgId == CMD_LOGOUT_REQ || msgId == CMD_HEARTBEAT_REQ || msgId == CMD_RECONNECT_REQ) {
			return false;
		}
		if (((msgId >>> 24) & 0xFF) == MODULE_SERVER_INTERNAL) {
			return false;
		}
		return true;
	}

	/**
	 * 获取协议不可模拟的具体原因说明
	 *
	 * @param msgId 协议号
	 * @param isRegistered 是否已在服务端绑定 Handler Invoker
	 * @return 原因描述（可模拟时返回 null）
	 */
	public static String getUnsupportedReason(int msgId, boolean isRegistered) {
		if (msgId == CMD_LOGIN_REQ) {
			return "系统多阶段登录握手协议，请走专属测试登录入口";
		}
		if (msgId == CMD_LOGOUT_REQ) {
			return "系统退出登录协议 (PlayerLogoutRequest_01000003)，禁止通用模拟以防意外踢出在线玩家";
		}
		if (msgId == CMD_HEARTBEAT_REQ) {
			return "底层连接心跳保活协议，不适合请求级业务模拟";
		}
		if (msgId == CMD_RECONNECT_REQ) {
			return "网络断线重连握手协议，依赖底层会话通道";
		}
		if (((msgId >>> 24) & 0xFF) == MODULE_SERVER_INTERNAL) {
			return "跨服内部服务通讯协议，不支持客户端模拟";
		}
		if (!isRegistered) {
			return "未在服务端注册业务处理器";
		}
		return null;
	}

	private static List<Map<String, Object>> extractActions(GmSchemaCatalog schemaCatalog) {
		if (schemaCatalog == null || schemaCatalog.getActions() == null) {
			return Collections.emptyList();
		}
		List<Map<String, Object>> actions = new ArrayList<>();
		for (GmActionSchema schema : schemaCatalog.getActions()) {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("id", schema.getId());
			map.put("group", schema.getGroup());
			map.put("action", schema.getAction());
			map.put("description", schema.getDescription());
			map.put("security", schema.getSecurity() != null ? schema.getSecurity().name() : null);
			map.put("scope", schema.getScope() != null ? schema.getScope().name() : null);
			map.put("example", schema.getExample());
			map.put("dryRunSupported", schema.isDryRunSupported());
			actions.add(map);
		}
		return actions;
	}

	@SuppressWarnings("unchecked")
	private static boolean matchesKeyword(Map<String, Object> mod, String keyword) {
		if (String.valueOf(mod.get("title")).toLowerCase(Locale.ROOT).contains(keyword)) {
			return true;
		}
		if (String.valueOf(mod.get("moduleKey")).toLowerCase(Locale.ROOT).contains(keyword)) {
			return true;
		}
		if (String.valueOf(mod.get("moduleIdHex")).toLowerCase(Locale.ROOT).contains(keyword)) {
			return true;
		}
		if (String.valueOf(mod.get("moduleId")).contains(keyword)) {
			return true;
		}
		String fn = mod.get("functionName") != null ? String.valueOf(mod.get("functionName")).toLowerCase(Locale.ROOT) : "";
		if (fn.contains(keyword)) {
			return true;
		}

		List<Map<String, Object>> reqs = (List<Map<String, Object>>) mod.get("requests");
		if (reqs != null) {
			for (Map<String, Object> req : reqs) {
				if (String.valueOf(req.get("msgName")).toLowerCase(Locale.ROOT).contains(keyword)) {
					return true;
				}
				if (String.valueOf(req.get("msgIdHex")).toLowerCase(Locale.ROOT).contains(keyword)) {
					return true;
				}
				String cmt = req.get("comment") != null ? String.valueOf(req.get("comment")).toLowerCase(Locale.ROOT) : "";
				if (cmt.contains(keyword)) {
					return true;
				}
			}
		}
		return false;
	}

	private static class ModuleBuilder {
		final int moduleId;
		final String moduleIdHex;
		final String moduleKey;
		final String title;
		final String functionName;
		final String handlerPackage;
		final List<Map<String, Object>> requests = new ArrayList<>();
		final List<Map<String, Object>> pushes = new ArrayList<>();
		final List<Map<String, Object>> errors = new ArrayList<>();
		final List<Map<String, Object>> actions = new ArrayList<>();
		final List<Map<String, Object>> matchedPanels = new ArrayList<>();

		ModuleBuilder(int moduleId, String moduleIdHex, String moduleKey, String title, String functionName, String handlerPackage) {
			this.moduleId = moduleId;
			this.moduleIdHex = moduleIdHex;
			this.moduleKey = moduleKey;
			this.title = title;
			this.functionName = functionName;
			this.handlerPackage = handlerPackage;
		}

		Map<String, Object> toMap() {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("moduleId", moduleId);
			map.put("moduleIdHex", moduleIdHex);
			map.put("moduleKey", moduleKey);
			map.put("title", title);
			map.put("functionName", functionName);
			map.put("moduleClassHint", capitalize(moduleKey) + "Module");
			map.put("requests", requests);
			map.put("pushes", pushes);
			map.put("errors", errors);
			map.put("actions", actions);
			map.put("matchedPanels", matchedPanels);
			map.put("acceptanceSuite", cn.game.games.net.game.gm.acceptance.ModuleAcceptanceEngine.getInstance().getSuiteSummary(this.moduleId));
			return map;
		}

		private static String capitalize(String str) {
			if (str == null || str.isEmpty()) {
				return str;
			}
			return Character.toUpperCase(str.charAt(0)) + str.substring(1);
		}
	}
}