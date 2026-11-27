package cn.game.games.net.game.gm;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.game.core.base.ServerContext;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.core.exception.LogicException;
import cn.game.core.net.client.LogoutType;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerResetPush_01100016;
import cn.game.util.DateUtil;
import cn.game.util.MonthSequenceUtils;
import cn.game.util.WeekSequenceUtils;

/**
 * 跨平台 JVM 虚拟时钟服务：毫秒偏移管理、倒退拦截保护、非生产文件持久化及跨周期一站式结算
 *
 * @author shuyongqiang
 */
@Component
public class VirtualClockService {

	private static final Logger log = LoggerFactory.getLogger(VirtualClockService.class);
	private static final VirtualClockService INSTANCE = new VirtualClockService();

	private static final Pattern OFFSET_PART_PATTERN = Pattern.compile("(\\d+)\\s*([a-zA-Z]+)?");

	private Path stateFilePath;

	public static VirtualClockService getInstance() {
		return INSTANCE;
	}

	public Path getStateFilePath() {
		if (stateFilePath != null) {
			return stateFilePath;
		}
		String sysProp = System.getProperty("gm.clock.stateFile");
		if (sysProp != null && !sysProp.isBlank()) {
			return Paths.get(sysProp.trim());
		}
		String envVal = System.getenv("GM_CLOCK_STATE_FILE");
		if (envVal != null && !envVal.isBlank()) {
			return Paths.get(envVal.trim());
		}
		if (System.getProperty("surefire.test.class.path") != null
				|| System.getProperty("surefire.real.class.path") != null) {
			return Paths.get(System.getProperty("java.io.tmpdir"), "virtual-clock-surefire.properties");
		}
		return Paths.get("logs/virtual-clock.properties");
	}

	public void setStateFilePath(Path path) {
		this.stateFilePath = path;
	}

