package cn.game.games.net.cross.zongmen;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.core.net.vertx.VxHolder;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.util.RedisUtil;
import com.google.protobuf.Message;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * @ClassName ZongMenHelper
 *
 * @description:
 * @author: ly
 * @create: 2025-02-06 15:00 @Version 1.0
 */
public class ZongMenHelper {
    public static long createZongMenId(long size){
        long id = Integer.parseInt(ServerContext.getInstance().getServerId()) << 32 | size;
        return id;
    }
    public static String getServerIdByZongMenId(long zongMenId){
        return String.valueOf(zongMenId >> 32);
    }

    /**
     * 异步获取宗门列表
     *
     * @param zongMenIdList
     * @return
     */
    public static CompletionStage<List<Object>> getSimpleZongMenListAsync(List<Long> zongMenIdList) {
        List<String> keyList = new ArrayList<>();
        for (Long zongMenId : zongMenIdList) {
            keyList.add(CacheType.ZONG_MEN_SIMPLE_DATA.key(zongMenId));
        }
        return RedisLocalCache.getInstance().multiGetAsync(keyList).toCompletionStage();
    }

    public static Future<SimpleZongMen> getSimpleZongMenAsync(long zongMenId) {
        Promise<SimpleZongMen> promise = Promise.promise();
        List<Long> keys = new ArrayList<>();
        keys.add(zongMenId);
        getSimpleZongMenListAsync(keys).thenAccept(list ->{
            if (list == null || list.size() == 0){
                promise.complete(null);
                return;
            }
          SimpleZongMen simpleZongMen = (SimpleZongMen) list.get(0);
            promise.complete(simpleZongMen);

        }).exceptionally(e ->{
            e.printStackTrace();
            promise.complete(null);
            return null;
        });
        return promise.future();
    }

    /**
     * 异步发送消息给 gameServer
     * @param playerId 玩家id
     * @param msg 消息
     * @param msgId 消息号
     */
    public static void notifyMsgToPlayer(long playerId, Message msg, int msgId) {
        ServerMsg.NotifyZongMenMsgToGame_7d000047.Builder builder = ServerMsg.NotifyZongMenMsgToGame_7d000047.newBuilder();
        builder.setMsgId(msgId);
        builder.setData(msg.toByteString());
        builder.setPlayerId(playerId);
        VxHolder.executeBlockingWithTimeout(()->{
            String serverId = getServerId(playerId);
            ZongMenManager.log.info("notifyMsgToPlayer playerId : " + playerId + " serverId : " + serverId +" msgId : " + msgId + " msg : " + msg);
            return VxHolder.requestRemoteServer(serverId, builder.build());
        });
    }

    private static String getServerId(long playerId) {
        return RedisUtil.get(CacheType.PLAYER_SERVER_ID.key(playerId));
    }
}
