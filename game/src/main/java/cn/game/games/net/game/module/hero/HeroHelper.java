package cn.game.games.net.game.module.hero;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.protocol.generated.config.ParameterConsumeConfig;
import cn.game.protocol.generated.manager.ParameterConsumeManager;

public class HeroHelper {

	private static final Logger log = LoggerFactory.getLogger(HeroHelper.class);
	
	/** 
	 * 计算某职业英雄升级需要消耗的物品
	 * @param level 英雄等级
	 * @param Career 英雄职业
	 * @return
	 */
	public static List<Entry<Integer, Integer>> calcUpLevelCost(int level, int Career) {
		List<Entry<Integer, Integer>> ret = new ArrayList<>(3);
		ParameterConsumeConfig uiTypetypeParam = ParameterConsumeManager.instance().getUITypetypeParam(1, Career);
		if (uiTypetypeParam.item1 > 0) {
			ret.add(new SimpleEntry<Integer, Integer>(uiTypetypeParam.item1,
					calcCostCount(level, uiTypetypeParam.item1Param)));
		}
		if (uiTypetypeParam.item2 > 0) {
			ret.add(new SimpleEntry<Integer, Integer>(uiTypetypeParam.item2,
					calcCostCount(level, uiTypetypeParam.item2Param)));
		}
		if (uiTypetypeParam.item3 > 0) {
			ret.add(new SimpleEntry<Integer, Integer>(uiTypetypeParam.item3,
					calcCostCount(level, uiTypetypeParam.item3Param)));
		}
		return ret;
	}

	/** 
	 * 根据参数变量，计算增量消耗数值
	 * @param level
	 * @param itemParam
	 * @return
	 */
	private static int calcCostCount(int level, int[] itemParam) {
		int max = itemParam[2];
		int ret = itemParam[0] + (level * itemParam[1]);
		return max == 0 ? ret : Math.min(ret, max);
	}

}
