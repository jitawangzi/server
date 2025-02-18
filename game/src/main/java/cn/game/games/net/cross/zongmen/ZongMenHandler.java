package cn.game.games.net.cross.zongmen;

import java.util.ArrayList;
import java.util.List;

import cn.game.protocol.protobuf.ChatMsg;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.protocol.generated.config.GuildBargainConfig;
import cn.game.protocol.generated.config.GuildPermissionsConfig;
import cn.game.protocol.generated.config.ShopItemConfig;
import cn.game.protocol.generated.config.ZongmenStoreConfig;
import cn.game.protocol.generated.manager.GuildBargainManager;
import cn.game.protocol.generated.manager.GuildPermissionsManager;
import cn.game.protocol.generated.manager.ShopItemManager;
import cn.game.protocol.generated.manager.ZongmenStoreManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.util.LockUtil;
import cn.game.util.Rnd;

/**
 * @ClassName ZongMenHandler
 *
 * @description:
 * @author: ly
 * @create: 2025-02-06 15:11 @Version 1.0
 */
@Component
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
    ZongMenManager.log.info(String.format("dispatchMsg pid:%s zongMenId:%s cmd:%s req:%s", playerId, zongMenId, message.getClass().getSimpleName(),message));
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
        case PbProtocol.getZongMenLogRequest_40000025 ->
                getZongMenLog(zongMenId, playerId, message, paramList, client);
        case PbProtocol.setZongMenMemberPositionRequest_40000015 ->
                setZongMenMemberPosition(zongMenId, playerId, message, paramList, client);
        case PbProtocol.quitZongMenRequest_40000017 ->
                quitZongMen(zongMenId,playerId, message, paramList, client);
        case PbProtocol.updateZongMenAssetRequest_40000037 ->
                updateZongMenAsset(zongMenId, playerId, message, paramList, client);
        case PbProtocol.updateMemberAuthRequest_40000041 ->
                updateMemberAuth(zongMenId,playerId,message,paramList,client);
        case PbProtocol.ZongMenActiveRewardRequest_40000045 ->
                ZongMenActiveReward(zongMenId, playerId, message, paramList, client);
        case PbProtocol.getZongMenShopRequest_40000027 -> 
                getZongMenShop(zongMenId,playerId,message,paramList,client);
        case PbProtocol.ZongMenBuyShopRequest_40000047 ->
                ZongMenBuyShop(zongMenId, playerId, message, paramList, client);
        case PbProtocol.ZongMenBargainRequest_40000060 ->
			bargain(zongMenId, playerId, message, paramList, client);
        case PbProtocol.ZongMenBargainBuyRequest_40000062 ->
			buyBargain(zongMenId, playerId, message, paramList, client);
          case PbProtocol.findZongMenRequest_40000003 ->
                  findZongMen(zongMenId,playerId, message, paramList, client);
          case PbProtocol.ChatRequest_31000001 ->
                  zongMenChat(zongMenId,playerId, message, paramList, client);
        case PbProtocol.ZongMenUpdateMemberFightPower_40000052 ->
                updateMemberFightPower(zongMenId, playerId, message, paramList, client);
        case PbProtocol.ZongMenUpdateContributeValueReq_40000057 ->
                updateContributeValue(zongMenId, playerId, message, paramList, client);
      }

    });
  }

  //同步宗门个人贡献度
  private void updateContributeValue(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
        ZongMenMsg.ZongMenUpdateContributeValueReq_40000057 req = (ZongMenMsg.ZongMenUpdateContributeValueReq_40000057) message;
        ZongMenMsg.ZongMenUpdateContributeValueRes_40000058.Builder res = ZongMenMsg.ZongMenUpdateContributeValueRes_40000058.newBuilder();
        if (zongMenInfo == null) {
          sendErrorCodeMsgToGameServer(playerId, client, ErrorMsgEnum.zong_men_not_exist, PbProtocol.ZongMenUpdateContributeValueRes_40000058);
            return;
        }
      ZongMenMember member = zongMenInfo.getMember(playerId);
      if (member == null) {
        sendErrorCodeMsgToGameServer(playerId, client, ErrorMsgEnum.zong_men_not_exist, PbProtocol.ZongMenUpdateContributeValueRes_40000058);
        return;
      }
      member.setTotalContribution(req.getValue());
      res.setResult(true);
      sendMsgToGameServer(playerId, client, res.build(), PbProtocol.ZongMenUpdateContributeValueRes_40000058);
  }

  //玩家战斗力同步
  private void updateMemberFightPower(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
        if (zongMenInfo == null) {
            return;
        }
        ZongMenMember member = zongMenInfo.getMember(playerId);
        if (member == null) {
            return;
        }
        member.setPower(((ZongMenMsg.ZongMenUpdateMemberFightPower_40000052)message).getFightPower() );
    }

  //宗门商店
  private void getZongMenShop(long zongMenId, long playerId, Message message, List<String> params){

  }

  //宗门聊天
    private void zongMenChat(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
        ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
        if (zongMenInfo == null) {
            sendErrorCodeMsgToGameServer(
                    playerId,
                    client,
                    ErrorMsgEnum.zong_men_not_exist,
                    PbProtocol.ChatResponse_31000002);
            return;
        }
        List<Long> memberIdList = new ArrayList<>(zongMenInfo.getModule().menMemberMap.keySet());
        memberIdList.remove(playerId);
        ZongMenHelper.broadcastNotifyMsgToPlayer(message,PbProtocol.ChatRequest_31000001,memberIdList);
    }

    //查找宗门
    private void findZongMen(long zongMenId,long playerId, Message message, List<String> paramList, NetClient client) {
        ZongMenMsg.findZongMenResponse_40000004.Builder res = ZongMenMsg.findZongMenResponse_40000004.newBuilder();
        ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
        if (zongMenInfo == null) {
            sendErrorCodeMsgToGameServer(
                    playerId,
                    client,
                    ErrorMsgEnum.zong_men_not_exist,
                    PbProtocol.findZongMenResponse_40000004);
            return;
        }
        res.setZongMen(zongMenInfo.toProto());
        sendMsgToGameServer(playerId, client, res.build(), PbProtocol.findZongMenResponse_40000004);
    }

	private void bargain(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
		ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
		if (zongMenInfo == null) {
			sendErrorCodeMsgToGameServer(playerId, client, ErrorMsgEnum.zong_men_not_exist, PbProtocol.ZongMenBargainResponse_40000061);
			return;
		}
		ZongMenMember member = zongMenInfo.getMember(playerId);
		if (member == null) {
			sendErrorCodeMsgToGameServer(playerId, client, ErrorMsgEnum.zong_men_player_member_not_exist,
					PbProtocol.ZongMenBargainResponse_40000061);
			return;
		}
		if (member.isBargain) {
			sendErrorCodeMsgToGameServer(playerId, client, ErrorMsgEnum.repeat_request, PbProtocol.ZongMenBargainResponse_40000061);
			return;
		}
		ZongMenBargain bargain = zongMenInfo.getModule().getBargain();
		GuildBargainConfig guildBargainConfig = GuildBargainManager.instance().get(bargain.getBargainItemId());
		int bargainTotalNum = bargain.getBargainTotalNum();
		int bargainCount = Rnd.nextInt(guildBargainConfig.Bargain[0], guildBargainConfig.Bargain[1] + 1);
		if (bargainCount + bargainTotalNum > guildBargainConfig.Price[1]) {
			bargainCount = guildBargainConfig.Price[1] - bargainTotalNum;
		}
		bargain.setBargainTotalNum(bargainTotalNum + bargainCount);
		bargain.setMemberBargainNum(bargain.getMemberBargainNum() + 1);

		member.setBargain(true);
		member.setBargainTime(System.currentTimeMillis());

	}

	private void buyBargain(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {

		ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
		if (zongMenInfo == null) {
			sendErrorCodeMsgToGameServer(playerId, client, ErrorMsgEnum.zong_men_not_exist, PbProtocol.ZongMenBargainBuyResponse_40000063);
			return;
		}
		ZongMenMember member = zongMenInfo.getMember(playerId);
		if (member == null) {
			sendErrorCodeMsgToGameServer(playerId, client, ErrorMsgEnum.zong_men_player_member_not_exist,
					PbProtocol.ZongMenBargainBuyResponse_40000063);
			return;
		}
		if (!member.isBargain) {
			sendErrorCodeMsgToGameServer(playerId, client, ErrorMsgEnum.zong_men_player_not_bargain,
					PbProtocol.ZongMenBargainBuyResponse_40000063);
			return;
		}
		member.setBargainBuy(true);
	}

  // 购买宗门商店物品
  private void ZongMenBuyShop(
      long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
    ZongMenMsg.ZongMenBuyShopRequest_40000047 req =
        (ZongMenMsg.ZongMenBuyShopRequest_40000047) message;
    ZongMenMsg.ZongMenBuyShopResponse_40000048.Builder res =
        ZongMenMsg.ZongMenBuyShopResponse_40000048.newBuilder();
    int playerLv = Integer.parseInt(paramList.get(0));
    ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
    if (zongMenInfo == null) {
      sendErrorCodeMsgToGameServer(
          playerId,
          client,
          ErrorMsgEnum.zong_men_not_exist,
          PbProtocol.ZongMenBuyShopResponse_40000048);
      return;
    }
    ZongMenMember member = zongMenInfo.getMember(playerId);
    if (member == null) {
      sendErrorCodeMsgToGameServer(
          playerId,
          client,
          ErrorMsgEnum.zong_men_player_member_not_exist,
          PbProtocol.ZongMenBuyShopResponse_40000048);
      return;
    }
    int itemId = req.getItemId();
    int count = req.getCount();
    ZongmenStoreConfig config = ZongmenStoreManager.instance().getNullable(itemId);
    if (config == null) {
      sendErrorCodeMsgToGameServer(
          playerId,
          client,
          ErrorMsgEnum.config_data_not_found,
          PbProtocol.ZongMenBuyShopResponse_40000048);
      return;
    }
    if (playerLv < config.LevelUnlock) {
      sendErrorCodeMsgToGameServer(
          playerId,
          client,
          ErrorMsgEnum.level_not_enough,
          PbProtocol.ZongMenBuyShopResponse_40000048);
      return;
    }
    ShopItemConfig itemConfig = ShopItemManager.instance().getNullable(config.Item);
    if (itemConfig == null) {
      sendErrorCodeMsgToGameServer(
          playerId,
          client,
          ErrorMsgEnum.config_data_not_found,
          PbProtocol.ZongMenBuyShopResponse_40000048);
      return;
    }
    if (itemConfig.ShopItemQuota != 0
        && member.buyShopItemNumMap.getOrDefault(itemConfig.ID, 0) + count
            > itemConfig.ShopItemQuota) {
      sendErrorCodeMsgToGameServer(
          playerId,
          client,
          ErrorMsgEnum.shop_item_buy_count_max,
          PbProtocol.ZongMenBuyShopResponse_40000048);
      return;
    }
    if (!zongMenInfo.isEnoughAsset(itemConfig.PurchaseParameter, count, member)) {
      sendErrorCodeMsgToGameServer(
          playerId,
          client,
          ErrorMsgEnum.resource_not_enough,
          PbProtocol.ZongMenBuyShopResponse_40000048);
      return;
    }
    zongMenInfo.costAsset(itemConfig.PurchaseParameter, count, member);
    sendMsgToGameServer(playerId, client, res.build(), PbProtocol.ZongMenBuyShopResponse_40000048);
  }

  private void getZongMenShop(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
    ZongMenMsg.getZongMenShopResponse_40000028.Builder res = ZongMenMsg.getZongMenShopResponse_40000028.newBuilder();
    ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
    if (zongMenInfo == null) {
      sendErrorCodeMsgToGameServer(
              playerId,
              client,
              ErrorMsgEnum.zong_men_not_exist,
              PbProtocol.ZongMenActiveRewardResponse_40000046);
      return;
    }
    ZongMenMember member = zongMenInfo.getMember(playerId);
    if (member == null) {
      sendErrorCodeMsgToGameServer(
              playerId,
              client,
              ErrorMsgEnum.zong_men_player_member_not_exist,
              PbProtocol.getZongMenShopResponse_40000028);
      return;
    }
    res.setShopList(zongMenInfo.getModule().shop.toProto(member));
    sendMsgToGameServer(playerId, client,res.build(), PbProtocol.getZongMenShopResponse_40000028);
  }

  //领取宗门活跃度奖励
  private void ZongMenActiveReward(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
    ZongMenMsg.ZongMenActiveRewardRequest_40000045 req = (ZongMenMsg.ZongMenActiveRewardRequest_40000045) message;
    ZongMenMsg.ZongMenActiveRewardResponse_40000046.Builder res =
            ZongMenMsg.ZongMenActiveRewardResponse_40000046.newBuilder();
    ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
    if (zongMenInfo == null) {
      sendErrorCodeMsgToGameServer(
              playerId,
              client,
              ErrorMsgEnum.zong_men_not_exist,
              PbProtocol.ZongMenActiveRewardResponse_40000046);
      return;
    }
    ZongMenMember member = zongMenInfo.getMember(playerId);
    if (member == null) {
      sendErrorCodeMsgToGameServer(
              playerId,
              client,
              ErrorMsgEnum.zong_men_player_member_not_exist,
              PbProtocol.ZongMenActiveRewardResponse_40000046);
      return;
    }
    for (int index : req.getIndexListList()) {
      if (member.getRewardLivenessIndexList().contains(index)) {
        sendErrorCodeMsgToGameServer(
                playerId,
                client,
                ErrorMsgEnum.zong_men_active_reward_already_get,
                PbProtocol.ZongMenActiveRewardResponse_40000046);
        return;
      }
      member.getRewardLivenessIndexList().addAll(req.getIndexListList());
      sendMsgToGameServer(playerId, client,res.build(), PbProtocol.ZongMenActiveRewardResponse_40000046);
    }
  }

  //宗门成员权限管理
  private void updateMemberAuth(long zongMenId, long playerId, Message message, List<String> paramList, NetClient client) {
    ZongMenMsg.updateMemberAuthRequest_40000041 req = (ZongMenMsg.updateMemberAuthRequest_40000041) message;
    ZongMenMsg.updateMemberAuthResponse_40000042.Builder res =
            ZongMenMsg.updateMemberAuthResponse_40000042.newBuilder().setResult(true);
    ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
    String playerName = paramList.get(0);
    if (zongMenInfo == null) {
      sendErrorCodeMsgToGameServer(
              playerId,
              client,
              ErrorMsgEnum.zong_men_not_exist,
              PbProtocol.updateZongMenAssetResponse_40000038);
      return;
    }
    ZongMenMember member = zongMenInfo.getMember(playerId);
    GuildPermissionsConfig permissionsConfig = GuildPermissionsManager.instance().get(member.position);
    List<Long> targetPidList = new ArrayList<>();
    req.getTargetPidListList().forEach(pid ->{targetPidList.add(pid.longValue());});
    //加入审批检测
    if (req.getOptType() == 1 || req.getOptType() == 2){
      if (!permissionsConfig.Approval){
        sendErrorCodeMsgToGameServer(
                playerId,
                client,
                ErrorMsgEnum.zong_men_permission_not_enough,
                PbProtocol.updateMemberAuthResponse_40000042);
        return;
        }
      for(long targetPid : req.getTargetPidListList()){
        if (!zongMenInfo.hasApply(targetPid)){
          sendErrorCodeMsgToGameServer(
                  playerId,
                  client,
                  ErrorMsgEnum.request_parameter_error,
                  PbProtocol.updateMemberAuthResponse_40000042);
          return;
        }

      }
        if (req.getOptType() == 1 && zongMenInfo.isFull()){
          sendErrorCodeMsgToGameServer(
                  playerId,
                  client,
                  ErrorMsgEnum.zong_men_full,
                  PbProtocol.updateMemberAuthResponse_40000042);
          }
    }
    //踢人 审批
    if (req.getOptType() == 3 ){
      if (!permissionsConfig.Rename){
        sendErrorCodeMsgToGameServer(
                playerId,
                client,
                ErrorMsgEnum.zong_men_permission_not_enough,
                PbProtocol.updateMemberAuthResponse_40000042);
        return;
      }
      for(long targetPid : req.getTargetPidListList()){
        if (!zongMenInfo.isHasMember(targetPid)){
          sendErrorCodeMsgToGameServer(
                  playerId,
                  client,
                  ErrorMsgEnum.zong_men_player_member_not_exist,
                  PbProtocol.updateMemberAuthResponse_40000042);
          return;
        }
      }
    }

    //审批同意添加成员
    if (req.getOptType() == 1){
      zongMenInfo.addMemberAuth(targetPidList, playerName);
    } else if (req.getOptType() == 2){//审批拒绝添加成员
      zongMenInfo.removeApplyAuth(targetPidList, playerName);
    } else if (req.getOptType() == 3){//踢人
      zongMenInfo.kickMember(targetPidList, playerName);
    }
    sendMsgToGameServer(playerId, client,res.build(), PbProtocol.updateMemberAuthResponse_40000042);
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
    GuildPermissionsConfig permissionsConfig = GuildPermissionsManager.instance().get(member.position);
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
    GuildPermissionsConfig targetPermissionsConfig = GuildPermissionsManager.instance().get(req.getPosition());
    if (targetPositionNum >= targetPermissionsConfig.Number) {
      sendErrorCodeMsgToGameServer(
              playerId,
              client,
              ErrorMsgEnum.zong_men_position_member_num_not_enough,
              PbProtocol.setZongMenMemberPositionResponse_40000016);
      return;
    }

    ZongMenMember targetMember = zongMenInfo.getMember(req.getTargetPid());
    if (member == targetMember || req.getPosition() == targetMember.getPosition()){
      sendErrorCodeMsgToGameServer(
              playerId,
              client,
              ErrorMsgEnum.request_parameter_error,
              PbProtocol.setZongMenMemberPositionResponse_40000016);
      return;
    }
    if (req.getPosition() == ZongMenConstants.ZONG_MEN_POSITION_ZONG_ZHU){// 转让宗主
      zongMenInfo.zongZhuTransfer(member, targetMember);

    } else {
      int oldPosition = targetMember.getPosition();
      targetMember.setPosition(req.getPosition());
      zongMenInfo.handleEvent(ZongMenConstants.ZongMenEvenType.ZONG_MEN_POSITION_CHANGE,targetMember.playerId,oldPosition,req.getPosition());
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
    String playerName = paramList.get(0);
    if (zongMenInfo == null) {
      sendErrorCodeMsgToGameServer(
              playerId,
              client,
              ErrorMsgEnum.zong_men_not_exist,
              PbProtocol.dissolveZongMenResponse_40000012);
      return;
    }
    ZongMenMember member = zongMenInfo.getMember(playerId);
    GuildPermissionsConfig permissionsConfig = GuildPermissionsManager.instance().get(member.position);
//
    // 修改宗门名称
    if (!StringUtils.isEmpty(req.getName())) {
      if (!permissionsConfig.Rename){
        sendErrorCodeMsgToGameServer(
                playerId,
                client,
                ErrorMsgEnum.zong_men_permission_not_enough,
                PbProtocol.setZongMenSettingResponse_40000014);
        return;
      }

      RedisLocalCache.getInstance()
              .getAsync(CacheType.ZONG_MEN_NAME_ID.key(req.getName()))
              .onSuccess(
                      (result) -> {
                        if (result == null) {//该名称 未被占用
                          boolean redisLock =
                                  LockUtil.tryLockNoWaitSync(
                                          6, CacheType.ZONG_MEN_NAME_CHANGE_LOCK.key(req.getName()));
                          if (redisLock) {
                            //删除旧的宗门 名称 id 映射
                            zongMenInfo.delZongMenNameIdRedisData();
                            zongMenInfo.getData().setName(req.getName());
                            // - 名称修改：玩家昵称修改宗门名称为宗门昵称；
                            zongMenInfo.handleEvent(ZongMenConstants.ZongMenEvenType.CHANGE_ZONG_MEN_NAME,playerName,req.getName());
                            //保存新的宗门 名称 id 映射
                            ZongMenManager.getInstance()
                                    .saveRedisNameIdMap(req.getName(), zongMenInfo.getId());
                            sendMsgToGameServer(
                                    playerId,
                                    client,
                                    res.build(),
                                    PbProtocol.setZongMenSettingResponse_40000014);
                          } else {
                            sendErrorCodeMsgToGameServer(
                                    playerId,
                                    client,
                                    ErrorMsgEnum.zong_men_name_repeat,
                                    PbProtocol.setZongMenSettingResponse_40000014);
                            return;
                          }
                        } else {//该名称 被占用
                          sendErrorCodeMsgToGameServer(
                                  playerId,
                                  client,
                                  ErrorMsgEnum.zong_men_name_repeat,
                                  PbProtocol.setZongMenSettingResponse_40000014);
                          return;
                        }
                      }).onFailure(err ->{
                err.printStackTrace();
                sendErrorCodeMsgToGameServer(
                        playerId,
                        client,
                        ErrorMsgEnum.zong_men_name_repeat,
                        PbProtocol.setZongMenSettingResponse_40000014);
              });
    }

    if (!req.getWxBytes().isEmpty()) {
      zongMenInfo.getModule().setting.setWx(req.getWx());
    }
    if (!req.getNoticeBytes().isEmpty()) {
      if (!permissionsConfig.Notice){
        sendErrorCodeMsgToGameServer(
                playerId,
                client,
                ErrorMsgEnum.zong_men_permission_not_enough,
                PbProtocol.setZongMenSettingResponse_40000014);
        return;
      }
      zongMenInfo.getData().setNotice(req.getNotice());
//    - 公告修改：玩家昵称修改了公告；
      zongMenInfo.handleEvent(ZongMenConstants.ZongMenEvenType.CHANGE_ZONG_MEN_NOTICE,playerName);
    }
    if (!req.getDeclarationBytes().isEmpty()) {
      if (!permissionsConfig.Manifesto){
        sendErrorCodeMsgToGameServer(
                playerId,
                client,
                ErrorMsgEnum.zong_men_permission_not_enough,
                PbProtocol.setZongMenSettingResponse_40000014);
        return;
      }
      zongMenInfo.getData().setDeclaration(req.getDeclaration());
//    - 宣言修改：玩家昵称修改了宣言；
      zongMenInfo.handleEvent(ZongMenConstants.ZongMenEvenType.CHANGE_ZONG_MEN_DECLARATION,playerName);
    }
    if (req.getIcon() != 0
            && zongMenInfo.getModule().setting.unlockIconMap. containsKey(req.getIcon())) {
      if (!permissionsConfig.Icon){
        sendErrorCodeMsgToGameServer(
                playerId,
                client,
                ErrorMsgEnum.zong_men_permission_not_enough,
                PbProtocol.setZongMenSettingResponse_40000014);
        return;
      }
      zongMenInfo.getData().setIcon(req.getIcon());
    }
    if (req.getAutoJoin() != 1 && req.getAutoJoin() != 2 && req.getAutoJoin() != 3) {
      zongMenInfo.getModule().setting.setAutoJoin(req.getAutoJoin());
    }
    if (req.getTianDaoLevel() != 0) {
      zongMenInfo.getModule().setting.setTianDaoLevel(req.getTianDaoLevel());
    }
    res.setResult(true);
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
    }  else if (zongMenInfo.getModule().setting.autoJoin == 2) {
        zongMenInfo.applyJoin(playerId);
    } else {
      sendErrorCodeMsgToGameServer(
              playerId,
              client,
              ErrorMsgEnum.zong_men_not_allow_join,
              PbProtocol.applyJoinZongMenResponse_40000008);
      return;
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
	String serverId = paramList.get(2);
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
			.createZongMen(name, playerId, createPlayerName, power, serverId)
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
    ZongMenManager.log.error(String.format("%s %d %d", errorCode.getId()+":"+errorCode.getDesc(), playerId, msgId));
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
    ZongMenManager.log.info(String.format("pid:%d msgId:%d message:%s", playerId, msgId,message));
    // TODO log message

  }
}
