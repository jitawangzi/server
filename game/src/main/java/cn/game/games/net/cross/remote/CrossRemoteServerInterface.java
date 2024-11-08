package cn.game.games.net.cross.remote;

import java.util.List;

import cn.game.core.net.remote.ServerStatus;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**
 * 跨服提供给其他服务器调用的接口
 * 2021年4月12日 下午2:10:22
 * @author SYQ
 */
public interface CrossRemoteServerInterface {

	/**
	 * 增加好友
	 * @param playerId
	 * @param friendId
	 * @param serverId
	 * @return
	 */
	public boolean addFriend(long playerId, long friendId, String serverId);

	public List<RewardInfo> addResources(long playerId, int id, int value, String serverId);
	
	public boolean addMail(long playerId, String serverId, int titleId, int contentId, int typeId, String resourceText);

	/**
	 * 查询服务器状态
	 * @param serverIds
	 * @return
	 */
	@Deprecated
	public List<ServerStatus> serverStatus(String[] serverIds);

	@Deprecated
	public void stopServers(String[] serverIds);

	public void notifyBroadcastAddForbidAccount(List<Long> pids, String reason, String timer );
	public void notifyBroadcastDelForbidAccount(List<Long> pids );
	public void notifyBroadcastAddGlobalGmMail(int mailId);
	public void notifyBroadcastDelGlobalGmMail(int mailId);

}
