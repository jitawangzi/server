package cn.game.games.net.game.gm;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.core.exception.LogicException;
import cn.game.protocol.manual.ErrorMsgEnum;

/**
 * 静态配表只读GM指令分组（@GmGroup("config")，全部动作均为OPS_SAFE）
 *
 * @author shuyongqiang
 */
@Component
@GmGroup(name = "config", description = "静态配表只读查询")
public class ConfigGm extends AbstractGm {

	private static final Logger log = LoggerFactory.getLogger(ConfigGm.class);
	private static final ObjectMapper FILTER_MAPPER = new ObjectMapper();
	private static final TypeReference<Map<String, String>> FILTER_TYPE_REF = new TypeReference<>() {
	};

	private final ConfigQueryService configQueryService;

	@Autowired
	public ConfigGm(ConfigQueryService configQueryService) {
		this.configQueryService = configQueryService;
	}

	/**
	 * 列出所有已注册配表及元信息
	 *
	 * @return 配表清单
	 */
	@GmAction(name = "tables", description = "查询所有已加载静态配表目录", security = GmSecurityTier.OPS_SAFE, scope = GmScope.GLOBAL)
	public Map<String, Object> tables() {
		return Map.of("schemaVersion", GmResult.SCHEMA_VERSION, "tables", configQueryService.listTables());
	}

	/**
	 * 分页与过滤查询指定配表
	 *
	 * @param table 配置表名
	 * @param filterJson 可选JSON格式字段过滤条件
	 * @param page 页码（默认1）
	 * @param pageSize 每页条数（默认50，上限200）
	 * @return 结构化查询结果
	 */
	@GmAction(name = "list", description = "分页与条件查询静态配表", security = GmSecurityTier.OPS_SAFE, scope = GmScope.GLOBAL)
	public GmResult list(
			@GmParam(description = "配置表名称", example = "ShopItem") String table,
			@GmParam(description = "JSON格式字段等值过滤", required = false) String filterJson,
			@GmParam(description = "页码", required = false, defaultValue = "1") Integer page,
			@GmParam(description = "每页大小(最大200)", required = false, defaultValue = "50") Integer pageSize) {
		Map<String, String> filters = Collections.emptyMap();
		if (filterJson != null && !filterJson.isBlank()) {
			try {
				filters = FILTER_MAPPER.readValue(filterJson, FILTER_TYPE_REF);
			} catch (Exception e) {
				log.warn("解析config.list过滤JSON失败: filterJson={}", filterJson, e);
				return GmResult.fail(ErrorMsgEnum.gm_cmd_param, "过滤条件JSON格式非法: " + e.getMessage());
			}
		}
		try {
			Map<String, Object> payload = configQueryService.listRows(table, filters, page, pageSize);
			return GmResult.success("ok", payload);
		} catch (LogicException e) {
			log.warn("config.list查询失败: table={}", table, e);
			return GmResult.fail(e.getErrorCode(), resolveErrorName(e.getErrorCode()), e.getErrorMessage());
		}
	}

	/**
	 * 按ID或键查询单条/单组配置
	 *
	 * @param table 配置表名
	 * @param id 数值主键
	 * @param key 字符串键（GlobalConst使用）
	 * @return 查询结果
	 */
	@GmAction(name = "get", description = "按ID或键查询单条配置记录", security = GmSecurityTier.OPS_SAFE, scope = GmScope.GLOBAL)
	public GmResult get(
			@GmParam(description = "配置表名称", example = "ShopItem") String table,
			@GmParam(description = "配置数值ID", required = false) Integer id,
			@GmParam(description = "GlobalConst键名", required = false) String key) {
		try {
			Object row = configQueryService.getRow(table, id, key);
			return GmResult.success("ok", row);
		} catch (LogicException e) {
			log.warn("config.get查询失败: table={}, id={}, key={}", table, id, key, e);
			return GmResult.fail(e.getErrorCode(), resolveErrorName(e.getErrorCode()), e.getErrorMessage());
		}
	}

	private String resolveErrorName(int errorCode) {
		for (ErrorMsgEnum item : ErrorMsgEnum.values()) {
			if (item.getId() == errorCode) {
				return item.name();
			}
		}
		return String.valueOf(errorCode);
	}
}