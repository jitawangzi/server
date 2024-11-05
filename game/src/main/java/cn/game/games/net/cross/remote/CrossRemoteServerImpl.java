package cn.game.games.net.cross.remote;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.core.SimplePlayer;
import cn.game.games.net.cross.CrossServer;
import cn.game.games.net.game.remote.GameServerInterface;
import cn.game.games.net.game.remote.ServerStatus;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.Pair;


public class CrossRemoteServerImpl implements CrossRemoteServerInterface {
	
	private static final Logger	log	= LoggerFactory.getLogger(CrossRemoteServerImpl.class);

	@Override
	public List<SimplePlayer> getSimplePlayers(List<Long> playerIds, List<String> serverIds) {

		List<SimplePlayer> retList = new ArrayList<SimplePlayer>();

		for (int i = 0; i < serverIds.size(); i++) {

			GameServerInterface gameServerInterface = CrossServer.getInstance().getGameServer(serverIds.get(i));
			try {
				SimplePlayer simplePlayer = gameServerInterface.getSimplePlayer(playerIds.get(i));
				if (simplePlayer != null) {
					retList.add(simplePlayer);
				} else {
					log.warn("player not found :  id [{}]  serverId [{}]", playerIds.get(i), serverIds.get(i));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return retList;
	}

	@Override
	public SimplePlayer getSimplePlayer(long playerId, String serverId) throws Exception {
		GameServerInterface gameServerInterface = CrossServer.getInstance().getGameServer(serverId);
		return gameServerInterface.getSimplePlayer(playerId);

	}

	@Override
	public List<SimplePlayer> getSimplePlayers(List<Pair<Long, String>> players) {

		List<SimplePlayer> retList = new ArrayList<SimplePlayer>();

		for (Pair<Long, String> pair : players) {

			GameServerInterface gameServerInterface = CrossServer.getInstance().getGameServer(pair.second);
			try {
				SimplePlayer simplePlayer = gameServerInterface.getSimplePlayer(pair.first);
				if (simplePlayer != null) {
					retList.add(simplePlayer);
				} else {
					log.warn("player not found :  id [{}]  serverId [{}]", pair.first, pair.second);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return retList;
	}

	@Override
	public SimplePlayer searchFriendPlayer(long playerId, long searchPlayerId, String serverId) throws Exception {
		GameServerInterface gameServerInterface = CrossServer.getInstance().getGameServer(serverId);
		return gameServerInterface.searchFriendPlayer(playerId, searchPlayerId);

	}

	@Override
	public boolean addFriend(long playerId, long friendId, String serverId) {
		GameServerInterface gameServerInterface = CrossServer.getInstance().getGameServer(serverId);
		return gameServerInterface.addFriend(playerId, friendId, serverId);
	}

	@Override
	public List<RewardInfo> addResources(long playerId, int id, int value, String serverId) {
		GameServerInterface gameServerInterface = CrossServer.getInstance().getGameServer(serverId);
		return gameServerInterface.addResources(playerId, id, value);

	}
	
	@Override
	public boolean addMail(long playerId, String serverId, int titleId, int contentId, int typeId, String resourceText) {
		GameServerInterface gameServerInterface = CrossServer.getInstance().getGameServer(serverId);
		gameServerInterface.addMail(playerId, serverId, titleId, contentId, typeId, resourceText);
		return false;
	}
	@Override
	public void notifyBroadcastAddForbidAccount(List<Long> pids, String reason, String timer ){
		CrossServer.getInstance().getGameServerInterfaces().values().forEach(
				gameServerInterface -> gameServerInterface.notifyAddForbidAccount(pids, reason, timer)
		);
	}
	@Override
	public void notifyBroadcastDelForbidAccount(List<Long> pids ){
		CrossServer.getInstance().getGameServerInterfaces().values().forEach(
				gameServerInterface -> gameServerInterface.notifyDelForbidAccount(pids)
		);
	}

	@Override
	public void notifyBroadcastAddGlobalGmMail(int mailId) {
		CrossServer.getInstance().getGameServerInterfaces().values().forEach(
				gameServerInterface -> gameServerInterface.addGlobalGmMail(mailId)
		);
	}

	@Override
	public void notifyBroadcastDelGlobalGmMail(int mailId) {
		CrossServer.getInstance().getGameServerInterfaces().values().forEach(
				gameServerInterface -> gameServerInterface.delGlobalGmMail(mailId)
		);
	}

	@Override
	public List<ServerStatus> serverStatus(String[] serverIds) {
		List<ServerStatus> ret = new ArrayList<>();
		Supplier<ServerStatus>[] suppliers = new Supplier[serverIds.length];
		CompletableFuture<ServerStatus>[] futrues = new CompletableFuture[serverIds.length];

		int i = 0;
		for (String string : serverIds) {
			suppliers[i] = () -> CrossServer.getInstance().getGameServer(string).status();
			futrues[i] = CompletableFuture.supplyAsync(suppliers[i]);
			i++;

		}
		try {
			CompletableFuture.allOf(futrues).join();
		} catch (Exception e) {
		}

		for (int j = 0; j < suppliers.length; j++) {
			ServerStatus s = null;
			try {
				s = futrues[j].getNow(null);
			} catch (Exception e2) {
			}
			ret.add(s);
		}

		return ret;
	}

	@Override
	public void stopServers(String[] serverIds) {
		
		try {
			for (String string : serverIds) {
				CrossServer.getInstance().getGameServer(string).shutdown();
			}
		} catch (Exception e) {
		}
	}
}
