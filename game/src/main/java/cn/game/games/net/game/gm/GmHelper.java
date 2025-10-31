package cn.game.games.net.game.gm;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import cn.game.games.cache.entity.GmMail;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.award.Goods;
import cn.game.protocol.protobuf.BaseMsg;
import cn.game.protocol.protobuf.GmMsg;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * @ClassName GmHelper
 *
 * @description:
 * @author: ly
 * @create: 2024-09-12 17:26 @Version 1.0
 */
public class GmHelper {

  public static GmMsg.GmMailInfo toGmMailPb(GmMail gmMail) throws ParseException {
    GmMsg.GmMailInfo.Builder builder = GmMsg.GmMailInfo.newBuilder();
    builder.setUid(gmMail.getId() + "");
    builder.setType(gmMail.getMailopttype());
    builder.setTitle(gmMail.getTitle());
    builder.setContent(gmMail.getContext());
    builder.setSendName(gmMail.getSendName() == null ? "null": gmMail.getSendName());
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
      String[] strs = gmMail.getServerids().replace("[","").replace("]","").split(",");
      for (String str : strs) {
        builder.addServerId(str);
      }
    }
	builder.setTimeCheckType(gmMail.getTimeCheckType());
	builder.setLevelStart(gmMail.getMinLevel());
    builder.setLevelEnd(gmMail.getMaxLevel());
    long sendStartTimer = 0;
    long sendEndTimer = 0;
    if (gmMail.getSendStartTimer() != null){
      sendStartTimer = DateUtil.getLongDate(gmMail.getSendStartTimer())/1000;
    }
    if (gmMail.getSendEndTimer() != null){
      sendEndTimer = DateUtil.getLongDate(gmMail.getSendEndTimer())/1000;
    }
    builder.setSendStartTime((int)sendStartTimer);
    builder.setSendEndTime((int)sendEndTimer);

    if (gmMail.getPids() != null) {
      String[] strs = gmMail.getPids().replace("[","").replace("]","").split(",");
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
