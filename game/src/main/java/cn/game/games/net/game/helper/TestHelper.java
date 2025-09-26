package cn.game.games.net.game.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.net.client.LogoutType;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Chapter;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.gm.GmHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.BattleModule;
import cn.game.games.net.game.module.currency.CurrencyModule;
import cn.game.games.net.game.module.item.ItemModule;
import cn.game.games.net.game.module.rank.RankEntry;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.DefenceSkinConfig;
import cn.game.protocol.generated.config.EquipConfig;
import cn.game.protocol.generated.config.GemConfig;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.config.SoulPetConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.DefenceSkinManager;
import cn.game.protocol.generated.manager.EquipManager;
import cn.game.protocol.generated.manager.GemManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.generated.manager.SoulPetManager;
import cn.game.protocol.generated.manager.VirtualServerManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.ServerMsg.LoginPlayerDeleteRequest_7d000080;
import cn.game.util.MapWrapper;
import cn.game.util.ServerType;
import io.vertx.core.Future;

/**    
 * 测试指令的一些方法
 * 2024年11月1日 16:50:21
 * @author SYQ
 */
public class TestHelper {
	private static final Logger log = LoggerFactory.getLogger(GmHelper.class);

	public static List<RewardInfo> addItems(Player player, int id, int count) {
		List<RewardInfo> allRewards = new ArrayList<>();
		List<RewardInfo> rewardItems = null;
		int goodsType = ItemHelper.getGoodsType(id);

		if (count == 0) {
			if (goodsType == 0) {
				goodsType = (byte) id;
			}
			boolean typeCheck = false;
			for (GoodsTypeEnum rewardInfo : GoodsTypeEnum.values()) {
				if (rewardInfo.getId() == goodsType) {
					typeCheck = true;
					break;
				}
			}
			if (!typeCheck) {
				player.fail(ErrorMsgEnum.config_data_not_found);
			}
			if (goodsType == GoodsTypeEnum.Resource.getId()) {
				for (Asset resourceEnum : Asset.values()) {
					// if (resourceEnum.getType() == 2 && !inExplore) {
					// continue;
					// }
					rewardItems = PlayerHelper.addResources(player, resourceEnum.ID, 1000000, OpType.Test);
					allRewards.addAll(rewardItems);
				}
			} else if (goodsType == GoodsTypeEnum.Item.getId()) {
				Collection<ItemConfig> list = ItemManager.instance().list();
				for (ItemConfig e : list) {
					rewardItems = PlayerHelper.addResources(player, e.ID, 80000, OpType.Test);
					allRewards.addAll(rewardItems);
				}
			} else if (goodsType == GoodsTypeEnum.Hero.getId()) {
				Collection<HeroConfig> list = HeroManager.instance().list();
				for (HeroConfig e : list) {
					if (e.HeroType != 1) {
						continue;
					}
					rewardItems = PlayerHelper.addResources(player, e.ID, 1, OpType.Test);
					allRewards.addAll(rewardItems);
				}
			} else if (goodsType == GoodsTypeEnum.Pet.getId()) {
				Collection<SoulPetConfig> list = SoulPetManager.instance().list();
				for (SoulPetConfig e : list) {
					rewardItems = PlayerHelper.addResources(player, e.ID, 10, OpType.Test);
					allRewards.addAll(rewardItems);
				}
			} else if (goodsType == GoodsTypeEnum.Equipment.getId()) {
				Collection<EquipConfig> list = EquipManager.instance().list();
				for (EquipConfig e : list) {
					rewardItems = PlayerHelper.addResources(player, e.ID, 1, OpType.Test);
					allRewards.addAll(rewardItems);
				}
			} else if (goodsType == GoodsTypeEnum.Gem.getId()) {
				Collection<GemConfig> list = GemManager.instance().list();
				for (GemConfig e : list) {
					rewardItems = PlayerHelper.addResources(player, e.ID, 10, OpType.Test);
					allRewards.addAll(rewardItems);
				}
			} else if (goodsType == GoodsTypeEnum.DefenceSkin.getId()) {
				Collection<DefenceSkinConfig> list = DefenceSkinManager.instance().list();
				for (DefenceSkinConfig e : list) {
					rewardItems = PlayerHelper.addResources(player, e.ID, 10, OpType.Test);
					allRewards.addAll(rewardItems);
				}
			} else {
				List<RewardInfo> tmp = PlayerHelper.addResources(player, id, count, OpType.Test);
				allRewards.addAll(tmp);
			}
		} else {
			rewardItems = PlayerHelper.addResources(player, id, count, OpType.Test);
			allRewards.addAll(rewardItems);
		}
		return allRewards;
	}

