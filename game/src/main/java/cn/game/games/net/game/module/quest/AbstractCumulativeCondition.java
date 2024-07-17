package cn.game.games.net.game.module.quest;

import cn.game.games.net.game.helper.PlayerHelper;

/**    
 * 累计计数的条件，累计的计数一般存在某个统一的地方，不在任务里
 * 2024年6月27日 上午10:54:21
 * @author SYQ
 */
public abstract class AbstractCumulativeCondition extends AbstractCondition {

	@Override
	public long getFinishCount() {
		return PlayerHelper.getConditionCount(player, condition);
	}
}
