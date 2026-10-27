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
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.account.Account;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.develop.hero.HeroModule;
import cn.game.games.net.game.module.develop.hero.QualityStarObj;
import cn.game.games.net.game.module.player.headbox.HeadBoxModule;
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
	private transient Map<Long, Promise<Boolean>> payCallback = new HashMap<Long, Promise<Boolean>>() ; 

    /** 支付承诺与完整奖励步骤的临时绑定；不恢复旧进程的回调。 */
    @JsonIgnore
    private transient Map<io.vertx.core.Future<Boolean>, cn.game.games.net.game.module.recharge.PaymentCompletion<?>> payRewards
            = new java.util.concurrent.ConcurrentHashMap<>();
	/** 本次订单充了多少钱 */
	@JsonIgnore
	@Deprecated
	private Map<Long, Integer> payRmbs = new HashMap<Long, Integer>();
	/** 订单记录 */
	private Map<Long, PayItem> payItems = new HashMap<Long, PayItem>();

    /** 本次发货的同事务凭证；只在所属邮箱准备和清除，不进入模块 JSON。 */
    @JsonIgnore
    private transient cn.game.games.net.game.helper.PaymentDedupStore.Completion durablePayment;

    /** @return 尚未获得原提交回执的凭证附件 */
    @JsonIgnore
    public cn.game.games.net.game.helper.PaymentDedupStore.Completion getDurablePayment() { return durablePayment; }

    /** @param completion 本次不可变凭证；仅原提交成功后可传 null 清除 */
    public void setDurablePayment(cn.game.games.net.game.helper.PaymentDedupStore.Completion completion) {
        if (completion != null && durablePayment != null) { throw new IllegalStateException("PAYMENT_DURABLE_PENDING"); }
        durablePayment = completion;
    }
	
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


//	@Override
//	public Class<?>[] defaultDbMapperClass() {
//		return new Class[] { PlayerIdsMapper.class };
//	}

//	@Override
//	protected void initFromDb(ListIterator<?> iterator) {
//		List<PlayerIds> ids = (List<PlayerIds>) iterator.next();
//		for (PlayerIds playerIds : ids) {
//			idsMap.computeIfAbsent(playerIds.getType(), key -> new HashMap<>()).put(playerIds.getConfigId(), playerIds);
//		}
//	}
	@Override
	public void initFromDbAfter() {
		// finish 和日龄均不证明平台已终结或证据已归档。当前没有可信删除依据，
		// 因而保留普通订单、历史 finish、完成凭证及异常阶段，避免重启后丢失去重屏障。
		// 容量随订单数增长；归档与限额告警须在具备可验证的终态/归档协议后单独落地。
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

    /** @param uid 订单 ID @param callback 本次发布的回调；仅在所属玩家邮箱清理同一回调，不影响已有订单 */
    public void removePayCallback(long uid, Promise<Boolean> callback) {
        this.payCallback.remove(uid, callback);
    }

	public void addPayItems(PayItem payItem) {
		this.payItems.put(payItem.getOrderId(), payItem);
	}

	public PayItem getPayItems(long uid) {
		return this.payItems.get(uid);
	}

    /**
     * 仅在所属玩家队列、封存 modules 的同一执行段调用，不遍历或清理订单。
     * @return 全部保留订单的数量；不包含临时支付回调
     */
    @JsonIgnore
    public int retainedPaymentOrderCount() { return payItems.size(); }
	
	public Map<Integer, Integer> getGuideMap() {
		return guideMap;
	}

    /** @param payment 原支付承诺 @param reward 完整奖励步骤；调用方在发货任务入队前绑定 */
    public void registerPayReward(io.vertx.core.Future<Boolean> payment,
            cn.game.games.net.game.module.recharge.PaymentCompletion<?> reward) {
        if (payRewards.putIfAbsent(payment, reward) != null) {
            throw new IllegalStateException("PAYMENT_REWARD_DUPLICATE_REGISTRATION");
        }
        // 发布失败可在外部完成承诺；仅清除线程安全的临时绑定，不修改玩家业务字段。
        payment.onComplete(ignored -> payRewards.remove(payment, reward));
    }

    /** @param uid 订单 ID @return 是否有在线奖励绑定；完成奖励不等于完成保存，不能在此通知购买成功 */
    public boolean execPayCallback(long uid) {
        Promise<Boolean> callback = payCallback.get(uid);
        if (callback == null) { return false; }
        var reward = payRewards.get(callback.future());
        if (reward == null) { throw new IllegalStateException("PAYMENT_REWARD_CONTRACT_MISSING"); }
        reward.execute();
        return true;
    }

    /** @param uid 本次订单 @param failure 失败原因；null 仅允许在保存确认后使用 */
    public void confirmPayCallback(long uid, Throwable failure) {
        Promise<Boolean> callback = payCallback.remove(uid);
        if (callback == null) { return; }
        payRewards.remove(callback.future());
        if (failure == null) { callback.tryComplete(true); }
        else { callback.tryFail(failure); }
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
		builder.setLoginDay(PlayerHelper.getConditionCount(player, 3001));
	}
	@Override
	public void handleEvent(PlayerEvent event) {
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
				if (!reward.isEmpty()) {
					player.getGameClient().sendProtocol(RewardMsg.RewardPush_55000501.newBuilder().addAllRewards(reward));
				}
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
					if (asset.ID != Asset.VIPExp.ID && asset.ID != Asset.CatalogPoints.ID){ //VIP 初始 从 0 级开始
						expLevelMap.add(asset.ID, 1);
					}
				}
			}
		}
	}

	public void initLevel(Asset asset) {
		if (asset.Type == 2) {
			if (!expLevelMap.hasValue(asset.ID)) {
				expLevelMap.add(asset.ID, 1);
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
