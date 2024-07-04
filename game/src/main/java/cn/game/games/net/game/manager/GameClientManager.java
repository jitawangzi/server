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
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.task.TaskManager;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.GameServer;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg.PlayerLogoutPush_01100030;
import cn.game.protocol.protobuf.ServerMsg.GameCrossBroadcast_7d000008;
import cn.game.protocol.protobuf.ServerMsg.GameCrossForwardPush_7d000002;
import cn.game.protocol.protobuf.ServerMsg.GameCrossPlayerBroadcast_7d000005;
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
	
	public void removeGameClientSession(GameClient gameClient) {
		clients.remove(gameClient.getSessionId());
		log.info("removeGameClientSession " + gameClient.toDetailString());
	}
	
	/** 
	 * 删除一个GameClient
	 * @param gameClient
	 */
	public void removeGameClient(GameClient gameClient) {
		log.info("removeGameClient " + gameClient.toDetailString());
		if (gameClient.getSessionId() != null) {
			GameClient remove = clients.remove(gameClient.getSessionId());
			if (remove == null) {
				log.error("clients " + clients + " remove client " + gameClient.toDetailString() + " " + gameClient.toString());
			} else {
				log.info("remove old client " + remove.toDetailString());
			}
		} else {
			log.error("clients " + clients + " remove client " + gameClient.toDetailString());
		}
		if (gameClient.getPlayerId() > 0) {
			GameClient remove = players.remove(gameClient.getPlayerId());
			if (remove == null) {
				log.error("clients " + clients + " remove player " + gameClient.toDetailString() + " " + gameClient.toString());
			} else {
				log.info("remove old player " + remove.toDetailString());
			}
		}
		connections.remove(gameClient.getChannel().binaryHandlerID());

		gameClient.sendProtocol(PlayerLogoutPush_01100030.getDefaultInstance());
		gameClient.close();
	}

	/** 
	 * 一般是删除老的连接
	 * @param gameClient
	 */
	public void removeGameClientConnection(GameClient gameClient) {
		log.info("removeGameClient " + gameClient.toDetailString());
		if (gameClient.getSessionId() != null) {
			GameClient remove = clients.remove(gameClient.getSessionId());
			if (remove == null) {
				log.error("clients " + clients + " remove client " + gameClient.toDetailString() + " "
						+ gameClient.toString());
			} else {
				log.info("remove old client " + remove.toDetailString());
			}
		} else {
			log.error("clients " + clients + " remove client " + gameClient.toDetailString());
		}
		connections.remove(gameClient.getChannel().binaryHandlerID());

		gameClient.sendProtocol(PlayerLogoutPush_01100030.getDefaultInstance());
		gameClient.close();
	}

	public void addGameClientPlayer(GameClient gameClient) {

//		GameClient oldGameClient = players.get(gameClient.getPlayerId());
//		if (oldGameClient != null) {
//			removeGameClient(oldGameClient);
//		}
		if (gameClient.getPlayerId() > 0) {
			players.put(gameClient.getPlayerId(), gameClient);
			log.info("addGameClientPlayer " + gameClient.toDetailString());
		}
	}

	public void addGameClientConnection(String id,GameClient gameClient) {
		connections.put(id, gameClient);
		log.info("addGameClientConnection " + gameClient.toDetailString());
	}
	public GameClient removeGameClientConnection(String id) {
		log.info("removeGameClientConnection id: " + id);
		return connections.remove(id);
	}

	/**
	 * @Description 玩家退出，清除缓存，保存数据
	 * @param gameClient
	 * @return 
	 */
	public Future<?> logout(GameClient gameClient) {
		long playerId = gameClient.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (player != null) {
			player.setIslogouting(true);
		}
		removeGameClient(gameClient);
		broadcastOnlineToOtherServer(playerId, false, null);
		return PlayerManager.getInstance().logoutCache(playerId);
	}
	
	/**
	 * 玩家退出
	 * @param playerId
	 * @return 
	 */
	public Future<?> logout(long playerId) {
		GameClient gameClient = getGameClientByPlayer(playerId);
		if (gameClient != null) {
			return logout(gameClient);
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
				gc.getContext().runOnContext(r -> {
					logout(gc);
					log.info("{}logout by timeout", gc);
				});

			} catch (Exception e) {
				log.error("GameClient : " + gc + "logout failed", e);
			}
		}
