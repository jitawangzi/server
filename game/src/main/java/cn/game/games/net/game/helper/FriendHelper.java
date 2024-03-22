package cn.game.games.net.game.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.entity.Friend;
import cn.game.games.cache.entity.FriendApplication;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.op.impl.FriendOp;
import cn.game.games.net.data.mapper.FriendApplicationMapper;
import cn.game.games.net.data.mapper.FriendMapper;
import cn.game.games.net.game.GameServer;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.util.DAO;

public class FriendHelper {

	private static final Logger log = LoggerFactory.getLogger(FriendHelper.class);

	public static void receiveApplication(long playerId, long applyPlayerId, String applyPlayerServer) {
		FriendApplication friendApplication = FriendApplication.valueOf(playerId, applyPlayerId, applyPlayerServer);
		if (PlayerManager.getInstance().hasCache(playerId)) {
			Player player = PlayerManager.getInstance().getPlayer(playerId);
			FriendOp friendOp = player.getModule(FriendOp.class);
			if (friendOp.isApplicationLimit()) {
				return; 
			}
			if (friendOp.isBlack(applyPlayerId)) {
				return ; 
			}
			if (friendOp.addApplication(friendApplication)) {
				DAO.execute(FriendApplicationMapper.class, MapperConstant.insert,
						friendApplication);
			}
		} else {
			// 这里离线增加可能会重复，不过也没有问题，上线处理
			DAO.execute(FriendApplicationMapper.class, MapperConstant.insert,
					friendApplication);
		}
	}
	
	/**
	 * @Description
	 * @param playerId
	 * @param friendId
	 * @param friendServer
	 */
	public static boolean addFriend(long playerId, long friendId, String friendServer, byte relation) {
		if (PlayerManager.getInstance().hasCache(playerId)) {
			Player player = PlayerManager.getInstance().getPlayer(playerId);
			FriendOp friendOp = player.getModule(FriendOp.class);
			return friendOp.addFriend(friendId, friendServer, relation);

		} else {
			String methed = GameServer.getInstance().isLocalServer(friendServer)
					? "selectFriendLocalCount"
					: "selectFriendOtherCount";
			int count = (int) DAO.executeSync(FriendMapper.class, methed,
					new Object[] { playerId, ServerContext.getInstance().getServerId() });

			if (count < FriendOp.maxFriends) {

				Friend add = Friend.valueOf(playerId, friendId, relation);
				DAO.insert(FriendMapper.class, add);
				return true;
			}
		}
		return false;
	}

	/**
	 * @Description 成为好友后，如果我也申请对方为好友了，删除这个申请
	 * @param playerId
	 * @param applyPlayerId
	 * @return
	 */
	public static void removeMyApplication(long playerId, long applyPlayerId) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		if (PlayerManager.getInstance().hasCache(playerId)) {
			FriendOp friendOp = player.getModule(FriendOp.class);
			boolean remove = friendOp.removeMyApplications(applyPlayerId);
			if (remove) {
				DAO.execute(FriendApplicationMapper.class,
						MapperConstant.deleteByPrimaryKey, new Object[] { applyPlayerId, playerId });
			}

		} else {
			DAO.execute(FriendApplicationMapper.class, MapperConstant.deleteByPrimaryKey,
					new Object[] { applyPlayerId, playerId });
		}
	}
	public static void removeApplication(long playerId, long applyPlayerId) {

		if (PlayerManager.getInstance().hasCache(playerId)) {
			Player player = PlayerManager.getInstance().getPlayer(playerId);

			FriendOp friendOp = player.getModule(FriendOp.class);
			FriendApplication remove = friendOp.getAllApplications().remove(applyPlayerId);
			if (remove != null) {
				DAO.execute(FriendApplicationMapper.class,
						MapperConstant.deleteByPrimaryKey, new Object[] { playerId, applyPlayerId });
			}

		} else {
			DAO.execute(FriendApplicationMapper.class, MapperConstant.deleteByPrimaryKey,
					new Object[] { playerId, applyPlayerId });
		}
	}

	/**
	 * @Description 单向删除好友
	 * @param playerId
	 * @param friendId
	 */
	public static void deleteFriend(long playerId, long friendId) {

		if (PlayerManager.getInstance().hasCache(playerId)) {
			Player player = PlayerManager.getInstance().getPlayer(playerId);

			FriendOp friendOp = player.getModule(FriendOp.class);
			if (!friendOp.hasRelation(friendId)) {
				return; 
			}
			friendOp.delete(friendId);

		} else {
			delete(playerId, friendId);
		}
	}

	/**
	 * @Description 判断单向拉黑关系
	 * @param playerId
	 *            拉黑操作发起方
	 * @param friendId
	 * @return
	 */
	public static boolean isBlack(long playerId, long friendId) {

		boolean hasCache = PlayerManager.getInstance().hasCache(playerId); 
		if (hasCache) {
			Player player = PlayerManager.getInstance().getPlayer(playerId);

			FriendOp friendOp = player.getModule(FriendOp.class);
			return friendOp.isBlack(friendId) ; 
		}else {

			Long pid = (Long) DAO.executeSync(FriendMapper.class, "selectBlack",
					new Object[] { playerId, friendId, Friend.BLACK });

			return pid != null && pid > 0;
		}
	}

	public static void delete(Friend friend) {
		if (friend == null) {
			return ; 
		}
		delete(friend.getPlayerId(), friend.getFriendId());
	}
	public static void delete(long playerId, long friendId) {
		DAO.execute(FriendMapper.class, MapperConstant.deleteByPrimaryKey, new Object[] {
				playerId, friendId });
	}
	public static void insert(Friend friend) {
		DAO.execute(FriendMapper.class, MapperConstant.insert, friend);
	}

	public static void update(Friend friend) {
		DAO.execute(FriendMapper.class, MapperConstant.updateByPrimaryKeySelective,
				friend);
	}

}
