package cn.game.games.net.cross.zongmen;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import com.google.protobuf.Message;

import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.IdUtil;
import cn.game.core.util.IdUtil.IdType;
import cn.game.games.cache.id.IdCache;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.protobuf.ServerMsg;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * @ClassName ZongMenHelper
 *
 * @description:
 * @author: ly
 * @create: 2025-02-06 15:00 @Version 1.0
 */
public class ZongMenHelper {
	private static final long DEFAULT_PREFIX = 88L;

	public static long createZongMenId() {
//        long id = Integer.parseInt(ServerContext.getInstance().getServerId()) << 32 | size;
//        return id;
		// - 宗门编号生成：888（默认前缀）0001（注册账号给的标签数）0001（创建顺序），举例：289服的第123个宗门编号是88802890123；
		long id = (DEFAULT_PREFIX * 10000000) + IdUtil.getIdAutoIncrease(IdType.ZONGMEN);
		return id;

    }
    public static String getServerIdByZongMenId(long zongMenId){
//        return String.valueOf(zongMenId >> 32);
		return IdCache.getZongMenServerId(zongMenId);
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
			String serverId = IdCache.getPlayerServerId(playerId);
            ZongMenManager.log.info("notifyMsgToPlayer playerId : " + playerId + " serverId : " + serverId +" msgId : " + msgId + " msg : " + msg);
            return VxHolder.requestRemoteServer(serverId, builder.build());
        });
    }

    public static boolean isZongMenAsset(int idType){
        List<Integer> list = new ArrayList<>();
        list.contains(1);
        return idType == Asset.ZongMenPoint.ID || idType == Asset.ZongMenExp.ID || idType == Asset.ZongMenContribute.ID;
    }

    /**
     * 是否包含 宗门的资源
     * @param assetArr 资源
     * @return true 包含 需要去宗门服务器处理 扣除逻辑 ，false 不包含
     */
    public static boolean containsZongMenAsset(int[][] assetArr){
        for (int[] arr : assetArr ) {
            int idType = arr[0];
            if (isZongMenAsset(idType)){
                return true;
            }
        }
        return false;
    }

}
