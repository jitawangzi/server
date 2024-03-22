package cn.game.games.net.game.module.quest;

import java.util.function.Consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.manager.ConditionManager;

/**
 * 默认实现
 * 
 */
public abstract class AbstractCondition implements Condition {

	/** 只序列化字段，不调用get()序列化 */
	protected static final boolean fieldBased = true;
	protected static transient SerializeConfig serializeConfig = new SerializeConfig(fieldBased);

	protected long playerId;

	transient protected Player player;

	/** 需求编号 */
	protected byte index;

	protected int finishCount;

	transient protected int condition;

	/** 条件是否完成 */
	protected boolean achieve;

	transient protected Consumer<Condition> updateAction;
	transient protected Consumer<Condition> achieveAction;

	/** 监听的事件 */
	transient protected EventTypeEnum[] events;

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;

	}

	public AbstractCondition() {

	}

	@Override
	public byte getIndex() {
		return index;
	}

	@Override
	public int getRequireCount() {
		ConditionConfig conditionConfig = ConditionManager.instance().get(condition);
		return conditionConfig.numParam;
	}
	@Override
	public int getRequireId() {
		ConditionConfig conditionConfig = ConditionManager.instance().get(condition);
		return conditionConfig.numParam;
	}

	@Override
	public long getFinishCount() {
		return this.finishCount;
	}

	@Override
	public boolean isAchieve() {
		if (achieve)
			return true;
		achieve = getFinishCount() >= getRequireCount();
		if (achieve) {
			finishAction();
		}
		return achieve;
	}

	@Override
	public void setAchieve() {
		if (!achieve) {
			finishAction();
		}
		achieve = true;
	}

	// 反序列化使用
	public void setAchieve(boolean achieve) {
		this.achieve = achieve;
	}
	public void setFinishCount(int finishCount) {
		this.finishCount = finishCount;
	}

	public abstract void setEvents();


	/**
	 * 当处理关心的事件时，检查事件的相关参数，判断是否符合当前条件
	 * 
	 * @param event
	 * @return true，符合，需要变更条件数据，通常情况就是增加计数
	 */
	public abstract boolean checkEventParam(GameEvent event);

	/**
	 * 更新需求数量，默认增加1次/个
	 * 
	 * @param event
	 */
	public void updateRequireCount(GameEvent event) {
		finishCount++;
	}

	@Override
	public int getParam(int index) {
		ConditionConfig conditionConfig = ConditionManager.instance().get(condition);
		int[] extParam = conditionConfig.extParam;
		if (index >= extParam.length) {
			return 0;
		}
		return extParam[index];
	}
	@Override
	public int getParam() {
		return getParam(0);
	}
	@Override
	public String toSaveString() {
		return JSON.toJSONString(this, serializeConfig);
	}
	@Override
	public int getCondition() {
		return this.condition;
	}

	@Override
	public void finishAction() {
//		QuestHelper.conditionCmd(quest, condition);
//		PlayerHelper.sendProtcol(quest.getPlayerId(), MissionConditionCompletePush_20500001.newBuilder().setId(quest.getId()).setIndex(
//				index).build());
		if (achieveAction != null) {
			achieveAction.accept(this);
		}
	}
	@Override
	public void updateAction() {
		if (updateAction != null) {
			updateAction.accept(this);
		}
	}
	@Override
	public void unregisterEvent() {
//		
//		eventOp.unregisterEventHandler(events, this);
		player.getEventModule().unregisterEventHandler(this);
	}

	@Override
	public void registerEvent() {
//		
//		eventOp.registerEventHandler(events, this);
		player.getEventModule().registerEventHandler(this);

	}

	// 子类不用复写此方法
	@Override
	public void handleEvent(GameEvent event) {
		if (!checkEventParam(event))
			return;
		updateRequireCount(event);
		updateAction();
		isAchieve();
	}

	@Override
	public void init(long playerId, int condition, byte index, Consumer<Condition> updateAction, Consumer<Condition> achieveAction) {

		this.condition = condition;
		this.index = index;
		this.playerId = playerId;
		this.player = PlayerManager.getInstance().getPlayer(playerId);
		this.updateAction = updateAction;
		this.achieveAction = achieveAction;
		setEvents();
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	@Override
	public void addCount(int count) {
		if (count <= 0) {
			return;
		}
		finishCount += count;
		updateAction();
		isAchieve();
	}
}
