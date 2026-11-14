package cn.game.games.net.game.gm;

/**
 * GM指令执行作用域
 *
 * @author shuyongqiang
 */
public enum GmScope {

	/** 绑定具体玩家上下文，需进入玩家邮箱与写保护作用域执行 */
	PLAYER,

	/** 全局只读或服务级指令，不依赖特定在线玩家 */
	GLOBAL
}