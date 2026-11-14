package cn.game.games.net.game.gm;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Parser;

import cn.game.core.net.socket.controller.DispatcherImpl;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.core.net.socket.handler.Handler;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;

/**
 * 协议与错误码目录服务，基于PbProtocol、DispatcherImpl与Protobuf Descriptor实时构建
 *
 * @author shuyongqiang
 */
@Service
public class ProtoCatalogService {

	private static final Logger log = LoggerFactory.getLogger(ProtoCatalogService.class);

	public static final int MAX_SCHEMA_DEPTH = 8;

	private static final Pattern PACKAGE_DECL_PATTERN = Pattern.compile("^\\s*package\\s+([A-Za-z0-9_.]+)\\s*;");
	private static final Pattern MESSAGE_DECL_PATTERN = Pattern.compile("^\\s*message\\s+([A-Za-z0-9_]+)\\s*\\{?");
	private static final Pattern FIELD_DECL_PATTERN = Pattern
			.compile("^\\s*(?:optional|required|repeated)?\\s*[A-Za-z0-9_.<>\\s]+\\s+([A-Za-z0-9_]+)\\s*=\\s*\\d+");

	private final Path protoSourceDir;
	private volatile Map<String, String> cachedComments;

	/**
	 * 使用默认proto源码目录构造协议目录服务
	 */
	public ProtoCatalogService() {
		this(resolveDefaultProtoDir());
	}

	/**
	 * 指定proto源码目录构造协议目录服务
	 *
	 * @param protoSourceDir proto源码根目录
	 */
	public ProtoCatalogService(Path protoSourceDir) {
		this.protoSourceDir = protoSourceDir;
	}

	/**
	 * 严格按Request + 1 = Response规则查找配对响应ID（目标名称必须含Response）
	 *
	 * @param reqId 请求协议ID
	 * @param idNameMap 协议ID到名称映射
	 * @return 配对响应ID，不满足条件时返回null
	 */
	public static Integer resolvePairedResponseId(int reqId, Map<Integer, String> idNameMap) {
		if (idNameMap == null) {
			return null;
		}
		int candidateId = reqId + 1;
		String candidateName = idNameMap.get(candidateId);
		if (candidateName != null && candidateName.contains("Response")) {
			return candidateId;
		}
		return null;
	}

	/**
	 * 递归提取Protobuf消息结构描述，深度达到MAX_SCHEMA_DEPTH(8)时返回truncated截断标记
	 *
	 * @param descriptor 消息Descriptor
	 * @param comments 可选注释字典
	 * @param depth 当前递归深度（根为0）
	 * @return 结构化Schema描述
	 */
	public static Map<String, Object> describeMessage(Descriptor descriptor, Map<String, String> comments, int depth) {
		if (descriptor == null) {
			return null;
		}
		Map<String, Object> node = new LinkedHashMap<>();
		node.put("type", descriptor.getFullName());
		if (depth >= MAX_SCHEMA_DEPTH) {
			node.put("truncated", true);
			return node;
		}
		node.put("truncated", false);
		node.put("comment", lookupComment(comments, descriptor.getFullName(), descriptor.getName()));
		List<Map<String, Object>> fields = new ArrayList<>();
		for (FieldDescriptor field : descriptor.getFields()) {
			Map<String, Object> f = new LinkedHashMap<>();
			f.put("name", field.getName());
			f.put("number", field.getNumber());
			f.put("protoType", field.getType().name());
			f.put("javaType", field.getJavaType().name());
			f.put("repeated", field.isRepeated());
			f.put("mapField", field.isMapField());
			String fullFieldKey = descriptor.getFullName() + "." + field.getName();
			String shortFieldKey = descriptor.getName() + "." + field.getName();
			f.put("comment", lookupComment(comments, fullFieldKey, shortFieldKey));
			if (field.getJavaType() == FieldDescriptor.JavaType.ENUM) {
				List<String> enumVals = new ArrayList<>();
				for (EnumValueDescriptor ev : field.getEnumType().getValues()) {
					enumVals.add(ev.getName() + "(" + ev.getNumber() + ")");
				}
				f.put("enumValues", enumVals);
			}
			if (field.getJavaType() == FieldDescriptor.JavaType.MESSAGE) {
				f.put("nestedSchema", describeMessage(field.getMessageType(), comments, depth + 1));
			}
			fields.add(f);
		}
		node.put("fields", fields);
		return node;
	}

	/**
	 * 列出已注册与PbProtocol索引的协议目录
	 *
	 * @param moduleFilter 可选模块过滤（如 0x77 或 77）
	 * @param keyword 可选协议名或ID关键字
	 * @return 协议目录视图
	 */
	public Map<String, Object> listCatalog(String moduleFilter, String keyword) {
		return listCatalog(moduleFilter, keyword, true, false);
	}

