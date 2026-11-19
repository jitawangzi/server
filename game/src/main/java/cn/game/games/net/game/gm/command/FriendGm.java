package cn.game.games.net.game.gm.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.entity.Friend;
import cn.game.games.cache.entity.FriendApplication;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.gm.GmAction;
import cn.game.games.net.game.gm.GmGroup;
import cn.game.games.net.game.gm.GmParam;
import cn.game.games.net.game.gm.GmScope;
import cn.game.games.net.game.gm.GmSecurityTier;
import cn.game.games.net.game.module.friend.FriendModule;
import cn.game.protocol.protobuf.FriendMsg.FriendAddPush_30000023;

/**
 * 好友系统调试GM指令分组
 *
 * @author shuyongqiang
 */
@Component
@GmGroup(name = "friend", description = "好友系统调试指令")
public class FriendGm extends AbstractGm {

	@GmAction(name = "add", description = "给当前玩家直接添加好友", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String add(Player player,
			@GmParam(description = "目标玩家ID") long targetPlayerId) {
		if (targetPlayerId <= 0 || targetPlayerId == player.getPlayerId()) {
			return "无效的目标玩家ID: " + targetPlayerId;
		}
		FriendModule friendModule = player.getModule(FriendModule.class);
		if (friendModule == null) {
			return "当前玩家未初始化好友模块";
		}
		if (friendModule.isFriend(targetPlayerId)) {
			return "目标玩家已经是好友: " + targetPlayerId;
		}
		String serverId = ServerContext.getInstance().getServerId();
		boolean success = friendModule.addFriend(targetPlayerId, serverId != null ? serverId : "local", Friend.FRIEND);
		if (success) {
			friendModule.getAllApplications().remove(targetPlayerId);
			if (player.getGameClient() != null) {
				player.getGameClient().sendProtocol(FriendAddPush_30000023.newBuilder()
						.setPlayerId(player.getPlayerId())
						.setFriendId(targetPlayerId)
						.setFriendServer(serverId != null ? serverId : "local")
						.build());
			}
			return "成功添加好友: " + targetPlayerId;
		}
		return "添加好友失败，可能已达上限";
	}

	@GmAction(name = "del", description = "删除当前玩家的指定好友", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String del(Player player,
			@GmParam(description = "目标好友ID") long targetPlayerId) {
		FriendModule friendModule = player.getModule(FriendModule.class);
		if (friendModule == null) {
			return "当前玩家未初始化好友模块";
		}
		friendModule.delete(targetPlayerId);
		return "已删除好友或关联: " + targetPlayerId;
	}

	@GmAction(name = "apply", description = "模拟接收到好友申请", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String apply(Player player,
			@GmParam(description = "申请人玩家ID") long applyPlayerId) {
		if (applyPlayerId <= 0 || applyPlayerId == player.getPlayerId()) {
			return "无效的申请人玩家ID: " + applyPlayerId;
		}
		FriendModule friendModule = player.getModule(FriendModule.class);
		if (friendModule == null) {
			return "当前玩家未初始化好友模块";
		}
		String serverId = ServerContext.getInstance().getServerId();
		FriendApplication application = FriendApplication.valueOf(player.getPlayerId(), applyPlayerId, serverId != null ? serverId : "local");
		friendModule.addApplication(application);
		return "已记录来自玩家 " + applyPlayerId + " 的好友申请";
	}

	@GmAction(name = "accept", description = "同意指定好友申请", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String accept(Player player,
			@GmParam(description = "申请人玩家ID") long applyPlayerId) {
		FriendModule friendModule = player.getModule(FriendModule.class);
		if (friendModule == null) {
			return "当前玩家未初始化好友模块";
		}
		boolean deal = friendModule.applicationDeal(applyPlayerId, true);
		return deal ? ("已同意玩家 " + applyPlayerId + " 的好友申请") : ("处理失败或申请不存在: " + applyPlayerId);
	}

	@GmAction(name = "reject", description = "拒绝指定好友申请", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String reject(Player player,
			@GmParam(description = "申请人玩家ID") long applyPlayerId) {
		FriendModule friendModule = player.getModule(FriendModule.class);
		if (friendModule == null) {
			return "当前玩家未初始化好友模块";
		}
		boolean deal = friendModule.applicationDeal(applyPlayerId, false);
		return deal ? ("已拒绝玩家 " + applyPlayerId + " 的好友申请") : ("申请不存在: " + applyPlayerId);
	}

	@GmAction(name = "black", description = "将玩家加入黑名单", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String black(Player player,
			@GmParam(description = "目标玩家ID") long targetPlayerId) {
		FriendModule friendModule = player.getModule(FriendModule.class);
		if (friendModule == null) {
			return "当前玩家未初始化好友模块";
		}
		friendModule.delete(targetPlayerId);
		String serverId = ServerContext.getInstance().getServerId();
		friendModule.addFriend(targetPlayerId, serverId != null ? serverId : "local", Friend.BLACK);
		return "已将玩家加入黑名单: " + targetPlayerId;
	}

	@GmAction(name = "unblack", description = "将玩家移出黑名单", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String unblack(Player player,
			@GmParam(description = "目标玩家ID") long targetPlayerId) {
		FriendModule friendModule = player.getModule(FriendModule.class);
		if (friendModule == null) {
			return "当前玩家未初始化好友模块";
		}
		if (friendModule.isBlack(targetPlayerId)) {
			friendModule.delete(targetPlayerId);
			return "已移出黑名单: " + targetPlayerId;
		}
		return "玩家不在黑名单中: " + targetPlayerId;
	}

	@GmAction(name = "gift", description = "设置好友送礼及可领状态", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String gift(Player player,
			@GmParam(description = "目标好友ID") long friendId,
			@GmParam(description = "是否标记对方送我礼物", required = false, defaultValue = "true") boolean gifted,
			@GmParam(description = "是否已领取该礼物", required = false, defaultValue = "false") boolean receive) {
		FriendModule friendModule = player.getModule(FriendModule.class);
		if (friendModule == null) {
			return "当前玩家未初始化好友模块";
		}
		Friend friend = friendModule.getFriend(friendId);
		if (friend == null) {
			return "未找到好友: " + friendId;
		}
		friend.setGifted(gifted);
		friend.setReceive(receive);
		return "好友 " + friendId + " 礼物状态已更新: gifted=" + gifted + ", receive=" + receive;
	}

	@GmAction(name = "intimate", description = "增减指定好友亲密度", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String intimate(Player player,
			@GmParam(description = "目标好友ID") long friendId,
			@GmParam(description = "亲密度增量") int count) {
		FriendModule friendModule = player.getModule(FriendModule.class);
		if (friendModule == null) {
			return "当前玩家未初始化好友模块";
		}
		Friend friend = friendModule.getFriend(friendId);
		if (friend == null) {
			return "未找到好友: " + friendId;
		}
		friend.setIntimate(Math.max(0, friend.getIntimate() + count));
		return "好友 " + friendId + " 当前亲密度: " + friend.getIntimate();
	}

	@GmAction(name = "clear", description = "清空当前玩家所有好友、申请与黑名单", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String clear(Player player) {
		FriendModule friendModule = player.getModule(FriendModule.class);
		if (friendModule == null) {
			return "当前玩家未初始化好友模块";
		}
		List<Friend> allFriends = new ArrayList<>(friendModule.getAllFriends());
		for (Friend f : allFriends) {
			friendModule.delete(f.getFriendId());
		}
		List<Friend> allBlack = new ArrayList<>(friendModule.getAllBlack());
		for (Friend f : allBlack) {
			friendModule.delete(f.getFriendId());
		}
		friendModule.getAllApplications().clear();
		return "已清空当前玩家的所有好友、黑名单和好友申请";
	}

	@GmAction(name = "list", description = "查看当前玩家好友概况", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public Map<String, Object> list(Player player) {
		Map<String, Object> res = new LinkedHashMap<>();
		FriendModule friendModule = player.getModule(FriendModule.class);
		if (friendModule == null) {
			res.put("error", "未初始化好友模块");
			return res;
		}
		res.put("playerId", player.getPlayerId());
		res.put("friendCount", friendModule.getAllFriends().size());
		res.put("friends", friendModule.getAllFriends().stream().map(f -> Map.of(
				"friendId", f.getFriendId(),
				"relation", (int) f.getRelation(),
				"gifted", f.getGifted(),
				"receive", f.getReceive(),
				"intimate", f.getIntimate()
		)).toList());
		res.put("applicationCount", friendModule.getAllApplications().size());
		res.put("applications", friendModule.getAllApplications().keySet());
		res.put("blackCount", friendModule.getAllBlack().size());
		res.put("blacklist", friendModule.getAllBlack().stream().map(Friend::getFriendId).toList());
		return res;
	}
}
