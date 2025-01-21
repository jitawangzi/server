package cn.game.games.net.game.remote;

import java.util.List;

import cn.game.core.net.remote.RemoteGameServerInterface;
import io.vertx.core.Future;

/**
 * GameServer提供给其他服务器（Game、Cross）调用的接口
 * 2020年11月30日 上午10:35:37
 * @author SYQ
 */
public interface GameServerInterface extends RemoteGameServerInterface {

	public boolean addFriend(long playerId, long friendId, String serverId);

	public boolean addMail(long playerId, String serverId, int titleId, int contentId, int typeId, String resourceText);

	public void notifyAddForbidAccount(List<Long> pids, String reason, String timer );
	public void notifyDelForbidAccount(List<Long> pids );
	public void addGlobalGmMail(int mailId);

	void delGlobalGmMail(int mailId);

	/** 
	 * 改名
	 * @param playerId
	 * @param name
	 * @return
	 */
	public Future<?> rename(long playerId, String name);
}
