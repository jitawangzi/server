package cn.game.games.net.game.module.zongmen;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.cross.remote.CrossServerInterface;
import cn.game.games.net.cross.zongmen.SimpleZongMen;
import cn.game.games.net.cross.zongmen.ZongMenHelper;
import cn.game.games.net.game.GameServer;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.rank.RankEntry;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.GuildBargainConfig;
import cn.game.protocol.generated.config.QuestPointRewardConfig;
import cn.game.protocol.generated.config.ShopItemConfig;
import cn.game.protocol.generated.config.ZongmenStoreConfig;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.GuildBargainManager;
import cn.game.protocol.generated.manager.QuestPointRewardManager;
import cn.game.protocol.generated.manager.ShopItemManager;
import cn.game.protocol.generated.manager.VirtualServerManager;
import cn.game.protocol.generated.manager.ZongmenStoreManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.ZongMenCrossMsg.ZongMenMsgRequest_41000045;
import cn.game.protocol.protobuf.ZongMenCrossMsg.ZongMenMsgResponse_41000046;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.util.DateUtil;
import cn.game.util.ServerType;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * @ClassName ZongMenHandler
 *
 * @description: 宗门handler
 * @author: ly
 * @create: 2025-02-06 17:27 @Version 1.0
 */
@Component
public class ZongMenGameHandler extends BaseHandler {
   static Logger log = LoggerFactory.getLogger(ZongMenGameHandler.class);
    public static Future<ZongMenCallbackMsg> sendMsgToZongMenServer( long zongMenId,
            Player player, Message req, String... params) {
// 封装宗门请求
		ZongMenMsgRequest_41000045.Builder serverReq =
				ZongMenMsgRequest_41000045.newBuilder();
        // 设置请求参数
        // 宗门ids
        serverReq.setZongMenId(zongMenId);
        // 玩家id
        serverReq.setPlayerId(player.getPlayerId());
        // 请求消息
        serverReq.setData(req.toByteString());
        // TODO 上下文设置 后续日志记录
        if (params != null && params.length > 0) {
            for (String param : params) {
                serverReq.addParams(param);
            }
        }
        int reqMsgId = PbProtocol.getInstance().getMsgId(req.getClass().getSimpleName());
        serverReq.setMsgId(reqMsgId);
        // 回包id
        final int responseMsgId = reqMsgId + 1;
        Promise<ZongMenCallbackMsg> future = Promise.promise();
        // 异步RPC请求
		Future<ZongMenMsgResponse_41000046> rpcFuture;
		if (player.getZongMenId() == 0) { // 宗门不存在 创建宗门 随机找一个节点
            rpcFuture = VxHolder.requestRemoteServer(ServerType.Cross, serverReq.build());
        } else {
            rpcFuture = VxHolder.requestRemoteServer(ZongMenHelper.getServerIdByZongMenId(player.getZongMenId()), serverReq.build());
        }
//            rpcFuture = VxHolder.requestRemoteServer("LY_ZONG_MEN", serverReq.build());

        rpcFuture
                .onSuccess( // 请求成功
                        result -> {
                            if (result != null) {
                                log.info(String.format("sendMsgToZongMenServer callBack msgId:%d %s, errorCode:%d, pid:%d ",
                                        result.getMsgId(),req.getClass().getSimpleName(), result.getErrorCode(), result.getPlayerId()));
								ZongMenMsgResponse_41000046 serverResponse = (ZongMenMsgResponse_41000046) result;

                                if (serverResponse.getErrorCode() == ErrorMsgEnum.ok.ID) {
                                    Message response =
                                            PbProtocol.getInstance()
                                                    .parseFrom(responseMsgId, serverResponse.getData().toByteArray());
                                    log.info(String.format("response:%s",response));
                                    // 异步请求成功 封装 proto 信息和错误码 回调
                                    future.complete(new ZongMenCallbackMsg(serverResponse.getErrorCode(), response));
                                } else {
                                    // 异步请求失败 封装 错误码 回调
                                    future.complete(new ZongMenCallbackMsg(serverResponse.getErrorCode(), null));
                                }
                            } else {
                                future.complete(new ZongMenCallbackMsg(ErrorMsgEnum.unknown.ID, null));
                            }
                        })
                .onFailure(
                        err -> {
                            future.complete(new ZongMenCallbackMsg(ErrorMsgEnum.unknown.ID, null));
                            err.printStackTrace();
                        });
        return future.future();
    }
  public static Future<ZongMenCallbackMsg> sendMsgToZongMenServer(
      Player player, Message req, String... params) {
        return sendMsgToZongMenServer(player.getZongMenId(), player, req, params);
  }

