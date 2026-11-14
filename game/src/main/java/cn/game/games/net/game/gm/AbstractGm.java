package cn.game.games.net.game.gm;

import javax.annotation.PostConstruct;

/**
 * GM处理器基类，保留参数辅助方法并兼容基于注解的GM分组扫描
 *
 * @author shuyongqiang
 */
public abstract class AbstractGm {

	/**
	 * 生命周期初始化钩子（注解式GM处理器无需手写命令注册）
	 */
	@PostConstruct
	public void init() {
	}

	protected void register(String cmd, GmCommand handler) {
		GmRegistry.register(cmd, handler);
	}

	protected int getInt(String[] params, int index) {
		return getInt(params, index, 0);
	}

	protected int getInt(String[] params, int index, int defaultVal) {
		return (params.length > index && params[index] != null && !params[index].isEmpty())
				? Integer.parseInt(params[index])
				: defaultVal;
	}

	protected long getLong(String[] params, int index, long defaultVal) {
		return (params.length > index && params[index] != null && !params[index].isEmpty())
				? Long.parseLong(params[index])
				: defaultVal;
	}

	protected String getString(String[] params, int index, String defaultVal) {
		return (params.length > index && params[index] != null && !params[index].isEmpty())
				? params[index]
				: defaultVal;
	}
}