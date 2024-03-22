package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import cn.game.games.cache.op.impl.RoleOp;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.quest.Condition;
import cn.game.games.net.game.module.quest.ConditionContainer;
import cn.game.games.net.game.module.quest.require.tag.AbstractRoleTagCondition;
import cn.game.protocol.generated.config.RoleTagConfig;
import cn.game.protocol.generated.manager.RoleTagManager;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class RoleTagQuest implements Serializable, DbEntity {
    /**
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 角色配置表id
	 * @mbg.generated
	 */
	private Integer roleDictId;
	/**
	 * 标签id
	 * @mbg.generated
	 */
	private Integer tagId;
	/**
	 * 是否达成
	 * @mbg.generated
	 */
	private Boolean isFinished;
	/**
	 * 完成次数
	 * @mbg.generated
	 */
	private Integer finishedTimes;
	/**
	 * @mbg.generated
	 */
	private String params;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

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
	public Integer getRoleDictId() {
		return roleDictId;
	}

	/**
	 * @mbg.generated
	 */
	public void setRoleDictId(Integer roleDictId) {
		this.roleDictId = roleDictId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getTagId() {
		return tagId;
	}

	/**
	 * @mbg.generated
	 */
	public void setTagId(Integer tagId) {
		this.tagId = tagId;
	}

	/**
	 * @mbg.generated
	 */
	public Boolean getIsFinished() {
		return isFinished;
	}

	/**
	 * @mbg.generated
	 */
	public void setIsFinished(Boolean isFinished) {
		this.isFinished = isFinished;
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
		return cn.game.games.net.data.mapper.RoleTagQuestMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, roleDictId, tagId };
	}

	private transient ConditionContainer conditionContainer;
    
	public void initCondition() {
		RoleTagConfig roleTagConfig = RoleTagManager.getInstance().getRoleTagConfig(this.tagId);
		List<Integer> conditions = roleTagConfig.getCondition();
		
		Consumer<Condition> condChangeAction = c -> {
			this.updateDb();
		};
		Consumer<Condition> condAchieveAction = c -> {
		};
		// 达成时的操作:发放标签
		Consumer<Condition> finishAction = t -> {
			Player player = PlayerManager.getInstance().getPlayer(playerId);

			RoleOp roleOp = player.getModule(RoleOp.class);
			roleOp.addTag(this.roleDictId, this.tagId);
//			ExploreOp exploreOp = player.getModule(ExploreOp.class);
//			exploreOp.addTagRecord(this.roleDictId, this.tagId);
			
			this.isFinished = true;
			this.unregEvent();
			boolean achieve = roleOp.checkTagAchieve(this.roleDictId, this.tagId);
			if (!achieve) {
				this.updateDb();
			}
		};
		conditionContainer = new ConditionContainer();
		conditionContainer.create(playerId, conditions, true, condChangeAction, condAchieveAction, finishAction, params);
		List<Condition> requires = conditionContainer.getRequires();
		for (Condition condition : requires) {
			AbstractRoleTagCondition c = (AbstractRoleTagCondition) condition;
			c.setRoleId(this.roleDictId);
		}
		if (StringUtils.isEmpty(params)) {
			toParams();
		}
		regEvent();
	}
	
	private void updateDb() {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		RoleOp roleOp = player.getModule(RoleOp.class);
		toParams();
		roleOp.update(this);
	}
	
	public void toParams() {
		setParams(conditionContainer.toString());
	}

	public void regEvent() {
		if (!this.isFinished) {
			conditionContainer.regEvent();
		}
	}
	
	public void unregEvent() {
		conditionContainer.unregEvent();
	}
	
	public static RoleTagQuest valueOf(long playerId, int roleId, int tagId) {
		RoleTagQuest quest = new RoleTagQuest();
		quest.playerId = playerId;
		quest.roleDictId = roleId;
		quest.tagId = tagId;
		quest.isFinished = false;
		quest.finishedTimes = 0;
		quest.params = "";
		return quest;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}