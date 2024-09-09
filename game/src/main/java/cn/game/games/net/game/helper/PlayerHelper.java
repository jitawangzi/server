package cn.game.games.net.game.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.GoodsModule;
import cn.game.games.core.SimplePlayer;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.GameServer;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.db.DbTask;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.account.Account;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.util.BIHelper;
import cn.game.games.util.DAO;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.config.ConsumeConfig;
import cn.game.protocol.generated.config.ExpConfig;
import cn.game.protocol.generated.config.GameCommandConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.RandomGivenConfig;
import cn.game.protocol.generated.config.RandomGroupConfig;
import cn.game.protocol.generated.config.RewardConfig;
import cn.game.protocol.generated.config.versionConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.ConditionManager;
import cn.game.protocol.generated.manager.ConsumeManager;
import cn.game.protocol.generated.manager.FairyFriendFavorabilityManager;
import cn.game.protocol.generated.manager.FundPassUpgradeManager;
import cn.game.protocol.generated.manager.GameCommandManager;
import cn.game.protocol.generated.manager.QiankunMirrorLvManager;
import cn.game.protocol.generated.manager.RandomGivenManager;
import cn.game.protocol.generated.manager.RandomGroupManager;
import cn.game.protocol.generated.manager.RewardManager;
import cn.game.protocol.generated.manager.UserUpgradeManager;
import cn.game.protocol.generated.manager.VIPManager;
import cn.game.protocol.generated.manager.versionManager;
import cn.game.protocol.manual.ErrorMsgEnum;
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
import cn.game.util.Pair;
import cn.game.util.RedisUtil;
import cn.game.util.Rnd;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class PlayerHelper {

	private static final Logger log = LoggerFactory.getLogger(PlayerHelper.class);
//	private static final Logger resourceDelLog = LoggerFactory.getLogger("resourceDelLog");
//	private static final Logger levellog = LoggerFactory.getLogger("levelLog");
//	private static final Logger loginlog = LoggerFactory.getLogger("loginLog");

	public static boolean isEnough(Player player, List<? extends Entry<Integer, Integer>> list) {

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

	public static boolean isEnoughOld(Player player, int[][] list) {

		if (list == null || list.length == 0) {
			return true;
		}
		for (int i = 0; i < list.length; i++) {
			if (!isEnough(player, list[i][0], list[i][1])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * or的关系判断,二维数组中有一个满足就可以
	 * @param player
	 * @param list
	 * @return
	 */
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
	 * @param id
	 * @param value
	 * @param mode 数值或当前百分比
	 * @param consumeType
	 * @return
	 */
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

	public static boolean delResources(Player player, int id, int value, OpType consumeType) {
		return delResources(player, id, value, consumeType, true);
	}

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
	 * @param id
	 * @param value
	 * @param consumeType
	 * @param notify 是否通知客户端  如果直接调用该方法不涉及合并问题则传true, 如果涉及合并则传false，合并后需要推送协议SpendPush_55001501
	 * @return
	 */
	public static boolean delResources(Player player, int id, int value, OpType consumeType, boolean notify) {

		if (value <= 0) {
			return true;
		}
		GoodsModule goodsModule = player.getGoodsModule(id);
		boolean ret = goodsModule.del(id, value, consumeType);

		if (ret) {
			player.handleEvent(EventTypeEnum.CostItem, id, value);
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
	 * @param player
	 * @param list
	 * @param consumeType
	 * @return
	 */
	public static boolean delResources(Player player, List<? extends Entry<Integer, Integer>> list, OpType consumeType) {

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
			player.getGameClient().sendProtocol(spendPush.build());
			return true;
		}
		return false;
	}

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
			player.getGameClient().sendProtocol(spendPush.build());
			return true;
		}
		return false;
	}

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
			player.getGameClient().sendProtocol(spendPush.build());
			return true;
		}
		return false;
	}

	/**
	 * 二维数组用或的关系扣东西
	 * @param playerId
	 * @param list
	 * @param consumeType
	 * @return
	 */
	/*	public static boolean delResources(Player player, int[][] list, ResourceConsumeEnum consumeType) {

			if (list == null || list.length == 0) {
				return true;
			}
			if (isEnough(player, list)) {
				SpendPush_55001501.Builder spendPush = SpendPush_55001501.newBuilder();
				for (int i = 0; i < list.length; i++) {
					delResources(player, list[i][0], list[i][1], consumeType, false);
					spendPush.addSpend(PbBuilder.buildGoodsInfo(list[i][0], list[i][1]));
				}
				player.getGameClient().sendProtocol(spendPush.build());
				return true;
			}
			return false;
		}*/

	/**
	 * 根据奖励id，增加所有物品
	 * @param player
	 * @param randomRewardId
	 * @return
	 */
	public static List<RewardInfo> addReward(Player player, int randomRewardId, OpType opType) {
		RandomGivenConfig randomGivenConfig = RandomGivenManager.instance().get(randomRewardId);
		List<RewardInfo> resources = addResources(player, randomGivenConfig.MustGiven, opType, false);
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
				resources.addAll(addResources(player, groupConfig.AssetID, groupConfig.Several, opType));
			}
		}
		for (int i = 0; i < randomGivenConfig.FixedNumRandomDrop.length; i++) {
			int group = randomGivenConfig.FixedNumRandomDrop[i][0];
			int randomCount = randomGivenConfig.FixedNumRandomDrop[i][1];
			List<RandomGroupConfig> randomGroupIDList = RandomGroupManager.instance().getRandomGroupIDList(group);
			for (int j = 0; j < randomCount; j++) {
				RandomGroupConfig groupConfig = Rnd.randomWeighableElement(randomGroupIDList);
				resources.addAll(addResources(player, groupConfig.AssetID, groupConfig.Several, opType));
			}
		}
		return resources;
	}

	/**
	 * 只是随机出来具体的奖励，不加到玩家身上,较少用到
	 * @param player
	 * @param randomRewardId
	 * @return
	 */
	public static List<Goods> randomReward(Player player, int randomRewardId) {
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
				RandomGroupConfig groupConfig = Rnd.randomOne(randomGroupIDList);

				Goods goods = new Goods(groupConfig.AssetID, groupConfig.Several);
				ret.add(goods);
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
		}
		return ret;
	}

	/**
	 * 上线时刷新数据
	 * @param player
	 */
	public static void refresh(Player player) {
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
			PlayerHelper.saveSimplePlayerToRedis(player);
		});
		// 上线后生成自己的简单信息
