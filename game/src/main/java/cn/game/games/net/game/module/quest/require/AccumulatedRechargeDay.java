package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.activity.impl.player.ActivityMeiRiBaoLi;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.util.DateUtil;

/**
 * @ClassName AccumulatedRechargeDay
 * @description: 41	AccumulatedRechargeDay	累计充值		天数
 * @author: ly
 * @create: 2024-09-05 19:06 @Version 1.0
 */
@ConditionType(type = ConditionTypeEnum.AccumulatedRechargeDay)
public class AccumulatedRechargeDay extends AbstractCondition {

    @Override
    public EventTypeEnum[] getEventTypes() {
        return new EventTypeEnum[]{EventTypeEnum.Charge};
    }

    @Override
    public boolean checkEventParam(GameEvent event) {
        int activityId = getExtParam()[0];
        int day = getExtParam()[1];
        if (player.getActivityModule().get(activityId) == null){
            return false;
        }
        ActivityMeiRiBaoLi meiRiBaoLi = (ActivityMeiRiBaoLi) player.getActivityModule().get(activityId);
        int difDay = DateUtil.diffDays(meiRiBaoLi.getStartTime()) + 1;
        return difDay == day;
    }

    @Override
    public void updateRequireCount(GameEvent event) {
        int rechargeNum = event.getIntParameter(0);
        addCount(rechargeNum);
    }
}
