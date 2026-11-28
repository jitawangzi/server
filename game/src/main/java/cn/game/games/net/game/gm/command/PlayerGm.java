package cn.game.games.net.game.gm.command;

import cn.game.games.net.game.gm.RequestTraceService;
import cn.game.games.net.game.gm.PlayerSnapshotService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.game.core.base.ServerContext;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.core.exception.LogicException;
import cn.game.core.net.client.LogoutType;
import cn.game.games.cache.entity.Chapter;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.BasePlayerModule;
import cn.game.util.Rnd;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.cross.guild.service.GuildServiceInterface;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.gm.GmAction;
import cn.game.games.net.game.gm.GmDispatcher;
import cn.game.games.net.game.gm.GmGroup;
import cn.game.games.net.game.gm.GmParam;
import cn.game.games.net.game.gm.GmScope;
import cn.game.games.net.game.gm.VirtualClockService;
import java.util.Map;

import cn.game.games.net.game.gm.GmSecurityPolicy;
import cn.game.games.net.game.gm.GmSecurityTier;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.helper.TestHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.BattleModule;
import cn.game.games.net.game.module.vip.VipModule;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.ExpConfig;
import cn.game.protocol.generated.config.UserUpgradeConfig;
import cn.game.protocol.generated.config.VIPConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.UserUpgradeManager;
import cn.game.protocol.generated.manager.VIPManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.ExpLevelInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerExpLevelPush_01100050;
import cn.game.protocol.protobuf.PlayerMsg.PlayerResetPush_01100016;
import cn.game.protocol.protobuf.VipMsg;
import cn.game.util.DateUtil;
import cn.game.util.IntMapWrapper;
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

	private static final Logger log = LoggerFactory.getLogger(PlayerGm.class);

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
		// 同步提升角色基础等级并解锁全部功能，避免中后期玩法被等级或功能门禁阻挡
		int currentLv = player.getPlayerModule().getExpLevelMap().getValue(Asset.playerExp.ID);
		if (currentLv < 60) {
			setLevel(player, 60, false, -1);
		}
		if (player.getFuncModule() != null) {
			player.getFuncModule().gmUnlockAllFuncs();
		}
	}

	@GmAction(name = "init", description = "将当前玩家货币拉满", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void initCmd(Player player) {
		TestHelper.setMaxCurrency(player, OpType.Test);
	}

	/**
	 * 直接设置玩家等级，并支持一键将当前经验设为当前等级最大经验减 1（差 1 点经验即升级，方便测试升级流程）
	 *
	 * @param player 目标在线玩家
	 * @param level 目标等级（<=0 表示保持当前等级不变，仅调整经验）
	 * @param nearLevelUp true 时自动将当前经验设置为当前等级升级所需经验 - 1
	 * @param exp 指定精确经验值（>=0 且 nearLevelUp=false 时生效；<0 且修改等级时默认清零）
	 * @return 设置后的等级与经验详情
	 */
	@GmAction(name = "setLevel", aliases = {"setlevel", "lv"}, description = "设置玩家等级或将经验设为差1点升级状态", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER, sideEffects = "修改玩家等级与当前经验值并刷新功能开放", example = "player.setLevel level=20 nearLevelUp=true")
	public Map<String, Object> setLevel(Player player,
			@GmParam(required = false, defaultValue = "0", description = "目标等级(0表示保持当前等级不变)") int level,
			@GmParam(required = false, defaultValue = "false", description = "是否将当前经验设为当前等级最大经验-1(差1点即升级)") boolean nearLevelUp,
			@GmParam(required = false, defaultValue = "-1", description = "精确经验值(>=0且nearLevelUp=false时生效)") int exp) {
		if (player == null) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "目标玩家不存在");
		}
		IntMapWrapper levelsMap = player.getPlayerModule().getExpLevelMap();
		int beforeLevel = levelsMap.getValue(Asset.playerExp.ID);
		if (beforeLevel <= 0) {
			beforeLevel = Math.max(1, player.getData() != null ? player.getData().getLevel() : 1);
		}
		int maxLevel = resolveMaxPlayerLevel();
		int targetLevel = level > 0 ? Math.min(level, maxLevel) : beforeLevel;
		if (targetLevel < 1) {
			targetLevel = 1;
		}
		boolean levelChanged = (targetLevel != beforeLevel);
		levelsMap.setValue(Asset.playerExp.ID, targetLevel);
		if (player.getData() != null) {
			player.getData().setLevel(targetLevel);
		}

		ExpConfig curCfg = safeGetExpConfig(Asset.playerExp.ID, targetLevel);
		ExpConfig nextCfg = safeGetExpConfig(Asset.playerExp.ID, targetLevel + 1);
		int maxExpForLevel = curCfg != null ? curCfg.experience : 0;
		boolean isMaxLevel = (nextCfg == null && curCfg != null);

		long targetExp = player.getCurrencyModule().getCount(Asset.playerExp.ID);
		String warning = null;
		if (nearLevelUp) {
			if (isMaxLevel) {
				targetExp = levelChanged ? 0L : Math.min(targetExp, maxExpForLevel);
				warning = "当前已达玩家最高等级(" + targetLevel + ")，无法进入差1经验即升级状态";
			} else {
				targetExp = maxExpForLevel > 0 ? (maxExpForLevel - 1L) : 0L;
			}
		} else if (exp >= 0) {
			targetExp = (maxExpForLevel > 0 && nextCfg != null && exp >= maxExpForLevel) ? (maxExpForLevel - 1L) : exp;
		} else if (levelChanged) {
			targetExp = 0L;
		}
		player.getCurrencyModule().setCount(Asset.playerExp.ID, targetExp);

		if (levelChanged && player.getFuncModule() != null) {
			player.getFuncModule().refreshFuncOpen();
		}
		pushExpLevel(player, Asset.playerExp.ID, targetLevel, targetExp);

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("beforeLevel", beforeLevel);
		result.put("level", targetLevel);
		result.put("maxLevel", maxLevel);
		result.put("exp", targetExp);
		result.put("maxExpForLevel", maxExpForLevel);
		result.put("expToNextLevel", (nextCfg != null && maxExpForLevel > targetExp) ? (maxExpForLevel - targetExp) : 0L);
		result.put("nearLevelUp", nearLevelUp && !isMaxLevel);
		result.put("isMaxLevel", isMaxLevel);
		if (warning != null) {
			result.put("warning", warning);
		}
		return result;
	}

	/**
	 * 直接设置玩家 VIP 等级，并支持一键将当前 VIP 经验设为当前 VIP 等级最大经验减 1（差 1 点 VIP 经验即升级）
	 *
	 * @param player 目标在线玩家
	 * @param vipLevel 目标 VIP 等级（<0 表示保持当前 VIP 等级不变，仅调整 VIP 经验）
	 * @param nearLevelUp true 时自动将当前 VIP 经验设置为升级所需经验 - 1
	 * @param exp 指定精确 VIP 经验值（>=0 且 nearLevelUp=false 时生效；<0 且修改等级时默认清零）
	 * @return 设置后的 VIP 等级与经验详情
	 */
	@GmAction(name = "setVip", aliases = {"setvip", "vip"}, description = "设置玩家VIP等级或将VIP经验设为差1点升级状态", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER, sideEffects = "修改玩家VIP等级与VIP经验值", example = "player.setVip vipLevel=5 nearLevelUp=true")
	public Map<String, Object> setVip(Player player,
			@GmParam(required = false, defaultValue = "-1", description = "目标VIP等级(-1表示保持当前VIP等级不变)") int vipLevel,
			@GmParam(required = false, defaultValue = "false", description = "是否将当前VIP经验设为当前VIP等级最大经验-1(差1点即升级)") boolean nearLevelUp,
			@GmParam(required = false, defaultValue = "-1", description = "精确VIP经验值(>=0且nearLevelUp=false时生效)") int exp) {
		if (player == null) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "目标玩家不存在");
		}
		IntMapWrapper levelsMap = player.getPlayerModule().getExpLevelMap();
		int beforeVip = levelsMap.getValue(Asset.VIPExp.ID);
		int maxVip = resolveMaxVipLevel();
		int targetVip = vipLevel >= 0 ? Math.min(vipLevel, maxVip) : beforeVip;
		if (targetVip < 0) {
			targetVip = 0;
		}
		boolean vipChanged = (targetVip != beforeVip);
		levelsMap.setValue(Asset.VIPExp.ID, targetVip);
		if (player.getData() != null) {
			player.getData().setVipLevel(targetVip);
		}

		ExpConfig curCfg = safeGetExpConfig(Asset.VIPExp.ID, targetVip);
		ExpConfig nextCfg = safeGetExpConfig(Asset.VIPExp.ID, targetVip + 1);
		int maxExpForLevel = curCfg != null ? curCfg.experience : 0;
		boolean isMaxLevel = (nextCfg == null && curCfg != null);

		long targetExp = player.getCurrencyModule().getCount(Asset.VIPExp.ID);
		String warning = null;
		if (nearLevelUp) {
			if (isMaxLevel) {
				targetExp = vipChanged ? 0L : Math.min(targetExp, maxExpForLevel);
				warning = "当前已达最高VIP等级(" + targetVip + ")，无法进入差1经验即升级状态";
			} else {
				targetExp = maxExpForLevel > 0 ? (maxExpForLevel - 1L) : 0L;
			}
		} else if (exp >= 0) {
			targetExp = (maxExpForLevel > 0 && nextCfg != null && exp >= maxExpForLevel) ? (maxExpForLevel - 1L) : exp;
		} else if (vipChanged) {
			targetExp = 0L;
		}
		player.getCurrencyModule().setCount(Asset.VIPExp.ID, targetExp);
		pushExpLevel(player, Asset.VIPExp.ID, targetVip, targetExp);
		if (vipChanged && player.getGameClient() != null) {
			VipModule vipModule = player.getModule(VipModule.class);
			if (vipModule != null) {
				player.getGameClient().sendProtocol(
						VipMsg.getVipInfoResponse_34000002.newBuilder().setInfo(vipModule.toPb()).build());
			}
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("beforeVipLevel", beforeVip);
		result.put("vipLevel", targetVip);
		result.put("maxVipLevel", maxVip);
		result.put("exp", targetExp);
		result.put("maxExpForLevel", maxExpForLevel);
		result.put("expToNextLevel", (nextCfg != null && maxExpForLevel > targetExp) ? (maxExpForLevel - targetExp) : 0L);
		result.put("nearLevelUp", nearLevelUp && !isMaxLevel);
		result.put("isMaxLevel", isMaxLevel);
		if (warning != null) {
			result.put("warning", warning);
		}
		return result;
	}

	/**
	 * 一键解锁玩家全部 InitialUI 功能入口并正常触发 FuncOpen 初始化事件
	 *
	 * @param player 目标在线玩家
	 * @return 新解锁数量与功能总数摘要
	 */
	@GmAction(name = "openAllFunc", aliases = {"openallfunc", "unlockallfunc"}, description = "一键解锁玩家全部功能模块(InitialUI)", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER, sideEffects = "解锁玩家全部功能并触发FuncOpen事件", example = "player.openAllFunc")
	public Map<String, Object> openAllFunc(Player player) {
		if (player == null || player.getFuncModule() == null) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "目标玩家或功能模块不存在");
		}
		int newlyUnlocked = player.getFuncModule().gmUnlockAllFuncs();
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("newlyUnlocked", newlyUnlocked);
		result.put("totalFunctions", InitialUI.values().length);
		return result;
	}

	private int resolveMaxPlayerLevel() {
		int maxLv = 100;
		if (UserUpgradeManager.instance() != null && UserUpgradeManager.instance().list() != null && !UserUpgradeManager.instance().list().isEmpty()) {
			int found = 0;
			for (UserUpgradeConfig cfg : UserUpgradeManager.instance().list()) {
				if (cfg.ID > found) {
					found = cfg.ID;
				}
			}
			if (found > 0) {
				maxLv = found;
			}
		}
		return maxLv;
	}

	private int resolveMaxVipLevel() {
		int maxVip = 15;
		if (VIPManager.instance() != null && VIPManager.instance().list() != null && !VIPManager.instance().list().isEmpty()) {
			int found = 0;
			for (VIPConfig cfg : VIPManager.instance().list()) {
				if (cfg.ID > found) {
					found = cfg.ID;
				}
			}
			if (found > 0) {
				maxVip = found;
			}
		}
		return maxVip;
	}

	private ExpConfig safeGetExpConfig(int expId, int level) {
		try {
			return PlayerHelper.getExpConfig(expId, level, 0);
		} catch (Exception e) {
			log.debug("查询升级配置未命中 expId={}, level={}", expId, level, e);
			return null;
		}
	}

	private void pushExpLevel(Player player, int expId, int level, long exp) {
		if (player.getGameClient() == null) {
			return;
		}
		player.getGameClient().sendProtocol(PlayerExpLevelPush_01100050.newBuilder()
				.setExpLevel(ExpLevelInfo.newBuilder()
						.setId(expId)
						.setExp((int) Math.min(Integer.MAX_VALUE, Math.max(0L, exp)))
						.setLevel(level)
						.build())
				.build());
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

		@GmAction(name = "time.status", aliases = {"timestatus"}, description = "获取当前物理时间、JVM虚拟时间、偏移量与周期序号状态", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL, idempotent = true, readOnly = true, example = "time.status")
	public Map<String, Object> timeStatus() {
		return VirtualClockService.getInstance().getStatus().toMap();
	}

	@GmAction(name = "time.offset", aliases = {"timeoffset"}, description = "按相对偏移表达式调整虚拟时间并触发跨周期一站式结算", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL, sideEffects = "调整JVM虚拟时间并按需触发跨天/周/月结算与踢线", example = "time.offset offset=\"+1d\"")
	public Object timeOffset(
			@GmParam(description = "时间偏移量表达式(+1d, -2h, 30m, 3600s)") String offset,
			@GmParam(required = false, defaultValue = "false", description = "是否允许向后倒退") boolean allowBackward,
			@GmParam(required = false, defaultValue = "false", description = "是否踢出所有在线客户端") boolean kickClients) {
		return VirtualClockService.getInstance().applyOffset(offset, allowBackward, kickClients);
	}

	@GmAction(name = "time.set", aliases = {"timeset"}, description = "设置绝对目标虚拟时间并触发跨周期一站式结算", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL, sideEffects = "调整JVM虚拟时间并按需触发跨天/周/月结算与踢线", example = "time.set targetTime=\"2026-05-01 05:00:00\"")
	public Object timeSet(
			@GmParam(description = "目标时间(yyyy-MM-dd HH:mm:ss)") String targetTime,
			@GmParam(required = false, defaultValue = "false", description = "是否允许向后倒退") boolean allowBackward,
			@GmParam(required = false, defaultValue = "false", description = "是否踢出所有在线客户端") boolean kickClients) {
		return VirtualClockService.getInstance().setTargetTime(targetTime, allowBackward, kickClients);
	}

	@GmAction(name = "time.reset", aliases = {"timereset"}, description = "重置虚拟时间偏移为0恢复物理系统时间", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL, sideEffects = "重置JVM虚拟时钟偏移为0", example = "time.reset allowBackward=true")
	public Object timeReset(
			@GmParam(required = false, defaultValue = "false", description = "是否允许向后倒退") boolean allowBackward,
			@GmParam(required = false, defaultValue = "false", description = "是否踢出所有在线客户端") boolean kickClients) {
		return VirtualClockService.getInstance().resetOffset(allowBackward, kickClients);
	}

	@GmAction(name = "time", description = "调整测试服虚拟时间并触发跨天/周/月事件(兼容旧命令格式)", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL, sideEffects = "修改虚拟时间并触发跨天/周/月事件", example = "time set \"2026-05-01 04:59:55\"")
	public Object time(
			@GmParam(description = "子命令(set/add/sub/restore/status)或偏移量") String opOrOffset,
			@GmParam(required = false, defaultValue = "", description = "时间值或偏移参数") String value,
			@GmParam(required = false, defaultValue = "false", description = "是否允许向后倒退") boolean allowBackward,
			@GmParam(required = false, defaultValue = "false", description = "是否踢出所有在线客户端") boolean kickClients) {
		return VirtualClockService.getInstance().executeLegacyCommand(opOrOffset, value, allowBackward, kickClients);
	}

	public Object time(String opOrOffset, String value) {
		return time(opOrOffset, value, false, false);
	}

	@GmAction(name = "gm.rng.seed", aliases = {"rng.seed"}, description = "设置确定性随机种子", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL, sideEffects = "切换随机源至可控随机", example = "gm.rng.seed seed=12345")
	public Map<String, Object> rngSeed(@GmParam(description = "随机种子数值") long seed) {
		if (cn.game.games.net.game.gm.GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止设置可控随机源");
		}
		Rnd.setControlledRandomSeed(seed);
		return Rnd.getControlledRandomStatus();
	}

	@GmAction(name = "gm.rng.sequence", aliases = {"rng.sequence"}, description = "设置确定性随机序列", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL, sideEffects = "设置可控随机数序列", example = "gm.rng.sequence intValues=\"1,2,3\"")
	public Map<String, Object> rngSequence(
			@GmParam(required = false, defaultValue = "", description = "逗号分隔或JSON数组整数序列") String intValues,
			@GmParam(required = false, defaultValue = "", description = "逗号分隔或JSON数组浮点序列") String doubleValues,
			@GmParam(required = false, defaultValue = "", description = "通用序列(兼容旧格式)") String values,
			@GmParam(required = false, defaultValue = "0", description = "序列耗尽后的回退种子") long fallbackSeed) {
		if (cn.game.games.net.game.gm.GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止设置可控随机源");
		}
		List<Integer> ints = parseRngInts(intValues, values);
		List<Double> doubles = parseRngDoubles(doubleValues);
		if ((ints == null || ints.isEmpty()) && (doubles == null || doubles.isEmpty())) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "intValues与doubleValues不能同时为空");
		}
		Long fb = fallbackSeed > 0 ? fallbackSeed : null;
		Rnd.setControlledRandomSequence(ints, doubles, fb);
		return Rnd.getControlledRandomStatus();
	}

	@GmAction(name = "gm.rng.status", aliases = {"rng.status"}, description = "获取当前随机源可控状态与序列剩余", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL, idempotent = true, readOnly = true, example = "gm.rng.status")
	public Map<String, Object> rngStatus() {
		return Rnd.getControlledRandomStatus();
	}

	@GmAction(name = "gm.rng.reset", aliases = {"rng.reset"}, description = "重置随机源恢复默认ThreadLocalRandom", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL, sideEffects = "恢复默认随机源", example = "gm.rng.reset")
	public Map<String, Object> rngReset() {
		if (cn.game.games.net.game.gm.GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止设置可控随机源");
		}
		Rnd.resetControlledRandom();
		return Rnd.getControlledRandomStatus();
	}

	@GmAction(name = "gm.resetModule", aliases = {"resetModule", "module.reset"}, description = "模块级测试重置与状态快照", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER, sideEffects = "清空玩家指定模块业务状态与容器", example = "gm.resetModule modules=\"ItemModule,CurrencyModule\"")
	public Object resetModule(
			Player player,
			@GmParam(required = false, defaultValue = "all", description = "要重置的模块名，逗号分隔或all") String modules,
			@GmParam(required = false, defaultValue = "", description = "模块别名参数") String module) {
		if (cn.game.games.net.game.gm.GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止执行模块重置");
		}
		if (player == null) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "目标玩家不存在");
		}
		String target = (modules != null && !modules.isBlank() && !"all".equalsIgnoreCase(modules)) ? modules : module;
		if (target == null || target.isBlank() || "all".equalsIgnoreCase(target)) {
			Map<String, Object> result = new LinkedHashMap<>();
			for (BasePlayerModule m : player.getModules().values()) {
				result.put(m.getClass().getSimpleName(), m.resetForTest());
			}
			return result;
		}
		String raw = target.trim();
		if (raw.startsWith("[") && raw.endsWith("]")) {
			raw = raw.substring(1, raw.length() - 1);
		}
		String[] tokens = raw.split(",");
		// 阶段一：前置全量校验与收集（两阶段执行，防止部分重置脏写）
		List<BasePlayerModule> targets = new ArrayList<>();
		for (String token : tokens) {
			token = token.trim();
			if (token.isEmpty()) continue;
			if ((token.startsWith("\"") && token.endsWith("\"")) || (token.startsWith("'") && token.endsWith("'"))) {
				token = token.substring(1, token.length() - 1).trim();
			}
			BasePlayerModule matched = findModule(player, token);
			if (matched == null) {
				throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "未知模块名: " + token);
			}
			if (!targets.contains(matched)) {
				targets.add(matched);
			}
		}
		// 阶段二：全部通过后执行重置
		Map<String, Object> result = new LinkedHashMap<>();
		for (BasePlayerModule m : targets) {
			result.put(m.getClass().getSimpleName(), m.resetForTest());
		}
		return result;
	}

	private BasePlayerModule findModule(Player player, String token) {
		for (BasePlayerModule m : player.getModules().values()) {
			String simpleName = m.getClass().getSimpleName();
			if (simpleName.equalsIgnoreCase(token) || simpleName.equalsIgnoreCase(token + "Module")
					|| m.getClass().getName().equalsIgnoreCase(token)) {
				return m;
			}
		}
		return null;
	}

	private List<Integer> parseRngInts(String intValues, String values) {
		String raw = (intValues != null && !intValues.isBlank()) ? intValues : values;
		if (raw == null || raw.isBlank()) {
			return null;
		}
		raw = raw.trim();
		if (raw.startsWith("[") && raw.endsWith("]")) {
			raw = raw.substring(1, raw.length() - 1);
		}
		if (raw.isBlank()) {
			return null;
		}
		String[] tokens = raw.split(",");
		List<Integer> list = new ArrayList<>(tokens.length);
		for (String token : tokens) {
			token = token.trim();
			if (token.isEmpty()) continue;
			try {
				list.add(Integer.parseInt(token));
			} catch (NumberFormatException e) {
				throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "无效的整数序列值: " + token);
			}
		}
		return list;
	}

	private List<Double> parseRngDoubles(String doubleValues) {
		if (doubleValues == null || doubleValues.isBlank()) {
			return null;
		}
		String raw = doubleValues.trim();
		if (raw.startsWith("[") && raw.endsWith("]")) {
			raw = raw.substring(1, raw.length() - 1);
		}
		if (raw.isBlank()) {
			return null;
		}
		String[] tokens = raw.split(",");
		List<Double> list = new ArrayList<>(tokens.length);
		for (String token : tokens) {
			token = token.trim();
			if (token.isEmpty()) continue;
			try {
				double val = Double.parseDouble(token);
				if (val < 0.0 || val >= 1.0) {
					throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "浮点序列值必须在[0.0, 1.0)范围内: " + val);
				}
				list.add(val);
			} catch (NumberFormatException e) {
				throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "无效的浮点序列值: " + token);
			}
		}
		return list;
	}

	@GmAction(name = "gm.trace.start", aliases = {"trace.start"}, description = "开启非生产请求Trace录制器", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL, sideEffects = "开启请求Trace与资源流水监控", example = "gm.trace.start playerId=1001 maxRecords=100")
	public Map<String, Object> traceStart(
			Player player,
			@GmParam(required = false, defaultValue = "0", description = "过滤录制的玩家ID(0为录制全部玩家)") long playerId,
			@GmParam(required = false, defaultValue = "0", description = "兼容别名filterPlayerId") long filterPlayerId,
			@GmParam(required = false, defaultValue = "500", description = "最大保留条数(10~2000)") int maxRecords,
			@GmParam(required = false, defaultValue = "0", description = "兼容别名maxEntries") int maxEntries,
			@GmParam(required = false, defaultValue = "false", description = "是否清空已有缓冲区") boolean clearExisting) {
		if (cn.game.games.net.game.gm.GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止开启Trace录制");
		}
		long targetId = playerId > 0 ? playerId : (filterPlayerId > 0 ? filterPlayerId : (player != null ? player.getPlayerId() : 0L));
		int effectiveMax = maxRecords != 500 ? maxRecords : (maxEntries > 0 ? maxEntries : 500);
		RequestTraceService.getInstance().start(targetId, effectiveMax, clearExisting);
		return RequestTraceService.getInstance().status();
	}

	public Map<String, Object> traceStart(Player player, long playerId, int maxRecords, boolean clearExisting) {
		return traceStart(player, playerId, 0L, maxRecords, 0, clearExisting);
	}

	@GmAction(name = "gm.trace.stop", aliases = {"trace.stop"}, description = "停止非生产请求Trace录制器", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL, sideEffects = "停止请求Trace录制", example = "gm.trace.stop")
	public Map<String, Object> traceStop(Player player) {
		if (cn.game.games.net.game.gm.GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止操作Trace录制");
		}
		RequestTraceService.getInstance().stop();
		return RequestTraceService.getInstance().status();
	}

	@GmAction(name = "gm.trace.status", aliases = {"trace.status"}, description = "查询非生产请求Trace录制器状态", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL, idempotent = true, readOnly = true, example = "gm.trace.status")
	public Map<String, Object> traceStatus(Player player) {
		if (cn.game.games.net.game.gm.GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止查询Trace录制");
		}
		return RequestTraceService.getInstance().status();
	}

	@GmAction(name = "gm.trace.query", aliases = {"trace.query"}, description = "多维过滤检索Trace请求记录", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL, idempotent = true, readOnly = true, example = "gm.trace.query playerId=1001 onlyErrors=true limit=20")
	public List<RequestTraceService.TraceRecord> traceQuery(
			Player player,
			@GmParam(required = false, defaultValue = "0", description = "玩家ID过滤(0为不过滤)") long playerId,
			@GmParam(required = false, defaultValue = "0", description = "协议ID过滤(0为不过滤)") int msgId,
			@GmParam(required = false, defaultValue = "", description = "协议名过滤(空为不过滤)") String msgName,
			@GmParam(required = false, defaultValue = "-1", description = "请求序号过滤(-1为不过滤)") int seq,
			@GmParam(required = false, defaultValue = "", description = "TraceId精确过滤") String traceId,
			@GmParam(required = false, defaultValue = "false", description = "是否仅返回含错误/异常的记录") boolean onlyErrors,
			@GmParam(required = false, defaultValue = "50", description = "返回条数限制(1~200)") int limit) {
		if (cn.game.games.net.game.gm.GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止查询Trace记录");
		}
		return RequestTraceService.getInstance().query(playerId, msgId, msgName, seq, traceId, onlyErrors, limit);
	}

	@GmAction(name = "gm.trace.clear", aliases = {"trace.clear"}, description = "清空Trace录制缓冲区", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL, sideEffects = "清空Trace缓冲区", example = "gm.trace.clear")
	public Map<String, Object> traceClear(Player player) {
		if (cn.game.games.net.game.gm.GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止清理Trace缓冲区");
		}
		return RequestTraceService.getInstance().clear();
	}


	@GmAction(name = "gm.snapshot.save", aliases = {"snapshot.save"}, description = "保存当前玩家状态命名快照", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER, sideEffects = "捕获模块与标量快照", example = "gm.snapshot.save snapshotName=\"snap1\"")
	public Map<String, Object> snapshotSave(
			Player player,
			@GmParam(required = false, defaultValue = "default", description = "快照名称") String snapshotName,
			@GmParam(required = false, defaultValue = "", description = "兼容别名label") String label) {
		if (cn.game.games.net.game.gm.GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止执行快照保存");
		}
		String target = (label != null && !label.isBlank()) ? label.trim() : snapshotName;
		return PlayerSnapshotService.getInstance().save(player, target);
	}

	public Map<String, Object> snapshotSave(Player player, String snapshotName) {
		return snapshotSave(player, snapshotName, "");
	}

	@GmAction(name = "gm.snapshot.restore", aliases = {"snapshot.restore"}, description = "还原当前玩家命名快照", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER, sideEffects = "回滚模块与标量数据至快照点", example = "gm.snapshot.restore snapshotName=\"snap1\"")
	public Map<String, Object> snapshotRestore(
			Player player,
			@GmParam(required = false, defaultValue = "default", description = "快照名称") String snapshotName,
			@GmParam(required = false, defaultValue = "", description = "兼容别名label") String label) {
		if (cn.game.games.net.game.gm.GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止执行快照还原");
		}
		String target = (label != null && !label.isBlank()) ? label.trim() : snapshotName;
		return PlayerSnapshotService.getInstance().restore(player, target);
	}

	public Map<String, Object> snapshotRestore(Player player, String snapshotName) {
		return snapshotRestore(player, snapshotName, "");
	}

	@GmAction(name = "gm.snapshot.list", aliases = {"snapshot.list"}, description = "列出当前玩家已保存的快照元数据", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER, idempotent = true, readOnly = true, example = "gm.snapshot.list")
	public List<Map<String, Object>> snapshotList(Player player) {
		if (cn.game.games.net.game.gm.GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止查询快照列表");
		}
		if (player == null) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "目标玩家不存在");
		}
		return PlayerSnapshotService.getInstance().list(player.getPlayerId());
	}

	@GmAction(name = "gm.snapshot.delete", aliases = {"snapshot.delete"}, description = "删除当前玩家命名快照或全部快照", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER, sideEffects = "删除指定或全部快照槽位", example = "gm.snapshot.delete snapshotName=\"snap1\" all=false")
	public Map<String, Object> snapshotDelete(
			Player player,
			@GmParam(required = false, defaultValue = "default", description = "快照名称") String snapshotName,
			@GmParam(required = false, defaultValue = "", description = "兼容别名label") String label,
			@GmParam(required = false, defaultValue = "false", description = "是否清空全部快照") boolean all) {
		if (cn.game.games.net.game.gm.GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止删除快照");
		}
		if (player == null) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "目标玩家不存在");
		}
		String target = (label != null && !label.isBlank()) ? label.trim() : snapshotName;
		return PlayerSnapshotService.getInstance().delete(player.getPlayerId(), target, all);
	}

	public Map<String, Object> snapshotDelete(Player player, String snapshotName, boolean all) {
		return snapshotDelete(player, snapshotName, "", all);
	}

	@GmAction(name = "gm.flushAndColdReload", aliases = {"flushAndColdReload", "player.flushAndColdReload"}, description = "强制落盘并从数据库真实冷重载全新Player实例", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL, sideEffects = "写库、驱逐内存实例并从DB冷重载", example = "gm.flushAndColdReload playerId=1001")
	public Map<String, Object> flushAndColdReload(
			Player player,
			@GmParam(required = false, defaultValue = "0", description = "玩家ID(0为当前玩家)") long playerId,
			@GmParam(required = false, defaultValue = "0", description = "兼容别名targetPlayerId") long targetPlayerId) {
		if (cn.game.games.net.game.gm.GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止执行强制落盘与冷重载");
		}
		long targetId = playerId > 0 ? playerId : (targetPlayerId > 0 ? targetPlayerId : (player != null ? player.getPlayerId() : 0L));
		return PlayerSnapshotService.getInstance().flushAndColdReload(targetId);
	}

	public Map<String, Object> flushAndColdReload(Player player, long playerId) {
		return flushAndColdReload(player, playerId, 0L);
	}

}
