package cn.game.games.net.game.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.core.net.client.LogoutType;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.AsyncUtils;
import cn.game.core.util.BatchQueryUtil;
import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.GoodsModule;
import cn.game.games.core.SimplePlayer;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.log.GameLogger;
import cn.game.games.core.push.PushService;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.game.GameServer;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.db.DbTask;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.manager.PlayerNameManager;
import cn.game.games.net.game.module.account.Account;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.rank.RankModule;
import cn.game.games.util.BIHelper;
import cn.game.games.util.DAO;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.config.ConsumeConfig;
import cn.game.protocol.generated.config.ExpConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.config.RandomGivenConfig;
import cn.game.protocol.generated.config.RandomGroupConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.ConditionManager;
import cn.game.protocol.generated.manager.ConsumeManager;
import cn.game.protocol.generated.manager.FairyFriendFavorabilityManager;
import cn.game.protocol.generated.manager.FundPassUpgradeManager;
import cn.game.protocol.generated.manager.HeroBandBookManager;
import cn.game.protocol.generated.manager.QiankunMirrorLvManager;
import cn.game.protocol.generated.manager.RandomGivenManager;
import cn.game.protocol.generated.manager.RandomGroupManager;
import cn.game.protocol.generated.manager.UserUpgradeManager;
import cn.game.protocol.generated.manager.VIPManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BaseMsg.AssetInfo;
import cn.game.protocol.protobuf.BaseMsg.HCHeroInfo;
import cn.game.protocol.protobuf.BaseMsg.HeroInfo;
import cn.game.protocol.protobuf.BaseMsg.ItemInfo;
import cn.game.protocol.protobuf.BaseMsg.MergeEquipmentInfo;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099;
import cn.game.protocol.protobuf.PlayerMsg.PlayerLoginResponse_01000002;
import cn.game.protocol.protobuf.RewardMsg;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.RewardMsg.SpendPush_55001501;
import cn.game.protocol.protobuf.ServerMsg.GamePlayerPush_7d000100;
import cn.game.protocol.protobuf.ServerMsg.GamePlayerRequest_7d000015;
import cn.game.protocol.protobuf.ServerMsg.GamePlayerResponse_7d000016;
import cn.game.util.Config;
import cn.game.util.DateUtil;
import cn.game.util.GameUtil;
import cn.game.util.JsonUtil;
import cn.game.util.RedisUtil;
import cn.game.util.Rnd;
import cn.game.util.SpringContextLoader;
import cn.game.util.log.LoggerType;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.json.JsonObject;

public class PlayerHelper {

	private static final Logger log = LoggerFactory.getLogger(PlayerHelper.class);
//	private static final Logger resourceDelLog = LoggerFactory.getLogger("resourceDelLog");
//	private static final Logger levellog = LoggerFactory.getLogger("levelLog");
//	private static final Logger loginlog = LoggerFactory.getLogger("loginLog");
	public static final int REFRESH_TYPE_DAY = 1;
	public static final int REFRESH_TYPE_WEEK = 2;
	public static final int REFRESH_TYPE_MONTH = 3;

	/** 
	 * 判断玩家是否有足够的物品
	 * @param player
	 * @param list entry key:物品id,entry value:数量
	 * @return
	 */
	public static boolean isEnough(Player player, Collection<? extends Entry<Integer, Integer>> list) {

		if (list == null || list.isEmpty()) {
			return true;
		}
		for (Entry<Integer, Integer> entry : list) {
			if (!isEnough(player, entry.getKey(), entry.getValue())) {
				return false;
			}
		}
		return true;
	}

	/** 
	 * 判断玩家是否有足够的物品
	 * @param player
	 * @param map key:物品id, value:数量
	 * @return
	 */
	public static boolean isEnough(Player player, Map<Integer, Integer> map) {
		if (map == null || map.isEmpty()) {
			return true;
		}
		return isEnough(player, map.entrySet());
	}

	/**
	 * or的关系判断,二维数组中有一个满足就可以,暂时不用了
	 * @param player
	 * @param list
	 * @return
	 */
	@Deprecated
	public static boolean isEnoughOr(Player player, int[][] list) {

		if (list == null || list.length == 0) {
			return true;
		}
		for (int i = 0; i < list.length; i++) {
			if (isEnough(player, list[i])) {
				return true;
			}
		}
		return false;
	}

	/** 
	 * 判断玩家是否有足够的物品
	 * @param player
	 * @param list ，数组0是id，1是数量
	 * @return
	 */
	public static boolean isEnough(Player player, int[][] list) {

		if (list == null || list.length == 0) {
			return true;
		}
		for (int i = 0; i < list.length; i++) {
			if (!isEnough(player, list[i])) {
				return false;
			}
		}
		return true;
	}

	/** 
	 * 判断玩家是否有足够的物品
	 * @param player
	 * @param list ，数组0是id，1是数量，也可以向后扩展，例如 2是id，3是数量
	 * @return
	 */
	public static boolean isEnough(Player player, int[] list) {
		if (list == null || list.length == 0) {
			return true;
		}
		for (int i = 0; i < list.length; i += 2) {
			if (!isEnough(player, list[i], list[i + 1])) {
				return false;
			}
		}
		return true;
	}

	/** 
	 * 判断玩家是否有足够的物品
	 * @param player
	 * @param id 物品id，可能是({@link Asset#ID}) 或者是({@link ItemConfig#ID}) ({@link HeroConfig#ID})等等的id
	 * 通过物品id的规则，计算出属于什么物品类型
	 * 所有物品类型 : {@link GoodsTypeEnum#getId()}
	 * @param count
	 * @return
	 */
	public static boolean isEnough(Player player, int id, int count) {
		return player.getGoodsModule(id).isEnough(id, count);
	}

	/**
	 * 合并同id 的资源和道具的数量。
	 * @param rewards
	 */
	public static void mergeRewards(List<RewardInfo> rewards) {
		cn.game.protocol.protobuf.BaseMsg.ItemInfo.Builder itemBuilder = ItemInfo.newBuilder();
		cn.game.protocol.protobuf.BaseMsg.AssetInfo.Builder assetBuilder = AssetInfo.newBuilder();

		Iterator<RewardInfo> iterator = rewards.iterator();
		while (iterator.hasNext()) {
			RewardMsg.RewardInfo rewardInfo = (RewardMsg.RewardInfo) iterator.next();
			if (rewardInfo.hasItem()) {
				ItemInfo item = rewardInfo.getItem();
				if (itemBuilder.getId() == 0 || itemBuilder.getId() == item.getId()) {
					itemBuilder.setId(item.getId());
					itemBuilder.setCount(itemBuilder.getCount() + item.getCount());
					iterator.remove();
				}
			}
			if (rewardInfo.hasAsset()) {
				AssetInfo asset = rewardInfo.getAsset();
				if (assetBuilder.getId() == 0 || assetBuilder.getId() == asset.getId()) {
					assetBuilder.setId(asset.getId());
					assetBuilder.setCount(assetBuilder.getCount() + asset.getCount());
					iterator.remove();
				}
			}

		}
		if (itemBuilder.getId() > 0) {
			rewards.add(RewardInfo.newBuilder().setItem(itemBuilder.build()).build());
		}
		if (assetBuilder.getId() > 0) {
			rewards.add(RewardInfo.newBuilder().setAsset(assetBuilder.build()).build());
		}
	}

