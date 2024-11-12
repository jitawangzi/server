package cn.game.games.net.cross.remote;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.net.remote.ServerStatus;
import cn.game.core.net.rpc.CallType;
import cn.game.games.net.cross.CrossServer;
import cn.game.games.net.game.remote.GameServerInterface;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;


public class CrossRemoteServerImpl implements CrossRemoteServerInterface {
	
	private static final Logger	log	= LoggerFactory.getLogger(CrossRemoteServerImpl.class);

	@Override
	public boolean addFriend(long playerId, long friendId, String serverId) {
		GameServerInterface gameServerInterface = CrossServer.getInstance().getGameServerInterface(CallType.PointToPoint, serverId);
		return gameServerInterface.addFriend(playerId, friendId, serverId);
	}

	@Override
	public List<RewardInfo> addResources(long playerId, int id, int value, String serverId) {
		GameServerInterface gameServerInterface = CrossServer.getInstance().getGameServerInterface(CallType.PointToPoint, serverId);
		return gameServerInterface.addResources(playerId, id, value);

	}
	
	@Override
	public boolean addMail(long playerId, String serverId, int titleId, int contentId, int typeId, String resourceText) {
		GameServerInterface gameServerInterface = CrossServer.getInstance().getGameServerInterface(CallType.PointToPoint, serverId);
		gameServerInterface.addMail(playerId, serverId, titleId, contentId, typeId, resourceText);
		return false;
	}
	@Override
	public void notifyBroadcastAddForbidAccount(List<Long> pids, String reason, String timer ){
		GameServerInterface gameServerInterface = CrossServer.getInstance().getGameServerInterface(CallType.Broadcast, null);
		gameServerInterface.notifyAddForbidAccount(pids, reason, timer);
	}
	@Override
	public void notifyBroadcastDelForbidAccount(List<Long> pids ){
		GameServerInterface gameServerInterface = CrossServer.getInstance().getGameServerInterface(CallType.Broadcast, null);
		gameServerInterface.notifyDelForbidAccount(pids);
	}

	@Override
	public void notifyBroadcastAddGlobalGmMail(int mailId) {
		CrossServer.getInstance().getGameServerInterface(CallType.Broadcast, null).addGlobalGmMail(mailId);
	}

	@Override
	public void notifyBroadcastDelGlobalGmMail(int mailId) {
		CrossServer.getInstance().getGameServerInterface(CallType.Broadcast, null).delGlobalGmMail(mailId);
	}

	@Override
	public List<ServerStatus> serverStatus(String[] serverIds) {
		List<ServerStatus> ret = new ArrayList<>();
		Supplier<ServerStatus>[] suppliers = new Supplier[serverIds.length];
		CompletableFuture<ServerStatus>[] futrues = new CompletableFuture[serverIds.length];

		int i = 0;
		for (String string : serverIds) {
			suppliers[i] = () -> CrossServer.getInstance().getGameServerInterface(CallType.PointToPoint, string).status();
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
				CrossServer.getInstance().getGameServerInterface(CallType.PointToPoint, string).shutdown();
			}
		} catch (Exception e) {
		}
	}
}
