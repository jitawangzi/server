package cn.game.games.net.game.gm;

/**
 * GM指令安全等级分层
 *
 * @author shuyongqiang
 */
public enum GmSecurityTier {

	/** 仅研发与测试环境允许（DEV/TEST/CHECK/PRESSURE），生产环境强制禁止 */
	DEV_TEST_ONLY,

	/** 只读或安全查询类指令，允许在生产环境经鉴权后执行 */
	OPS_SAFE,

	/** 线上运维修复类指令，允许在生产环境经严格权限与审计后执行 */
	OPS_REPAIR
}