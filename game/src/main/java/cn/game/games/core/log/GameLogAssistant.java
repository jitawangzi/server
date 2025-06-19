package cn.game.games.core.log;

import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import com.google.inject.Stage;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.module.account.Account;
import cn.game.protocol.manual.OpType;
import cn.game.util.Config;
import cn.game.util.DateUtil;
import cn.game.util.log.Logger;

/**
 * 游戏日志助手
 *
 * @author pangjiawei - [Created on 2018/8/31 16:51]
 */
public class GameLogAssistant extends Logger {

//	/**
//	 * 全游戏唯一标识
//	 */
//	public static final String APP_KEY = "23121231442121";

	public static String TIME_ZONE = "-1";

	static {
		if (TimeZone.getDefault().getRawOffset() >= 0) {
			TIME_ZONE = "UTC+" + TimeZone.getDefault().getRawOffset() / (1000 * 60 * 60);
		} else {
			TIME_ZONE = "UTC" + TimeZone.getDefault().getRawOffset() / (1000 * 60 * 60);
		}
	}

	/**
	 * 构造畅游日志前缀
	 * <p>
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
	 * 账号id，角色id，角色等级，班级id，设备唯一标识
	 */
	static Object[] buildLogCYPrefix(Player player, String logName, String logVersion, String stepNum) {
		Account account = player.getAccount();

		return new Object[] { getCurrentTimeLogText(), Config.APP_KEY, account.version != null ? account.version : "null", logName, logVersion, stepNum,
				ServerContext.getInstance().getServerId(), account.adChannel != null ? account.adChannel : "null",
				player.getData().getAccountId() != null ? player.getData().getAccountId() : "null",
				player.getPlayerId(), player.getLevel(), 0,
				player.getData().getDeviceId() != null ? player.getData().getDeviceId() : "null" };
	}

	/**
	 * 构造畅游日志前缀
	 * <p>
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
	 * 账号id，角色id，角色等级，班级id，设备唯一标识
	 */
	static Object[] buildLogCYPrefix(SimplePlayer simplePlayer, String logName, String logVersion, String stepNum) {

		return new Object[] { getCurrentTimeLogText(), Config.APP_KEY, "null", logName, logVersion, stepNum, -1,
				simplePlayer.accountAdChannel != null ? simplePlayer.accountAdChannel : "null",
				simplePlayer.getAccountId() != null ? simplePlayer.getAccountId() : "null", simplePlayer.getId(), simplePlayer.getLevel(), -1,
//                LeagueManager.getLeagueIdByPlayerId(miniPlayer.getPlayerId(), miniPlayer.getServerNum()) <= 0?"null":LeagueManager.getLeagueIdByPlayerId(miniPlayer.getPlayerId(), miniPlayer.getServerNum()),
				(simplePlayer.getClientDeviceId() == null || simplePlayer.getClientDeviceId().length() == 0) ? "null" : simplePlayer.getClientDeviceId() };
	}

	/**
	 * 玩家状态日志数据集，key：玩家id，value：记录玩家状态日志所需的数据集日志文本
	 */
	private final static ConcurrentHashMap<Long, String> playerStateLogInfoMap = new ConcurrentHashMap<>();

	public static String getPlayerStateLogInfo(long playerId) {
		return playerStateLogInfoMap.get(playerId);
	}

	/**
	 * 计算在线时长（秒）
	 */
	static long calculatePlayerOnlineDurationSecond(Player player) {
		return (DateUtil.currentTimeMillis() - DateUtil.parseDate(player.getData().getLoginDate()).getTime()) / 1000;
	}

	/**
	 * <pre>
	 * 获得玩家状态日志数据 - 上阵英雄信息
	 *
	 *     字段结构：英雄数据1,英雄数据2,……英雄数据6（不存在时填-1）
	 * </pre>
	 */
	private static String getPlayerStateLogInfoHero(Player player) {
		StringBuilder sb = new StringBuilder();

		return sb.toString();
	}

	/**
	 * 获得战场攻击方英雄模板id数组字符串
	 */
	static String getStageAttackerHeroes(Stage stage) {
		return getStageHeroes(stage, true);
	}

	/**
	 * 获得战场指定方英雄模板id数组字符串
	 */
	static String getStageHeroes(Stage stage, boolean isAttacker) {
//        HeroUnit[] units = null;//isAttacker ? stage.getAtks().get(0).getUnits() : stage.getDefs().get(0).getUnits();
//        int[] ids = Arrays.stream(units).mapToInt(value -> value == null ? -1 : value.getTid()).toArray();
//        return StringUtils.join(ids, ',');
		return null;
	}

	public static String genSkillInfo(Map<Integer, Integer> newLevelMap, Map<Integer, Integer> oldLevelMap) {
		StringBuilder sb = new StringBuilder();

		for (Map.Entry<Integer, Integer> entry : newLevelMap.entrySet()) {
			Integer skillId = entry.getKey();
			Integer nowLevel = entry.getValue();

			Integer oldLevel = oldLevelMap.get(skillId);
			if (Objects.nonNull(oldLevel)) {
				sb.append(skillId).append("#").append(oldLevel).append("#").append(nowLevel).append("_");
			}
		}

		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1);
		}

		return sb.toString();
	}

	public static String getSubcauseIdByBehaviorType(Player player, OpType opType) {
		return "null";
	}
}
