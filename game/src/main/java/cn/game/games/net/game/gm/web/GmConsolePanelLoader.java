package cn.game.games.net.game.gm.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.RunMode;
import cn.game.core.exception.LogicException;
import cn.game.protocol.manual.ErrorMsgEnum;

/**
 * GM自定义面板热重载加载器，具备正则白名单与路径穿越防御 (DC-12)
 *
 * @author shuyongqiang
 */
public final class GmConsolePanelLoader {

	private static final Logger log = LoggerFactory.getLogger(GmConsolePanelLoader.class);

	public static final String PROP_PANELS_DIR = "gm.http.panelsDir";
	public static final String ENV_PANELS_DIR = "GM_HTTP_PANELS_DIR";
	public static final String DEFAULT_PANELS_DIR = "gm-console/panels";

	private static final Pattern PANEL_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+\\.html$");

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
						panels.add(item);
					});
		} catch (IOException e) {
			log.error("扫描自定义面板目录失败 dir={}", rootDir, e);
			return Collections.emptyList();
		}
		return panels;
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
