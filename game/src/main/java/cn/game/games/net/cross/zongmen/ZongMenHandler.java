package cn.game.games.net.cross.zongmen;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.core.log.GameLogger;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.util.LockUtil;
import cn.game.util.RedisUtil;
import com.google.protobuf.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ZongMenHandler
 *
 * @description:
 * @author: ly
 * @create: 2025-02-06 15:11 @Version 1.0
 */
public class ZongMenHandler extends BaseHandler {

  @Override
  protected void inititialize() {
    putInvoker(PbProtocol.ZongMenMsgRequest_7d000045, this::dispatchMsg);
  }

  @Override
  protected int getModule() {
    return 0x7d;
  }

  private void dispatchMsg(NetClient client, Object o) {
    ServerMsg.ZongMenMsgRequest_7d000045 request = (ServerMsg.ZongMenMsgRequest_7d000045) o;
    int msgId = request.getMsgId();
    long playerId = request.getPlayerId();
    long zongMenId = request.getZongMenId();
    List<String> paramList = new ArrayList<>(request.getParamsList().stream().toList());
    Message message = PbProtocol.getInstance().parseFrom(msgId, request.getData());
    
    ServerContext.getInstance().getProcessor().process(zongMenId, () -> {
    	// TODO log message
    	switch (msgId) {
    	case PbProtocol.getZongMenInfoRequest_40000021 ->
    	getZongMenInfo(zongMenId, playerId, message, paramList, client);
    	case PbProtocol.createZongMenRequest_40000005 ->
    	createZongMen(playerId, message, paramList, client);
    	case PbProtocol.applyJoinZongMenRequest_40000007 ->
    	applyJoinZongMen(playerId, message, paramList, client);
    	case PbProtocol.dissolveZongMenRequest_40000011 -> 
    	dissolveZongMen(zongMenId,playerId, message, paramList, client);
    	}
    });
    
  }

  private void dissolveZongMen(long zongMenId,long playerId, Message message, List<String> paramList, NetClient client) {
    ZongMenMsg.dissolveZongMenResponse_40000012.Builder res = ZongMenMsg.dissolveZongMenResponse_40000012.newBuilder();
    ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
    if (zongMenInfo == null) {
      sendErrorCodeMsgToGameServer(
          playerId,
          client,
          ErrorMsgEnum.zong_men_not_exist,
          PbProtocol.dissolveZongMenResponse_40000012);
      return;
    }
    ZongMenMember menMember = zongMenInfo.getMember(playerId);
    if (menMember.getPosition() != ZongMenConstants.ZONG_MEN_POSITION_ZONG_ZHU) {
      sendErrorCodeMsgToGameServer(
          playerId,
          client,
          ErrorMsgEnum.zong_men_permission_not_enough,
          PbProtocol.dissolveZongMenResponse_40000012);
      return;
    }
    zongMenInfo.dissolveZongMen();
    sendMsgToGameServer(
        playerId, client, res.build(), PbProtocol.dissolveZongMenResponse_40000012);

  }

  private void applyJoinZongMen(
      long playerId, Message message, List<String> paramList, NetClient client) {
    ZongMenMsg.applyJoinZongMenRequest_40000007 req =
        (ZongMenMsg.applyJoinZongMenRequest_40000007) message;
    ZongMenMsg.applyJoinZongMenResponse_40000008.Builder res =
        ZongMenMsg.applyJoinZongMenResponse_40000008.newBuilder();
    long zongMenId = req.getId();
    int power = Integer.parseInt(paramList.get(0));
    String playerName = paramList.get(1);
    ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
    if (zongMenInfo.isHasMember(playerId)) {
      sendErrorCodeMsgToGameServer(
          playerId,
          client,
          ErrorMsgEnum.zong_men_player_apply_has,
          PbProtocol.applyJoinZongMenResponse_40000008);
      return;
    }
    if (zongMenInfo.isFull()) {
      sendErrorCodeMsgToGameServer(
          playerId,
          client,
          ErrorMsgEnum.zong_men_full,
          PbProtocol.applyJoinZongMenResponse_40000008);
      return;
    }
    if (zongMenInfo.hasApply(playerId)) {
      sendErrorCodeMsgToGameServer(
          playerId,
          client,
          ErrorMsgEnum.zong_men_apply_exist,
          PbProtocol.applyJoinZongMenResponse_40000008);
      return;
    }
    //开启自动加入 则直接加入宗门
    if (zongMenInfo.isAutoJoin()) {
      zongMenInfo.joinZongMen(
          playerId, playerName, power, ZongMenConstants.ZONG_MEN_POSITION_BANG_ZHONG);
      res.setZongMen(zongMenInfo.toProto());
    } else {
      zongMenInfo.applyJoin(playerId);
    }
    sendMsgToGameServer(
        playerId, client, res.build(), PbProtocol.applyJoinZongMenResponse_40000008);
  }

