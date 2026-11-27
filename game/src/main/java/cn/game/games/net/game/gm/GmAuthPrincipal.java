package cn.game.games.net.game.gm;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * GM HTTP 请求已鉴权主体信息（操作人、RBAC 角色与来源 IP）
 *
 * @author shuyongqiang
 */
public final class GmAuthPrincipal {

	private final String operator;
	private final GmRole role;
	private final String remoteIp;
	private final boolean authenticated;

	public GmAuthPrincipal(String operator, GmRole role, String remoteIp, boolean authenticated) {
		this.operator = (operator != null && !operator.isBlank()) ? operator.trim() : "unknown";
		this.role = role != null ? role : GmRole.READONLY;
		this.remoteIp = remoteIp != null ? remoteIp.trim() : "";
		this.authenticated = authenticated;
	}

	/**
	 * 构造非鉴权开发/测试环境下的默认本地开发主体
	 *
	 * @param remoteIp 来源 IP
	 * @return 默认开发主体
	 */
	public static GmAuthPrincipal anonymousDev(String remoteIp) {
		return new GmAuthPrincipal("local_dev", GmRole.ADMIN, remoteIp, false);
	}

	public String getOperator() {
		return operator;
	}

	public GmRole getRole() {
		return role;
	}

	public String getRemoteIp() {
		return remoteIp;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	/**
	 * 转换为脱敏 JSON 视图（供 /bootstrap 与前端顶栏展示）
	 *
	 * @return 脱敏 Map
	 */
	public Map<String, Object> toViewMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("operator", operator);
		map.put("role", role.name());
		map.put("roleDesc", role.getDesc());
		map.put("remoteIp", remoteIp);
		map.put("authenticated", authenticated);
		return map;
	}
}
