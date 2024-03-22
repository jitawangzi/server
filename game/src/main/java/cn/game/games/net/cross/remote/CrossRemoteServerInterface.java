package cn.game.games.net.cross.remote;

import java.util.List;

import cn.game.games.cache.entity.Group;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.module.chat.GroupAllInfo;
import cn.game.games.net.game.remote.ServerStatus;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.Pair;

/**
 * @Description 跨服提供给其他服务器调用的接口
 * @date 2021年4月12日 下午2:10:22
 * @author SYQ
 */
public interface CrossRemoteServerInterface {
	
	public List<SimplePlayer> getSimplePlayers(List<Long> playerIds, List<String> serverIds);

	public List<SimplePlayer> getSimplePlayers(List<Pair<Long, String>> players);

	public SimplePlayer getSimplePlayer(long playerId, String serverId) throws Exception;
	
	public Group getOneGroupBriefInfo(long groupId, String serverId) throws Exception;
	
	public GroupAllInfo getChatGroupInfo(long groupId, String serverId) throws Exception;

	/**
	 * @Description 搜索查找好友数据，如果双方有黑名单关系，则不能查看数据
	 * @param playerId
	 *            被查看人
	 * @param searchPlayerId
	 *            查看人
	 * @param serverId
	 *            查看人所在服务器id
	 * @return
	 */
	public SimplePlayer searchFriendPlayer(long playerId, long searchPlayerId, String serverId) throws Exception;

	/**
	 * @Description 增加好友
	 * @param playerId
	 * @param friendId
	 * @param serverId
	 * @return
	 */
	public boolean addFriend(long playerId, long friendId, String serverId);

	public List<RewardInfo> addResources(long playerId, int id, int value, String serverId);
	
	public boolean addMail(long playerId, String serverId, int titleId, int contentId, int typeId, String resourceText);

	/**
	 * @Description 查询服务器状态
	 * @param serverIds
	 * @return
	 */
	@Deprecated
	public List<ServerStatus> serverStatus(String[] serverIds);

	@Deprecated
	public void stopServers(String[] serverIds);


}