	/**
	 * 列出全服协议目录与配对关系，支持按需裁剪字段Schema与附带错误码字典
	 *
	 * @param moduleFilter 可选模块过滤（如 0x77 或 77）
	 * @param keyword 可选协议名或ID关键字
	 * @param includeSchemas 是否展开请求/响应/推送字段Schema
	 * @param includeErrors 是否附带完整ErrorMsgEnum错误码列表
	 * @return 协议目录视图
	 */
	public Map<String, Object> listCatalog(String moduleFilter, String keyword, boolean includeSchemas, boolean includeErrors) {
		Map<Integer, String> idNameMap = PbProtocol.idNameMap != null ? PbProtocol.idNameMap : Collections.emptyMap();
		Map<Integer, Parser<?>> parsersMap = PbProtocol.parsersMap != null
				? PbProtocol.parsersMap
				: Collections.emptyMap();
		Set<Integer> registeredCmds = collectRegisteredHandlerCmds();
		Map<String, String> comments = getCommentsSafe();

		String normalizedModule = normalizeModuleFilter(moduleFilter);
		String normalizedKeyword = keyword != null && !keyword.isBlank() ? keyword.trim().toLowerCase(Locale.ROOT) : null;

		List<Integer> sortedIds = new ArrayList<>(idNameMap.keySet());
		Collections.sort(sortedIds);

		List<Map<String, Object>> entries = new ArrayList<>();
		Map<String, List<Map<String, Object>>> modules = new LinkedHashMap<>();
		for (Integer msgId : sortedIds) {
			String name = idNameMap.get(msgId);
			if (name == null) {
				continue;
			}
			boolean isRequest = name.contains("Request_");
			boolean isPush = name.contains("Push_");
			if (!isRequest && !isPush) {
				continue;
			}
			String moduleHex = String.format("0x%02x", (msgId >>> 24) & 0xFF);
			if (normalizedModule != null && !normalizedModule.equalsIgnoreCase(moduleHex)) {
				continue;
			}

			Integer respId = isRequest ? resolvePairedResponseId(msgId, idNameMap) : null;
			String respName = respId != null ? idNameMap.get(respId) : null;
			if (normalizedKeyword != null) {
				String hexId = String.format("%08x", msgId);
				boolean hit = name.toLowerCase(Locale.ROOT).contains(normalizedKeyword)
						|| hexId.contains(normalizedKeyword)
						|| (respName != null && respName.toLowerCase(Locale.ROOT).contains(normalizedKeyword));
				if (!hit) {
					continue;
				}
			}

			Descriptor reqDesc = resolveDescriptor(parsersMap.get(msgId));
			String comment = reqDesc != null ? lookupComment(comments, reqDesc.getFullName(), reqDesc.getName()) : "";

			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("kind", isRequest ? "REQUEST" : "PUSH");
			entry.put("module", moduleHex);
			entry.put("registered", registeredCmds.contains(msgId));
			if (isRequest) {
				entry.put("reqId", msgId);
				entry.put("reqHex", String.format("0x%08x", msgId));
				entry.put("reqName", name);
				entry.put("respId", respId);
				entry.put("respHex", respId != null ? String.format("0x%08x", respId) : null);
				entry.put("respName", respName);
				if (includeSchemas) {
					entry.put("reqSchema", describeMessage(reqDesc, comments, 0));
					entry.put("respSchema", respId != null ? describeMessage(resolveDescriptor(parsersMap.get(respId)), comments, 0) : null);
				}
			} else {
				entry.put("pushId", msgId);
				entry.put("pushHex", String.format("0x%08x", msgId));
				entry.put("pushName", name);
				if (includeSchemas) {
					entry.put("pushSchema", describeMessage(reqDesc, comments, 0));
				}
			}
			entry.put("comment", comment);
			entries.add(entry);
			modules.computeIfAbsent(moduleHex, k -> new ArrayList<>()).add(entry);
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("schemaVersion", GmResult.SCHEMA_VERSION);
		result.put("total", entries.size());
		result.put("registeredCount", registeredCmds.size());
		result.put("modules", modules);
		result.put("entries", entries);
		if (includeErrors) {
			result.put("errorCodes", listErrors(null).get("errors"));
		}
		return result;
	}

	/**
	 * 列出完整ErrorMsgEnum错误码字典
	 *
	 * @param keyword 可选过滤关键字
	 * @return 错误码字典视图
	 */
	public Map<String, Object> listErrors(String keyword) {
		String normalizedKeyword = keyword != null && !keyword.isBlank() ? keyword.trim().toLowerCase(Locale.ROOT) : null;
		List<Map<String, Object>> list = new ArrayList<>();
		for (ErrorMsgEnum item : ErrorMsgEnum.values()) {
			if (normalizedKeyword != null) {
				boolean hit = item.name().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
						|| (item.getDesc() != null && item.getDesc().toLowerCase(Locale.ROOT).contains(normalizedKeyword))
						|| String.valueOf(item.getId()).equals(normalizedKeyword);
				if (!hit) {
					continue;
				}
			}
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("id", item.getId());
			row.put("name", item.name());
			row.put("desc", item.getDesc());
			row.put("disconnect", item.isNeedProcessed());
			list.add(row);
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("schemaVersion", GmResult.SCHEMA_VERSION);
		result.put("total", list.size());
		result.put("errors", list);
		return result;
	}

	private static String lookupComment(Map<String, String> comments, String fullKey, String shortKey) {
		if (comments == null || comments.isEmpty()) {
			return "";
		}
		String fullHit = comments.get(fullKey);
		if (fullHit != null && !fullHit.isEmpty()) {
			return fullHit;
		}
		if (shortKey != null) {
			String shortHit = comments.get(shortKey);
			if (shortHit != null) {
				return shortHit;
			}
		}
		return "";
	}

	private Set<Integer> collectRegisteredHandlerCmds() {
		Set<Integer> cmds = new HashSet<>();
		Map<Integer, Handler> handlers = DispatcherImpl.getModuleHandlers();
		for (Handler handler : handlers.values()) {
			if (handler instanceof BaseHandler baseHandler) {
				cmds.addAll(baseHandler.getRegisteredCmds());
			}
		}
		return cmds;
	}

	private Descriptor resolveDescriptor(Parser<?> parser) {
		if (parser == null) {
			return null;
		}
		try {
			Object defaultMsg = parser.parsePartialFrom(new byte[0]);
			if (defaultMsg instanceof com.google.protobuf.Message message) {
				return message.getDescriptorForType();
			}
		} catch (Throwable e) {
			log.warn("解析Protobuf Descriptor失败", e);
		}
		return null;
	}

	private Map<String, String> getCommentsSafe() {
		Map<String, String> cached = this.cachedComments;
		if (cached != null) {
			return cached;
		}
		synchronized (this) {
			if (this.cachedComments != null) {
				return this.cachedComments;
			}
			this.cachedComments = loadCommentsSafe(protoSourceDir);
			return this.cachedComments;
		}
	}

	private Map<String, String> loadCommentsSafe(Path dir) {
		if (dir == null || !Files.isDirectory(dir)) {
			log.info("Proto源码目录不存在，注释降级为空串: dir={}", dir);
			return Collections.emptyMap();
		}
		Map<String, String> comments = new HashMap<>();
		try (Stream<Path> walk = Files.walk(dir)) {
			walk.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".proto"))
					.forEach(p -> parseProtoComments(p, comments));
		} catch (Exception e) {
			log.warn("扫描proto注释目录失败，平稳降级为空注释: dir={}", dir, e);
		}
		return Collections.unmodifiableMap(comments);
	}

