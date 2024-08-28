package cn.game.games.net.game.module.friend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.entity.Friend;
import cn.game.games.cache.entity.FriendApplication;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.SimplePlayer;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.FriendApplicationMapper;
import cn.game.games.net.game.GameServer;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.helper.FriendHelper;
import cn.game.games.util.DAO;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

public class FriendModule extends BasePlayerModule {

	/** 好友数据 */
	@JsonIgnore
	private Map<Long, Friend> friends;

	/** 申请成为我的好友的玩家 */
	@JsonIgnore
	private Map<Long, FriendApplication> applications;
	
	/** 我申请的玩家id，推荐好友时不能包含我申请过的玩家id */
	@JsonIgnore
	private Set<Long> myApplications;

	/** 上次推荐好友换一批时间 */
	@JsonIgnore
	private long lastRefreshTime;

	@JsonIgnore
	private List<SimplePlayer> lastRefreshPlayers;

	public static final int maxFriends = 100;

	@Override
	public void init() {
		if (friends != null) {
			return;
		}
		friends = new HashMap<Long, Friend>();
		applications = new HashMap<>();
		myApplications = new HashSet<>();
		lastRefreshPlayers = new ArrayList<SimplePlayer>();
	}

	public void initLoadData(List<Friend> friends,List<FriendApplication> applications) {

		for (Friend friend : friends) {
			this.friends.put(friend.getFriendId(), friend);
		}

		for (FriendApplication friendApplication : applications) {
			if (friendApplication.getApplyPlayerId() == playerId) {
				this.myApplications.add(friendApplication.getPlayerId());
			} else {
				this.applications.put(friendApplication.getApplyPlayerId(), friendApplication);
			}
		}
		// 把我黑名单里边的申请删除掉,可能是离线申请的

		for (Friend friend : this.friends.values()) {

			if (friend.getRelation() == Friend.BLACK) {
				FriendHelper.removeApplication(playerId, friend.getFriendId());
			}

		}

//		if (applications!=null)
//		{
//			for (FriendApplication friendApplication : applications)
//			{
				
//				if((System.currentTimeMillis()-friendApplication.getApplyTime())>24*60*60*1000*3){
//					DAO.execute(FriendApplicationMapper.class, MapperConstant.deleteByPrimaryKey, friendApplication) ; 
//					continue ; 
//				}
//				this.applications.add(friendApplication.getApplyPlayerId()) ; 
//			}
			
//		}
		
	}

	public boolean addFriend(long id, String serverId, byte relation) {
		
		if (relation == Friend.FRIEND) {
			// 在判断下好友数量
			boolean friendMax = isFriendMax(GameServer.getInstance().isLocalServer(serverId)); 
			if (friendMax) {
				return false ; 
			}
		}

		Friend friend = Friend.valueOf(playerId, id, serverId, relation);
		this.friends.put(id, friend);

		FriendHelper.insert(friend);
		return true ; 
		
	}
	public Collection<Friend> getAllFriends() {

		return this.friends.values();
	}

	public Map<Long, FriendApplication> getAllApplications() {

		return this.applications;
	}

	public boolean addApplication(FriendApplication friendApplication) {

		this.applications.put(friendApplication.getApplyPlayerId(), friendApplication);
		return true;
	}

