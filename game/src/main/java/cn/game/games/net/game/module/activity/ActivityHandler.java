package cn.game.games.net.game.module.activity;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import cn.game.protocol.generated.config.ActivityQingShenConfig;
import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.activity.impl.player.ActivityJQB;
import cn.game.games.net.game.module.activity.impl.player.ActivityLeiChong;
import cn.game.games.net.game.module.activity.impl.player.ActivityMeiRiBaoLi;
import cn.game.games.net.game.module.activity.impl.player.ActivityQingShen;
import cn.game.games.net.game.module.activity.impl.player.FirstChargeActivity;
import cn.game.games.net.game.module.activity.impl.player.SevenDayCarnivalActivity;
import cn.game.games.net.game.module.activity.impl.player.SevenDaysSignin;
import cn.game.games.net.game.module.recharge.PayType;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.config.FirstChargeConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.generated.manager.FirstChargeManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.ActivityMsg;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeBuyRequest_11000010;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeBuyResponse_11000011;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRequest_11000007;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeResponse_11000008;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardRequest_11000012;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardResponse_11000013;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.ActivityMsg.ActivityListResponse_11000002;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysCarnivalRequest_11000020;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysCarnivalResponse_11000021;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninInfoRequest_11000024;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninInfoResponse_11000025;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninRequest_11000026;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninResponse_11000027;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import io.vertx.core.Future;

/** 活动处理器 */
@Component
public class ActivityHandler extends BaseHandler {

  @Override
  protected int getModule() {
    return 0x11;
  }

  @Override
  protected void inititialize() {

    putInvoker(
        PbProtocol.ActivityListRequest_11000001,
        (client, message) -> {
          list(client, message);
        });
    //		putInvoker(PbProtocol.ActivityFirstChargeRequest_11000003, (client, message) -> {
    //			firstCharge(client, message);
    //		});
    //		putInvoker(PbProtocol.ActivityFirstChargeBuyRequest_11000005, (client, message) -> {
    //			firstChargeBuy(client, message);
    //		});
    putInvoker(
        PbProtocol.ActivityFirstChargeRequest_11000007,
        (client, message) -> {
          singleCharge(client, message);
        });
    putInvoker(
        PbProtocol.ActivityFirstChargeBuyRequest_11000010,
        (client, message) -> {
          singleChargeBuy(client, message);
        });
    putInvoker(
        PbProtocol.ActivityFirstChargeRewardRequest_11000012,
        (client, message) -> {
          singleChargeReward(client, message);
        });
    putInvoker(
        PbProtocol.ActivitySevenDaysCarnivalRequest_11000020,
        (client, message) -> {
          sevenDaysCarnival(client, message);
        });
    putInvoker(
        PbProtocol.ActivitySevenDaysSigninInfoRequest_11000024,
        (client, message) -> {
          sevenDaysSigninInfo(client, message);
        });
    putInvoker(
        PbProtocol.ActivitySevenDaysSigninRequest_11000026,
        (client, message) -> {
          sevenDaysSignin(client, message);
        });
    putInvoker(PbProtocol.ActivityLeiChongInfoRequest_11000051, this::getLeiChongInfo);
    putInvoker(PbProtocol.ActivityTaskRewardRequest_11000041, this::rewardActivityTask);
    putInvoker(PbProtocol.ActivityBaoLiInfoRequest_11000061, this::getBaoLiInfo);
    putInvoker(PbProtocol.ActivityQingShenInfoRequest_11000071, this::getQingShenInfo);
    putInvoker(PbProtocol.ActivityJQBInfoRequest_11000081, this::getJQBInfo);
  }

  private void empty(NetClient client, Object message) {
    ActivityFirstChargeBuyRequest_11000010 req = (ActivityFirstChargeBuyRequest_11000010) message;
    ActivityFirstChargeRewardResponse_11000013.Builder resp =
        ActivityFirstChargeRewardResponse_11000013.newBuilder();
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    FirstChargeActivity activityBase =
        (FirstChargeActivity) player.getActivityModule().get(req.getId());
    if (activityBase == null) {
      client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
      return;
    }

    client.sendProtocol(resp);
  }

