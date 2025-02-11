package cn.game.games.net.game.module.player;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.PlayerIds;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.PlayerIdsMapper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.account.Account;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.develop.hero.HeroModule;
import cn.game.games.net.game.module.develop.hero.QualityStarObj;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.protocol.generated.config.FuncOpenConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeadPortraitConfig;
import cn.game.protocol.generated.config.UserUpgradeConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.FuncOpenManager;
import cn.game.protocol.generated.manager.HeadPortraitManager;
import cn.game.protocol.generated.manager.UserUpgradeManager;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BaseMsg.GoodsInfo;
import cn.game.protocol.protobuf.BaseMsg.QualityStar;
import cn.game.protocol.protobuf.PlayerMsg.CloudBoxInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.PlayerMsg.PlayerCloudBoxPush_01100040;
import cn.game.protocol.protobuf.RewardMsg;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.Config;
import cn.game.util.DateUtil;
import cn.game.util.IntMapWrapper;
import cn.game.util.Rnd;
import io.vertx.core.Promise;

/**    
 * 零散、简单、通用的一些数据，都可以放这里,
 * 2024年3月19日 下午6:38:13
 * @author SYQ
 */
public class PlayerModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay,
			EventTypeEnum.LoginFinish, EventTypeEnum.Reconnect, EventTypeEnum.LevelUp, EventTypeEnum.ResourceRemove, EventTypeEnum.FuncOpen };

	@JsonIgnore
	@Deprecated
	private Map<Integer, Map<Integer, PlayerIds>> idsMap = new HashMap<Integer, Map<Integer, PlayerIds>>();
	/** 玩家拥有的各种单纯的id集合，通常是只增加新id，并且id不能重复。 key:type {@link IdConstant}*/
	private Map<Integer, Set<Integer>> idsSet = new HashMap<Integer, Set<Integer>>();
	/** 等级数据，key: {@link Asset} 这里是经验升的等级*/
	private IntMapWrapper expLevelMap = new IntMapWrapper();
	/** 炼金等级 */
	private IntMapWrapper alchemysMap = new IntMapWrapper();
	/** 支付成功后的回调 */
	@JsonIgnore
	private Map<Long, Promise<Boolean>> payCallback = new HashMap<Long, Promise<Boolean>>() ; 
	/** 本次订单充了多少钱 */
	@JsonIgnore
	@Deprecated
	private Map<Long, Integer> payRmbs = new HashMap<Long, Integer>();
	/** 订单记录 */
	private Map<Long, PayItem> payItems = new HashMap<Long, PayItem>();
	
	/** 随机宝箱，小云宝箱 */
	private List<Goods> cloudBox;
	/** 每日出现的小云宝箱次数 */
	private int cloudBoxCount;
	private int lastCloudBoxRewardTime;
	private boolean isFirstLoign = false;

	private Map<Integer, Integer> guideMap = new HashMap<Integer, Integer>();

	/** 上次世界聊天发言时间 */
	private int lastChatTime;
	
	/** 账号也记录一下，如果离线修复数据时触发bi使用 */
	private Account account;

	@Override
	public Class<?>[] defaultDbMapperClass() {
		return new Class[] { PlayerIdsMapper.class };
	}

//	@Override
//	protected void initFromDb(ListIterator<?> iterator) {
//		List<PlayerIds> ids = (List<PlayerIds>) iterator.next();
//		for (PlayerIds playerIds : ids) {
//			idsMap.computeIfAbsent(playerIds.getType(), key -> new HashMap<>()).put(playerIds.getConfigId(), playerIds);
//		}
//	}
	@Override
	public void initFromDbAfter() {
		Iterator<Entry<Long, PayItem>> iterator = payItems.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<java.lang.Long, cn.game.games.net.game.module.recharge.PayItem> entry = (Map.Entry<java.lang.Long, cn.game.games.net.game.module.recharge.PayItem>) iterator
					.next();
			PayItem value = entry.getValue();
			if (value.isFinish() && System.currentTimeMillis() - value.getFinishTime() > DateUtil.DAY_MILLIS * 10) {
				iterator.remove();
			}
		}
	};

	public boolean addId(IdConstant type, int configId) {
		return getIdsSet(type).add(configId);
//		Map<Integer, PlayerIds> map = getOrCreateIdSet(type);
//		if (map.containsKey(configId)) {
//			return;
//		}
//		PlayerIds add = new PlayerIds();
//		add.setPlayerId(playerId);
//		add.setType(type);
//		add.setConfigId(configId);
//		add.setCreateTime(new Date());
//		add.setUpdateTime(System.currentTimeMillis());
//		add.insert();
//		map.put(add.getConfigId(), add);
	}

	public boolean removeId(IdConstant type, int configId) {
//		Map<Integer, PlayerIds> map = getOrCreateIdMap(type);
//		PlayerIds playerIds = map.remove(configId);
//		if (playerIds != null) {
//			playerIds.delete();
//		}
		return getIdsSet(type).remove(configId);
	}

