package cn.game.games.net.game.module.activity;

import cn.game.games.net.game.module.recharge.PaymentCompletion;
import cn.game.games.net.game.helper.PlayerWriteGuard;
import cn.game.games.net.game.module.recharge.PaymentReply;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import cn.game.games.net.game.module.activity.impl.player.*;
import cn.game.protocol.generated.config.*;
import cn.game.protocol.generated.manager.*;
import org.springframework.stereotype.Component;
import com.mysql.cj.x.protobuf.MysqlxNotice.ServerHello;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.core.net.client.NetClient;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.common.module.activity.AbstractActivityManager;
import cn.game.games.net.common.module.activity.DefaultActivityScopeResolver;
import cn.game.games.net.common.module.activity.GameActivityService;
import cn.game.games.net.cross.activity.CrossActivityRouteMissingException;
import cn.game.games.net.cross.activity.dto.CrossActivityActionResult;
import cn.game.games.net.cross.activity.dto.CrossGuildWarPayloads;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.AttackRequest;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.AttackResponse;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.ClaimKillRewardRequest;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.ClaimMilestoneRequest;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.ClaimMilestoneResponse;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.QueryDetailRequest;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.QueryDetailResponse;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.QueryRankRequest;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.QueryRankResponse;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.RankEntryPayload;
import cn.game.games.net.cross.activity.impl.CrossGuildWarActivity;
import cn.game.games.net.cross.activity.impl.CrossWorldBossActivity;
import cn.game.games.net.cross.activity.service.CrossActivityServiceInterface;
import cn.game.games.net.cross.guild.SimpleGuild;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.rank.RankHelper;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.games.net.game.module.recharge.PayType;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.ActivityMsg;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.protocol.protobuf.ActivityMsg.CrossGuildWarGuildRankEntry;
import cn.game.protocol.protobuf.ActivityMsg.CrossGuildWarMemberRankEntry;
import cn.game.protocol.protobuf.ActivityMsg.CrossGuildWarStrongholdInfo;
import cn.game.protocol.protobuf.ActivityMsg.CrossWorldBossRankEntry;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;
import com.google.protobuf.Message;
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
import cn.game.protocol.protobuf.ActivityMsg.FundPassSignBuyRequest_11000301;
import cn.game.protocol.protobuf.ActivityMsg.FundPassSignBuyResponse_11000302;
import cn.game.protocol.protobuf.ActivityMsg.FundPassSignAllRequest_11000303;
import cn.game.protocol.protobuf.ActivityMsg.FundPassSignAllResponse_11000304;
import cn.game.protocol.protobuf.ActivityMsg.FundPassSignReceiveRequest_11000305;
import cn.game.protocol.protobuf.ActivityMsg.FundPassSignReceiveResponse_11000306;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossInfoRequest_11000401;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossInfoResponse_11000402;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossAttackRequest_11000403;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossAttackResponse_11000404;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossRewardRequest_11000405;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossRewardResponse_11000406;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossBuyCountRequest_11000407;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossBuyCountResponse_11000408;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossRankRequest_11000409;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossRankResponse_1100040a;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarInfoRequest_11000501;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarInfoResponse_11000502;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarAttackRequest_11000503;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarAttackResponse_11000504;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarRewardRequest_11000505;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarRewardResponse_11000506;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarBuyCountRequest_11000507;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarBuyCountResponse_11000508;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarGuildRankRequest_11000509;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarGuildRankResponse_1100050a;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarMemberRankRequest_1100050b;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarMemberRankResponse_1100050c;

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
        putInvoker(PbProtocol.FundPassSignBuyRequest_11000301, this::fundPassSignBuy);
        putInvoker(PbProtocol.FundPassSignAllRequest_11000303, this::fundPassSignAll);
        putInvoker(PbProtocol.FundPassSignReceiveRequest_11000305, this::fundPassSignReceive);
        putInvoker(PbProtocol.ActivityCrossWorldBossInfoRequest_11000401, this::crossWorldBossInfo);
        putInvoker(PbProtocol.ActivityCrossWorldBossAttackRequest_11000403, this::crossWorldBossAttack);
        putInvoker(PbProtocol.ActivityCrossWorldBossRewardRequest_11000405, this::crossWorldBossReward);
        putInvoker(PbProtocol.ActivityCrossWorldBossBuyCountRequest_11000407, this::crossWorldBossBuyCount);
        putInvoker(PbProtocol.ActivityCrossWorldBossRankRequest_11000409, this::crossWorldBossRank);
        putInvoker(PbProtocol.ActivityCrossGuildWarInfoRequest_11000501, this::crossGuildWarInfo);
        putInvoker(PbProtocol.ActivityCrossGuildWarAttackRequest_11000503, this::crossGuildWarAttack);
        putInvoker(PbProtocol.ActivityCrossGuildWarRewardRequest_11000505, this::crossGuildWarReward);
        putInvoker(PbProtocol.ActivityCrossGuildWarBuyCountRequest_11000507, this::crossGuildWarBuyCount);
        putInvoker(PbProtocol.ActivityCrossGuildWarGuildRankRequest_11000509, this::crossGuildWarGuildRank);
        putInvoker(PbProtocol.ActivityCrossGuildWarMemberRankRequest_1100050b, this::crossGuildWarMemberRank);
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
        PaymentReply reply = new PaymentReply(client);
        Future<Boolean> pay = player.pay(PayType.FirstCharge, chargeId, firstChargeConfig.Price, id);
        PaymentCompletion.reward(player, pay, () -> {
            activityBase.buy(chargeId);
            return resp.build();
        }).onSuccess(reply::send).onFailure(error -> reply.fail(resp.getDefaultInstanceForType(), error, ErrorMsgEnum.unknown.getId()));
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

    public static Map<Integer, ActivityInfo> filterValidActivityShowState(Player player, Map<Integer, ActivityInfo> stateMap) {
        if (stateMap == null || stateMap.isEmpty()) {
            return stateMap;
        }
        Iterator<Entry<Integer, ActivityInfo>> iterator = stateMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ActivityInfo> entry = iterator.next();
            ActivityConfig activityConfig = ActivityManager.instance().getNullable(entry.getKey());
            if (activityConfig == null) {
                iterator.remove();
                continue;
            }
            if (activityConfig.isServerOnly && player.getActivityModule().get(entry.getKey()) == null) {
                iterator.remove();
            }
        }
        return stateMap;
    }

    public static Map<Integer, ActivityInfo> getMergedActivityShowState(Player player) {
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
        return filterValidActivityShowState(player, playerState);
    }

    private void list(NetClient client, Object message) {
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        ActivityListResponse_11000002.Builder resp = ActivityListResponse_11000002.newBuilder();
        Map<Integer, ActivityInfo> playerState = getMergedActivityShowState(player);
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
        if (!(activityBase instanceof PlayerActivityBase playerActivity)) {
            client.sendProtocol(res, ErrorMsgEnum.activity_not_found.getId());
            return;
        }
        int checkCode = playerActivity.canReceive(req.getTaskIdsList());
        if (checkCode != ErrorMsgEnum.ok.ID) {
            client.sendProtocol(res, checkCode);
            return;
        }
        if (playerActivity instanceof ActivityJQB) {
            List<CompletableFuture<List<RewardInfo>>> allFuture = new ArrayList<>();
            int taskId = req.getTaskIds(0);
            PaymentReply reply = new PaymentReply(client);
            playerActivity.asyncReceive(taskId).map(reward -> res.addAllRewards(reward).build()).onSuccess(reply::send).onFailure(error -> reply.fail(res.getDefaultInstanceForType(), error, ErrorMsgEnum.unknown.getId()));
        } else {
            // 请神任务 每轮最后一个任务完成之后 自动领取每轮回奖励的任务
            List<Integer> rewardTaskIds = new ArrayList<>(req.getTaskIdsList());
            if (playerActivity instanceof ActivityQingShen activityQingShen) {
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
                List<RewardInfo> reward = playerActivity.receive(taskId);
                if (reward == null) {
                    client.sendProtocol(res, ErrorMsgEnum.request_parameter_error.getId());
                    return;
                }
                res.addAllRewards(reward);
            });
            playerActivity.checkRefreshActivity();
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
            if (activityBase instanceof PlayerActivityBase playerActivity && playerActivity.hasRed()) {
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
        client.sendProtocol(activityWestLucky.buildActivityShowInfo());
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
        if (activityWestLucky.getTodayCount() + drawNum > GlobalConst.ActivityWestLuckyDayCount) {
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
        PaymentReply reply = new PaymentReply(client);
        Future<Boolean> pay = player.pay(PayType.WestLucky, id, config.PurchaseParameter, activityId);
        PaymentCompletion.reward(player, pay, () -> {
            GameLogger.activity(player, activityId, id);
            resp.addAllDrops(PlayerHelper.addResources(player, config.Item, OpType.ZhuanPanItemBuy));
            activityWestLucky.getBuyIdMap().put(id, buyNum + 1);
            return resp.build();
        }, ErrorMsgEnum.shop_item_not_exist).onSuccess(reply::send).onFailure(error -> reply.fail(resp.getDefaultInstanceForType(), error, ErrorMsgEnum.unknown.getId()));
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

    /**
     * @param client 原购买连接 @param message 每日礼包请求；支付成功前不推进购买和奖励
     */
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
        PaymentReply reply = new PaymentReply(client);
        Future<Boolean> pay = player.pay(PayType.DayGift, giftId, meiRiTeHuiConfig.PurchaseParameter, id);
        PaymentCompletion.reward(player, pay, () -> {
            List<RewardInfo> rewardInfos = giftId == 99 ? activity.packageBuy() : activity.buy(giftId);
            resp.addAllRewards(rewardInfos);
            GameLogger.activity(player, id, giftId);
            return resp.build();
        }).onSuccess(reply::send).onFailure(error -> reply.fail(defaultInstance, error, ErrorMsgEnum.unknown.getId()));
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
        ActivityServerOpenRankConfig curConfig = ActivityServerOpenRankManager.instance().getNullable(serverOpenDay);
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
        } else {
            if (curConfig == null) {
                // 在7天后封榜时间
                rankType = RankType.get(targetConfig.RewardRankId);
            } else {
                // 7天内的榜单活跃时间
                // 当前活跃排行榜
                if (curConfig == targetConfig) {
                    rankType = RankType.get(targetConfig.RankID);
                } else {
                    // 封榜
                    rankType = RankType.get(targetConfig.RewardRankId);
                }
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
            List<RewardInfo> resources = PlayerHelper.addResources(player, activityWestLuckyProgressConfig.reward[rewardIndexList.get(i)], OpType.ZhuanPanCountReward);
            resp.addAllReward(resources);
        }
        rewardIndexList2.addAll(rewardIndexList);
        client.sendProtocol(resp.build());
    }

    /**
     * @param client 原购买连接 @param message 基金签到购买请求；只在支付终态回复
     */
    private void fundPassSignBuy(NetClient client, Object message) {
        FundPassSignBuyRequest_11000301 req = (FundPassSignBuyRequest_11000301) message;
        int id = req.getId();
        FundPassSignBuyResponse_11000302 defaultInstance = FundPassSignBuyResponse_11000302.getDefaultInstance();
        FundPassSignBuyResponse_11000302.Builder resp = FundPassSignBuyResponse_11000302.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        int activityId = req.getActivityId();
        FundPassConfig fundPassConfig = FundPassManager.instance().get(id);
        PaymentReply reply = new PaymentReply(client);
        Future<Boolean> pay = player.pay(PayType.FundPassSignActivity, id, fundPassConfig.Price, activityId);
        PaymentCompletion.reward(player, pay, () -> {
            FundPassSignActivity activity = player.getActivityModule().get(activityId);
            if (activity == null) {
                player.fail(ErrorMsgEnum.activity_not_found);
            }
            return activity.buy(id);
        }).onSuccess(reply::send).onFailure(error -> reply.fail(defaultInstance, error, ErrorMsgEnum.unknown.getId()));
    }

    private void fundPassSignAll(NetClient client, Object message) {
        FundPassSignAllRequest_11000303 req = (FundPassSignAllRequest_11000303) message;
        FundPassSignAllResponse_11000304 defaultInstance = FundPassSignAllResponse_11000304.getDefaultInstance();
        FundPassSignAllResponse_11000304.Builder resp = FundPassSignAllResponse_11000304.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        int id = req.getActivityId();
        FundPassSignActivity activity = player.getActivityModule().get(id);
        if (activity == null) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.activity_not_found.getId());
            return;
        }
        client.sendProtocol(activity.buildActivityShowInfo());
    }

    private void fundPassSignReceive(NetClient client, Object message) {
        FundPassSignReceiveRequest_11000305 req = (FundPassSignReceiveRequest_11000305) message;
        FundPassSignReceiveResponse_11000306 defaultInstance = FundPassSignReceiveResponse_11000306.getDefaultInstance();
        FundPassSignReceiveResponse_11000306.Builder resp = FundPassSignReceiveResponse_11000306.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        int activityId = req.getActivityId();
        int id = req.getId();
        FundPassSignActivity activity = player.getActivityModule().get(activityId);
        if (activity == null) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.activity_not_found.getId());
            return;
        }
        client.sendProtocol(activity.receive(id));
    }

    /**
     * 解析或懒开启玩家跨服世界Boss活动实例，统一收口配置校验、周期状态拦截、等级校验与防卫性跨天/绑定
     *
     * @param client                网络客户端连接
     * @param player                在线玩家对象
     * @param activityId            活动配置 ID
     * @param defaultResp           失败时回发的默认响应消息
     * @param allowClose            是否允许在 CLOSE 领奖展示期放行
     * @param allowPeriodAutoSwitch 是否允许在持有旧期 UID 时自动切换至新期继续执行（仅只读查询允许）
     * @return 就绪的 {@link CrossWorldBossPlayerActivity}，任一前置不满足均已回发错误码并返回 null
     */
    private CrossWorldBossPlayerActivity resolveOrOpenCrossWorldBoss(NetClient client, Player player, int activityId, Message defaultResp, boolean allowClose, boolean allowPeriodAutoSwitch) {
        if (player == null) {
            client.sendProtocol(defaultResp, ErrorMsgEnum.player_not_online.ID);
            return null;
        }
        ActivityConfig actCfg = ActivityManager.instance().getNullable(activityId);
        ActivityCrossWorldBossConfig bossCfg = ActivityCrossWorldBossManager.instance().getNullable(activityId);
        if (actCfg == null || bossCfg == null || actCfg.disable || actCfg.type != ActivityTypeEnum.CrossWorldBoss.ID || !actCfg.isCross) {
            client.sendProtocol(defaultResp, ErrorMsgEnum.config_data_not_found.ID);
            return null;
        }
        int realState = resolveEffectiveState(activityId, player);
        if (realState == ActivityState.NONE_VALUE) {
            client.sendProtocol(defaultResp, ErrorMsgEnum.activity_not_found.ID);
            return null;
        }
        if (realState == ActivityState.VIEW_VALUE) {
            client.sendProtocol(defaultResp, ErrorMsgEnum.not_open.ID);
            return null;
        }
        if (realState == ActivityState.CLOSE_VALUE && !allowClose) {
            client.sendProtocol(defaultResp, ErrorMsgEnum.not_open.ID);
            return null;
        }
        if (realState != ActivityState.START_VALUE && realState != ActivityState.CLOSE_VALUE) {
            client.sendProtocol(defaultResp, ErrorMsgEnum.not_open.ID);
            return null;
        }
        if (actCfg.levelLimit > 0 && player.getLevel() < actCfg.levelLimit) {
            client.sendProtocol(defaultResp, ErrorMsgEnum.level_not_enough.ID);
            return null;
        }
        // 即使客户端跳过 11000401 或 list 直接发写请求，也在此懒创建并强制完成防卫性跨天与跨服绑定
        ActivityBase raw = player.getActivityModule().get(activityId);
        if (raw == null) {
            player.getActivityModule().open(activityId, false);
            raw = player.getActivityModule().get(activityId);
        }
        if (!(raw instanceof CrossWorldBossPlayerActivity playerAct)) {
            client.sendProtocol(defaultResp, ErrorMsgEnum.activity_not_found.ID);
            return null;
        }
        long prevUid = playerAct.getCrossActivityUid();
        playerAct.ensureDailyReset();
        playerAct.ensureCrossActivityBinding();
        if (!allowPeriodAutoSwitch && prevUid > 0L && playerAct.getCrossActivityUid() != prevUid) {
            // 已持有旧期 UID 且跨服已换期时，写/领奖入口绝不跨期错打新期实例，返回 397 提示刷新周期
            client.sendProtocol(defaultResp, ErrorMsgEnum.activity_not_found.ID);
            return null;
        }
        return playerAct;
    }

    /**
     * 解析活动当前生效状态，优先读取全局 {@link ActivityStateManager}；回退本地实例状态时校验销毁时间防止过期残留
     */
    private int resolveEffectiveState(int activityId, Player player) {
        int mgrState = ActivityStateManager.getInstance().getState(activityId);
        if (mgrState != ActivityState.NONE_VALUE) {
            return mgrState;
        }
        ActivityBase existing = player.getActivityModule().get(activityId);
        if (existing != null && existing.getState() != ActivityState.NONE_VALUE) {
            long destroyTime = existing.getDestroyTime();
            if (destroyTime <= 0L || DateUtil.currentTimeMillis() <= destroyTime) {
                return existing.getState();
            }
        }
        return ActivityState.NONE_VALUE;
    }

    /**
     * 通过严格点对点代理调用 Cross 权威动作；支持同 UID 节点迁移单次重试，换期时仅只读动作切新期、写动作绝不跨期错打
     *
     * @param player           在线玩家
     * @param playerAct        玩家侧跨服世界Boss活动实例
     * @param actionType       动作类型常量
     * @param payloadJson      JSON 请求载荷
     * @param isReadOnlyAction true 表示幂等只读动作，换期时可切新期重查；false 表示写/领奖动作，换期直接拒打
     * @return Cross 返回的动作执行结果或标准化失败结果
     */
    private CrossActivityActionResult invokeCrossBossAction(Player player, CrossWorldBossPlayerActivity playerAct, String actionType, String payloadJson, boolean isReadOnlyAction) {
        long beforeUid = playerAct.getCrossActivityUid();
        playerAct.ensureCrossActivityBinding();
        long targetUid = playerAct.getCrossActivityUid();
        if (!isReadOnlyAction && beforeUid > 0L && targetUid != beforeUid) {
            return CrossActivityActionResult.fail(ErrorMsgEnum.activity_not_found.ID, "跨服世界Boss已换期");
        }
        if (targetUid <= 0L) {
            return CrossActivityActionResult.fail(ErrorMsgEnum.activity_not_found.ID, "跨服世界Boss实例未绑定");
        }
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                CrossActivityServiceInterface proxy = ServerHelper.getCrossActivityProxy(targetUid);
                CrossActivityActionResult result = proxy.executePlayerAction(targetUid, player.getPlayerId(), actionType, payloadJson);
                if (result == null) {
                    log.warn("跨服世界Boss RPC返回空结果: playerId={}, uid={}, actionType={}", player.getPlayerId(), targetUid, actionType);
                    return CrossActivityActionResult.fail(ErrorMsgEnum.request_remote_server.ID, "跨服服务返回空结果");
                }
                if (!result.isSuccess() && result.getErrorCode() == ErrorMsgEnum.activity_not_found.ID) {
                    if (attempt == 0) {
                        long nextUid = resolveNextUidAfterRouteFailure(playerAct, targetUid, new IllegalStateException("remote activity_not_found"), isReadOnlyAction);
                        if (nextUid > 0L) {
                            targetUid = nextUid;
                            continue;
                        }
                    }
                    return CrossActivityActionResult.fail(ErrorMsgEnum.activity_not_found.ID, "跨服世界Boss实例不存在或已换期");
                }
                return result;
            } catch (CrossActivityRouteMissingException routeEx) {
                if (attempt == 0) {
                    long nextUid = resolveNextUidAfterRouteFailure(playerAct, targetUid, routeEx, isReadOnlyAction);
                    if (nextUid > 0L) {
                        targetUid = nextUid;
                        continue;
                    }
                    return CrossActivityActionResult.fail(ErrorMsgEnum.activity_not_found.ID, "跨服世界Boss路由失效或已换期");
                }
                log.warn("跨服世界Boss路由重试仍失败: playerId={}, uid={}, actionType={}", player.getPlayerId(), targetUid, actionType, routeEx);
                return CrossActivityActionResult.fail(ErrorMsgEnum.activity_not_found.ID, "跨服世界Boss路由失效");
            } catch (Exception e) {
                log.warn("跨服世界Boss RPC调用异常: playerId={}, uid={}, actionType={}", player.getPlayerId(), targetUid, actionType, e);
                return CrossActivityActionResult.fail(ErrorMsgEnum.request_remote_server.ID, "跨服服务调用异常");
            }
        }
        return CrossActivityActionResult.fail(ErrorMsgEnum.activity_not_found.ID, "跨服世界Boss路由重试耗尽");
    }

    /**
     * 在路由失效或远端返回不存在后判定下一步目标 UID：同 UID 迁移返回原 UID 重试，换期仅放行只读动作，彻底销毁返回 0
     */
    private long resolveNextUidAfterRouteFailure(CrossWorldBossPlayerActivity playerAct, long beforeUid, Exception cause, boolean isReadOnlyAction) {
        playerAct.handleCrossRouteFailure(cause);
        long afterUid = playerAct.getCrossActivityUid();
        if (afterUid > 0L && afterUid == beforeUid) {
            return afterUid;
        }
        if (afterUid > 0L && afterUid != beforeUid) {
            return isReadOnlyAction ? afterUid : 0L;
        }
        return 0L;
    }

    private void crossWorldBossInfo(NetClient client, Object message) {
        ActivityCrossWorldBossInfoRequest_11000401 req = (ActivityCrossWorldBossInfoRequest_11000401) message;
        int activityId = req.getActivityId();
        ActivityCrossWorldBossInfoResponse_11000402 defaultInstance = ActivityCrossWorldBossInfoResponse_11000402.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        CrossWorldBossPlayerActivity playerAct = resolveOrOpenCrossWorldBoss(client, player, activityId, defaultInstance, true, true);
        if (playerAct == null) {
            return;
        }
        QueryDetailRequest crossReq = new QueryDetailRequest();
        crossReq.setPlayerId(player.getPlayerId());
        crossReq.setPlayerName(player.getData().getName());
        crossReq.setServerId(player.getServerId());
        CrossActivityActionResult actionRes = invokeCrossBossAction(player, playerAct, CrossWorldBossActivity.ACTION_QUERY_DETAIL, JsonUtil.toJsonString(crossReq), true);
        if (!actionRes.isSuccess()) {
            client.sendProtocol(defaultInstance, actionRes.getErrorCode());
            return;
        }
        QueryDetailResponse detail = JsonUtil.parseObject(actionRes.getResultPayloadJson(), QueryDetailResponse.class);
        if (detail == null) {
            log.warn("跨服世界Boss详情反序列化为空: playerId={}, activityId={}", player.getPlayerId(), activityId);
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_remote_server.ID);
            return;
        }
        playerAct.setMyTotalDamage(detail.getMyTotalDamage());
        playerAct.setMyRank(detail.getMyRank());
        playerAct.setCachedBossKilled(detail.isKilled());
        playerAct.setCachedUnlockedMilestones(detail.getUnlockedMilestoneIds());
        playerAct.setCachedSnapshotSynced(true);
        ActivityConfig actCfg = ActivityManager.instance().get(activityId);
        ActivityCrossWorldBossConfig bossCfg = ActivityCrossWorldBossManager.instance().get(activityId);
        String scopeKey = DefaultActivityScopeResolver.getInstance().resolveScopeKey(actCfg, player.getServerId());
        int remainingTimes = Math.max(0, bossCfg.dailyFreeTimes + playerAct.getDailyBoughtCount() - playerAct.getDailyUsedCount());
        long endTime = ActivityStateManager.getInstance().getEndTime(activityId);
        if (endTime <= 0L) {
            endTime = playerAct.getEndTime();
        }
        long destroyTime = ActivityStateManager.getInstance().getDestroyTime(activityId);
        if (destroyTime <= 0L) {
            destroyTime = playerAct.getDestroyTime();
        }
        List<Integer> claimedList = new ArrayList<>(playerAct.getClaimedMilestoneIds());
        Collections.sort(claimedList);
        ActivityCrossWorldBossInfoResponse_11000402.Builder resp = ActivityCrossWorldBossInfoResponse_11000402.newBuilder();
        resp.setActivityId(activityId);
        resp.setScopeKey(scopeKey != null ? scopeKey : "");
        resp.setCrossActivityUid(playerAct.getCrossActivityUid());
        resp.setState(detail.getState());
        resp.setMaxHp(detail.getMaxHp());
        resp.setCurrentHp(detail.getCurrentHp());
        resp.setTotalDamage(detail.getTotalDamage());
        resp.setKilled(detail.isKilled());
        resp.setKillerPlayerId(detail.getKillerPlayerId());
        resp.setKillerName(detail.getKillerName() != null ? detail.getKillerName() : "");
        resp.setKillerServerId(detail.getKillerServerId() != null ? detail.getKillerServerId() : "");
        resp.setKillTime(detail.getKillTime());
        resp.setKillRewardClaimed(playerAct.isKillRewardClaimed() || detail.isKillRewardClaimed());
        if (detail.getUnlockedMilestoneIds() != null) {
            resp.addAllUnlockedMilestoneIds(detail.getUnlockedMilestoneIds());
        }
        resp.addAllClaimedMilestoneIds(claimedList);
        resp.setDailyUsedCount(playerAct.getDailyUsedCount());
        resp.setDailyBoughtCount(playerAct.getDailyBoughtCount());
        resp.setRemainingTimes(remainingTimes);
        resp.setMyTotalDamage(detail.getMyTotalDamage());
        resp.setMyRank(detail.getMyRank());
        resp.setEndTime(endTime);
        resp.setDestroyTime(destroyTime);
        client.sendProtocol(resp.build());
    }

    private void crossWorldBossAttack(NetClient client, Object message) {
        ActivityCrossWorldBossAttackRequest_11000403 req = (ActivityCrossWorldBossAttackRequest_11000403) message;
        int activityId = req.getActivityId();
        long damage = req.getDamage();
        ActivityCrossWorldBossAttackResponse_11000404 defaultInstance = ActivityCrossWorldBossAttackResponse_11000404.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        CrossWorldBossPlayerActivity playerAct = resolveOrOpenCrossWorldBoss(client, player, activityId, defaultInstance, false, false);
        if (playerAct == null) {
            return;
        }
        ActivityCrossWorldBossConfig bossCfg = ActivityCrossWorldBossManager.instance().get(activityId);
        if (damage <= 0L || damage > bossCfg.maxSingleDamage) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_parameter_error.ID);
            return;
        }
        int maxTimes = bossCfg.dailyFreeTimes + playerAct.getDailyBoughtCount();
        if (playerAct.getDailyUsedCount() >= maxTimes) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.times_limit.ID);
            return;
        }
        AttackRequest crossReq = new AttackRequest();
        crossReq.setPlayerId(player.getPlayerId());
        crossReq.setPlayerName(player.getData().getName());
        crossReq.setServerId(player.getServerId());
        crossReq.setDamage(damage);
        CrossActivityActionResult actionRes = invokeCrossBossAction(player, playerAct, CrossWorldBossActivity.ACTION_ATTACK, JsonUtil.toJsonString(crossReq), false);
        if (!actionRes.isSuccess()) {
            if (actionRes.getErrorCode() == ErrorMsgEnum.cross_world_boss_killed.ID) {
                playerAct.setCachedBossKilled(true);
            }
            client.sendProtocol(defaultInstance, actionRes.getErrorCode());
            return;
        }
        AttackResponse atkResp = JsonUtil.parseObject(actionRes.getResultPayloadJson(), AttackResponse.class);
        if (atkResp == null) {
            log.warn("跨服世界Boss攻打响应反序列化为空: playerId={}, activityId={}", player.getPlayerId(), activityId);
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_remote_server.ID);
            return;
        }
        // 仅在 Cross 权威确认有效扣血后才递增玩家已用次数，彻底杜绝并发双杀或网络异常白扣次数
        if (atkResp.getActualDamage() > 0L) {
            playerAct.setDailyUsedCount(playerAct.getDailyUsedCount() + 1);
            playerAct.setMyTotalDamage(atkResp.getMyTotalDamage());
            playerAct.setMyRank(atkResp.getMyRank());
            playerAct.setCachedBossKilled(atkResp.isKilled());
            playerAct.setCachedUnlockedMilestones(atkResp.getUnlockedMilestoneIds());
            GameLogger.activity(player, activityId, atkResp.getActualDamage());
        }
        int remainingTimes = Math.max(0, maxTimes - playerAct.getDailyUsedCount());
        ActivityCrossWorldBossAttackResponse_11000404.Builder resp = ActivityCrossWorldBossAttackResponse_11000404.newBuilder();
        resp.setActivityId(activityId);
        resp.setActualDamage(atkResp.getActualDamage());
        resp.setRemainingHp(atkResp.getRemainingHp());
        resp.setMaxHp(atkResp.getMaxHp());
        resp.setKilled(atkResp.isKilled());
        resp.setIsKillShot(atkResp.isKillShot());
        resp.setMyTotalDamage(atkResp.getMyTotalDamage());
        resp.setMyRank(atkResp.getMyRank());
        resp.setDailyUsedCount(playerAct.getDailyUsedCount());
        resp.setRemainingTimes(remainingTimes);
        if (atkResp.getUnlockedMilestoneIds() != null) {
            resp.addAllUnlockedMilestoneIds(atkResp.getUnlockedMilestoneIds());
        }
        if (atkResp.isKillShot() && !playerAct.isKillRewardClaimed()) {
            List<RewardInfo> killRewards = PlayerHelper.addResources(player, bossCfg.killRewards, OpType.CrossWorldBoss, false);
            playerAct.setKillRewardClaimed(true);
            if (killRewards != null && !killRewards.isEmpty()) {
                resp.addAllKillRewards(killRewards);
            }
            // 直发成功后尽力确认 Cross 状态；若确认失败仅告警，后续由 Game 侧状态阻止双领
            try {
                ClaimKillRewardRequest confirmReq = new ClaimKillRewardRequest();
                confirmReq.setPlayerId(player.getPlayerId());
                confirmReq.setConfirmOnly(true);
                CrossActivityActionResult confirmRes = invokeCrossBossAction(player, playerAct, CrossWorldBossActivity.ACTION_CLAIM_KILL_REWARD, JsonUtil.toJsonString(confirmReq), false);
                if (confirmRes != null && !confirmRes.isSuccess()) {
                    log.warn("跨服世界Boss击杀奖励确认返回失败，不影响本次直发: playerId={}, uid={}, errCode={}", player.getPlayerId(), playerAct.getCrossActivityUid(), confirmRes.getErrorCode());
                }
            } catch (Exception e) {
                log.warn("跨服世界Boss击杀奖励确认异常，不影响本次直发: playerId={}, uid={}", player.getPlayerId(), playerAct.getCrossActivityUid(), e);
            }
        }
        client.sendProtocol(resp.build());
    }

    private void crossWorldBossReward(NetClient client, Object message) {
        ActivityCrossWorldBossRewardRequest_11000405 req = (ActivityCrossWorldBossRewardRequest_11000405) message;
        int activityId = req.getActivityId();
        int rewardType = req.getRewardType();
        int milestoneId = req.getMilestoneId();
        ActivityCrossWorldBossRewardResponse_11000406 defaultInstance = ActivityCrossWorldBossRewardResponse_11000406.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        CrossWorldBossPlayerActivity playerAct = resolveOrOpenCrossWorldBoss(client, player, activityId, defaultInstance, true, false);
        if (playerAct == null) {
            return;
        }
        if (rewardType != CrossWorldBossActivity.REWARD_TYPE_MILESTONE && rewardType != CrossWorldBossActivity.REWARD_TYPE_KILL) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_parameter_error.ID);
            return;
        }
        ActivityCrossWorldBossConfig bossCfg = ActivityCrossWorldBossManager.instance().get(activityId);
        List<RewardInfo> rewards;
        int respMilestoneId;
        if (rewardType == CrossWorldBossActivity.REWARD_TYPE_MILESTONE) {
            if (bossCfg.milestoneThresholds == null || milestoneId < 1 || milestoneId > bossCfg.milestoneThresholds.length || bossCfg.milestoneRewards == null || milestoneId > bossCfg.milestoneRewards.length) {
                client.sendProtocol(defaultInstance, ErrorMsgEnum.request_parameter_error.ID);
                return;
            }
            if (playerAct.getClaimedMilestoneIds().contains(milestoneId)) {
                client.sendProtocol(defaultInstance, ErrorMsgEnum.repeat_request.ID);
                return;
            }
            ClaimMilestoneRequest crossReq = new ClaimMilestoneRequest();
            crossReq.setPlayerId(player.getPlayerId());
            crossReq.setMilestoneId(milestoneId);
            CrossActivityActionResult actionRes = invokeCrossBossAction(player, playerAct, CrossWorldBossActivity.ACTION_CLAIM_MILESTONE, JsonUtil.toJsonString(crossReq), false);
            if (!actionRes.isSuccess()) {
                client.sendProtocol(defaultInstance, actionRes.getErrorCode());
                return;
            }
            ClaimMilestoneResponse claimResp = JsonUtil.parseObject(actionRes.getResultPayloadJson(), ClaimMilestoneResponse.class);
            if (claimResp == null) {
                log.warn("跨服世界Boss里程碑校验响应反序列化为空: playerId={}, activityId={}, milestoneId={}", player.getPlayerId(), activityId, milestoneId);
                client.sendProtocol(defaultInstance, ErrorMsgEnum.request_remote_server.ID);
                return;
            }
            playerAct.getClaimedMilestoneIds().add(milestoneId);
            playerAct.setMyTotalDamage(claimResp.getMyTotalDamage());
            playerAct.setCachedUnlockedMilestones(claimResp.getUnlockedMilestoneIds());
            rewards = PlayerHelper.addResources(player, bossCfg.milestoneRewards[milestoneId - 1], OpType.CrossWorldBoss);
            GameLogger.activity(player, activityId, milestoneId);
            respMilestoneId = milestoneId;
        } else {
            if (playerAct.isKillRewardClaimed()) {
                client.sendProtocol(defaultInstance, ErrorMsgEnum.repeat_request.ID);
                return;
            }
            ClaimKillRewardRequest crossReq = new ClaimKillRewardRequest();
            crossReq.setPlayerId(player.getPlayerId());
            crossReq.setConfirmOnly(false);
            CrossActivityActionResult actionRes = invokeCrossBossAction(player, playerAct, CrossWorldBossActivity.ACTION_CLAIM_KILL_REWARD, JsonUtil.toJsonString(crossReq), false);
            if (!actionRes.isSuccess()) {
                client.sendProtocol(defaultInstance, actionRes.getErrorCode());
                return;
            }
            playerAct.setKillRewardClaimed(true);
            rewards = PlayerHelper.addResources(player, bossCfg.killRewards, OpType.CrossWorldBoss, false);
            GameLogger.activity(player, activityId, 0);
            respMilestoneId = 0;
        }
        List<Integer> claimedList = new ArrayList<>(playerAct.getClaimedMilestoneIds());
        Collections.sort(claimedList);
        ActivityCrossWorldBossRewardResponse_11000406.Builder resp = ActivityCrossWorldBossRewardResponse_11000406.newBuilder();
        resp.setActivityId(activityId);
        resp.setRewardType(rewardType);
        resp.setMilestoneId(respMilestoneId);
        if (rewards != null && !rewards.isEmpty()) {
            resp.addAllRewards(rewards);
        }
        resp.addAllClaimedMilestoneIds(claimedList);
        resp.setKillRewardClaimed(playerAct.isKillRewardClaimed());
        client.sendProtocol(resp.build());
    }

    private void crossWorldBossBuyCount(NetClient client, Object message) {
        ActivityCrossWorldBossBuyCountRequest_11000407 req = (ActivityCrossWorldBossBuyCountRequest_11000407) message;
        int activityId = req.getActivityId();
        ActivityCrossWorldBossBuyCountResponse_11000408 defaultInstance = ActivityCrossWorldBossBuyCountResponse_11000408.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        CrossWorldBossPlayerActivity playerAct = resolveOrOpenCrossWorldBoss(client, player, activityId, defaultInstance, false, false);
        if (playerAct == null) {
            return;
        }
        ActivityCrossWorldBossConfig bossCfg = ActivityCrossWorldBossManager.instance().get(activityId);
        if (playerAct.getDailyBoughtCount() >= bossCfg.dailyBuyLimit) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.buy_over_limit.ID);
            return;
        }
        if (bossCfg.buyCosts == null || bossCfg.buyCosts.length == 0) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.config_data_not_found.ID);
            return;
        }
        int costIdx = Math.min(playerAct.getDailyBoughtCount(), bossCfg.buyCosts.length - 1);
        int[] costPair = bossCfg.buyCosts[costIdx];
        if (!PlayerHelper.isEnough(player, costPair)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.resource_not_enough.ID);
            return;
        }
        // 先向 Cross 确认 Boss 尚未被击杀且仍处于 START，避免死 Boss 场景错扣玩家货币
        QueryDetailRequest detailReq = new QueryDetailRequest();
        detailReq.setPlayerId(player.getPlayerId());
        detailReq.setPlayerName(player.getData().getName());
        detailReq.setServerId(player.getServerId());
        CrossActivityActionResult actionRes = invokeCrossBossAction(player, playerAct, CrossWorldBossActivity.ACTION_QUERY_DETAIL, JsonUtil.toJsonString(detailReq), false);
        if (!actionRes.isSuccess()) {
            client.sendProtocol(defaultInstance, actionRes.getErrorCode());
            return;
        }
        QueryDetailResponse detail = JsonUtil.parseObject(actionRes.getResultPayloadJson(), QueryDetailResponse.class);
        if (detail == null) {
            log.warn("跨服世界Boss购买次数查询详情反序列化为空: playerId={}, activityId={}", player.getPlayerId(), activityId);
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_remote_server.ID);
            return;
        }
        if (detail.isKilled()) {
            playerAct.setCachedBossKilled(true);
            client.sendProtocol(defaultInstance, ErrorMsgEnum.cross_world_boss_killed.ID);
            return;
        }
        if (detail.getState() != ActivityState.START_VALUE) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.not_open.ID);
            return;
        }
        PlayerHelper.delResources(player, costPair, OpType.CrossWorldBoss);
        int newBought = playerAct.getDailyBoughtCount() + 1;
        playerAct.setDailyBoughtCount(newBought);
        GameLogger.activity(player, activityId, newBought);
        int remainingTimes = Math.max(0, bossCfg.dailyFreeTimes + newBought - playerAct.getDailyUsedCount());
        ActivityCrossWorldBossBuyCountResponse_11000408.Builder resp = ActivityCrossWorldBossBuyCountResponse_11000408.newBuilder();
        resp.setActivityId(activityId);
        resp.setDailyBoughtCount(newBought);
        resp.setRemainingTimes(remainingTimes);
        client.sendProtocol(resp.build());
    }

    private void crossWorldBossRank(NetClient client, Object message) {
        ActivityCrossWorldBossRankRequest_11000409 req = (ActivityCrossWorldBossRankRequest_11000409) message;
        int activityId = req.getActivityId();
        int page = req.getPage();
        int pageSize = req.getPageSize();
        ActivityCrossWorldBossRankResponse_1100040a defaultInstance = ActivityCrossWorldBossRankResponse_1100040a.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        CrossWorldBossPlayerActivity playerAct = resolveOrOpenCrossWorldBoss(client, player, activityId, defaultInstance, true, true);
        if (playerAct == null) {
            return;
        }
        if (page < 1 || pageSize < 1 || pageSize > 100) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_parameter_error.ID);
            return;
        }
        QueryRankRequest crossReq = new QueryRankRequest();
        crossReq.setPlayerId(player.getPlayerId());
        crossReq.setPage(page);
        crossReq.setPageSize(pageSize);
        CrossActivityActionResult actionRes = invokeCrossBossAction(player, playerAct, CrossWorldBossActivity.ACTION_QUERY_RANK, JsonUtil.toJsonString(crossReq), true);
        if (!actionRes.isSuccess()) {
            client.sendProtocol(defaultInstance, actionRes.getErrorCode());
            return;
        }
        QueryRankResponse rankResp = JsonUtil.parseObject(actionRes.getResultPayloadJson(), QueryRankResponse.class);
        if (rankResp == null) {
            log.warn("跨服世界Boss排行榜反序列化为空: playerId={}, activityId={}", player.getPlayerId(), activityId);
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_remote_server.ID);
            return;
        }
        playerAct.setMyRank(rankResp.getMyRank());
        playerAct.setMyTotalDamage(rankResp.getMyDamage());
        ActivityCrossWorldBossRankResponse_1100040a.Builder resp = ActivityCrossWorldBossRankResponse_1100040a.newBuilder();
        resp.setActivityId(activityId);
        resp.setTotalCount(rankResp.getTotalCount());
        resp.setPage(rankResp.getPage());
        resp.setPageSize(rankResp.getPageSize());
        if (rankResp.getEntries() != null) {
            for (RankEntryPayload entry : rankResp.getEntries()) {
                resp.addEntries(CrossWorldBossRankEntry.newBuilder().setRank(entry.getRank()).setPlayerId(entry.getPlayerId()).setPlayerName(entry.getPlayerName() != null ? entry.getPlayerName() : "").setServerId(entry.getServerId() != null ? entry.getServerId() : "").setDamage(entry.getDamage()).build());
            }
        }
        String myName = player.getData().getName() != null ? player.getData().getName() : "";
        String myServerId = player.getServerId() != null ? player.getServerId() : "";
        resp.setMyRankEntry(CrossWorldBossRankEntry.newBuilder().setRank(rankResp.getMyRank()).setPlayerId(player.getPlayerId()).setPlayerName(myName).setServerId(myServerId).setDamage(rankResp.getMyDamage()).build());
        client.sendProtocol(resp.build());
    }

    /**
     * 无锁读取 RedisLocalCache 中的宗门展示元数据；缓存缺失或异常时安全降级，绝不发起同步跨服 RPC
     */
    private GuildDisplayMeta resolveGuildDisplayMeta(Player player, long guildId) {
        String fallbackName = "宗门#" + guildId;
        String fallbackServerId = player.getServerId() != null ? player.getServerId() : "";
        if (guildId <= 0L) {
            return new GuildDisplayMeta("", 0, fallbackServerId);
        }
        try {
            SimpleGuild simpleGuild = RedisLocalCache.getInstance().get(CacheType.ZONG_MEN_SIMPLE_DATA.key(guildId));
            if (simpleGuild != null) {
                String name = simpleGuild.getName();
                String serverId = simpleGuild.getServerId();
                return new GuildDisplayMeta(
                        (name != null && !name.trim().isEmpty()) ? name : fallbackName,
                        simpleGuild.getIcon(),
                        (serverId != null && !serverId.trim().isEmpty()) ? serverId : fallbackServerId);
            }
        } catch (Exception e) {
            log.warn("读取宗门展示缓存异常，降级使用兜底元数据: playerId={}, guildId={}",
                    player.getPlayerId(), guildId, e);
        }
        return new GuildDisplayMeta(fallbackName, 0, fallbackServerId);
    }

    /**
     * 跨服宗门争霸统一前置守卫：校验配置、状态与等级门槛，完成懒创建、防卫性跨天重置与跨服绑定
     */
    private CrossGuildWarPlayerActivity resolveOrOpenCrossGuildWar(NetClient client, Player player, int activityId,
            Message defaultResp, boolean allowClose, boolean allowPeriodAutoSwitch) {
        if (player == null) {
            client.sendProtocol(defaultResp, ErrorMsgEnum.player_not_found.ID);
            return null;
        }
        ActivityConfig actCfg = ActivityManager.instance().getNullable(activityId);
        ActivityCrossGuildWarConfig warCfg = ActivityCrossGuildWarManager.instance().getNullable(activityId);
        if (actCfg == null || warCfg == null || actCfg.disable
                || actCfg.type != ActivityTypeEnum.CrossGuildWar.ID || !actCfg.isCross) {
            client.sendProtocol(defaultResp, ErrorMsgEnum.config_data_not_found.ID);
            return null;
        }
        int effState = resolveEffectiveState(activityId, player);
        if (effState == ActivityState.NONE_VALUE) {
            client.sendProtocol(defaultResp, ErrorMsgEnum.activity_not_found.ID);
            return null;
        }
        if (effState == ActivityState.VIEW_VALUE || (effState == ActivityState.CLOSE_VALUE && !allowClose)) {
            client.sendProtocol(defaultResp, ErrorMsgEnum.not_open.ID);
            return null;
        }
        if (actCfg.levelLimit > 0 && player.getLevel() < actCfg.levelLimit) {
            client.sendProtocol(defaultResp, ErrorMsgEnum.level_not_enough.ID);
            return null;
        }
        ActivityBase raw = player.getActivityModule().get(activityId);
        if (raw == null) {
            player.getActivityModule().open(activityId, false);
            raw = player.getActivityModule().get(activityId);
        }
        if (!(raw instanceof CrossGuildWarPlayerActivity playerAct)) {
            client.sendProtocol(defaultResp, ErrorMsgEnum.activity_not_found.ID);
            return null;
        }
        long prevUid = playerAct.getCrossActivityUid();
        playerAct.ensureDailyReset();
        playerAct.ensureCrossActivityBinding();
        if (!allowPeriodAutoSwitch && prevUid > 0L && playerAct.getCrossActivityUid() != prevUid) {
            // 已持有旧期 UID 且跨服已换期时，写/领奖入口绝不跨期错打新期实例
            client.sendProtocol(defaultResp, ErrorMsgEnum.activity_not_found.ID);
            return null;
        }
        return playerAct;
    }

    /**
     * 通过严格点对点代理调用 Cross 宗门争霸权威动作；支持同 UID 节点迁移单次重试，换期仅只读动作切新期重查
     */
    private CrossActivityActionResult invokeCrossGuildWarAction(Player player, CrossGuildWarPlayerActivity playerAct,
            String actionType, String payloadJson, boolean allowPeriodChangeRetry) {
        long beforeUid = playerAct.getCrossActivityUid();
        playerAct.ensureCrossActivityBinding();
        long targetUid = playerAct.getCrossActivityUid();
        if (!allowPeriodChangeRetry && beforeUid > 0L && targetUid != beforeUid) {
            return CrossActivityActionResult.fail(ErrorMsgEnum.activity_not_found.ID, "跨服宗门争霸已换期");
        }
        if (targetUid <= 0L) {
            return CrossActivityActionResult.fail(ErrorMsgEnum.activity_not_found.ID, "跨服宗门争霸实例未绑定");
        }
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                CrossActivityServiceInterface proxy = ServerHelper.getCrossActivityProxy(targetUid);
                CrossActivityActionResult result = proxy.executePlayerAction(
                        targetUid, player.getPlayerId(), actionType, payloadJson);
                if (result == null) {
                    log.warn("跨服宗门争霸 RPC返回空结果: playerId={}, uid={}, actionType={}",
                            player.getPlayerId(), targetUid, actionType);
                    return CrossActivityActionResult.fail(ErrorMsgEnum.request_remote_server.ID, "跨服服务返回空结果");
                }
                if (!result.isSuccess() && result.getErrorCode() == ErrorMsgEnum.activity_not_found.ID) {
                    if (attempt == 0) {
                        long nextUid = resolveNextUidAfterWarRouteFailure(playerAct, targetUid,
                                new IllegalStateException("remote activity_not_found"), allowPeriodChangeRetry);
                        if (nextUid > 0L) {
                            targetUid = nextUid;
                            continue;
                        }
                    }
                    return CrossActivityActionResult.fail(ErrorMsgEnum.activity_not_found.ID,
                            "跨服宗门争霸实例不存在或已换期");
                }
                return result;
            } catch (CrossActivityRouteMissingException routeEx) {
                if (attempt == 0) {
                    long nextUid = resolveNextUidAfterWarRouteFailure(
                            playerAct, targetUid, routeEx, allowPeriodChangeRetry);
                    if (nextUid > 0L) {
                        targetUid = nextUid;
                        continue;
                    }
                    return CrossActivityActionResult.fail(ErrorMsgEnum.activity_not_found.ID,
                            "跨服宗门争霸路由失效或已换期");
                }
                log.warn("跨服宗门争霸路由重试仍失败: playerId={}, uid={}, actionType={}",
                        player.getPlayerId(), targetUid, actionType, routeEx);
                return CrossActivityActionResult.fail(ErrorMsgEnum.activity_not_found.ID, "跨服宗门争霸路由失效");
            } catch (Exception e) {
                log.warn("跨服宗门争霸 RPC调用异常: playerId={}, uid={}, actionType={}",
                        player.getPlayerId(), targetUid, actionType, e);
                return CrossActivityActionResult.fail(ErrorMsgEnum.request_remote_server.ID, "跨服服务调用异常");
            }
        }
        return CrossActivityActionResult.fail(ErrorMsgEnum.activity_not_found.ID, "跨服宗门争霸路由重试耗尽");
    }

    /**
     * 路由失效后判定下一步目标 UID：同 UID 迁移返回原 UID 重试，换期仅放行只读动作，彻底销毁返回 0
     */
    private long resolveNextUidAfterWarRouteFailure(CrossGuildWarPlayerActivity playerAct, long beforeUid,
            Exception cause, boolean allowPeriodChangeRetry) {
        playerAct.handleCrossRouteFailure(cause);
        long afterUid = playerAct.getCrossActivityUid();
        if (afterUid > 0L && afterUid == beforeUid) {
            return afterUid;
        }
        if (afterUid > 0L && afterUid != beforeUid) {
            return allowPeriodChangeRetry ? afterUid : 0L;
        }
        return 0L;
    }

    /**
     * 校验据点 ID 是否属于当前配置表合法据点列表
     */
    private static boolean isValidStrongholdId(ActivityCrossGuildWarConfig warCfg, int strongholdId) {
        if (strongholdId <= 0 || warCfg.strongholdIds == null) {
            return false;
        }
        for (int candidate : warCfg.strongholdIds) {
            if (candidate == strongholdId) {
                return true;
            }
        }
        return false;
    }

    /**
     * 转换 Cross 宗门排行载荷为客户端 Proto 结构
     */
    private static CrossGuildWarGuildRankEntry toGuildRankProto(CrossGuildWarPayloads.GuildRankEntryPayload src) {
        if (src == null) {
            return CrossGuildWarGuildRankEntry.getDefaultInstance();
        }
        return CrossGuildWarGuildRankEntry.newBuilder()
                .setRank(src.getRank())
                .setGuildId(src.getGuildId())
                .setGuildName(src.getGuildName() != null ? src.getGuildName() : "")
                .setGuildIcon(src.getGuildIcon())
                .setGuildServerId(src.getGuildServerId() != null ? src.getGuildServerId() : "")
                .setBaseScore(src.getBaseScore())
                .setStrongholdBonusScore(src.getStrongholdBonusScore())
                .setTotalScore(src.getTotalScore())
                .setOwnedStrongholdCount(src.getOwnedStrongholdCount())
                .build();
    }

    /**
     * 转换 Cross 成员贡献排行载荷为客户端 Proto 结构
     */
    private static CrossGuildWarMemberRankEntry toMemberRankProto(CrossGuildWarPayloads.MemberRankEntryPayload src) {
        if (src == null) {
            return CrossGuildWarMemberRankEntry.getDefaultInstance();
        }
        return CrossGuildWarMemberRankEntry.newBuilder()
                .setRank(src.getRank())
                .setPlayerId(src.getPlayerId())
                .setPlayerName(src.getPlayerName() != null ? src.getPlayerName() : "")
                .setServerId(src.getServerId() != null ? src.getServerId() : "")
                .setScore(src.getScore())
                .build();
    }

    /**
     * 宗门展示元数据值对象
     */
    private record GuildDisplayMeta(String name, int icon, String serverId) {
    }

    private void crossGuildWarInfo(NetClient client, Object message) {
        ActivityCrossGuildWarInfoRequest_11000501 req = (ActivityCrossGuildWarInfoRequest_11000501) message;
        int activityId = req.getActivityId();
        ActivityCrossGuildWarInfoResponse_11000502 defaultInstance = ActivityCrossGuildWarInfoResponse_11000502.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        CrossGuildWarPlayerActivity playerAct = resolveOrOpenCrossGuildWar(
                client, player, activityId, defaultInstance, true, true);
        if (playerAct == null) {
            return;
        }
        CrossGuildWarPayloads.QueryDetailRequest crossReq = new CrossGuildWarPayloads.QueryDetailRequest();
        crossReq.setPlayerId(player.getPlayerId());
        crossReq.setCurrentGuildId(player.getGuildId());
        crossReq.setLockedGuildId(playerAct.getLockedGuildId());
        CrossActivityActionResult actionRes = invokeCrossGuildWarAction(
                player, playerAct, CrossGuildWarActivity.ACTION_QUERY_DETAIL, JsonUtil.toJsonString(crossReq), true);
        if (!actionRes.isSuccess()) {
            client.sendProtocol(defaultInstance, actionRes.getErrorCode());
            return;
        }
        CrossGuildWarPayloads.QueryDetailResponse detail = JsonUtil.parseObject(
                actionRes.getResultPayloadJson(), CrossGuildWarPayloads.QueryDetailResponse.class);
        if (detail == null) {
            log.warn("跨服宗门争霸详情反序列化为空: playerId={}, activityId={}", player.getPlayerId(), activityId);
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_remote_server.ID);
            return;
        }

        // 无条件同步 Cross 权威锁定宗门与战功镜像到本地
        playerAct.setLockedGuildId(detail.getLockedGuildId());
        playerAct.setMyScore(detail.getMyScore());
        if (detail.getMyGuildSummary() != null) {
            playerAct.setMyGuildTotalScore(detail.getMyGuildSummary().getTotalScore());
            playerAct.setMyGuildRank(detail.getMyGuildSummary().getRank());
        }
        playerAct.setCachedUnlockedPersonalMilestoneIds(detail.getUnlockedPersonalMilestoneIds());
        playerAct.setCachedUnlockedGuildMilestoneIds(detail.getUnlockedGuildMilestoneIds());
        playerAct.setCachedSnapshotSynced(true);

        ActivityConfig actCfg = ActivityManager.instance().get(activityId);
        ActivityCrossGuildWarConfig warCfg = ActivityCrossGuildWarManager.instance().get(activityId);
        String scopeKey = DefaultActivityScopeResolver.getInstance().resolveScopeKey(actCfg, player.getServerId());
        int remainingTimes = Math.max(0,
                warCfg.dailyFreeTimes + playerAct.getDailyBoughtCount() - playerAct.getDailyUsedCount());
        long endTime = detail.getEndTime() > 0L ? detail.getEndTime() : ActivityStateManager.getInstance().getEndTime(activityId);
        if (endTime <= 0L) {
            endTime = playerAct.getEndTime();
        }
        long destroyTime = detail.getDestroyTime() > 0L
                ? detail.getDestroyTime() : ActivityStateManager.getInstance().getDestroyTime(activityId);
        if (destroyTime <= 0L) {
            destroyTime = playerAct.getDestroyTime();
        }
        List<Integer> claimedPersonal = new ArrayList<>(playerAct.getClaimedPersonalMilestoneIds());
        Collections.sort(claimedPersonal);
        List<Integer> claimedGuild = new ArrayList<>(playerAct.getClaimedGuildMilestoneIds());
        Collections.sort(claimedGuild);

        ActivityCrossGuildWarInfoResponse_11000502.Builder resp = ActivityCrossGuildWarInfoResponse_11000502.newBuilder();
        resp.setActivityId(activityId);
        resp.setScopeKey(scopeKey != null ? scopeKey : "");
        resp.setCrossActivityUid(playerAct.getCrossActivityUid());
        resp.setState(detail.getState());
        resp.setLockedGuildId(playerAct.getLockedGuildId());
        resp.setCurrentGuildId(player.getGuildId());
        resp.setMyScore(detail.getMyScore());
        resp.setDailyUsedCount(playerAct.getDailyUsedCount());
        resp.setDailyBoughtCount(playerAct.getDailyBoughtCount());
        resp.setRemainingTimes(remainingTimes);
        if (detail.getStrongholds() != null) {
            for (CrossGuildWarPayloads.StrongholdPayload sh : detail.getStrongholds()) {
                resp.addStrongholds(CrossGuildWarStrongholdInfo.newBuilder()
                        .setStrongholdId(sh.getStrongholdId())
                        .setOwnerGuildId(sh.getOwnerGuildId())
                        .setOwnerGuildName(sh.getOwnerGuildName() != null ? sh.getOwnerGuildName() : "")
                        .setOwnerGuildIcon(sh.getOwnerGuildIcon())
                        .setOwnerGuildServerId(sh.getOwnerGuildServerId() != null ? sh.getOwnerGuildServerId() : "")
                        .setOwnerScore(sh.getOwnerScore())
                        .setMyGuildScore(sh.getMyGuildScore())
                        .build());
            }
        }
        resp.setMyGuildSummary(toGuildRankProto(detail.getMyGuildSummary()));
        if (detail.getUnlockedPersonalMilestoneIds() != null) {
            resp.addAllUnlockedPersonalMilestoneIds(detail.getUnlockedPersonalMilestoneIds());
        }
        resp.addAllClaimedPersonalMilestoneIds(claimedPersonal);
        if (detail.getUnlockedGuildMilestoneIds() != null) {
            resp.addAllUnlockedGuildMilestoneIds(detail.getUnlockedGuildMilestoneIds());
        }
        resp.addAllClaimedGuildMilestoneIds(claimedGuild);
        resp.setEndTime(endTime);
        resp.setDestroyTime(destroyTime);
        client.sendProtocol(resp.build());
    }

    private void crossGuildWarAttack(NetClient client, Object message) {
        ActivityCrossGuildWarAttackRequest_11000503 req = (ActivityCrossGuildWarAttackRequest_11000503) message;
        int activityId = req.getActivityId();
        int strongholdId = req.getStrongholdId();
        long score = req.getScore();
        ActivityCrossGuildWarAttackResponse_11000504 defaultInstance = ActivityCrossGuildWarAttackResponse_11000504.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        CrossGuildWarPlayerActivity playerAct = resolveOrOpenCrossGuildWar(
                client, player, activityId, defaultInstance, false, false);
        if (playerAct == null) {
            return;
        }
        long currentGuildId = player.getGuildId();
        if (currentGuildId <= 0L) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.zong_men_not_exist.ID);
            return;
        }
        if (playerAct.getLockedGuildId() > 0L && playerAct.getLockedGuildId() != currentGuildId) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.cross_guild_war_guild_locked.ID);
            return;
        }
        ActivityCrossGuildWarConfig warCfg = ActivityCrossGuildWarManager.instance().get(activityId);
        if (!isValidStrongholdId(warCfg, strongholdId)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.cross_guild_war_stronghold_invalid.ID);
            return;
        }
        if (score <= 0L || score > warCfg.maxSingleScore) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_parameter_error.ID);
            return;
        }
        int maxTimes = warCfg.dailyFreeTimes + playerAct.getDailyBoughtCount();
        if (playerAct.getDailyUsedCount() >= maxTimes) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.times_limit.ID);
            return;
        }

        GuildDisplayMeta guildMeta = resolveGuildDisplayMeta(player, currentGuildId);
        CrossGuildWarPayloads.AttackRequest crossReq = new CrossGuildWarPayloads.AttackRequest();
        crossReq.setPlayerId(player.getPlayerId());
        crossReq.setPlayerName(player.getData().getName());
        crossReq.setServerId(player.getServerId());
        crossReq.setCurrentGuildId(currentGuildId);
        crossReq.setGuildName(guildMeta.name());
        crossReq.setGuildIcon(guildMeta.icon());
        crossReq.setGuildServerId(guildMeta.serverId());
        crossReq.setStrongholdId(strongholdId);
        crossReq.setScore(score);

        CrossActivityActionResult actionRes = invokeCrossGuildWarAction(
                player, playerAct, CrossGuildWarActivity.ACTION_ATTACK_STRONGHOLD,
                JsonUtil.toJsonString(crossReq), false);
        if (!actionRes.isSuccess()) {
            client.sendProtocol(defaultInstance, actionRes.getErrorCode());
            return;
        }
        CrossGuildWarPayloads.AttackResponse atkResp = JsonUtil.parseObject(
                actionRes.getResultPayloadJson(), CrossGuildWarPayloads.AttackResponse.class);
        if (atkResp == null) {
            log.warn("跨服宗门争霸出征响应反序列化为空: playerId={}, activityId={}", player.getPlayerId(), activityId);
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_remote_server.ID);
            return;
        }

        // 仅 Cross 成功记分后才递增已用次数并锁定参战宗门
        playerAct.setDailyUsedCount(playerAct.getDailyUsedCount() + 1);
        playerAct.setLockedGuildId(atkResp.getLockedGuildId());
        playerAct.setMyScore(atkResp.getMyScore());
        playerAct.setMyGuildTotalScore(atkResp.getGuildTotalScore());
        playerAct.setMyGuildRank(atkResp.getGuildRank());
        playerAct.setCachedUnlockedPersonalMilestoneIds(atkResp.getUnlockedPersonalMilestoneIds());
        playerAct.setCachedUnlockedGuildMilestoneIds(atkResp.getUnlockedGuildMilestoneIds());
        playerAct.setCachedSnapshotSynced(true);
        GameLogger.activity(player, activityId, strongholdId);

        int remainingTimes = Math.max(0, maxTimes - playerAct.getDailyUsedCount());
        ActivityCrossGuildWarAttackResponse_11000504.Builder resp = ActivityCrossGuildWarAttackResponse_11000504.newBuilder();
        resp.setActivityId(activityId);
        resp.setEffectiveScore(atkResp.getEffectiveScore());
        resp.setMyScore(atkResp.getMyScore());
        resp.setLockedGuildId(atkResp.getLockedGuildId());
        resp.setGuildTotalScore(atkResp.getGuildTotalScore());
        resp.setGuildRank(atkResp.getGuildRank());
        resp.setStrongholdId(atkResp.getStrongholdId());
        resp.setGuildStrongholdScore(atkResp.getGuildStrongholdScore());
        resp.setOwnerGuildId(atkResp.getOwnerGuildId());
        resp.setOwnerGuildName(atkResp.getOwnerGuildName() != null ? atkResp.getOwnerGuildName() : "");
        resp.setOwnerScore(atkResp.getOwnerScore());
        resp.setCaptured(atkResp.isCaptured());
        resp.setDailyUsedCount(playerAct.getDailyUsedCount());
        resp.setRemainingTimes(remainingTimes);
        if (atkResp.getUnlockedPersonalMilestoneIds() != null) {
            resp.addAllUnlockedPersonalMilestoneIds(atkResp.getUnlockedPersonalMilestoneIds());
        }
        if (atkResp.getUnlockedGuildMilestoneIds() != null) {
            resp.addAllUnlockedGuildMilestoneIds(atkResp.getUnlockedGuildMilestoneIds());
        }
        client.sendProtocol(resp.build());
    }

    private void crossGuildWarReward(NetClient client, Object message) {
        ActivityCrossGuildWarRewardRequest_11000505 req = (ActivityCrossGuildWarRewardRequest_11000505) message;
        int activityId = req.getActivityId();
        int rewardType = req.getRewardType();
        int milestoneId = req.getMilestoneId();
        ActivityCrossGuildWarRewardResponse_11000506 defaultInstance = ActivityCrossGuildWarRewardResponse_11000506.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        CrossGuildWarPlayerActivity playerAct = resolveOrOpenCrossGuildWar(
                client, player, activityId, defaultInstance, true, false);
        if (playerAct == null) {
            return;
        }
        ActivityCrossGuildWarConfig warCfg = ActivityCrossGuildWarManager.instance().get(activityId);
        int[] rewardPairs;
        if (rewardType == CrossGuildWarActivity.REWARD_TYPE_PERSONAL) {
            if (warCfg.personalMilestoneThresholds == null
                    || milestoneId < 1 || milestoneId > warCfg.personalMilestoneThresholds.length) {
                client.sendProtocol(defaultInstance, ErrorMsgEnum.request_parameter_error.ID);
                return;
            }
            if (playerAct.getClaimedPersonalMilestoneIds().contains(milestoneId)) {
                client.sendProtocol(defaultInstance, ErrorMsgEnum.repeat_request.ID);
                return;
            }
            rewardPairs = warCfg.personalMilestoneRewards[milestoneId - 1];
        } else if (rewardType == CrossGuildWarActivity.REWARD_TYPE_GUILD) {
            if (warCfg.guildMilestoneThresholds == null
                    || milestoneId < 1 || milestoneId > warCfg.guildMilestoneThresholds.length) {
                client.sendProtocol(defaultInstance, ErrorMsgEnum.request_parameter_error.ID);
                return;
            }
            if (playerAct.getLockedGuildId() <= 0L) {
                client.sendProtocol(defaultInstance, ErrorMsgEnum.cross_guild_war_no_locked_guild.ID);
                return;
            }
            if (playerAct.getClaimedGuildMilestoneIds().contains(milestoneId)) {
                client.sendProtocol(defaultInstance, ErrorMsgEnum.repeat_request.ID);
                return;
            }
            rewardPairs = warCfg.guildMilestoneRewards[milestoneId - 1];
        } else {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_parameter_error.ID);
            return;
        }

        CrossGuildWarPayloads.ClaimMilestoneRequest crossReq = new CrossGuildWarPayloads.ClaimMilestoneRequest();
        crossReq.setPlayerId(player.getPlayerId());
        crossReq.setRewardType(rewardType);
        crossReq.setMilestoneId(milestoneId);
        crossReq.setExpectedLockedGuildId(playerAct.getLockedGuildId());
        CrossActivityActionResult actionRes = invokeCrossGuildWarAction(
                player, playerAct, CrossGuildWarActivity.ACTION_CLAIM_MILESTONE,
                JsonUtil.toJsonString(crossReq), false);
        if (!actionRes.isSuccess()) {
            client.sendProtocol(defaultInstance, actionRes.getErrorCode());
            return;
        }

        if (rewardType == CrossGuildWarActivity.REWARD_TYPE_PERSONAL) {
            playerAct.getClaimedPersonalMilestoneIds().add(milestoneId);
        } else {
            playerAct.getClaimedGuildMilestoneIds().add(milestoneId);
        }
        List<RewardInfo> rewards = PlayerHelper.addResources(
                player, new int[][] { rewardPairs }, OpType.CrossGuildWar, false);
        GameLogger.activity(player, activityId, milestoneId);

        List<Integer> claimedPersonal = new ArrayList<>(playerAct.getClaimedPersonalMilestoneIds());
        Collections.sort(claimedPersonal);
        List<Integer> claimedGuild = new ArrayList<>(playerAct.getClaimedGuildMilestoneIds());
        Collections.sort(claimedGuild);

        ActivityCrossGuildWarRewardResponse_11000506.Builder resp = ActivityCrossGuildWarRewardResponse_11000506.newBuilder();
        resp.setActivityId(activityId);
        resp.setRewardType(rewardType);
        resp.setMilestoneId(milestoneId);
        if (rewards != null) {
            resp.addAllRewards(rewards);
        }
        resp.addAllClaimedPersonalMilestoneIds(claimedPersonal);
        resp.addAllClaimedGuildMilestoneIds(claimedGuild);
        client.sendProtocol(resp.build());
    }

    private void crossGuildWarBuyCount(NetClient client, Object message) {
        ActivityCrossGuildWarBuyCountRequest_11000507 req = (ActivityCrossGuildWarBuyCountRequest_11000507) message;
        int activityId = req.getActivityId();
        ActivityCrossGuildWarBuyCountResponse_11000508 defaultInstance = ActivityCrossGuildWarBuyCountResponse_11000508.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        CrossGuildWarPlayerActivity playerAct = resolveOrOpenCrossGuildWar(
                client, player, activityId, defaultInstance, false, false);
        if (playerAct == null) {
            return;
        }
        long currentGuildId = player.getGuildId();
        if (currentGuildId <= 0L) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.zong_men_not_exist.ID);
            return;
        }
        if (playerAct.getLockedGuildId() > 0L && playerAct.getLockedGuildId() != currentGuildId) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.cross_guild_war_guild_locked.ID);
            return;
        }
        ActivityCrossGuildWarConfig warCfg = ActivityCrossGuildWarManager.instance().get(activityId);
        if (playerAct.getDailyBoughtCount() >= warCfg.dailyBuyLimit) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.buy_over_limit.ID);
            return;
        }
        if (warCfg.buyCosts == null || warCfg.buyCosts.length == 0) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.config_data_not_found.ID);
            return;
        }
        int costIndex = Math.min(playerAct.getDailyBoughtCount(), warCfg.buyCosts.length - 1);
        int[] costPair = warCfg.buyCosts[costIndex];
        if (!PlayerHelper.isEnough(player, costPair)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.resource_not_enough.ID);
            return;
        }

        // 扣费前向 Cross 二次确认仍处于 START 争霸期且未发生换会锁定冲突
        CrossGuildWarPayloads.QueryDetailRequest detailReq = new CrossGuildWarPayloads.QueryDetailRequest();
        detailReq.setPlayerId(player.getPlayerId());
        detailReq.setCurrentGuildId(currentGuildId);
        detailReq.setLockedGuildId(playerAct.getLockedGuildId());
        CrossActivityActionResult actionRes = invokeCrossGuildWarAction(
                player, playerAct, CrossGuildWarActivity.ACTION_QUERY_DETAIL,
                JsonUtil.toJsonString(detailReq), false);
        if (!actionRes.isSuccess()) {
            client.sendProtocol(defaultInstance, actionRes.getErrorCode());
            return;
        }
        CrossGuildWarPayloads.QueryDetailResponse detail = JsonUtil.parseObject(
                actionRes.getResultPayloadJson(), CrossGuildWarPayloads.QueryDetailResponse.class);
        if (detail == null || detail.getState() != ActivityState.START_VALUE) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.not_open.ID);
            return;
        }
        if (detail.getLockedGuildId() > 0L && detail.getLockedGuildId() != currentGuildId) {
            playerAct.setLockedGuildId(detail.getLockedGuildId());
            client.sendProtocol(defaultInstance, ErrorMsgEnum.cross_guild_war_guild_locked.ID);
            return;
        }

        PlayerHelper.delResources(player, costPair, OpType.CrossGuildWar);
        playerAct.setDailyBoughtCount(playerAct.getDailyBoughtCount() + 1);
        GameLogger.activity(player, activityId, playerAct.getDailyBoughtCount());

        int remainingTimes = Math.max(0,
                warCfg.dailyFreeTimes + playerAct.getDailyBoughtCount() - playerAct.getDailyUsedCount());
        ActivityCrossGuildWarBuyCountResponse_11000508.Builder resp = ActivityCrossGuildWarBuyCountResponse_11000508.newBuilder();
        resp.setActivityId(activityId);
        resp.setDailyBoughtCount(playerAct.getDailyBoughtCount());
        resp.setRemainingTimes(remainingTimes);
        client.sendProtocol(resp.build());
    }

    private void crossGuildWarGuildRank(NetClient client, Object message) {
        ActivityCrossGuildWarGuildRankRequest_11000509 req = (ActivityCrossGuildWarGuildRankRequest_11000509) message;
        int activityId = req.getActivityId();
        int page = req.getPage();
        int pageSize = req.getPageSize();
        ActivityCrossGuildWarGuildRankResponse_1100050a defaultInstance = ActivityCrossGuildWarGuildRankResponse_1100050a.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        CrossGuildWarPlayerActivity playerAct = resolveOrOpenCrossGuildWar(
                client, player, activityId, defaultInstance, true, true);
        if (playerAct == null) {
            return;
        }
        if (page < 1 || pageSize < 1 || pageSize > 100) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_parameter_error.ID);
            return;
        }
        CrossGuildWarPayloads.QueryGuildRankRequest crossReq = new CrossGuildWarPayloads.QueryGuildRankRequest();
        crossReq.setPlayerId(player.getPlayerId());
        crossReq.setCurrentGuildId(player.getGuildId());
        crossReq.setPage(page);
        crossReq.setPageSize(pageSize);
        CrossActivityActionResult actionRes = invokeCrossGuildWarAction(
                player, playerAct, CrossGuildWarActivity.ACTION_QUERY_GUILD_RANK,
                JsonUtil.toJsonString(crossReq), true);
        if (!actionRes.isSuccess()) {
            client.sendProtocol(defaultInstance, actionRes.getErrorCode());
            return;
        }
        CrossGuildWarPayloads.QueryGuildRankResponse rankResp = JsonUtil.parseObject(
                actionRes.getResultPayloadJson(), CrossGuildWarPayloads.QueryGuildRankResponse.class);
        if (rankResp == null) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_remote_server.ID);
            return;
        }
        if (rankResp.getMyGuildEntry() != null && playerAct.getLockedGuildId() > 0L) {
            playerAct.setMyGuildTotalScore(rankResp.getMyGuildEntry().getTotalScore());
            playerAct.setMyGuildRank(rankResp.getMyGuildEntry().getRank());
        }
        ActivityCrossGuildWarGuildRankResponse_1100050a.Builder resp = ActivityCrossGuildWarGuildRankResponse_1100050a.newBuilder();
        resp.setActivityId(activityId);
        resp.setTotalCount(rankResp.getTotalCount());
        resp.setPage(rankResp.getPage());
        resp.setPageSize(rankResp.getPageSize());
        if (rankResp.getEntries() != null) {
            for (CrossGuildWarPayloads.GuildRankEntryPayload entry : rankResp.getEntries()) {
                resp.addEntries(toGuildRankProto(entry));
            }
        }
        resp.setMyGuildEntry(toGuildRankProto(rankResp.getMyGuildEntry()));
        client.sendProtocol(resp.build());
    }

    private void crossGuildWarMemberRank(NetClient client, Object message) {
        ActivityCrossGuildWarMemberRankRequest_1100050b req = (ActivityCrossGuildWarMemberRankRequest_1100050b) message;
        int activityId = req.getActivityId();
        long reqGuildId = req.getGuildId();
        int page = req.getPage();
        int pageSize = req.getPageSize();
        ActivityCrossGuildWarMemberRankResponse_1100050c defaultInstance = ActivityCrossGuildWarMemberRankResponse_1100050c.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        CrossGuildWarPlayerActivity playerAct = resolveOrOpenCrossGuildWar(
                client, player, activityId, defaultInstance, true, true);
        if (playerAct == null) {
            return;
        }
        if (page < 1 || pageSize < 1 || pageSize > 100) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_parameter_error.ID);
            return;
        }
        long targetGuildId = reqGuildId > 0L ? reqGuildId
                : (playerAct.getLockedGuildId() > 0L ? playerAct.getLockedGuildId() : player.getGuildId());
        if (targetGuildId <= 0L) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.cross_guild_war_no_locked_guild.ID);
            return;
        }
        CrossGuildWarPayloads.QueryMemberRankRequest crossReq = new CrossGuildWarPayloads.QueryMemberRankRequest();
        crossReq.setPlayerId(player.getPlayerId());
        crossReq.setTargetGuildId(targetGuildId);
        crossReq.setPage(page);
        crossReq.setPageSize(pageSize);
        CrossActivityActionResult actionRes = invokeCrossGuildWarAction(
                player, playerAct, CrossGuildWarActivity.ACTION_QUERY_MEMBER_RANK,
                JsonUtil.toJsonString(crossReq), true);
        if (!actionRes.isSuccess()) {
            client.sendProtocol(defaultInstance, actionRes.getErrorCode());
            return;
        }
        CrossGuildWarPayloads.QueryMemberRankResponse rankResp = JsonUtil.parseObject(
                actionRes.getResultPayloadJson(), CrossGuildWarPayloads.QueryMemberRankResponse.class);
        if (rankResp == null) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_remote_server.ID);
            return;
        }
        ActivityCrossGuildWarMemberRankResponse_1100050c.Builder resp = ActivityCrossGuildWarMemberRankResponse_1100050c.newBuilder();
        resp.setActivityId(activityId);
        resp.setGuildId(rankResp.getTargetGuildId());
        resp.setTotalCount(rankResp.getTotalCount());
        resp.setPage(rankResp.getPage());
        resp.setPageSize(rankResp.getPageSize());
        if (rankResp.getEntries() != null) {
            for (CrossGuildWarPayloads.MemberRankEntryPayload entry : rankResp.getEntries()) {
                resp.addEntries(toMemberRankProto(entry));
            }
        }
        resp.setMyMemberEntry(toMemberRankProto(rankResp.getMyMemberEntry()));
        client.sendProtocol(resp.build());
    }
}
