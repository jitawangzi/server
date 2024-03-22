package cn.game.games.cache.op.face;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.game.games.cache.entity.Friend;
import cn.game.games.cache.base.ICacheOp;
import cn.game.games.cache.entity.FriendApplication;
import cn.game.games.core.SimplePlayer;


public interface IFriendOp{

	//初始数据,
	public void initLoadData(List<Friend> friends,List<FriendApplication> applications);
	
	public boolean addFriend(long id, String serverId, byte relation);
	
	public Friend getFriend(long id);

	public Collection<Friend> getAllFriends();
	
	public Map<Long, FriendApplication> getAllApplications();
	
	public boolean addApplication(FriendApplication friendApplication);
	
	public boolean addMyApplication(long playerId);

	public boolean applicationDeal(long id, boolean result);
	
	/**
	 * @Description 判断是否是好友
	 * @param playerId
	 * @return
	 */
	public boolean isFriend(long playerId) ; 
	/**
	 * @Description 判断是否是好友，本服和全服的都算
	 * @param friend
	 * @return
	 */
	public boolean isFriend(Friend friend);
	/**
	 * @Description 判断是否是好友
	 * @param friend
	 * @param localServer
	 *            true 本服好友，false 全服好友
	 * @return
	 */
	public boolean isFriend(Friend friend, boolean localServer);
	/**
	 * @Description 判断是否在我黑名单里
	 * @param playerId
	 * @return
	 */
	public boolean isBlack(long playerId);
	
	/**
	 * @Description 是不是好友，或者在不在黑名单里
	 * @param playerId
	 * @return
	 */
	public boolean hasRelation(long playerId);

	public boolean isApplicationLimit() ; 
	

	public void delete(long friendId);

	/**
	 * @Description 好友数量是否达到最大
	 * @param localServer
	 *            是否是本服好友
	 * @return
	 */
	public boolean isFriendMax(boolean localServer);
	
	public void refreshDay();

	long getLastRefreshTime();

	void setLastRefreshTime(long lastRefreshTime);

	List<SimplePlayer> getLastRefreshPlayers();

	void setLastRefreshPlayers(List<SimplePlayer> lastRefreshPlayers);
	
	/**
	 * @Description 推荐好友时，不能出现的玩家id
	 * @return
	 */
	Set<Long> excludeIds();

	public boolean removeMyApplications(long playerId);
	 
}
