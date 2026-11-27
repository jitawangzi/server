package cn.game.games.net.game.gm.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.core.base.RunMode;
import cn.game.core.exception.LogicException;
import cn.game.protocol.manual.ErrorMsgEnum;

/**
 * GM自定义面板与业务测试页热重载加载器，具备正则白名单、路径穿越防御与 @gm-panel 元数据解析能力 (DC-12)
 *
 * @author shuyongqiang
 */
public final class GmConsolePanelLoader {

	private static final Logger log = LoggerFactory.getLogger(GmConsolePanelLoader.class);

	public static final String PROP_PANELS_DIR = "gm.http.panelsDir";
	public static final String ENV_PANELS_DIR = "GM_HTTP_PANELS_DIR";
	public static final String DEFAULT_PANELS_DIR = "gm-console/panels";

	private static final Pattern PANEL_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+\\.html$");
	private static final Pattern METADATA_BLOCK_PATTERN = Pattern.compile("<!--\\s*@gm-panel\\s*(\\{[\\s\\S]*?\\})\\s*-->");
	private static final int METADATA_SCAN_LIMIT = 8192;
	private static final ObjectMapper METADATA_MAPPER = new ObjectMapper();
	private static final TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<Map<String, Object>>() {
	};

	private GmConsolePanelLoader() {
	}

	/**
	 * 解析自定义面板磁盘存储目录
	 *
	 * @return 规范化绝对路径
	 */
	public static Path resolvePanelsDir() {
		String prop = System.getProperty(PROP_PANELS_DIR);
		if (prop != null && !prop.isBlank()) {
			return Path.of(prop.trim()).toAbsolutePath().normalize();
		}
		String env = System.getenv(ENV_PANELS_DIR);
		if (env != null && !env.isBlank()) {
			return Path.of(env.trim()).toAbsolutePath().normalize();
		}
		Path defaultPath = Path.of(DEFAULT_PANELS_DIR);
		if (!Files.exists(defaultPath)) {
			Path parentFallback = Path.of("..", DEFAULT_PANELS_DIR);
			if (Files.exists(parentFallback)) {
				return parentFallback.toAbsolutePath().normalize();
			}
		}
		return defaultPath.toAbsolutePath().normalize();
	}

	/**
	 * 扫描并列出可用自定义面板元数据列表（生产模式下强制返回空列表）
	 *
	 * @param runMode 运行模式
	 * @return 面板元数据列表
	 */
	public static List<Map<String, Object>> listPanels(RunMode runMode) {
		if (runMode != null && runMode.isProduction()) {
			return Collections.emptyList();
		}
		Path rootDir = resolvePanelsDir();
		if (!Files.exists(rootDir) || !Files.isDirectory(rootDir)) {
			return Collections.emptyList();
		}

		List<Map<String, Object>> panels = new ArrayList<>();
		try (Stream<Path> stream = Files.list(rootDir)) {
			stream.filter(Files::isRegularFile)
					.filter(p -> PANEL_NAME_PATTERN.matcher(p.getFileName().toString()).matches())
					.sorted(Comparator.comparing(p -> p.getFileName().toString()))
					.forEach(p -> {
						String filename = p.getFileName().toString();
						String name = filename.endsWith(".html") ? filename.substring(0, filename.length() - 5) : filename;
						Map<String, Object> item = new LinkedHashMap<>();
						item.put("name", name);
						item.put("file", filename);
						try {
							item.put("size", Files.size(p));
							item.put("lastModified", Files.getLastModifiedTime(p).toMillis());
						} catch (IOException e) {
							log.warn("读取自定义面板文件属性失败 file={}", filename, e);
						}
						applyPanelMetadata(p, name, item);
						panels.add(item);
					});
		} catch (IOException e) {
			log.error("扫描自定义面板目录失败 dir={}", rootDir, e);
			return Collections.emptyList();
		}
		panels.sort(Comparator
				.comparingInt((Map<String, Object> m) -> toInt(m.get("order"), 100))
				.thenComparing(m -> String.valueOf(m.getOrDefault("file", ""))));
		return panels;
	}

	/**
	 * 判断指定业务测试页是否关联到目标协议模块（优先按 @gm-panel.modules 多对多匹配，兼容旧文件名规则）
	 *
	 * @param panel 面板元数据Map
	 * @param moduleId 十进制模块号
	 * @param moduleIdHex 十六进制模块号（如 0x15）
	 * @param moduleKey 模块英文键（如 shop）
	 * @return true 表示关联到目标模块
	 */
	@SuppressWarnings("unchecked")
	public static boolean matchesModule(Map<String, Object> panel, int moduleId, String moduleIdHex, String moduleKey) {
		if (panel == null) {
			return false;
		}
		String normKey = moduleKey != null ? moduleKey.trim().toLowerCase(Locale.ROOT) : "";
		String normHex = moduleIdHex != null ? moduleIdHex.trim().toLowerCase(Locale.ROOT) : "";
		String bareHex = normHex.startsWith("0x") ? normHex.substring(2) : normHex;
		String decStr = moduleId > 0 ? String.valueOf(moduleId) : "";

		Object modulesObj = panel.get("modules");
		if (modulesObj instanceof List<?>) {
			List<Object> declaredModules = (List<Object>) modulesObj;
			if (!declaredModules.isEmpty()) {
				for (Object entry : declaredModules) {
					if (entry == null) {
						continue;
					}
					String token = String.valueOf(entry).trim().toLowerCase(Locale.ROOT);
					if (token.isEmpty()) {
						continue;
					}
					if (!normKey.isEmpty() && token.equals(normKey)) {
						return true;
					}
					if (!normHex.isEmpty() && token.equals(normHex)) {
						return true;
					}
					if (!bareHex.isEmpty() && (token.equals(bareHex) || token.equals("0x" + bareHex))) {
						return true;
					}
					if (!decStr.isEmpty() && token.equals(decStr)) {
						return true;
					}
				}
				return false;
			}
		}

		// 兼容未声明 @gm-panel.modules 的历史面板：按文件名包含模块键或十六进制模块号匹配
		String panelName = String.valueOf(panel.getOrDefault("name", "")).toLowerCase(Locale.ROOT);
		if (!normKey.isEmpty() && panelName.contains(normKey)) {
			return true;
		}
		return !normHex.isEmpty() && panelName.contains(normHex);
	}