	/**
	 * 服务启动时恢复虚拟时钟配置
	 */
	public void initOnStartup() {
		if (GmSecurityPolicy.currentRunMode().isProduction()) {
			DateUtil.resetTimeOffset();
			return;
		}
		Path path = getStateFilePath();
		if (!Files.exists(path)) {
			DateUtil.resetTimeOffset();
			return;
		}
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			Properties props = new Properties();
			props.load(reader);
			String offsetStr = props.getProperty("offsetMillis");
			if (offsetStr != null && !offsetStr.isBlank()) {
				long offset = Long.parseLong(offsetStr.trim());
				DateUtil.setTimeOffsetMillis(offset);
				if (offset != 0L) {
					log.warn("Virtual clock initialized with non-zero offset: {} ms ({})", offset, DateUtil.getStringDate());
				}
			} else {
				DateUtil.resetTimeOffset();
			}
		} catch (Exception e) {
			log.warn("Failed to load virtual clock state from {}, fallback to 0 offset", path, e);
			DateUtil.resetTimeOffset();
		}
	}

	/**
	 * 设置相对偏移表达式（如 +1d, -2h, 30m, 1000）
	 */
	public VirtualClockResult applyOffset(String offsetExpr, boolean allowBackward, boolean kickClients) {
		checkProductionBlock();
		long deltaMillis = parseOffsetExpression(offsetExpr);
		long beforeVirtualMs = DateUtil.currentTimeMillis();
		long targetVirtualMs = beforeVirtualMs + deltaMillis;
		long targetOffset = DateUtil.getTimeOffsetMillis() + deltaMillis;
		return updateVirtualClock(beforeVirtualMs, targetVirtualMs, targetOffset, allowBackward, kickClients);
	}

	/**
	 * 设置绝对目标时间（yyyy-MM-dd HH:mm:ss）
	 */
	public VirtualClockResult setTargetTime(String targetTimeStr, boolean allowBackward, boolean kickClients) {
		checkProductionBlock();
		if (targetTimeStr == null || targetTimeStr.isBlank()) {
			throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID, "目标时间字符串不能为空");
		}
		long targetVirtualMs;
		try {
			LocalDateTime dt = LocalDateTime.parse(targetTimeStr.trim(), DateUtil.formatter);
			targetVirtualMs = dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		} catch (Exception e) {
			throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID, "时间格式无效，需为 yyyy-MM-dd HH:mm:ss: " + targetTimeStr);
		}
		long beforeVirtualMs = DateUtil.currentTimeMillis();
		long targetOffset = targetVirtualMs - System.currentTimeMillis();
		return updateVirtualClock(beforeVirtualMs, targetVirtualMs, targetOffset, allowBackward, kickClients);
	}

	/**
	 * 重置虚拟时钟偏移为 0
	 */
	public VirtualClockResult resetOffset(boolean allowBackward, boolean kickClients) {
		checkProductionBlock();
		long beforeVirtualMs = DateUtil.currentTimeMillis();
		long targetVirtualMs = System.currentTimeMillis();
		long targetOffset = 0L;
		return updateVirtualClock(beforeVirtualMs, targetVirtualMs, targetOffset, allowBackward, kickClients);
	}

	/**
	 * 兼容原有 time 命令操作
	 */
	public VirtualClockResult executeLegacyCommand(String opOrOffset, String value, boolean allowBackward, boolean kickClients) {
		checkProductionBlock();
		if (opOrOffset == null || opOrOffset.isBlank()) {
			throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID, "time 子命令或偏移量不能为空");
		}
		String rawOp = opOrOffset.trim().toLowerCase(Locale.ROOT);
		String rawVal = value != null ? value.trim() : "";

		switch (rawOp) {
			case "status": {
				VirtualClockStatus st = getStatus();
				VirtualClockResult res = new VirtualClockResult();
				res.setSuccess(true);
				res.setOffsetMillis(st.getOffsetMillis());
				res.setBeforeVirtualMs(st.getVirtualTimeMs());
				res.setAfterVirtualMs(st.getVirtualTimeMs());
				return res;
			}
			case "reset":
			case "restore":
				return resetOffset(allowBackward, kickClients);
			case "set":
				return setTargetTime(rawVal, allowBackward, kickClients);
			case "add": {
				String expr = rawVal.startsWith("+") ? rawVal : ("+" + rawVal);
				return applyOffset(expr, allowBackward, kickClients);
			}
			case "sub": {
				String expr = rawVal.startsWith("-") ? rawVal : ("-" + rawVal);
				return applyOffset(expr, allowBackward, kickClients);
			}
			default: {
				String expr = rawVal.isEmpty() ? rawOp : (rawOp + " " + rawVal);
				return applyOffset(expr, allowBackward, kickClients);
			}
		}
	}

	/**
	 * 获取当前时钟状态
	 */
	public Map<String, Object> getStatusMap() {
		VirtualClockStatus st = getStatus();
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("physicalTime", st.getPhysicalTimeText());
		map.put("physicalTimeMs", st.getPhysicalTimeMs());
		map.put("virtualTime", st.getVirtualTimeText());
		map.put("virtualTimeMs", st.getVirtualTimeMs());
		map.put("offsetMillis", st.getOffsetMillis());
		map.put("offsetText", st.getOffsetText());
		map.put("day", st.getDay());
		map.put("customDay", st.getCustomDay());
		map.put("week", st.getWeek());
		map.put("month", st.getMonth());
		map.put("stateFile", st.getStateFile());
		map.put("notice", st.getScheduledTasksNotice());
		return map;
	}

	public VirtualClockStatus getStatus() {
		VirtualClockStatus status = new VirtualClockStatus();
		long physMs = System.currentTimeMillis();
		long virtMs = DateUtil.currentTimeMillis();
		long offset = DateUtil.getTimeOffsetMillis();

		status.setPhysicalTimeMs(physMs);
		status.setPhysicalTimeText(LocalDateTime.ofInstant(Instant.ofEpochMilli(physMs), ZoneId.systemDefault()).format(DateUtil.formatter));
		status.setVirtualTimeMs(virtMs);
		status.setVirtualTimeText(DateUtil.getStringDate());
		status.setOffsetMillis(offset);
		status.setDay(DateUtil.getDay());
		status.setCustomDay(DateUtil.getDayCustom());
		status.setWeek(DateUtil.getWeek());
		status.setMonth(DateUtil.getMonth());
		status.setStateFile(getStateFilePath().toString());
		status.setScheduledTasksNotice("虚拟时钟已与 DateUtil/WeekSequenceUtils/MonthSequenceUtils/GuildManager 全量收敛");

		long hours = offset / 3600000L;
		long minutes = (Math.abs(offset) % 3600000L) / 60000L;
		long seconds = (Math.abs(offset) % 60000L) / 1000L;
		status.setOffsetText(String.format("%s%dh %dm %ds (%d ms)", offset >= 0 ? "+" : "-", Math.abs(hours), minutes, seconds, offset));
		return status;
	}

	private void checkProductionBlock() {
		if (GmSecurityPolicy.currentRunMode().isProduction()) {
			DateUtil.resetTimeOffset();
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境严禁修改虚拟时钟");
		}
	}

	private VirtualClockResult updateVirtualClock(long beforeVirtualMs, long targetVirtualMs, long targetOffset,
			boolean allowBackward, boolean kickClients) {
		List<String> warnings = new ArrayList<>();
		if (targetVirtualMs < beforeVirtualMs) {
			if (!allowBackward) {
				throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID,
						"目标虚拟时间早于当前虚拟时间，默认禁止时间倒退；如确需回退请显式设置 allowBackward=true");
			}
			warnings.add("虚拟时钟已向后回退，已触发过的跨天/跨周/跨月状态及一次性活动不会自动回滚");
		}

		// 1. 设置内存偏移
		DateUtil.setTimeOffsetMillis(targetOffset);
		long afterVirtualMs = DateUtil.currentTimeMillis();

		// 2. 状态持久化
		persistState(targetOffset);

		// 3. 计算跨周期边界
		int beforeDay = DateUtil.getDayNumber(DateUtil.toLocalDate(beforeVirtualMs));
		int afterDay = DateUtil.getDayNumber(DateUtil.toLocalDate(afterVirtualMs));
		int beforeCustomDay = DateUtil.getDayCustom(DateUtil.toLocalDateTime(beforeVirtualMs));
		int afterCustomDay = DateUtil.getDayCustom(DateUtil.toLocalDateTime(afterVirtualMs));
		int beforeWeek = WeekSequenceUtils.getWeekIndex(DateUtil.toLocalDate(beforeVirtualMs));
		int afterWeek = WeekSequenceUtils.getWeekIndex(DateUtil.toLocalDate(afterVirtualMs));
		int beforeMonth = MonthSequenceUtils.getMonthIndex(DateUtil.toLocalDate(beforeVirtualMs));
		int afterMonth = MonthSequenceUtils.getMonthIndex(DateUtil.toLocalDate(afterVirtualMs));

		boolean crossedDay = (afterDay > beforeDay) || (afterCustomDay > beforeCustomDay);
		boolean crossedWeek = (afterWeek > beforeWeek);
		boolean crossedMonth = (afterMonth > beforeMonth);

		// 4. 一站式跨周期结算（仅向前推进时结算）
		if (afterVirtualMs > beforeVirtualMs) {
			triggerCrossBoundarySettlement(afterVirtualMs, crossedDay, crossedWeek, crossedMonth, kickClients);
		}

		VirtualClockResult result = new VirtualClockResult();
		result.setSuccess(true);
		result.setBeforeVirtualMs(beforeVirtualMs);
		result.setAfterVirtualMs(afterVirtualMs);
		result.setOffsetMillis(targetOffset);
		result.setCrossedDay(crossedDay);
		result.setCrossedWeek(crossedWeek);
		result.setCrossedMonth(crossedMonth);
		result.setWarnings(warnings);
		return result;
	}

	private void persistState(long offsetMillis) {
		if (GmSecurityPolicy.currentRunMode().isProduction()) {
			return;
		}
		Path path = getStateFilePath();
		try {
			if (path.getParent() != null) {
				Files.createDirectories(path.getParent());
			}
			Properties props = new Properties();
			props.setProperty("offsetMillis", String.valueOf(offsetMillis));
			props.setProperty("updatedAtPhysicalMs", String.valueOf(System.currentTimeMillis()));
			props.setProperty("virtualTimeText", DateUtil.getStringDate());
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				props.store(writer, "Virtual Clock State File");
			}
		} catch (Exception e) {
			log.warn("Failed to persist virtual clock state to {}", path, e);
		}
	}

	private void triggerCrossBoundarySettlement(long afterVirtualMs, boolean crossedDay, boolean crossedWeek, boolean crossedMonth, boolean kickClients) {
		// 刷新在线玩家与发送事件（严格在玩家虚拟线程队列内执行）
		try {
			PlayerManager pm = PlayerManager.getInstance();
			if (pm != null && pm.getAllPlayer() != null) {
				for (Player p : pm.getAllPlayer().values()) {
					if (p == null) {
						continue;
					}
					long playerId = p.getData().getPlayerId();
					try {
						PlayerHelper.addTask(playerId, () -> {
							p.getData().setOfflineTime(afterVirtualMs);
							if (crossedDay) {
								PlayerHelper.refresh(p);
								if (!kickClients && p.getGameClient() != null) {
									p.getGameClient().sendProtocol(PlayerResetPush_01100016.getDefaultInstance());
								}
							}
							p.fireAndHandleEvent(EventTypeEnum.SystemTimeChange);
						});
					} catch (Exception e) {
						// 降级：若未启动虚拟线程队列或测试环境无写守卫，直接在当前线程执行
						p.getData().setOfflineTime(afterVirtualMs);
						if (crossedDay) {
							PlayerHelper.refresh(p);
							if (!kickClients && p.getGameClient() != null) {
								p.getGameClient().sendProtocol(PlayerResetPush_01100016.getDefaultInstance());
							}
						}
						p.fireAndHandleEvent(EventTypeEnum.SystemTimeChange);
					}
				}
			}
		} catch (Exception e) {
			log.warn("Error processing online players during time settlement", e);
		}

		// 触发全局服务事件广播
		try {
			ServerContext sc = ServerContext.getInstance();
			if (sc != null) {
				sc.fireEvent(ServerEventTypeEnum.SystemTimeChange);
				if (crossedWeek) {
					sc.fireEvent(ServerEventTypeEnum.NewWeek);
				}
				if (crossedMonth) {
					sc.fireEvent(ServerEventTypeEnum.NewMonth);
				}
				if (crossedDay) {
					sc.fireEvent(ServerEventTypeEnum.NewDay);
				}
			}
		} catch (Exception e) {
			log.warn("Error firing server events during time settlement", e);
		}

		// 若指定 kickClients，则踢出所有客户端
		if (kickClients) {
			try {
				GameClientManager cm = GameClientManager.getInstance();
				if (cm != null) {
					cm.logoutAll(LogoutType.TimeChange);
				}
			} catch (Exception e) {
				log.warn("Error logging out clients during time settlement", e);
			}
		}
	}

	public long parseOffsetExpression(String expr) {
		if (expr == null || expr.isBlank()) {
			throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID, "时间偏移表达式不能为空");
		}
		String s = expr.trim();
		long sign = 1;
		if (s.startsWith("+")) {
			s = s.substring(1).trim();
		} else if (s.startsWith("-")) {
			sign = -1;
			s = s.substring(1).trim();
		}

		Matcher matcher = OFFSET_PART_PATTERN.matcher(s);
		long totalMillis = 0L;
		boolean found = false;

		while (matcher.find()) {
			found = true;
			long val = Long.parseLong(matcher.group(1));
			String unit = matcher.group(2);
			if (unit == null || unit.isBlank()) {
				totalMillis += val;
			} else {
				String u = unit.toLowerCase(Locale.ROOT);
				if (u.equals("d") || u.equals("day") || u.equals("days")) {
					totalMillis += val * 86400000L;
				} else if (u.equals("w") || u.equals("week") || u.equals("weeks")) {
					totalMillis += val * 7 * 86400000L;
				} else if (u.equals("h") || u.equals("hour") || u.equals("hours")) {
					totalMillis += val * 3600000L;
				} else if (u.equals("m") || u.equals("min") || u.equals("minute") || u.equals("minutes")) {
					totalMillis += val * 60000L;
				} else if (u.equals("s") || u.equals("sec") || u.equals("second") || u.equals("seconds")) {
					totalMillis += val * 1000L;
				} else if (u.equals("ms") || u.equals("milli") || u.equals("millis")) {
					totalMillis += val;
				} else {
					throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID, "未知时间单位: " + unit);
				}
			}
		}

		if (!found) {
			try {
				totalMillis = Long.parseLong(s);
			} catch (NumberFormatException e) {
				throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID, "无法解析时间偏移表达式: " + expr);
			}
		}
		return sign * totalMillis;
	}

	public static class VirtualClockResult {
		private boolean success;
		private long beforeVirtualMs;
		private long afterVirtualMs;
		private long offsetMillis;
		private boolean crossedDay;
		private boolean crossedWeek;
		private boolean crossedMonth;
		private List<String> warnings = new ArrayList<>();

		public boolean isSuccess() { return success; }
		public void setSuccess(boolean success) { this.success = success; }
		public long getBeforeVirtualMs() { return beforeVirtualMs; }
		public void setBeforeVirtualMs(long beforeVirtualMs) { this.beforeVirtualMs = beforeVirtualMs; }
		public long getAfterVirtualMs() { return afterVirtualMs; }
		public void setAfterVirtualMs(long afterVirtualMs) { this.afterVirtualMs = afterVirtualMs; }
		public long getOffsetMillis() { return offsetMillis; }
		public void setOffsetMillis(long offsetMillis) { this.offsetMillis = offsetMillis; }
		public boolean isCrossedDay() { return crossedDay; }
		public void setCrossedDay(boolean crossedDay) { this.crossedDay = crossedDay; }
		public boolean isCrossedWeek() { return crossedWeek; }
		public void setCrossedWeek(boolean crossedWeek) { this.crossedWeek = crossedWeek; }
		public boolean isCrossedMonth() { return crossedMonth; }
		public void setCrossedMonth(boolean crossedMonth) { this.crossedMonth = crossedMonth; }
		public List<String> getWarnings() { return warnings; }
		public void setWarnings(List<String> warnings) { this.warnings = warnings; }
	}

	public static class VirtualClockStatus {
		private long physicalTimeMs;
		private String physicalTimeText;
		private long virtualTimeMs;
		private String virtualTimeText;
		private long offsetMillis;
		private String offsetText;
		private int day;
		private int customDay;
		private int week;
		private int month;
		private String stateFile;
		private String scheduledTasksNotice;

		public long getPhysicalTimeMs() { return physicalTimeMs; }
		public void setPhysicalTimeMs(long physicalTimeMs) { this.physicalTimeMs = physicalTimeMs; }
		public String getPhysicalTimeText() { return physicalTimeText; }
		public void setPhysicalTimeText(String physicalTimeText) { this.physicalTimeText = physicalTimeText; }
		public long getVirtualTimeMs() { return virtualTimeMs; }
		public void setVirtualTimeMs(long virtualTimeMs) { this.virtualTimeMs = virtualTimeMs; }
		public String getVirtualTimeText() { return virtualTimeText; }
		public void setVirtualTimeText(String virtualTimeText) { this.virtualTimeText = virtualTimeText; }
		public long getOffsetMillis() { return offsetMillis; }
		public void setOffsetMillis(long offsetMillis) { this.offsetMillis = offsetMillis; }
		public String getOffsetText() { return offsetText; }
		public void setOffsetText(String offsetText) { this.offsetText = offsetText; }
		public int getDay() { return day; }
		public void setDay(int day) { this.day = day; }
		public int getCustomDay() { return customDay; }
		public void setCustomDay(int customDay) { this.customDay = customDay; }
		public int getWeek() { return week; }
		public void setWeek(int week) { this.week = week; }
		public int getMonth() { return month; }
		public void setMonth(int month) { this.month = month; }
		public String getStateFile() { return stateFile; }
		public void setStateFile(String stateFile) { this.stateFile = stateFile; }
		public String getScheduledTasksNotice() { return scheduledTasksNotice; }
		public void setScheduledTasksNotice(String scheduledTasksNotice) { this.scheduledTasksNotice = scheduledTasksNotice; }

		public Map<String, Object> toMap() {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("physicalTime", physicalTimeText);
			map.put("physicalTimeMs", physicalTimeMs);
			map.put("virtualTime", virtualTimeText);
			map.put("virtualTimeMs", virtualTimeMs);
			map.put("offsetMillis", offsetMillis);
			map.put("offsetText", offsetText);
			map.put("day", day);
			map.put("customDay", customDay);
			map.put("week", week);
			map.put("month", month);
			map.put("stateFile", stateFile);
			map.put("scheduledTasksNotice", scheduledTasksNotice);
			return map;
		}
	}
}
