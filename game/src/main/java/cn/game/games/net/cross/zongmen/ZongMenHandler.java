package cn.game.games.net.cross.zongmen;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.core.base.ServerContext;
import cn.game.core.net.client.NetClient;
import cn.game.games.net.cross.zongmen.dto.CreateZongmenRequest;
import cn.game.games.net.cross.zongmen.dto.MemberAuthRequest;
import cn.game.games.net.cross.zongmen.dto.ZongmenOperationResult;
import cn.game.games.net.cross.zongmen.dto.ZongmenSettingRequest;
import cn.game.games.net.cross.zongmen.service.ZongmenService;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.ZongMenCrossMsg;
import cn.game.protocol.protobuf.ZongMenMsg;

/**
 * @ClassName ZongMenHandler
 *
 * @description: 宗门请求处理器 - 负责协议解析和调用宗门服务
 * @author: ly
 * @create: 2025-02-06 15:11 @Version 1.0
 */
@Component
public class ZongMenHandler extends GameBaseHandler {

    private final ZongmenService zongmenService = ZongmenService.getInstance();

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.ZongMenMsgRequest_41000045, this::dispatchMsg);
    }

    @Override
    protected int getModule() {
        return 0x41;
    }

    private void dispatchMsg(NetClient client, Object o) {
        ZongMenCrossMsg.ZongMenMsgRequest_41000045 request = (ZongMenCrossMsg.ZongMenMsgRequest_41000045) o;
        int msgId = request.getMsgId();
        long playerId = request.getPlayerId();
        long zongMenId = request.getZongMenId();
        List<String> paramList = new ArrayList<>(request.getParamsList().stream().toList());
        Message message = PbProtocol.getInstance().parseFrom(msgId, request.getData());
        
        ZongMenManager.log.info(String.format("dispatchMsg pid:%s zongMenId:%s cmd:%s req:%s", playerId, zongMenId,
                message.getClass().getSimpleName(), message));
        
        ServerContext.getInstance().getProcessor().process(zongMenId, () -> {
            try {
                switch (msgId) {
                    case PbProtocol.getZongMenInfoRequest_40000021 -> getZongMenInfo(zongMenId, playerId, message, paramList, client);
                    case PbProtocol.createZongMenRequest_40000005 -> createZongMen(playerId, message, paramList, client);
                    case PbProtocol.applyJoinZongMenRequest_40000007 -> applyJoinZongMen(playerId, message, paramList, client);
                    case PbProtocol.dissolveZongMenRequest_40000011 -> dissolveZongMen(zongMenId, playerId, message, paramList, client);
                    case PbProtocol.setZongMenSettingRequest_40000013 -> setZongMenSetting(zongMenId, playerId, message, paramList, client);
                    case PbProtocol.getZongMenLogRequest_40000025 -> getZongMenLog(zongMenId, playerId, message, paramList, client);
                    case PbProtocol.setZongMenMemberPositionRequest_40000015 ->
                        setZongMenMemberPosition(zongMenId, playerId, message, paramList, client);
                    case PbProtocol.quitZongMenRequest_40000017 -> quitZongMen(zongMenId, playerId, message, paramList, client);
                    case PbProtocol.updateZongMenAssetRequest_40000037 -> updateZongMenAsset(zongMenId, playerId, message, paramList, client);
                    case PbProtocol.updateMemberAuthRequest_40000041 -> updateMemberAuth(zongMenId, playerId, message, paramList, client);
                    case PbProtocol.ZongMenActiveRewardRequest_40000045 -> ZongMenActiveReward(zongMenId, playerId, message, paramList, client);
                    case PbProtocol.getZongMenShopRequest_40000027 -> getZongMenShop(zongMenId, playerId, message, paramList, client);
                    case PbProtocol.ZongMenBuyShopRequest_40000047 -> ZongMenBuyShop(zongMenId, playerId, message, paramList, client);
                    case PbProtocol.ZongMenBargainRequest_40000060 -> bargain(zongMenId, playerId, message, paramList, client);
                    case PbProtocol.ZongMenBargainBuyRequest_40000062 -> buyBargain(zongMenId, playerId, message, paramList, client);
                    case PbProtocol.findZongMenRequest_40000003 -> findZongMen(zongMenId, playerId, message, paramList, client);
                    case PbProtocol.ChatMessagePush_31010001 -> zongMenChat(zongMenId, playerId, message, paramList, client);
                    case PbProtocol.ZongMenUpdateMemberFightPowerRequest_40000051 ->
                        updateMemberFightPower(zongMenId, playerId, message, paramList, client);
                    case PbProtocol.ZongMenUpdateContributeValueReq_40000057 ->
                        updateContributeValue(zongMenId, playerId, message, paramList, client);
                }

            } catch (Exception e) {
                sendErrorCodeMsgToGameServer(playerId, client, ErrorMsgEnum.unknown, msgId + 1);
                e.printStackTrace();
            }

        });
    }

    // 获取宗门信息
    private void getZongMenInfo(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongmenOperationResult<ZongMenInfo> result = zongmenService.getZongmenInfo(zongMenId, playerId);
        if (!result.isSuccess()) {
			sendErrorCodeMsgToGameServer(playerId, client, result.getErrorCode(), PbProtocol.getZongMenInfoResponse_40000022);
            return;
        }
        
        ZongMenMsg.ZongMenInfoProto infoProto = result.getData().toProto(playerId);
        sendMsgToGameServer(playerId, client, ZongMenMsg.getZongMenInfoResponse_40000022.newBuilder().setInfo(infoProto).build(),
                PbProtocol.getZongMenInfoResponse_40000022);
    }

    // 创建宗门
    private void createZongMen(long playerId, Message message, List<String> paramList, NetClient client) {
        ZongMenMsg.createZongMenRequest_40000005 req = (ZongMenMsg.createZongMenRequest_40000005) message;
        
        CreateZongmenRequest createRequest = new CreateZongmenRequest(
                req.getName(), 
                playerId, 
                paramList.get(0), 
                Integer.parseInt(paramList.get(1)), 
                paramList.get(2)
        );
        createRequest.setIcon(req.getIcon());
        createRequest.setNotice(req.getNotice());
        createRequest.setDeclaration(req.getDeclaration());

        zongmenService.createZongmen(createRequest).onSuccess(result -> {
            ZongMenMsg.createZongMenResponse_40000006.Builder res = ZongMenMsg.createZongMenResponse_40000006.newBuilder();
            if (result.isSuccess()) {
                res.setZongMen(result.getData().toProto(playerId));
                sendMsgToGameServer(playerId, client, res.build(), PbProtocol.createZongMenResponse_40000006);
            } else {
				sendErrorCodeMsgToGameServer(playerId, client, result.getErrorCode(), PbProtocol.createZongMenResponse_40000006);
            }
        }).onFailure(err -> {
            err.printStackTrace();
            sendErrorCodeMsgToGameServer(playerId, client, ErrorMsgEnum.unknown, PbProtocol.createZongMenResponse_40000006);
        });
    }

    // 申请加入宗门
    private void applyJoinZongMen(long playerId, Message message, List<String> paramList, NetClient client) {
        ZongMenMsg.applyJoinZongMenRequest_40000007 req = (ZongMenMsg.applyJoinZongMenRequest_40000007) message;
        long zongMenId = req.getId();
        int power = Integer.parseInt(paramList.get(0));
        String playerName = paramList.get(1);

        ZongmenOperationResult<ZongMenInfo> result = zongmenService.applyJoinZongmen(zongMenId, playerId, playerName, power);
        ZongMenMsg.applyJoinZongMenResponse_40000008.Builder res = ZongMenMsg.applyJoinZongMenResponse_40000008.newBuilder();
        
        if (result.isSuccess()) {
            if (result.getData() != null) { // 直接加入成功
                res.setZongMen(result.getData().toProto(playerId));
            }
            sendMsgToGameServer(playerId, client, res.build(), PbProtocol.applyJoinZongMenResponse_40000008);
        } else {
			sendErrorCodeMsgToGameServer(playerId, client, result.getErrorCode(), PbProtocol.applyJoinZongMenResponse_40000008);
        }
    }

    // 解散宗门
    private void dissolveZongMen(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongmenOperationResult<Boolean> result = zongmenService.dissolveZongmen(zongMenId, playerId);
        if (result.isSuccess()) {
            sendMsgToGameServer(playerId, client, ZongMenMsg.dissolveZongMenResponse_40000012.newBuilder().build(), 
                    PbProtocol.dissolveZongMenResponse_40000012);
        } else {
			sendErrorCodeMsgToGameServer(playerId, client, result.getErrorCode(),
                    PbProtocol.dissolveZongMenResponse_40000012);
        }
    }

    // 设置宗门配置
    private void setZongMenSetting(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongMenMsg.setZongMenSettingRequest_40000013 req = (ZongMenMsg.setZongMenSettingRequest_40000013) message;
        
        ZongmenSettingRequest settingRequest = new ZongmenSettingRequest();
        settingRequest.setOperatorId(playerId);
        settingRequest.setOperatorName(paramList.get(0));
        settingRequest.setName(req.getName());
        settingRequest.setWx(req.getWx());
        settingRequest.setNotice(req.getNotice());
        settingRequest.setDeclaration(req.getDeclaration());
        settingRequest.setIcon(req.getIcon());
        settingRequest.setAutoJoin(req.getAutoJoin());
        settingRequest.setTianDaoLevel(req.getTianDaoLevel());

        zongmenService.setZongmenSetting(zongMenId, settingRequest).onSuccess(result -> {
            if (result.isSuccess()) {
                sendMsgToGameServer(playerId, client, ZongMenMsg.setZongMenSettingResponse_40000014.newBuilder().setResult(true).build(),
                        PbProtocol.setZongMenSettingResponse_40000014);
            } else {
				sendErrorCodeMsgToGameServer(playerId, client, result.getErrorCode(),
                        PbProtocol.setZongMenSettingResponse_40000014);
            }
        });
    }

    // 获取宗门日志
    private void getZongMenLog(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongmenOperationResult<ZongMenInfo> result = zongmenService.getZongmenInfo(zongMenId, playerId);
        if (!result.isSuccess()) {
			sendErrorCodeMsgToGameServer(playerId, client, result.getErrorCode(), PbProtocol.getZongMenLogResponse_40000026);
            return;
        }
        
        ZongMenMsg.getZongMenLogResponse_40000026.Builder res = ZongMenMsg.getZongMenLogResponse_40000026.newBuilder();
        res.addAllLogList(result.getData().getModule().optLog.toProto());
        sendMsgToGameServer(playerId, client, res.build(), PbProtocol.getZongMenLogResponse_40000026);
    }

    // 设置成员职位
    private void setZongMenMemberPosition(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongMenMsg.setZongMenMemberPositionRequest_40000015 req = (ZongMenMsg.setZongMenMemberPositionRequest_40000015) message;
        
        ZongmenOperationResult<Boolean> result = zongmenService.setMemberPosition(zongMenId, playerId, req.getTargetPid(), req.getPosition());
        if (result.isSuccess()) {
            sendMsgToGameServer(playerId, client, 
                    ZongMenMsg.setZongMenMemberPositionResponse_40000016.newBuilder()
                            .setResult(true)
                            .setPosition(req.getPosition())
                            .setTargetPid(req.getTargetPid())
                            .build(),
                    PbProtocol.setZongMenMemberPositionResponse_40000016);
        } else {
			sendErrorCodeMsgToGameServer(playerId, client, result.getErrorCode(),
                    PbProtocol.setZongMenMemberPositionResponse_40000016);
        }
    }

    // 退出宗门
    private void quitZongMen(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        String playerName = paramList.get(0);
        
        ZongmenOperationResult<Boolean> result = zongmenService.quitZongmen(zongMenId, playerId, playerName);
        if (result.isSuccess()) {
            sendMsgToGameServer(playerId, client, ZongMenMsg.quitZongMenResponse_40000018.newBuilder().setResult(true).build(),
                    PbProtocol.quitZongMenResponse_40000018);
        } else {
			sendErrorCodeMsgToGameServer(playerId, client, result.getErrorCode(),
                    PbProtocol.quitZongMenResponse_40000018);
        }
    }

    // 更新宗门资产
    private void updateZongMenAsset(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongMenMsg.updateZongMenAssetRequest_40000037 req = (ZongMenMsg.updateZongMenAssetRequest_40000037) message;
        
        req.getZongMenAssetMapsList().forEach(reward -> {
            int id = reward.getAsset().getId();
            int num = (int) reward.getAsset().getCount();
            zongmenService.updateZongmenAsset(zongMenId, playerId, id, num);
        });
        
        sendMsgToGameServer(playerId, client, ZongMenMsg.updateZongMenAssetResponse_40000038.newBuilder().build(),
                PbProtocol.updateZongMenAssetResponse_40000038);
    }

    // 成员权限管理
    private void updateMemberAuth(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongMenMsg.updateMemberAuthRequest_40000041 req = (ZongMenMsg.updateMemberAuthRequest_40000041) message;
        
        MemberAuthRequest authRequest = new MemberAuthRequest();
        authRequest.setOperatorId(playerId);
        authRequest.setOperatorName(paramList.get(0));
        authRequest.setOptType(req.getOptType());
        
        List<Long> targetPidList = new ArrayList<>();
        req.getTargetPidListList().forEach(pid -> targetPidList.add(pid.longValue()));
        authRequest.setTargetPlayerIds(targetPidList);

        ZongmenOperationResult<Boolean> result = zongmenService.updateMemberAuth(zongMenId, authRequest);
        if (result.isSuccess()) {
            sendMsgToGameServer(playerId, client, ZongMenMsg.updateMemberAuthResponse_40000042.newBuilder().setResult(true).build(),
                    PbProtocol.updateMemberAuthResponse_40000042);
        } else {
			sendErrorCodeMsgToGameServer(playerId, client, result.getErrorCode(),
                    PbProtocol.updateMemberAuthResponse_40000042);
        }
    }

    // 领取宗门活跃度奖励
    private void ZongMenActiveReward(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongMenMsg.ZongMenActiveRewardRequest_40000045 req = (ZongMenMsg.ZongMenActiveRewardRequest_40000045) message;
        
        ZongmenOperationResult<Boolean> result = zongmenService.receiveActiveReward(zongMenId, playerId, req.getIndexListList());
        if (result.isSuccess()) {
            sendMsgToGameServer(playerId, client, ZongMenMsg.ZongMenActiveRewardResponse_40000046.newBuilder().build(),
                    PbProtocol.ZongMenActiveRewardResponse_40000046);
        } else {
			sendErrorCodeMsgToGameServer(playerId, client, result.getErrorCode(),
                    PbProtocol.ZongMenActiveRewardResponse_40000046);
        }
    }

    // 获取宗门商店
    private void getZongMenShop(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongmenOperationResult<ZongMenInfo> result = zongmenService.getZongmenInfo(zongMenId, playerId);
        if (!result.isSuccess()) {
			sendErrorCodeMsgToGameServer(playerId, client, result.getErrorCode(),
                    PbProtocol.getZongMenShopResponse_40000028);
            return;
        }
        
        ZongMenMsg.getZongMenShopResponse_40000028.Builder res = ZongMenMsg.getZongMenShopResponse_40000028.newBuilder();
        sendMsgToGameServer(playerId, client, res.build(), PbProtocol.getZongMenShopResponse_40000028);
    }

    // 购买宗门商店物品
    private void ZongMenBuyShop(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongMenMsg.ZongMenBuyShopRequest_40000047 req = (ZongMenMsg.ZongMenBuyShopRequest_40000047) message;
        int playerLv = Integer.parseInt(paramList.get(0));
        
        ZongmenOperationResult<Boolean> result = zongmenService.buyShopItem(zongMenId, playerId, playerLv, req.getItemId(), req.getCount());
        if (result.isSuccess()) {
            sendMsgToGameServer(playerId, client, ZongMenMsg.ZongMenBuyShopResponse_40000048.newBuilder().build(),
                    PbProtocol.ZongMenBuyShopResponse_40000048);
        } else {
			sendErrorCodeMsgToGameServer(playerId, client, result.getErrorCode(),
                    PbProtocol.ZongMenBuyShopResponse_40000048);
        }
    }

    // 宗门砍价
    private void bargain(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongmenOperationResult<Integer> result = zongmenService.bargain(zongMenId, playerId);
        if (result.isSuccess()) {
            ZongMenMsg.ZongMenBargainResponse_40000061.Builder res = ZongMenMsg.ZongMenBargainResponse_40000061.newBuilder();
            res.setCount(result.getData());
            sendMsgToGameServer(playerId, client, res.build(), PbProtocol.ZongMenBargainResponse_40000061);
        } else {
			sendErrorCodeMsgToGameServer(playerId, client, result.getErrorCode(),
                    PbProtocol.ZongMenBargainResponse_40000061);
        }
    }

    // 砍价购买
    private void buyBargain(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongmenOperationResult<Boolean> result = zongmenService.buyBargain(zongMenId, playerId);
        if (result.isSuccess()) {
            sendMsgToGameServer(playerId, client, ZongMenMsg.ZongMenBargainBuyResponse_40000063.newBuilder().build(),
                    PbProtocol.ZongMenBargainBuyResponse_40000063);
        } else {
			sendErrorCodeMsgToGameServer(playerId, client, result.getErrorCode(),
                    PbProtocol.ZongMenBargainBuyResponse_40000063);
        }
    }

    // 查找宗门
    private void findZongMen(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongmenOperationResult<ZongMenInfo> result = zongmenService.getZongmenInfo(zongMenId, playerId);
        if (result.isSuccess()) {
            ZongMenMsg.findZongMenResponse_40000004.Builder res = ZongMenMsg.findZongMenResponse_40000004.newBuilder();
            res.setZongMen(result.getData().toProto());
            sendMsgToGameServer(playerId, client, res.build(), PbProtocol.findZongMenResponse_40000004);
        } else {
			sendErrorCodeMsgToGameServer(playerId, client, result.getErrorCode(), PbProtocol.findZongMenResponse_40000004);
        }
    }

    // 宗门聊天
    private void zongMenChat(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongmenOperationResult<ZongMenInfo> result = zongmenService.getZongmenInfo(zongMenId, playerId);
        if (!result.isSuccess()) {
			sendErrorCodeMsgToGameServer(playerId, client, result.getErrorCode(), PbProtocol.ChatResponse_31000002);
            return;
        }
        
        List<Long> memberIdList = new ArrayList<>(result.getData().getModule().menMemberMap.keySet());
        ZongMenHelper.broadcastNotifyMsgToPlayer(message, PbProtocol.ChatMessagePush_31010001, memberIdList);
    }

    // 更新成员战斗力
    private void updateMemberFightPower(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongMenMsg.ZongMenUpdateMemberFightPowerRequest_40000051 req = (ZongMenMsg.ZongMenUpdateMemberFightPowerRequest_40000051) message;
        
        zongmenService.updateMemberFightPower(zongMenId, playerId, req.getFightPower());
        sendMsgToGameServer(playerId, client, ZongMenMsg.ZongMenUpdateContributeValueRes_40000058.newBuilder().setResult(true).build(),
                PbProtocol.ZongMenUpdateContributeValueRes_40000058);
    }

    // 更新贡献度
    private void updateContributeValue(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongMenMsg.ZongMenUpdateContributeValueReq_40000057 req = (ZongMenMsg.ZongMenUpdateContributeValueReq_40000057) message;
        
        ZongmenOperationResult<Boolean> result = zongmenService.updateContributeValue(zongMenId, playerId, req.getValue());
        if (result.isSuccess()) {
            sendMsgToGameServer(playerId, client, ZongMenMsg.ZongMenUpdateContributeValueRes_40000058.newBuilder().setResult(true).build(),
                    PbProtocol.ZongMenUpdateContributeValueRes_40000058);
        } else {
			sendErrorCodeMsgToGameServer(playerId, client, result.getErrorCode(),
                    PbProtocol.ZongMenUpdateContributeValueRes_40000058);
        }
    }

	public void sendErrorCodeMsgToGameServer(long playerId, NetClient client, ErrorMsgEnum errorCode, int msgId) {
        ZongMenCrossMsg.ZongMenMsgResponse_41000046.Builder response = ZongMenCrossMsg.ZongMenMsgResponse_41000046.newBuilder();
        response.setMsgId(msgId);
        response.setPlayerId(playerId);
        response.setErrorCode(errorCode.getId());
        client.sendProtocol(response.build());
        ZongMenManager.log.error(String.format("%s %d %d", errorCode.getId() + ":" + errorCode.getDesc(), playerId, msgId));
    }

    public void sendMsgToGameServer(long playerId, NetClient client, Message message, int msgId) {
        ZongMenCrossMsg.ZongMenMsgResponse_41000046.Builder response = ZongMenCrossMsg.ZongMenMsgResponse_41000046.newBuilder();
        response.setMsgId(msgId);
        response.setErrorCode(ErrorMsgEnum.ok.getId());
        response.setPlayerId(playerId);
        response.setData(message.toByteString());
        client.sendProtocol(response.build());
        ZongMenManager.log.info(String.format("pid:%d msgId:%d message:%s", playerId, msgId, message));
    }
}

