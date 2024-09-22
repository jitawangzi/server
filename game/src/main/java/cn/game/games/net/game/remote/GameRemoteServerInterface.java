package cn.game.games.net.game.remote;

import java.util.List;

import cn.game.games.core.SimplePlayer;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import io.vertx.core.Future;

/**
 * GameServer提供给其他服务器调用的接口
 * 2020年11月30日 上午10:35:37
 * @author SYQ
 */
public interface GameRemoteServerInterface {

	public SimplePlayer getSimplePlayer(long id) throws Exception;

	public Future<SimplePlayer> getSimplePlayerAsync(long id);

	public List<SimplePlayer> getSimplePlayers(List<Long> ids);

	public SimplePlayer searchFriendPlayer(long playerId, long searchPlayerId) throws Exception;

	public boolean addFriend(long playerId, long friendId, String serverId);

	public List<RewardInfo> addResources(long playerId, int id, int value);
	
	public boolean addMail(long playerId, String serverId, int titleId, int contentId, int typeId, String resourceText);

	public boolean alive();

	public void shutdown();

	public ServerStatus status();

	public boolean delResources(long playerId, int id, int value);
	
	/** 
	 * 获取玩家地图数据
	 * @return
	 */

	public List<Object> getExploreMap(long playerId);

	public void notifyAddForbidAccount(List<Long> pids, String reason, String timer );
	public void notifyDelForbidAccount(List<Long> pids );
	public void addGlobalGmMail(int mailId);

	void delGlobalGmMail(int mailId);
}