  @Override
  protected void inititialize() {
    putInvoker(PbProtocol.getZongMenListRequest_40000001, this::zongMenList);
    putInvoker(PbProtocol.findZongMenRequest_40000003, this::findZongMen);
    putInvoker(PbProtocol.createZongMenRequest_40000005, this::createZongMen);
    putInvoker(PbProtocol.applyJoinZongMenRequest_40000007, this::applyJoinZongMen);
    putInvoker(PbProtocol.dissolveZongMenRequest_40000011, this::dissolveZongMen);
    putInvoker(PbProtocol.setZongMenSettingRequest_40000013, this::setZongMenSetting);
    putInvoker(PbProtocol.setZongMenMemberPositionRequest_40000015, this::setZongMenMemberPosition);
    putInvoker(PbProtocol.quitZongMenRequest_40000017, this::quitZongMen);
    putInvoker(PbProtocol.getZongMenInfoRequest_40000021, this::getZongMenInfo);
    putInvoker(PbProtocol.getZongMenLogRequest_40000025, this::getZongMenLogs);
    putInvoker(PbProtocol.updateMemberAuthRequest_40000041, this::updateMemberAuth);
    putInvoker(PbProtocol.ZongMenActiveRewardRequest_40000045, this::rewardLiveness);
    putInvoker(PbProtocol.getZongMenShopRequest_40000027, this::getZongMenShop);
    putInvoker(PbProtocol.ZongMenBuyShopRequest_40000047, this::buyZongMenShop);
	putInvoker(PbProtocol.ZongMenBargainRequest_40000060, this::bargain);
	putInvoker(PbProtocol.ZongMenBargainBuyRequest_40000062, this::buyBargain);
      putInvoker(PbProtocol.ZongMenGetMyApplyZongMenIdListRequest_40000055, this::getMyApplyZongMenIdList);

  }

    @Override
  protected int getModule() {
    return 0x40;
  }
    // 获取申请过的宗门列表
    private void getMyApplyZongMenIdList(NetClient client, Object o) {
        ZongMenMsg.ZongMenGetMyApplyZongMenIdListRequest_40000055 req =
                (ZongMenMsg.ZongMenGetMyApplyZongMenIdListRequest_40000055) o;
        ZongMenMsg.ZongMenGetMyApplyZongMenIdListResponse_40000056.Builder res =
                ZongMenMsg.ZongMenGetMyApplyZongMenIdListResponse_40000056.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        ZongMenModule zongMenModule = player.getZongmenModule();
        if (player.getZongMenId() != 0 || zongMenModule.applyJoinList.isEmpty()) {
            client.sendProtocol(res.build());
            return;
        }
        ZongMenHelper.getSimpleZongMenListAsync(zongMenModule.getApplyJoinList())
                .thenAccept(resultList -> {
                    if (resultList == null){// 没有申请宗门
                        zongMenModule.getApplyJoinList().clear();
                        client.sendProtocol(res.build());
                        return;
                    }
                    resultList.forEach(obj -> {
                        SimpleZongMen simpleZongMen = (SimpleZongMen) obj;
                        if (!simpleZongMen.getApplyPidList().contains(player.getPlayerId())){
                            zongMenModule.removeApplyJoinList(simpleZongMen.getId());
                        }
                    });
                    zongMenModule.getApplyJoinList().forEach(zongMenId ->{
                        res.addZongMenIdList(zongMenId.intValue());
                    });
                    client.sendProtocol(res.build());
                })
                .exceptionally(
                        err -> {
                            client.sendProtocol(res.build(), ErrorMsgEnum.unknown.ID);
                            err.printStackTrace();
                            return null;
                        });
    }

