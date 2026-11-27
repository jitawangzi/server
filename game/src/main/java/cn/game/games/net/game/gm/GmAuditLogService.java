package cn.game.games.net.game.gm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.util.DateUtil;

/**
 * GM 控制台统一持久化操作审计日志服务：记录所有 HTTP GM 指令与运维动作（追加写入 JSONL + 有界内存环形查询缓冲）
 *
 * @author shuyongqiang
 */
public final class GmAuditLogService {

	private static final Logger log = LoggerFactory.getLogger(GmAuditLogService.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static final String PROP_AUDIT_FILE = "gm.audit.file";
	public static final String DEFAULT_AUDIT_FILE = "logs/gm-console-audit.jsonl";
	private static final int MAX_IN_MEMORY_RECORDS = 500;
	private static final int MAX_ARGS_SUMMARY_LEN = 500;
	private static final Pattern SENSITIVE_FIELD_PATTERN = Pattern.compile("(?i)\"(token|password|secret|opsToken)\"\\s*:\\s*\"[^\"]*\"");

	private static final GmAuditLogService INSTANCE = new GmAuditLogService();

	private final ConcurrentLinkedDeque<Map<String, Object>> recentRecords = new ConcurrentLinkedDeque<>();
	private final ReentrantLock fileLock = new ReentrantLock();

	private GmAuditLogService() {
	}

	public static GmAuditLogService getInstance() {
		return INSTANCE;
	}

	/**
	 * 解析当前审计 JSONL 文件路径
	 *
	 * @return 审计日志路径
	 */
	public Path resolveAuditFilePath() {
		String configured = System.getProperty(PROP_AUDIT_FILE);
		if (configured != null && !configured.isBlank()) {
			return Paths.get(configured.trim());
		}
		if (System.getProperty("surefire.test.class.path") != null
				|| System.getProperty("surefire.real.class.path") != null) {
			return Paths.get(System.getProperty("java.io.tmpdir"), "gm-console-audit-surefire.jsonl");
		}
		return Paths.get(DEFAULT_AUDIT_FILE);
	}

	/**
	 * 记录一条 GM/运维操作审计日志并追加持久化至 JSONL 文件
	 *
	 * @param principal 操作人主体
	 * @param endpoint HTTP 端点路径
	 * @param action 动作或命令标识
	 * @param playerId 目标玩家 ID（全局动作传 0）
	 * @param dryRun 是否预演模式
	 * @param args 请求参数摘要对象
	 * @param ok 执行是否成功
	 * @param errorCode 错误码（成功为 0）
	 * @param summary 结果摘要
	 * @param diffCount 变更字段数
	 * @param elapsedMs 耗时毫秒
	 * @return 已记录的审计条目 Map
	 */
	public Map<String, Object> record(
			GmAuthPrincipal principal,
			String endpoint,
			String action,
			long playerId,
			boolean dryRun,
			Object args,
			boolean ok,
			int errorCode,
			String summary,
			int diffCount,
			long elapsedMs) {
		GmAuthPrincipal effective = principal != null ? principal : GmAuthPrincipal.anonymousDev("");
		Map<String, Object> entry = new LinkedHashMap<>();
		entry.put("auditId", UUID.randomUUID().toString());
		entry.put("timestamp", Instant.ofEpochMilli(DateUtil.currentTimeMillis()).toString());
		entry.put("virtualTime", DateUtil.getStringDate());
		entry.put("runMode", GmSecurityPolicy.currentRunMode().name());
		entry.put("operator", effective.getOperator());
		entry.put("role", effective.getRole().name());
		entry.put("remoteIp", effective.getRemoteIp());
		entry.put("endpoint", endpoint != null ? endpoint : "");
		entry.put("action", action != null ? action : "");
		entry.put("playerId", playerId);
		entry.put("dryRun", dryRun);
		entry.put("argsSummary", serializeAndSanitizeArgs(args));
		entry.put("ok", ok);
		entry.put("errorCode", errorCode);
		entry.put("summary", truncate(summary, 300));
		entry.put("diffCount", Math.max(0, diffCount));
		entry.put("elapsedMs", Math.max(0L, elapsedMs));

		recentRecords.addFirst(entry);
		while (recentRecords.size() > MAX_IN_MEMORY_RECORDS) {
			recentRecords.pollLast();
		}
		appendToDisk(entry);
		return entry;
	}

	/**
	 * 按条件查询最近操作审计记录（最新优先）
	 *
	 * @param operator 操作人关键字（可选）
	 * @param actionKeyword 动作/端点关键字（可选）
	 * @param playerId 目标玩家 ID 过滤（可选）
	 * @param onlyFailed 仅查看失败记录（可选）
	 * @param limit 返回上限（1~200，默认 50）
	 * @return 结构化查询响应 Map
	 */
	public Map<String, Object> query(String operator, String actionKeyword, Long playerId, Boolean onlyFailed, int limit) {
		int effectiveLimit = Math.max(1, Math.min(200, limit > 0 ? limit : 50));
		String opLower = operator != null ? operator.trim().toLowerCase(Locale.ROOT) : "";
		String actLower = actionKeyword != null ? actionKeyword.trim().toLowerCase(Locale.ROOT) : "";

		List<Map<String, Object>> matched = new ArrayList<>();
		int totalMatched = 0;
		for (Map<String, Object> row : recentRecords) {
			if (!opLower.isEmpty()) {
				String rowOp = String.valueOf(row.getOrDefault("operator", "")).toLowerCase(Locale.ROOT);
				if (!rowOp.contains(opLower)) {
					continue;
				}
			}
			if (!actLower.isEmpty()) {
				String hay = (String.valueOf(row.getOrDefault("action", "")) + " " + String.valueOf(row.getOrDefault("endpoint", ""))).toLowerCase(Locale.ROOT);
				if (!hay.contains(actLower)) {
					continue;
				}
			}
			if (playerId != null && playerId > 0L) {
				Object rowPid = row.get("playerId");
				long parsedPid = rowPid instanceof Number n ? n.longValue() : 0L;
				if (parsedPid != playerId.longValue()) {
					continue;
				}
			}
			if (Boolean.TRUE.equals(onlyFailed) && Boolean.TRUE.equals(row.get("ok"))) {
				continue;
			}
			totalMatched++;
			if (matched.size() < effectiveLimit) {
				matched.add(row);
			}
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("auditFile", resolveAuditFilePath().toString());
		result.put("bufferedTotal", recentRecords.size());
		result.put("matchedTotal", totalMatched);
		result.put("records", matched);
		return result;
	}

	/**
	 * 获取当前内存缓存的审计条目数
	 *
	 * @return 条目数
	 */
	public int bufferedCount() {
		return recentRecords.size();
	}

	/**
	 * 清理内存缓冲（仅供单元测试隔离）
	 */
	public void clearMemoryForTest() {
		recentRecords.clear();
	}

	private void appendToDisk(Map<String, Object> entry) {
		Path path = resolveAuditFilePath();
		fileLock.lock();
		try {
			if (path.getParent() != null && !Files.exists(path.getParent())) {
				Files.createDirectories(path.getParent());
			}
			String line = MAPPER.writeValueAsString(entry) + System.lineSeparator();
			Files.writeString(path, line, StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
		} catch (IOException e) {
			log.warn("持久化写入 GM 审计日志失败: path={}", path, e);
		} finally {
			fileLock.unlock();
		}
	}

	private String serializeAndSanitizeArgs(Object args) {
		if (args == null) {
			return "{}";
		}
		try {
			String json = (args instanceof String s) ? s : MAPPER.writeValueAsString(args);
			json = SENSITIVE_FIELD_PATTERN.matcher(json).replaceAll("\"$1\":\"***\"");
			return truncate(json, MAX_ARGS_SUMMARY_LEN);
		} catch (Exception e) {
			log.warn("序列化 GM 审计参数失败", e);
			return truncate(String.valueOf(args), MAX_ARGS_SUMMARY_LEN);
		}
	}

	private String truncate(String s, int maxLen) {
		if (s == null) {
			return "";
		}
		if (s.length() <= maxLen) {
			return s;
		}
		return s.substring(0, maxLen) + "...(truncated)";
	}
}