//			idletimeouts.clear();
	}

	/**
	 * @Description 持久化所有玩家的数据
	 */
	public void storeAllPlayers() {

		Collection<GameClient> lists = players.values();
		StopWatch watch = new StopWatch();
		watch.start();

//		ExecutorService executorService = Executors.newFixedThreadPool(30);
//
//		for (GameClient client : lists) {
//			executorService.execute(() -> {
//				PlayerManager.getInstance().saveClientCache(client.getPlayerId(), true);
//			});
//		}
//		MoreExecutors.shutdownAndAwaitTermination(executorService, Config.shutdownWaitTime, TimeUnit.SECONDS);

//		List<Future> futures = new ArrayList<>();
		int onLineCount = lists.size();
		AtomicInteger finishCount = new AtomicInteger(0);
		Promise<Object> promise = Promise.promise();
		Future<Object> future = promise.future();
		if (onLineCount == 0) {
			promise.complete();
		}
		for (GameClient gameClient : lists) {
			gameClient.getContext().runOnContext(r -> {
				Future<?> logout = logout(gameClient);
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
			future.toCompletionStage().toCompletableFuture().get(Config.shutdownWaitTime, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("storeAllPlayers error,maybe time out ", e);
		}

		watch.stop();
		log.info("Store GameClient Total Size : [{}] usedTime[{}]ms", lists.size(), watch.getTime());
	}

	public void logoutAll() {
		for (GameClient gameClient : clients.values()) {
			gameClient.sendProtocol(PlayerLogoutPush_01100030.getDefaultInstance());
		}
	}

	/**
	 * @Description 给所有在线玩家广播消息
	 * @param message
	 */
	public void broadcast(Object message) {

		broadcast(message, null);
	}

	/**
	 * @Description 给指定的一些玩家广播消息
	 * @param message
	 * @param playerIds
	 */
	public void broadcast(Object message, List<Long> playerIds) {

		TaskManager.getInstance().addWorkerTask(() -> {
			if (playerIds == null) {

				for (GameClient gameClient : clients.values()) {
					gameClient.sendProtocol(message);
				}
			} else {

				for (Long id : playerIds) {
					noticeOne(message, id);
				}
			}
		});
	}
	
	/**
	 * @Description 将消息发送给指定服务器的玩家，通过cross服务器转发
	 * @param playerId
	 *            目标玩家id
	 * @param message
	 * @param serverId
	 *            目标服务器id
	 */
	@Deprecated
	public void sendToRemotePlayerOld(long playerId, String serverId, Message message) {
		int msgId = PbProtocol.getInstance().getMsgId(message.getClass().getSimpleName());
		GameCrossForwardPush_7d000002.Builder builder = GameCrossForwardPush_7d000002.newBuilder();
		int forwardMsgId = PbProtocol.GameCrossForwardPush_7d000002;

		builder.setData(message.toByteString());
		builder.setId(msgId);
		builder.setPlayerId(playerId);
		builder.setServerId(serverId);

		VxHolder.sendToRemoteServer(GameServer.getInstance().getServerId(ServerType.Cross), forwardMsgId, builder.build()
				.toByteArray());

	}
	/**
	 * @Description 将消息发送给指定Game服务器
	 * @param serverId
	 *            目标服务器id
	 * @param message
	 */
	public void sendToGameServer(String serverId, Message message) {
		int msgId = PbProtocol.getInstance().getMsgId(message.getClass().getSimpleName());
		VxHolder.sendToRemoteServer(serverId, msgId, message.toByteArray());
	}
	/**
	 * @Description 将消息发送给指定Game服务器，通过cross转发
	 * @param serverId
	 *            目标服务器id
	 * @param message
	 */
	@Deprecated
	public void sendToGameServerOld(String serverId, Message message) {
		int msgId = PbProtocol.getInstance().getMsgId(message.getClass().getSimpleName());
		GameCrossBroadcast_7d000008.Builder builder = GameCrossBroadcast_7d000008.newBuilder();
		int forwardMsgId = PbProtocol.GameCrossBroadcast_7d000008;

		builder.setData(message.toByteString());
		builder.setId(msgId);
		builder.addServerId(serverId);

		VxHolder.sendToRemoteServer(GameServer.getInstance().getServerId(ServerType.Cross), forwardMsgId, builder.build()
				.toByteArray());
	}

	/**
	 * @Description 给本服玩家和跨服玩家广播消息
	 * @param message
	 * @param playerIds
	 * @param serverIds
	 */
	public void broadcast(Message message, List<Long> playerIds, List<String> serverIds) {

		if (playerIds != null && serverIds != null) {

			TaskManager.getInstance().addWorkerTask(() -> {

				List<Long> localPlayers = new ArrayList<Long>();
				List<String> localServers = new ArrayList<String>();

				for (int i = 0; i < serverIds.size(); i++) {
					String serverId = serverIds.get(i);
					if (GameServer.getInstance().isLocalServer(serverId)) {
						localPlayers.add(playerIds.get(i));
						localServers.add(serverId);
					}
				}
				if (!localPlayers.isEmpty()) {
					broadcast(message, localPlayers);
					playerIds.removeAll(localPlayers);
					serverIds.removeAll(localServers);
				}

				PlayerHelper.sendToRemotePlayers(message, playerIds, serverIds);

			});
		}

	}

	/**
	 * @Description 将消息广播给跨服玩家
	 * @param message
	 * @param playerIds
	 * @param serverIds
	 */
	@Deprecated
	public void sendToRemotePlayersOld(Message message, List<Long> playerIds, List<String> serverIds) {
		int msgId = PbProtocol.getInstance().getMsgId(message.getClass().getSimpleName());
		GameCrossPlayerBroadcast_7d000005.Builder builder = GameCrossPlayerBroadcast_7d000005.newBuilder();
		int forwardMsgId = PbProtocol.GameCrossPlayerBroadcast_7d000005;

		builder.setData(message.toByteString());
		builder.setId(msgId);
		builder.addAllPlayerId(playerIds);
		builder.addAllServerId(serverIds);

		VxHolder.sendToRemoteServer(GameServer.getInstance().getServerId(ServerType.Cross), forwardMsgId, builder.build()
				.toByteArray());

	}
	/**
	 * @Description 将消息广播给其他GameServer服务器
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
				VxHolder.sendToRemoteServer(string, message);
			}
		}
	}
	/**
	 * @Description 将消息广播给其他GameServer服务器,通过cross服务器
	 * @param message
	 *            待广播消息
	 * @param serverIds
	 *            接收消息的服务器id，如果是null，广播给所有的服务器
	 */
	@Deprecated
	public void broadcastGameServersOld(Message message, List<String> serverIds) {
		int msgId = PbProtocol.getInstance().getMsgId(message.getClass().getSimpleName());
		GameCrossBroadcast_7d000008.Builder builder = GameCrossBroadcast_7d000008.newBuilder();
		int forwardMsgId = PbProtocol.GameCrossBroadcast_7d000008;

		builder.setData(message.toByteString());
		builder.setId(msgId);
		if (serverIds != null) {
			builder.addAllServerId(serverIds);
		}

		VxHolder.sendToRemoteServer(GameServer.getInstance().getServerId(ServerType.Cross), forwardMsgId, builder.build()
				.toByteArray());

	}
	/**
	 * @Description 将玩家的在线状态广播给其他服务器，暂时广播给所有服务器，以后根据系统，广播给指定服务器
	 * @param playerId
	 * @param online
	 * @param serverIds
	 */
	public void broadcastOnlineToOtherServer(long playerId, boolean online, List<String> serverIds) {

		GamePlayerOnlinePush_7d000010 message = GamePlayerOnlinePush_7d000010.newBuilder().setPlayerId(playerId).setOnline(online).setServerId(ServerContext.getInstance().getServerId())
				.build();
		broadcastGameServers(message, serverIds);
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
