package cn.game.games.net.cross.zongmen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.google.protobuf.Message;

import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.core.cache.id.IdCache;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.IdUtil;
import cn.game.core.util.IdUtil.IdType;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.zongmen.ZongMenHandler;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BaseMsg;
import cn.game.protocol.protobuf.RewardMsg;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.util.RedisUtil;
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

	public static String getServerIdByZongMenId(long zongMenId) {
		return IdCache.getZongMenServerId(zongMenId);
//		return "LY_ZONG_MEN";
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
		getSimpleZongMenListAsync(keys).thenAccept(list -> {
			if (list == null || list.size() == 0) {
				promise.complete(null);
				return;
			}
			SimpleZongMen simpleZongMen = (SimpleZongMen) list.get(0);
			promise.complete(simpleZongMen);

		}).exceptionally(e -> {
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
		builder.addPlayerId(playerId);
		VxHolder.executeBlockingWithTimeout(() -> {
			String serverId = IdCache.getPlayerServerId(playerId);
			ZongMenManager.log
					.info("notifyMsgToPlayer playerId : " + playerId + " serverId : " + serverId + " msgId : " + msgId + " msg : " + msg);
			return VxHolder.requestRemoteServer(serverId, builder.build());
		});
	}

	/**
	 * 异步广播发送消息给 gameServer
	 * @param msg
	 * @param msgId
	 * @param notifyPlayerId
	 */
	public static void broadcastNotifyMsgToPlayer(Message msg, int msgId, List<Long> notifyPlayerId) {
		ServerMsg.NotifyZongMenMsgToGame_7d000047.Builder builder = ServerMsg.NotifyZongMenMsgToGame_7d000047.newBuilder();
		builder.setMsgId(msgId);
		builder.setData(msg.toByteString());
		if (notifyPlayerId.size() == 0)
			return;
		Set<String> serverIdList = new HashSet<>();
		StringBuffer pidSb = new StringBuffer("pid:");
		for (long playerId : notifyPlayerId) {
			builder.addPlayerId(playerId);
			String serverId = IdCache.getPlayerServerId(playerId);
			serverIdList.add(serverId);
			pidSb.append(playerId).append(",");
		}
		pidSb.deleteCharAt(pidSb.length() - 1).append("]");
		ZongMenManager.log
				.info("notifyMsgToPlayer msgId : " + msgId + " msg : " + msg + " serverIdList : " + serverIdList + " pidSb : " + pidSb);
		ServerMsg.NotifyZongMenMsgToGame_7d000047 req = builder.build();
		serverIdList.forEach(serverId -> {
			VxHolder.requestRemoteServer(serverId, req);
		});
	}

	public static boolean isZongMenAsset(int idType) {
		return idType == Asset.ZongMenPoint.ID || idType == Asset.ZongMenExp.ID || idType == Asset.ZongMenContribute.ID;
	}

	/**
	 * 是否包含 宗门的资源
	 * @param assetArr 资源
	 * @return true 包含 需要去宗门服务器处理 扣除逻辑 ，false 不包含
	 */
	public static boolean containsZongMenAsset(int[][] assetArr) {
		for (int[] arr : assetArr) {
			int idType = arr[0];
			if (isZongMenAsset(idType)) {
				return true;
			}
		}
		return false;
	}

	public static RewardMsg.RewardInfo addZongMenResources(Player player, int id, int value, OpType opType) {
		if (id == Asset.ZongMenContribute.ID) {
			player.getZongmenModule().addContribute(value);
		}
		RewardMsg.RewardInfo rewardInfo = RewardMsg.RewardInfo.newBuilder()
				.setAsset(BaseMsg.AssetInfo.newBuilder().setId(id).setCount(value).build())
				.build();
		// 领取的是宗门任务奖励 则存储宗门奖励 并同步到宗门服务器
		// 同步宗门任务掉落 到宗门服务器
		ZongMenMsg.updateZongMenAssetRequest_40000037.Builder builder = ZongMenMsg.updateZongMenAssetRequest_40000037.newBuilder();
		builder.addZongMenAssetMaps(rewardInfo);
		sendMsgToZongMenServer(player, builder.build());
		return rewardInfo;
	}

	public static Future<ZongMenHandler.ZongMenCallbackMsg> sendMsgToZongMenServer(Player player, Message req, String... params) {
		return ZongMenHandler.sendMsgToZongMenServer(player, req);
	}

	public static CompletableFuture<Boolean> checkZongMenNameRepeat(Player player, String name) {
		Promise<Boolean> promise = Promise.promise();
		RedisLocalCache.getInstance().getAsync(CacheType.ZONG_MEN_NAME_ID.key(name)).onSuccess(id -> {
			if (id != null) {
				promise.complete(false);
			} else {
				promise.complete(true);
			}
		}).onFailure(err -> {
			promise.complete(false);
			err.printStackTrace();
		});
		return promise.future().toCompletionStage().toCompletableFuture();
	}

	public static List<ZongMenMsg.ZongMenMemberProto.Builder> sortMemberList(Collection<ZongMenMsg.ZongMenMemberProto.Builder> values) {
		List<ZongMenMsg.ZongMenMemberProto.Builder> list = new ArrayList<>(values);
//        - 成员排序规则：
//        - 在线状态：在线、离线（从近到远）；
//        - 职位、战力：从高到低；
		list.sort((o1, o2) -> {
			if (o1.getSimplePlayer().getOnline() && o2.getSimplePlayer().getOnline()) {// 在线
				if (o1.getSimplePlayer().getOfflineTime() == o2.getSimplePlayer().getOfflineTime()) {// 离线（从近到远）
					if (o1.getPosition() == o2.getPosition()) {// 职位
						if (o1.getSimplePlayer().getCombatEffectiveness() == o2.getSimplePlayer().getCombatEffectiveness()) {// 战力
							return 0;
						} else {
							return o1.getSimplePlayer().getCombatEffectiveness() - o2.getSimplePlayer().getCombatEffectiveness();
						}
					} else {
						return o2.getPosition() - o1.getPosition();
					}
				} else {
					return o1.getSimplePlayer().getOfflineTime() - o2.getSimplePlayer().getOfflineTime();
				}
			} else if (o1.getSimplePlayer().getOnline()) {
				return 1;
			} else {
				return -1;
			}
		});
		return list;
	}

	public static boolean isHasZongMen(long zongMenId, long targetPid) {
		return RedisUtil.get(CacheType.PLAYER_ID_ZONG_MEN_ID.key(targetPid, zongMenId)) == null ? false : true;
	}
}
