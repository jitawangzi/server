package cn.game.games.net.game.module.activity;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.Message;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventHandler;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**
 * 基本的活动，这个活动可能是全体活动，也可能是玩家的活动，或者都是
 * 
 * @date 2021年6月9日 下午12:15:20
 * @author SYQ
 */
public abstract class ActivityBase implements EventHandler {
	/** 只序列化字段，不调用get()序列化 */
	private static transient final boolean fieldBased = true;
	private static transient SerializeConfig serializeConfig = new SerializeConfig(fieldBased);

	@JsonIgnore
	protected transient Player player;
	/** 监听的事件，通常在玩家活动里监听事件 */
	protected transient EventTypeEnum[] events;

	/** 配置表id */
	protected int id;

	public abstract Message buildActivityInfo();

	public abstract List<RewardInfo> receive(int id);

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	/** 活动开始 */
	public abstract void startUp();

	/** 活动结束 */
	public abstract void shutDown();

	/** 彻底销毁活动，不再展示 */
	public void destroy() {
	};

	@JsonIgnore
	public abstract void setEvents(EventTypeEnum[] events);

	public boolean newDay() {
		return false;
	};

	public void init(int id, Player player, boolean isNew) {

		this.player = player;
		setEvents(events);
//		ActivityStateManager.getInstance().registerEventHandler(events, this);
		this.id = id;
		if (isNew) {
			startUp();
		}

	}

	@Override
	public void handleEvent(GameEvent event) {
	};

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
}
