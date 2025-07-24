package cn.game.games.net.game.module.quest;

import cn.game.games.net.game.helper.PlayerHelper;

/**    
 * 每日计数的条件，每日的计数，记录在某个统一的地方，不记录在条件示例里
 * 2025年7月24日 17:03:18
 * @author SYQ
 */
public abstract class AbstractDayCountCondition extends AbstractCondition {

	@Override
	public long getFinishCount() {
		return PlayerHelper.getConditionCount(player, condition);
	}
}
