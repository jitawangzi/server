package cn.game.games.net.game.remote;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.springframework.stereotype.Component;

import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.exception.LogicException;
import cn.game.core.net.remote.ServerStatus;
import cn.game.games.cache.entity.Friend;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.id.IdCache;
import cn.game.games.net.game.helper.FriendHelper;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.manager.PlayerNameManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import io.vertx.core.Future;

@Component
public class GameServerImpl implements GameServerInterface {

	@Override
	public boolean addFriend(long playerId, long friendId, String serverId) {

		boolean ret = FriendHelper.addFriend(playerId, friendId, serverId, Friend.FRIEND);

		FriendHelper.removeMyApplication(playerId, friendId);
		FriendHelper.removeApplication(playerId, friendId);
		return ret;
	}

	@Override
	public List<RewardInfo> addResources(long playerId, int id, int value) {
//		return PlayerHelper.addResources(player, id, value);
		return null;
	}
	
	@Override
	public boolean delResources(long playerId, int id, int value) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		PlayerHelper.delResources(player, id, value, OpType.GM);
		return true;
	}

	@Override
	public boolean alive() {
		return true;
	}

	@Override
	public void shutdown() {
		CompletableFuture.runAsync(() -> {
			System.exit(0);
		});
	}

	@Override
	public ServerStatus status() {
		ServerStatus status = new ServerStatus();
		status.setOnline(GameClientManager.getInstance().getOnlineCount());
		return status;
	}

	@Override
	public boolean addMail(long playerId, String serverId, int titleId, int contentId, int typeId,
			String resourceText) {
		List<Entry<Integer, Integer>> rewards = new ArrayList<Entry<Integer,Integer>>();
		
		String[] texts = resourceText.split(",");
		
		Entry<Integer, Integer> entry = null;
		for (String text : texts) {
			String[] params = text.split(":");
			entry = new Entry<Integer, Integer>() {

				@Override
				public Integer getKey() {
					return Integer.parseInt(params[0]);
				}

				@Override
				public Integer getValue() {
					return Integer.parseInt(params[1]);
				}

				@Override
				public Integer setValue(Integer value) {
					return null;
				}
			};						
		}
		
		rewards.add(entry);
		
		MailHelper.sendMailMultiLanguage(playerId, 0, titleId, contentId, (byte) typeId, rewards);
		return true;
	}

	@Override
	public void notifyAddForbidAccount(List<Long> pids, String reason, String timer) {
		pids.forEach(pid ->{
			PlayerManager.getInstance()
					.forbidAccount(pid, reason, timer+"",0);
		});
	}

	@Override
	public void addGlobalGmMail(int mailId) {
		MailHelper.addGlobalMail(mailId);
	}

	@Override
	public void delGlobalGmMail(int mailId) {
		MailHelper.removeGlobalMail(mailId);
	}

	@Override
  public void notifyDelForbidAccount(List<Long> pids) {
    pids.forEach(
        pid -> {
          PlayerManager.getInstance().unblockAccount(pid);
        });

	}

	@Override
	public boolean isObjectInCurrentServer(DistributedObjectType type, long objectId) {
		return IdCache.getManager(type).isObjectInCurrentServer(objectId);
	}

	@Override
	public Future<?> rename(long playerId, String newName) {
		return PlayerManager.getInstance().getPlayerAsync(playerId).compose(player -> {

			String oldName = player.getData().getName();
			Future<Boolean> checkFuture = PlayerHelper.checkContextData(player, newName);
			return checkFuture.compose(b -> {
				if (!b) {
					return Future.failedFuture(new LogicException(ErrorMsgEnum.player_name_illegal.ID));
				}
				return Future.fromCompletionStage(PlayerNameManager.getInstance().tryCreateUser(newName));
			}).compose(r -> {
				if (!r) {
					throw new LogicException(ErrorMsgEnum.player_name_repeat.ID);
				}
				CompletionStage<Void> completionStage = PlayerNameManager.getInstance()
						.saveName2Id(newName, player.getData().getPlayerId())
						.thenCompose(rr -> PlayerNameManager.getInstance().removeName(oldName))
						.thenCompose(rr -> PlayerHelper.saveSimplePlayer(player).toCompletionStage())
						.thenAccept(rr -> {
							player.getData().setName(newName);
						});
				;	
				return Future.fromCompletionStage(completionStage);
			});
		});
	}

}
