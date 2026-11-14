package cn.game.games.net.game.gm;

/**
 * GM指令参数类型描述
 *
 * @author shuyongqiang
 */
public enum GmParamType {

	/** 根据Java形参类型自动推断 */
	AUTO,

	/** 32位整数 */
	INT,

	/** 64位整数 */
	LONG,

	/** 浮点数 */
	DOUBLE,

	/** 布尔值 */
	BOOLEAN,

	/** 单值字符串 */
	STRING,

	/** 枚举值 */
	ENUM,

	/** 结构化JSON对象或数组 */
	JSON,

	/** 长文本 */
	TEXT
}