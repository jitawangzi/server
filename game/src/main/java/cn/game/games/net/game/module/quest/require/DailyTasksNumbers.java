package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.MissionConfig;
import cn.game.protocol.generated.enume.OldConditionTypeEnum;
import cn.game.protocol.generated.enume.MissionTypeEnum;

/**
 * 完成完成每日任务数量
 * 
 */
@ConditionType(type = OldConditionTypeEnum.DailyTasksNumbers)
public class DailyTasksNumbers extends AbstractCondition {

	public DailyTasksNumbers() {

	}

	@Override
	public void setEvents() {
		super.events = new EventTypeEnum[] { EventTypeEnum.QuestFinish };
	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		int id = event.getIntParameter(0);
		MissionConfig missionConfig = QuestHelper.getMissionConfig(id);
		if (missionConfig.getType() == MissionTypeEnum.Daily) {
			return true;
		}
		return false;
	}
}
