package cn.game.games.net.game.gm;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.exception.LogicException;
import cn.game.games.cache.entity.Player;

/**
 * GM指令注册与分发的向后兼容委托入口，优先路由至GmDispatcher
 *
 * @author shuyongqiang
 */
public class GmRegistry {

	private static final Logger log = LoggerFactory.getLogger(GmRegistry.class);
	private static final Map<String, GmCommand> commands = new HashMap<>();

	/**
	 * 注册遗留函数式GM命令
	 *
	 * @param cmd 命令名称
	 * @param handler 处理器
	 */
	public static void register(String cmd, GmCommand handler) {
		if (cmd != null && handler != null) {
			commands.put(cmd.toLowerCase(), handler);
		}
	}

	/**
	 * 兼容文本命令分发，优先委托至GmDispatcher，不存在时回退至动态注册表
	 *
	 * @param player 玩家上下文
	 * @param cmd 命令名称
	 * @param params 参数数组
	 * @return true表示命中并已分发
	 */
	public static boolean dispatch(Player player, String cmd, String[] params) {
		if (cmd == null) {
			return false;
		}
		GmDispatcher dispatcher = GmDispatcher.getInstance();
		if (dispatcher.hasCommand(cmd)) {
			GmResult result = dispatcher.dispatchLegacy(player, cmd, params);
			if (!result.isOk()) {
				throw new LogicException(result.getErrorCode(), result.getSummary());
			}
			return true;
		}
		GmCommand handler = commands.get(cmd.toLowerCase());
		if (handler != null) {
			try {
				handler.handle(player, params);
			} catch (Exception e) {
				log.error("GM command execution failed: {}", cmd, e);
				throw e;
			}
			return true;
		}
		return false;
	}

	/**
	 * 委托GmDispatcher执行结构化GM命令并返回完整GmResult信封
	 *
	 * @param player 玩家上下文
	 * @param rawCommand 原始命令行
	 * @return GmResult结果
	 */
	public static GmResult dispatchRaw(Player player, String rawCommand) {
		return GmDispatcher.getInstance().dispatchText(player, rawCommand);
	}
}