	private void parseProtoComments(Path protoFile, Map<String, String> comments) {
		try {
			List<String> lines = Files.readAllLines(protoFile, StandardCharsets.UTF_8);
			String currentPkg = null;
			String currentMsg = null;
			StringBuilder pendingComment = new StringBuilder();
			for (String rawLine : lines) {
				String line = rawLine.trim();
				if (line.startsWith("//")) {
					if (pendingComment.length() > 0) {
						pendingComment.append(' ');
					}
					pendingComment.append(line.substring(2).trim());
					continue;
				}
				Matcher pkgMatcher = PACKAGE_DECL_PATTERN.matcher(line);
				if (pkgMatcher.find()) {
					currentPkg = pkgMatcher.group(1);
					pendingComment.setLength(0);
					continue;
				}
				Matcher msgMatcher = MESSAGE_DECL_PATTERN.matcher(line);
				if (msgMatcher.find()) {
					currentMsg = msgMatcher.group(1);
					if (pendingComment.length() > 0) {
						String commentText = pendingComment.toString();
						comments.putIfAbsent(currentMsg, commentText);
						if (currentPkg != null && !currentPkg.isEmpty()) {
							comments.putIfAbsent(currentPkg + "." + currentMsg, commentText);
						}
						pendingComment.setLength(0);
					}
					continue;
				}
				if ("}".equals(line)) {
					currentMsg = null;
					pendingComment.setLength(0);
					continue;
				}
				if (currentMsg != null) {
					Matcher fieldMatcher = FIELD_DECL_PATTERN.matcher(line);
					if (fieldMatcher.find() && pendingComment.length() > 0) {
						String fieldName = fieldMatcher.group(1);
						String commentText = pendingComment.toString();
						comments.putIfAbsent(currentMsg + "." + fieldName, commentText);
						if (currentPkg != null && !currentPkg.isEmpty()) {
							comments.putIfAbsent(currentPkg + "." + currentMsg + "." + fieldName, commentText);
						}
						pendingComment.setLength(0);
						continue;
					}
				}
				if (!line.isEmpty()) {
					pendingComment.setLength(0);
				}
			}
		} catch (Exception e) {
			log.warn("读取proto注释失败，忽略文件: {}", protoFile, e);
		}
	}

	private String normalizeModuleFilter(String moduleFilter) {
		if (moduleFilter == null || moduleFilter.isBlank()) {
			return null;
		}
		String trimmed = moduleFilter.trim().toLowerCase(Locale.ROOT);
		if (!trimmed.startsWith("0x")) {
			trimmed = "0x" + trimmed;
		}
		return trimmed;
	}

	private static Path resolveDefaultProtoDir() {
		Path direct = Path.of("protocol/src/main/proto");
		if (Files.isDirectory(direct)) {
			return direct;
		}
		return Path.of("../protocol/src/main/proto");
	}
}