  private void sevenDaysSigninInfo(NetClient client, Object message) {
    ActivitySevenDaysSigninInfoRequest_11000024 req =
        (ActivitySevenDaysSigninInfoRequest_11000024) message;
    ActivitySevenDaysSigninInfoResponse_11000025 resp =
        ActivitySevenDaysSigninInfoResponse_11000025.getDefaultInstance();
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
    ActivitySevenDaysSigninResponse_11000027.Builder resp =
        ActivitySevenDaysSigninResponse_11000027.newBuilder();
    int id = req.getId();
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
    if (activityBase.isSignin()) {
      client.sendProtocol(resp.build(), ErrorMsgEnum.repeat_request.getId());
      return;
    }
    resp.addAllRewards(activityBase.receive(0));
    client.sendProtocol(resp.build());
  }

  private void sevenDaysCarnival(NetClient client, Object message) {
    ActivitySevenDaysCarnivalRequest_11000020 req =
        (ActivitySevenDaysCarnivalRequest_11000020) message;
    ActivitySevenDaysCarnivalResponse_11000021.Builder resp =
        ActivitySevenDaysCarnivalResponse_11000021.newBuilder();
    int id = req.getId();
    ActivityConfig activityConfig = ActivityManager.instance().getNullable(id);
    if (activityConfig == null) {
      client.sendProtocol(resp.build(), ErrorMsgEnum.config_data_not_found.getId());
      return;
    }
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    SevenDayCarnivalActivity activityBase =
        (SevenDayCarnivalActivity) player.getActivityModule().get(id);
    if (activityBase == null) {
      client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
      return;
    }
    client.sendProtocol(activityBase.buildActivityShowInfo());
  }

  private void singleCharge(NetClient client, Object message) {
    ActivityFirstChargeRequest_11000007 req = (ActivityFirstChargeRequest_11000007) message;
    ActivityFirstChargeResponse_11000008.Builder resp =
        ActivityFirstChargeResponse_11000008.newBuilder();
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
	int id = req.getId();
    FirstChargeActivity activityBase =
			(FirstChargeActivity) player.getActivityModule().get(id);

    if (activityBase == null) {
      client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
      return;
    }

	client.sendProtocol(activityBase.buildActivityShowInfo(id));
  }

  private void singleChargeBuy(NetClient client, Object message) {
    ActivityFirstChargeBuyRequest_11000010 req = (ActivityFirstChargeBuyRequest_11000010) message;
    ActivityFirstChargeBuyResponse_11000011.Builder resp =
        ActivityFirstChargeBuyResponse_11000011.newBuilder();
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

	Future<Boolean> pay = player.pay(PayType.FirstCharge, chargeId, firstChargeConfig.Price,id);
    pay.onComplete(
        t -> {
          if (t.result()) {
            activityBase.buy(chargeId);
            client.sendProtocol(resp.build());
          } else {
            client.sendProtocol(resp, ErrorMsgEnum.unknown.getId());
          }
        });
  }

  private void singleChargeReward(NetClient client, Object message) {
    ActivityFirstChargeRewardRequest_11000012 req =
        (ActivityFirstChargeRewardRequest_11000012) message;
    ActivityFirstChargeRewardResponse_11000013.Builder resp =
        ActivityFirstChargeRewardResponse_11000013.newBuilder();
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    int chargeId = req.getChargeId();
    FirstChargeActivity activityBase =
        (FirstChargeActivity) player.getActivityModule().get(req.getId());
    if (activityBase == null) {
      client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
      return;
    }
    //		SingleCharge singleCharge = activityBase.getSingleCharge(chargeId);
    //		if (singleCharge == null) {
    //			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
    //			return;
    //		}
    //		if (rewardDay > DateUtil.getDay() - singleCharge.getDay()) {
    //			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
    //			return;
    //		}
    //		if (singleCharge.getSelectedIndex().contains(rewardDay)) {
    //			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
    //			return;
    //		}
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
    Collection<ActivityInfo> activityInfos = ActivityStateManager.getInstance().getShowState();
    Map<Integer, ActivityInfo> playerState = player.getActivityModule().getShowState();
    for (ActivityInfo activityInfo : activityInfos) {
      if (!playerState.containsKey(activityInfo.getId())) {
        playerState.put(activityInfo.getId(), activityInfo);
      }
    }
    resp.addAllActivitys(playerState.values());
    client.sendProtocol(resp);
  }

