package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.activity.impl.player.ActivityMeiRiBaoLi;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.util.DateUtil;

/**
 * @ClassName AccumulatedRechargeDay
 * @description: 43	AccumulatedRechargeDay	累计充值		天数	日进斗金
 * @author: ly
 * @create: 2024-09-05 19:06 @Version 1.0
 */
@ConditionType(type = ConditionTypeEnum.AccumulatedRechargeTotalDay)
public class AccumulatedRechargeTotalDay extends AbstractCondition {

    @Override
    public EventTypeEnum[] getEventTypes() {
        return new EventTypeEnum[]{EventTypeEnum.Charge};
    }

    @Override
    public boolean checkEventParam(PlayerEvent event) {
//        日进斗金：第一天充值了10元，第二天充了30，相当于只完成了第一个的任务
        int activityId = getExtParam()[0];
        int day = getExtParam()[1];
        int recharge = getExtParam()[2];
        if (player.getActivityModule().get(activityId) == null){
            return false;
        }
        ActivityMeiRiBaoLi meiRiBaoLi = (ActivityMeiRiBaoLi) player.getActivityModule().get(activityId);
        if (DateUtil.isSameDay(System.currentTimeMillis(), meiRiBaoLi.getFinishRechargeTimer())){
            return false;
        }
        meiRiBaoLi.addTotalRecharge(event.getIntParameter(0),event);
        boolean res = false;
        if (meiRiBaoLi.getTotalRecharge() >= recharge){
            meiRiBaoLi.addTotalRechargeNum(1);
             res = day == meiRiBaoLi.getTotalRechargeNum();
        }
        return res;
  }

    @Override
    public void updateRequireCount(PlayerEvent event) {
        addCount(1);
        int activityId = getExtParam()[0];
        ActivityMeiRiBaoLi meiRiBaoLi = (ActivityMeiRiBaoLi) player.getActivityModule().get(activityId);
        meiRiBaoLi.setTotalRecharge(0);
        meiRiBaoLi.setFinishRechargeTimer(System.currentTimeMillis());
    }
}
