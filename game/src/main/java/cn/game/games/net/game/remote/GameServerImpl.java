package cn.game.games.net.game.remote;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.springframework.stereotype.Component;

import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.cache.id.IdCache;
import cn.game.core.exception.LogicException;
import cn.game.core.net.remote.ServerStatus;
import cn.game.games.cache.entity.EquiptowerHelp;
import cn.game.games.cache.entity.Friend;
import cn.game.games.cache.entity.Item;
import cn.game.games.cache.entity.Mail;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.GoodsModule;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.helper.FriendHelper;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.manager.PlayerNameManager;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.battle.EquipTowerBattle;
import cn.game.games.net.game.module.mail.MailModule;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import io.vertx.codegen.annotations.Nullable;
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
	public Future<?> addResources(long playerId, int id, int value) {
		return PlayerHelper.modifyPlayer(playerId, player -> {
			PlayerHelper.addResources(player, id, value, OpType.GM);
			return true;
		});
	}

	@Override
	public Future<?> delResources(long playerId, int itemId, int count) {

		return PlayerHelper.modifyPlayer(playerId, player -> {
			if (itemId > 0) {
				GoodsModule<? extends Item> goodsModule = player.getGoodsModule(itemId);
				long totalCount = goodsModule.getCount(itemId);
				long countDel = count > totalCount ? totalCount : count;
				if (count == 0) {
					countDel = totalCount;
				}
				PlayerHelper.delResources(player, itemId, countDel, OpType.Test);
			} else {
				player.getCurrencyModule().getCurrencyMap().clear();
				player.getItemModule().getId_items().clear();
			}
			return true;
		});
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
	public Future<@Nullable Object> addMail(long receiverId, int mailId, Object[] contentArguments, String sender, String title,
			String content, int type, List<Goods> attachmentList, boolean notify) {
		Mail mail = Mail.valueOf(receiverId, mailId, contentArguments, sender, title, content, type, attachmentList);
		if (PlayerManager.getInstance().hasCache(receiverId)) { // 在线，或者服务器中还有玩家缓存
			Player player = PlayerManager.getInstance().getPlayer(receiverId);
			MailModule mailModule = player.getMailModule();
			return mailModule.sendOnline(mail, notify);
		}
		return mail.insert();
	}

	@Override
	public void notifyAddForbidAccount(List<Long> pids, String reason, String timer) {
		pids.forEach(pid -> {
			PlayerManager.getInstance().forbidAccount(pid, reason, timer + "", 0);
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
		pids.forEach(pid -> {
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

	@Override
	public Future<Void> addEquipTowerHelp(long playerId, EquiptowerHelp help) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		EquipTowerBattle equipTower = player.getBattleModule().getBattle(DungeonTypeEnum.EquipTower);
		equipTower.addHelpReward_onLine(help);
		return Future.succeededFuture();

	}

	@Override
	public Future<SimplePlayer> getSimplePlayer(long playerId) {
		Player player = PlayerManager.getInstance().getPlayer(playerId); 
		if (player == null) {
			return Future.failedFuture(ErrorMsgEnum.player_data_not_found.ID + "");
		}
		return Future.succeededFuture(new SimplePlayer(player));
	}
}