	private void bargain(NetClient client, Object o) {
		ZongMenMsg.ZongMenBargainRequest_40000060 req = (ZongMenMsg.ZongMenBargainRequest_40000060) o;
		ZongMenMsg.ZongMenBargainResponse_40000061.Builder res = ZongMenMsg.ZongMenBargainResponse_40000061.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		ZongMenModule zongmenModule = player.getZongmenModule();
		long joinTime = zongmenModule.getJoinTime();
		if (zongmenModule.getDisbandCount() > 0 && System.currentTimeMillis() - joinTime < GlobalConst.ZongmenBargainCD * 1000) {
			client.sendProtocol(res.build(), ErrorMsgEnum.cd_time_error.ID);
			return;
		}
		autoForwardZongMenServer(client, res, req, result->{
            player.handleEvent(EventTypeEnum.ZongMenBargain);
            client.sendProtocol(result);
            return null;
        });
	}

	private void buyBargain(NetClient client, Object o) {
		ZongMenMsg.ZongMenBargainBuyRequest_40000062 req = (ZongMenMsg.ZongMenBargainBuyRequest_40000062) o;
		ZongMenMsg.ZongMenBargainBuyResponse_40000063.Builder res = ZongMenMsg.ZongMenBargainBuyResponse_40000063.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (player.getZongMenId() == 0) {
			client.sendProtocol(res.build(), ErrorMsgEnum.illegal_request.ID);
			return;
		}
		CrossServerInterface crossServerInterface = GameServer.getInstance()
				.getCrossServerInterface(DistributedObjectType.ZONGMEN, player.getZongMenId());
		Future<Integer> priceFuture = crossServerInterface.zongmenBargainPrice(player.getZongMenId());
		priceFuture.map(price -> {
			GuildBargainConfig guildBargainConfig = GuildBargainManager.instance().get(1);
			if (price > 0) {
				if (PlayerHelper.isEnough(player, guildBargainConfig.Price[0], price)) {
					Future<Boolean> buyZongmenBargain = crossServerInterface.buyZongmenBargain(player.getZongMenId(), player.getPlayerId());
					buyZongmenBargain.onSuccess(result -> {
						if (result) {
							boolean delResources = PlayerHelper.delResources(player, guildBargainConfig.Price[0], price,
									OpType.ZongMenBargain);
							if (!delResources) {
								client.sendProtocol(res.build(), ErrorMsgEnum.resource_not_enough.ID);
								return;
							}
							List<RewardInfo> rewards = PlayerHelper.addResources(player, guildBargainConfig.Item, OpType.ZongMenBargain);
							res.addAllRewards(rewards);
							client.sendProtocol(res.build());
						} else {
							client.sendProtocol(res.build(), ErrorMsgEnum.illegal_request.ID);
						}
					}).onFailure(player::handleFail);
				} ; 

			}
			return null;
		}).onFailure(player::handleFail);

	}

	private void buyZongMenShop(NetClient client, Object o) {
      ZongMenMsg.ZongMenBuyShopRequest_40000047 req = (ZongMenMsg.ZongMenBuyShopRequest_40000047) o;
      ZongMenMsg.ZongMenBuyShopResponse_40000048.Builder res =
          ZongMenMsg.ZongMenBuyShopResponse_40000048.newBuilder();
      Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
      autoForwardZongMenServer(client,res,req,(result)->{
          int itemId = req.getItemId();
          int count = req.getCount();
          ZongmenStoreConfig config = ZongmenStoreManager.instance().get(itemId);
          ShopItemConfig itemConfig = ShopItemManager.instance().get(config.Item);
          int[][] drops = itemConfig.Item;
          for(int i = 0; i < drops.length; i++) {
              drops[i][1] = drops[i][1] * count;
          }
            res.addAllDrops(PlayerHelper.addResources(player,drops, OpType.ZongMenShopReward));
            client.sendProtocol(res.build());
          return null;
      });
    }