	/** 
	 * 某玩家删档，只是删除账号数据，没有删除这个玩家相关的全部数据。 
	 * @param playerId
	 */
	public static void deletePlayer(long deletePlayerId) {
		Player playerDelete = PlayerManager.getInstance().getPlayer(deletePlayerId);
		if (playerDelete != null) {
			GameClient gameClientByPlayer = GameClientManager.getInstance().getGameClientByPlayer(deletePlayerId);
			if (gameClientByPlayer != null) {
				GameClientManager.getInstance().removeGameClient(gameClientByPlayer, LogoutType.Delete);
			}
			PlayerHelper.clearPlayer(deletePlayerId);
		}
		// 删除数据库
		DAO.execute(PlayerDataMapper.class, MapperConstant.deleteByPrimaryKey, deletePlayerId);
		// 删除login账号
		VxHolder.requestRemoteServer(ServerType.Login, LoginPlayerDeleteRequest_7d000080.newBuilder().setPlayerId(deletePlayerId).build());
	}

	/** 
	 * 玩家退出（踢下线） 
	 * @param playerId
	 */
	public static void logoutPlayer(long playerId, LogoutType logoutType) {
		GameClient gameClientByPlayer = GameClientManager.getInstance().getGameClientByPlayer(playerId);
		if (gameClientByPlayer != null) {
			// 保存数据
			Future<?> logout = GameClientManager.getInstance().logout((GameClient) gameClientByPlayer, logoutType);
		}
	}

	/** 
	 * 玩家换服务器
	 * @param playerId
	 * @param targetServer
	 */
	public static void transferServer(long playerId, String targetServer) {
		String serverName = ServerHelper.getServerName(targetServer);
		if (StringUtils.isEmpty(serverName)) {
			log.warn("transferServer failed, player[{}],targetServer[{}] not found", playerId, targetServer);
			return;
		}
		PlayerHelper.modifyPlayer(playerId, player -> {
			String serverId = player.getData().getServerId();
			if (player.getData().getServerId().equals(targetServer)) {
				log.error("目标服务器与源服务器不能相同");
				return false;
			}
			// 修改玩家的服务器id
			player.getData().setServerId(targetServer);
			PlayerHelper.saveSimplePlayerToRedis(player);

			// 换排行榜
			for (RankType rankType : RankType.values()) {
				RankEntry rankEntry = RankService.getInstance().getRankEntry(serverId, rankType, playerId);
				if (rankEntry.getScore() > 0) {
					RankService.getInstance().setScoreAsync(targetServer, rankType, rankEntry.getId(), rankEntry.getScore());
					RankService.getInstance().removeRankAsync(rankType, serverId, playerId);
				}
			}
			return true;
		});
	}

	/** 
	 * 给资源，解锁功能等
	 * @param player
	 * @param opType
	 */
	public static  void setMaxCurrency(Player player, OpType opType) {
		CurrencyModule currencyModule = player.getCurrencyModule();
		MapWrapper currencyMap = currencyModule.getCurrencyMap();
		Asset[] values = Asset.values();
		for (Asset asset : values) {
			if (asset.Type == 1) {
				currencyMap.setValue(asset.ID, Integer.MAX_VALUE / 2);
			} else if (asset.Type == 2) {
				if (asset == Asset.playerExp) {
					currencyModule.addExp(asset.ID, 100_0000);
				}
			} else if (asset.Type == 3) {
				currencyMap.setValue(asset.ID, Integer.MAX_VALUE / 2);
			}
		}

		// 在给些道具。
		ItemModule itemModule = player.getItemModule();
		Collection<ItemConfig> list = ItemManager.instance().list();
		for (ItemConfig itemConfig : list) {
			int itemType = itemConfig.ItemType;
			if (itemType == 1 || itemType == 2 || itemType == 3 || itemType == 10 || itemType == 11 || itemType == 13) {
				itemModule.add(itemConfig.ID, Integer.MAX_VALUE / 2, opType);
			}
		}
		// 主线关卡全开,方便测试关卡
		BattleModule battleModule = player.getBattleModule();
		List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(DungeonTypeEnum.BattleChapter.getId());
		for (BattleConfig battleConfig : battleTypeList) {
			battleModule.setMainBattleHighest(battleConfig.ID);
			battleModule.addChapter(battleConfig.ID);
			Chapter chapter = battleModule.getChapter(battleConfig.ID);
			chapter.setBattleTime(30);
			chapter.setPass(true);
		}

		for (GemConfig config : GemManager.instance().list()) {
			PlayerHelper.addResources(player, config.ID, 100, opType);
		}
		for (EquipConfig config : EquipManager.instance().list()) {
			PlayerHelper.addResources(player, config.ID, 100, opType);
		}
		for (HeroConfig config : HeroManager.instance().list()) {
			if (config.HeroType == 1) {
				PlayerHelper.addResources(player, config.ID, 1, opType);
			}
		}
		
		// 最后解锁所有功能
		player.getFuncModule().gmUnlockFunc((byte) 0);

	}
}