//		try {
//			PlayerManager.getInstance().getAndLoadSimplePlayer(playerId);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		player.handleEvent(EventTypeEnum.LoginFinish);

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

		long nowTime = DateUtil.getStamp();

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
	 * @param rewards
	 * @return
	 */
	public static List<RewardInfo> addResources(Player player, List<Entry<Integer, Integer>> rewards, OpType opType) {
		List<RewardInfo> rewardItems = new ArrayList<>();
		if (rewards != null && rewards.size() > 0) {
			for (Entry<Integer, Integer> entry : rewards) {

				List<RewardInfo> rewardItem = addResources(player, entry.getKey(), entry.getValue(), opType, false);
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

		List<RewardInfo> ret = new ArrayList<>();
		for (Goods g : goods) {
			ret.addAll(addResources(player, g.getId(), g.getCount(), opType));
		}
		return ret;
	}

//	public static List<RewardInfo> addResources(long playerId, int[][] rewards, boolean notify) {
//		List<RewardInfo> rewardItems = new ArrayList<>();
//		if (rewards != null && rewards.length > 0) {
//			for (int i = 0; i < rewards.length; i++) {
//				for (int j = 0; j < rewards[i].length; j += 2) {
//					List<RewardInfo> rewardItem = addResources(playerId, rewards[i][j], rewards[i][j + 1], false);
//					rewardItems.addAll(rewardItem);
//				}
//			}
//			if (notify) {
//				PlayerHelper.sendProtocol(playerId, RewardMsg.RewardPush_55000501.newBuilder().addAllRewards(rewardItems));
//			}
//		}
//		return rewardItems;
//	}

	public static List<RewardInfo> addResources(Player player, int[][] rewards, OpType opType, boolean notify) {
		List<RewardInfo> rewardItems = new ArrayList<>();
		if (rewards != null && rewards.length > 0) {
			for (int i = 0; i < rewards.length; i++) {
				for (int j = 0; j < rewards[i].length; j += 2) {
					List<RewardInfo> rewardItem = addResources(player, rewards[i][j], rewards[i][j + 1], opType, false);
					rewardItems.addAll(rewardItem);
				}
			}
			if (notify) {
				player.getGameClient().sendProtocol(RewardMsg.RewardPush_55000501.newBuilder().addAllRewards(rewardItems));
			}
		}
		return rewardItems;
	}

	public static List<RewardInfo> addResources(Player player, int[] rewards, OpType opType) {

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
	public static List<RewardInfo> addResources(Player player, Set<Pair<Integer, Integer>> rewards, OpType opType) {
		List<RewardInfo> rewardItems = new ArrayList<>();
		if (rewards != null && rewards.size() > 0) {
			for (Pair<Integer, Integer> pair : rewards) {
				List<RewardInfo> rewardItem = addResources(player, pair.first, pair.second, opType, false);
				rewardItems.addAll(rewardItem);
			}
			PlayerHelper.sendProtocol(player.getPlayerId(), RewardMsg.RewardPush_55000501.newBuilder().addAllRewards(rewardItems));
		}
		return rewardItems;
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

	public static void sendProtocol(long playerId, Object message, int errorCode) {
		GameClient gameClientByPlayer = GameClientManager.getInstance().getGameClientByPlayer(playerId);
		if (gameClientByPlayer != null) {
			gameClientByPlayer.sendProtocol(message, errorCode);
		}
	}

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
	public static Future<io.vertx.core.eventbus.Message<GamePlayerResponse_7d000016>> sendRemotePlayer(long playerId, Object message,
			boolean discardWhenOffline) {
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
			GamePlayerRequest_7d000015 gamePlayerRequest_7d000015 = GamePlayerRequest_7d000015
					.newBuilder()
					.setPlayerId(playerId)
					.setId(msgId)
					.setData(m.toByteString())
					.build();
			Future<io.vertx.core.eventbus.Message<GamePlayerResponse_7d000016>> requestRemoteServer = VxHolder
					.requestRemoteServer(serverId, gamePlayerRequest_7d000015);
			requestRemoteServer.onSuccess(resp -> {
				int errorCode = resp.body().getErrorCode();
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
	 * @param playerId
	 * @param message
	 */
	public static void sendOnlinePlayer(long playerId, Object message) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (player != null) {
			player.getGameClient().sendProtocol(message);
			return;
		}

		// 发给所在服务器处理。
		String serverId = PlayerManager.getInstance().getServerId(playerId);
		if (StringUtils.isEmpty(serverId)) {
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
	 * 给某玩家发送消息，可能是跨服的玩家
	 * @param playerId
	 * @param message
	 * @param serverId
	 */
	public void sendProtcolCrossServer(long playerId, String serverId, com.google.protobuf.Message message) {
		if (StringUtils.isEmpty(serverId) || serverId.equals(ServerContext.getInstance().getServerId())) { // 本服务器玩家
			GameClient gameClient = GameClientManager.getInstance().getGameClientByPlayer(playerId);
			if (gameClient != null) {
				gameClient.sendProtocol(message);
			}

		} else {
			sendToRemotePlayer(playerId, serverId, message);
		}

	}

	/**
	 * 将消息发送给指定服务器的玩家
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

		VxHolder.sendToRemoteServer(serverId, PbProtocol.GamePlayerPush_7d000100, builder.build().toByteArray());

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
			VxHolder.sendToRemoteServer(serverIds.get(i), msgId, builder.build().toByteArray());
		}
	}

	/**
	 * 是否满足所有条件
	 * @param player
	 * @param conditions
	 * @return
	 */
	public static boolean checkCondition(Player player, List<Integer> conditions) {

		return checkCondition(player, conditions, false);
	}

	public static boolean checkCondition(Player player, int[] conditions) {

		return checkCondition(player, GameUtil.transform1(conditions));
	}

	/**
	 * @Description
	 * @param player
	 * @param conditions
	 * @param or
	 *            true,如果满足任意条件
	 * @return
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
	 * @param condition
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
	 * @param condition
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

//		switch (type) {
//		// 直接根据当前数据获取的：
//		case PlayerLevel: {
//			return player.getLevel();
//		}
//		case ChapterFinish: {
//			ChapterModule chapterModule = player.getModule(ChapterModule.class);
//			return chapterModule.isBattlePass(id) ? 1 : 0;
//		}
//		case CultivatesImmortals: {
//			return player.getDevelopModule().getHeavenlyDaoLevel();
//		}
//		// 累计计数带有额外参数的：
//		case EarnHeroCumulation: {
//			return player.getQuestModule().getCumulativeCount(type, extParam);
//		}
//
//		// 累计计数不带额外参数直接获取的
//		case RechargeCnt:
//		case ConsumesDiamonds:
//		case CumulativeLogins:
//		case EliteFinish:
//		case KillBoss:
//		case KillMonsters:
//		case WatchAdsCumulation:
//		case AccumulatedRecharge:
//		case BreakHeroCumulation: {
//			return player.getQuestModule().getCumulativeCount(type);
//		}
//		default:
//			throw new IllegalArgumentException(" not suport condition  " + type);
//		}

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

	public static boolean command(long playerId, List<Integer> command) {
		for (Integer oneCommand : command) {
			if (!command(playerId, oneCommand))
				return false;
		}

		return true;
	}

	public static boolean command(long playerId, int[] command) {
		for (int oneCommand : command) {
			if (!command(playerId, oneCommand))
				return false;
		}
		return true;
	}

	public static boolean command(long playerId, int command) {
		log.info("Player[{}] execute command[{}]", playerId, command);
		GameCommandConfig gameCommandConfig = GameCommandManager.getInstance().getGameCommandConfig(command);
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		List<Integer> parameterList = gameCommandConfig.getParameterList();

		switch (gameCommandConfig.getType()) {
		case RewardItem:
			RewardConfig rewardConfig = RewardManager.getInstance().getRewardConfig(parameterList.get(0));
			if (rewardConfig == null)
				return false;

//				List<RewardInfo> addResources = addResources(playerId, rewardConfig.getInfo());
//				RewardShowPush_55002501 build = RewardShowPush_55002501.newBuilder()
//						.addAllRewards(PbBuilder.buildRewardInfo(addResources))
//						.build();
//				PlayerHelper.sendProtcol(playerId, build);
			break;

//			case AddBuff: {
//				int id = parameterList.get(0);
//				BuffOp buffOp = player.getModule(BuffOp.class);
//				buffOp.add(id, null);
//				break;
//			}
		case DeductionRandomItem: {
			break;
		}
		default:
			return false;
		}

		return true;
	}

	public static List<DbTask> genPlayerDbTask(long uid) {

		List<DbTask> dbTasks = new ArrayList<>();
		return dbTasks;
	}

	/**
	 * @param newGameClient
	 * @param reconnect 客户端传递的参数，是否是重连
	 * @param playerId
	 * @return  是否重连了
	 */
	public static boolean reconnect(GameClient newGameClient, boolean reconnect, long playerId) {
		if (playerId == 0) {
			return false;
		}
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (player == null) {
			return false;
		}
		GameClient oldGameClient = GameClientManager.getInstance().getGameClientByPlayer(playerId);

		if (oldGameClient != null && oldGameClient != newGameClient) {
			// 可能不同设备登录同一账号,应该退出老的GameClient
			if (reconnect) {
				newGameClient.copy(oldGameClient);
			} else {
				newGameClient.copyClintLoign(oldGameClient);
			}
			GameClientManager.getInstance().removeGameClient(oldGameClient);
		}
		GameClientManager.getInstance().addGameClientSession(newGameClient);
		GameClientManager.getInstance().addGameClientPlayer(newGameClient);

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
		RFuture<Boolean> playerLockFuture = RedisUtil
				.trySetAsync(CacheType.PLAYER_SERVER_ID.key(playerId), ServerContext.getInstance().getServerId(), 5, TimeUnit.MINUTES);
		return playerLockFuture;
	}

	public static RFuture<Void> setServerId(long playerId) {
		RFuture<Void> playerLockFuture = RedisUtil
				.setAsync(CacheType.PLAYER_SERVER_ID.key(playerId), ServerContext.getInstance().getServerId(), 5, TimeUnit.MINUTES);
		return playerLockFuture;
	}

	public static Future<Void> saveSimplePlayerToRedis(Player player) {
		String key = CacheType.PLAYER_SIMPLE.key(player.getData().getPlayerId());
		return  RedisLocalCache.getInstance().putAsync(key, new SimplePlayer(player));
	}





	public static Future<Player> saveSimplePlayer(Player player) {
		Future<Void> future = saveSimplePlayerToRedis(player);
		return future.map(player);
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

	public static void selectPlayerDataWithMQ(boolean reconnect, Player dbPlayer) {
		long playerId = dbPlayer.getData().getPlayerId();
		/*
		List<DbTask> dbTasks = PlayerHelper.initDbTasks(dbPlayer.getData().getUid(), playerId);
		
		Handler<List<Object>> callBackTask = PlayerHelper.selectPlayerDataSuccess(dbPlayer);
		
		GameDataPushBatch_7d00000b.Builder builder = GameDataPushBatch_7d00000b.newBuilder();
		GameDataPushBatch2_7d00000c.Builder builder2 = GameDataPushBatch2_7d00000c.newBuilder();
		
		for (DbTask dbTask : dbTasks) {
			DbTaskProto proto = DbTaskProto.newBuilder().setMapperClass(dbTask.getMapper().getName()).setMethod(dbTask.getMethod())
					.setArg(UnsafeByteOperations.unsafeWrap(KryoUtils.serializeClassAndObject(dbTask.getArg()))).build();
			builder.addDbTasks(proto);
		}
		builder2.setArg(UnsafeByteOperations.unsafeWrap(KryoUtils.serializeClassAndObject(dbTasks)));
		GameServer.getInstance().requestDataServer(builder2.build(), new RequestCallback() {
		
			@Override
			public void onSuccess(Message message) {
				byte[] body = message.getBody();
				Object obj = KryoUtils.deserializeClassAndObject(body);
				callBackTask.handle((List<Object>) obj);
			}
			@Override
			public void onException(Throwable e) {
				e.printStackTrace();
			}
		});*/

	}

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

	public static String getServerConfigVersion() {
		List<versionConfig> list = versionManager.getInstance().list();
		return list.get(0).getVersion();

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
				data.setModules(JsonUtil.toJsonString(player.getModules()));
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
		}
		throw new IllegalArgumentException("没有实现的经验id： " + id);
	}
}
