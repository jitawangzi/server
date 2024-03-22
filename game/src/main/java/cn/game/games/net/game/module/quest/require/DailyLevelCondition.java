package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.BattleLevelConfig;
import cn.game.protocol.generated.enume.OldConditionTypeEnum;
import cn.game.protocol.generated.manager.BattleLevelManager;

/**   
 * @Description 进行类型关卡、次数
 * @date 2019年1月8日 下午3:37:18
 * @author SYQ
 */
@ConditionType(type = OldConditionTypeEnum.GivenLevelComplete)
public class DailyLevelCondition extends AbstractCondition {

	public DailyLevelCondition() {

	}

	@Override
	public void setEvents() {
		super.events = new EventTypeEnum[] { EventTypeEnum.Level };
	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		int level = event.getIntParameter(0);
		BattleLevelConfig levelConfig = BattleLevelManager.getInstance().getBattleLevelConfig(level);
		int param = getParam(0);
//		if (levelConfig.getCat() == param) {
//			finishCount++;
//			return true;
//		}
		return false;
	}
}
