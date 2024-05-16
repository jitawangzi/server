package cn.game.games.net.game.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.RequestCallback;
import org.apache.rocketmq.common.message.Message;
import org.redisson.api.RFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.google.protobuf.UnsafeByteOperations;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Buff;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.cache.op.impl.BuffOp;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.GoodsModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.data.mapper.ItemMapper;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.data.mapper.PlayerExtMapper;
import cn.game.games.net.data.mapper.RoleMapper;
import cn.game.games.net.data.mapper.UserMapper;
import cn.game.games.net.data.mapper.UserTagMapper;
import cn.game.games.net.game.GameServer;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.db.DbTask;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.account.Account;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.buff.BuffValue;
import cn.game.games.util.BIHelper;
import cn.game.games.util.DAO;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.config.ConsumeConfig;
import cn.game.protocol.generated.config.EventOptionConfig;
import cn.game.protocol.generated.config.GameCommandConfig;
import cn.game.protocol.generated.config.RandomGivenConfig;
import cn.game.protocol.generated.config.RandomGroupConfig;
import cn.game.protocol.generated.config.RewardConfig;
import cn.game.protocol.generated.config.UserUpgradeConfig;
import cn.game.protocol.generated.config.versionConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.ConditionManager;
import cn.game.protocol.generated.manager.ConsumeManager;
import cn.game.protocol.generated.manager.EventOptionManager;
import cn.game.protocol.generated.manager.GameCommandManager;
import cn.game.protocol.generated.manager.RandomGivenManager;
import cn.game.protocol.generated.manager.RandomGroupManager;
import cn.game.protocol.generated.manager.RewardManager;
import cn.game.protocol.generated.manager.UserUpgradeManager;
import cn.game.protocol.generated.manager.versionManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BaseMsg.AssetInfo;
import cn.game.protocol.protobuf.BaseMsg.ItemInfo;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099;
import cn.game.protocol.protobuf.PlayerMsg.PlayerLoginResponse_01000002;
import cn.game.protocol.protobuf.PlayerMsg.PlayerLogoutPush_01100030;
import cn.game.protocol.protobuf.RewardMsg;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.RewardMsg.SpendPush_55001501;
import cn.game.protocol.protobuf.ServerMsg.DbTaskProto;
import cn.game.protocol.protobuf.ServerMsg.GameDataPushBatch2_7d00000c;
import cn.game.protocol.protobuf.ServerMsg.GameDataPushBatch_7d00000b;
import cn.game.protocol.protobuf.ServerMsg.GamePlayerRequest_7d000015;
import cn.game.protocol.protobuf.ServerMsg.GamePlayerResponse_7d000016;
import cn.game.util.Config;
import cn.game.util.DateUtil;
import cn.game.util.GameUtil;
import cn.game.util.KryoUtils;
import cn.game.util.Pair;
import cn.game.util.RedissonUtil;
import cn.game.util.Rnd;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class PlayerHelper {

	private static final Logger log = LoggerFactory.getLogger(PlayerHelper.class);
//	private static final Logger levellog = LoggerFactory.getLogger("levelLog");
//	private static final Logger resourceDelLog = LoggerFactory.getLogger("resourceDelLog");
//	private static final Logger loginlog = LoggerFactory.getLogger("loginLog");

	public static boolean isEnough(Player player, List<Entry<Integer, Integer>> list) {

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
	public static boolean isEnough(long playerId, int id, int count) {
		Player player =  PlayerManager.getInstance().getPlayer(playerId) ; 
		return isEnough(player, id, count);
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

	public static List<RewardInfo> addResources(Player player, int id, int value, OpType opType, boolean notify) {
		if (value < 0) {
			return Collections.EMPTY_LIST;
		}
		List<RewardInfo> rewards = null;
		try {
			GoodsModule goodsModule = player.getGoodsModule(id);
			rewards = goodsModule.addReward(id, value, opType);
			log.info("player[{}] addReward  id[{}]count[{}]", player.getPlayerId(), id, value);
			player.handleEvent(EventTypeEnum.GetItem, id, value);
			BIHelper.resrouceUpdate(player, id, value, opType, true);
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

	/**
	 * 进行某操作时扣除资源，包括所有大类型
	 * @param playerId
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
		if (mode != BuffValue.CHANGE_BY_VALUE) {
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
	 * @Description 进行某操作时扣除资源，包括所有大类型
	 * @param playerId
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
//		if (!PlayerManager.getInstance().hasCache(player)) {
//			OfflineResourceAdd add = new OfflineResourceAdd();
//			add.setItemId(id);
//			add.setCount(value);
//			add.setPlayerId(playerId);
//			add.setType(false);
//			DAO.insert(OfflineResourceAddMapper.class, add);
//			return true;
			// 不在线不能扣资源
//			return false;
//		}
		GoodsModule goodsModule = player.getGoodsModule(id);
		long maxCount = goodsModule.getCount(id);
		if (value > maxCount) {
			value = (int) maxCount;
		}
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
			BIHelper.resrouceUpdate(player, id, value, consumeType, false);
		}
		return ret;
	}

	/**
	 * @Description 进行某操作时扣除资源，包括所有大类型
	 * @param playerId
	 * @param list
	 * @param consumeType
	 * @return
	 */
	public static boolean delResources(Player player, List<Entry<Integer, Integer>> list, OpType consumeType) {

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
				RandomGroupConfig groupConfig = Rnd.randomOne(randomGroupIDList);
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
		}
		return ret;
	}

	@Deprecated
	public static Player addExp(Player player, int exp) {
		int curExp = player.getData().getExp() + exp;
		UserUpgradeConfig expConfig = UserUpgradeManager.instance().get(player.getData().getLevel());
		UserUpgradeConfig nextexpConfig = UserUpgradeManager.instance().getNullable(player.getData().getLevel() + 1);

		while (curExp >= expConfig.experience && nextexpConfig != null) {
			player.getData().setExp(curExp - expConfig.experience);
			player.getData().setLevel(player.getData().getLevel() + 1);
			curExp = player.getData().getExp();
//			player.handleEvent(new GameEvent(EventTypeEnum.LevelUp, player, player.getData().getLevel()));

			expConfig = UserUpgradeManager.instance().getNullable(player.getData().getLevel());
			nextexpConfig = UserUpgradeManager.instance().getNullable(player.getData().getLevel() + 1);
//			levellog.info("opType[levelUp]playerId[{}]newLevel[{}]", player.getData().getPlayerId(), player.getData().getLevel());
		}
		if (curExp > expConfig.experience) {
			curExp = expConfig.experience;
		}
		player.getData().setExp(curExp);
		return player;
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
	 * @Description 初始化新角色数据，角色第一次创建时需要调用此方法
	 * @param player
	 */
	public static void initNewPlayerData(Player player) {
		Long playerId = player.getData().getPlayerId();
		log.info("首次初始化角色playerId={}", playerId);

		player.handleEvent(EventTypeEnum.PLAYER_CREATE);

		GameLogger.rolebuild(player);
	}

	/**
	 * @Description 登陆后进行一些初始化操作，例如刷新离线数据等，初始化定时任务等等, 创建新玩家后，也会执行此方法;
	 * @param player
	 */
	public static void initAfterLogin(Player player) {
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
			PlayerManager.getInstance().saveClientCache(playerId);
		});
		// 上线后生成自己的简单信息
		try {
			PlayerManager.getInstance().getAndLoadSimplePlayer(playerId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.LoginFinish));
		GameLogger.login(player);
		GameLogger.rolelogin(player);
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
		EventHelper.handleEvent(player.getData().getPlayerId(), new GameEvent(EventTypeEnum.NewDay));

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

		EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.NewWeek));

		player.getData().setRefreshWeek(nowWeek);

		log.info("new week refresh player:" + playerId + " succ");

	}
	
	public static void refreshMonth(Player player) {
		int nowMonth = DateUtil.getMonth();
		//设置参数
		if (player.getData().getRefreshMonth()==null) {
			player.getData().setRefreshMonth(nowMonth);
			return;
		}
		if (nowMonth == player.getData().getRefreshMonth()) {
			return;
		}
		long playerId = player.getData().getPlayerId();
		log.info("new month refresh player:" + playerId);
		EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.NewMonth));
		player.getData().setRefreshMonth(nowMonth);
		log.info("new month refresh player:" + playerId + " succ");

	}
	
	public static void refreshFiveDay(Player player) {
		// 每天早晨5点刷新
		long playerId = player.getData().getPlayerId();		
		int fiveTime = (int) (DateUtil.getDayHourTimestamp(5) / 1000);
		
		if(player.getData().getRefreshFiveDay() == null) {
			player.getData().setRefreshFiveDay(0);
		}
		
		if (player.getData().getRefreshFiveDay() >= fiveTime) {
			return;
		}

		int secord = fiveTime - player.getData().getRefreshFiveDay(); 
		
		long nowTime = DateUtil.getStamp();
		
		// 判断必须要跨一天以上才可以刷
		int refTime = fiveTime;
		
		if(nowTime < fiveTime) {
						
			refTime = (int) (fiveTime - DateUtil.DAY_SECONDS);
			
			if(secord == DateUtil.DAY_SECONDS) {
				return;
			}
		}
		EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.NewDay5));
		
		player.getData().setRefreshFiveDay(refTime);

		log.info("new day five clock refresh player:" + playerId + " succ");
	}

	/** 
	 * 一次性增加多个奖励，增加完奖励后推送给客户端一次。 
	 * @param playerId
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
			PlayerHelper.sendProtocol(player.getPlayerId(), RewardMsg.RewardPush_55000501.newBuilder().addAllRewards(rewardItems));
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
	 * @param playerId
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
	 * @Description 主动给玩家发送消息
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
	public static Future<io.vertx.core.eventbus.Message<GamePlayerResponse_7d000016>> sendRemotePlayer(long playerId,
			Object message, boolean discardWhenOffline) {
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
					.setPlayerId(playerId).setId(msgId).setData(m.toByteString()).build();
			Future<io.vertx.core.eventbus.Message<GamePlayerResponse_7d000016>> requestRemoteServer = VxHolder.requestRemoteServer(serverId, gamePlayerRequest_7d000015);
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
	 * @Description 是否满足所有条件
	 * @param playerId
	 * @param conditions
	 * @return
	 */
	public static boolean checkCondition(Player player, List<Integer> conditions) {

		return checkCondition(player, conditions, false, null);
	}

	public static boolean checkCondition(Player player, int[] conditions) {

		return checkCondition(player, GameUtil.transform1(conditions));
	}
	/**
	 * @Description
	 * @param playerId
	 * @param conditions
	 * @param or
	 *            true,如果满足任意条件
	 * @return
	 */
	public static boolean checkCondition(Player player, List<Integer> conditions, boolean or) {

		return checkCondition(player, conditions, or, null);
	}

	public static boolean checkCondition(Player player, List<Integer> conditions, boolean or, GameEvent param) {

		if (conditions.isEmpty()) {
			return true ; 
		}
		if (or) {
			for (Integer e : conditions) {
				if (checkCondition(player, e, param)) {
					return true; 
				}
			}
		} else {
			for (Integer e : conditions) {
				if (!checkCondition(player, e, param)) {
					return false; 
				}
			}
			return true;
		}
		return false;
	}

	public static boolean checkCondition(Player player, List<Integer> conditions, GameEvent param) {
		return checkCondition(player, conditions, false, param);
		
	}

	/**
	 * @Description 基础的条件检查
	 * @param playerId
	 * @param condition
	 * @param param
	 *            需要传入待检查的一些参数
	 * @return
	 */
	public static boolean checkCondition(Player player, int condition, Object... param) {
		if (condition == 0) {
			return true;
		}

		ConditionConfig conditionConfig = ConditionManager.instance().get(condition);

		ConditionTypeEnum type = ConditionTypeEnum.get(conditionConfig.type);
		int id = conditionConfig.idParam;
		int count = conditionConfig.numParam;
		int[] extParam = conditionConfig.extParam;
		int operator = conditionConfig.operator;

		switch (type) {
			case ChapterFinish: {
				ChapterModule chapterOp = player.getModule(ChapterModule.class);
				return chapterOp.isBattlePass(id);
			}
			case PlayerLevel: {
				return operator(player.getData().getLevel(), count, operator);
			}
			case AccumulatedRecharge: {
				return operator(player.getQuestModule().getCumulativeCount(ConditionTypeEnum.AccumulatedRecharge), count, operator);
			}
//			case PlayerCombat: {
//				return true;
//			}
//			case MonthCard: {
//				MonthCardModule monthCardModule = player.getModule(MonthCardModule.class);
//				for (int i : extParam) {
//					MonthCard monthCard = monthCardModule.getMonthCard(i);
//					if (monthCard == null) {
//						return false;
//					}
//				}
//				return true;
//			}
			default:
				throw new IllegalArgumentException(" not suport condition  " + type);
		}
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
			
			case AddBuff: {
				int id = parameterList.get(0);
				BuffOp buffOp = player.getModule(BuffOp.class);
				buffOp.add(id, null);
				break;
			}
			case DeductionRandomItem: {
				break;
			}
			default:
				return false;
		}
		
		return true;
	}

	/** 
	 * 一般选择事件选项的时候，不需要目标id
	 * @param playerId
	 * @param eventId
	 * @param optionId
	 * @param notify 是否通知客户端
	 */
	public static List<Buff> chooseEventOption(Player player, int eventId, int optionId, boolean notify) {

		EventOptionConfig config = EventOptionManager.getInstance().getEventOptionConfig(optionId);
		// 选择时花费
		if (!PlayerHelper.delResources(player, config.getChooseCost(), OpType.EventOptin)) {
			return null;
		}
		List<Buff> ret = new ArrayList<>();
		if (config.getIsRandom()) {// 权重
			int[] buffWeight = config.getBuffWeight();
			int randomIndex = Rnd.randomIndex(buffWeight);
			int[] buffId = config.getBuffId();
			int randomBuffId = buffId[randomIndex];
			List<Buff> buff = BuffHelper.addBuff(player.getPlayerId(), randomBuffId, null, notify);
			ret.addAll(buff);

		} else {
			// 固定选项
			int[] buffId = config.getBuffId();
			for (int id : buffId) {
				List<Buff> buff = BuffHelper.addBuff(player.getPlayerId(), id, null, notify);
				ret.addAll(buff);
			}
		}

		return ret;
	}

	/**
	 * 查询玩家存档通用数据的任务
	 * 
	 * @param uid
	 * @return
	 */
	public static List<DbTask> genUserDbTask(long uid) {

		List<DbTask> dbTasks = new ArrayList<>();
		dbTasks.add(new DbTask(UserMapper.class, MapperConstant.selectByPrimaryKey, uid));
		dbTasks.add(new DbTask(UserTagMapper.class, MapperConstant.selectByUid, uid));
		return dbTasks;
	}
	public static List<DbTask> genPlayerDbTask(long uid) {

		List<DbTask> dbTasks = new ArrayList<>();
		return dbTasks;
	}

	/**
	 * @param oldGameClient
	 * @param newGameClient
	 * @param reconnect
	 * @return  是否重连了 
	 */
	public static boolean reconnect(GameClient oldGameClient, GameClient newGameClient, boolean reconnect) {
		if (oldGameClient != null && oldGameClient.getPlayerId() > 0) { // 可能不同设备登录同一账号,应该退出老的GameClient
			if (reconnect) {
				newGameClient.copy(oldGameClient);
			} else {
				newGameClient.copyClintLoign(oldGameClient);
			}
			oldGameClient.sendProtocol(PlayerLogoutPush_01100030.getDefaultInstance());
			GameClientManager.getInstance().removeGameClientConnection(oldGameClient);

			GameClientManager.getInstance().addGameClientSession(newGameClient);
			GameClientManager.getInstance().addGameClientPlayer(newGameClient);

			Player player = PlayerManager.getInstance().getPlayer(oldGameClient.getPlayerId());
			if (player == null) {
				newGameClient.sendProtocol(PlayerErrorPush_01000099.getDefaultInstance(), ErrorMsgEnum.unknown.getId());
				return true; 
			}
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
		return false;
	}

	public static void startLoadPlayerFromDb(GameClient gameClient, PlayerData dbPlayer, Account account) {

		Long playerId = dbPlayer.getPlayerId();
		BiConsumer<Boolean, ? super Throwable> action = (v, throwable) -> {
			GameClientManager.getInstance().addGameClientPlayer(gameClient);
			GameClientManager.getInstance().addGameClientSession(gameClient);

			Player player = new Player(dbPlayer);
			player.setGameClient(gameClient);
			player.setAccount(account);
			// load from db
			PlayerHelper.selectPlayerData(player);

		};
//		获取分布式锁之后再load
		RFuture<Boolean> playerLockFuture = PlayerHelper.trySetServerId(playerId);
		playerLockFuture.onComplete((v, throwable) -> {
			if (v) {
				action.accept(v, throwable);
			} else {
				String serverId = ServerContext.getInstance().getServerId();
				// 看看是不是自己服务器
				RFuture<String> setAsync = RedissonUtil.getAsync(CacheType.PLAYER_SERVER_ID.key(playerId));
				setAsync.onComplete((vv, tt) -> {
					if (vv != null && vv.equals(serverId)) {
						action.accept(v, throwable);
					} else {
						log.error(playerId + " getPlayerLock failed", throwable);
						gameClient.sendProtocol(PlayerLoginResponse_01000002.getDefaultInstance(),
								ErrorMsgEnum.unknown.getId());
					}
				});
			}
		});
	}

	public static RFuture<Boolean> trySetServerId(long playerId) {
		RFuture<Boolean> playerLockFuture = RedissonUtil.trySetAsync(CacheType.PLAYER_SERVER_ID.key(playerId),
				ServerContext.getInstance().getServerId(), 5, TimeUnit.MINUTES);
		return playerLockFuture;
	}

	public static RFuture<Void> setServerId(long playerId) {
		RFuture<Void> playerLockFuture = RedissonUtil.setAsync(CacheType.PLAYER_SERVER_ID.key(playerId),
				ServerContext.getInstance().getServerId(), 5, TimeUnit.MINUTES);
		return playerLockFuture;
	}

	/** 
	 * 从数据库中查询玩家所有数据
	 * @param reconnect
	 * @param player
	 */
	public static void selectPlayerData(Player player) {
		List<DbTask> dbTasks = initDbTasks(player);
		Future<List<Object>> select = DAO.execute(dbTasks);
		Handler<List<Object>> callBackTask = PlayerHelper.selectPlayerDataSuccess(player);
		select.onSuccess(callBackTask).onFailure(e -> {
			selectPlayerDataFail(player, e);
		});
//		DataGameServerInterface dataGameCallback = GameServer.getInstance().getDataGameCallback(callBackTask);
//		dataGameCallback.execMutiTasks(dbTasks);
	}
	public static void selectPlayerDataWithMQ(boolean reconnect, Player dbPlayer) {
		long playerId = dbPlayer.getData().getPlayerId();

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
		});


	}
	/** 
	 * 从数据库载入玩家数据，同步调用，一般测试时使用。
	 * @param playerIds
	 */
	public static void loadPlayerTest(long... playerIds) {
		for (long id : playerIds) {
			if (PlayerManager.getInstance().hasCache(id)) {
				continue;
			}
			Player p = (Player) DAO.executeSync(PlayerDataMapper.class, MapperConstant.selectByPrimaryKey, id);
			PlayerHelper.selectPlayerData(p);
		}
	}

	/** 
	 * 玩家所有的数据库查询任务
	 * @param uid
	 * @param playerId
	 * @return
	 */
	@Deprecated
	public static List<DbTask> initDbTasks(long uid, long playerId) {
		List<DbTask> dbTasks = new ArrayList<>();

		dbTasks.add(new DbTask(PlayerExtMapper.class, MapperConstant.selectByPrimaryKey, playerId));
		dbTasks.add(new DbTask(RoleMapper.class, MapperConstant.selectByPlayerId, playerId));
		dbTasks.add(new DbTask(ItemMapper.class, MapperConstant.selectByPrimaryKey, playerId));
		return dbTasks;
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

	
	public static Handler<List<Object>> selectPlayerDataSuccess(Player player) {

		return list -> {
			try {
				PlayerManager.getInstance().initAdd(player);
				long playerId = player.getData().getPlayerId();
				ListIterator<?> listIterator = list.listIterator();
				if (GameServer.getInstance().isSinglePlayerTable()) {
					for (BasePlayerModule module : player.getModuleSorted()) {
						if (module.alwaysStoreDataInStandaloneTable()) {
							module.loadFromDb(listIterator);
						}
						module.initFromDbAfter();
					}
				} else {
					for (BasePlayerModule module : player.getModuleSorted()) {
						module.loadFromDb(listIterator);
					}
				}
				initAfterLogin(player);

				PlayerLoginResponse_01000002.Builder resp = PlayerLoginResponse_01000002.newBuilder();
				resp.setInfo(PbBuilder.buildPlayerInfo(player));
//				resp.setConfigFileVersion(PlayerHelper.getServerConfigVersion());

				PlayerHelper.sendProtocol(playerId, resp);

				GameClientManager.getInstance().broadcastOnlineToOtherServer(playerId, true, null);

//				loginlog.info("opType[gameLogin]playerId[{}]isCreate[{}]isLogin[{}]onlineTime[{}]", playerId, false, true, 0);
			} catch (Exception e) {
				selectPlayerDataFail(player, e);
			}
		};
	}

	private static void selectPlayerDataFail(Player player, Throwable e) {
		log.error("player " + player.getData().getPlayerId() + " login error ", e);
		PlayerManager.getInstance().deletePlayer(player.getPlayerId());
		PlayerHelper.sendErrorProtocol(player.getData().getPlayerId(), ErrorMsgEnum.unknown.getId());
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
}
