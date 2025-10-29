package cn.game.games.net.game.module.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.function.ToIntFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.google.protobuf.Message;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.module.quest.Quest;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;
import io.vertx.core.Future;

/**
 * 基本的活动，这个活动可能是全体活动，也可能是玩家的活动
 * 
 * 2021年6月9日 下午12:15:20
 * @author SYQ
 */
public abstract class ActivityBase {
	protected final static transient Logger log = LoggerFactory.getLogger(ActivityBase.class);

	/** 只序列化字段，不调用get()序列化 */
	private static transient final boolean fieldBased = true;
	private static transient SerializeConfig serializeConfig = new SerializeConfig(fieldBased);

	protected long uid;

	/** 配置表id */
	protected int id;
	/** 活动状态 */
	protected int state;

	/** 活动实际开始/参加时间,而不是配置的活动开启时间 */
	protected long startTime;
	protected long endTime;
	protected long destroyTime;
	protected String serverId;

	/** 
	 * 当前活动是否有红点显示
	 * @return
	 */
	public boolean hasRed() {
		return false;
	}

	public abstract Message buildActivityShowInfo();

	public abstract void registerEvent();

	public abstract void unregisterEvent();

	public Message buildActivityShowInfo(int id) {
		return null;
	}

	// 获取活动参与者列表方法
	public List<Long> getParticipants() {
		return new ArrayList<>();
	}

	// 判断玩家是否可以参与活动
	public boolean canJoin(long playerId) {
		return true; // 默认都可以参加
	}

	public ActivityInfo buildActivityInfo() {
		ActivityInfo.Builder builder = ActivityInfo.newBuilder();
		builder.setId(id);
		builder.setStateValue(state);
		builder.setEndTime((int) (destroyTime <= 0 ? 0 : (destroyTime - System.currentTimeMillis()) / 1000));
		return builder.build();
	}

	/** 
	 * 当活动状态修改时，同步当前活动数据
	 */
	public abstract void syncActivityInfo();

	/** 
	 * 领取活动奖励，看情况使用，适合活动奖励比较简单的情况。 
	 * @param id
	 * @return
	 */
	public abstract List<RewardInfo> receive(int id);

	@Deprecated
	public Future<List<RewardInfo>> asyncReceive(int id) {
		return Future.succeededFuture(receive(id));
	}

	/**
	 * 是否能够领取活动奖励
	 * @param ids 要领取的id集合
	 * @return 错误码
	 */
	@Deprecated
	public int canReceive(List<Integer> ids) {
		return ErrorMsgEnum.ok.ID;
	};

	@Deprecated
	public int canReceive(List<Integer> ids, List<Integer> rewardIdList, Player player) {
		int failSize = 0;
		int errCode = 0;
		QuestModule questModule = player.getQuestModule();
		for (int taskId : ids) {
			if (rewardIdList.contains(taskId)) {
				failSize++;
				errCode = ErrorMsgEnum.activity_lei_chong_has_reward.ID;
				continue;
			}
			Quest quest = questModule.get(taskId);
			if (quest.getState() != QuestHelper.CAN_GIVEWARD) {
				failSize++;
				errCode = ErrorMsgEnum.activity_task_not_finish.ID;
				continue;
			}
		}
		if (failSize == ids.size()) {
			return errCode;
		}
		return ErrorMsgEnum.ok.ID;
	};

	/** 活动开始，可以参加活动 */
	private void startUp() {
//		player.getActivityModule().syncActivityState(id);
		this.state = ActivityState.START_VALUE;
		this.startTime = System.currentTimeMillis();
		this.endTime = calcEndTime();
		this.destroyTime = calcDestroyTime();
		afterStart();
	}

	/** 活动结束,可能还保留，领取活动奖励等 */
	public void end() {
		this.state = ActivityState.CLOSE_VALUE;
		unregisterEvent();
		afterEnd();
	}

	/** 彻底销毁活动，不再展示，删除活动数据 */
	public void destroy() {
		this.state = ActivityState.NONE_VALUE;
		unregisterEvent();
		afterDestroy();
	};

	/** 
	 * 活动开始后的一些自定义行为
	 */
	protected void afterStart() {

	}

	protected void afterEnd() {

	}

	protected void afterDestroy() {

	}

	/** 
	 * 
	 * 跨天
	 * @return true 跨天活动数据有修改 ，不过现在没什么用了
	 */
	public boolean newDay() {
		return false;
	};

	public void init(int id, Object owner, boolean isNew) {

		this.id = id;
		registerEvent();
		if (isNew) {
			startUp();
		}
	}
	public void init(int id) {
		this.id = id;
	}

	/**
	 * 转化为存储字符用于存储 ，如果活动不需要保存到数据库，复写这个方法，返回null
	 * 
	 * @return 存储用字符串
	 */
	public String toSaveString() {
		return JsonUtil.toJsonStringWithType(this);
	}

	public int getId() {
		return id;
	}

	public int getState() {
		return state;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getEndTime() {
		return endTime;
	}

	public long getDestroyTime() {
		return destroyTime;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	/**
	 * 计算通用的结束/销毁时间
	 * @param durationType 获取时长类型的函数引用（如 cfg -> cfg.endDurationType）
	 * @param duration 获取时长值的函数引用（如 cfg -> cfg.endDuration）
	 * @param fallback 兜底获取时间的函数引用（如 id -> ActivityStateManager.getInstance().getEndTime(id)）
	 */
	private long calcTime(ToIntFunction<ActivityConfig> durationType, ToIntFunction<ActivityConfig> duration, IntFunction<Long> fallback) {
		long result = 0;
		ActivityConfig activityConfig = ActivityManager.instance().get(id);
		int dType = durationType.applyAsInt(activityConfig);

		if (dType > 0) {
			if (startTime == 0) { // 还没开始，默认返回0
				return result;
			}
			if (activityConfig.openType == ActivityHelper.OPENTYPE_SERVER_OPEN_DAY) {
				// 按开服时间开启的活动
				String serverId = getServerId();
				if (serverId == null || serverId.isEmpty()) {
					log.error("按开服时间开启的活动，必须指定服务器id");
					return result;
				}
				int serverOpenDay = ServerHelper.getServerOpenDay(serverId);
				int d = duration.applyAsInt(activityConfig);
				return DateUtil.nextDayStartTime(System.currentTimeMillis(), d - serverOpenDay + 1);
			}
			// 计算结束时间
			int d = duration.applyAsInt(activityConfig);
			if (dType == 1) {
				return DateUtil.nextDayStartTime(startTime, d);
			} else if (dType == 2) {
				return startTime + d * 1000L;
			} else {
				// 未知类型，按0返回或可记录告警
				return result;
			}
		} else {
			// 按时间开启的活动，走兜底
			return fallback.apply(id);
		}
	}

	public long calcEndTime() {
		return calcTime(cfg -> cfg.endDurationType, cfg -> cfg.endDuration,
				activityId -> ActivityStateManager.getInstance().getEndTime(activityId));
	}

	public long calcDestroyTime() {
		return calcTime(cfg -> cfg.destroyDurationType, cfg -> cfg.destroyDuration,
				activityId -> ActivityStateManager.getInstance().getDestroyTime(activityId));
	}

	/**
	 * 检测活动是否能够刷新 且刷新
	 */
	@Deprecated
	public void checkRefreshActivity() {
	};
}
