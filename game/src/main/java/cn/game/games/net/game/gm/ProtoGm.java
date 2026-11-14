package cn.game.games.net.game.gm;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 协议与错误码目录GM指令分组（@GmGroup("proto")，全部动作均为OPS_SAFE）
 *
 * @author shuyongqiang
 */
@Component
@GmGroup(name = "proto", description = "协议与错误码目录查询")
public class ProtoGm extends AbstractGm {

	private final ProtoCatalogService protoCatalogService;

	@Autowired
	public ProtoGm(ProtoCatalogService protoCatalogService) {
		this.protoCatalogService = protoCatalogService;
	}

	/**
	 * 查询协议目录、请求响应配对及Descriptor结构
	 *
	 * @param module 可选模块号过滤（如 0x77）
	 * @param keyword 可选协议名或ID关键字
	 * @return 协议目录数据
	 */
	@GmAction(name = "catalog", description = "查询服务端Protobuf协议目录与结构", security = GmSecurityTier.OPS_SAFE, scope = GmScope.GLOBAL)
	public Map<String, Object> catalog(
			@GmParam(description = "模块十六进制前缀，如0x77", required = false) String module,
			@GmParam(description = "协议名或ID关键字", required = false) String keyword) {
		return protoCatalogService.listCatalog(module, keyword);
	}

	/**
	 * 查询服务端错误码字典
	 *
	 * @param keyword 可选关键字过滤
	 * @return 错误码字典数据
	 */
	@GmAction(name = "errors", description = "查询服务端ErrorMsgEnum错误码字典", security = GmSecurityTier.OPS_SAFE, scope = GmScope.GLOBAL)
	public Map<String, Object> errors(
			@GmParam(description = "错误码ID、名称或说明关键字", required = false) String keyword) {
		return protoCatalogService.listErrors(keyword);
	}
}