package cn.game.games.net.game.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.task.TaskManager;
import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.ForbidAccount;
import cn.game.games.cache.entity.Group;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.SimplePlayer;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.data.mapper.ForbidAccountMapper;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.game.GameServer;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.db.DbTask;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.friend.FriendModule;
import cn.game.games.util.DAO;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.protocol.protobuf.ChatMsg.ChatGroupInfo;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;
import cn.game.util.Pair;
import cn.game.util.RedissonUtil;
import cn.game.util.Rnd;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class PlayerManager {
	private static final Logger log = LoggerFactory.getLogger(PlayerManager.class);
	private static final Logger loginlog = LoggerFactory.getLogger("loginLog");

	// playerId => Player,在线玩家
	private ConcurrentHashMap<Long, Player> id_players = new ConcurrentHashMap<>();
	// playerName => Player,在线玩家
	private ConcurrentHashMap<String, Player> name_players = new ConcurrentHashMap<>();

	private ConcurrentHashMap<Long, Long> id_offLineTime = new ConcurrentHashMap<>();

	private ConcurrentHashMap<Long, String> id_names = new ConcurrentHashMap<>();
	/** 本服玩家简略信息，在线和离线都有 */
	private Cache<Long, SimplePlayer> simplePlayers = CacheBuilder.newBuilder().maximumSize(8192).expireAfterWrite(60,
			TimeUnit.MINUTES).build();
	/** 其他服务器玩家简略信息， */
	private Cache<Long, SimplePlayer> simplePlayersOtherServer = CacheBuilder.newBuilder().maximumSize(8192).expireAfterWrite(60,
			TimeUnit.MINUTES).build();
	/** 缓存玩家在哪个服务器 */
	private Cache<Long, String> playerServers = CacheBuilder.newBuilder().maximumSize(8192)
			.expireAfterWrite(5, TimeUnit.MINUTES).build();
	
	// playerId => ForbidAccount 封禁的账号
	private ConcurrentHashMap<Long, ForbidAccount> forbidAccounts = new ConcurrentHashMap<>();
	
	private static PlayerManager instance = new PlayerManager() ; 

	public static PlayerManager getInstance() {
		return instance ; 
	}

	/** 推荐好友时，相近的等级 */
	public static final int similarLevel = 10;
	/** 推荐好友时，最多找到的相似玩家数量 */
	public static final int fitCountMax = 100;

	/** 初始化一些数据 */
	public void init() {
//		initForbidAccount();

	}

	public void online(long playerId, String serverId) {
		playerServers.put(playerId, serverId);
	}
	public void offline(long playerId) {
		playerServers.put(playerId, "");
	}

	public void resetOnline(long playerId) {
		playerServers.invalidate(playerId);
	}

	/** 
	 * 某玩家是否在线(全服范围内)
	 * @param playerId
	 * @return
	 */
	public boolean isOnline(long playerId) {
		if (isOnlineInCurrentServer(playerId)) {
			return true;
		}
		String serverId = getServerId(playerId);
		return !StringUtils.isEmpty(serverId);
	}

	/** 
	 * 当前服务器是否在线
	 * @param playerId
	 * @return 
	 */
	public boolean isOnlineInCurrentServer(long playerId) {
		return getPlayer(playerId) != null;
	}

	/** 
	 * 获取玩家当前所在服务器id，如果玩家不在线返回空字符串
	 * @param playerId
	 * @return
	 */
	public String getServerId(long playerId) {
		try {
			return playerServers.get(playerId, () -> {
				String serverId = RedissonUtil.get(CacheType.PLAYER_SERVER_ID.key(playerId));
				return serverId == null ? "" : serverId;
			});
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @Description 严禁在主线程内直接调用！！！！！！！！！！！！！！！
	 * 
	 *              没有数据时会从数据库查找数据，是阻塞操作，如果没有查找到数据会报异常，调用方要处理异常，并且在线程池中执行。
	 * @param playerId
	 * @return
	 * @throws Exception
	 */
	public SimplePlayer getAndLoadSimplePlayer(long playerId) throws Exception {
		return simplePlayers.get(playerId, () -> {
			if (PlayerManager.getInstance().hasCache(playerId)) {
				Player player = PlayerManager.getInstance().getPlayer(playerId);
				SimplePlayer simplePlayer = new SimplePlayer(player);
				simplePlayer.initDataEx();
				return simplePlayer;
			}

			SimplePlayer simplePlayer = (SimplePlayer) DAO.executeSync(PlayerDataMapper.class, "selectSimplePlayer",
					playerId);
			// 基本数据都是从player表查询的，可能执行另外一些赋值操作
			if (simplePlayer != null) {
				simplePlayer.initDataEx();
			}
			return simplePlayer;
		});
	}
	/**
	 * @Description 严禁在主线程内直接调用！！！！！！！！！！！！！！！
	 * 
	 *              没有数据时会从数据库查找数据，是阻塞操作，要在其他线程里执行
	 * @param playerIds
	 * @return
	 * @throws Exception
	 */
	public List<SimplePlayer> getAndLoadSimplePlayers(List<Long> playerIds) {
		List<Long> absentIds = new ArrayList<Long>();
		List<SimplePlayer> presentPlayers = new ArrayList<SimplePlayer>();
		for (Long id : playerIds) {

			SimplePlayer simplePlayer = getSimplePlayer(id);
			if (simplePlayer == null) {
				absentIds.add(id);
			} else {
				presentPlayers.add(simplePlayer);
			}
		}
		if (absentIds.isEmpty()) {
			return presentPlayers;
		}

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("ids", absentIds);
		List<SimplePlayer> playersFromDb = (List<SimplePlayer>) DAO.executeSync(PlayerDataMapper.class,
				"selectSimplePlayers", params);
		for (SimplePlayer simplePlayer : playersFromDb) {
			simplePlayer.initDataEx();
		}

		presentPlayers.addAll(playersFromDb);
		return presentPlayers;
	}
	/**
	 * @Description 严禁在主线程内直接调用！！！！！！！！！！！！！！！ 注意确保serverId的正确性
	 * 
	 *              没有数据时会从其他服务器查找数据，是阻塞操作，在线程池中执行。
	 * @param playerId
	 * @param serverId
	 * @return
	 * @throws Exception
	 */
	public SimplePlayer getAndLoadSimplePlayer(long playerId, String serverId) throws Exception {
		if (GameServer.getInstance().isLocalServer(serverId)) {
			return getAndLoadSimplePlayer(playerId) ; 
		}
		return GameServer.getInstance().getCrossGameServerInterfaceSync().getSimplePlayer(playerId, serverId);
	}
	
	/**
	 * 跨服获取群组简略信息，如果为空代表没有这个群组的信息，可能是被解散了
	 * @param groupId
	 * @param serverId
	 * @return
	 */
	public Group getOneOtherServerGroupBriefInfo(long groupId, String serverId) {
		Group chatGroupBriefInfo  = null;
		
		try {
			chatGroupBriefInfo = GameServer.getInstance().getCrossGameServerInterfaceSync().getOneGroupBriefInfo(groupId, serverId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return chatGroupBriefInfo;
	}
	/**
	 * 跨服获取群组全部信息，如果为空代表没有这个群组的信息，可能是被解散了	
	 * @param groupId
	 * @param serverId
	 * @return
	 */
	public ChatGroupInfo getOtherServerGroupInfo(long groupId, String serverId) {
		ChatGroupInfo chatGroupInfo = null;
		
		try {
			chatGroupInfo = PbBuilder.buildChatGroupInfo(GameServer.getInstance().getCrossGameServerInterfaceSync().getChatGroupInfo(groupId, serverId));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return chatGroupInfo;
	}
	
	/**
	 * @Description 严禁在主线程内直接调用！！！！！！！！！！！！！！！
	 * 
	 *              没有数据时会从其他服务器查找数据，是阻塞操作，在线程池中执行。
	 * @param playerId
	 * @return
	 * @throws Exception
	 */
	public List<SimplePlayer> getAndLoadSimplePlayers(List<Long> playerIds, List<String> serverIds) throws Exception {

		// 本地已经缓存的玩家
		List<SimplePlayer> cachePlayers = new ArrayList<SimplePlayer>();

		for (int i = 0; i < playerIds.size(); i++) {
			SimplePlayer simplePlayer = getSimplePlayer(playerIds.get(i), serverIds.get(i));
			if (simplePlayer != null) {
				cachePlayers.add(simplePlayer);
			}
		}
		if (cachePlayers.size() == playerIds.size()) {
			return cachePlayers ; 
		}
		for (SimplePlayer simplePlayer : cachePlayers) {
			playerIds.remove(simplePlayer.getId());
			serverIds.remove(simplePlayer.getServerId());
		}

		// 找出本服的玩家
		List<Long> localPlayerIds = new ArrayList<Long>();
		List<String> localServerIds = new ArrayList<String>();
		for (int i = 0; i < serverIds.size(); i++) {
			if (GameServer.getInstance().isLocalServer(serverIds.get(i))) {
				localPlayerIds.add(playerIds.get(i));
				localServerIds.add(serverIds.get(i));
			}
		}
		List<SimplePlayer> localSimplePlayers = null;
		if (!localPlayerIds.isEmpty()) {
			localSimplePlayers = getAndLoadSimplePlayers(localPlayerIds);

			playerIds.removeAll(localPlayerIds);
			serverIds.removeAll(localServerIds);

			cachePlayers.addAll(localSimplePlayers);
		}
		if (playerIds.isEmpty()) {
			return cachePlayers;
		}
		// 跨服玩家
		List<SimplePlayer> otherServerPlayers = GameServer.getInstance().getCrossGameServerInterfaceSync().getSimplePlayers(playerIds,
				serverIds);
		if (otherServerPlayers != null) {
			if (!cachePlayers.isEmpty()) {
				cachePlayers.addAll(otherServerPlayers);
			} else {
				cachePlayers = otherServerPlayers;
			}
			// 跨服玩家加入本地缓存
			addSimplePlayers(otherServerPlayers);
		}
		return cachePlayers;
	}

//	public Future<List<SimplePlayer>> getAndLoadSimplePlayersAsync(List<Long> playerIds, List<String> serverIds) throws Exception {
//
//		// 本地已经缓存的玩家
//		List<SimplePlayer> cachePlayers = new ArrayList<SimplePlayer>();
//		Promise<List<SimplePlayer>> promise = Promise.promise();
//
//		for (int i = 0; i < playerIds.size(); i++) {
//			SimplePlayer simplePlayer = getSimplePlayer(playerIds.get(i), serverIds.get(i));
//			if (simplePlayer != null) {
//				cachePlayers.add(simplePlayer);
//			}
//		}
//		if (cachePlayers.size() == playerIds.size()) { 
//			promise.complete(cachePlayers);
//			return promise.future();
//		}
//		for (SimplePlayer simplePlayer : cachePlayers) {
//			playerIds.remove(simpleplayer.getId());
//			serverIds.remove(simpleplayer.getServerId());
//		}
//
//		// 找出本服的玩家
//		List<Long> localPlayerIds = new ArrayList<Long>();
//		List<String> localServerIds = new ArrayList<String>();
//		for (int i = 0; i < serverIds.size(); i++) {
//			if (GameServer.getInstance().isLocalServer(serverIds.get(i))) {
//				localPlayerIds.add(playerIds.get(i));
//				localServerIds.add(serverIds.get(i));
//			}
//		}
//		List<SimplePlayer> localSimplePlayers = null;
//		if (!localPlayerIds.isEmpty()) {
//			localSimplePlayers = getSimplePlayerAsync(localPlayerIds);
//
//			playerIds.removeAll(localPlayerIds);
//			serverIds.removeAll(localServerIds);
//
//			cachePlayers.addAll(localSimplePlayers);
//		}
//		if (playerIds.isEmpty()) {  
//			promise.complete(cachePlayers);
//			return promise.future();
//		}
//		// 跨服玩家
//		List<SimplePlayer> otherServerPlayers = GameServer.getInstance().getCrossGameServerInterfaceSync().getSimplePlayers(playerIds,
//				serverIds);
//		if (otherServerPlayers != null) {
//			if (!cachePlayers.isEmpty()) {
//				cachePlayers.addAll(otherServerPlayers);
//			} else {
//				cachePlayers = otherServerPlayers;
//			}
//			// 跨服玩家加入本地缓存
//			addSimplePlayers(otherServerPlayers);
//		}
//		return cachePlayers;
//	}
	public List<SimplePlayer> getAndLoadSimplePlayerPairs(List<Pair<Long, String>> players) throws Exception {

		// 找出本服的玩家
		List<Pair<Long, String>> localPlayers = new ArrayList<Pair<Long, String>>();
		for (int i = 0; i < players.size(); i++) {
			if (GameServer.getInstance().isLocalServer(players.get(i).second)) {
				localPlayers.add(players.get(i));
			}
		}
		List<SimplePlayer> localSimplePlayers = null;
		if (!localPlayers.isEmpty()) {
			List<Long> localIds = new ArrayList<Long>();
			for (Pair<Long, String> pair : localPlayers) {
				localIds.add(pair.first);
			}
			localSimplePlayers = getAndLoadSimplePlayers(localIds);
			players.removeAll(localPlayers);
		}
		List<SimplePlayer> otherServerPlayers = GameServer.getInstance().getCrossGameServerInterfaceSync().getSimplePlayers(players);
		if (localSimplePlayers != null) {
			otherServerPlayers.addAll(localSimplePlayers);
		}
		return otherServerPlayers;

	}

	/**
	 * @Description 获取本服玩家简单信息
	 * @param playerId
	 * @return null，玩家不在缓存
	 */
	public SimplePlayer getSimplePlayer(long playerId) {
		return simplePlayers.getIfPresent(playerId);
	}
	/**
	 * 异步获取本服玩家简单信息
	 * 
	 * @param playerId
	 * @return
	 */
	public Future<SimplePlayer> getSimplePlayerAsync(long playerId) {
		Promise<SimplePlayer> promise = Promise.promise();
		VxHolder.vertx.executeBlocking(r -> {
			try {
				SimplePlayer simplePlayer = getAndLoadSimplePlayer(playerId);
				TaskManager.getInstance().addMainTask(() -> {
					promise.complete(simplePlayer);
				});
			} catch (Exception e) {
				TaskManager.getInstance().addMainTask(() -> {
					promise.complete(null);
				});
			}
		});

		return promise.future();
	}
	/**
	 * 
	 * 异步获取一个玩家简单信息，包含本服或者其他服
	 * 
	 * @param playerId
	 *            玩家id
	 * @param serverId
	 *            玩家所在服务器id
	 * @return
	 */
	public Future<SimplePlayer> getSimplePlayerAsync(long playerId, String serverId) {
		if (!GameServer.getInstance().isLocalServer(serverId)) {
			return GameServer.getInstance().getGameServerRemoteAsync(serverId).getSimplePlayerAsync(playerId);
		}
		return getSimplePlayerAsync(playerId);
	}

	/**
	 * @Description 获取其他服玩家简单信息，需要保证参数是其他服的玩家id
	 * @param playerId
	 * @return null，玩家不在缓存
	 */
	public SimplePlayer getSimplePlayerOther(long playerId) {
		return simplePlayersOtherServer.getIfPresent(playerId);
	}

	public Future<SimplePlayer> getSimplePlayerOtherAsync(long playerId,String serverId) {
		return GameServer.getInstance().getGameServerRemoteAsync(serverId).getSimplePlayerAsync(playerId) ; 
	}

	/**
	 * @Description 尝试获取玩家简单信息，本服玩家，或者其他服玩家
	 * @param playerId
	 * @param serverId
	 *            玩家所在服务器id
	 * @return null，玩家不在缓存
	 */
	public SimplePlayer getSimplePlayer(long playerId, String serverId) {
		if (GameServer.getInstance().isLocalServer(serverId)) {
			return getSimplePlayer(playerId); 
		}
		return simplePlayersOtherServer.getIfPresent(playerId);
	}
	/**
	 * @Description 主动添加玩家简单数据到缓存，一般是添加其他服务器的玩家
	 * @param simplePlayer
	 */
	public void addSimplePlayer(SimplePlayer simplePlayer) {
		if (GameServer.getInstance().isLocalServer(simplePlayer.getServerId())) {
			this.simplePlayers.put(simplePlayer.getId(), simplePlayer);
		} else {
			this.simplePlayersOtherServer.put(simplePlayer.getId(), simplePlayer);
		}
	}
	/**
	 * @Description 主动添加玩家简单数据到缓存，一般是添加其他服务器的玩家
	 * @param simplePlayers
	 */
	public void addSimplePlayers(List<SimplePlayer> simplePlayers) {
		for (SimplePlayer simplePlayer : simplePlayers) {
			addSimplePlayer(simplePlayer);
		}
	}

	public void initAdd(Player p) {
		id_players.put(p.getPlayerId(), p);
//		if (!StringUtils.isEmpty(p.getName())) {
//			name_players.put(p.getName(), p);
//		}
	}
	/**
	 * 更新角色名，同时更新角色名索引
	 */
	
	public void updatePlayername(Player player, String playerName) {
		if (playerName == null || playerName.length() == 0)
			return;

//		name_players.remove(player.getData().getName());
		name_players.put(playerName, player);

//		player.getData().setName(playerName);
//		PlayerHelper.update(player.getdta);// commit
	}

	private Player getPlayer(long playerId, boolean init) {

		Player player = id_players.get(playerId);
		if (player == null && init) {
			log.info("player[{}] is null , init  from  db: ", playerId);
//			DataGameServerInterface dataGameServerInterfaceSync = GameServer.getInstance()
//					.getDataGameServerInterfaceSync();
//			PlayerData playerData = (PlayerData) dataGameServerInterfaceSync.exec(PlayerDataMapper.class,
//					MapperConstant.selectByPrimaryKey, playerId);
//			if (playerData != null && init) {
//				initAdd(player);
//			} else {
//				log.warn("player[{}] is not exsit, init  fail ", playerId);
//			}
		}
		return player;
	}

	/**
	 * @Description 获取指定角色id的角色数据
	 * @param playerId
	 * @return
	 */
	public Player getPlayer(long playerId) {
		return getPlayer(playerId, false);
	}
	
	/**
	 * 查询一组玩家的简单信息 要另起线程调用
	 * @param playerIds
	 * @return
	 */
	public List<SimplePlayerInfo> getSimplePlayerInfos(List<String> playerIds){		 
		List<SimplePlayerInfo> sPlayerInfos = new ArrayList<>();
		
		try {
			for (int i = 0; i < playerIds.size(); i++) {
				SimplePlayerInfo sPlayerInfo = PbBuilder.buildSimplePlayerInfo(Long.parseLong(playerIds.get(i)));					
				sPlayerInfos.add(sPlayerInfo);
			}
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
		
		return sPlayerInfos;
	}

	/**
	 * 查询一组玩家的简单信息 要另起线程调用 包含跨服的玩家
	 * @param playerIds
	 * @param serverIds
	 * @return
	 */
	public List<SimplePlayerInfo> getSimplePlayerOtherInfos(List<String> playerIds, List<String> serverIds){		
		List<SimplePlayerInfo> sPlayerInfos = new ArrayList<>();
		try {						
			for (int i = 0; i < playerIds.size(); i++) {																	
				sPlayerInfos.add(getSimpleOtherPlayerInfo(playerIds.get(i), serverIds.get(i)));																	
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
				
		return sPlayerInfos;
	}
	
	/**
	 * 查询一组玩家的简单信息
	 * 
	 * @param playerIds
	 * @return
	 */
	public Future<List<SimplePlayer>> getSimplePlayersAsync(List<Long> playerIds, List<String> serverIds) {
		Promise<List<SimplePlayer>> ret = Promise.promise();
		List<SimplePlayer> list = new ArrayList<>() ; 
		List<Future> futureList = new ArrayList<>();
		for (int i = 0; i < playerIds.size(); i++) {
			Future<SimplePlayer> simplePlayerFuture = getSimplePlayerAsync(playerIds.get(i), serverIds.get(i));
			futureList.add(simplePlayerFuture);

		}
		CompositeFuture join = CompositeFuture.join(futureList); 
		join.onComplete(r -> {
			for (int i = 0; i < futureList.size(); i++) {
				SimplePlayer result = join.resultAt(i);
				if (getSimplePlayer(playerIds.get(i)) == null) {
					addSimplePlayer(result);
				}
				list.add(result);
			}
			ret.complete(list);
		});
		return ret.future() ; 
	}
	/**
	 * 异步获取本服玩家简单数据
	 * 
	 * @param playerIds
	 * @return
	 */
	public Future<List<SimplePlayer>> getSimplePlayersAsync(List<Long> playerIds) {
		Promise<List<SimplePlayer>> ret = Promise.promise();
		List<SimplePlayer> list = new ArrayList<>();
		List<Future> futureList = new ArrayList<>();
		for (int i = 0; i < playerIds.size(); i++) {
			Future<SimplePlayer> simplePlayerFuture = getSimplePlayerAsync(playerIds.get(i));
			futureList.add(simplePlayerFuture);
		}
		CompositeFuture join = CompositeFuture.join(futureList);
		join.onComplete(r -> {
			for (int i = 0; i < futureList.size(); i++) {
				SimplePlayer result = join.resultAt(i);
				if (getSimplePlayer(playerIds.get(i)) == null) {
					addSimplePlayer(result);
				}
				list.add(result);
			}
			ret.complete(list);
		});
		return ret.future();
	}

	/**
	 * 获取一个不确定区服的玩家简略信息
	 * 
	 * @param playerId
	 * @param serverId
	 * @return
	 */
	public SimplePlayerInfo getSimpleOtherPlayerInfo(String playerId, String serverId) {
		SimplePlayerInfo sPlayerInfo = null;
		try {			
			if(serverId.equals(ServerContext.getInstance().getServerId())) {
				sPlayerInfo = PbBuilder.buildSimplePlayerInfo(Long.parseLong(playerId));
			}else {
				// 跨服 查玩家数据， 暂时先不查，给new个，有接口时再查
				SimplePlayer simplePlayer = getAndLoadSimplePlayer(Long.parseLong(playerId), serverId);				
				sPlayerInfo = PbBuilder.buildSimplePlayerInfo(simplePlayer);								 
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return sPlayerInfo;
		}
				
		return sPlayerInfo;
	}
	
	/**
	 * @Description 判断某玩家在服务器是否有缓存数据
	 * @param playerId
	 * @return
	 */
	public boolean hasCache(long playerId) {

		return getPlayer(playerId) != null;
	}

	/**
	 * 获取在线角色人数
	 */
	public int getOnlineCount() {
		return id_players.size();
	}

	public Player getOfName(String playerName) {
		return name_players.get(playerName);
	}

	/**
	 * 获取所有角色ID
	 */
	// 
	// public Set<Long> getIds(){
	// return id_players.keySet();
	// }

	
	public void deletePlayer(long id) {

		Player del = id_players.get(id);
		if (del != null) {
			name_players.remove(del.getData().getName());
			id_players.remove(id);
//			PlayerCacheFactory.removeCache(id);
			
			Date loginDate = DateUtil.getDate(del.getData().getLoginDate());
			long loginTime = loginDate.getTime();
			long nowTime = System.currentTimeMillis();
			int onlineTime = (int) ((nowTime - loginTime) / 1000);
			loginlog.info("opType[gameLogin]playerId[{}]isCreate[{}]isLogin[{}]onlineTime[{}]", id, false,false, onlineTime);
		}
	}

	
	public ConcurrentHashMap<Long, Player> getAllPlayer() {

		return this.id_players;
	}

	
	public String getName(long id) {

		String name = this.id_names.get(id);
		if (!StringUtils.isEmpty(name)) {

			return name;
		}

		Player player = this.id_players.get(id);
		if (player == null) {
			player = getPlayer(id, false);
		}
		if (player != null) {
			this.id_names.put(id, player.getData().getName());
			return player.getData().getName();
		}

		return "";
	}

	public List<SimplePlayer> searchPlayersTest(HashMap<String, Object> hashMap) {

		return (List<SimplePlayer>) DAO.executeSync(PlayerDataMapper.class, "searchPlayers", hashMap);
	}

	public List<SimplePlayer> searchPlayersFromDb(int count, Set<Long> excludeIds) {

		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("count", count);
		hashMap.put("ids", excludeIds);

		return (List<SimplePlayer>) DAO.executeSync(PlayerDataMapper.class, "selectSimplePlayersLimit", hashMap);

	}

	/**
	 * @Description 根据某一玩家，来推荐等级相近的玩家,不能再主线程调用。
	 * @param playerId
	 * @return
	 */
	public List<SimplePlayer> searchPlayers(long playerId) {

		List<SimplePlayer> ret = new ArrayList<>();
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		FriendModule friendOp = player.getModule(FriendModule.class);
		Set<Long> excludeIds = friendOp.excludeIds();
		int level = player.getData().getLevel();
		ConcurrentMap<Long, SimplePlayer> map = simplePlayers.asMap();

		loop: for (int i = 0; i < 2; i++) {

			for (SimplePlayer simplePlayer : map.values()) {

				if (excludeIds.contains(simplePlayer.getId())) {
					continue;
				}
				// 等级
				if (i == 0) {
					if (level >= simplePlayer.getLevel() - similarLevel
							&& level <= simplePlayer.getLevel() + similarLevel) {
						ret.add(simplePlayer);
					}
				} else {
					if (level >= simplePlayer.getLevel() - similarLevel
							&& level <= simplePlayer.getLevel() + similarLevel) {
						continue;
					}
					ret.add(simplePlayer);
				}

				if (ret.size() > fitCountMax) {
					break loop;
				}

			}
		}
		if (ret.size() > 10) {
			ret = randomPlayer(ret, 10, null);
		} else {
			int need = 10 - ret.size();
			// 从数据库补齐
			List<SimplePlayer> searchPlayersFromDb = searchPlayersFromDb(20, excludeIds);
			ret.addAll(randomPlayer(searchPlayersFromDb, need, ret));
		}

		// 设置本次刷新的记录
		friendOp.setLastRefreshPlayers(ret);
		friendOp.setLastRefreshTime(System.currentTimeMillis());

		return ret;

	}

	private List<SimplePlayer> randomPlayer(List<SimplePlayer> list, int count, List<SimplePlayer> exclude) {

		if (list.size() > count) {
			List<SimplePlayer> ret = new ArrayList<>();
			while (true) {
				int index = Rnd.nextInt(list.size());
				SimplePlayer p = list.get(index);
				if (p != null) {
					if (exclude != null && exclude.contains(p)) {
						list.set(index, null);
						continue;
					}
					ret.add(p);
					list.set(index, null);
					if (ret.size() == count) {
						break;
					}
				}
			}
			return ret;
		} else {

			return list;
		}

	}

	
	public long getOfflineTime(long id) {

		if (GameClientManager.getInstance().isOnline(id)) {
			return 0;
		}
		Long time = this.id_offLineTime.get(id);
		if (time != null) {
			return time;
		}
		return 0;
	}

	
	public void rename(String oldName, String newName, Player player) {

		Player remove = this.name_players.remove(oldName);
		if (remove != null) {
			this.name_players.put(newName, remove);
		} else {
			this.name_players.put(newName, player);
		}
	}
	
	/**
	 * 账号是否被封禁
	 * @param playerId
	 * @return
	 */
	public boolean isForbidAccount(long playerId) {
		return this.forbidAccounts.containsKey(playerId);
	}
	
	/***
	 * 初始化被封禁的账号
	 */
	private void initForbidAccount() {
//		DataGameServerInterface dataGameServerInterfaceSync = GameServer.getInstance().getDataGameServerInterfaceSync();
		// 查询所有封禁账号
		List<ForbidAccount> forbidAccounts = (List<ForbidAccount>) DAO.executeSync(ForbidAccountMapper.class,
				MapperConstant.selectAll);
		
		if (forbidAccounts == null) {
			return;
		}
		for (ForbidAccount account : forbidAccounts) {
			this.forbidAccounts.put(account.getPlayerId(), account);
		}
	}

	/** 查询封号 */
	public List<ForbidAccount> getForbidAccount() {
		List<ForbidAccount> res = new ArrayList<>();
		Date now = new Date();
		Date unblockTime = null;
		Iterator<Map.Entry<Long, ForbidAccount>> it = this.forbidAccounts.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<Long, ForbidAccount> entry = it.next();
			ForbidAccount account = entry.getValue();
			unblockTime = account.getUnblockTime();
			// 未到解封时间
			if (now.before(unblockTime)) {
				res.add(account);
				continue;
			}
			it.remove();
			DAO.delete(account);
		}
		
		return res;
	}

	/**
	 * 封号
	 * @param playerId
	 * @param reason
	 * @param unblockTime
	 * @return 
	 */
	public int forbidAccount(long playerId, String reason, String unblockTime) {
		Date now = new Date();
		Date unblock = new Date(Long.parseLong(unblockTime));
		// 解封时间不合法
		if (unblock.before(now)) {
			return ErrorMsgEnum.unknown.getId();
		}
		// 本来已经封号
		if (isForbidAccount(playerId)) {
			ForbidAccount account = forbidAccounts.get(playerId);
			account.setReason(reason);
			account.setUnblockTime(unblock);
			account.update();
			return 0;
		}
		// 玩家在线
		if (hasCache(playerId)) {
			Player player = id_players.get(playerId);
			PlayerHelper.addTask(playerId, r -> {
				GameClientManager.getInstance().logout(playerId);
			});
			ForbidAccount insert = ForbidAccount.valueOf(player, reason, unblock);
			DAO.insert(insert);
			this.forbidAccounts.put(playerId, insert);
			return 0;
		}
		
		// 玩家不在线
		try {
			SimplePlayer p = getAndLoadSimplePlayer(playerId);
			if (p == null) {
				return ErrorMsgEnum.player_not_found.getId();
			}
			ForbidAccount insert = ForbidAccount.valueOf(p, reason, unblock);
			DAO.insert(insert);
			this.forbidAccounts.put(playerId, insert);

			return 0;
			
		} catch (Exception e) {
			e.printStackTrace();
			return ErrorMsgEnum.unknown.getId();
		}
	}

	/**
	 * 解封账号
	 * @param playerId
	 * @return
	 */
	public int unblockAccount(long playerId) {
		//没有被封
		if (!isForbidAccount(playerId)) {
			return 0;
		}
		ForbidAccount remove = this.forbidAccounts.remove(playerId);
		DAO.delete(remove);
		return 0;
	}
	
	/**
	 * 检查账号解封
	 * 
	 * @param playerId
	 * @return 没有被封/到期解封成功 true ;被封且没有到解封时间  false 
	 */
	public boolean checkUnlock(long playerId) {

		ForbidAccount account = this.forbidAccounts.get(playerId);
		if (account == null) {
			return true;
		}
		Date unlockDate = account.getUnblockTime();
		Date now = new Date();
		if (now.before(unlockDate)) {
			return false;
		}
		unblockAccount(playerId);
		return true;

	}
	
	
	/** 
	 * 清理玩家缓存
	 * @param playerId
	 * @return 
	 */
	public Future<?> logoutCache(long playerId) {

		Future<List<Object>> dbFuture = saveClientCache(playerId, true).onComplete(r -> {
			OnLineTaskManager.getInstance().removeScheduledTask(playerId);
			PlayerManager.getInstance().getPlayer(playerId);
			deletePlayer(playerId);
		});
		RFuture<Boolean> deleteAsync = RedissonUtil.deleteAsync(CacheType.PLAYER_SERVER_ID.key(playerId));
		Future<Object> deleteFuture = Future.future(promise -> {
			deleteAsync.onComplete((v, throwable) -> {
				if (v) {
					promise.complete(v);
				} else {
					promise.fail(throwable);
				}
			});
		});
		return CompositeFuture.join(dbFuture, deleteFuture);
	}

	/**
	 * @Description 保存在线玩家缓存数据到数据库
	 * @param playerId
	 * @param logout
	 *            是否是退出时
	 * @return 
	 */
	public Future<List<Object>> saveClientCache(long playerId, boolean logout) {

		Player player = getPlayer(playerId);
		if (player != null) {
			if (player.isActive()) {
				PlayerData data = player.getData();
				if (logout) {
					data.setOfflineTime(System.currentTimeMillis());
					data.setGameTime(data.getGameTime()
							+ (int) ((data.getOfflineTime() - DateUtil.getDate(data.getLoginDate()).getTime()) / 1000));

					player.cancelAllTimer();
					GameLogger.logout(player);
					// TODO 异步保存SimplePlayer 到redis。

				}
				if (GameServer.getInstance().isSinglePlayerTable()) {
					data.beforeSave();
					data.setModules(JsonUtil.toJsonString(player.getModules()));
					List<DbTask> dbTasks = new ArrayList<>(1);
					dbTasks.add(new DbTask(data.getMapperClass(), MapperConstant.updateByPrimaryKeyWithBLOBs, data));
					return DAO.execute(dbTasks);
				}
				List<DbEntity> entities = new ArrayList<>();

				for (BasePlayerModule module : player.getAllModule()) {
					module.autoSaveTasks(entities);
				}
				List<DbTask> dbTasks = new ArrayList<>(entities.size());
				for (DbEntity dbEntity : entities) {
					dbEntity.beforeSave();
					dbTasks.add(new DbTask(dbEntity.getMapperClass(), MapperConstant.updateByPrimaryKeySelective,
							dbEntity));
				}
				if (!dbTasks.isEmpty()) {
					Future<List<Object>> updateFuture = DAO.execute(dbTasks);
					return updateFuture;
				}
			}
		}
		return Future.succeededFuture();
	}

	/**
	 * @Description 保存在线玩家缓存数据到数据库
	 * @param playerId
	 * @return 
	 */
	public Future<List<Object>> saveClientCache(long playerId) {
		return saveClientCache(playerId, false);
	}

	/** 
	 * 延长 player id锁
	 */
	public void setPlayerServerId() {
		Collection<Player> values = this.id_players.values();
		for (Player player : values) {
			RFuture<Void> setServerId = PlayerHelper.setServerId(player.getPlayerId());
			setServerId.onComplete((v, throwable) -> {
				if (throwable != null) {
					log.error(player.getPlayerId() + " setServerId error ", throwable);
				}
			});
		}
	}

}
