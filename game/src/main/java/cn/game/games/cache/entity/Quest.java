package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import cn.game.games.cache.base.DbEntity;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.quest.Condition;
import cn.game.games.net.game.module.quest.ConditionContainer;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.protocol.protobuf.BaseMsg.UpdateType;
import cn.game.protocol.protobuf.QuestMsg.QuestConditionCompletePush_20500001;
import cn.game.protocol.protobuf.QuestMsg.QuestInfo;

public class Quest implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 任务id
	 * @mbg.generated
	 */
	private Integer id;
	/**
	 * 任务状态
	 * @mbg.generated
	 */
	private Byte state;
	/**
	 * 任务开始时间
	 * @mbg.generated
	 */
	private Long startTime;
	/**
	 * 完成次数
	 * @mbg.generated
	 */
	private Integer finishedTimes;
	/**
	 * 完成时间
	 * @mbg.generated
	 */
	private Long endTime;
	/**
	 * @mbg.generated
	 */
	private String params;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/** 任务完成条件 */
//	private transient List<Condition> requires = new ArrayList<Condition>();
	private ConditionContainer conditionContainer;

	/**
	 * @mbg.generated
	 */
	public Long getPlayerId() {
		return playerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @mbg.generated
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getState() {
		return state;
	}

	/**
	 * @mbg.generated
	 */
	public void setState(Byte state) {
		this.state = state;
	}

	/**
	 * @mbg.generated
	 */
	public Long getStartTime() {
		return startTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getFinishedTimes() {
		return finishedTimes;
	}

	/**
	 * @mbg.generated
	 */
	public void setFinishedTimes(Integer finishedTimes) {
		this.finishedTimes = finishedTimes;
	}

	/**
	 * @mbg.generated
	 */
	public Long getEndTime() {
		return endTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	/**
	 * @mbg.generated
	 */
	public String getParams() {
		return params;
	}

	/**
	 * @mbg.generated
	 */
	public void setParams(String params) {
		this.params = params;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.QuestMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, id };
	}

	@Deprecated
	public void disable() {

		close();
		setState(QuestHelper.DISABLED);
	}

	public boolean receive() {
		this.finishedTimes += 1;
		endTime = System.currentTimeMillis();
		return true;
	}

	public void close() {
		if (state == QuestHelper.ACCEPTED) {
			unregEvent();
		}
//		this.conditionContainer = null;
	}

	/**
	 * 只序列化使用
	 */
	public Quest() {
	};

	public void clear() {
		close();
		setParams(null);
		this.conditionContainer = null;
		setFinishedTimes(0);
		setEndTime(0l);

		Player player = PlayerManager.getInstance().getPlayer(getPlayerId());
		QuestModule questModule = player.getQuestModule();
		questModule.setState(this, QuestHelper.CAN_ACCEPT);
		setStartTime(System.currentTimeMillis());

	}

	public Quest(long playerId, int id) {

		setId(id);
		setStartTime(System.currentTimeMillis());
		setFinishedTimes(0);
		setEndTime(0L);
		setPlayerId(playerId);
		setState(QuestHelper.INIT);
	}

	public void toParams() {
		if (conditionContainer != null) {
			setParams(conditionContainer.toString());
		}
	}

	public void initCondition() {

		if (this.state != QuestHelper.ACCEPTED) {
			return;
		}
		QuestConfig questConfig = QuestHelper.getQuestConfig(id);
		Consumer<Condition> condChangeAction = c -> {
//			// 推送条件数量变更
			QuestHelper.notifyQuestChange(this, UpdateType.UPDATE);
//			QuestHelper.updateParams(this);
		};
		Consumer<Condition> condAchieveAction = c -> {
//			QuestHelper.conditionCmd(this, c.getCondition());
			PlayerHelper.sendProtocol(this.getPlayerId(), QuestConditionCompletePush_20500001.newBuilder().setId(this.getId())
					.setIndex(c.getIndex()).build());
//			QuestHelper.updateParams(this);
		};
		Consumer<Condition> finishAction = t -> {
			Player player = PlayerManager.getInstance().getPlayer(playerId);
			QuestModule questModule = player.getModule(QuestModule.class);
			if (this.getState() == QuestHelper.ACCEPTED) {
				questModule.setState(this, QuestHelper.CAN_GIVEWARD);
			}
		};
		if (conditionContainer == null) {
			conditionContainer = new ConditionContainer();
		}
		conditionContainer.create(playerId, questConfig.Condition, false, condChangeAction,
				condAchieveAction, finishAction, null);
		/*		if (StringUtils.isEmpty(params)) {
					toParams();
				}*/
		regEvent();
	}

	public void regEvent() {
		if (this.state == QuestHelper.ACCEPTED) {
			conditionContainer.regEvent();
		}
	}
	public void unregEvent() {

		if (conditionContainer != null) {
			conditionContainer.unregEvent();
		}
	}

	public List<Condition> getRequires() {

		return conditionContainer == null ? Collections.EMPTY_LIST : conditionContainer.getRequires();
	}
	public Condition getRequire(int id) {

		return conditionContainer == null ? null : conditionContainer.getRequire(id);
	}

	public ConditionContainer getConditionContainer() {
		return conditionContainer;
	}

	/** 
	 * 检查是否所有条件已达成
	 */
	public void checkFinish() {
		if (conditionContainer != null) {
			conditionContainer.checkFinish(null);
		}
	}

	public QuestInfo toQuestInfo() {

		QuestInfo.Builder questInfo = QuestInfo.newBuilder();
		questInfo.setId(id);
		if (conditionContainer != null) {
			List<Condition> requires = conditionContainer.getRequires();
			if (requires != null) {
				for (int i = 0; i < requires.size(); i++) {
					long count = requires.get(i).getFinishCount();
					if (count >= Integer.MAX_VALUE) {
						count = Integer.MAX_VALUE;
					}
					questInfo.addFinishCount((int) count);
				}
			}
		}
		questInfo.setState(getState());
		return questInfo.build();
	}
}