package cn.game.games.net.game.gm;

import java.util.Locale;

/**
 * GM 控制台细粒度 RBAC 角色定义，按权限等级递增：READONLY < OPERATOR < REPAIR < ADMIN
 *
 * @author shuyongqiang
 */
public enum GmRole {

	/** 只读客服/排障角色：仅允许只读查询、健康诊断、配表查询、审计查看与离线玩家只读检索 */
	READONLY(1, "只读查询"),

	/** 运营操作角色：包含只读能力及常规 OPS_SAFE 运营操作（如邮件补发、公告等） */
	OPERATOR(2, "运营操作"),

	/** 受控修数角色：包含运营操作及 OPS_REPAIR 具名数据修复能力 */
	REPAIR(3, "受控修数"),

	/** 研发运维管理员：拥有当前 RunMode 所允许的全部能力（仍受生产安全硬门禁约束） */
	ADMIN(4, "研发运维管理员");

	private final int level;
	private final String desc;

	GmRole(int level, String desc) {
		this.level = level;
		this.desc = desc;
	}

	public int getLevel() {
		return level;
	}

	public String getDesc() {
		return desc;
	}

	/**
	 * 判断当前角色权限等级是否满足目标要求
	 *
	 * @param required 所需最低角色
	 * @return true表示满足
	 */
	public boolean atLeast(GmRole required) {
		if (required == null) {
			return true;
		}
		return this.level >= required.level;
	}

	/**
	 * 按名称（忽略大小写）解析角色枚举
	 *
	 * @param raw 角色名称
	 * @return 匹配的角色枚举，不合法时返回null
	 */
	public static GmRole parseOrNull(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		String upper = raw.trim().toUpperCase(Locale.ROOT);
		for (GmRole role : values()) {
			if (role.name().equals(upper)) {
				return role;
			}
		}
		return null;
	}
}