	/**
	 * 根据现有的奖励，在给n倍的奖励
	 * @param player
	 * @param rewards
	 * @param multiple
	 * @return
	 */
	public static List<RewardInfo> multipleRewards(Player player, List<RewardInfo> rewards, int multiple) {
		List<RewardInfo> ret = new ArrayList<>();
		Iterator<RewardInfo> iterator = rewards.iterator();
		while (iterator.hasNext()) {
			RewardMsg.RewardInfo rewardInfo = (RewardMsg.RewardInfo) iterator.next();
			if (rewardInfo.hasItem()) {
				ItemInfo item = rewardInfo.getItem();
				ret.addAll(addResources(player, item.getId(), item.getCount() * multiple, OpType.BattleEndMultipleReward));
			} else if (rewardInfo.hasAsset()) {
				AssetInfo asset = rewardInfo.getAsset();
				ret.addAll(addResources(player, asset.getId(), (int) asset.getCount() * multiple, OpType.BattleEndMultipleReward));
			} else if (rewardInfo.hasRole()) {
				HeroInfo info = rewardInfo.getRole();
				ret.addAll(addResources(player, info.getConfigId(), multiple, OpType.BattleEndMultipleReward));
			} else if (rewardInfo.hasMergeEquip()) {
				MergeEquipmentInfo info = rewardInfo.getMergeEquip();
				ret.addAll(addResources(player, info.getId(), multiple, OpType.BattleEndMultipleReward));
			} else if (rewardInfo.hasHcHero()) {
				HCHeroInfo info = rewardInfo.getHcHero();
				ret.addAll(addResources(player, info.getConfigId(), multiple, OpType.BattleEndMultipleReward));
			}
		}
		return ret;
	}