  //获取宗门商店
    private void getZongMenShop(NetClient client, Object o) {
      ZongMenMsg.getZongMenShopRequest_40000027 req = (ZongMenMsg.getZongMenShopRequest_40000027) o;
      ZongMenMsg.getZongMenShopResponse_40000028.Builder res =
          ZongMenMsg.getZongMenShopResponse_40000028.newBuilder();
      autoForwardZongMenServer(client,res,req,null);
    }

    //领取任务活跃度奖励
    private void rewardLiveness(NetClient client, Object o) {
      ZongMenMsg.ZongMenActiveRewardRequest_40000045 req = (ZongMenMsg.ZongMenActiveRewardRequest_40000045) o;
      ZongMenMsg.ZongMenActiveRewardResponse_40000046.Builder res =
          ZongMenMsg.ZongMenActiveRewardResponse_40000046.newBuilder();
      Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        QuestPointRewardConfig questPointRewardConfig = QuestPointRewardManager.instance().get(6);
        for (int index : req.getIndexListList()) {
            if (index > questPointRewardConfig.Reward.length) {
                client.sendProtocol(res.build(), ErrorMsgEnum.request_parameter_error.ID);
                return;
            }
        }
        if (player.getZongMenId() == 0) {
          client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_not_exist.ID);
          return;
      }

