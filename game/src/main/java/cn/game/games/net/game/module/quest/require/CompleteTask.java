package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

/**
 * @ClassName CompleteTask
 *
 * @description: 42	CompleteTask	完成指定任务		任务ID
 * @author: ly
 * @create: 2024-09-05 20:03 @Version 1.0
 */
@ConditionType(type = ConditionTypeEnum.CompleteTask)
public class CompleteTask extends AbstractCondition {
    @Override
    public EventTypeEnum[] getEventTypes() {
        return new EventTypeEnum[]{EventTypeEnum.QuestFinish};
    }

    @Override
    public boolean checkEventParam(GameEvent event) {
        int taskId = event.getIntParameter(0);
        int[] extParam = getExtParam();
        int requireTaskId =  extParam.length > 0 ? extParam[0] : 0;
        return taskId == requireTaskId;
    }

}
