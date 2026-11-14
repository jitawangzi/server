package cn.game.games.net.game.gm.command;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import cn.game.core.base.ServerContext;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.core.exception.LogicException;
import cn.game.core.net.client.LogoutType;
import cn.game.games.cache.entity.Chapter;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.cross.guild.service.GuildServiceInterface;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.gm.GmAction;
import cn.game.games.net.game.gm.GmDispatcher;
import cn.game.games.net.game.gm.GmGroup;
import cn.game.games.net.game.gm.GmParam;
import cn.game.games.net.game.gm.GmScope;
import cn.game.games.net.game.gm.GmSecurityPolicy;
import cn.game.games.net.game.gm.GmSecurityTier;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.helper.TestHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.BattleModule;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerResetPush_01100016;
import cn.game.util.DateUtil;
import cn.game.util.LinuxTimeShift;
import cn.game.util.LinuxTimeShift.PreviewResult;
import cn.game.util.ServerType;

/**
 * 玩家属性、跨天与系统时间调试GM指令分组
 *
 * @author shuyongqiang
 */
@Component
@GmGroup(name = "player", description = "玩家数据与时间调试指令")
public class PlayerGm extends AbstractGm {

	@GmAction(name = "tdlv", description = "设置玩家天道修为等级", security = GmSecurityTier.OPS_REPAIR, scope = GmScope.PLAYER)
	public void tdlv(Player player,
			@GmParam(description = "天道修为等级") int level) {
		if (level == 0) {
			throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID);
		}
		GmDispatcher.updateHeavenlyDaoLevel(player, level);
	}

	@GmAction(name = "playerquit", description = "强制下线指定玩家或全服玩家", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void playerquit(Player player,
			@GmParam(description = "目标玩家ID(0或省略表示全服下线)", required = false, defaultValue = "0") long targetPlayerId) {
		if (targetPlayerId == 0) {
			GameClientManager.getInstance().logoutAll(LogoutType.GMTestRequest);
		} else {
			TestHelper.logoutPlayer(targetPlayerId, LogoutType.GMTestRequest);
		}
	}

	@GmAction(name = "playerdel", description = "删除指定玩家存档数据", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void playerdel(Player player,
			@GmParam(description = "目标玩家ID(0或省略表示当前玩家)", required = false, defaultValue = "0") long targetPlayerId) {
		long delId = targetPlayerId == 0 ? player.getPlayerId() : targetPlayerId;
		if (delId == 0) {
			throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID);
		}
		TestHelper.deletePlayerData(delId);
	}

	@GmAction(name = "huanfu", description = "将当前玩家转移至指定逻辑服", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void huanfu(Player player,
			@GmParam(description = "目标服务器ID", required = false, defaultValue = "") String serverId) {
		TestHelper.transferServer(player.getPlayerId(), serverId);
	}

	@GmAction(name = "newday", description = "触发当前玩家个人跨天刷新", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void newday(Player player) {
		int nowDay = DateUtil.getDay();
		player.getData().setRefreshDay(nowDay - 1);
		PlayerHelper.refreshDay(player);
		// 通知客户端跨天了，使用登陆来刷新所有数据
		PlayerHelper.sendProtocol(player.getPlayerId(), PlayerResetPush_01100016.getDefaultInstance());
	}

	@GmAction(name = "gamenewday", description = "触发全服在线玩家跨天刷新", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL)
	public void gamenewday() {
		ServerContext.getInstance().fireEvent(ServerEventTypeEnum.NewDay);
		int nowDay = DateUtil.getDay();
		PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
			p.getData().setRefreshDay(nowDay - 1);
			PlayerHelper.refreshDay(p);
		});
	}

	@GmAction(name = "guildnewday", description = "触发跨服公会跨天刷新", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL)
	public void guildnewday() {
		List<GuildServiceInterface> allServerInterface = ServerHelper.getAllServerInterface(ServerType.Cross, GuildServiceInterface.class);
		for (GuildServiceInterface guildServiceInterface : allServerInterface) {
			guildServiceInterface.testGuildNewDay();
		}
	}

	@GmAction(name = "super", description = "一键开启超级测试账号资源与关卡进度", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void superCmd(Player player) {
		PlayerHelper.addResources(player, 100001, 1000000, OpType.Test);
		PlayerHelper.addResources(player, 100007, 100000, OpType.Test);
		PlayerHelper.addResources(player, 100201, 100000, OpType.Test);
		PlayerHelper.addResources(player, 101003, 1000, OpType.Test);
		PlayerHelper.addResources(player, 709056, 100000, OpType.Test);

		PlayerHelper.addResources(player, 709061, 5, OpType.Test);
		PlayerHelper.addResources(player, 709062, 5, OpType.Test);
		PlayerHelper.addResources(player, 709063, 5, OpType.Test);
		PlayerHelper.addResources(player, 709064, 5, OpType.Test);
		PlayerHelper.addResources(player, 709065, 5, OpType.Test);

		PlayerHelper.addResources(player, 1500106, 5, OpType.Test);
		PlayerHelper.addResources(player, 1500206, 5, OpType.Test);
		PlayerHelper.addResources(player, 1500306, 5, OpType.Test);
		PlayerHelper.addResources(player, 1500406, 5, OpType.Test);
		PlayerHelper.addResources(player, 1500606, 5, OpType.Test);

		PlayerHelper.addResources(player, 355003, 5, OpType.Test);
		PlayerHelper.addResources(player, 355004, 5, OpType.Test);
		PlayerHelper.addResources(player, 355005, 5, OpType.Test);
		PlayerHelper.addResources(player, 332003, 5, OpType.Test);
		PlayerHelper.addResources(player, 332004, 5, OpType.Test);
		TestHelper.addItems(player, 3000, 0);
		PlayerHelper.addResources(player, 203001, 2000, OpType.Test);

		// 跳过新手引导
		player.getPlayerModule().getGuideMap().put(6, 99);
		player.getPlayerModule().getGuideMap().put(7, 99);
		player.getPlayerModule().getGuideMap().put(8, 99);
		player.getPlayerModule().getGuideMap().put(9, 99);

		BattleModule battleModule = player.getBattleModule();
		battleModule.setMainBattleHighest(110025);
		BattleConfig battleConfig = BattleManager.instance().getNullable(110025);
		while (battleConfig != null) {
			battleModule.addChapter(battleConfig.ID);
			Chapter chapter = battleModule.getChapter(battleConfig.ID);
			chapter.setBattleTime(30);
			chapter.setPass(true);
			battleConfig = BattleManager.instance().getNullable(battleConfig.preBattle);
		}
	}

	@GmAction(name = "init", description = "将当前玩家货币拉满", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void initCmd(Player player) {
		TestHelper.setMaxCurrency(player, OpType.Test);
	}

	/**
	 * 校验并构造LinuxTimeShift参数数组，杜绝缺失子命令参数触发System.exit(2)
	 *
	 * @param opOrOffset 子命令或时间偏移量
	 * @param value 时间值或偏移参数
	 * @return 传给LinuxTimeShift的参数数组
	 */
	public static String[] resolveTimeArgs(String opOrOffset, String value) {
		String rawOp = opOrOffset != null ? opOrOffset.trim() : "";
		String rawVal = value != null ? value.trim() : "";
		if (rawVal.isEmpty() && rawOp.contains(" ")) {
			String[] parts = rawOp.split("\\s+", 2);
			rawOp = parts[0].trim();
			rawVal = parts.length > 1 ? parts[1].trim() : "";
		}
		if (rawOp.isEmpty()) {
			throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID, "time命令参数不能为空");
		}
		String lowerOp = rawOp.toLowerCase(Locale.ROOT);
		if (("set".equals(lowerOp) || "add".equals(lowerOp) || "sub".equals(lowerOp)) && rawVal.isEmpty()) {
			throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID, "time " + rawOp + " 缺少时间或偏移参数");
		}
		return rawVal.isEmpty() ? new String[] { rawOp } : new String[] { rawOp, rawVal };
	}

	@GmAction(name = "time", description = "调整测试服系统时间并触发跨天/周/月事件", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL, sideEffects = "修改系统时间并触发跨天/周/月事件", example = "time set \"2026-05-01 04:59:55\"")
	public void time(
			@GmParam(description = "子命令(set/add/sub/restore/status)或偏移量") String opOrOffset,
			@GmParam(required = false, description = "时间值或偏移参数") String value) {
		if (GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID);
		}

		String[] timeArgs = resolveTimeArgs(opOrOffset, value);
		LocalDateTime now = LocalDateTime.now();
		PreviewResult previewPlannedTime = LinuxTimeShift.previewPlannedTime(timeArgs);
		if (previewPlannedTime != null && previewPlannedTime.crossedDay) {
			PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
				p.getData().setOfflineTime(DateUtil.currentTimeMillis());
			});
		}
		LinuxTimeShift.main(timeArgs);

		LocalDateTime nowDateTime = LocalDateTime.now();
		if (previewPlannedTime != null && previewPlannedTime.crossedDay) {
			PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
				PlayerHelper.refresh(p);
			});
		}
		PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
			p.fireAndHandleEvent(EventTypeEnum.SystemTimeChange);
		});
		ServerContext.getInstance().fireEvent(ServerEventTypeEnum.SystemTimeChange);
		if (nowDateTime.isAfter(now) && DateUtil.diff(nowDateTime, now, ChronoUnit.WEEKS) > 0) {
			ServerContext.getInstance().fireEvent(ServerEventTypeEnum.NewWeek);
			PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
				p.fireAndHandleEvent(EventTypeEnum.NewWeek);
			});
		} else if (nowDateTime.isAfter(now) && DateUtil.diff(nowDateTime, now, ChronoUnit.MONTHS) > 0) {
			ServerContext.getInstance().fireEvent(ServerEventTypeEnum.NewMonth);
			PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
				p.fireAndHandleEvent(EventTypeEnum.NewMonth);
			});
		} else if (nowDateTime.isAfter(now) && DateUtil.diff(nowDateTime, now, ChronoUnit.DAYS) > 0) {
			ServerContext.getInstance().fireEvent(ServerEventTypeEnum.NewDay);
			PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
				p.fireAndHandleEvent(EventTypeEnum.NewDay);
			});
		}
	}
}