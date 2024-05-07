package cn.game.games.net.game.module.player;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
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
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.UserUpgradeConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.UserUpgradeManager;
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
 * 零散、简单的一些数据，都可以放这里
 * 2024年3月19日 下午6:38:13
 * @author SYQ
 */
public class PlayerModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE,
			EventTypeEnum.LoginFinish, EventTypeEnum.Reconnect, EventTypeEnum.LevelUp, EventTypeEnum.ResourceRemove };

	/** 玩家拥有的各种id集合，通常是只增加新id，并且id不能重复。 key1:type ,key2:configId*/
	private Map<Integer, Map<Integer, PlayerIds>> idsMap = new HashMap<Integer, Map<Integer, PlayerIds>>();
	/** 等级数据，key: {@link Asset} 这里是经验升的等级*/
	private IntMapWrapper expLevelMap = new IntMapWrapper();
	/** 炼金等级 */
	private IntMapWrapper alchemysMap = new IntMapWrapper();
	/** 支付成功后的回调 */
	@JsonIgnore
	private Map<Long, Promise<Boolean>> payCallback = new HashMap<Long, Promise<Boolean>>() ; 
	
	/** 随机宝箱，小云宝箱 */
	private List<Goods> cloudBox;
	private int lastCloudBoxRewardTime;
	

	@Override
	public Class<?>[] defaultDbMapperClass() {
		return new Class[] { PlayerIdsMapper.class };
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		List<PlayerIds> ids = (List<PlayerIds>) iterator.next();
		for (PlayerIds playerIds : ids) {
			idsMap.computeIfAbsent(playerIds.getType(), key -> new HashMap<>()).put(playerIds.getConfigId(), playerIds);
		}
	}
	@Override
	public void initFromDbAfter() {
	};
	public void addId(int type, int configId) {
		Map<Integer, PlayerIds> map = getOrCreateIdMap(type);
		if (map.containsKey(configId)) {
			return;
		}
		PlayerIds add = new PlayerIds();
		add.setPlayerId(playerId);
		add.setType(type);
		add.setConfigId(configId);
		add.setCreateTime(new Date());
		add.setUpdateTime(System.currentTimeMillis());
		add.insert();
		map.put(add.getConfigId(), add);
	}

	public void removeId(int type, int configId) {
		Map<Integer, PlayerIds> map = getOrCreateIdMap(type);
		PlayerIds playerIds = map.remove(configId);
		if (playerIds != null) {
			playerIds.delete();
		}
	}

	public void updateTime(int type, int configId) {
		PlayerIds ids = getIds(type, configId);
		if (ids != null) {
			ids.setUpdateTime(System.currentTimeMillis());
			ids.update();
		} else {
			addId(type, configId);
		}
	}

	public boolean hasId(int type, int configId) {
		Map<Integer, PlayerIds> map = getOrCreateIdMap(type);
		return map.containsKey(configId);
	}

	public Set<Integer> getIdsSet(int type) {
		Map<Integer, PlayerIds> map = getOrCreateIdMap(type);
		return map.keySet();
	}

	public PlayerIds getIds(int type, int configId) {
		Map<Integer, PlayerIds> map = getOrCreateIdMap(type);
		return map.get(configId);
	}

	public Collection<PlayerIds> getIds(int type) {
		Map<Integer, PlayerIds> map = getOrCreateIdMap(type);
		return map.values();
	}

	public Map<Integer, PlayerIds> getOrCreateIdMap(int type) {
		Map<Integer, PlayerIds> map = idsMap.get(type);
		if (map == null) {
			map = new HashMap<Integer, PlayerIds>();
			idsMap.put(type, map);
		}
		return map;
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
	
	public void execPayCallback(long uid) {
		Promise<Boolean> callback = this.payCallback.remove(uid); 
		callback.complete(true);; 
	}

	public void startCloudBoxTask() {
		int[] randomCLoud = GlobalConst.RandomCLoud;
		if (randomCLoud[0] == 1) {
			player.setPeriodicTask(randomCLoud[1]*1000, r -> {
				if (cloudBox != null && !cloudBox.isEmpty()) {
					return;
				}
				if (lastCloudBoxRewardTime != 0 && DateUtil.currentTimeSeconds() - lastCloudBoxRewardTime < randomCLoud[1]) {
					return;
				}
				if (Rnd.hit(randomCLoud[2])) {
					cloudBox = PlayerHelper.randomReward(player, randomCLoud[3]);
					List<GoodsInfo> collect = cloudBox.stream().map(Goods::toGoodsInfo).collect(Collectors.toList());
					player.getGameClient().sendProtocol(PlayerCloudBoxPush_01100040.newBuilder().setCloudBox(CloudBoxInfo.newBuilder().addAllItems(collect)));
				}
			});
		}
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		builder.setPlayer(player.toProto());
		builder.putAllLevels(expLevelMap.getMap());
		builder.setLastPatrolRewardTime(lastCloudBoxRewardTime);

		// 礼包
		Map<Integer, PlayerIds> map = idsMap.get(IdConstant.SHOP_GIFT);
		if (map != null) {
			builder.addAllShopGift(map.keySet());
		}
		
		if (cloudBox != null && !cloudBox.isEmpty()) {
			List<GoodsInfo> collect = cloudBox.stream().map(Goods::toGoodsInfo).collect(Collectors.toList());
			builder.setCloudBox(CloudBoxInfo.newBuilder().addAllItems(collect));
		}
	}
	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		case LoginFinish: {
			PlayerManager.getInstance().online(playerId, ServerContext.getInstance().getServerId());
			if (player.isFuncOpen(InitialUI.RandomBox)) {
				startCloudBoxTask();
			}
			break;
		}
		case PLAYER_CREATE: {
			PlayerManager.getInstance().online(playerId, ServerContext.getInstance().getServerId());
			expLevelMap.add(Asset.playerExp.ID, 1);
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
				List<RewardInfo> reward = PlayerHelper.addReward(player, userUpgradeConfig.LvRewardID);
				player.getGameClient().sendProtocol(RewardMsg.RewardPush_55000501.newBuilder().addAllRewards(reward));
			}

			if (level == InitialUI.RandomBox.DisplayLevel) {
				startCloudBoxTask();
			}
			break;
		}
		case ResourceRemove: {
			int id = event.getIntParameter(0);
			expLevelMap.removeValue(id);
			break;
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
