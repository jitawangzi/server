package cn.game.games.net.game.module.activity.impl.player;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.PlayerActivityBase;
import cn.game.games.net.game.module.award.Goods;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.config.FundPassRewardsConfig;
import cn.game.protocol.generated.config.SevenDaysSigninConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.enume.WelfareTypeEnum;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.generated.manager.FundPassManager;
import cn.game.protocol.generated.manager.FundPassRewardsManager;
import cn.game.protocol.generated.manager.SevenDaysSigninManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.ActivityMsg;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninInfoResponse_11000025;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.ShopMsg;
import cn.game.util.DateUtil;
import cn.game.util.GameUtil;
import com.google.protobuf.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ActivityType(type = ActivityTypeEnum.ActivityFundPassSign)
public class FundPassSignActivity extends PlayerActivityBase {
    private static transient EventTypeEnum[] events = new EventTypeEnum[]{EventTypeEnum.NewDay};
    private Map<Integer, FundPassSignActivityData> FundPassSignActivityData = new HashMap<>();
    private int loginday = 0;
    private final int Begin_Free = 10001;
    private final int Begin_30 = 11001;
    private final int Begin_60 = 12001;
    private final int Max_Day = 30;

    @Override
    public void afterStart() {
        loginday = 1;
        for (int i = Begin_60; i <= Begin_60 + Max_Day; i++) {
            create(i, false);
        }
        for (int i = Begin_30; i <= Begin_30 + Max_Day; i++) {
            create(i, false);
        }
        for (int i = Begin_Free; i <= Begin_Free + Max_Day; i++) {
            create(i, true);
        }
    }
    @Override
    public void afterEnd() {
        loginday = 0;
        for (int i = Begin_60; i <= Begin_60 + Max_Day; i++) {
            create(i, false);
        }
        for (int i = Begin_30; i <= Begin_30 + Max_Day; i++) {
            create(i, false);
        }
        for (int i = Begin_Free; i <= Begin_Free + Max_Day; i++) {
            create(i, true);
        }
    }
    @Override
    public void afterDestroy() {
        loginday = 0;
        FundPassSignActivityData.clear();
    }
    void create(int i, boolean unlock) {
        var fundPassSignActivityData = new FundPassSignActivityData();
        fundPassSignActivityData.Id = i;
        fundPassSignActivityData.day = i % 100;
        fundPassSignActivityData.unlock = unlock;
        if (i == 1) {
            fundPassSignActivityData.finish = true;
        }
        FundPassSignActivityData.put(i, fundPassSignActivityData);
    }

    public Message buy(int id) {
        if (id == 15) {
            for (int i = Begin_30; i <= Begin_30 + Max_Day; i++) {
                FundPassSignActivityData.get(i).unlock = true;
            }
        } else if (id == 16) {
            for (int i = Begin_60; i <= Begin_60 + Max_Day; i++) {
                FundPassSignActivityData.get(i).unlock = true;
            }
        }
        ActivityMsg.FundPassSignBuyResponse_11000302.Builder builder = ActivityMsg.FundPassSignBuyResponse_11000302.newBuilder();
        FundPassSignActivityData.values().forEach(fundPassSignActivityData -> {
            builder.addInfo(ActivityMsg.FundPassSignInfo.newBuilder()
                    .setId(fundPassSignActivityData.Id)
                    .setDay(fundPassSignActivityData.day)
                    .setUnlock(fundPassSignActivityData.unlock?1:0)
                    .setFinish(fundPassSignActivityData.finish?1:0)
                    .setReward(fundPassSignActivityData.reward?1:0)
                    .build());
        });

        return builder.build();
    }

    @Override
    public Message buildActivityShowInfo() {
        ActivityMsg.FundPassSignAllResponse_11000304.Builder builder = ActivityMsg.FundPassSignAllResponse_11000304.newBuilder();
        FundPassSignActivityData.values().forEach(fundPassSignActivityData -> {
            builder.addInfo(ActivityMsg.FundPassSignInfo.newBuilder()
                    .setId(fundPassSignActivityData.Id)
                    .setDay(fundPassSignActivityData.day)
                    .setUnlock(fundPassSignActivityData.unlock?1:0)
                    .setFinish(fundPassSignActivityData.finish?1:0)
                    .setReward(fundPassSignActivityData.reward?1:0)
                    .build());
        });

        return builder.build();
    }

    @Override
    public void handleEvent(PlayerEvent event) {

    }

    @Override
    public boolean newDay() {
        loginday++;
        if (FundPassSignActivityData.containsKey(Begin_30 + loginday)) {
            var fundPassSignActivityData = FundPassSignActivityData.get(Begin_30 + loginday);
            fundPassSignActivityData.finish = true;
        }
        if (FundPassSignActivityData.containsKey(Begin_Free + loginday)) {
            var fundPassSignActivityData = FundPassSignActivityData.get(Begin_Free + loginday);
            fundPassSignActivityData.finish = true;
        }
        if (FundPassSignActivityData.containsKey(Begin_60 + loginday)) {
            var fundPassSignActivityData = FundPassSignActivityData.get(Begin_60 + loginday);
            fundPassSignActivityData.finish = true;
        }
        return true;
    }

    @Override
    public List<RewardInfo> receive(int taskId) {
        if (FundPassSignActivityData.containsKey(taskId)) {
            var fundPassSignActivityData = FundPassSignActivityData.get(taskId);
            if (fundPassSignActivityData.unlock && fundPassSignActivityData.finish && !fundPassSignActivityData.reward) {
                fundPassSignActivityData.reward = true;
                FundPassRewardsConfig fundPassRewardsConfig = FundPassRewardsManager.instance().get(taskId);
                if (fundPassRewardsConfig != null) {
                    return PlayerHelper.addResources(player, fundPassRewardsConfig.Reward, OpType.FundPassSignActivity);
                }
            }
        }
        return null;
    }


    @Override
    public EventTypeEnum[] getEventTypes() {
        return events;
    }


}
