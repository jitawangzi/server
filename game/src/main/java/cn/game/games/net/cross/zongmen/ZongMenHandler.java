package cn.game.games.net.cross.zongmen;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Message;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.protocol.generated.config.PermissionsConfig;
import cn.game.protocol.generated.manager.PermissionsManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.util.LockUtil;

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
    // TODO log message
    ServerContext.getInstance().getProcessor().process(zongMenId, () -> {
      switch (msgId) {
        case PbProtocol.getZongMenInfoRequest_40000021 ->
                getZongMenInfo(zongMenId, playerId, message, paramList, client);
        case PbProtocol.createZongMenRequest_40000005 ->
                createZongMen(playerId, message, paramList, client);
        case PbProtocol.applyJoinZongMenRequest_40000007 ->
                applyJoinZongMen(playerId, message, paramList, client);
        case PbProtocol.dissolveZongMenRequest_40000011 ->
                dissolveZongMen(zongMenId, playerId, message, paramList, client);
        case PbProtocol.setZongMenSettingRequest_40000013 ->
                setZongMenSetting(zongMenId, playerId, message, paramList, client);
        case PbProtocol.getZongMenLogResponse_40000026 ->
                getZongMenLog(zongMenId, playerId, message, paramList, client);
        case PbProtocol.setZongMenMemberPositionRequest_40000015 ->
                setZongMenMemberPosition(zongMenId, playerId, message, paramList, client);
        case PbProtocol.quitZongMenRequest_40000017 ->
                quitZongMen(zongMenId,playerId, message, paramList, client);
        case PbProtocol.updateZongMenAssetRequest_40000037 ->
                updateZongMenAsset(zongMenId, playerId, message, paramList, client);
      }

    });
  }

  //跟新 贡献度 活跃度 之类的资产
  private void updateZongMenAsset(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
    ZongMenMsg.updateZongMenAssetRequest_40000037 req = (ZongMenMsg.updateZongMenAssetRequest_40000037) message;
    ZongMenMsg.updateZongMenAssetResponse_40000038.Builder res =
            ZongMenMsg.updateZongMenAssetResponse_40000038.newBuilder();
    ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
    if (zongMenInfo == null) {
      sendErrorCodeMsgToGameServer(
              playerId,
              client,
              ErrorMsgEnum.zong_men_not_exist,
              PbProtocol.updateZongMenAssetResponse_40000038);
      return;
    }
    req.getZongMenAssetMapsList().forEach(reward -> {
      int id = reward.getAsset().getId();
      int num = (int) reward.getAsset().getCount();
      zongMenInfo.addZongMenAsset(playerId,id, num);
    });
    sendMsgToGameServer(playerId, client,res.build(), PbProtocol.updateZongMenAssetResponse_40000038);
  }

  private void quitZongMen(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
    ZongMenMsg.quitZongMenRequest_40000017 req = (ZongMenMsg.quitZongMenRequest_40000017) message;
    ZongMenMsg.quitZongMenResponse_40000018.Builder res =
            ZongMenMsg.quitZongMenResponse_40000018.newBuilder();
    ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
    String playerName = paramList.get(0);
    if (zongMenInfo == null){
      sendErrorCodeMsgToGameServer(
              playerId,
              client,
              ErrorMsgEnum.zong_men_not_exist,
              PbProtocol.quitZongMenResponse_40000018);
      return;
    }
    ZongMenMember member = zongMenInfo.getMember(playerId);
    //对宗主的处理
    if (member.getPosition() == ZongMenConstants.ZONG_MEN_POSITION_ZONG_ZHU){
      //宗门 没人了 直接 解散
      if (zongMenInfo.getModule().menMemberMap.size() <= 1) {
        zongMenInfo.dissolveZongMen();
      } else {//宗门 有人存在 则不可退出 需要先把宗主 转让出去
        sendErrorCodeMsgToGameServer(
                playerId,
                client,
                ErrorMsgEnum.zong_men_permission_not_enough,
                PbProtocol.quitZongMenResponse_40000018);
        return;
      }
    } else {
      zongMenInfo.quitZongMen(member,playerName);
    }

  }

  private void setZongMenMemberPosition(
          long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
    ZongMenMsg.setZongMenMemberPositionRequest_40000015 req =
            (ZongMenMsg.setZongMenMemberPositionRequest_40000015) message;
    ZongMenMsg.setZongMenMemberPositionResponse_40000016.Builder res =
            ZongMenMsg.setZongMenMemberPositionResponse_40000016.newBuilder();
    ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
    if (zongMenInfo == null) {
      sendErrorCodeMsgToGameServer(
              playerId,
              client,
              ErrorMsgEnum.zong_men_not_exist,
              PbProtocol.setZongMenMemberPositionResponse_40000016);
      return;
    }
    ZongMenMember member = zongMenInfo.getMember(playerId);
    if (member == null) {
      sendErrorCodeMsgToGameServer(
              playerId,
              client,
              ErrorMsgEnum.zong_men_player_member_not_exist,
              PbProtocol.setZongMenMemberPositionResponse_40000016);
      return;
    }
    PermissionsConfig permissionsConfig = PermissionsManager.instance().get(member.position);
    if (!permissionsConfig.Posts) {
      sendErrorCodeMsgToGameServer(
              playerId,
              client,
              ErrorMsgEnum.zong_men_permission_not_enough,
              PbProtocol.setZongMenMemberPositionResponse_40000016);
      return;
    }

    //目标职位人数
    int targetPositionNum = zongMenInfo.getPositionMemberNum(req.getPosition());
    PermissionsConfig targetPermissionsConfig = PermissionsManager.instance().get(req.getPosition());
    if (targetPositionNum >= targetPermissionsConfig.Number) {
      sendErrorCodeMsgToGameServer(
              playerId,
              client,
              ErrorMsgEnum.zong_men_position_member_num_not_enough,
              PbProtocol.setZongMenMemberPositionResponse_40000016);
      return;
    }

    ZongMenMember targetMember = zongMenInfo.getMember(req.getTargetPid());

    if (req.getPosition() >= ZongMenConstants.ZONG_MEN_POSITION_ZHANG_LAO
            && req.getPosition() <= ZongMenConstants.ZONG_MEN_POSITION_BANG_ZHONG && req.getPosition() != targetMember.getPosition()) {
      int oldPosition = targetMember.getPosition();
      targetMember.setPosition(req.getPosition());
      zongMenInfo.handleEvent(ZongMenConstants.ZongMenEvenType.ZONG_MEN_POSITION_CHANGE,targetMember.playerId,oldPosition,req.getPosition());
    } else {
      sendErrorCodeMsgToGameServer(
              playerId,
              client,
              ErrorMsgEnum.request_parameter_error,
              PbProtocol.setZongMenMemberPositionResponse_40000016);
      return;
    }
    sendMsgToGameServer(
            playerId, client, res.build(), PbProtocol.setZongMenMemberPositionResponse_40000016);
  }

  private void getZongMenLog(
          long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
    ZongMenMsg.getZongMenLogResponse_40000026.Builder res =
            ZongMenMsg.getZongMenLogResponse_40000026.newBuilder();
    ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
    if (zongMenInfo == null) {
      sendErrorCodeMsgToGameServer(
              playerId,
              client,
              ErrorMsgEnum.zong_men_not_exist,
              PbProtocol.getZongMenLogResponse_40000026);
      return;
    }
    res.addAllLogList(zongMenInfo.getModule().optLog.toProto());
    sendMsgToGameServer(playerId, client, res.build(), PbProtocol.getZongMenLogResponse_40000026);
  }

  private void setZongMenSetting(
          long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
    ZongMenMsg.setZongMenSettingRequest_40000013 req =
            (ZongMenMsg.setZongMenSettingRequest_40000013) message;
    ZongMenMsg.setZongMenSettingResponse_40000014.Builder res =
            ZongMenMsg.setZongMenSettingResponse_40000014.newBuilder();
    ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
    if (zongMenInfo == null) {
      sendErrorCodeMsgToGameServer(
              playerId,
              client,
              ErrorMsgEnum.zong_men_not_exist,
              PbProtocol.dissolveZongMenResponse_40000012);
      return;
    }
    if (!req.getWxBytes().isEmpty()) {
      zongMenInfo.getModule().setting.setWx(req.getWx());
    }
    if (!req.getNoticeBytes().isEmpty()) {
      zongMenInfo.getData().setNotice(req.getNotice());
    }
    if (!req.getDeclarationBytes().isEmpty()) {
      zongMenInfo.getData().setDeclaration(req.getDeclaration());
    }
    if (req.getIcon() != 0
            && zongMenInfo.getModule().setting.unlockIconList.contains(req.getIcon())) {
      zongMenInfo.getData().setIcon(req.getIcon());
    }
    if (req.getAutoJoin() == 1 || req.getAutoJoin() == 2) {
      zongMenInfo.getModule().setting.setAutoJoin(req.getAutoJoin() == 1);
    }
    if (req.getTianDaoLevel() != 0) {
      zongMenInfo.getModule().setting.setTianDaoLevel(req.getTianDaoLevel());
    }
    sendMsgToGameServer(
            playerId, client, res.build(), PbProtocol.setZongMenSettingResponse_40000014);
  }

  private void dissolveZongMen(
          long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
    ZongMenMsg.dissolveZongMenResponse_40000012.Builder res =
            ZongMenMsg.dissolveZongMenResponse_40000012.newBuilder();
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
    sendMsgToGameServer(playerId, client, res.build(), PbProtocol.dissolveZongMenResponse_40000012);
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
    // 开启自动加入 则直接加入宗门
    if (zongMenInfo.isAutoJoin()) {
      zongMenInfo.joinZongMen(
              playerId, playerName, power, ZongMenConstants.ZONG_MEN_POSITION_BANG_ZHONG);
      res.setZongMen(zongMenInfo.toProto(playerId));
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
	int serverSeq = Integer.parseInt(paramList.get(2));
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
			.createZongMen(name, playerId, createPlayerName, power, serverSeq)
            .onSuccess(
                    zongMenInfo -> {
                      if (zongMenInfo != null) { // 创建宗门成功
                        res.setZongMen(zongMenInfo.toProto(playerId));
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
    ZongMenMsg.ZongMenInfoProto infoProto = zongMenInfo.toProto(playerId);
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