  private void createZongMen(
      long playerId, Message message, List<String> paramList, NetClient client) {
    ZongMenMsg.createZongMenRequest_40000005 req =
        (ZongMenMsg.createZongMenRequest_40000005) message;
    String name = req.getName();
    String createPlayerName = paramList.get(0);
    int power = Integer.parseInt(paramList.get(1));
    ZongMenMsg.createZongMenResponse_40000006.Builder res =
        ZongMenMsg.createZongMenResponse_40000006.newBuilder();
    boolean createLock = LockUtil.tryLockNoWaitSync(3, CacheType.ZONG_MEN_CREATE_LOCK.key(name));
    // 该名称被其他节点使用
    if (!createLock) {
      sendErrorCodeMsgToGameServer(
          playerId,
          client,
          ErrorMsgEnum.zong_men_name_repeat,
          PbProtocol.createZongMenResponse_40000006);
      return;
    }
    ZongMenManager.getInstance()
        .createZongMen(name, playerId, createPlayerName, power)
        .onSuccess(
            zongMenInfo -> {
              if (zongMenInfo != null) { // 创建宗门成功
                res.setZongMen(zongMenInfo.toProto());
                sendMsgToGameServer(
                    playerId, client, res.build(), PbProtocol.createZongMenResponse_40000006);
              } else {
                sendErrorCodeMsgToGameServer(
                    playerId,
                    client,
                    ErrorMsgEnum.zong_men_name_repeat,
                    PbProtocol.createZongMenResponse_40000006);
              }
            })
        .onFailure(
            err -> {
              err.printStackTrace();
              sendErrorCodeMsgToGameServer(
                  playerId,
                  client,
                  ErrorMsgEnum.zong_men_name_repeat,
                  PbProtocol.createZongMenResponse_40000006);
            });
  }

  private void getZongMenInfo(
      long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
    ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
    if (zongMenInfo == null) {
      sendErrorCodeMsgToGameServer(
          playerId,
          client,
          ErrorMsgEnum.zong_men_not_exist,
          PbProtocol.getZongMenInfoResponse_40000022);
      return;
    }
    ZongMenMsg.ZongMenInfoProto infoProto = zongMenInfo.toProto();
    sendMsgToGameServer(
        playerId,
        client,
        ZongMenMsg.getZongMenInfoResponse_40000022.newBuilder().setInfo(infoProto).build(),
        PbProtocol.getZongMenInfoResponse_40000022);
  }

  public void sendErrorCodeMsgToGameServer(
      long playerId, NetClient client, ErrorMsgEnum errorCode, int msgId) {
    ServerMsg.ZongMenMsgResponse_7d000046.Builder response =
        ServerMsg.ZongMenMsgResponse_7d000046.newBuilder();
    response.setMsgId(msgId);
    response.setPlayerId(playerId);
    response.setErrorCode(errorCode.getId());
    client.sendProtocol(response.build());
    // TODO log message
  }

  public void sendMsgToGameServer(long playerId, NetClient client, Message message, int msgId) {
    ServerMsg.ZongMenMsgResponse_7d000046.Builder response =
        ServerMsg.ZongMenMsgResponse_7d000046.newBuilder();
    response.setMsgId(msgId);
    response.setErrorCode(ErrorMsgEnum.ok.getId());
    response.setPlayerId(playerId);
    response.setData(message.toByteString());
    client.sendProtocol(response.build());
    // TODO log message

  }
}