//	public void updateTime(int type, int configId) {
//		PlayerIds ids = getIds(type, configId);
//		if (ids != null) {
//			ids.setUpdateTime(System.currentTimeMillis());
//			ids.update();
//		} else {
//			addId(type, configId);
//		}
//	}

	public boolean hasId(IdConstant type, int configId) {
		return getIdsSet(type).contains(configId);
	}

	public Set<Integer> getIdsSet(IdConstant type) {
		return idsSet.computeIfAbsent(type.getValue(), k -> new HashSet<>());
	}

	public IntMapWrapper getExpLevelMap() {
		return expLevelMap;
	}

	public IntMapWrapper getAlchemysMap() {
		return alchemysMap;
	}

	public List<Goods> getCloudBox() {
		return cloudBox;
	}

	public void setCloudBox(List<Goods> cloudBox) {
		this.cloudBox = cloudBox;
	}

	public void setLastCloudBoxRewardTime(int lastCloudBoxRewardTime) {
		this.lastCloudBoxRewardTime = lastCloudBoxRewardTime;
	}

	@Override
	public void autoSaveTasks(List<DbEntity> entities) {
		entities.add(player.getData());
	}
	
	public void addPayCallback(long uid,Promise<Boolean> callback) {
		this.payCallback.put(uid, callback); 
	}

	public void addPayItems(PayItem payItem) {
		this.payItems.put(payItem.getOrderId(), payItem);
	}

	public PayItem getPayItems(long uid) {
		return this.payItems.get(uid);
	}
	
	public Map<Integer, Integer> getGuideMap() {
		return guideMap;
	}

	public boolean execPayCallback(long uid) {
		Promise<Boolean> callback = this.payCallback.remove(uid);
		if(callback != null){
			callback.complete(true);
			return true;
		}
		return false;
	}

	public int getLastChatTime() {
		return lastChatTime;
	}

	public void setLastChatTime(int lastChatTime) {
		this.lastChatTime = lastChatTime;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public void startCloudBoxTask() {
		int[] randomCLoud = GlobalConst.RandomCLoud;
		if (randomCLoud[0] == 1) {
			player.setPeriodicTask(randomCLoud[1]*1000, r -> {
//				log.info("playerId:{} startCloudBoxTask at:{}", playerId, DateUtil.getTimeByPattern(new Date()));
//				System.err.println("playerId:" + playerId + " startCloudBoxTask at ss" + DateUtil.getTimeByPattern(new Date()));
				if (cloudBoxCount >= GlobalConst.RandomCLoudCnt) {
					return;
				}
				if (cloudBox != null && !cloudBox.isEmpty()) {
					return;
				}
				if (lastCloudBoxRewardTime != 0 && DateUtil.currentTimeSeconds() - lastCloudBoxRewardTime < randomCLoud[1]) {
					return;
				}
				if (Rnd.hit(randomCLoud[2])) {
					cloudBox = PlayerHelper.randomReward(randomCLoud[3]);
					List<GoodsInfo> collect = cloudBox.stream().map(Goods::toGoodsInfo).collect(Collectors.toList());
					player.getGameClient().sendProtocol(PlayerCloudBoxPush_01100040.newBuilder().setCloudBox(CloudBoxInfo.newBuilder().addAllItems(collect)));
					cloudBoxCount++;
				}
			});
		}
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		builder.setPlayer(player.toProto());
		builder.putAllLevels(expLevelMap.getMap());

		// 礼包
		builder.addAllChapterPacks(getIdsSet(IdConstant.CHAPTER_PACK));
		
		if (cloudBox != null && !cloudBox.isEmpty()) {
			List<GoodsInfo> collect = cloudBox.stream().map(Goods::toGoodsInfo).collect(Collectors.toList());
			builder.setCloudBox(CloudBoxInfo.newBuilder().addAllItems(collect));
		}
		builder.setFirstLogin(isFirstLoign);
		isFirstLoign = false;
		builder.putAllGuide(guideMap);
		builder.setDisableIosPayVersion(Config.disableIosPayClientVersion);
		
		builder.addAllHeadboxs(getIdsSet(IdConstant.HEAD_BOX));
		builder.addAllHeadPortraits(getIdsSet(IdConstant.HEAD_PORTRAIT));
		builder.addAllFuncOpenRewardIds(getIdsSet(IdConstant.FUNC_OPEN_REWARD));
		builder.addAllHeroSkinIds(getIdsSet(IdConstant.HERO_SKIN));
		List<FuncOpenConfig> lockHideList = FuncOpenManager.instance().getLockHideList(false);
		if (lockHideList != null) {
			builder.addAllCloseFuncs(lockHideList.stream().map(f -> f.ID).collect(Collectors.toList()));
		}

		HeroModule heroModule = player.getHeroModule();
		Map<Integer, QualityStarObj> heroStarsMap = heroModule.getIllustrationsHeroStars();
		heroStarsMap.forEach((k, v) -> {
			builder.addHeroStars(QualityStar.newBuilder().setHeroId(k).setQuality(v.quality).setStar(v.star));
		});
		builder.setRewardLevel(heroModule.getIllustrationRewardLevel());

		builder.setShabiyincangguanggao(Config.shabiyincangguanggao);

		//宗门信息
		builder.setZongMenId(player.getZongMenId());
		if (player.getZongMenId() != 0) {
			builder.setZongMenName(player.getZongMenName());
		}
	}
	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		case LoginFinish: {
//			PlayerManager.getInstance().online(playerId, ServerContext.getInstance().getServerId());
			initLevel();
			if (player.isFuncOpen(InitialUI.RandomBox)) {
				startCloudBoxTask();
			}
			setAccount(player.getAccount());
			break;
		}
		case PLAYER_CREATE: {
//			PlayerManager.getInstance().online(playerId, ServerContext.getInstance().getServerId());
			// 初始化头像框
			Collection<HeadPortraitConfig> list = HeadPortraitManager.instance().list();
			HeroModule heroModule = player.getHeroModule();
			int headPortrait = 0;
			for (HeadPortraitConfig headPortraitConfig : list) {
				if (heroModule.has(headPortraitConfig.ConditionHero)) {
					headPortrait = headPortraitConfig.ID;
					break;
				}
			}
			player.getData().setHead(headPortrait);
			int headBox = 0;
			Set<Integer> headBoxSet = player.getPlayerModule().getIdsSet(IdConstant.HEAD_BOX);
			for (Integer integer : headBoxSet) {
				headBox = integer;
				break;
			}
			player.getData().setHeadFrame(headBox);
			player.getData().setImage(headPortrait);

			break;
		}
		case Reconnect: {
			player.setActive(true);
			break;
		}
		case LevelUp: {
			int exp = event.getIntParameter(0);
			int level = event.getIntParameter(1);
			if (exp == Asset.playerExp.ID) {
				// 给等级奖励
				UserUpgradeConfig userUpgradeConfig = UserUpgradeManager.instance().get(level);
				List<RewardInfo> reward = PlayerHelper.addResources(player, userUpgradeConfig.LvReward, OpType.PlayerLevelUp);
				player.getGameClient().sendProtocol(RewardMsg.RewardPush_55000501.newBuilder().addAllRewards(reward));
			}
			break;
		}
		case ResourceRemove: {
			int id = event.getIntParameter(0);
			expLevelMap.removeValue(id);
			break;
		}
		case FuncOpen: {
			InitialUI func = event.getParameter(0);
			if (func == InitialUI.RandomBox) {
				startCloudBoxTask();
			}
			break;
		}
		case NewDay: {
			isFirstLoign = true;
			cloudBoxCount = 0;
			break;
		}
		}
	}

	/** 
	 * 初始化经验的等级
	 */
	public void initLevel() {
		for (Asset asset : Asset.values()) {
			if (asset.Type == 2) {
				if (!expLevelMap.hasValue(asset.ID)) {
					if (asset.ID != Asset.VIPExp.ID){ //VIP 初始 从 0 级开始
						expLevelMap.add(asset.ID, 1);
					}
				}
			}
		}
	}

	@Override
	protected int getInitOrder() {
		return INIT_PRIORITY_HIGH;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

}
