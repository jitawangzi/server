package cn.game.games.net.game.module.activity.impl.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.protobuf.Message;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.PlayerActivityBase;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.config.ActivityCrossGuildWarConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.ActivityCrossGuildWarManager;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarInfoResponse_11000502;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;

/**
 * 跨服宗门争霸玩家侧活动模块。
 * <p>
 * 职责边界：在玩家私有存档快照内维护每日出征/购买次数、本期锁定参战宗门 ID、个人与宗门里程碑领取记录及本地展示缓存；
 * 严格遵循零同步跨服 RPC 红点与列表展示原则，所有跨服写操作前通过 {@link #ensureDailyReset()} 与
 * {@link #ensureCrossActivityBinding()} 完成防卫性自愈。
 * </p>
 *
 * @author shuyongqiang
 */
@ActivityType(type = ActivityTypeEnum.CrossGuildWar)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrossGuildWarPlayerActivity extends PlayerActivityBase {

	/** 跨服宗门争霸玩家活动不订阅高频玩家事件，避免冗余转发跨服事件总线 */
	private static final EventTypeEnum[] EMPTY_EVENTS = new EventTypeEnum[0];

	/** 今日已使用免费/购买出征次数 */
	protected int dailyUsedCount;
	/** 今日已购买额外出征次数 */
	protected int dailyBoughtCount;
	/** 上次重置每日次数的自然日标识（{@link DateUtil#getDay()} 口径） */
	protected int lastResetDay;
	/** 本期已锁定参战宗门 ID（未锁定为 0） */
	protected long lockedGuildId;
	/** 本人本期累计战功镜像（供本地红点与展示兜底使用） */
	protected long myScore;
	/** 本锁定宗门总战功镜像（供本地宗门里程碑红点判定使用） */
	protected long myGuildTotalScore;
	/** 本锁定宗门当前战区排名镜像（未上榜为 0） */
	protected int myGuildRank;
	/** 本期已领取的个人战功目标里程碑 ID 集合（1-based） */
	protected Set<Integer> claimedPersonalMilestoneIds = new HashSet<>();
	/** 本期已领取的锁定宗门战功阶段里程碑 ID 集合（1-based） */
	protected Set<Integer> claimedGuildMilestoneIds = new HashSet<>();

	/** 瞬态缓存：当前绑定周期已知的个人已解锁里程碑列表 */
	@JsonIgnore
	private transient List<Integer> cachedUnlockedPersonalMilestoneIds = new ArrayList<>();
	/** 瞬态缓存：当前绑定周期已知的锁定宗门已解锁里程碑列表 */
	@JsonIgnore
	private transient List<Integer> cachedUnlockedGuildMilestoneIds = new ArrayList<>();
	/** 瞬态缓存：本周期是否至少与 Cross 同步过一次详情快照 */
	@JsonIgnore
	private transient boolean cachedSnapshotSynced;

	/**
	 * 默认无参构造，兼容 CBOR/JSON 多态反序列化冷重载
	 */
	public CrossGuildWarPlayerActivity() {
	}

	/**
	 * 防卫性自然日重置：核对当前日期，若已跨天或首次初始化则清零今日已用与已购次数
	 */
	public void ensureDailyReset() {
		ensureCollectionsInitialized();
		int today = DateUtil.getDay();
		if (this.lastResetDay != today) {
			this.dailyUsedCount = 0;
			this.dailyBoughtCount = 0;
			this.lastResetDay = today;
		}
	}

	/**
	 * 跨天事件重置每日出征次数与购买次数
	 *
	 * @return 固定返回 {@code true} 标识存在跨天状态维护
	 */
	@Override
	public boolean newDay() {
		ensureDailyReset();
		return true;
	}

	/**
	 * 初始化或从数据库冷重载恢复玩家活动实例
	 *
	 * @param id    活动配置 ID
	 * @param owner 所属玩家对象 {@link cn.game.games.cache.entity.Player}
	 * @param isNew 是否首次开启创建
	 */
	@Override
	public void init(int id, Object owner, boolean isNew) {
		ensureCollectionsInitialized();
		super.init(id, owner, isNew);
		ensureDailyReset();
	}

	/**
	 * 构建下发给客户端活动列表的展示摘要，优先以 {@link ActivityStateManager} 实时周期状态覆盖本地陈旧状态
	 *
	 * @return 活动列表条目 {@link ActivityInfo}
	 */
	@Override
	public ActivityInfo buildActivityInfo() {
		ActivityStateManager stateManager = ActivityStateManager.getInstance();
		int mgrState = stateManager.getState(id);
		if (mgrState != ActivityState.NONE_VALUE) {
			return ActivityInfo.newBuilder()
					.setId(id)
					.setStateValue(mgrState)
					.setStartTime(stateManager.getOpenTimeRemaining(id))
					.setEndTime(stateManager.getDestroyTimeRemaining(id))
					.build();
		}
		if (this.destroyTime > 0L && DateUtil.currentTimeMillis() > this.destroyTime) {
			return ActivityInfo.newBuilder()
					.setId(id)
					.setStateValue(ActivityState.NONE_VALUE)
					.setEndTime(0)
					.build();
		}
		return super.buildActivityInfo();
	}

	/**
	 * 首次绑定跨服活动实例钩子：清理可能残留的周期字段并同步本期时间窗口
	 *
	 * @param activeUid 绑定成功的跨服活动实例 UID
	 */
	@Override
	protected void onCrossActivityBound(long activeUid) {
		clearPerPeriodState();
		syncPeriodTimesFromStateManager();
	}

	/**
	 * 跨服活动换期钩子：清空上一周期的锁定宗门、战功与领奖记录，并同步新周期状态与时间窗口（保留自然日次数独立维护）
	 *
	 * @param oldUid 旧周期跨服活动 UID
	 * @param newUid 新周期跨服活动 UID
	 */
	@Override
	protected void onCrossPeriodChanged(long oldUid, long newUid) {
		clearPerPeriodState();

		// 离线跨期经 checkExpired 先置 CLOSE 后再触发绑定时，强制收敛为新周期真实状态
		int mgrState = ActivityStateManager.getInstance().getState(id);
		this.state = (mgrState == ActivityState.START_VALUE
				|| mgrState == ActivityState.CLOSE_VALUE
				|| mgrState == ActivityState.VIEW_VALUE) ? mgrState : ActivityState.START_VALUE;
		syncPeriodTimesFromStateManager();
	}

	/**
	 * 跨服活动彻底销毁且无新期接续时清理绑定并重置周期与瞬态展示缓存
	 *
	 * @param oldUid 已销毁的跨服活动 UID
	 */
	@Override
	protected void onCrossActivityExpired(long oldUid) {
		super.onCrossActivityExpired(oldUid);
		clearPerPeriodState();
	}

	/**
	 * 清理跨周期持久字段与瞬态缓存（保留 dailyUsedCount/dailyBoughtCount/lastResetDay 自然日维度）
	 */
	private void clearPerPeriodState() {
		ensureCollectionsInitialized();
		this.lockedGuildId = 0L;
		this.myScore = 0L;
		this.myGuildTotalScore = 0L;
		this.myGuildRank = 0;
		this.claimedPersonalMilestoneIds.clear();
		this.claimedGuildMilestoneIds.clear();
		resetTransientCache();
	}

	/**
	 * 计算当前活动是否亮红点；严格纯本地只读判定，绝不发起同步跨服 RPC 也不修改持久化字段
	 *
	 * @return true 表示存在可领取个人/宗门里程碑奖励，或 START 期有宗门无换会冲突且仍有剩余出征次数
	 */
	@Override
	public boolean hasRed() {
		ActivityConfig actCfg = ActivityManager.instance().getNullable(id);
		ActivityCrossGuildWarConfig warCfg = ActivityCrossGuildWarManager.instance().getNullable(id);
		if (actCfg == null || warCfg == null || actCfg.disable) {
			return false;
		}
		if (actCfg.levelLimit > 0 && player != null && player.getLevel() < actCfg.levelLimit) {
			return false;
		}

		int effState = resolveLocalEffectiveState();
		if (effState != ActivityState.START_VALUE && effState != ActivityState.CLOSE_VALUE) {
			return false;
		}

		ensureCollectionsInitialized();
		// 检查个人战功目标奖是否已达标且未领取
		if (warCfg.personalMilestoneThresholds != null && this.myScore > 0L) {
			for (int i = 0; i < warCfg.personalMilestoneThresholds.length; i++) {
				if (this.myScore >= warCfg.personalMilestoneThresholds[i]
						&& !this.claimedPersonalMilestoneIds.contains(i + 1)) {
					return true;
				}
			}
		}
		for (Integer milestoneId : this.cachedUnlockedPersonalMilestoneIds) {
			if (milestoneId != null && !this.claimedPersonalMilestoneIds.contains(milestoneId)) {
				return true;
			}
		}

		// 检查锁定宗门战功阶段奖是否已达标且未领取（必须已锁定宗门且个人战功满足最低门槛）
		if (this.lockedGuildId > 0L && this.myScore >= warCfg.minParticipateScore) {
			if (warCfg.guildMilestoneThresholds != null && this.myGuildTotalScore > 0L) {
				for (int i = 0; i < warCfg.guildMilestoneThresholds.length; i++) {
					if (this.myGuildTotalScore >= warCfg.guildMilestoneThresholds[i]
							&& !this.claimedGuildMilestoneIds.contains(i + 1)) {
						return true;
					}
				}
			}
			for (Integer milestoneId : this.cachedUnlockedGuildMilestoneIds) {
				if (milestoneId != null && !this.claimedGuildMilestoneIds.contains(milestoneId)) {
					return true;
				}
			}
		}

		// 争霸期内仅当有宗门、未发生换会冲突且今日仍有可用出征次数时才亮红点
		int today = DateUtil.getDay();
		int effectiveUsed = (this.lastResetDay == today) ? this.dailyUsedCount : 0;
		int effectiveBought = (this.lastResetDay == today) ? this.dailyBoughtCount : 0;
		long currentGuildId = player != null ? player.getGuildId() : 0L;
		if (effState == ActivityState.START_VALUE
				&& currentGuildId > 0L
				&& (this.lockedGuildId == 0L || this.lockedGuildId == currentGuildId)
				&& (warCfg.dailyFreeTimes + effectiveBought - effectiveUsed) > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 获取玩家事件订阅列表（返回空数组，防止自动向 Cross 广播无关个人事件）
	 *
	 * @return 空事件数组
	 */
	@Override
	public EventTypeEnum[] getEventTypes() {
		return EMPTY_EVENTS;
	}

	/**
	 * 空实现玩家事件处理，禁止基类或子类转发冗余跨服全局事件
	 *
	 * @param event 玩家事件
	 */
	@Override
	public void handleEvent(PlayerEvent event) {
	}

	/**
	 * 构建本地展示详情协议对象（不发起同步跨服 RPC；真实全量详情请走 {@code 11000501}）
	 *
	 * @return 基于本地持久化状态组装的详情消息
	 */
	@Override
	public Message buildActivityShowInfo() {
		ensureCollectionsInitialized();
		ensureDailyReset();
		ActivityCrossGuildWarConfig warCfg = ActivityCrossGuildWarManager.instance().getNullable(id);
		int freeTimes = warCfg != null ? warCfg.dailyFreeTimes : 0;
		int remainingTimes = Math.max(0, freeTimes + this.dailyBoughtCount - this.dailyUsedCount);
		return ActivityCrossGuildWarInfoResponse_11000502.newBuilder()
				.setActivityId(id)
				.setCrossActivityUid(crossActivityUid)
				.setState(state)
				.setLockedGuildId(lockedGuildId)
				.setCurrentGuildId(player != null ? player.getGuildId() : 0L)
				.setMyScore(myScore)
				.setDailyUsedCount(dailyUsedCount)
				.setDailyBoughtCount(dailyBoughtCount)
				.setRemainingTimes(remainingTimes)
				.addAllUnlockedPersonalMilestoneIds(cachedUnlockedPersonalMilestoneIds)
				.addAllClaimedPersonalMilestoneIds(claimedPersonalMilestoneIds)
				.addAllUnlockedGuildMilestoneIds(cachedUnlockedGuildMilestoneIds)
				.addAllClaimedGuildMilestoneIds(claimedGuildMilestoneIds)
				.setEndTime(endTime)
				.setDestroyTime(destroyTime)
				.build();
	}

	/**
	 * 通用领奖抽象方法安全兜底实现（跨服宗门争霸领奖统一通过 {@code 11000505} 入口处理）
	 *
	 * @param id 子奖励标识
	 * @return 固定返回不可变空奖励列表
	 */
	@Override
	public List<RewardInfo> receive(int id) {
		return Collections.emptyList();
	}

	/**
	 * 确保持久化与瞬态集合在反序列化后均非 null
	 */
	private void ensureCollectionsInitialized() {
		if (this.claimedPersonalMilestoneIds == null) {
			this.claimedPersonalMilestoneIds = new HashSet<>();
		}
		if (this.claimedGuildMilestoneIds == null) {
			this.claimedGuildMilestoneIds = new HashSet<>();
		}
		if (this.cachedUnlockedPersonalMilestoneIds == null) {
			this.cachedUnlockedPersonalMilestoneIds = new ArrayList<>();
		}
		if (this.cachedUnlockedGuildMilestoneIds == null) {
			this.cachedUnlockedGuildMilestoneIds = new ArrayList<>();
		}
	}

	/**
	 * 解析本地只读展示有效状态；全局状态为 NONE 且回退本地状态时校验销毁时间，防止持续在线跨过销毁时间后红点残留
	 */
	private int resolveLocalEffectiveState() {
		int mgrState = ActivityStateManager.getInstance().getState(id);
		if (mgrState != ActivityState.NONE_VALUE) {
			return mgrState;
		}
		if (this.state != ActivityState.NONE_VALUE
				&& (this.destroyTime <= 0L || DateUtil.currentTimeMillis() <= this.destroyTime)) {
			return this.state;
		}
		return ActivityState.NONE_VALUE;
	}

	/**
	 * 重置本周期瞬态展示与红点缓存
	 */
	private void resetTransientCache() {
		if (this.cachedUnlockedPersonalMilestoneIds == null) {
			this.cachedUnlockedPersonalMilestoneIds = new ArrayList<>();
		} else {
			this.cachedUnlockedPersonalMilestoneIds.clear();
		}
		if (this.cachedUnlockedGuildMilestoneIds == null) {
			this.cachedUnlockedGuildMilestoneIds = new ArrayList<>();
		} else {
			this.cachedUnlockedGuildMilestoneIds.clear();
		}
		this.cachedSnapshotSynced = false;
	}

	/**
	 * 从 {@link ActivityStateManager} 同步当前周期的起止与销毁时间戳，防止换期沿用旧期过期时间
	 */
	private void syncPeriodTimesFromStateManager() {
		ActivityStateManager stateManager = ActivityStateManager.getInstance();
		Date startDate = stateManager.getStartDate(id);
		if (startDate != null) {
			this.startTime = startDate.getTime();
		} else if (this.startTime <= 0L) {
			this.startTime = DateUtil.currentTimeMillis();
		}
		long mgrEndTime = stateManager.getEndTime(id);
		if (mgrEndTime > 0L) {
			this.endTime = mgrEndTime;
		} else {
			this.endTime = calcEndTime();
		}
		long mgrDestroyTime = stateManager.getDestroyTime(id);
		if (mgrDestroyTime > 0L) {
			this.destroyTime = mgrDestroyTime;
		} else {
			this.destroyTime = calcDestroyTime();
		}
	}

	public int getDailyUsedCount() {
		return dailyUsedCount;
	}

	public void setDailyUsedCount(int dailyUsedCount) {
		this.dailyUsedCount = dailyUsedCount;
	}

	public int getDailyBoughtCount() {
		return dailyBoughtCount;
	}

	public void setDailyBoughtCount(int dailyBoughtCount) {
		this.dailyBoughtCount = dailyBoughtCount;
	}

	public int getLastResetDay() {
		return lastResetDay;
	}

	public void setLastResetDay(int lastResetDay) {
		this.lastResetDay = lastResetDay;
	}

	public long getLockedGuildId() {
		return lockedGuildId;
	}

	public void setLockedGuildId(long lockedGuildId) {
		this.lockedGuildId = lockedGuildId;
	}

	public long getMyScore() {
		return myScore;
	}

	public void setMyScore(long myScore) {
		this.myScore = myScore;
	}

	public long getMyGuildTotalScore() {
		return myGuildTotalScore;
	}

	public void setMyGuildTotalScore(long myGuildTotalScore) {
		this.myGuildTotalScore = myGuildTotalScore;
	}

	public int getMyGuildRank() {
		return myGuildRank;
	}

	public void setMyGuildRank(int myGuildRank) {
		this.myGuildRank = myGuildRank;
	}

	public Set<Integer> getClaimedPersonalMilestoneIds() {
		ensureCollectionsInitialized();
		return claimedPersonalMilestoneIds;
	}

	public void setClaimedPersonalMilestoneIds(Set<Integer> claimedPersonalMilestoneIds) {
		this.claimedPersonalMilestoneIds = claimedPersonalMilestoneIds != null
				? claimedPersonalMilestoneIds : new HashSet<>();
	}

	public Set<Integer> getClaimedGuildMilestoneIds() {
		ensureCollectionsInitialized();
		return claimedGuildMilestoneIds;
	}

	public void setClaimedGuildMilestoneIds(Set<Integer> claimedGuildMilestoneIds) {
		this.claimedGuildMilestoneIds = claimedGuildMilestoneIds != null
				? claimedGuildMilestoneIds : new HashSet<>();
	}

	@JsonIgnore
	public List<Integer> getCachedUnlockedPersonalMilestoneIds() {
		ensureCollectionsInitialized();
		return cachedUnlockedPersonalMilestoneIds;
	}

	@JsonIgnore
	public void setCachedUnlockedPersonalMilestoneIds(List<Integer> cachedUnlockedPersonalMilestoneIds) {
		this.cachedUnlockedPersonalMilestoneIds = cachedUnlockedPersonalMilestoneIds != null
				? new ArrayList<>(cachedUnlockedPersonalMilestoneIds) : new ArrayList<>();
	}

	@JsonIgnore
	public List<Integer> getCachedUnlockedGuildMilestoneIds() {
		ensureCollectionsInitialized();
		return cachedUnlockedGuildMilestoneIds;
	}

	@JsonIgnore
	public void setCachedUnlockedGuildMilestoneIds(List<Integer> cachedUnlockedGuildMilestoneIds) {
		this.cachedUnlockedGuildMilestoneIds = cachedUnlockedGuildMilestoneIds != null
				? new ArrayList<>(cachedUnlockedGuildMilestoneIds) : new ArrayList<>();
	}

	@JsonIgnore
	public boolean isCachedSnapshotSynced() {
		return cachedSnapshotSynced;
	}

	@JsonIgnore
	public void setCachedSnapshotSynced(boolean cachedSnapshotSynced) {
		this.cachedSnapshotSynced = cachedSnapshotSynced;
	}
}
