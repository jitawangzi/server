package cn.game.games.net.game.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.cache.id.IdCache;
import cn.game.core.net.client.LogoutType;
import cn.game.games.cache.entity.ForbidAccount;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.data.mapper.ForbidAccountMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.friend.FriendModule;
import cn.game.games.net.game.module.player.OfflineScheduleTask;
import cn.game.games.util.DAO;
import io.vertx.core.Future;

//@Component
public class PlayerManager {
	private static final Logger log = LoggerFactory.getLogger(PlayerManager.class);
	private static final Logger loginlog = LoggerFactory.getLogger("loginLog");

	// playerId => Player,在线玩家
	private ConcurrentHashMap<Long, Player> id_players = new ConcurrentHashMap<>();

//	/** 缓存玩家在哪个服务器 */
//	private Cache<Long, String> playerServers = CacheBuilder.newBuilder().maximumSize(8192)
//			.expireAfterWrite(10, TimeUnit.MINUTES)
//			.build();
	// playerId => ForbidAccount 封禁的账号
	private ConcurrentHashMap<Long, ForbidAccount> forbidAccounts = new ConcurrentHashMap<>();

	//正在进行中的离线任务
	private ConcurrentHashMap<Long,List<OfflineScheduleTask>> runOfflineTaskMap =  new ConcurrentHashMap<>();
	
//	private static PlayerManager instance;
	private static PlayerManager instance = new PlayerManager();

	public static PlayerManager getInstance() {
		return instance ; 
	}

//	@PostConstruct
//	private void init() {
//		instance = this; // 静态代理初始化
//	}

	/** 初始化一些数据 */
	public void init2() {
		initForbidAccount();
	}

	public void online(long playerId, String serverId) {
		IdCache.getManager(DistributedObjectType.PLAYER).setServerId(playerId, serverId);
	}
	public void offline(long playerId) {
		IdCache.getManager(DistributedObjectType.PLAYER).setServerId(playerId, "");
	}

