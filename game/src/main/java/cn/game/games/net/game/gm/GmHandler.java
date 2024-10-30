package cn.game.games.net.game.gm;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import cn.game.games.cache.entity.Player;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.core.net.client.LogoutType;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.ForbidAccount;
import cn.game.games.cache.entity.GmMail;
import cn.game.games.net.data.mapper.GmMailMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.manager.PlayerNameManager;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.util.DAO;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.BaseMsg.GoodsInfo;
import cn.game.protocol.protobuf.GmMsg;
import cn.game.protocol.protobuf.GmMsg.GmAccountForbidListResponse_77000004;
import cn.game.protocol.protobuf.GmMsg.GmAccountForbidRequest_77000005;
import cn.game.protocol.protobuf.GmMsg.GmAccountForbidResponse_77000006;
import cn.game.protocol.protobuf.GmMsg.GmAccountUnblockRequest_77000007;
import cn.game.protocol.protobuf.GmMsg.GmAccountUnblockResponse_77000008;
import cn.game.protocol.protobuf.GmMsg.GmPlayerLogoutRequest_77000009;
import cn.game.protocol.protobuf.GmMsg.GmPlayerLogouttResponse_7700000a;
import cn.game.protocol.protobuf.GmMsg.GmPlayerRequest_77000021;
import cn.game.protocol.protobuf.GmMsg.GmPlayerResponse_77000022;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.protocol.protobuf.ServerMsg.GameGmPlayerInfoRequest_7d000050;
import cn.game.protocol.protobuf.ServerMsg.GameGmPlayerInfoResponse_7d000051;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;
import cn.game.util.ServerType;
import io.vertx.core.Future;

/** gm处理器 */
@Component
public class GmHandler extends BaseHandler {

  @Override
  protected int getModule() {
    return 0x77;
  }

  @Override
  protected void inititialize() {

    putInvoker(PbProtocol.GmShutdownServerRequest_77000001, this::shutdown);
    putInvoker(PbProtocol.GmAccountForbidListRequest_77000003, this::forbidAccountList);
    putInvoker(PbProtocol.GmAccountForbidRequest_77000005, this::forbidAccount);
    putInvoker(PbProtocol.GmAccountUnblockRequest_77000007, this::unblockAccount);
    putInvoker(PbProtocol.GmPlayerLogoutRequest_77000009, this::playerLogout);
    putInvoker(PbProtocol.GmPlayerRequest_77000021, this::playerInfo);
    putInvoker(PbProtocol.GmMailServerSendRequest_77000048, this::gmSendMail);
    putInvoker(PbProtocol.GmMailListRequest_77000042, this::selectGmMailList);
    putInvoker(PbProtocol.GmMailCheckRequest_77000044, this::checkMail);
    putInvoker(PbProtocol.GmMailDeleteRequest_77000046, this::delGmMail);
  }

  private void gmSendMail(NetClient client, Object o) {
    GmMsg.GmMailServerSendRequest_77000048 req = (GmMsg.GmMailServerSendRequest_77000048) o;
    GmMsg.GmMailServerSendResponse_77000049.Builder res =
        GmMsg.GmMailServerSendResponse_77000049.newBuilder();
    String title = req.getTitle();
    String content = req.getContent();
    List<GoodsInfo> attachmentsList = req.getAttachmentsList();
    List<Goods> list = new ArrayList<>();
    for (GoodsInfo goods : attachmentsList) {
      Goods g = new Goods();
      g.setId(goods.getId());
      g.setCount(goods.getCount());
      list.add(g);
    }
    if (title.isEmpty() || content.isEmpty()) {
      sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.request_parameter_null, "");
      return;
    }
    GmMail gmMail = new GmMail();
    gmMail.setTitle(title);
    gmMail.setContext(content);
    gmMail.setOptFlag((byte)0);
    gmMail.setCreateTime(new Date());
    if (!list.isEmpty()) {
      gmMail.setAttachment(JsonUtil.toJsonString(list));
    }
    // 全服邮件
    if (req.getPlayerIdsCount() == 0) {
      if (req.getSendEndTime() <= req.getSendStartTime()
          || req.getLevelEnd() <= req.getLevelStart()
          || (req.getTimeCheckType() != 0 && req.getTimeCheckType() != 1)) {
        sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.request_parameter_null, "");
        return;
      }
      gmMail.setServerids(req.getServerIdList().toString());