        autoForwardZongMenServer(client,res,req, (Void)->{
        req.getIndexListList().forEach(index -> {
            int[] drop = questPointRewardConfig.Reward[index];
            res.addAllDrops(PlayerHelper.addResources(player,drop, OpType.ZongMenActive));
        });
        client.sendProtocol(res.build());
         return null;
      });
    }

    private void updateMemberAuth(NetClient client, Object o) {
      ZongMenMsg.updateMemberAuthRequest_40000041 req = (ZongMenMsg.updateMemberAuthRequest_40000041) o;
      ZongMenMsg.updateMemberAuthResponse_40000042.Builder res =
          ZongMenMsg.updateMemberAuthResponse_40000042.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (player.getZongMenId() == 0) {
            client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_not_exist.ID);
            return;
        }
        //不能审批自己
    if (req.getTargetPidListList().contains(player.getPlayerId())){
        client.sendProtocol(res.build(), ErrorMsgEnum.request_parameter_error.ID);
        return;
    }
    if (req.getOptType() != 1 && req.getOptType() != 2 && req.getOptType() != 3) {
        client.sendProtocol(res.build(), ErrorMsgEnum.request_parameter_error.ID);
        return;
    }
      sendMsgToZongMenServer(player, req, player.getPlayerName())
          .onSuccess(
              callBack -> {
                if (callBack.errorCode != ErrorMsgEnum.ok.ID) {
                  client.sendProtocol(res.build(), callBack.errorCode);
                } else {
                  client.sendProtocol(callBack.response);
                }
              })
          .onFailure(
              err -> {
                err.printStackTrace();
                client.sendProtocol(res, ErrorMsgEnum.zong_men_not_exist.ID);
              });
    }

  private void getZongMenLogs(NetClient client, Object o) {
    ZongMenMsg.getZongMenLogRequest_40000025 req =
        (ZongMenMsg.getZongMenLogRequest_40000025) o;
    ZongMenMsg.getZongMenLogResponse_40000026.Builder res =
        ZongMenMsg.getZongMenLogResponse_40000026.newBuilder();
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

    if (player.getZongMenId() == 0) {
      client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_not_exist.ID);
      return;
    }
    autoForwardZongMenServer(client, res, req, null);
  }

  private void getZongMenInfo(NetClient client, Object o) {
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    ZongMenMsg.getZongMenInfoResponse_40000022.Builder res =
        ZongMenMsg.getZongMenInfoResponse_40000022.newBuilder();
    ZongMenMsg.getZongMenInfoRequest_40000021 req = (ZongMenMsg.getZongMenInfoRequest_40000021) o;
    if (player.getZongMenId() == 0) {
      client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_not_exist.ID);
      return;
    }
    sendMsgToZongMenServer(player, req)
        .onSuccess(
            callBack -> {
              if (callBack.errorCode != ErrorMsgEnum.ok.ID) {
                client.sendProtocol(res.build(), callBack.errorCode);
              } else {
                client.sendProtocol(callBack.response);
              }
            })
        .onFailure(
            err -> {
              err.printStackTrace();
              client.sendProtocol(res, ErrorMsgEnum.zong_men_not_exist.ID);
            });
  }

  private void autoForwardZongMenServer(
      NetClient client, Message.Builder res, Message req, Function<Message, Void> successCallBack) {
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    if (player.getZongMenId() == 0) {
      client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_not_exist.ID);
      return;
    }
    sendMsgToZongMenServer(player, req, player.getPlayerName())
        .onSuccess(
            callBack -> {
              if (callBack.errorCode != ErrorMsgEnum.ok.ID) {
                client.sendProtocol(res.build(), callBack.errorCode);
              } else {
                if (successCallBack != null) {
                  successCallBack.apply(callBack.response);
                } else {
                  client.sendProtocol(callBack.response);
                }
              }
            })
        .onFailure(
            err -> {
              err.printStackTrace();
              client.sendProtocol(res, ErrorMsgEnum.zong_men_not_exist.ID);
            });
  }

  private void quitZongMen(NetClient client, Object o) {
    ZongMenMsg.quitZongMenRequest_40000017 req = (ZongMenMsg.quitZongMenRequest_40000017) o;
    ZongMenMsg.quitZongMenResponse_40000018.Builder res =
        ZongMenMsg.quitZongMenResponse_40000018.newBuilder();
    autoForwardZongMenServer(
        client,
        res,
        req,
        (result) -> { // 退出成功
          Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
          player.getZongmenModule().clearZongMen();
          player
              .getZongmenModule()
              .setDisbandCount(player.getZongmenModule().getDisbandCount() + 1);
          // 第二次及后续退出时，宗主需要1小时才可加入其它宗门（ZongmenSuzerainCD）；
          if (player.getZongmenModule().getDisbandCount() > 1) {
            player
                .getZongmenModule()
                .setNextJoinTimer(
                    System.currentTimeMillis()
                        + GlobalConst.ZongmenMemberCD * DateUtil.HOUR_MILLIS);
          }
          client.sendProtocol(result);
          return null;
        });
  }

  private void setZongMenMemberPosition(NetClient client, Object o) {
    ZongMenMsg.setZongMenMemberPositionRequest_40000015 req =
        (ZongMenMsg.setZongMenMemberPositionRequest_40000015) o;
    ZongMenMsg.setZongMenMemberPositionResponse_40000016.Builder res =
        ZongMenMsg.setZongMenMemberPositionResponse_40000016.newBuilder();
    autoForwardZongMenServer(client, res, req, null);
  }

  private void setZongMenSetting(NetClient client, Object o) {
    ZongMenMsg.setZongMenSettingRequest_40000013 req =
        (ZongMenMsg.setZongMenSettingRequest_40000013) o;
    ZongMenMsg.setZongMenSettingResponse_40000014.Builder res =
        ZongMenMsg.setZongMenSettingResponse_40000014.newBuilder();
      Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

    List<String> checkStrs = new ArrayList<>();
    if (!StringUtils.isEmpty(req.getName())){
        //- 宗门名称：需要花费500元宝（ZongmenNameRevise），最多输入6个字；
        if (req.getName().length() > 6) {
            client.sendProtocol(res.build(), ErrorMsgEnum.not_name.ID);
            return;
        }
        if (!player.isEnough( GlobalConst.ZongmenNameRevise[0], GlobalConst.ZongmenNameRevise[1])){
            client.sendProtocol(res.build(), ErrorMsgEnum.resource_not_enough.ID);
            return;
        }
            checkStrs.add(req.getName());

    }
      if (!StringUtils.isEmpty(req.getNotice())){
          checkStrs.add(req.getNotice());
      }

      if (!StringUtils.isEmpty(req.getDeclaration())){
          checkStrs.add(req.getDeclaration());
      }
      if (!StringUtils.isEmpty(req.getWx())){
          checkStrs.add(req.getWx());
      }
      //非法字符串检测
      List<CompletableFuture<Boolean>> checkComplatableList = new ArrayList<>();
      for (String str : checkStrs) {
          checkComplatableList.add((CompletableFuture<Boolean>) PlayerHelper.checkContextData(player,str).toCompletionStage());
      }
      CompletableFuture.allOf(checkComplatableList.toArray(new CompletableFuture[0]))
          .thenAcceptAsync(
              result -> {
                if (checkComplatableList.stream().anyMatch(CompletableFuture::isCompletedExceptionally)) {
                  client.sendProtocol(res.build(), ErrorMsgEnum.unknown.ID);
                  return;
                }
                for (CompletableFuture<Boolean> future : checkComplatableList) {
                    try {
                        if (future.get().booleanValue() == false) {
                            client.sendProtocol(res.build(), ErrorMsgEnum.we_chat_context_check_fail.ID);
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        client.sendProtocol(res.build(), ErrorMsgEnum.unknown.ID);
                        return;
                    }
                }
                autoForwardZongMenServer(client, res, req, message -> {
                    if (!StringUtils.isEmpty(req.getName())){//宗门改名 扣除资源
                        PlayerHelper.delResources(player,GlobalConst.ZongmenNameRevise,OpType.zongMenChangeName);
                    }
                    res.setResult(true);
                    client.sendProtocol(res);
                    return null;
                });
              });
  }

  //已废弃 不要主动解散宗门了
  @Deprecated
  private void dissolveZongMen(NetClient client, Object o) {
    ZongMenMsg.dissolveZongMenRequest_40000011 req = (ZongMenMsg.dissolveZongMenRequest_40000011) o;
    ZongMenMsg.dissolveZongMenResponse_40000012.Builder res =
        ZongMenMsg.dissolveZongMenResponse_40000012.newBuilder();
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    if (player.getZongMenId() == 0) {
      client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_not_exist.ID);
      return;
    }
    sendMsgToZongMenServer(player, req, player.getZongMenName())
        .onSuccess(
            callBack -> {
              if (callBack.errorCode != ErrorMsgEnum.ok.ID) {
                client.sendProtocol(res.build(), callBack.errorCode);
              } else {
                // 解散宗门成功
                ZongMenModule module = player.getZongmenModule();
                module.clearZongMen();
                module.setDisbandCount(module.getDisbandCount() + 1);
                // 第二次及后续解散时，宗主需要1小时才可加入其它宗门（ZongmenSuzerainCD）；
                if (module.getDisbandCount() > 1) {
                  module.setNextJoinTimer(
                      System.currentTimeMillis()
                          + GlobalConst.ZongmenSuzerainCD * DateUtil.HOUR_MILLIS);
                }

                client.sendProtocol(callBack.response);
              }
            })
        .onFailure(
            err -> {
              err.printStackTrace();
              client.sendProtocol(res, ErrorMsgEnum.zong_men_not_exist.ID);
            });
  }

  private void applyJoinZongMen(NetClient client, Object o) {
    ZongMenMsg.applyJoinZongMenRequest_40000007 req =
        (ZongMenMsg.applyJoinZongMenRequest_40000007) o;
    ZongMenMsg.applyJoinZongMenResponse_40000008.Builder res =
        ZongMenMsg.applyJoinZongMenResponse_40000008.newBuilder();
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    if (player.getZongMenId() != 0) {
      client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_exist.ID);
      return;
    }
    long nextJoinTimer = player.getZongmenModule().getNextJoinTimer();
    if (nextJoinTimer != 0 && System.currentTimeMillis() < nextJoinTimer) {
      client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_apply_join_timer.ID);
      return;
    }
    sendMsgToZongMenServer(req.getId(),
            player, req, player.getAttrModule().getPower() + "", player.getPlayerName())
        .onSuccess(
            callBack -> {
              if (callBack.errorCode != ErrorMsgEnum.ok.ID) {
                client.sendProtocol(res.build(), callBack.errorCode);
              } else {
                  ZongMenMsg.applyJoinZongMenResponse_40000008 applyRes = (ZongMenMsg.applyJoinZongMenResponse_40000008) callBack.response;
                  if (applyRes.hasZongMen()){//玩家直接加入宗门
                      player.getZongmenModule().setZongMenInfo(applyRes.getZongMen());
                      player.getZongmenModule().refreshZongMenTask();
                      player.getShopModule().refreshZongMenShop();
                  }
                client.sendProtocol(callBack.response);
              }
            })
        .onFailure(
            err -> {
              err.printStackTrace();
              client.sendProtocol(res, ErrorMsgEnum.zong_men_not_exist.ID);
            });
  }

  private void createZongMen(NetClient client, Object o) {
    ZongMenMsg.createZongMenRequest_40000005 req = (ZongMenMsg.createZongMenRequest_40000005) o;
    ZongMenMsg.createZongMenResponse_40000006.Builder res =
        ZongMenMsg.createZongMenResponse_40000006.newBuilder();
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    if (player.getZongMenId() != 0) {
      client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_exist.ID);
      return;
    }
    long nextJoinTimer = player.getZongmenModule().getNextJoinTimer();
    if (nextJoinTimer != 0 && System.currentTimeMillis() < nextJoinTimer) {
      client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_apply_join_timer.ID);
      return;
    }
    String name = req.getName();
    List<String> checkStrs = new ArrayList<>();
    if (!player.isEnough(
        GlobalConst.ZongmenCreationConsume[0], GlobalConst.ZongmenCreationConsume[1])) {
      client.sendProtocol(res.build(), ErrorMsgEnum.resource_not_enough.ID);
      return;
    }

    if (name == null || name.length() > GlobalConst.ZongmenName) {
      client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_name_repeat.ID);
      return;
    }
    if (!StringUtils.isEmpty(req.getNotice())) {
      checkStrs.add(req.getNotice());
    }

    if (!StringUtils.isEmpty(req.getDeclaration())) {
      checkStrs.add(req.getDeclaration());
    }
    checkStrs.add(name);
    Future<Boolean> checkFuture = PlayerHelper.checkContextData(player, name);

    // 非法字符串检测
    List<CompletableFuture<Boolean>> checkComplatableList = new ArrayList<>();
    for (String str : checkStrs) {
      checkComplatableList.add(
          (CompletableFuture<Boolean>)
              PlayerHelper.checkContextData(player, str).toCompletionStage());
    }
    // 检查宗门名称是否重复
    checkComplatableList.add(ZongMenHelper.checkZongMenNameRepeat(player, name));
    CompletableFuture.allOf(checkComplatableList.toArray(new CompletableFuture[0]))
        .thenAcceptAsync(
            result -> {
              if (checkComplatableList.stream()
                  .anyMatch(CompletableFuture::isCompletedExceptionally)) {
                client.sendProtocol(res.build(), ErrorMsgEnum.unknown.ID);
                return;
              }
              for (CompletableFuture<Boolean> future : checkComplatableList) {
                try {
                  if (future.get().booleanValue() == false) {
                    client.sendProtocol(res.build(), ErrorMsgEnum.we_chat_context_check_fail.ID);
                    return;
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  client.sendProtocol(res.build(), ErrorMsgEnum.unknown.ID);
                  return;
                }
              }
              // 创建宗门
              sendMsgToZongMenServer(
                      player,
                      req,
                      player.getPlayerName(),
                      player.getAttrModule().getPower() + "",
                      player.getServerId())
                  .onSuccess(
                      createZongMenCallback -> {
                        if (createZongMenCallback.errorCode == ErrorMsgEnum.ok.ID) { // 创建宗门成功
                          PlayerHelper.delResources(
                              player, GlobalConst.ZongmenCreationConsume, OpType.zongMenChangeName);
                          ZongMenMsg.createZongMenResponse_40000006 createRes =
                              (ZongMenMsg.createZongMenResponse_40000006)
                                  createZongMenCallback.response;
                          // 设置玩家宗门信息
                          player.getZongmenModule().setZongMenInfo(createRes.getZongMen());
                          player.getZongmenModule().refreshZongMenTask();
                          player.getShopModule().refreshZongMenShop();
                          client.sendProtocol(createRes);
                        } else { // 创建宗门失败
                          client.sendProtocol(res.build(), createZongMenCallback.errorCode);
                        }
                      })
                  // 创建宗门异常
                  .onFailure(
                      err -> {
                        err.printStackTrace();
                        client.sendProtocol(res, ErrorMsgEnum.unknown.getId());
                      });
            });
  }

  private void findZongMen(NetClient client, Object o) {
    ZongMenMsg.findZongMenRequest_40000003 req = (ZongMenMsg.findZongMenRequest_40000003) o;
    ZongMenMsg.findZongMenResponse_40000004.Builder res =
        ZongMenMsg.findZongMenResponse_40000004.newBuilder();
    long zongMenId = req.getId();
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    sendMsgToZongMenServer(zongMenId,player,req).onSuccess(result ->{
        if (result.errorCode == ErrorMsgEnum.ok.ID){
            client.sendProtocol(result.response);
        }else{
            client.sendProtocol(res.build(), result.errorCode);
        }
    }).onFailure(err ->{
    client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_not_exist.ID);
    err.printStackTrace();
    });
  }

  private void zongMenList(NetClient client, Object o) {
    ZongMenMsg.getZongMenListRequest_40000001 req = (ZongMenMsg.getZongMenListRequest_40000001) o;
    ZongMenMsg.getZongMenListResponse_40000002.Builder res =
        ZongMenMsg.getZongMenListResponse_40000002.newBuilder();
    final int page = req.getPage();
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    RankType rankType = RankType.ZongMen;
    res.setPage(page);
    // 宗门总数
    CompletionStage<Integer> rankSizeStage =
        RankService.getInstance().getRankSizeAsync(player.getServerId(), rankType);
    // 查询指定页码的战斗力宗门数据
    CompletionStage<List<RankEntry>> zongMenListStage =
			RankService.getInstance().getTopNAsync(player.getServerId(), rankType, 30);
    // 获取宗门 simpleZongMen 列表
        zongMenListStage.thenCombine(
            rankSizeStage,
            (zongMenRankList, rankSize) -> {
              List<Long> zongMenIdList =
                  zongMenRankList.stream().map(RankEntry::getPlayerId).collect(Collectors.toList());
              ZongMenHelper.getSimpleZongMenListAsync(zongMenIdList).thenAccept(list -> {
                  int readRankSize = rankSize;
                  if (list != null){
                      int i = 0;
                      for (Object simpleZongMen : list ) {
                          if (simpleZongMen != null) {
                              i++;
                              res.addZongMenList(((SimpleZongMen)simpleZongMen).toProto());
                          }else {
                              readRankSize--;
                          }
                          if (i >= 20){
                              break;
                          }
                      }
                  }
                  res.setTotal(readRankSize);
                  client.sendProtocol(res.build());
              }).exceptionally( err ->{
                  client.sendProtocol(res.build(), ErrorMsgEnum.unknown.getId());
                  err.printStackTrace();
                return null;
              });
              return null;
            }).exceptionally(
                    err->{
                        client.sendProtocol(res.build(), ErrorMsgEnum.unknown.getId());
                        err.printStackTrace();
                return null;});

  }

  /** ZongMenCallbackMsg 宗门 RPC 回调 消息 */
 public static class ZongMenCallbackMsg {
    /** 错误码 */
    int errorCode;

    /** 消息 */
    Message response;

    public ZongMenCallbackMsg(int errorCode, Message response) {
      this.errorCode = errorCode;
      this.response = response;
    }
  }
}
