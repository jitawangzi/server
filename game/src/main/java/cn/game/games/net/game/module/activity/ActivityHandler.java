package cn.game.games.net.game.module.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.springframework.stereotype.Component;
import com.mysql.cj.x.protobuf.MysqlxNotice.ServerHello;
import cn.game.core.net.client.NetClient;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.common.module.activity.AbstractActivityManager;
import cn.game.games.net.common.module.activity.GameActivityService;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.activity.impl.player.ActivityJQB;
import cn.game.games.net.game.module.activity.impl.player.ActivityLeiChong;
import cn.game.games.net.game.module.activity.impl.player.ActivityMeiRiBaoLi;
import cn.game.games.net.game.module.activity.impl.player.ActivityQingShen;
import cn.game.games.net.game.module.activity.impl.player.ActivityWestLucky;
import cn.game.games.net.game.module.activity.impl.player.DayGiftActivity;
import cn.game.games.net.game.module.activity.impl.player.FirstChargeActivity;
import cn.game.games.net.game.module.activity.impl.player.ServerOpenRankPlayerActivity;
import cn.game.games.net.game.module.activity.impl.player.SevenDayCarnivalActivity;
import cn.game.games.net.game.module.activity.impl.player.SevenDaysSignin;
import cn.game.games.net.game.module.rank.RankHelper;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.games.net.game.module.recharge.PayType;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.config.ActivityMeiRiTeHuiConfig;
import cn.game.protocol.generated.config.ActivityQingShenConfig;
import cn.game.protocol.generated.config.ActivityServerOpenRankConfig;
import cn.game.protocol.generated.config.ActivityWestLuckyPackConfig;
import cn.game.protocol.generated.config.ActivityWestLuckyProgressConfig;
import cn.game.protocol.generated.config.ActivityWestLuckyTurntableConfig;
import cn.game.protocol.generated.config.FirstChargeConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.SevenDaysSigninConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.generated.manager.ActivityMeiRiTeHuiManager;
import cn.game.protocol.generated.manager.ActivityServerOpenRankManager;
import cn.game.protocol.generated.manager.ActivityWestLuckyPackManager;
import cn.game.protocol.generated.manager.ActivityWestLuckyProgressManager;
import cn.game.protocol.generated.manager.ActivityWestLuckyTurntableManager;
import cn.game.protocol.generated.manager.FirstChargeManager;
import cn.game.protocol.generated.manager.SevenDaysSigninManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.ActivityMsg;
import cn.game.protocol.protobuf.ActivityMsg.ActivityDayGiftBuyRequest_11000102;
import cn.game.protocol.protobuf.ActivityMsg.ActivityDayGiftBuyResponse_11000103;
import cn.game.protocol.protobuf.ActivityMsg.ActivityDayGiftRequest_11000100;
import cn.game.protocol.protobuf.ActivityMsg.ActivityDayGiftResponse_11000101;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeBuyRequest_11000010;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeBuyResponse_11000011;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRequest_11000007;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeResponse_11000008;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardRequest_11000012;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardResponse_11000013;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.ActivityMsg.ActivityListResponse_11000002;
import cn.game.protocol.protobuf.ActivityMsg.ActivityRedPointRequest_11000003;
import cn.game.protocol.protobuf.ActivityMsg.ActivityRedPointResponse_11000004;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysCarnivalRequest_11000020;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysCarnivalResponse_11000021;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninInfoRequest_11000024;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninInfoResponse_11000025;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninRequest_11000026;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninResponse_11000027;
import cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyBuyRequest_11000095;
import cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyBuyResponse_11000096;
import cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyDrawRequest_11000093;
import cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyDrawResponse_11000094;
import cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyInfoRequest_11000091;
import cn.game.protocol.protobuf.RankMsg.RankInfo;
import cn.game.protocol.protobuf.RankMsg.RankListResponse_35000002;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.GameUtil;
import io.vertx.core.Future;
import cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankRequest_11000200;
import cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankResponse_11000201;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankListRequest_11000203;
import cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankListResponse_11000204;
import cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankRewardRequest_11000205;
import cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankRewardResponse_11000206;
import cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyCountRewardRequest_11000097;
import cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyCountRewardResponse_11000098;

