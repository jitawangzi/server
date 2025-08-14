package cn.game.games.net.game.module.quest;

import java.util.function.Consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.manager.ConditionManager;

/**
 * 默认实现
 * 
 */
public abstract class AbstractCondition implements Condition {

	/** 只序列化字段，不调用get()序列化 */
	protected static final transient boolean fieldBased = true;
	protected static transient SerializeConfig serializeConfig = new SerializeConfig(fieldBased);

	transient protected long playerId;

	transient protected Player player;

	/** 需求编号 */
	protected byte index;

	protected int finishCount;

	protected int condition;

	/** 条件是否完成 */
	protected boolean achieve;

	transient protected Consumer<Condition> updateAction;
	transient protected Consumer<Condition> achieveAction;

	@Override
	public abstract EventTypeEnum[] getEventTypes();

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
		return conditionConfig.idParam;
	}

	@Override
	public long getFinishCount() {
		return this.finishCount;
	}

	/** 
	 * 默认的检查方法，检查当前计数是否大于等于需求数量，
	 * 如果需要小于的判断方法，需要自行覆盖
	 * @return
	 */
	protected boolean checkAchieve() {
		return getFinishCount() >= getRequireCount() ; 
	}
	@Override
	public boolean isAchieve() {
		if (achieve)
			return true;
		achieve = checkAchieve() ;
		if (achieve) {
			finishAction();
		}
		return achieve;
	}

	@Override
	public void setAchieve() {
		// 设置一下显示数量
		setFinishCount(getRequireCount());
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

	/**
	 * 当处理关心的事件时，检查事件的相关参数，判断是否符合当前条件
	 * 
	 * @param event
	 * @return true，符合，需要变更条件数据，通常情况就是增加计数
	 */
	public abstract boolean checkEventParam(PlayerEvent event);

	/**
	 * 更新需求数量，默认增加1次/个
	 * 
	 * @param event
	 */
	public void updateRequireCount(PlayerEvent event) {
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
	public int[] getExtParam() {
		ConditionConfig conditionConfig = ConditionManager.instance().get(condition);
		return conditionConfig.extParam;
	}
	@Override
	public String toSaveString() {
//		return JsonUtil.toJsonString(this);
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
		player.getPlayerEventBus().unregister(this);
	}

	@Override
	public void registerEvent() {
//		
		player.getPlayerEventBus().register(this);

	}

	// 子类不用复写此方法
	@Override
	public void handleEvent(PlayerEvent event) {
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
	}

	@Override
	public void init(Player player) {
		this.player = player;
		this.playerId = player.getPlayerId();
	}

	@Override
	public void addCount(int count) {
		if (count <= 0) {
			return;
		}
		finishCount += count;
		int requireCount = getRequireCount();
		if (finishCount > requireCount){
			finishCount = requireCount;
		}
		updateAction();
		isAchieve();
	}
}