	/** 
	 * 给某玩家增加物品
	 * @param player
	 * @param id 物品id，可能是({@link Asset#ID}) 或者是({@link ItemConfig#ID}) ({@link HeroConfig#ID})等等的id 
	 * @param value 增加数量
	 * @param opType  操作类型
	 * @param notify   增加后，是否通知客户端
	 * @return
	 */
	public static List<RewardInfo> addResources(Player player, int id, int value, OpType opType, boolean notify) {
		if (value < 0) {
			return Collections.EMPTY_LIST;
		}
		List<RewardInfo> rewards = null;
		try {
			GoodsModule goodsModule = player.getGoodsModule(id);
			rewards = goodsModule.addReward(id, value, opType);
			log.info("player[{}] addReward  id[{}]count[{}]opType[{}]", player.getPlayerId(), id, value, opType);
			player.handleEvent(EventTypeEnum.GetItem, id, value);
			BIHelper.resourceUpdate(player, id, value, opType, true);
			if (notify && !rewards.isEmpty()) {
				player.getGameClient().sendProtocol(PbBuilder.buildRewardPush(rewards));
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("添加物品出现异常,id： " + id, e);
		}
		return rewards;
	}

	public static List<RewardInfo> addResources(Player player, int id, int value, OpType opType) {
		return addResources(player, id, value, opType, false);
	}

	public static List<RewardInfo> addResources(Player player, int id, int value) {
		return addResources(player, id, value, OpType.None, false);
	}

	/**
	 * 进行某操作时扣除资源，包括所有大类型
	 * @param player
	 * @param id 物品id，可能是({@link Asset#ID}) 或者是({@link ItemConfig#ID}) ({@link HeroConfig#ID})等等的id
	 * @param value
	 * @param mode 数值或当前百分比
	 * @param consumeType
	 * @return
	 */
	@Deprecated
	public static boolean delResources(Player player, int id, int value, int mode, OpType consumeType) {
		if (value <= 0) {
			return true;
		}
		if (mode != 0) {
			if (value > 100) {
				return true;
			}
			long playerValue = player.getGoodsModule(id).getCount(id);
			value = Math.round(playerValue * (100 - value) / 100f);
		}

		return delResources(player, id, value, consumeType, true);

	}

	/** 
	 * 扣除玩家的物品
	 * @param player
	 * @param id 物品id，可能是({@link Asset#ID}) 或者是({@link ItemConfig#ID}) ({@link HeroConfig#ID})等等的id
	 * @param value 减少的数量
	 * @param consumeType 消耗类型
	 * @return
	 */
	public static boolean delResources(Player player, int id, long value, OpType consumeType) {
		return delResources(player, id, value, consumeType, true);
	}

	/** 
	 * 通过消耗表的id，来扣除玩家的物品
	 * @param player
	 * @param consumeId 消耗表的id  {@link ConsumeConfig#ID}
	 * @param consumeType 消耗类型
	 * @return
	 */
	public static boolean delResources(Player player, int consumeId, OpType consumeType) {
		if (consumeId == 0) {
			return true;
		}
		ConsumeConfig consumeConfig = ConsumeManager.instance().get(consumeId);
		return delResources(player, consumeConfig.cost, consumeType);
	}

	/**
	 * 进行某操作时扣除资源，包括所有大类型
	 * @param player
	 * @param id 物品id，可能是({@link Asset#ID}) 或者是({@link ItemConfig#ID}) ({@link HeroConfig#ID})等等的id
	 * @param value 减少的数量
	 * @param consumeType 	    消耗类型
	 * @param notify 是否通知客户端  如果直接调用该方法不涉及合并问题则传true, 如果涉及合并则传false，合并后需要推送协议SpendPush_55001501
	 * @return
	 */
	public static boolean delResources(Player player, int id, long value, OpType consumeType, boolean notify) {

		if (value <= 0) {
			return true;
		}
		GoodsModule goodsModule = player.getGoodsModule(id);
		boolean ret = goodsModule.del(id, value, consumeType);

		if (ret) {
			player.handleEvent(EventTypeEnum.CostItem, id, (int) value);
//			resourceDelLog.info("opType[resourceDel]playerId[{}]resourceId[{}]value[{}]consumeType[{}]", player.getPlayerId(), id, value,
//					consumeType == null ? "NO_DEFINE" : consumeType.getName());
			if (notify) {
				SpendPush_55001501.Builder spendPush = SpendPush_55001501.newBuilder();
				spendPush.addSpend(PbBuilder.buildGoodsInfo(id, value));
				player.getGameClient().sendProtocol(spendPush.build());
			}
			BIHelper.resourceUpdate(player, id, value, consumeType, false);
		}
		return ret;
	}

	/**
	 * 进行某操作时扣除资源，包括所有大类型
	 * @param player 玩家
	 * @param list，entry key:物品id,entry value:数量
	 * @param consumeType 消耗类型
	 * @return
	 */
	public static boolean delResources(Player player, Collection<? extends Entry<Integer, Integer>> list, OpType consumeType) {

		if (list == null || list.isEmpty()) {
			return true;
		}

		if (isEnough(player, list)) {
			SpendPush_55001501.Builder spendPush = SpendPush_55001501.newBuilder();
			for (Entry<Integer, Integer> entry : list) {
				if (entry.getValue() <= 0) {
					continue;
				}
				delResources(player, entry.getKey(), entry.getValue(), consumeType, false);
				spendPush.addSpend(PbBuilder.buildGoodsInfo(entry.getKey(), entry.getValue()));
			}
			if (spendPush.getSpendCount() > 0) {
				player.getGameClient().sendProtocol(spendPush.build());
			}
			return true;
		}
		return false;
	}

	/**
	 * 进行某操作时扣除资源，包括所有大类型
	 * @param player
	 * @param map，key:物品id,value:数量
	 * @param consumeType 消耗类型
	 * @return
	 */
	public static boolean delResources(Player player, Map<Integer, Integer> map, OpType consumeType) {

		if (map == null || map.isEmpty()) {
			return true;
		}
		return delResources(player, map.entrySet(), consumeType);
	}

	/** 
	 * 进行某操作时扣除资源，包括所有大类型
	 * @param player
	 * @param list ，数组0是id，1是数量
	 * @param consumeType
	 * @return
	 */
	public static boolean delResources(Player player, int[][] list, OpType consumeType) {

		if (list == null || list.length == 0) {
			return true;
		}
		if (isEnough(player, list)) {
			SpendPush_55001501.Builder spendPush = SpendPush_55001501.newBuilder();
			for (int i = 0; i < list.length; i++) {
				for (int j = 0; j < list[i].length; j += 2) {
					if (list[i][j + 1] <= 0) {
						continue;
					}
					delResources(player, list[i][j], list[i][j + 1], consumeType, false);
					spendPush.addSpend(PbBuilder.buildGoodsInfo(list[i][j], list[i][j + 1]));
				}
			}
			if (spendPush.getSpendCount() > 0) {
				player.getGameClient().sendProtocol(spendPush.build());
			}
			return true;
		}
		return false;
	}

	/** 
	 * 进行某操作时扣除资源，包括所有大类型
	 * @param player
	 * @param list ，数组0是id，1是数量,也可以向后扩展，例如 2是id，3是数量
	 * @param consumeType
	 * @return
	 */
	public static boolean delResources(Player player, int[] list, OpType consumeType) {

		if (list == null || list.length == 0) {
			return true;
		}
		if (isEnough(player, list)) {
			SpendPush_55001501.Builder spendPush = SpendPush_55001501.newBuilder();
			for (int j = 0; j < list.length; j += 2) {
				if (list[j + 1] <= 0) {
					continue;
				}
				delResources(player, list[j], list[j + 1], consumeType, false);
				spendPush.addSpend(PbBuilder.buildGoodsInfo(list[j], list[j + 1]));
			}
			if (spendPush.getSpendCount() > 0) {
				player.getGameClient().sendProtocol(spendPush.build());
			}
			return true;
		}
		return false;
	}

	/**
	 * 根据奖励id，增加所有物品
	 * @param player
	 * @param randomRewardId  {@link RandomGivenConfig#ID}
	 * @return
	 */
	public static List<RewardInfo> addReward(Player player, int randomRewardId, OpType opType) {
		return addReward(player, randomRewardId, 0, opType);
	}

	/**
	 * 根据奖励id，增加所有物品,带加成
	 * @param player
	 * @param randomRewardId  {@link RandomGivenConfig#ID}
	 * @param additionValue,额外增加的数量加成， 除以10000
	 * @return
	 */

	public static List<RewardInfo> addReward(Player player, int randomRewardId, int additionValue, OpType opType) {
		List<Goods> randomReward = randomReward(randomRewardId);
		if (additionValue > 0) {
			for (Goods goods : randomReward) {
				goods.setCount((goods.getCount() + (int) (goods.getCount() * additionValue / 10000f)));
			}
		}
		List<RewardInfo> resources = addGoods(player, randomReward, opType);
		return resources;
	}

	/**
	 * 只是随机出来具体的奖励，不加到玩家身上,较少直接用到
	 * @param player
	 * @param randomRewardId {@link RandomGivenConfig#ID}
	 * @return
	 */
	public static List<Goods> randomReward(int randomRewardId) {
		if (randomRewardId <= 0) {
			return Collections.EMPTY_LIST;
		}
		List<Goods> ret = new ArrayList<>();
		RandomGivenConfig randomGivenConfig = RandomGivenManager.instance().get(randomRewardId);
		for (int[] rewardInfo : randomGivenConfig.MustGiven) {
			for (int i = 0; i < rewardInfo.length; i += 2) {
				Goods goods = new Goods(rewardInfo[i], rewardInfo[i + 1]);
				ret.add(goods);
			}
		}
		if (randomGivenConfig.RandomNumber.length > 0) {
			int randomCount = 0;
			if (randomGivenConfig.RandomNumber.length == 1) {
				randomCount = randomGivenConfig.RandomNumber[0];
			} else if (randomGivenConfig.RandomNumber.length == 2) {
				randomCount = Rnd.get(randomGivenConfig.RandomNumber[0], randomGivenConfig.RandomNumber[1]);
			} else {
				throw new IllegalArgumentException("RandomGiven奖励数量貌似不对：  " + randomGivenConfig.ID);
			}
			for (int i = 0; i < randomCount; i++) {
				int randomIndex = Rnd.randomIndex(randomGivenConfig.RandomParameterWeight);
				int group = randomGivenConfig.RandomParameterGroupId[randomIndex];
				List<RandomGroupConfig> randomGroupIDList = RandomGroupManager.instance().getRandomGroupIDList(group);
				RandomGroupConfig groupConfig = Rnd.randomWeighableElement(randomGroupIDList);

				Goods goods = new Goods(groupConfig.AssetID, groupConfig.Several);
				ret.add(goods);
			}
		}
		for (int i = 0; i < randomGivenConfig.FixedNumRandomDrop.length; i++) {
			int group = randomGivenConfig.FixedNumRandomDrop[i][0];
			int randomDropCount = randomGivenConfig.FixedNumRandomDrop[i][1];
			List<RandomGroupConfig> randomGroupIDList = RandomGroupManager.instance().getRandomGroupIDList(group);
			for (int j = 0; j < randomDropCount; j++) {
				RandomGroupConfig groupConfig = Rnd.randomWeighableElement(randomGroupIDList);
				Goods goods = new Goods(groupConfig.AssetID, groupConfig.Several);
				ret.add(goods);
			}
		}
		return ret;
	}

	/**
	 * 上线时刷新数据
	 * @param player
	 */
	public static void refresh(Player player) {
		player.handleEvent(EventTypeEnum.LoginSuccess);
		refreshDay(player);
		refreshFiveDay(player);
		refreshWeek(player);
		refreshMonth(player);
	}

	/**
	 * 初始化新角色数据，角色第一次创建时需要调用此方法
	 * @param player
	 */
	public static void initNewPlayerData(Player player) {
		Long playerId = player.getData().getPlayerId();
		log.info("首次初始化角色playerId={}", playerId);
		// 对模块数据初始化顺序有要求的，其他模块需要的， 一些基础数据尽量放到这里初始化。
		player.getPlayerModule().initLevel();

		if (ServerContext.getInstance().getRunMode().isPressure()) {
			player.getCurrencyModule().setMaxCurrency();
		}

		// 对于事件的处理是没有顺序的
		player.handleEvent(EventTypeEnum.PLAYER_CREATE);

		GameLogger.rolebuild(player);
	}

	/**
	 * 登陆后进行一些初始化操作，例如刷新离线数据、初始化定时任务等等,
	 * 创建新玩家后，也会执行此方法;
	 * @param player
	 */
	public static void initAfterLogin(Player player) {
		player.handleEvent(EventTypeEnum.Login);

		long playerId = player.getData().getPlayerId();
		player.getData().setLoginDate(DateUtil.getStringDate());

		PlayerHelper.refresh(player);

		// player 表，定时保存
//		OnLineTaskManager.getInstance().addScheduledTasksAtFixedRate(playerId, () -> {
//			PlayerHelper.addTask(playerId, r -> {
//				PlayerManager.getInstance().saveClientCache(playerId);
//			}) ;
//		}, Config.ONELINE_SAVE, Config.ONELINE_SAVE);
		player.setPeriodicTask(Config.ONLINE_SAVE * 1000, r -> {
			saveClientCache(player.getPlayerId());
			RankModule rankModule = player.getModule(RankModule.class);
			rankModule.updateHeroCombatRank();
			PlayerHelper.saveSimplePlayerToRedis(player);
		});
		// 上线后生成自己的简单信息
//		try {
//			PlayerManager.getInstance().getAndLoadSimplePlayer(playerId);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		player.handleEvent(EventTypeEnum.LoginFinish);
		Collection<BasePlayerModule> allModule = player.getAllModule();
		for (BasePlayerModule basePlayerModule : allModule) {
			basePlayerModule.onLogin();
		}

		GameLogger.login(player);
		GameLogger.rolelogin(player);
		GameLogger.login_wxxcx(player);

//		levellog.info("opType[levelUp]playerId[{}]newLevel[{}]", player.getPlayerId(), player.getLevel());
//		loginlog.info("opType[gameLogin]playerId[{}]isCreate[{}]isLogin[{}]onlineTime[{}]", player.getPlayerId(), true, true, 0);
	}

	public static void refreshDay(Player player) {
		// int nowDay =
		// util.DateUtil.getDay(GlobalFormulaManager.getInstance().getInt("globalResetTime"));
		// 几点刷新天
		// int nowDay = util.DateUtil.getDay(5);
		int nowDay = DateUtil.getDay();
		if (nowDay == player.getData().getRefreshDay()) {
			return;
		}
		long playerId = player.getData().getPlayerId();
		log.info("new day refresh player:" + playerId);
		// 各个模块各自刷新
		player.handleEvent(EventTypeEnum.NewDay);

		player.getData().setRefreshDay(nowDay);

		log.info("new day refresh player:" + playerId + " succ");
	}

	public static void refreshWeek(Player player) {
		// int nowDay =
		// util.DateUtil.getDay(GlobalFormulaManager.getInstance().getInt("globalResetTime"));
		// 几点刷新天
		int nowWeek = DateUtil.getWeek();
		if (nowWeek == player.getData().getRefreshWeek()) {
			return;
		}
		long playerId = player.getData().getPlayerId();
		log.info("new week refresh player:" + playerId);

		player.handleEvent(EventTypeEnum.NewWeek);

		player.getData().setRefreshWeek(nowWeek);

		log.info("new week refresh player:" + playerId + " succ");

	}

	public static void refreshMonth(Player player) {
		int nowMonth = DateUtil.getMonth();
		// 设置参数
		if (player.getData().getRefreshMonth() == null) {
			player.getData().setRefreshMonth(nowMonth);
			return;
		}
		if (nowMonth == player.getData().getRefreshMonth()) {
			return;
		}
		long playerId = player.getData().getPlayerId();
		log.info("new month refresh player:" + playerId);
		player.handleEvent(EventTypeEnum.NewMonth);

		player.getData().setRefreshMonth(nowMonth);
		log.info("new month refresh player:" + playerId + " succ");

	}

	public static void refreshFiveDay(Player player) {
		// 每天早晨5点刷新
		long playerId = player.getData().getPlayerId();
		int fiveTime = (int) (DateUtil.getDayHourTimestamp(5) / 1000);

		if (player.getData().getRefreshFiveDay() == null) {
			player.getData().setRefreshFiveDay(0);
		}

		if (player.getData().getRefreshFiveDay() >= fiveTime) {
			return;
		}

		int secord = fiveTime - player.getData().getRefreshFiveDay();

		long nowTime = DateUtil.currentTimeSeconds();

		// 判断必须要跨一天以上才可以刷
		int refTime = fiveTime;

		if (nowTime < fiveTime) {

			refTime = (int) (fiveTime - DateUtil.DAY_SECONDS);

			if (secord == DateUtil.DAY_SECONDS) {
				return;
			}
		}
		player.handleEvent(EventTypeEnum.NewDay5);

		player.getData().setRefreshFiveDay(refTime);

		log.info("new day five clock refresh player:" + playerId + " succ");
	}

	/**
	 * 一次性增加多个奖励
	 * @param player
	 * @param goods
	 * @return
	 */
	public static List<RewardInfo> addResources(Player player, List<Goods> goods, OpType opType) {
		List<RewardInfo> rewardItems = new ArrayList<>();
		if (goods != null && goods.size() > 0) {
			for (Goods g : goods) {

				List<RewardInfo> rewardItem = addResources(player, g.getId(), g.getCount(), opType, false);
				rewardItems.addAll(rewardItem);
			}
//			PlayerHelper.sendProtocol(player.getPlayerId(), RewardMsg.RewardPush_55000501.newBuilder().addAllRewards(rewardItems));
		}
		return rewardItems;
	}

	public static List<RewardInfo> addResources(Player player, int[][] rewards, OpType opType) {
		return addResources(player, rewards, opType, false);
	}

	public static List<RewardInfo> addGoods(Player player, List<Goods> goods, OpType opType) {
		if (goods == null || goods.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		List<RewardInfo> ret = new ArrayList<>();
		for (Goods g : goods) {
			ret.addAll(addResources(player, g.getId(), g.getCount(), opType));
		}
		return ret;
	}

	public static List<RewardInfo> addResources(Player player, int[][] rewards, OpType opType, boolean notify) {
		if (rewards != null && rewards.length > 0) {
			List<RewardInfo> rewardItems = new ArrayList<>();
			for (int i = 0; i < rewards.length; i++) {
				for (int j = 0; j < rewards[i].length; j += 2) {
					List<RewardInfo> rewardItem = addResources(player, rewards[i][j], rewards[i][j + 1], opType, false);
					rewardItems.addAll(rewardItem);
				}
			}
			if (notify) {
				player.getGameClient().sendProtocol(RewardMsg.RewardPush_55000501.newBuilder().addAllRewards(rewardItems));
			}
			return rewardItems;
		}
		return Collections.EMPTY_LIST;
	}

	public static List<RewardInfo> addResources(Player player, int[] rewards, OpType opType) {

		if (rewards == null || rewards.length == 0) {
			return Collections.EMPTY_LIST;
		}
		if (rewards.length > 2) {
			List<RewardInfo> ret = new ArrayList<>();
			for (int i = 0; i < rewards.length - 1; i += 2) {
				ret.addAll(addResources(player, rewards[i], rewards[i + 1], opType));
			}
			return ret;
		}
		return addResources(player, rewards[0], rewards[1], opType);
	}

	/**
	 * 一次性增加多个奖励，增加完奖励后推送给客户端一次。
	 * @param player
	 * @param rewards
	 * @return
	 */
	public static List<RewardInfo> addResources(Player player, Collection<? extends Entry<Integer, Integer>> rewards, OpType opType) {
		if (rewards != null && rewards.size() > 0) {
			List<RewardInfo> rewardItems = new ArrayList<>();
			for (Entry<Integer, Integer> pair : rewards) {
				List<RewardInfo> rewardItem = addResources(player, pair.getKey(), pair.getValue(), opType, false);
				rewardItems.addAll(rewardItem);
			}
			PlayerHelper.sendProtocol(player.getPlayerId(), RewardMsg.RewardPush_55000501.newBuilder().addAllRewards(rewardItems));
			return rewardItems;
		}
		return Collections.EMPTY_LIST;
	}

	public static List<RewardInfo> addResources(Player player, Map<Integer, Integer> map, OpType opType) {
		if (map == null || map.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		return addResources(player, map.entrySet(), opType);
	}

	/**
	 * 主动给玩家发送消息
	 * @param playerId
	 * @param message
	 */
	public static void sendProtocol(long playerId, Object message) {
		GameClient gameClientByPlayer = GameClientManager.getInstance().getGameClientByPlayer(playerId);
		if (gameClientByPlayer != null) {
			gameClientByPlayer.sendProtocol(message);
		}
	}

	/** 
	 * 给玩家发消息，带错误码
	 * @param playerId
	 * @param message 消息
	 * @param errorCode 错误码
	 */
	public static void sendProtocol(long playerId, Object message, int errorCode) {
		GameClient gameClientByPlayer = GameClientManager.getInstance().getGameClientByPlayer(playerId);
		if (gameClientByPlayer != null) {
			gameClientByPlayer.sendProtocol(message, errorCode);
		}
	}

	/** 
	 * 给玩家发默认的错误消息，带错误码
	 * @param playerId
	 * @param errorCode 	    错误码
	 */
	public static void sendErrorProtocol(long playerId, int errorCode) {
		GameClient gameClientByPlayer = GameClientManager.getInstance().getGameClientByPlayer(playerId);
		if (gameClientByPlayer != null) {
			gameClientByPlayer.sendProtocol(PlayerErrorPush_01000099.getDefaultInstance(), errorCode);
		}
	}

	/**
	 * 给某玩家发送一个待处理的消息
	 * @param playerId
	 * @param message
	 * @param discardWhenOffline true，如果玩家不在线，就丢弃消息。 false，玩家离线也需要处理，一般是记录下来上线处理
	 * @return Future,如果发到别的服务器处理，null，不用后续处理。
	 */
	@Deprecated
	public static Future<GamePlayerResponse_7d000016> sendRemotePlayer(long playerId, Object message, boolean discardWhenOffline) {
		if (!PlayerManager.getInstance().isOnline(playerId) && discardWhenOffline) {
			return null;
		}
		if (PlayerManager.getInstance().isOnline(playerId)) {
			// 发给所在服务器处理。
			String serverId = PlayerManager.getInstance().getServerId(playerId);
			if (StringUtils.isEmpty(serverId)) {
				serverId = ServerContext.getInstance().getServerId();
			}
			com.google.protobuf.Message m = null;
			if (message instanceof com.google.protobuf.Message) {
				m = (com.google.protobuf.Message) message;
			} else if (message instanceof Builder) {
				m = (com.google.protobuf.Message) ((Builder) message).build();
			} else {
				throw new IllegalArgumentException("not support message ：" + message);
			}

			int msgId = PbProtocol.getInstance().getMsgId(message.getClass().getSimpleName());
			GamePlayerRequest_7d000015 gamePlayerRequest_7d000015 = GamePlayerRequest_7d000015.newBuilder()
					.setPlayerId(playerId)
					.setId(msgId)
					.setData(m.toByteString())
					.build();
			Future<GamePlayerResponse_7d000016> requestRemoteServer = VxHolder.requestRemoteServer(serverId, gamePlayerRequest_7d000015);
			requestRemoteServer.onSuccess(resp -> {
				int errorCode = resp.getErrorCode();
				if (errorCode == ErrorMsgEnum.not_online.getId()) {
					PlayerManager.getInstance().resetOnline(playerId);
				}
			}).onFailure(e -> {
				log.error("sendRemotePlayer to server: " + playerId, e);
			});
			return requestRemoteServer;

		} else {
			// TODO 当前server保存任务到db,加分布式锁。
			return null;
		}
	}

	/**
	 * 给某在线玩家推送一个消息，如果玩家不在线，可以丢弃消息。
	 * 玩家不一定在某个服务器。 
	 * @param playerId 	    玩家id
	 * @param message 	    消息
	 */
	public static void sendOnlinePlayer(long playerId, Object message) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		// 在当前服务器
		if (player != null) {
			player.getGameClient().sendProtocol(message);
			return;
		}

		// 发给所在服务器处理。
		String serverId = PlayerManager.getInstance().getServerId(playerId);
		if (StringUtils.isEmpty(serverId)) {
			// 玩家不在线
			return;
		}
		com.google.protobuf.Message m = null;
		if (message instanceof com.google.protobuf.Message) {
			m = (com.google.protobuf.Message) message;
		} else if (message instanceof Builder) {
			m = (com.google.protobuf.Message) ((Builder) message).build();
		} else {
			throw new IllegalArgumentException("not support message ：" + message);
		}

		sendToRemotePlayer(playerId, serverId, m);

	}

	/**
	 * 将消息发送给指定服务器的玩家，不需要返回消息
	 * @param playerId
	 *            目标玩家id
	 * @param message
	 * @param serverId
	 *            目标服务器id
	 */
	public static void sendToRemotePlayer(long playerId, String serverId, com.google.protobuf.Message message) {
		int msgId = PbProtocol.getInstance().getMsgId(message.getClass().getSimpleName());
		GamePlayerPush_7d000100.Builder builder = GamePlayerPush_7d000100.newBuilder();

		builder.setData(message.toByteString());
		builder.setId(msgId);
		builder.setPlayerId(playerId);

		VxHolder.sendRemoteServer(serverId, builder.build());

	}

	/**
	 * 将消息广播给跨服玩家
	 * @param message
	 * @param playerIds
	 * @param serverIds
	 */
	public static void sendToRemotePlayers(com.google.protobuf.Message message, List<Long> playerIds, List<String> serverIds) {
		int msgId = PbProtocol.getInstance().getMsgId(message.getClass().getSimpleName());

		for (int i = 0; i < playerIds.size(); i++) {
			GamePlayerPush_7d000100.Builder builder = GamePlayerPush_7d000100.newBuilder();

			builder.setData(message.toByteString());
			builder.setId(msgId);
			builder.setPlayerId(playerIds.get(i));
			VxHolder.requestRemoteServer(serverIds.get(i), builder.build());
		}
	}

	/**
	 * 是否满足所有条件
	 * @param player
	 * @param conditions 待检查条件，  {@link ConditionConfig#ID}
	 * @return
	 */
	public static boolean checkCondition(Player player, List<Integer> conditions) {

		return checkCondition(player, conditions, false);
	}

	/** 
	 * 是否满足所有条件
	 * @param player
	 * @param conditions 待检查条件，  {@link ConditionConfig#ID}
	 * @return
	 */
	public static boolean checkCondition(Player player, int[] conditions) {

		return checkCondition(player, GameUtil.transform1(conditions));
	}

	/**
	 * 是否满足所有条件
	 * @param player
	 * @param conditions 待检查条件，  {@link ConditionConfig#ID}
	 * @param or 是否满足任意条件即可
	 *            
	 * @return true,如果满足任意条件
	 */
	public static boolean checkCondition(Player player, List<Integer> conditions, boolean or) {

		if (conditions.isEmpty()) {
			return true;
		}
		if (or) {
			for (Integer e : conditions) {
				if (checkCondition(player, e)) {
					return true;
				}
			}
		} else {
			for (Integer e : conditions) {
				if (!checkCondition(player, e)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 基础的条件检查
	 * @param player
	 * @param conditions 待检查条件，  {@link ConditionConfig#ID}
	 * @return
	 */
	public static boolean checkCondition(Player player, int condition) {
		if (condition == 0) { // 一般认为没有配置条件，默认符合。
			return true;
		}
		ConditionConfig conditionConfig = ConditionManager.instance().get(condition);
		int count = conditionConfig.numParam;
		return getConditionCount(player, condition) >= count;
	}

	/**
	 * 获取某条件的计数，一般是根据当前数据直接可以获得的，或者是累计计数等，不需要额外条件的。
	 * @param player
	 * @param condition 待检查条件，  {@link ConditionConfig#ID}
	 * @return
	 */
	public static int getConditionCount(Player player, int condition) {
		if (condition == 0) {
			return 0;
		}
		ConditionConfig conditionConfig = ConditionManager.instance().get(condition);

		ConditionTypeEnum type = ConditionTypeEnum.get(conditionConfig.type);
		if (type.countType == 0) {
			throw new IllegalArgumentException(condition + " 计数类型是0，只能从任务处获取数据");
		}
		int id = conditionConfig.idParam;
		int[] extParam = conditionConfig.extParam;

		if (type.countType == 2) {
			// 累计计数带有额外参数的：
			if (type == ConditionTypeEnum.EarnHeroCumulation) {
				return player.getQuestModule().getCumulativeCount(type, extParam);
			}
			// 累计计数不带额外参数直接获取的
			return player.getQuestModule().getCumulativeCount(type);
		}
		if (type.countType == 1) {
			switch (type) {
			// 直接根据当前数据获取的：
			case PlayerLevel: {
				return player.getLevel();
			}
			case ChapterFinish: {
				ChapterModule chapterModule = player.getModule(ChapterModule.class);
				return chapterModule.isBattlePass(id) ? 1 : 0;
			}
			case CultivatesImmortals: {
				return player.getDevelopModule().getHeavenlyDaoLevel();
			}
			default: {
				throw new IllegalArgumentException(" not suport countType1 condition  " + type);
			}
			}
		}
		throw new IllegalArgumentException(" not suport condition  " + type);
	}

	public static boolean operator(int value, int configValue, int operator) {
		switch (operator) {
		case 1:
			return value > configValue;
		case 2:
			return value >= configValue;
		case 3:
			return value == configValue;
		case 4:
			return value <= configValue;
		case 5:
			return value < configValue;
		case 6:
			return value != configValue;
		default:
			return false;
		}
	}

	/**
	 * 重连
	 * @param newGameClient
	 * @param reconnect 客户端传递的参数，是否是重连
	 * @param playerId
	 * @return  是否重连了
	 */
	public static boolean reconnect(GameClient newGameClient, boolean reconnect, long playerId, Account account) {
		if (playerId == 0) {
			return false;
		}
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (player == null) {
			return false;
		}
		GameClient oldGameClient = GameClientManager.getInstance().getGameClientByPlayer(playerId);
		if (oldGameClient == null) {
			log.warn("reconnect warn, oldGameClient is null playerId:" + playerId);
			newGameClient.setPlayerId(playerId);
		} else {
			if (oldGameClient != newGameClient) {
				// 可能不同设备登录同一账号,应该退出老的GameClient
				if (reconnect) {
					newGameClient.copy(oldGameClient);
				} else {
					newGameClient.copyClintLoign(oldGameClient);
				}
				GameClientManager.getInstance().removeGameClient(oldGameClient, LogoutType.Reconnect);
			}
		}
		GameClientManager.getInstance().addGameClientSession(newGameClient);
		GameClientManager.getInstance().addGameClientPlayer(newGameClient);

		player.setAccount(account);
		player.setGameClient((GameClient) newGameClient);
		PlayerHelper.refresh(player);
		player.handleEvent(EventTypeEnum.Reconnect);
		PlayerLoginResponse_01000002.Builder resp2 = PlayerLoginResponse_01000002.newBuilder();
		resp2.setReconnect(reconnect);
		resp2.setInfo(PbBuilder.buildPlayerInfo(player));
		resp2.setTime(System.currentTimeMillis() + "");
		newGameClient.sendProtocol(resp2);
		return true;
	}

	public static Future<Player> startLoadPlayerFromDb(GameClient gameClient, PlayerData dbPlayer, Account account) {
		Player player = createPlayer(dbPlayer, account, gameClient);
		return selectPlayerModuleData(player).compose(PlayerHelper::initPlayerData);
	}

	/** 
	 * 只是查询构造出来player对象，不做任何初始化操作
	 * 注意这里将Player加入到PlayerManager中，某些模块初始化会用到
	 * 如果查询出来Player不在使用，需要手动移除 @link PlayerHelper#clearPlayer(long playerId) 
	 * @param playerData
	 * @return
	 */
	public static Future<Player> loadPlayerFromDb(PlayerData playerData) {
		GameClient gameClient = new GameClient(null); // 临时的
		gameClient.setContext((ContextInternal) VxHolder.vertx.getOrCreateContext());
		Player player = new Player(playerData);
		player.setGameClient(gameClient);
		player.setOnline(false);
		PlayerManager.getInstance().initAdd(player);
		return selectPlayerModuleData(player);
	}

	/** 
	 * 从数据库中查询并构造出player对象，一般在操作玩家离线数据时使用
	 * 确保在玩家不在线的时候调用，包括当前服务器和其他服务器
	 * 例如gm修改资源，充值补单等等，正常游戏中不要使用。 
	 * 一般查询出来，修改后，保存player到数据库，之后手动移除 @link PlayerHelper#clearPlayer(long playerId)
	 * @param playerId
	 * @return
	 */
	public static Future<Player> loadPlayerFromDb(long playerId) {

		return getPlayerDistributedLock(playerId).compose(rr -> {
			return DAO.<PlayerData>execute(PlayerDataMapper.class, MapperConstant.selectByPrimaryKey, playerId).compose(r -> {
				if (r == null) {
					return Future.failedFuture(ErrorMsgEnum.player_not_exist.getId() + "");
				}
				return loadPlayerFromDb(r);
			});
		});
	}

	/** 
	 * 构造Player实例
	 * @param playerData
	 * @param account
	 * @param client
	 * @return
	 */
	public static Player createPlayer(PlayerData playerData, Account account, GameClient client) {
		client.setPlayerId(playerData.getPlayerId());
		GameClientManager.getInstance().addGameClientPlayer((GameClient) client);
		GameClientManager.getInstance().addGameClientSession((GameClient) client);

		Player player = new Player(playerData);
		player.setGameClient((GameClient) client);
		player.setAccount(account);

		PlayerManager.getInstance().initAdd(player);
		return player;
	}


	public static Future<Player> initPlayerData(Player player) {

		if (player.getData().isNew()) {
			// 初始的资源
			PlayerHelper.addResources(player, GlobalConst.initItems, OpType.Init);
			PlayerHelper.initNewPlayerData(player);
		}
		PlayerHelper.initAfterLogin(player);
		return Future.succeededFuture(player);
	}

	/**
	 * 获取某个玩家id的分布式锁
	 * @param playerId
	 * @return
	 */
	public static RFuture<Boolean> trySetServerId(long playerId) {
		RFuture<Boolean> playerLockFuture = RedisUtil.trySetAsync(CacheType.PLAYER_SERVER_ID.key(playerId),
				ServerContext.getInstance().getServerId(), 5, TimeUnit.MINUTES);
		return playerLockFuture;
	}

	public static RFuture<Void> setServerId(long playerId) {
		RFuture<Void> playerLockFuture = RedisUtil.setAsync(CacheType.PLAYER_SERVER_ID.key(playerId),
				ServerContext.getInstance().getServerId(), 5, TimeUnit.MINUTES);
		return playerLockFuture;
	}

	public static Future<Void> saveSimplePlayerToRedis(Player player) {
		String key = CacheType.PLAYER_SIMPLE.key(player.getData().getPlayerId());
		return RedisLocalCache.getInstance().putAsync(key, new SimplePlayer(player));
	}

	/** 
	 * 同步保存，一般只在初始化时使用
	 * @param player
	 */
	public static void saveSimplePlayerToRedisSync(Player player) {
		String key = CacheType.PLAYER_SIMPLE.key(player.getData().getPlayerId());
		RedisLocalCache.getInstance().put(key, new SimplePlayer(player));
	}

	public static Future<Player> saveSimplePlayer(Player player) {
		Future<Void> future = saveSimplePlayerToRedis(player);
		return future.map(player);
	}

	/** 
	 * 创建新玩家，保存到数据库
	 * @param player
	 * @return
	 */
	public static Future<Player> savePlayerToDb(Player player) {
		PlayerData data = player.getData();
		data.beforeSave();
		data.setModules(JsonUtil.toJsonStringWithType(player.getModules()));

		Promise<Player> promise = Promise.promise();
		DAO.execute(PlayerDataMapper.class, MapperConstant.insert, data)
				.onSuccess(r -> promise.complete(player))
				.onFailure(t -> promise.fail(t));
		return promise.future();
	}

	/** 
	 * 获取某玩家分布式锁
	 * @param playerId
	 * @return
	 */
	public static Future<Void> getPlayerDistributedLock(long playerId) {
		// 方便测试，强制关闭服务器后快速登陆账号使用
		if (!ServerContext.getInstance().getRunMode().isProduction()) {
			return Future.succeededFuture();
		}
		return VxHolder.toVertxFuture(PlayerHelper.trySetServerId(playerId)).compose(locked -> {
			if (!locked) {
				return Future.failedFuture(ErrorMsgEnum.player_lock.getId() + "");
			}
			return Future.succeededFuture();
		});
	}

	/** 
	 * 从数据库中查询玩家除了PlayerData表的其他表数据
	 * 独立的表存储的玩家模块数据
	 * @param player
	 * @return 
	 */
	public static Future<Player> selectPlayerModuleData(Player player) {
		List<DbTask> dbTasks = initDbTasks(player);
		return DAO.execute(dbTasks).compose(r -> initPlayerModuleFromDb(player, r));
	}

	/** 
	 * 初始化玩家数据库查询任务
	 * @param player
	 * @return
	 */
	public static List<DbTask> initDbTasks(Player player) {
		List<DbTask> dbTasks = new ArrayList<>();
		for (BasePlayerModule module : player.getModuleSorted()) {
			if (!GameServer.getInstance().isSinglePlayerTable() || module.alwaysStoreDataInStandaloneTable()) {
				module.initDbTasks(dbTasks);
			}
		}
		return dbTasks;
	}

	public static Future<Player> initPlayerModuleFromDb(Player player, List<Object> list) {

		ListIterator<?> listIterator = list.listIterator();
		if (GameServer.getInstance().isSinglePlayerTable()) {
			for (BasePlayerModule module : player.getModuleSorted()) {
				if (module.alwaysStoreDataInStandaloneTable()) {
					module.loadFromDb(listIterator);
				} else {
					module.initFromDbAfter();
				}
			}
		} else {
			for (BasePlayerModule module : player.getModuleSorted()) {
				module.loadFromDb(listIterator);
			}
		}
		return Future.succeededFuture(player);
	}

	/**
	 * 主动给客户端推送错误
	 * @param playerId
	 * @param code 错误码
	 */
	public static void pushError(long playerId, int code) {
		sendProtocol(playerId, PlayerErrorPush_01000099.newBuilder(), code);
	}

	/** 
	 * 执行关于某个玩家的任务
	 * @param playerId
	 * @param action
	 */
	public static void addTask(long playerId, Handler<Void> action) {
		GameClient gameClient = GameClientManager.getInstance().getGameClientByPlayer(playerId);
		if (gameClient != null) {
			gameClient.getContext().runOnContext(action);
		}
	}

	public static void addTask(Player player, Handler<Void> action) {
		player.getGameClient().getContext().runOnContext(action);
	}

	/** 
	 * 退出，数据存库
	 * @param playerId
	 * @return 
	 */
	public static Future<Object> logout(long playerId) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (player == null) {
			return Future.succeededFuture();
		}
		player.setOnline(false);
		PlayerData data = player.getData();
		data.setOfflineTime(System.currentTimeMillis());
		data.setGameTime(data.getGameTime() + (int) ((data.getOfflineTime() - DateUtil.getDate(data.getLoginDate()).getTime()) / 1000));

		return saveClientCache(playerId).onSuccess(r -> {
			clearPlayer(playerId);
			GameLogger.logout(player);
			PushService.getInstance().delPlayerTags(playerId);
		}).compose(v -> {
			RFuture<Boolean> deleteAsync = RedisUtil.deleteAsync(CacheType.PLAYER_SERVER_ID.key(playerId));
			return Future.fromCompletionStage(deleteAsync);
		}).compose(v -> {
			// 保存SimplePlayer 到redis。
			return PlayerHelper.saveSimplePlayerToRedis(player);
		}).mapEmpty().otherwise(e -> {
			log.error("Error during logout cache process for playerId: " + playerId, e);
			return null;
		});
	}

	/** 
	 * 清除玩家缓存数据
	 */
	public static void clearPlayer(long playerId) {
		Player player = PlayerManager.getInstance().deletePlayer(playerId);
		if (player == null) {
			return;
		}
		player.cancelAllTimer();
	}

	/**
	 * 保存缓存数据到数据库
	 * 这个方法最好不要放到Player里边，万一Player没有保证唯一，以PlayerManger里面的Player为准。不会覆盖数据
	 * @return 
	 */
	public static Future<List<Object>> saveClientCache(long playerId) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (player == null) {
			return Future.succeededFuture();
		}
		if (player.isActive()) {
			PlayerData data = player.getData();
			if (GameServer.getInstance().isSinglePlayerTable()) {
				data.beforeSave();
				data.setModules(JsonUtil.toJsonStringWithType(player.getModules()));
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
				dbTasks.add(new DbTask(dbEntity.getMapperClass(), MapperConstant.updateByPrimaryKeySelective, dbEntity));
			}
			if (!dbTasks.isEmpty()) {
				Future<List<Object>> updateFuture = DAO.execute(dbTasks);
				return updateFuture;
			}
		}
		return Future.succeededFuture();
	}

	/** 
	 * 通用的升级逻辑
	 * @param expId 经验id
	 * @param id 	具体升级的配置id，比如仙友id
	 * @param curLevel 当前等级
	 * @param curExp	当前经验
	 * @param count		增加的经验数量
	 * @return  新的经验、等级
	 */
	public static int[] addExp(int expId, int id, int curLevel, int curExp, int count) {
		int newLevel = curLevel;
		int newExp = curExp + count;
		ExpConfig expConfig = getExpConfig(expId, curLevel, id);
		ExpConfig nextExpConfig = getExpConfig(expId, curLevel + 1, id);
		while (expConfig != null && newExp >= expConfig.experience && nextExpConfig != null) {
			newExp -= expConfig.experience;
			newLevel++;
			expConfig = getExpConfig(expId, newLevel, id);
			nextExpConfig = getExpConfig(expId, newLevel, id);
		}
		// 不能升了，设置经验为最大
		if (expConfig != null && newExp > expConfig.experience) {
			newExp = expConfig.experience;
		}
		return new int[] { newExp, newLevel };
	}

	/** 
	 * 手动升级，对于一个玩家只有一种等级的，例如玩家等级，vip等级 等等
	 * @param expId 经验id
	 * @param subId	子id，如果同一类型下有多个配置，用这个区分。
	 * @param curLevel 当前等级
	 * @param curExp	当前经验
	 * @return
	 */
	public static int[] levelUp(Player player, int expId, int subId) {
		Asset expAsset = Asset.get(expId);
		if (expAsset.Type != 2) {
			throw new IllegalArgumentException("不是经验id");
		}
		int curExp = (int) player.getCurrencyModule().get(expAsset);
		int curLevel = player.getLevel(expAsset);
		ExpConfig expConfig = getExpConfig(expId, curLevel, subId);
		ExpConfig nextExpConfig = getExpConfig(expId, curLevel + 1, subId);
		if (expConfig != null && curExp >= expConfig.experience && nextExpConfig != null) {
			curExp -= expConfig.experience;
			curLevel++;
			player.getPlayerModule().getExpLevelMap().add(expId);
			player.getCurrencyModule().setCount(expId, curExp);
		}
		return new int[] { curExp, curLevel };
	}

	/** 
	 * 获取某个升级配置。 
	 * @param id  经验id
	 * @param level 等级
	 * @param subId	子id，如果同一类型下有多个配置，用这个区分。
	 * @return
	 */
	public static ExpConfig getExpConfig(int id, int level, int subId) {
		if (id == Asset.playerExp.ID) {
			return UserUpgradeManager.instance().getNullable(level);
		} else if (id == Asset.FundPass.ID) {
			return FundPassUpgradeManager.instance().getUIExpTypeLv(id, level);
		} else if (id == Asset.BrawlPoint.ID) {
			return FundPassUpgradeManager.instance().getUIExpTypeLv(id, level);
		} else if (id == Asset.QiankunMirrorExp.ID) {
			return QiankunMirrorLvManager.instance().getNullable(level);
		} else if (id == Asset.Favorability.ID) {
			return FairyFriendFavorabilityManager.instance().getUIFairyListIDLV(subId, level);
		} else if (id == Asset.VIPExp.ID) {
			return VIPManager.instance().getNullable(level);
		} else if (id == Asset.CatalogPoints.ID) {
			return HeroBandBookManager.instance().getNullable(level);
		}
		throw new IllegalArgumentException("没有实现的经验id： " + id);
	}

	/** 
	 * 获取某玩家虚拟的服务器id
	 * @param playerId
	 * @return
	 */
	public static String getServerId(long playerId) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (player != null) {
			return player.getServerId();
		}
		SimplePlayer simplePlayer = RedisLocalCache.getInstance().get(CacheType.PLAYER_SIMPLE.key(playerId));
		return simplePlayer.getServerId();
	}

	/** 
	 * 异步获取某玩家虚拟的服务器id
	 * @param playerId
	 * @return
	 */
	public static Future<String> getServerIdAsync(long playerId) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (player != null) {
			return Future.succeededFuture(player.getServerId());
		}
		Future<SimplePlayer> simplePlayer = RedisLocalCache.getInstance().getAsync(CacheType.PLAYER_SIMPLE.key(playerId));
		return simplePlayer.map(SimplePlayer::getServerId);
	}

	/** 
	 * 从数据库中载入所有玩家数据，逐个执行修正逻辑，发生异常继续处理，不中断。 
	 * @param function  修正方法，返回true为数据修正了，需要保存，false为数据没有修改，不需要保存
	 */
	public static void loadAndProcessPlayers(Function<Player, Boolean> function) {
		PlayerDataMapper mapper = SpringContextLoader.getContext().getBean(PlayerDataMapper.class);
		BatchQueryUtil.processBatch((offset, limit) -> mapper.getBatch(offset, limit), playerData -> {
			try {
				Future<Player> playerFromDb = PlayerHelper.loadPlayerFromDb(playerData);
				Player player = AsyncUtils.await(playerFromDb);
				modifyPlayerOffline(function, player);
			} catch (Exception e) {
				LoggerType.Stdout.logger.error("Failed to process player: " + playerData.getPlayerId() + ", error: " + e.getMessage());
			}
		});
	}

	/** 
	 * 
	 * 修改玩家数据, 允许在服务器运行时修改
	 * 如果玩家在线，直接修改内存数据，否则从数据库中加载数据修改
	 * 如果玩家在其他服务器，先抛出异常，后续完善
	 * 
	 * @param playerId
	 * @param function 修改数据的方法，结果true表示数据修改了， false表示数据没有修改
	 */
	public static void modifyPlayer(long playerId, Function<Player, Boolean> function) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		boolean online = player != null;
		if (player == null) {
			// 先不处理在其他服务器在线的情况， 后续再处理，如果发生先失败
			Future<Player> playerFromDb = PlayerHelper.loadPlayerFromDb(playerId);
			// 简单起见，同步获取玩家
			player = AsyncUtils.await(playerFromDb);
		}
		Player modify = player;
		if (online) {
			GameClient gameClient = player.getGameClient();
			if (gameClient == null) {
				log.error("modifyPlayer gameClient is null, playerId: " + playerId);
				return;
			}
			gameClient.getContext().runOnContext(v -> {
				function.apply(modify);
			});
		} else {
			modifyPlayerOffline(function, modify);
		}
	}

	private static void modifyPlayerOffline(Function<Player, Boolean> function, Player player) {
		Boolean apply = function.apply(player);
		if (apply != null && apply) {
			log.info("修改离线玩家数据，准备保存: " + player.getPlayerId());
			Future<List<Object>> saveClientCache = PlayerHelper.saveClientCache(player.getPlayerId());
			AsyncUtils.await(saveClientCache);// 简单起见，同步保存
		}
		// 修改完玩家数据后，需要从缓存中清除数据
		PlayerHelper.clearPlayer(player.getPlayerId());
		RedisUtil.deleteAsync(CacheType.PLAYER_SERVER_ID.key(player.getPlayerId()));
	}

	/** 
	 * 按照名字或者id查找玩家
	 * @param playerName
	 * @param playerId
	 * @return
	 */
	public static Future<SimplePlayer> seachPlayer(String playerName, long playerId) {
		if (playerId > 0) {
			return RedisLocalCache.getInstance().getAsync(CacheType.PLAYER_SIMPLE.key(playerId));
		} else if (playerName != null) {
			return PlayerNameManager.getInstance().getPlayerId(playerName).compose(r -> {
				return RedisLocalCache.getInstance().getAsync(CacheType.PLAYER_SIMPLE.key(r));
			});
		}
		return Future.failedFuture("没有传入playerId或者playerName");
	}

	/**
	 * https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/sec-center/sec-check/msgSecCheck.html#HTTPS-%E8%B0%83%E7%94%A8
	 * 检查玩家 的文本消息是否非法
	 * @param player
	 * @param msg
	 * @return true 可以发送， false 不可以发送
	 */
	public static Future<Boolean> checkContextData(Player player, String msg) {
		Promise<Boolean> promise = Promise.promise();
		if (Config.wechatAccessToken == null || Config.DISABLE_WECHAT_CONTENT_CHECK) {
			return Future.succeededFuture(true);
		}
		String url = String.format("https://api.weixin.qq.com/wxa/msg_sec_check?access_token=%s", Config.wechatAccessToken);
		/*{
			"content": "毛泽东你好",
				"version":2,
				"scene":2,
				"openid": "odN8m7fO2xG_3qneoOMxeWeOcmwQ"
		
		}*/
		Map<String, Object> data = new HashMap<>();
		data.put("content", msg);
		data.put("version", 2);
		data.put("scene", 2);
		data.put("openid", player.getOpenId());
		VxHolder.post(url, response -> {
			if (response != null) {
				int errcode = response.getInteger("errcode");
				if (errcode == 0) {
					JsonObject dataResult = response.getJsonObject("result");
//							label	number	命中标签枚举值，100 正常；10001 广告；20001 时政；20002 色情；20003 辱骂；20006 违法犯罪；20008 欺诈；20012 低俗；20013 版权；21000 其他
					if (dataResult.getInteger("label") == 100) {
						promise.complete(true);
						return;
					}
				}
			}
			promise.complete(false);
		}, err -> {
			err.printStackTrace();
			promise.fail(err);
		}, data);
		return promise.future();
	}
}
