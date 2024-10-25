package cn.game.games.net.game.module.activity;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.google.protobuf.Message;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventHandler;
import cn.game.games.net.game.helper.QuestHelper;
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
import io.vertx.core.Future;

/**
 * 基本的活动，这个活动可能是全体活动，也可能是玩家的活动
 * 
 * 2021年6月9日 下午12:15:20
 * @author SYQ
 */
public abstract class ActivityBase implements EventHandler {
	protected final static  transient Logger log = LoggerFactory.getLogger(ActivityBase.class);


	/** 只序列化字段，不调用get()序列化 */
	private static transient final boolean fieldBased = true;
	private static transient SerializeConfig serializeConfig = new SerializeConfig(fieldBased);

	/** 配置表id */
	protected int id;
	/** 活动状态 */
	protected int state;

	/** 活动实际开始/参加时间 */
	protected long startTime;
	protected long endTime;

	public abstract Message buildActivityShowInfo();

	public Message buildActivityShowInfo(int id) {
		return null;
	}

	public ActivityInfo buildActivityInfo() {
		ActivityInfo.Builder builder = ActivityInfo.newBuilder();
		builder.setId(id);
		builder.setStateValue(state);
		builder.setEndTime((int) (endTime <= 0 ? 0 : (System.currentTimeMillis() - endTime) / 1000));
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
	public   Future<List<RewardInfo>> asyncReceive(int id){
        return Future.succeededFuture(receive(id));
	}


	/**
	 * 是否能够领取活动奖励
	 * @param ids 要领取的id集合
	 * @return 错误码
	 */
	public  int canReceive(List<Integer> ids){
		return ErrorMsgEnum.ok.ID;
	};
	public  int canReceive(List<Integer> ids,List<Integer> rewardIdList, Player player){
		int failSize = 0;
		int errCode = 0;
		QuestModule questModule = player.getQuestModule();
		for (int taskId :ids ) {
			if (rewardIdList.contains(taskId)){
				failSize++;
				errCode = ErrorMsgEnum.activity_lei_chong_has_reward.ID;
				continue;
			}
			Quest quest = questModule.get(taskId);
			if (quest.getState() != QuestHelper.CAN_GIVEWARD){
				failSize++;
				errCode = ErrorMsgEnum.activity_task_not_finish.ID;
				continue;
			}
		}
		if (failSize == ids.size()){
			return errCode;
		}
		return ErrorMsgEnum.ok.ID;
	};

	/** 活动开始，可以参加活动 */
	public void startUp() {
//		player.getActivityModule().syncActivityState(id);
		this.state = ActivityState.START_VALUE;
		this.startTime = System.currentTimeMillis();
		this.endTime = calcEndTime();
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

	public void setState(int state) {
		this.state = state;
	}

	public long getEndTime() {
		return endTime;
	}

	/** 
	 * 获取活动的结束时间
	 * @return
	 */
	public long calcEndTime() {
		long endTime = 0 ; 
		ActivityConfig activityConfig = ActivityManager.instance().get(id);
		if (activityConfig.durationType > 0) {
			if (startTime == 0) { // 还没开始，默认返回0
				return endTime;
			}
			if (activityConfig.durationType == 1) {
				endTime = DateUtil.nextDayStartTime(startTime, activityConfig.duration);
			} else if (activityConfig.durationType == 2) {
				endTime = startTime + activityConfig.duration * 1000;
			}
		} else {
			endTime = ActivityStateManager.getInstance().getEndTime(id);
		}
		return endTime;
	}

	/**
	 * 检测活动是否能够刷新 且刷新
	 */
	public  void checkRefreshActivity(){};
}
