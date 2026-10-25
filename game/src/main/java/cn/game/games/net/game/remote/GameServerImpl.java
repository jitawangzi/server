package cn.game.games.net.game.remote;

import cn.game.games.net.game.helper.PlayerWriteGuard;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import cn.game.games.core.log.GameSSLogger;
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

        Player player = PlayerManager.getInstance().getPlayer(playerId);
        java.util.function.Supplier<Boolean> apply = () -> {
        boolean ret = FriendHelper.addFriend(playerId, friendId, serverId, Friend.FRIEND);

		FriendHelper.removeMyApplication(playerId, friendId);
		FriendHelper.removeApplication(playerId, friendId);
		return ret;
        };
        return player == null ? apply.get() : PlayerWriteGuard.call(player, apply);
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
			return PlayerWriteGuard.call(player, () -> mailModule.sendOnline(mail, notify));
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
        return PlayerManager.getInstance().getPlayerAsync(playerId).compose(player ->
                PlayerWriteGuard.submitValue(player, () -> {
                    // 同一玩家邮箱内等待外部校验；外部回调不再直接修改玩家字段。
                    String oldName = player.getData().getName();
                    if (!cn.game.core.util.AsyncUtils.await(PlayerHelper.checkContextData(player, newName))) {
                        throw new LogicException(ErrorMsgEnum.player_name_illegal.ID);
                    }
                    if (!cn.game.core.util.AsyncUtils.await(Future.fromCompletionStage(PlayerNameManager.getInstance().tryCreateUser(newName)))) {
                        throw new LogicException(ErrorMsgEnum.player_name_repeat.ID);
                    }
                    cn.game.core.util.AsyncUtils.await(Future.fromCompletionStage(PlayerNameManager.getInstance().saveName2Id(newName, playerId)));
                    cn.game.core.util.AsyncUtils.await(Future.fromCompletionStage(PlayerNameManager.getInstance().removeName(oldName)));
                    player.getData().setName(newName);
                    cn.game.core.util.AsyncUtils.await(PlayerHelper.saveSimplePlayer(player));
                    GameSSLogger.getInstance().logrolename(player, newName);
                    return null;
                }));
	}

	@Override
	public Future<Void> addEquipTowerHelp(long playerId, EquiptowerHelp help) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		EquipTowerBattle equipTower = player.getBattleModule().getBattle(DungeonTypeEnum.EquipTower);
        return PlayerWriteGuard.submit(player, () -> equipTower.addHelpReward_onLine(help));

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