	public boolean applicationDeal(long id, boolean result) {

		FriendApplication delApplication = this.applications.remove(id);
		if (delApplication == null) { 
			return false;
		}
		DAO.execute(FriendApplicationMapper.class, MapperConstant.deleteByPrimaryKey,
				new Object[] { playerId, id });

		if (result) {
			if (isFriendMax(GameServer.getInstance().isLocalServer(delApplication.getApplyPlayerServer()))) {
				return false; 
			}
			if (isFriend(id)) { 
				return false; 
			}

			if (!GameServer.getInstance().isLocalServer(delApplication.getApplyPlayerServer())) {
//				FriendAddPush_30000023 build = FriendAddPush_30000023.newBuilder().setPlayerId(id).setFriendId(playerId)
//						.setFriendServer(ServerContext.getInstance().getServerId()).build();
//				GameClientManager.getInstance().sendToGameServer(delApplication.getApplyPlayerServer(), build);
				boolean addFriend = GameServer.getInstance().getCrossGameServerInterface().addFriend(id, playerId,
						ServerContext.getInstance().getServerId());
				if (!addFriend) {
					return false;
				}

			} else {

				FriendHelper.removeMyApplication(id, playerId);
				FriendHelper.removeApplication(id, playerId);

				FriendHelper.addFriend(id, playerId, ServerContext.getInstance().getServerId(), Friend.FRIEND);
			}
			
			FriendHelper.removeMyApplication(playerId, id);
			return addFriend(id, delApplication.getApplyPlayerServer(), Friend.FRIEND);
		}

		return false;
	}

	public boolean isFriend(long playerId) {

		Friend friend = this.friends.get(playerId);
		return isFriend(friend);
	}
	public boolean isFriend(Friend friend) {

		return friend != null && (friend.getRelation() == Friend.FRIEND || friend.getRelation() == Friend.ATTENTION);
	}
	public boolean isFriend(Friend friend, boolean localServer) {

		return friend != null && (friend.getRelation() == Friend.FRIEND || friend.getRelation() == Friend.ATTENTION) && localServer
				? GameServer.getInstance().isLocalServer(friend.getServerId())
				: !GameServer.getInstance().isLocalServer(friend.getServerId());
	}

	public boolean isApplicationLimit() {
		return this.applications.size() >= 50;
	}

	public boolean isFriendMax(boolean localServer) {
		int friendSize = 0;

		for (Friend f : this.friends.values()) {
			if (isFriend(f)) {
				friendSize++;
			}
		}
		return friendSize >= maxFriends;
	}

	public boolean hasRelation(long playerId) {
		Friend friend = this.friends.get(playerId);
		return friend != null;
	}

	public boolean isBlack(long playerId) {

		Friend friend = this.friends.get(playerId);
		return friend != null && friend.getRelation() == Friend.BLACK;
	}

	public void delete(long friendId) {
		Friend friend = this.friends.remove(friendId);
		if (friend != null) {
			FriendHelper.delete(friend);
		}
	}

	public Friend getFriend(long id) {
		return this.friends.get(id);
	}

	public void refreshDay() {

		for (Friend friend : this.friends.values()) {
			if (friend.getRelation() == Friend.FRIEND) {
				if (friend.getGifted() && friend.getReceive()) {
					friend.setGifted(false);
					friend.setReceive(false);
					FriendHelper.update(friend);
				}
			}
		}

	}
	public long getLastRefreshTime() {
		return lastRefreshTime;
	}
	public void setLastRefreshTime(long lastRefreshTime) {
		this.lastRefreshTime = lastRefreshTime;
	}
	public List<SimplePlayer> getLastRefreshPlayers() {
		return lastRefreshPlayers;
	}
	public void setLastRefreshPlayers(List<SimplePlayer> lastRefreshPlayers) {
		this.lastRefreshPlayers = lastRefreshPlayers;
	}

	public boolean addMyApplication(long playerId) {
		this.myApplications.add(playerId);
		return false;
	}

	public Set<Long> excludeIds() {
		Set<Long> ret = new HashSet<Long>();
		ret.addAll(this.friends.keySet());
		ret.addAll(myApplications);
		for (SimplePlayer player : lastRefreshPlayers) {
			ret.add(player.getId());
		}
		ret.add(playerId);
		return ret;
	}

	public boolean removeMyApplications(long playerId) {
		return this.myApplications.remove(playerId);
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleEvent(GameEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initFromDbAfter() {

	};
	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean alwaysStoreDataInStandaloneTable() {
		return true;
	}

	@Override
	public boolean isComplete() {
		return false;
	}
}