      gmMail.setSendStartTimer(
          DateUtil.getTimeByPattern(new Date(req.getSendStartTime() * 1000L), DateUtil.pattern_en));
      gmMail.setSendEndTimer(
          DateUtil.getTimeByPattern(new Date(req.getSendEndTime() * 1000L), DateUtil.pattern_en));
      gmMail.setMinLevel(req.getLevelStart());
      gmMail.setMaxLevel(req.getLevelEnd());
      gmMail.setOptFlag((byte) 0);
      gmMail.setTimeCheckType((int) req.getTimeCheckType());
      gmMail.setMailopttype((int) 1);
    } else {
      gmMail.setMailopttype((int) 0);
      gmMail.setPids(req.getPlayerIdsList().toString());
    }
    DAO.insert(gmMail)
        .onSuccess(
            r -> {
              sendAndRecordOpt(client, req, res.build());
            })
        .onFailure(
            e -> {
              e.printStackTrace();
              sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.unknown, "");
            });
  }

  private void selectGmMailList(NetClient client, Object o) {
    GmMsg.GmMailListRequest_77000042 req = (GmMsg.GmMailListRequest_77000042) o;
    GmMsg.GmMailResponse_77000043.Builder res = GmMsg.GmMailResponse_77000043.newBuilder();
    int page = req.getPageNum();
    int size = req.getPageSize();
    if (page < 1 || size < 1) {
      sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.request_parameter_null, "");
      return;
    }
    DAO.execute(
            GmMailMapper.class,
            "selectGmMailList",
            req.getStartTime()== 0 ? null : new java.sql.Date(req.getStartTime()*1000L),
            req.getEndTime() == 0 ? null : new java.sql.Date(req.getEndTime()*1000L),
            req.getType() == 0 ? null : (req.getType() == 1 ? 0 : 1),
            req.getTitle() == null ? null : req.getTitle(),
            req.getContent() == null ? null : req.getContent(),
            req.getStatus() == 0 ? null : (req.getStatus() == 1 ? 0 : 2) ,//1 暂未审核 2 审核成功  3审核失败。0 全部状态
            (page - 1) * size,
            size)
        .onSuccess(
            result -> {
              List<GmMail> list = (List<GmMail>) result;
              if (list != null) {
                list.forEach(
                    gmMail -> {
                      try {
                        res.addMails(GmHelper.toGmMailPb(gmMail));
                      } catch (ParseException e) {
                        e.printStackTrace();
                      }
                    });
              }
              sendAndRecordOpt(client, req, res.build());
            })
        .onFailure(
            e -> {
                e.printStackTrace();
              sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.unknown, e.getMessage());
            });
  }

  private void checkMail(NetClient client, Object o) {
    GmMsg.GmMailCheckRequest_77000044 req = (GmMsg.GmMailCheckRequest_77000044) o;
    GmMsg.GmMailCheckResponse_77000045.Builder res =
        GmMsg.GmMailCheckResponse_77000045.newBuilder();
    if (req.getUidCount() <= 0) {
      sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.request_parameter_null, "");
      return;
    }
    req.getUidList()
        .forEach(
            mailId -> {
              DAO.execute(
                      GmMailMapper.class,
                      MapperConstant.selectByPrimaryKey,
                      Integer.parseInt(mailId))
                  .onSuccess(
                      r -> {
                        if (r == null) {
                          return;
                        }
                        GmMail gmMail = (GmMail) r;
                        if (gmMail.getApprovalTimer() != null) {
                          sendAndRecordOpt(
                              client, req, res.build(), ErrorMsgEnum.request_parameter_null, "");
                          return;
                        }
                        gmMail.setApprovalTimer(DateUtil.getStringDate());
                        gmMail.setOptFlag((byte) 1);
                        List<Goods> attachment = GmHelper.getAttachment(gmMail);
                        DAO.update(gmMail);

                        // 个人邮件
                        if (gmMail.getPids() != null) {
                          String[] pids = gmMail.getPids().replace("[","").replace("]","").trim().split(",");
                          for (String pid : pids) {
                            MailHelper.sendMail(
                                Long.parseLong(pid.trim()),
                                    0,
                                "系统管理员",
                                gmMail.getTitle(),
                                gmMail.getContext(),
                                MailHelper.SYSTEM,
                                attachment,
                                true);
                          }
                        } else { // 全服邮件
                          MailHelper.addGlobalMail(gmMail);
                          // 通知其他节点 添加新的全服邮件
                            VxHolder.broadcastRemoteServer(ServerType.Game,ServerMsg.NotifyAddGlobalGmMailRequest_7d000060.newBuilder().setAddGmMailId(gmMail.getId()).build());
//                            GameServer.getInstance().getCrossGameServerInterfaceSync().notifyBroadcastAddGlobalGmMail(gmMail.getId());
                        }
                        sendAndRecordOpt(client, req, res.build());
                      })
                  .onFailure(
                      e -> {
                        e.printStackTrace();
                        sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.unknown, "");
                      });
            });
  }

  private void delGmMail(NetClient client, Object o) {
    GmMsg.GmMailDeleteRequest_77000046 req = (GmMsg.GmMailDeleteRequest_77000046) o;
    GmMsg.GmMailDeleteResponse_77000047.Builder res =
        GmMsg.GmMailDeleteResponse_77000047.newBuilder();
    if (req.getUidCount() <= 0) {
      sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.request_parameter_null, "");
      return;
    }
    req.getUidList()
        .forEach(
            mailId -> {
              DAO.execute(GmMailMapper.class, MapperConstant.deleteByPrimaryKey, mailId)
                  .onSuccess(
                      r -> {
                        if (r != null &&  MailHelper.removeGlobalMail(mailId)) {
                          // 该邮件是全服邮件, 通知其他节点删除该邮件
//                            GameServer.getInstance().getCrossGameServerInterfaceSync().notifyBroadcastDelGlobalGmMail(Integer.parseInt(mailId));
                            VxHolder.broadcastRemoteServer(ServerType.Game,ServerMsg.NotifyDelGlobalGmMailRequest_7d000062.newBuilder().setDelGmMailId(Integer.parseInt(mailId)).build());
                        }
                        sendAndRecordOpt(client, req, res.build());
                      })
                  .onFailure(
                      e -> {
                        sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.unknown, "");
                      });
            });
  }

  protected void playerInfo(NetClient client, Object message) {
    GmPlayerRequest_77000021 request = (GmPlayerRequest_77000021) message;
    GmPlayerResponse_77000022.Builder response = GmPlayerResponse_77000022.newBuilder();
    String channel = request.getChannel();
    String name = request.getName();
    VxHolder.vertx.executeBlocking(
        f -> {
          long playerId =
              StringUtils.isEmpty(request.getPlayerId())
                  ? 0
                  : Long.parseLong(request.getPlayerId());
          if (playerId == 0) {
            if (name != null) {
              try {
                playerId =
                    PlayerNameManager.getInstance()
                        .getPlayerId(name)
                        .toCompletionStage()
                        .toCompletableFuture()
                        .get();
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
          if (!PlayerManager.getInstance().isOnline(playerId)) {
            Future<io.vertx.core.eventbus.Message<GameGmPlayerInfoResponse_7d000051>> respMessage =
                VxHolder.requestRemoteServer(
                    PlayerManager.getInstance().getServerId(playerId),
                    GameGmPlayerInfoRequest_7d000050.newBuilder().setPlayerId(playerId).build());
            respMessage
                .onSuccess(
                    r -> {
                      response.setPlayer(r.body().getPlayer());
                      sendAndRecordOpt(client, request, response.build());
                    })
                .onFailure(
                    err -> {
                      err.printStackTrace();
                      sendAndRecordOpt(
                          client,
                          request,
                          response.build(),
                          ErrorMsgEnum.player_data_not_found,
                          "");
                    });
          } else {
            // 从本服务器载入玩家数据
            GmHelper.getPlayerInfo(name, playerId)
                .onSuccess(
                    r -> {
                      response.setPlayer(r);
                      sendAndRecordOpt(client, request, response.build());
                    })
                .onFailure(
                    e -> {
                      sendAndRecordOpt(
                          client,
                          request,
                          response.build(),
                          ErrorMsgEnum.player_data_not_found,
                          "");
                    });
          }
          f.complete(null);
        });
  }

  private void sendAndRecordOpt(NetClient client, Message request, Message response) {
    sendAndRecordOpt(client, request, response, null, null);
  }

  /**
   * 发送协议并记录操作日志。 如果提供了错误消息枚举，则发送带有错误ID的响应协议； 否则，只发送响应协议。 然后创建一个操作日志对象，设置相关属性，并将其插入数据库。
   *
   * @param client 网络客户端实例
   * @param request 发送的请求消息
   * @param response 接收到的响应消息
   * @param errMsg 错误消息枚举，如果为null则表示没有错误
   * @param optMsg 操作消息，可以为null
   */
  private void sendAndRecordOpt(
      NetClient client, Message request, Message response, ErrorMsgEnum errMsg, String optMsg) {
    if (errMsg != null) {
      client.sendProtocol(response, errMsg.getId());
    } else {
      client.sendProtocol(response);
    }
    String result = errMsg == null ? response.toString() : errMsg.getDesc();
    ServerMsg.GmOptRecordRequest_7d000052.Builder req =
        ServerMsg.GmOptRecordRequest_7d000052.newBuilder();
    req.setOptmsg(optMsg == null ? "null" : optMsg)
        .setOptParam(request.toString())
        .setOptPid(
            client.getPlayerId()
                + ":"
                + PlayerManager.getInstance().getPlayer(client.getPlayerId()).getData().getName())
        .setOptResult(result);
    VxHolder.requestRemoteServer(ServerType.Login, req.build())
        .onComplete(r -> {})
        .onFailure(
            e -> {
              e.printStackTrace();
            });
  }

  private void shutdown(NetClient client, Object message) {
    CompletableFuture.runAsync(
        () -> {
          System.exit(0);
        });
  }

  /** 封号列表 */
  private void forbidAccountList(NetClient client, Object message) {
      GmMsg.GmAccountForbidListRequest_77000003 request = (GmMsg.GmAccountForbidListRequest_77000003) message;
    GmAccountForbidListResponse_77000004.Builder response =
        GmAccountForbidListResponse_77000004.newBuilder();
    List<ForbidAccount> accounts = PlayerManager.getInstance().getForbidAccount().stream().filter(forbidAccount -> forbidAccount.getType() == request.getType()).collect(Collectors.toList());
    response.addAllAccounts(PbBuilder.buildForbidAccount(accounts));
    client.sendProtocol(response);
  }

  /** 封号 */
  private void forbidAccount(NetClient client, Object message) {
    GmAccountForbidRequest_77000005 request = (GmAccountForbidRequest_77000005) message;
    GmAccountForbidResponse_77000006.Builder response =
        GmAccountForbidResponse_77000006.newBuilder();
    String reason = request.getReason();
    int type = request.getType();
    long unblockTime = request.getEndTime();
    List<Long> pids = new ArrayList<>();
    VxHolder.vertx.executeBlocking(
        (hand) -> {
          request
              .getPlayerIdList()
              .forEach(
                  playerId -> {
                    ForbidAccount forbidAccount =
                        PlayerManager.getInstance()
                            .forbidAccount(
                                Long.parseLong(playerId), reason, unblockTime * 1000L + "", type);
                    if (forbidAccount != null) {
                      sendAndRecordOpt(client, request, response.build());
                      pids.add(forbidAccount.getPlayerId());
                    } else {
                      sendAndRecordOpt(
                          client, request, response.build(), ErrorMsgEnum.unknown, reason);
                    }
                    if (PlayerManager.getInstance().isForbidAccount(Long.parseLong(playerId))) {
                      Player optPlayer =
                          PlayerManager.getInstance().getPlayer(Long.parseLong(playerId));
                      if (optPlayer != null) {
                        GameClientManager.getInstance()
                            .logout(Long.parseLong(playerId), LogoutType.GMKick);
                      }
                    }
                  });
          //  通知其他game节点添加封号记录
          if (!pids.isEmpty()) {
            VxHolder.broadcastRemoteServer(
                ServerType.Game,
                ServerMsg.NotifyGmAddForbidAccountRequest_7d000054.newBuilder()
                    .setReason(request.getReason())
                    .setTimer(unblockTime * 1000L)
                    .addAllPids(pids)
                    .setType(request.getType())
                    .build());
          }
        });
  }

  /** 解封账号 */
  private void unblockAccount(NetClient client, Object message) {
    GmAccountUnblockRequest_77000007 request = (GmAccountUnblockRequest_77000007) message;
    GmAccountUnblockResponse_77000008.Builder response =
        GmAccountUnblockResponse_77000008.newBuilder();
    List<Long> pids = new ArrayList<>();
    request
        .getPlayerIdList()
        .forEach(
            playerId -> {
              PlayerManager.getInstance().unblockAccount(Long.parseLong(playerId));
              pids.add(Long.parseLong(playerId));
              sendAndRecordOpt(client, request, response.build());
            });
    // 通知其他game节点删除封号记录
    if (!pids.isEmpty()) {
        VxHolder.broadcastRemoteServer(ServerType.Game, ServerMsg.NotifyGmDelForbidAccountRequest_7d000056.newBuilder().addAllPids(pids).build());
//      GameServer.getInstance()
//          .getCrossGameServerInterfaceSync()
//          .notifyBroadcastDelForbidAccount(pids);
    }
  }

  /** 踢玩家下线 */
  private void playerLogout(NetClient client, Object message) {
    GmPlayerLogoutRequest_77000009 request = (GmPlayerLogoutRequest_77000009) message;
    GmPlayerLogouttResponse_7700000a.Builder response =
        GmPlayerLogouttResponse_7700000a.newBuilder();

    long playerId =
        StringUtils.isEmpty(request.getPlayerId()) ? 0 : Long.parseLong(request.getPlayerId());
    PlayerHelper.addTask(
        playerId,
        r -> {
				GameClientManager.getInstance().logout(playerId, LogoutType.GMKick);
          client.sendProtocol(response);
        });
  }
}