	/**
	 * 从 HTML 面板顶部解析声明式 <!-- @gm-panel {...} --> 元数据；未声明或解析失败时平稳回退默认值
	 *
	 * @param path 面板文件路径
	 * @param defaultName 默认名称（不含 .html 后缀）
	 * @param target 写入的目标元数据 Map
	 */
	private static void applyPanelMetadata(Path path, String defaultName, Map<String, Object> target) {
		Map<String, Object> parsed = Collections.emptyMap();
		try {
			String content = Files.readString(path, StandardCharsets.UTF_8);
			String head = content.length() > METADATA_SCAN_LIMIT ? content.substring(0, METADATA_SCAN_LIMIT) : content;
			Matcher matcher = METADATA_BLOCK_PATTERN.matcher(head);
			if (matcher.find()) {
				String jsonBlock = matcher.group(1);
				Map<String, Object> raw = METADATA_MAPPER.readValue(jsonBlock, MAP_TYPE_REF);
				if (raw != null) {
					parsed = raw;
				}
			}
		} catch (Exception e) {
			log.warn("解析自定义面板 @gm-panel 元数据失败，已回退默认值 file={}", path.getFileName(), e);
		}

		target.put("id", nonBlankString(parsed.get("id"), defaultName));
		target.put("title", nonBlankString(parsed.get("title"), defaultName));
		target.put("category", nonBlankString(parsed.get("category"), "自定义面板"));
		target.put("summary", nonBlankString(parsed.get("summary"), ""));
		target.put("audience", toStringList(parsed.get("audience"), Arrays.asList("QA", "DEV")));
		target.put("modules", toStringList(parsed.get("modules"), Collections.emptyList()));
		target.put("tags", toStringList(parsed.get("tags"), Collections.emptyList()));
		target.put("order", toInt(parsed.get("order"), 100));
		target.put("hasMetadata", !parsed.isEmpty());
	}

	private static String nonBlankString(Object value, String fallback) {
		if (value == null) {
			return fallback;
		}
		String str = String.valueOf(value).trim();
		return str.isEmpty() ? fallback : str;
	}

	private static List<String> toStringList(Object value, List<String> fallback) {
		if (!(value instanceof List<?>)) {
			return new ArrayList<>(fallback);
		}
		List<String> list = new ArrayList<>();
		for (Object item : (List<?>) value) {
			if (item != null) {
				String s = String.valueOf(item).trim();
				if (!s.isEmpty()) {
					list.add(s);
				}
			}
		}
		return list.isEmpty() && !fallback.isEmpty() ? new ArrayList<>(fallback) : list;
	}

	private static int toInt(Object value, int fallback) {
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		if (value != null) {
			try {
				return Integer.parseInt(String.valueOf(value).trim());
			} catch (NumberFormatException e) {
				log.debug("忽略非法面板排序值 value={}", value, e);
			}
		}
		return fallback;
	}

	/**
	 * 实时加载指定自定义面板HTML内容（每次请求读盘实现免重启热重载，带白名单与路径穿越校验）
	 *
	 * @param panelFile 文件名（例如 custom-test.html）
	 * @param runMode 运行模式
	 * @return 面板HTML源码
	 */
	public static String loadPanelContent(String panelFile, RunMode runMode) {
		if (runMode != null && runMode.isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.getId(), "生产模式禁止加载自定义面板");
		}
		if (panelFile == null || !PANEL_NAME_PATTERN.matcher(panelFile).matches()) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.getId(), "非法面板文件名: " + panelFile);
		}

		Path rootDir = resolvePanelsDir();
		Path resolved = rootDir.resolve(panelFile).normalize();

		if (!resolved.startsWith(rootDir)) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.getId(), "路径穿越非法访问: " + panelFile);
		}
		if (!Files.exists(resolved) || !Files.isRegularFile(resolved)) {
			throw new LogicException(ErrorMsgEnum.gm_cmd_not_exist.getId(), "面板文件不存在: " + panelFile);
		}

		try {
			return Files.readString(resolved, StandardCharsets.UTF_8);
		} catch (IOException e) {
			log.error("读取自定义面板内容失败 file={}, path={}", panelFile, resolved, e);
			throw new LogicException(ErrorMsgEnum.unknown.getId(), "读取面板文件失败: " + e.getMessage());
		}
	}
}
