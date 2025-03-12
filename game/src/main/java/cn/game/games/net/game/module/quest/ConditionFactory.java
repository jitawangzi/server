package cn.game.games.net.game.module.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;

import cn.game.games.core.clazz.ClassManager;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.manager.ConditionManager;

public class ConditionFactory {

	protected static transient ParserConfig serializeConfig = new ParserConfig();

	public static Condition createConditionfromSaveString(long playerId, String string, int condition, byte index,
			Consumer<Condition> updateAction, Consumer<Condition> achieveAction) {

		ConditionConfig conditionConfig = ConditionManager.instance().get(condition);
		Condition questRequire = JSON.parseObject(string,
				ClassManager.getInstance().getConditionClass(conditionConfig.type));
		questRequire.init(playerId, condition, index, updateAction, achieveAction);
		return questRequire;
	}
	public static List<Condition> createConditionfromSaveString(long playerId, String[] saveStrings, List<Integer> conditions,
			Consumer<Condition> updateAction, Consumer<Condition> achieveAction) {
		List<Condition> ret = new ArrayList<>();

		for (byte i = 0; i < conditions.size(); i++) {
			int condition = conditions.get(i);
			ConditionConfig conditionConfig = ConditionManager.instance().get(condition);
			Condition questRequire = JSON.parseObject(saveStrings[i],
					ClassManager.getInstance().getConditionClass(conditionConfig.type));
			questRequire.init(playerId, condition, i, updateAction, achieveAction);
			ret.add(questRequire);
		}

		return ret;
	}

	public static Condition createAndInitCondition(long playerId, int condition, byte index, Consumer<Condition> updateAction,
			Consumer<Condition> achieveAction) {
		ConditionConfig conditionConfig = ConditionManager.instance().get(condition);
		Condition questCondition = createCondition(conditionConfig.type);
		questCondition.init(playerId, condition, index, updateAction, achieveAction);
		return questCondition;

	}

	public static List<Condition> createAndInitConditions(long playerId, List<Integer> conditions, Consumer<Condition> updateAction,
			Consumer<Condition> achieveAction) {

		List<Condition> ret = new ArrayList<Condition>(conditions.size());
		for (byte i = 0; i < conditions.size(); i++) {
			Condition questCondition = createAndInitCondition(playerId, conditions.get(i), i, updateAction, achieveAction);
			ret.add(questCondition);
		}
		return ret;
	}

	private static Condition createCondition(int type) {
		Condition condition = ClassManager.getInstance().createConditionClassInstance(type);
		return condition;
	}
}