	public void resetOnline(long playerId) {
		IdCache.getManager(DistributedObjectType.PLAYER).invalidateServerId(playerId);
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
	 * 某玩家是否在其他服务器在线。 
	 * @param playerId
	 * @return
	 */
	public boolean isOnlineOtherServer(long playerId) {
		if (isOnlineInCurrentServer(playerId)) {
			return false;
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
		return IdCache.getPlayerServerId(playerId);
	}

  public Future<SimplePlayer> getSimplePlayerFromRedisAsync(long playerId) {
		return RedisLocalCache.getInstance().getAsync(CacheType.PLAYER_SIMPLE.key(playerId));
  }

	public Future<List<SimplePlayer>> batchGetSimplePlayerListFromRedisAsync(
			List<Long> playerIdList) {
		return  RedisLocalCache.getInstance().multiGetAsync(CacheType.PLAYER_SIMPLE, playerIdList.toArray(new Long[0]));
	}
  public CompletionStage<Map<Long, SimplePlayer>> batchGetSimplePlayerFromRedisAsync(
      List<Long> playerIdList) {
    List<String> pidKeys = new ArrayList<>();
    playerIdList.forEach(
        pid -> {
          pidKeys.add(CacheType.PLAYER_SIMPLE.key(pid));
        });
	CompletableFuture<Map<Long, SimplePlayer>> future  = new CompletableFuture<>();
    RedisLocalCache.getInstance()
        .multiGetAsync(pidKeys.toArray(new String[0]))
        .onSuccess(
            list -> {
              Map<Long, SimplePlayer> result = new HashMap<>();
              list.forEach(
                  data -> {
                    SimplePlayer simplePlayer = (SimplePlayer) data;
                    result.put(simplePlayer.id, simplePlayer);
                  });
			  future.complete(result);
            })
        .onFailure(
            err -> {
              err.printStackTrace();
			  future.completeExceptionally(err);
            });
    return future;
  }

	public void initAdd(Player p) {
		id_players.put(p.getPlayerId(), p);
		//取消玩家离线推送微信任务
		delOfflineScheduleTask(p.getPlayerId());
	}

	/**
	 * 获取指定角色id的角色数据
	 * @param playerId
	 * @return
	 */
	public Player getPlayer(long playerId) {
		return id_players.get(playerId);
	}

	/**
	 * 获取指定角色id的角色数据
	 * 如果角色不在线，则从数据库中加载
	 * @param playerId
	 * @return
	 */
	public Future<Player> getPlayerAsync(long playerId) {
		Player player = getPlayer(playerId);
		if (player != null) {
			return Future.succeededFuture(player);
		}
		// TODO 从数据库加载,后续清理
		return PlayerHelper.loadPlayerFromDb(playerId);
	}
	/**
	 * 判断某玩家在服务器是否有缓存数据
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

	public Player deletePlayer(long id) {
		return id_players.remove(id);
	}
	
	public ConcurrentHashMap<Long, Player> getAllPlayer() {

		return this.id_players;
	}

	public Future<List<SimplePlayer>> searchPlayersAsync(Player player) {
		FriendModule friendModule = player.getModule(FriendModule.class);
		Set<Long> excludeIds = friendModule.excludeIds();
		CompletionStage<Map<String, Long>> stage = PlayerNameManager
				.getInstance()
				.getRandomUsernameFromAll(20)
				.thenCompose(names -> PlayerNameManager.getInstance().getPlayerIds(names));
		Future<Map<String, Long>> future = Future.fromCompletionStage(stage);
		Future<List<SimplePlayer>> playersFuture = future
				.map(r -> r.values().stream().map(String::valueOf).toArray(String[]::new))
				.compose(r -> RedisLocalCache.getInstance().multiGetAsync(CacheType.PLAYER_SIMPLE, r));
		return playersFuture.map(r -> {
			List<SimplePlayer> ret = new ArrayList<>();

			for (SimplePlayer simplePlayer : r) {
				if (simplePlayer == null) {
					continue;
				}
				if (excludeIds.contains(simplePlayer.getId())) {
					continue;
				}
				if (simplePlayer.getId() == player.getPlayerId()) {
					continue;
				}
				ret.add(simplePlayer);
			}
			Collections.sort(ret, (a, b) -> {
				return (int) (b.offlineTime - a.offlineTime);
			});
			List<SimplePlayer> subList = ret.subList(0, 8);
			// 设置本次刷新的记录
			List<Long> idList = subList.stream().map(p -> p.getId()).collect(Collectors.toList());
			friendModule.setLastRefreshPlayers(idList);
			return subList;
		});
	}
	
	/**
	 * 账号是否被封禁
	 * @param playerId
	 * @return
	 */
	public boolean isForbidAccount(long playerId) {
		ForbidAccount forbidAccount = forbidAccounts.get(playerId);
		return forbidAccount != null && forbidAccount.getUnblockTime().getTime() > System.currentTimeMillis() &&  ForbidAccount.isForbidLogin(forbidAccount.getType());
	}

	public boolean isForbidChat(long pid){
		ForbidAccount forbidAccount = forbidAccounts.get(pid);
		return forbidAccount != null && forbidAccount.getUnblockTime().getTime() > System.currentTimeMillis() &&  ForbidAccount.isForbidChat(forbidAccount.getType());
	}

	
	/***
	 * 初始化被封禁的账号
	 */
	private void initForbidAccount() {
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
	public ForbidAccount forbidAccount(long playerId, String reason, String unblockTime,int type) {
		Date now = new Date();
		Date unblock = new Date(Long.parseLong(unblockTime));
		// 解封时间不合法
		if (unblock.before(now)) {
			return null;
		}
		// 本来已经封号
		if (isForbidAccount(playerId)) {
			ForbidAccount account = forbidAccounts.get(playerId);
			account.setReason(reason);
			account.updateType(type);
			account.setUnblockTime(unblock);
			account.update();
			return account;
		}
		// 玩家在线
		if (hasCache(playerId)) {
			Player player = id_players.get(playerId);
			PlayerHelper.addTask(playerId, () -> {
				GameClientManager.getInstance().logout(playerId, LogoutType.ForbidAccount);
			});
			ForbidAccount insert = ForbidAccount.valueOf(player, reason, unblock);
			insert.updateType(type);
			DAO.insert(insert);
			this.forbidAccounts.put(playerId, insert);
			return insert;
		}
		
		// 玩家不在线
		try {
			SimplePlayer p = RedisLocalCache.getInstance().get(CacheType.PLAYER_SIMPLE.key(playerId));
			if (p == null) {
				return null;
			}
			ForbidAccount insert = ForbidAccount.valueOf(p, reason, unblock);
			insert.updateType(type);
			DAO.insert(insert);
			this.forbidAccounts.put(playerId, insert);

			return insert;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 解封账号
	 * @param playerId
	 * @return
	 */
	public int unblockAccount(long playerId) {
		ForbidAccount remove = this.forbidAccounts.remove(playerId);
		if (remove != null) {
			DAO.delete(remove);
		}
		return 0;
	}
	
	/**
	 * 检查账号解封
	 * 
	 * @param playerId
	 * @return 没有被封/到期解封成功 true ;被封且没有到解封时间  false 
	 */
	public boolean checkUnlock(long playerId) {
		return !isForbidAccount(playerId);

	}
	/** 
	 * 延长 player id锁
	 */
	@Deprecated
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

	public void addOfflineScheduleTask(long pid,ScheduledFuture<?> task){
		final List<OfflineScheduleTask> list;
		if (runOfflineTaskMap.containsKey(pid)){
			list = runOfflineTaskMap.get(pid);
		} else {
			list = new ArrayList<>();
			runOfflineTaskMap.put(pid,list);
		}
		list.add(new OfflineScheduleTask(task));
	}

	public void delOfflineScheduleTask(long pid){
		List<OfflineScheduleTask> taskList = runOfflineTaskMap.remove(pid);
		if (taskList != null){
			taskList.forEach(task ->{task.cancelTask();});
		}
	}

}
