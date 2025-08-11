package cn.game.games.net.game.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;

import cn.game.core.base.ServerContext;
import cn.game.core.net.client.LogoutType;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.AsyncUtils;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.protobuf.PlayerMsg.PlayerLogoutPush_01100030;
import cn.game.protocol.protobuf.ServerMsg.GamePlayerOnlinePush_7d000010;
import cn.game.util.Config;
import cn.game.util.ServerType;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class GameClientManager {
	private static Logger log = LoggerFactory.getLogger(GameClientManager.class);
	/* 客户端管理唯一实例 */
	private static GameClientManager instance = new GameClientManager();

	/** sessionId */
	private ConcurrentMap<String, GameClient> clients = new ConcurrentHashMap<>();
	/** playerId */
	private ConcurrentMap<Long, GameClient> players = new ConcurrentHashMap<>();

	private ConcurrentMap<String, GameClient> connections = new ConcurrentHashMap<>();

	public static GameClientManager getInstance() {
		return instance;
	}

	public GameClient getGameClient(String sessionId) {
		return this.clients.get(sessionId);
	}

	public GameClient getGameClientByPlayer(long playerId) {
		return this.players.get(playerId);
	}

	public GameClient getGameClientByConnection(String connectionId) {
		return this.connections.get(connectionId);
	}

	public void addGameClientSession(GameClient gameClient) {
		clients.put(gameClient.getSessionId(), gameClient);
		log.info("addGameClientSession " + gameClient.toDetailString());
	}
	
	/** 
	 * 删除一个GameClient
	 * @param gameClient
	 */
	public void removeGameClient(GameClient gameClient, LogoutType logoutType) {
		log.info("removeGameClient LogoutType:" + logoutType + " " + gameClient.toDetailString());
		if (gameClient.getSessionId() != null) {
			GameClient gameClient2 = clients.get(gameClient.getSessionId());
			if (gameClient2 != null && gameClient2 == gameClient) {
				clients.remove(gameClient.getSessionId());
			}
		} else {
			log.warn("clients [{}] remove client[{}] unusual, gameClient.getSessionId() is null ", clients, gameClient.toDetailString());
		}
		if (gameClient.getPlayerId() > 0) {
			GameClient gameClient2 = players.get(gameClient.getPlayerId());
			if (gameClient2 != null && gameClient2 == gameClient) {
				players.remove(gameClient.getPlayerId());
			}
		}
		connections.remove(gameClient.getChannel().binaryHandlerID());

		gameClient.sendProtocol(PlayerLogoutPush_01100030.getDefaultInstance());
		gameClient.close();
	}

	public void addGameClientPlayer(GameClient gameClient) {
		if (gameClient.getPlayerId() > 0) {
			players.put(gameClient.getPlayerId(), gameClient);
			log.info("addGameClientPlayer " + gameClient.toDetailString());
		}else {
            log.warn("addGameClientPlayer playerId is 0, gameClient: " + gameClient.toDetailString());
		}
	}

	public void addGameClientConnection(String id,GameClient gameClient) {
		connections.put(id, gameClient);
		log.info("addGameClientConnection " + gameClient.toDetailString());
	}
	public GameClient removeGameClientConnection(String id) {
		log.info("removeGameClientConnection id: " + id);
		if (id == null) {
			return null;
		}
		return connections.remove(id);
	}

	/**
	 * 玩家退出，清除缓存，保存数据
	 * @param gameClient
	 * @return 
	 */
	public Future<?> logout(GameClient gameClient, LogoutType logoutType) {
		long playerId = gameClient.getPlayerId();
		log.info("GameClient[{}]start logout", gameClient.toDetailString());
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (player != null) {
			player.setIslogouting(true);
		}
		removeGameClient(gameClient, logoutType);
		broadcastOnlineToOtherServer(playerId, false, null);
		return PlayerHelper.logout(playerId);
	}
	
	/**
	 * 玩家退出
	 * @param playerId
	 * @return 
	 */
	public Future<?> logout(long playerId, LogoutType logoutType) {
		GameClient gameClient = getGameClientByPlayer(playerId);
		if (gameClient != null) {
			return logout(gameClient, logoutType);
		}
		return Future.succeededFuture();
	}

	public void checkClient() {
//			log.info("client session size : " + clients.size());
//			log.info("client players size : " + players.size());
//			log.info("client connections size : " + connections.size());
		if (clients.size() != players.size() || clients.size() != connections.size()) {
			log.warn("客户端连接管理异常,clients[{}]players[{}]connections[{}]", clients.size(), players.size(), connections.size());
		}
		List<GameClient> idletimeouts = new ArrayList<>();

		Iterator<GameClient> it = clients.values().iterator();
		GameClient gameClient;
		while (it.hasNext()) {
			gameClient = it.next();
//			idletimeouts.add(gameClient);
			if (gameClient.isIdleTimeOut(Config.lastRecvPacketTime)) {
				idletimeouts.add(gameClient);
			}
		}

		for (GameClient gc : idletimeouts) {
			try {
				log.warn("{}start logout by timeout", gc);
				ServerContext.getInstance().getProcessor().process(gc.getPlayerId(), () -> {
					logout(gc, LogoutType.Timeout);
//					log.info("{}logout by timeout", gc);
				});

			} catch (Exception e) {
				log.error("GameClient : " + gc + "logout failed", e);
			}
		}
//			idletimeouts.clear();
	}

	/**
	 * 同步持久化所有玩家的数据，一般用在服务器关闭时
	 */
	public void storeAllPlayers() {

		Collection<GameClient> lists = players.values();
		StopWatch watch = new StopWatch();
		watch.start();

//		List<Future> futures = new ArrayList<>();
		int onLineCount = lists.size();
		AtomicInteger finishCount = new AtomicInteger(0);
		Promise<Object> promise = Promise.promise();
		Future<Object> future = promise.future();
		if (onLineCount == 0) {
			promise.complete();
		}
		for (GameClient gameClient : lists) {
			ServerContext.getInstance().getProcessor().process(gameClient.getPlayerId(), () -> {
				Future<?> logout = logout(gameClient, LogoutType.ServerClose);
//				futures.add(logout);
				logout.onComplete(ar -> {
					if (ar.succeeded()) {
					} else {
						log.error(gameClient.getPlayerId() + " logout fail : ", ar.cause());
					}
					finishCount.getAndIncrement();
					if (finishCount.intValue() == onLineCount) {
						promise.complete();
					}
				});
			});
		}
//		CompositeFuture all = CompositeFuture.join(futures);
		try {
			AsyncUtils.await(future, Config.shutdownWaitTime, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("storeAllPlayers error,maybe time out ", e);
		}

		watch.stop();
		log.info("Store GameClient Total Size : [{}] usedTime[{}]ms", lists.size(), watch.getTime());
	}

	public void notifyLogoutAllClients() {
		for (GameClient gameClient : clients.values()) {
			gameClient.sendProtocol(PlayerLogoutPush_01100030.getDefaultInstance());
		}
	}

	/** 
	 * 退出所有玩家，一般测试使用。
	 * @param logoutType
	 */
	public void logoutAll(LogoutType logoutType) {
		log.info("All GameClient[{}]start logout," + logoutType);
		for (GameClient gameClient : clients.values()) {
			logout(gameClient, logoutType);
		}
	}

	/**
	 * 将消息广播给其他GameServer服务器
	 * @param message
	 *            待广播消息
	 * @param serverIds
	 *            接收消息的服务器id，如果是null，广播给所有的服务器
	 */
	public void broadcastGameServers(Message message, List<String> serverIds) {
//		int msgId = PbProtocol.getInstance().getMsgId(message.getClass().getSimpleName());
		if (serverIds == null || serverIds.isEmpty()) {
			VxHolder.broadcastRemoteServer(ServerType.Game, message);
		} else {
			for (String string : serverIds) {
				VxHolder.sendRemoteServer(string, message);
			}
		}
	}
	
	/**
	 * 将消息广播给其他CrossServer服务器
	 * @param message
	 *            待广播消息
	 * @param serverIds
	 *            接收消息的服务器id，如果是null，广播给所有的服务器
	 */
	public void broadcastCrossServers(Message message, List<String> serverIds) {
		if (serverIds == null || serverIds.isEmpty()) {
			VxHolder.broadcastRemoteServer(ServerType.Cross, message);
		} else {
			for (String string : serverIds) {
				VxHolder.sendRemoteServer(string, message);
			}
		}
	}

	/**
	 * 将玩家的在线状态广播给其他服务器，暂时广播给所有服务器，以后根据系统，广播给指定服务器
	 * @param playerId
	 * @param online
	 * @param serverIds
	 */
	public void broadcastOnlineToOtherServer(long playerId, boolean online, List<String> serverIds) {

		GamePlayerOnlinePush_7d000010 message = GamePlayerOnlinePush_7d000010.newBuilder().setPlayerId(playerId).setOnline(online).setServerId(ServerContext.getInstance().getServerId())
				.build();
		broadcastGameServers(message, serverIds);
		broadcastCrossServers(message, serverIds);
//		if (!online) {
//			PlayerManager.getInstance().offline(playerId);
//		}
	}

	public void noticeOne(Object message, Long pId) {
		GameClient gameClient = players.get(pId);
		if (gameClient != null) {
			gameClient.sendProtocol(message);
		}
	}

	public void noticeOne(Object message, Long pId, int errorNum) {
		GameClient gameClient = players.get(pId);
		if (gameClient != null) {
			gameClient.sendProtocol(message, errorNum);
		}
	}
	
	public boolean isOnline(long playerId) {

		return this.players.get(playerId) != null;
	}

	public int getOnlineCount() {
		return this.players.size();
	}

	public Collection<GameClient> getGameClients() {
		return this.players.values();
	}

	public String sizeStr() {
		int[] sizes = new int[] { clients.size(), players.size(), connections.size() };
		return String.format("GameClientManager: clients[%d], players[%d], connections[%d] ", sizes[0], sizes[1], sizes[2]);
	}

}
