package cn.game.games.net.cross.guild;

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
import cn.game.games.net.cross.guild.service.GuildServiceInterface;
import cn.game.games.net.game.GameServer;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.guild.GuildHandler;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BaseMsg;
import cn.game.protocol.protobuf.RewardMsg;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.protocol.protobuf.GuildMsg;
import cn.game.protocol.protobuf.GuildMsg.GuildAllInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildPersonalInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildServiceInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildServiceInfo;
import cn.game.util.RedisUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * @ClassName GuildHelper
 *
 * @description:
 * @author: ly
 * @create: 2025-02-06 15:00 @Version 1.0
 */
public class GuildHelper {
	private static final long DEFAULT_PREFIX = 88L;

	public static long createGuildId() {
//        long id = Integer.parseInt(ServerContext.getInstance().getServerId()) << 32 | size;
//        return id;
		// - 公会编号生成：888（默认前缀）0001（注册账号给的标签数）0001（创建顺序），举例：289服的第123个公会编号是88802890123；
		long id = (DEFAULT_PREFIX * 10000000) + IdUtil.getIdAutoIncrease(IdType.GUILD);
		return id;

	}

	public static String getServerIdByGuildId(long guildId) {
		return IdCache.getGuildServerId(guildId);
//		return "LY_ZONG_MEN";
	}

	/**
	 * 异步获取公会列表
	 *
	 * @param guildIdList
	 * @return
	 */
	public static CompletionStage<List<Object>> getSimpleGuildListAsync(List<Long> guildIdList) {
		List<String> keyList = new ArrayList<>();
		for (Long guildId : guildIdList) {
			keyList.add(CacheType.ZONG_MEN_SIMPLE_DATA.key(guildId));
		}
		return RedisLocalCache.getInstance().multiGetAsync(keyList).toCompletionStage();
	}

	public static Future<SimpleGuild> getSimpleGuildAsync(long guildId) {
		Promise<SimpleGuild> promise = Promise.promise();
		List<Long> keys = new ArrayList<>();
		keys.add(guildId);
		getSimpleGuildListAsync(keys).thenAccept(list -> {
			if (list == null || list.size() == 0) {
				promise.complete(null);
				return;
			}
			SimpleGuild simpleGuild = (SimpleGuild) list.get(0);
			promise.complete(simpleGuild);

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
		ServerMsg.NotifyGuildMsgToGame_7d000047.Builder builder = ServerMsg.NotifyGuildMsgToGame_7d000047.newBuilder();
		builder.setMsgId(msgId);
		builder.setData(msg.toByteString());
		builder.addPlayerId(playerId);
		VxHolder.executeBlockingWithTimeout(() -> {
			String serverId = IdCache.getPlayerServerId(playerId);
			GuildManager.log
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
		ServerMsg.NotifyGuildMsgToGame_7d000047.Builder builder = ServerMsg.NotifyGuildMsgToGame_7d000047.newBuilder();
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
		GuildManager.log
				.info("notifyMsgToPlayer msgId : " + msgId + " msg : " + msg + " serverIdList : " + serverIdList + " pidSb : " + pidSb);
		ServerMsg.NotifyGuildMsgToGame_7d000047 req = builder.build();
		serverIdList.forEach(serverId -> {
			VxHolder.requestRemoteServer(serverId, req);
		});
	}

	public static Future<GuildHandler.GuildCallbackMsg> sendMsgToGuildServer(Player player, Message req, String... params) {
		return GuildHandler.sendMsgToGuildServer(player, req);
	}

	public static boolean checkGuildNameRepeat(String name) {
		String nameKey = getNameKey(name);
		return RedisUtil.get(nameKey) != null;
	}

	public static List<GuildMsg.GuildMemberInfo.Builder> sortMemberList(Collection<GuildMsg.GuildMemberInfo.Builder> values) {
		List<GuildMsg.GuildMemberInfo.Builder> list = new ArrayList<>(values);
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

	public static String getNameKey(String name) {
		return CacheType.ZONG_MEN_NAME_ID.key(name);
	}

	public static boolean trySetName(String name, long newGuildId) {
		String nameKey = getNameKey(name);
		return RedisUtil.trySet(nameKey, newGuildId);
	}

	public static GuildAllInfo buildAllInfo(GuildServiceInfo serviceInfo, long playerId) {
		GuildAllInfo.Builder builder = GuildAllInfo.newBuilder();
		builder.setSharedInfo(serviceInfo.getSharedInfo());
		builder.setShowInfo(serviceInfo.getShowInfo());
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		GuildPersonalInfo personalInfo = player.getGuildModule().toPersonalInfo();
		builder.setPersonalInfo(personalInfo);
		return builder.build();

	}
}
