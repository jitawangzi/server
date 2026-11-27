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
import cn.game.protocol.generated.config.ActivityCrossWorldBossConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.ActivityCrossWorldBossManager;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossInfoResponse_11000402;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;

/**
 * 跨服世界Boss玩家侧活动模块。
 * <p>
 * 职责边界：在玩家私有存档快照内维护每日挑战/购买次数、本期里程碑领取记录、击杀奖领取状态与本地展示缓存；
 * 严格遵循零同步跨服 RPC 红点与列表展示原则，所有跨服写操作前通过 {@link #ensureDailyReset()} 与
 * {@link #ensureCrossActivityBinding()} 完成防卫性自愈。
 * </p>
 *
 * @author shuyongqiang
 */
@ActivityType(type = ActivityTypeEnum.CrossWorldBoss)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrossWorldBossPlayerActivity extends PlayerActivityBase {

	/** 跨服世界Boss玩家活动不订阅高频玩家事件，避免冗余转发跨服事件总线 */
	private static final EventTypeEnum[] EMPTY_EVENT_TYPES = new EventTypeEnum[0];

	/** 今日已使用免费/购买挑战次数 */
	protected int dailyUsedCount;
	/** 今日已购买额外挑战次数 */
	protected int dailyBoughtCount;
	/** 上次重置每日次数的自然日标识（{@link DateUtil#getDay()} 口径） */
	protected int lastResetDay;
	/** 本期已领取的战区血量阶段里程碑 ID 集合（1-based） */
	protected Set<Integer> claimedMilestoneIds = new HashSet<>();
	/** 本人本期累计伤害镜像（供本地红点与展示兜底使用） */
	protected long myTotalDamage;
	/** 本人当前战区排名镜像（未上榜为 0） */
	protected int myRank;
	/** 本期最后一击击杀奖励是否已领取或直发成功 */
	protected boolean killRewardClaimed;

	/** 瞬态缓存：当前绑定周期的 Boss 是否已被击杀 */
	@JsonIgnore
	private transient boolean cachedBossKilled;
	/** 瞬态缓存：当前绑定周期已知的战区已解锁里程碑列表 */
	@JsonIgnore
	private transient List<Integer> cachedUnlockedMilestones = new ArrayList<>();
	/** 瞬态缓存：本周期是否至少与 Cross 同步过一次详情快照 */
	@JsonIgnore
	private transient boolean cachedSnapshotSynced;

	/**
	 * 默认无参构造，兼容 CBOR/JSON 多态反序列化冷重载
	 */
	public CrossWorldBossPlayerActivity() {
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
	 * 跨天事件重置每日挑战次数与购买次数
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
	 * 首次绑定跨服活动实例钩子：重置瞬态展示缓存并同步本期时间窗口
	 *
	 * @param activeUid 绑定成功的跨服活动实例 UID
	 */
	@Override
	protected void onCrossActivityBound(long activeUid) {
		resetTransientCache();
		syncPeriodTimesFromStateManager();
	}

	/**
	 * 跨服活动换期钩子：清空上一周期的个人伤害、名次与领奖状态，并同步新周期状态与时间窗口
	 *
	 * @param oldUid 旧周期跨服活动 UID
	 * @param newUid 新周期跨服活动 UID
	 */
	@Override
	protected void onCrossPeriodChanged(long oldUid, long newUid) {
		ensureCollectionsInitialized();
		this.claimedMilestoneIds.clear();
		this.myTotalDamage = 0L;
		this.myRank = 0;
		this.killRewardClaimed = false;
		resetTransientCache();

		// 离线跨期经 checkExpired 先置 CLOSE 后再触发绑定时，强制收敛为新周期真实状态
		int mgrState = ActivityStateManager.getInstance().getState(id);
		this.state = (mgrState == ActivityState.START_VALUE
				|| mgrState == ActivityState.CLOSE_VALUE
				|| mgrState == ActivityState.VIEW_VALUE) ? mgrState : ActivityState.START_VALUE;
		syncPeriodTimesFromStateManager();
	}

	/**
	 * 跨服活动彻底销毁且无新期接续时重置瞬态展示缓存
	 *
	 * @param oldUid 已销毁的跨服活动 UID
	 */
	@Override
	protected void onCrossActivityExpired(long oldUid) {
		resetTransientCache();
	}

	/**
	 * 计算当前活动是否亮红点；严格纯本地只读判定，绝不发起同步跨服 RPC 也不修改持久化字段
	 *
	 * @return true 表示存在可领取里程碑奖励或当前处于 START 期仍有剩余挑战次数
	 */
	@Override
	public boolean hasRed() {
		ActivityConfig actCfg = ActivityManager.instance().getNullable(id);
		ActivityCrossWorldBossConfig bossCfg = ActivityCrossWorldBossManager.instance().getNullable(id);
		if (actCfg == null || bossCfg == null || actCfg.disable) {
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
		if (this.myTotalDamage >= bossCfg.minParticipateDamage) {
			for (Integer milestoneId : this.cachedUnlockedMilestones) {
				if (milestoneId != null && !this.claimedMilestoneIds.contains(milestoneId)) {
					return true;
				}
			}
		}

		// 只读接口按当日口径估算有效次数，避免直接改写持久化字段
		int today = DateUtil.getDay();
		int effectiveUsed = (this.lastResetDay == today) ? this.dailyUsedCount : 0;
		int effectiveBought = (this.lastResetDay == today) ? this.dailyBoughtCount : 0;
		if (effState == ActivityState.START_VALUE
				&& !this.cachedBossKilled
				&& (bossCfg.dailyFreeTimes + effectiveBought - effectiveUsed) > 0) {
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
		return EMPTY_EVENT_TYPES;
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
	 * 构建本地展示详情协议对象（不发起同步跨服 RPC；真实全量详情请走 {@code 11000401}）
	 *
	 * @return 基于本地持久化状态组装的详情消息
	 */
	@Override
	public Message buildActivityShowInfo() {
		ensureCollectionsInitialized();
		ensureDailyReset();
		ActivityCrossWorldBossConfig bossCfg = ActivityCrossWorldBossManager.instance().getNullable(id);
		int freeTimes = bossCfg != null ? bossCfg.dailyFreeTimes : 0;
		int remainingTimes = Math.max(0, freeTimes + this.dailyBoughtCount - this.dailyUsedCount);
		return ActivityCrossWorldBossInfoResponse_11000402.newBuilder()
				.setActivityId(id)
				.setCrossActivityUid(crossActivityUid)
				.setState(state)
				.setKilled(cachedBossKilled)
				.setKillRewardClaimed(killRewardClaimed)
				.addAllUnlockedMilestoneIds(cachedUnlockedMilestones)
				.addAllClaimedMilestoneIds(claimedMilestoneIds)
				.setDailyUsedCount(dailyUsedCount)
				.setDailyBoughtCount(dailyBoughtCount)
				.setRemainingTimes(remainingTimes)
				.setMyTotalDamage(myTotalDamage)
				.setMyRank(myRank)
				.setEndTime(endTime)
				.setDestroyTime(destroyTime)
				.build();
	}

	/**
	 * 通用领奖抽象方法安全兜底实现（跨服世界Boss领奖统一通过 {@code 11000405} 入口处理）
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
		if (this.claimedMilestoneIds == null) {
			this.claimedMilestoneIds = new HashSet<>();
		}
		if (this.cachedUnlockedMilestones == null) {
			this.cachedUnlockedMilestones = new ArrayList<>();
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
		this.cachedBossKilled = false;
		if (this.cachedUnlockedMilestones == null) {
			this.cachedUnlockedMilestones = new ArrayList<>();
		} else {
			this.cachedUnlockedMilestones.clear();
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

	/**
	 * 获取今日已使用挑战次数
	 *
	 * @return 今日已用次数
	 */
	public int getDailyUsedCount() {
		return dailyUsedCount;
	}

	/**
	 * 设置今日已使用挑战次数
	 *
	 * @param dailyUsedCount 今日已用次数
	 */
	public void setDailyUsedCount(int dailyUsedCount) {
		this.dailyUsedCount = dailyUsedCount;
	}

	/**
	 * 获取今日已购买挑战次数
	 *
	 * @return 今日已购次数
	 */
	public int getDailyBoughtCount() {
		return dailyBoughtCount;
	}

	/**
	 * 设置今日已购买挑战次数
	 *
	 * @param dailyBoughtCount 今日已购次数
	 */
	public void setDailyBoughtCount(int dailyBoughtCount) {
		this.dailyBoughtCount = dailyBoughtCount;
	}

	/**
	 * 获取上次每日重置的自然日标识
	 *
	 * @return 自然日整数（如 20261125）
	 */
	public int getLastResetDay() {
		return lastResetDay;
	}

	/**
	 * 设置上次每日重置的自然日标识
	 *
	 * @param lastResetDay 自然日整数
	 */
	public void setLastResetDay(int lastResetDay) {
		this.lastResetDay = lastResetDay;
	}

	/**
	 * 获取本期已领取的里程碑 ID 集合
	 *
	 * @return 已领取里程碑 ID 集合
	 */
	public Set<Integer> getClaimedMilestoneIds() {
		ensureCollectionsInitialized();
		return claimedMilestoneIds;
	}

	/**
	 * 设置本期已领取的里程碑 ID 集合（允许反序列化传入 null，自动归一化为空集合）
	 *
	 * @param claimedMilestoneIds 已领取里程碑 ID 集合
	 */
	public void setClaimedMilestoneIds(Set<Integer> claimedMilestoneIds) {
		this.claimedMilestoneIds = claimedMilestoneIds != null ? claimedMilestoneIds : new HashSet<>();
	}

	/**
	 * 获取本人本期累计伤害镜像
	 *
	 * @return 累计伤害
	 */
	public long getMyTotalDamage() {
		return myTotalDamage;
	}

	/**
	 * 设置本人本期累计伤害镜像
	 *
	 * @param myTotalDamage 累计伤害
	 */
	public void setMyTotalDamage(long myTotalDamage) {
		this.myTotalDamage = myTotalDamage;
	}

	/**
	 * 获取本人当前战区排名镜像
	 *
	 * @return 当前排名（未上榜为 0）
	 */
	public int getMyRank() {
		return myRank;
	}

	/**
	 * 设置本人当前战区排名镜像
	 *
	 * @param myRank 当前排名
	 */
	public void setMyRank(int myRank) {
		this.myRank = myRank;
	}

	/**
	 * 判断本人是否已领取或直发本期最后一击击杀奖励
	 *
	 * @return true 表示已领取
	 */
	public boolean isKillRewardClaimed() {
		return killRewardClaimed;
	}

	/**
	 * 设置本人最后一击击杀奖励领取标记
	 *
	 * @param killRewardClaimed 是否已领取
	 */
	public void setKillRewardClaimed(boolean killRewardClaimed) {
		this.killRewardClaimed = killRewardClaimed;
	}

	/**
	 * 获取瞬态 Boss 击杀缓存状态
	 *
	 * @return true 表示已知 Boss 已被击杀
	 */
	public boolean isCachedBossKilled() {
		return cachedBossKilled;
	}

	/**
	 * 设置瞬态 Boss 击杀缓存状态
	 *
	 * @param cachedBossKilled Boss 击杀状态
	 */
	public void setCachedBossKilled(boolean cachedBossKilled) {
		this.cachedBossKilled = cachedBossKilled;
	}

	/**
	 * 获取瞬态已解锁里程碑缓存列表
	 *
	 * @return 已解锁里程碑 ID 列表
	 */
	public List<Integer> getCachedUnlockedMilestones() {
		ensureCollectionsInitialized();
		return cachedUnlockedMilestones;
	}

	/**
	 * 设置瞬态已解锁里程碑缓存列表
	 *
	 * @param cachedUnlockedMilestones 已解锁里程碑 ID 列表
	 */
	public void setCachedUnlockedMilestones(List<Integer> cachedUnlockedMilestones) {
		this.cachedUnlockedMilestones = cachedUnlockedMilestones != null ? new ArrayList<>(cachedUnlockedMilestones) : new ArrayList<>();
	}

	/**
	 * 判断瞬态快照是否已与 Cross 同步
	 *
	 * @return true 表示已同步
	 */
	public boolean isCachedSnapshotSynced() {
		return cachedSnapshotSynced;
	}

	/**
	 * 设置瞬态快照同步状态
	 *
	 * @param cachedSnapshotSynced 是否已同步
	 */
	public void setCachedSnapshotSynced(boolean cachedSnapshotSynced) {
		this.cachedSnapshotSynced = cachedSnapshotSynced;
	}
}
