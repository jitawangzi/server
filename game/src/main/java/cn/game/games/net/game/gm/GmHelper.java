package cn.game.games.net.game.gm;

import cn.game.games.cache.entity.GmMail;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.util.DAO;
import cn.game.protocol.protobuf.BaseMsg;
import cn.game.protocol.protobuf.GmMsg;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName GmHelper
 *
 * @description:
 * @author: ly
 * @create: 2024-09-12 17:26 @Version 1.0
 */
public class GmHelper {
  public static Future<GmMsg.GmPlayerInfo> getPlayerInfo(String playerName, Long playerId) {
    Promise<GmMsg.GmPlayerInfo> promise = Promise.promise();
    PlayerHelper.seachPlayer(playerName, playerId)
        .onSuccess(
            row -> {
              promise.complete(row.toGmPlayerInfo());
            })
        .onFailure(err -> promise.fail(err));
    return promise.future();
  }

  public static GmMsg.GmMailInfo toGmMailPb(GmMail gmMail) throws ParseException {
    GmMsg.GmMailInfo.Builder builder = GmMsg.GmMailInfo.newBuilder();
    builder.setUid(gmMail.getId() + "");
    builder.setType(gmMail.getMailopttype());
    builder.setTitle(gmMail.getTitle());
    builder.setContent(gmMail.getContext());
    builder.setCreateTime((int) (gmMail.getCreateTime().getTime() / 1000L));
    builder.setCheckTime(
        gmMail.getApprovalTimer() == null
            ? 0
            : (int) (DateUtil.getLongDate(gmMail.getApprovalTimer()) / 1000L));
    if (gmMail.getAttachment() != null) {
      List<Goods> goodsList = getAttachment(gmMail);
      if (goodsList != null) {
        goodsList.forEach(
            goods -> {
              BaseMsg.GoodsInfo.Builder goodsBuilder = BaseMsg.GoodsInfo.newBuilder();
              goodsBuilder.setId(goods.getId());
              goodsBuilder.setCount(goods.getCount());
              builder.addAttachments(goodsBuilder.build());
            });
      }
    }
    builder.setStatus(gmMail.getOptFlag());
    if (gmMail.getServerids() != null) {
      String[] strs = gmMail.getServerids().split(";");
      for (String str : strs) {
        builder.addServerId(str);
      }
    }
    builder.setTimeCheckType(gmMail.getTimeCheckType() == null ? 0 : gmMail.getTimeCheckType());
    builder.setLevelStart(gmMail.getMinLevel() ==null ? 0 :gmMail.getMinLevel());
    builder.setLevelEnd(gmMail.getMaxLevel() == null ? 0 : gmMail.getMaxLevel());
    if (gmMail.getPids() != null) {
      String[] strs = gmMail.getPids().split(";");
      for (String str : strs) {
        builder.addPlayerIds(str);
      }
    }
    return builder.build();
  }

  public static List<Goods> getAttachment(GmMail gmMail) {
    if (gmMail.getAttachment() != null) {
      List<Goods> goodsList = JsonUtil.parseObjectWithType(gmMail.getAttachment());
      return goodsList;
    }
    return new ArrayList<>();
  }
}