/**
 * 活动处理器
 */
@Component
public class ActivityHandler extends GameBaseHandler {

    @Override
    protected int getModule() {
        return 0x11;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.ActivityListRequest_11000001, this::list);
        putInvoker(PbProtocol.ActivityFirstChargeRequest_11000007, this::singleCharge);
        putInvoker(PbProtocol.ActivityFirstChargeBuyRequest_11000010, this::singleChargeBuy);
        putInvoker(PbProtocol.ActivityFirstChargeRewardRequest_11000012, this::singleChargeReward);
        putInvoker(PbProtocol.ActivitySevenDaysCarnivalRequest_11000020, this::sevenDaysCarnival);
        putInvoker(PbProtocol.ActivitySevenDaysSigninInfoRequest_11000024, this::sevenDaysSigninInfo);
        putInvoker(PbProtocol.ActivitySevenDaysSigninRequest_11000026, this::sevenDaysSignin);
        putInvoker(PbProtocol.ActivityLeiChongInfoRequest_11000051, this::getLeiChongInfo);
        putInvoker(PbProtocol.ActivityTaskRewardRequest_11000041, this::rewardActivityTask);
        putInvoker(PbProtocol.ActivityBaoLiInfoRequest_11000061, this::getBaoLiInfo);
        putInvoker(PbProtocol.ActivityQingShenInfoRequest_11000071, this::getQingShenInfo);
        putInvoker(PbProtocol.ActivityJQBInfoRequest_11000081, this::getJQBInfo);
        putInvoker(PbProtocol.ActivityRedPointRequest_11000003, this::redPoint);
        putInvoker(PbProtocol.ActivityWestLuckyInfoRequest_11000091, this::westLuckyInfo);
        putInvoker(PbProtocol.ActivityWestLuckyDrawRequest_11000093, this::westLuckyDraw);
        putInvoker(PbProtocol.ActivityWestLuckyBuyRequest_11000095, this::westLuckyBuy);
        putInvoker(PbProtocol.ActivityDayGiftRequest_11000100, this::dayGift);
        putInvoker(PbProtocol.ActivityDayGiftBuyRequest_11000102, this::dayGiftBuy);
        putInvoker(PbProtocol.ActivityServerOpenRankRequest_11000200, this::serverOpenRank);
        putInvoker(PbProtocol.ActivityServerOpenRankListRequest_11000203, this::serverOpenRankList);
        putInvoker(PbProtocol.ActivityServerOpenRankRewardRequest_11000205, this::serverOpenRankReward);
        putInvoker(PbProtocol.ActivityWestLuckyCountRewardRequest_11000097, this::westLuckyCountReward);
    }

    private void empty(NetClient client, Object message) {
        ActivityFirstChargeBuyRequest_11000010 req = (ActivityFirstChargeBuyRequest_11000010) message;
        ActivityFirstChargeRewardResponse_11000013.Builder resp = ActivityFirstChargeRewardResponse_11000013.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        FirstChargeActivity activityBase = (FirstChargeActivity) player.getActivityModule().get(req.getId());
        if (activityBase == null) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
            return;
        }
        client.sendProtocol(resp);
    }

    private void sevenDaysSigninInfo(NetClient client, Object message) {
        ActivitySevenDaysSigninInfoRequest_11000024 req = (ActivitySevenDaysSigninInfoRequest_11000024) message;
        ActivitySevenDaysSigninInfoResponse_11000025 resp = ActivitySevenDaysSigninInfoResponse_11000025.getDefaultInstance();
        int id = req.getId();
        ActivityConfig activityConfig = ActivityManager.instance().getNullable(id);
        if (activityConfig == null) {
            client.sendProtocol(resp, ErrorMsgEnum.config_data_not_found.getId());
            return;
        }
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        SevenDaysSignin activityBase = (SevenDaysSignin) player.getActivityModule().get(id);
        if (activityBase == null) {
            client.sendProtocol(resp, ErrorMsgEnum.request_parameter_error.getId());
            return;
        }
        client.sendProtocol(activityBase.buildActivityShowInfo());
    }

    private void sevenDaysSignin(NetClient client, Object message) {
        ActivitySevenDaysSigninRequest_11000026 req = (ActivitySevenDaysSigninRequest_11000026) message;
        ActivitySevenDaysSigninResponse_11000027.Builder resp = ActivitySevenDaysSigninResponse_11000027.newBuilder();
        int id = req.getId();
        int extRewardId = req.getExtRewardId();
        ActivityConfig activityConfig = ActivityManager.instance().getNullable(id);
        if (activityConfig == null) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.config_data_not_found.getId());
            return;
        }
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        SevenDaysSignin activityBase = (SevenDaysSignin) player.getActivityModule().get(id);
        if (activityBase == null) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
            return;
        }
        if (extRewardId > 0) {
            // 领取 额外签到奖励
            SevenDaysSigninConfig config = SevenDaysSigninManager.instance().getNullable(extRewardId);
            if (config == null) {
                client.sendProtocol(resp.build(), ErrorMsgEnum.config_data_not_found.getId());
                return;
            }
            resp.addAllRewards(activityBase.rewardExtra(config));
        } else {
            if (activityBase.isSignin()) {
                client.sendProtocol(resp.build(), ErrorMsgEnum.repeat_request.getId());
                return;
            }
            resp.addAllRewards(activityBase.receive(0));
        }
        client.sendProtocol(resp.build());
    }

    private void sevenDaysCarnival(NetClient client, Object message) {
        ActivitySevenDaysCarnivalRequest_11000020 req = (ActivitySevenDaysCarnivalRequest_11000020) message;
        ActivitySevenDaysCarnivalResponse_11000021.Builder resp = ActivitySevenDaysCarnivalResponse_11000021.newBuilder();
        int id = req.getId();
        ActivityConfig activityConfig = ActivityManager.instance().getNullable(id);
        if (activityConfig == null) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.config_data_not_found.getId());
            return;
        }
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        SevenDayCarnivalActivity activityBase = (SevenDayCarnivalActivity) player.getActivityModule().get(id);
        if (activityBase == null) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
            return;
        }
        client.sendProtocol(activityBase.buildActivityShowInfo());
    }

    private void singleCharge(NetClient client, Object message) {
        ActivityFirstChargeRequest_11000007 req = (ActivityFirstChargeRequest_11000007) message;
        ActivityFirstChargeResponse_11000008.Builder resp = ActivityFirstChargeResponse_11000008.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        int id = req.getId();
        FirstChargeActivity activityBase = (FirstChargeActivity) player.getActivityModule().get(id);
        if (activityBase == null) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
            return;
        }
        client.sendProtocol(activityBase.buildActivityShowInfo(id));
    }

    private void singleChargeBuy(NetClient client, Object message) {
        ActivityFirstChargeBuyRequest_11000010 req = (ActivityFirstChargeBuyRequest_11000010) message;
        ActivityFirstChargeBuyResponse_11000011.Builder resp = ActivityFirstChargeBuyResponse_11000011.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        int id = req.getId();
        int chargeId = req.getChargeId();
        FirstChargeActivity activityBase = (FirstChargeActivity) player.getActivityModule().get(id);
        if (activityBase == null) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
            return;
        }
        if (!activityBase.check(chargeId)) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
            return;
        }
        FirstChargeConfig firstChargeConfig = FirstChargeManager.instance().get(chargeId);
        Future<Boolean> pay = player.pay(PayType.FirstCharge, chargeId, firstChargeConfig.Price, id);
        pay.onComplete(t -> {
            if (t.result()) {
                activityBase.buy(chargeId);
                client.sendProtocol(resp.build());
            } else {
                client.sendProtocol(resp, ErrorMsgEnum.unknown.getId());
            }
        });
    }

    private void singleChargeReward(NetClient client, Object message) {
        ActivityFirstChargeRewardRequest_11000012 req = (ActivityFirstChargeRewardRequest_11000012) message;
        ActivityFirstChargeRewardResponse_11000013.Builder resp = ActivityFirstChargeRewardResponse_11000013.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        int chargeId = req.getChargeId();
        FirstChargeActivity activityBase = (FirstChargeActivity) player.getActivityModule().get(req.getId());
        if (activityBase == null) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
            return;
        }
        // SingleCharge singleCharge = activityBase.getSingleCharge(chargeId);
        // if (singleCharge == null) {
        // client.sendProtocol(resp.build(),
        // ErrorMsgEnum.request_parameter_error.getId());
        // return;
        // }
        // if (rewardDay > DateUtil.getDay() - singleCharge.getDay()) {
        // client.sendProtocol(resp.build(),
        // ErrorMsgEnum.request_parameter_error.getId());
        // return;
        // }
        // if (singleCharge.getSelectedIndex().contains(rewardDay)) {
        // client.sendProtocol(resp.build(),
        // ErrorMsgEnum.request_parameter_error.getId());
        // return;
        // }
        List<RewardInfo> reward = activityBase.reward(chargeId);
        if (reward == null) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
            return;
        }
        resp.addAllRewards(reward);
        client.sendProtocol(resp);
    }

    private void list(NetClient client, Object message) {
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        ActivityListResponse_11000002.Builder resp = ActivityListResponse_11000002.newBuilder();
        // 按时间开启的活动，全服的
        Collection<ActivityInfo> activityInfos = ActivityStateManager.getInstance().getShowState();
        // 玩家所在服务器的活动
        AbstractActivityManager serverActivityManager = GameActivityService.getInstance().getServerActivityManager(player.getServerId());
        Map<Integer, ActivityInfo> serverActivityShowState = serverActivityManager == null ? null : serverActivityManager.getShowState();
        // 个人活动
        Map<Integer, ActivityInfo> playerState = player.getActivityModule().getShowState();
        for (ActivityInfo activityInfo : activityInfos) {
            if (!playerState.containsKey(activityInfo.getId())) {
                playerState.put(activityInfo.getId(), activityInfo);
            }
        }
        if (serverActivityShowState != null) {
            for (ActivityInfo activityInfo : serverActivityShowState.values()) {
                if (!playerState.containsKey(activityInfo.getId())) {
                    playerState.put(activityInfo.getId(), activityInfo);
                }
            }
        }
        Iterator<Entry<Integer, ActivityInfo>> iterator = playerState.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<java.lang.Integer, cn.game.protocol.protobuf.ActivityMsg.ActivityInfo> entry = (Map.Entry<java.lang.Integer, cn.game.protocol.protobuf.ActivityMsg.ActivityInfo>) iterator.next();
            ActivityConfig activityConfig = ActivityManager.instance().get(entry.getKey());
            if (activityConfig.isServerOnly) {
                iterator.remove();
            }
        }
        resp.addAllActivitys(playerState.values());
        client.sendProtocol(resp);
    }

    private void getLeiChongInfo(NetClient client, Object o) {
        ActivityMsg.ActivityLeiChongInfoRequest_11000051 req = (ActivityMsg.ActivityLeiChongInfoRequest_11000051) o;
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        ActivityMsg.ActivityLeiChongInfoResponse_11000052.Builder res = ActivityMsg.ActivityLeiChongInfoResponse_11000052.newBuilder();
        ActivityModule activityModule = player.getActivityModule();
        if (activityModule.get(req.getActivityId()) == null) {
            client.sendProtocol(res, ErrorMsgEnum.activity_not_found.getId());
            return;
        }
        ActivityLeiChong leiChong = (ActivityLeiChong) activityModule.get(req.getActivityId());
        client.sendProtocol(leiChong.buildActivityShowInfo());
    }

    private void rewardActivityTask(NetClient client, Object o) {
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        ActivityMsg.ActivityTaskRewardRequest_11000041 req = (ActivityMsg.ActivityTaskRewardRequest_11000041) o;
        ActivityMsg.ActivityTaskRewardResponse_11000042.Builder res = ActivityMsg.ActivityTaskRewardResponse_11000042.newBuilder();
        ActivityModule activityModule = player.getActivityModule();
        if (activityModule.get(req.getActivityId()) == null) {
            client.sendProtocol(res, ErrorMsgEnum.activity_not_found.getId());
            return;
        }
        ActivityBase activityBase = activityModule.get(req.getActivityId());
        int checkCode = activityBase.canReceive(req.getTaskIdsList());
        if (checkCode != ErrorMsgEnum.ok.ID) {
            client.sendProtocol(res, checkCode);
            return;
        }
        if (activityBase instanceof ActivityJQB) {
            List<CompletableFuture<List<RewardInfo>>> allFuture = new ArrayList<>();
            int taskId = req.getTaskIds(0);
            activityBase.asyncReceive(taskId).onSuccess(reward -> {
                res.addAllRewards((Iterable<? extends RewardInfo>) reward);
                activityBase.checkRefreshActivity();
                client.sendProtocol(res.build());
            });
        } else {
            // 请神任务 每轮最后一个任务完成之后 自动领取每轮回奖励的任务
            List<Integer> rewardTaskIds = new ArrayList<>(req.getTaskIdsList());
            if (activityBase instanceof ActivityQingShen activityQingShen) {
                List<ActivityQingShenConfig> roundConfigList = activityQingShen.getRoundConfigList(activityQingShen.getRound());
                List<Integer> roundIds = new ArrayList<>();
                roundConfigList.forEach(roundConfig -> roundIds.add(roundConfig.taskID));
                if (rewardTaskIds.contains(roundConfigList.get(roundConfigList.size() - 2).taskID) && !rewardTaskIds.contains(roundConfigList.get(roundConfigList.size() - 1).taskID)) {
                    rewardTaskIds.add(roundConfigList.get(roundConfigList.size() - 1).taskID);
                }
                rewardTaskIds.removeIf(taskId -> !roundIds.contains(taskId));
                Collections.sort(rewardTaskIds);
            }
            log.info(String.format("ActivityQingShen rewardTaskIds:%s", rewardTaskIds));
            rewardTaskIds.forEach(taskId -> {
                List<RewardInfo> reward = activityBase.receive(taskId);
                if (reward == null) {
                    client.sendProtocol(res, ErrorMsgEnum.request_parameter_error.getId());
                    return;
                }
                res.addAllRewards(reward);
            });
            activityBase.checkRefreshActivity();
            client.sendProtocol(res.build());
        }
    }

    private void getBaoLiInfo(NetClient client, Object o) {
        ActivityMsg.ActivityBaoLiInfoRequest_11000061 req = (ActivityMsg.ActivityBaoLiInfoRequest_11000061) o;
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        ActivityMsg.ActivityBaoLiInfoResponse_11000062.Builder res = ActivityMsg.ActivityBaoLiInfoResponse_11000062.newBuilder();
        ActivityModule activityModule = player.getActivityModule();
        if (activityModule.get(req.getActivityId()) == null) {
            client.sendProtocol(res, ErrorMsgEnum.activity_not_found.getId());
            return;
        }
        ActivityMeiRiBaoLi baoLi = (ActivityMeiRiBaoLi) activityModule.get(req.getActivityId());
        client.sendProtocol(baoLi.buildActivityShowInfo());
    }

    private void getQingShenInfo(NetClient client, Object o) {
        ActivityMsg.ActivityQingShenInfoRequest_11000071 req = (ActivityMsg.ActivityQingShenInfoRequest_11000071) o;
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        ActivityMsg.ActivityQingShenInfoResponse_11000072.Builder res = ActivityMsg.ActivityQingShenInfoResponse_11000072.newBuilder();
        ActivityModule activityModule = player.getActivityModule();
        if (activityModule.get(req.getActivityId()) == null) {
            client.sendProtocol(res, ErrorMsgEnum.activity_not_found.getId());
            return;
        }
        ActivityQingShen qingShen = (ActivityQingShen) activityModule.get(req.getActivityId());
        client.sendProtocol(qingShen.buildActivityShowInfo());
    }

    private void getJQBInfo(NetClient client, Object o) {
        ActivityMsg.ActivityJQBInfoRequest_11000081 req = (ActivityMsg.ActivityJQBInfoRequest_11000081) o;
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        ActivityMsg.ActivityJQBInfoResponse_11000082.Builder res = ActivityMsg.ActivityJQBInfoResponse_11000082.newBuilder();
        ActivityModule activityModule = player.getActivityModule();
        if (activityModule.get(req.getActivityId()) == null) {
            client.sendProtocol(res, ErrorMsgEnum.activity_not_found.getId());
            return;
        }
        ActivityJQB activityJQB = (ActivityJQB) activityModule.get(req.getActivityId());
        client.sendProtocol(activityJQB.buildActivityShowInfo());
    }

    private void redPoint(NetClient client, Object message) {
        ActivityRedPointRequest_11000003 req = (ActivityRedPointRequest_11000003) message;
        List<Integer> idsList = req.getIdsList();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        ActivityRedPointResponse_11000004.Builder resp = ActivityRedPointResponse_11000004.newBuilder();
        List<Boolean> redList = new ArrayList<Boolean>();
        for (Integer id : idsList) {
            ActivityBase activityBase = player.getActivityModule().get(id);
            if (activityBase != null && activityBase.hasRed()) {
                redList.add(true);
            } else {
                redList.add(false);
            }
        }
        resp.addAllIsRed(redList);
        client.sendProtocol(resp.build());
    }

    private void westLuckyInfo(NetClient client, Object message) {
        ActivityWestLuckyInfoRequest_11000091 req = (ActivityWestLuckyInfoRequest_11000091) message;
        int activityId = req.getActivityId();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        ActivityWestLucky activityWestLucky = (ActivityWestLucky) player.getActivityModule().get(activityId);
        ActivityMsg.ActivityWestLuckyInfoResponse_11000092.Builder res = ActivityMsg.ActivityWestLuckyInfoResponse_11000092.newBuilder();
        if (activityWestLucky == null) {
            client.sendProtocol(res, ErrorMsgEnum.activity_not_found.getId());
            return;
        }
        client.sendProtocol(player.getActivityModule().get(activityId).buildActivityShowInfo());
    }

    private void westLuckyDraw(NetClient client, Object message) {
        ActivityWestLuckyDrawRequest_11000093 req = (ActivityWestLuckyDrawRequest_11000093) message;
        int activityId = req.getActivityId();
        int drawNum = req.getDrawNum();
        ActivityWestLuckyDrawResponse_11000094.Builder resp = ActivityWestLuckyDrawResponse_11000094.newBuilder();
        resp.setActivityId(activityId);
        resp.setDrawNum(drawNum);
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (drawNum != 1 && drawNum != 10) {
            client.sendProtocol(resp, ErrorMsgEnum.request_parameter_error.getId());
            return;
        }
        ActivityWestLucky activityWestLucky = (ActivityWestLucky) player.getActivityModule().get(activityId);
        if (activityWestLucky == null) {
            client.sendProtocol(resp, ErrorMsgEnum.activity_not_found.getId());
            return;
        }
        if (activityWestLucky.getTotalNum() + drawNum > GlobalConst.ActivityWestLuckyDayCount) {
            client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
            return;
		}
        int[] cost = activityWestLucky.getDrawItemId();
        if (drawNum > 1) {
            cost = GameUtil.arrayMultiple(cost, drawNum);
        }
        player.pay(cost, OpType.ZhuanPanDraw);
        GameLogger.activity(player, activityId, 0);
        for (int i = 0; i < drawNum; i++) {
            activityWestLucky.addDrawNum();
            List<Integer> ids = activityWestLucky.draw(false, new ArrayList<>());
            ids.forEach(id -> {
                ActivityWestLuckyTurntableConfig config = ActivityWestLuckyTurntableManager.instance().get(id);
                if (config.CircleType == 1 || config.CircleType == 2) {
                    resp.addAllDrops(PlayerHelper.addResources(player, config.Reward, OpType.ZhuanPanDraw));
                    if (config.AdditionalRewards.length > 0) {
                        resp.addAllDrops(PlayerHelper.addResources(player, config.AdditionalRewards, OpType.ZhuanPanDraw));
                    }
                }
            });
            resp.addAllDrawIds(ids);
            resp.setOutDrawNum(activityWestLucky.getTotalNum());
        }
        client.sendProtocol(resp.build());
    }

    private void westLuckyBuy(NetClient client, Object message) {
        ActivityWestLuckyBuyRequest_11000095 req = (ActivityWestLuckyBuyRequest_11000095) message;
        int activityId = req.getActivityId();
        int id = req.getId();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        ActivityWestLuckyBuyResponse_11000096.Builder resp = ActivityWestLuckyBuyResponse_11000096.newBuilder();
        resp.setActivityId(activityId);
        resp.setId(id);
        ActivityWestLucky activityWestLucky = (ActivityWestLucky) player.getActivityModule().get(activityId);
        if (activityWestLucky == null) {
            client.sendProtocol(resp, ErrorMsgEnum.activity_not_found.getId());
            return;
        }
        ActivityWestLuckyPackConfig config = ActivityWestLuckyPackManager.instance().getNullable(id);
        if (config == null) {
            client.sendProtocol(resp, ErrorMsgEnum.config_data_not_found.getId());
            return;
        }
        int buyNum = activityWestLucky.getBuyIdMap().getOrDefault(id, 0);
        if (buyNum >= config.Quota) {
            client.sendProtocol(resp, ErrorMsgEnum.buy_over_limit.getId());
            return;
        }
        player.pay(PayType.FirstCharge, id, config.PurchaseParameter, activityId).onSuccess(t -> {
            if (t) {
                GameLogger.activity(player, activityId, id);
                resp.addAllDrops(PlayerHelper.addResources(player, config.Item, OpType.ZhuanPanItemBuy));
                activityWestLucky.getBuyIdMap().put(id, buyNum + 1);
                client.sendProtocol(resp.build());
            } else {
                client.sendProtocol(resp, ErrorMsgEnum.shop_item_not_exist.getId());
            }
        }).onFailure(err -> {
            err.printStackTrace();
            client.sendProtocol(resp, ErrorMsgEnum.unknown.getId());
        });
    }

    private void dayGift(NetClient client, Object message) {
        ActivityDayGiftRequest_11000100 req = (ActivityDayGiftRequest_11000100) message;
        int id = req.getId();
        ActivityDayGiftResponse_11000101 defaultInstance = ActivityDayGiftResponse_11000101.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        DayGiftActivity activity = (DayGiftActivity) player.getActivityModule().get(id);
        if (activity == null) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.activity_not_found.getId());
            return;
        }
        client.sendProtocol(activity.buildActivityShowInfo());
    }

    private void dayGiftBuy(NetClient client, Object message) {
        ActivityDayGiftBuyRequest_11000102 req = (ActivityDayGiftBuyRequest_11000102) message;
        int id = req.getId();
        int giftId = req.getGiftId();
        ActivityDayGiftBuyResponse_11000103 defaultInstance = ActivityDayGiftBuyResponse_11000103.getDefaultInstance();
        ActivityDayGiftBuyResponse_11000103.Builder resp = ActivityDayGiftBuyResponse_11000103.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        DayGiftActivity activity = (DayGiftActivity) player.getActivityModule().get(id);
        if (activity == null) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.activity_not_found.getId());
            return;
        }
        ActivityMeiRiTeHuiConfig meiRiTeHuiConfig = ActivityMeiRiTeHuiManager.instance().get(giftId);
        if (activity.getBuyCount(giftId) >= meiRiTeHuiConfig.Quota) {
            player.fail(ErrorMsgEnum.times_limit);
        }
        Future<Boolean> pay = player.pay(PayType.DayGift, giftId, meiRiTeHuiConfig.PurchaseParameter, id);
        pay.map(r -> {
            List<RewardInfo> rewardInfos;
            if (giftId == 99) {
                rewardInfos = activity.packageBuy();
            } else {
                rewardInfos = activity.buy(giftId);
            }
            resp.addAllRewards(rewardInfos);
            client.sendProtocol(resp.build());
            GameLogger.activity(player, id, giftId);
            return null;
        }).onFailure(player::handleFail);
    }

    private void serverOpenRank(NetClient client, Object message) {
        ActivityServerOpenRankRequest_11000200 req = (ActivityServerOpenRankRequest_11000200) message;
        int id = req.getId();
        ActivityServerOpenRankResponse_11000201 defaultInstance = ActivityServerOpenRankResponse_11000201.getDefaultInstance();
        ActivityServerOpenRankResponse_11000201.Builder resp = ActivityServerOpenRankResponse_11000201.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        ServerOpenRankPlayerActivity activity = player.getActivityModule().get(id);
        if (activity == null) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.activity_not_found.getId());
            return;
        }
        client.sendProtocol(activity.buildActivityShowInfo());
    }

    private void serverOpenRankList(NetClient client, Object message) {
        ActivityServerOpenRankListRequest_11000203 req = (ActivityServerOpenRankListRequest_11000203) message;
        int type = req.getType();
        int page = req.getPage();
        int pageSize = req.getPageSize();
        ActivityServerOpenRankListResponse_11000204 defaultInstance = ActivityServerOpenRankListResponse_11000204.getDefaultInstance();
        ActivityServerOpenRankListResponse_11000204.Builder resp = ActivityServerOpenRankListResponse_11000204.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (pageSize > 100) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_parameter_error.ID);
            return;
        }
        int serverOpenDay = ServerHelper.getServerOpenDay(player.getServerId());
        ActivityServerOpenRankConfig curConfig = ActivityServerOpenRankManager.instance().get(serverOpenDay);
        ActivityServerOpenRankConfig targetConfig = null;
        Collection<ActivityServerOpenRankConfig> list = ActivityServerOpenRankManager.instance().list();
        for (ActivityServerOpenRankConfig activityServerOpenRankConfig2 : list) {
            if (activityServerOpenRankConfig2.RankID == type) {
                targetConfig = activityServerOpenRankConfig2;
                break;
            }
        }
        
        RankType rankType = null;
        if (type == RankType.TotalServerOpenActivity.ID) {
        	rankType = RankType.TotalServerOpenActivity;
        }else {
        	if (curConfig == targetConfig) {
        		rankType = RankType.get(targetConfig.RankID);
        	} else {
        		rankType = RankType.get(targetConfig.RewardRankId);
        	}
        }
        CompletionStage<RankInfo> rankInfo = RankHelper.getRankInfo(player, rankType, page, pageSize);
        rankInfo.thenAccept(r -> {
            resp.setRankInfo(r);
            client.sendProtocol(resp.build());
        }).exceptionally(player::handleFailFunction);
    }

    private void serverOpenRankReward(NetClient client, Object message) {
        ActivityServerOpenRankRewardRequest_11000205 req = (ActivityServerOpenRankRewardRequest_11000205) message;
        ActivityServerOpenRankRewardResponse_11000206 defaultInstance = ActivityServerOpenRankRewardResponse_11000206.getDefaultInstance();
        ActivityServerOpenRankRewardResponse_11000206.Builder resp = ActivityServerOpenRankRewardResponse_11000206.newBuilder();
        int id = req.getId();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        ServerOpenRankPlayerActivity activity = player.getActivityModule().get(id);
        if (activity == null) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.activity_not_found.getId());
            return;
        }
        List<RewardInfo> receive = activity.receive(0);
        resp.addAllRewards(receive);
        client.sendProtocol(resp.build());
    }

    private void westLuckyCountReward(NetClient client, Object message) {
        ActivityWestLuckyCountRewardRequest_11000097 req = (ActivityWestLuckyCountRewardRequest_11000097) message;
        List<Integer> rewardIndexList = req.getRewardIndexList();
        int activityId = req.getActivityId(); 
        ActivityWestLuckyCountRewardResponse_11000098 defaultInstance = ActivityWestLuckyCountRewardResponse_11000098.getDefaultInstance();
        ActivityWestLuckyCountRewardResponse_11000098.Builder resp = ActivityWestLuckyCountRewardResponse_11000098.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        
        ActivityWestLucky activityWestLucky = (ActivityWestLucky) player.getActivityModule().get(activityId);
        if (activityWestLucky == null) {
            client.sendProtocol(resp, ErrorMsgEnum.activity_not_found.getId());
            return;
        }
        List<Integer> rewardIndexList2 = activityWestLucky.getRewardIndexList(); 
        for (Integer integer : rewardIndexList) {
			if (rewardIndexList2.contains(integer)) {
				client.sendProtocol(resp, ErrorMsgEnum.repeat_request.getId());
				return;
			}
		}
        ActivityWestLuckyProgressConfig activityWestLuckyProgressConfig = ActivityWestLuckyProgressManager.instance().get(activityId); 
        int totalNum = activityWestLucky.getTotalNum(); 
        for (Integer integer : rewardIndexList) {
        	if (activityWestLuckyProgressConfig.count[integer] > totalNum) {
        		client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
        		return;
        	}
		}
        for (int i = 0; i < rewardIndexList.size(); i++) {
        	List<RewardInfo> resources = PlayerHelper.addResources(player, activityWestLuckyProgressConfig.reward[i], OpType.ZhuanPanCountReward);
        	resp.addAllReward(resources); 
		}
        rewardIndexList2.addAll(rewardIndexList); 
        client.sendProtocol(resp.build());
    }
}
