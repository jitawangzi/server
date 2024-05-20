package cn.game.games.net.game.module.activity;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.google.protobuf.Message;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventHandler;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**
 * 基本的活动，这个活动可能是全体活动，也可能是玩家的活动
 * 
 * @date 2021年6月9日 下午12:15:20
 * @author SYQ
 */
public abstract class ActivityBase implements EventHandler {
	/** 只序列化字段，不调用get()序列化 */
	private static transient final boolean fieldBased = true;
	private static transient SerializeConfig serializeConfig = new SerializeConfig(fieldBased);

	/** 配置表id */
	protected int id;
	/** 活动状态 */
	protected int state;

	/** 活动实际开始/参加时间 */
	protected long startTime;

	public abstract Message buildActivityShowInfo();

	public ActivityInfo buildActivityInfo() {
		ActivityInfo.Builder builder = ActivityInfo.newBuilder();
		builder.setId(id);
		builder.setStateValue(state);
		return builder.build();
	}

	public abstract List<RewardInfo> receive(int id);

	/** 活动开始，可以参加活动 */
	public void startUp() {
//		player.getActivityModule().syncActivityState(id);
		this.state = ActivityState.START_VALUE;
		this.startTime = System.currentTimeMillis();
	}

	/** 活动结束,可能还保留，领取活动奖励等 */
	public void shutDown() {
//		syncActivityState(id);
		this.state = ActivityState.CLOSE_VALUE;
	}

	/** 彻底销毁活动，不再展示，删除活动数据 */
	public void destroy() {
	};

	public boolean newDay() {
		return false;
	};

	public void init(int id, Player player, boolean isNew) {

//		ActivityStateManager.getInstance().registerEventHandler(events, this);
		if (player != null) {
			player.registerEventHandler(getEventTypes(), this);
		}
		this.id = id;
		if (isNew) {
			startUp();
		}
	}

	/**
	 * 转化为存储字符用于存储 ，如果活动不需要保存到数据库，复写这个方法，返回null
	 * 
	 * @return 存储用字符串
	 */
	public String toSaveString() {
		return JSON.toJSONString(this, serializeConfig);
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

	public long getEndTime() {
		long endTime = 0 ; 
		ActivityConfig activityConfig = ActivityManager.instance().get(id);
		if (activityConfig.durationType > 0) {
			if (activityConfig.durationType == 1) {
//				endTime
			} else if (activityConfig.durationType == 2) {
				endTime = startTime + activityConfig.duration * 1000;
			} else {

			}
		}
		return startTime;
	}

}
