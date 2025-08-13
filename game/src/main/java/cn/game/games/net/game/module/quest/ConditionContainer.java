package cn.game.games.net.game.module.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import cn.game.core.base.ServerContext;

/**    
 * 条件容器，一般管理那种需要持续观察变化的那种条件
 * 2022年6月6日 下午12:28:49
 * @author SYQ
 */
public class ConditionContainer {

	/** 管理的各种条件，或者说是需求 */
	private List<Condition> requires = new ArrayList<Condition>();
	/** 条件之间，是or还是and的关系 */
	private boolean condOr;
	/** 所有条件达成时的操作 */
	transient private Consumer<Condition> achieveAction;
//	private Consumer<Condition> changeWrapAction;
	/** 当某个条件达成，用来触发全部条件检查 */
	transient private Consumer<Condition> achieveWrapAction;

	public ConditionContainer() {
	}

	@Deprecated
	private ConditionContainer create(long playerId, List<Integer> conditions, boolean or, Consumer<Condition> condChangeActions,
			Consumer<Condition> condAchieveActions, Consumer<Condition> achieveAction) {
		achieveWrapAction = r -> {
			condAchieveActions.accept(r);
			checkFinish(r);
		};

		List<Condition> createQuestCondition = ConditionFactory.createAndInitConditions(playerId, conditions, condChangeActions,
				achieveWrapAction);

		set(createQuestCondition, or, achieveAction);
		return this;
	}

	public ConditionContainer create(long playerId, List<Integer> conditions, boolean or, Consumer<Condition> condChangeActions,
			Consumer<Condition> condAchieveActions, Consumer<Condition> achieveAction, String dbString) {

		achieveWrapAction = r -> {
			condAchieveActions.accept(r);
			checkFinish(r);
		};
		if (ServerContext.getInstance().isSinglePlayerTable()) {
			set(or, achieveAction);
			if (requires.isEmpty()) {
				this.requires = ConditionFactory.createAndInitConditions(playerId, conditions, condChangeActions, achieveWrapAction);
			} else {
				for (byte i = 0; i < this.requires.size(); i++) {
					Condition condition = requires.get(i);
					condition.init(playerId, condition.getCondition(), i, condChangeActions, achieveWrapAction);
				}
			}
		} else {
			List<Condition> conditionsCreate = null;
			if (!StringUtils.isEmpty(dbString)) {
				String[] condString = dbString.split("\\|");
				conditionsCreate = ConditionFactory.createConditionfromSaveString(playerId, condString, conditions, condChangeActions,
						achieveWrapAction);
			} else {
				conditionsCreate = ConditionFactory.createAndInitConditions(playerId, conditions, condChangeActions, achieveWrapAction);
			}
			set(conditionsCreate, or, achieveAction);
		}
		return this;
	}

	public ConditionContainer create(long playerId, int condition, boolean or, Consumer<Condition> condChangeActions,
			Consumer<Condition> condAchieveActions, Consumer<Condition> achieveAction, String dbString) {
		List<Integer> conditions = new ArrayList<>(1);
		conditions.add(condition);
		return create(playerId, conditions, or, condChangeActions, condAchieveActions, achieveAction, dbString);
	}

	private void set(List<Condition> requires, boolean condOr, Consumer<Condition> achieveAction) {
		this.requires = requires;
		this.condOr = condOr;
		this.achieveAction = achieveAction;
	}

	private void set(boolean condOr, Consumer<Condition> achieveAction) {
		this.condOr = condOr;
		this.achieveAction = achieveAction;
	}

	/** 
	 * 检查全部条件是否已达成
	 * @return
	 */
	private boolean checkAchieve() {
		boolean achieve = false;
		if (condOr) {
			for (Condition condition : requires) {
				if (condition.isAchieve()) {
					achieve = true;
				}
			}
		} else {
			achieve = true;
			for (Condition condition : requires) {
				if (!condition.isAchieve()) {
					achieve = false;
				}
			}
		}
		return achieve;
	}

	/** 
	 * 检查所有条件达成，执行条件达成的操作
	 * @param arg
	 */
	public void checkFinish(Condition arg) {
		if (checkAchieve()) {
			if (achieveAction != null) {
				achieveAction.accept(arg);
			}
		}
	}

	public List<Condition> getRequires() {
		return requires;
	}

	public void setRequires(List<Condition> requires) {
		this.requires = requires;
	}

	public Condition getRequire(int id) {

		for (Condition questCondition : requires) {
			if (questCondition.getCondition() == id) {
				return questCondition;
			}
		}
		return null;
	}

	public boolean isCondOr() {
		return condOr;
	}

	public void setCondOr(boolean condOr) {
		this.condOr = condOr;
	}
	/*	@Override
		public String toString() {
	
			StringBuffer requiresBuffer = new StringBuffer();
			for (int i = 0; i < requires.size(); i++) {
				requiresBuffer.append(requires.get(i).toSaveString()).append("|");
			}
			return requiresBuffer.toString();
		}*/

	public void regEvent() {
		for (Condition questCondition : requires) {
			questCondition.registerEvent();
		}
	}

	public void unregEvent() {
		for (Condition condition : this.requires) {
			condition.unregisterEvent();
		}
	}

	/** 
	 * 直接增加某条件的完成数量，一般是客户端更新时使用
	 * @param index
	 * @param count
	 */
	public void addCount(int index, int count) {
		if (count <= 0) {
			return;
		}
		Condition condition = requires.get(index);
		condition.addCount(count);
	}

}
