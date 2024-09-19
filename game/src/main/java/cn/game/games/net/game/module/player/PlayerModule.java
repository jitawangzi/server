package cn.game.games.net.game.module.player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.PlayerIds;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.PlayerIdsMapper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.develop.hero.HeroModule;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeadPortraitConfig;
import cn.game.protocol.generated.config.UserUpgradeConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.HeadPortraitManager;
import cn.game.protocol.generated.manager.UserUpgradeManager;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BaseMsg.GoodsInfo;
import cn.game.protocol.protobuf.PlayerMsg.CloudBoxInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.PlayerMsg.PlayerCloudBoxPush_01100040;
import cn.game.protocol.protobuf.RewardMsg;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
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
	};

	public boolean addId(int type, int configId) {
		return getOrCreateIdSet(type).add(configId);
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

	public boolean removeId(int type, int configId) {
//		Map<Integer, PlayerIds> map = getOrCreateIdMap(type);
//		PlayerIds playerIds = map.remove(configId);
//		if (playerIds != null) {
//			playerIds.delete();
//		}
		return getOrCreateIdSet(type).remove(configId);
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

	public boolean hasId(int type, int configId) {
		return getOrCreateIdSet(type).contains(configId);
	}

	public Set<Integer> getIdsSet(int type) {
		return getOrCreateIdSet(type);
	}

	public Set<Integer> getOrCreateIdSet(int type) {
		Set<Integer> set = idsSet.get(type);
		if (set == null) {
			set = new HashSet<Integer>();
			idsSet.put(type, set);
		}
		return set;
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

	public void execPayCallback(long uid) {
		Promise<Boolean> callback = this.payCallback.remove(uid); 
		callback.complete(true);; 
	}

	public int getLastChatTime() {
		return lastChatTime;
	}

	public void setLastChatTime(int lastChatTime) {
		this.lastChatTime = lastChatTime;
	}

	public void startCloudBoxTask() {
		int[] randomCLoud = GlobalConst.RandomCLoud;
		if (randomCLoud[0] == 1) {
			player.setPeriodicTask(randomCLoud[1]*1000, r -> {
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
		builder.addAllChapterPacks(getOrCreateIdSet(IdConstant.CHAPTER_PACK));
		
		if (cloudBox != null && !cloudBox.isEmpty()) {
			List<GoodsInfo> collect = cloudBox.stream().map(Goods::toGoodsInfo).collect(Collectors.toList());
			builder.setCloudBox(CloudBoxInfo.newBuilder().addAllItems(collect));
		}
		builder.setFirstLogin(isFirstLoign);
		isFirstLoign = false;
		builder.putAllGuide(guideMap);
		
		builder.addAllHeadboxs(getOrCreateIdSet(IdConstant.HEAD_BOX));
	}
	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		case LoginFinish: {
			PlayerManager.getInstance().online(playerId, ServerContext.getInstance().getServerId());
			initLevel();
			if (player.isFuncOpen(InitialUI.RandomBox)) {
				startCloudBoxTask();
			}
			break;
		}
		case PLAYER_CREATE: {
			PlayerManager.getInstance().online(playerId, ServerContext.getInstance().getServerId());
			// 初始化头像框
			List<HeadPortraitConfig> list = HeadPortraitManager.instance().list();
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
					expLevelMap.add(asset.ID, 1);
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