  private void getLeiChongInfo(NetClient client, Object o) {
    ActivityMsg.ActivityLeiChongInfoRequest_11000051 req =
        (ActivityMsg.ActivityLeiChongInfoRequest_11000051) o;
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    ActivityMsg.ActivityLeiChongInfoResponse_11000052.Builder res =
        ActivityMsg.ActivityLeiChongInfoResponse_11000052.newBuilder();
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
    ActivityMsg.ActivityTaskRewardRequest_11000041 req =
        (ActivityMsg.ActivityTaskRewardRequest_11000041) o;
    ActivityMsg.ActivityTaskRewardResponse_11000042.Builder res =
        ActivityMsg.ActivityTaskRewardResponse_11000042.newBuilder();
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
      req.getTaskIdsList()
          .forEach(
              taskId -> {
                Future<List<RewardInfo>> rewardFuture = activityBase.asyncReceive(taskId);
                var completableFuture =  rewardFuture.toCompletionStage().toCompletableFuture();
                allFuture.add(completableFuture);
                completableFuture.whenComplete((v, t) -> {
                      res.addAllRewards(v);
                });
              });
      CompletableFuture.allOf(allFuture.toArray(new CompletableFuture[0]))
          .thenAccept(
              action -> {
                activityBase.checkRefreshActivity();
                client.sendProtocol(res.build());
              });
    } else {

      // 请神任务 每轮最后一个任务完成之后 自动领取每轮回奖励的任务
      List<Integer> rewardTaskIds = new ArrayList<>(req.getTaskIdsList());
      if (activityBase instanceof ActivityQingShen activityQingShen) {
        List<ActivityQingShenConfig> roundConfigList = activityQingShen.getRoundConfigList();
        if (rewardTaskIds.contains(roundConfigList.get(roundConfigList.size() - 2).taskID) && !rewardTaskIds.contains(roundConfigList.get(roundConfigList.size() - 1).taskID)){
           rewardTaskIds.add(roundConfigList.get(roundConfigList.size() - 1).taskID);
        }
      }
        Collections.sort(rewardTaskIds);
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
    ActivityMsg.ActivityBaoLiInfoRequest_11000061 req =
        (ActivityMsg.ActivityBaoLiInfoRequest_11000061) o;
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    ActivityMsg.ActivityBaoLiInfoResponse_11000062.Builder res =
        ActivityMsg.ActivityBaoLiInfoResponse_11000062.newBuilder();
    ActivityModule activityModule = player.getActivityModule();
    if (activityModule.get(req.getActivityId()) == null) {
      client.sendProtocol(res, ErrorMsgEnum.activity_not_found.getId());
      return;
    }
    ActivityMeiRiBaoLi baoLi = (ActivityMeiRiBaoLi) activityModule.get(req.getActivityId());
    client.sendProtocol(baoLi.buildActivityShowInfo());
  }

  private void getQingShenInfo(NetClient client, Object o) {
    ActivityMsg.ActivityQingShenInfoRequest_11000071 req =
        (ActivityMsg.ActivityQingShenInfoRequest_11000071) o;
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    ActivityMsg.ActivityQingShenInfoResponse_11000072.Builder res =
        ActivityMsg.ActivityQingShenInfoResponse_11000072.newBuilder();
    ActivityModule activityModule = player.getActivityModule();
    if (activityModule.get(req.getActivityId()) == null) {
      client.sendProtocol(res, ErrorMsgEnum.activity_not_found.getId());
      return;
    }
    ActivityQingShen qingShen = (ActivityQingShen) activityModule.get(req.getActivityId());
    client.sendProtocol(qingShen.buildActivityShowInfo());
  }

  private void getJQBInfo(NetClient client, Object o) {
    ActivityMsg.ActivityJQBInfoRequest_11000081 req =
        (ActivityMsg.ActivityJQBInfoRequest_11000081) o;
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    ActivityMsg.ActivityJQBInfoResponse_11000082.Builder res =
        ActivityMsg.ActivityJQBInfoResponse_11000082.newBuilder();
    ActivityModule activityModule = player.getActivityModule();
    if (activityModule.get(req.getActivityId()) == null) {
      client.sendProtocol(res, ErrorMsgEnum.activity_not_found.getId());
      return;
    }
    ActivityJQB activityJQB = (ActivityJQB) activityModule.get(req.getActivityId());
    client.sendProtocol(activityJQB.buildActivityShowInfo());
  }
}
