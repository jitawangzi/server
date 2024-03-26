package cn.game.games.net.game.module.player;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.PlayerIds;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.PlayerIdsMapper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.enume.Money;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import io.vertx.core.Promise;

public class PlayerModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE,
			EventTypeEnum.LoginFinish, EventTypeEnum.Reconnect, EventTypeEnum.ResourceRemove };

	/** 玩家拥有的各种id集合，通常是只增加新id，并且id不能重复。 key1:type ,key2:configId*/
	private Map<Integer, Map<Integer, PlayerIds>> idsMap = new HashMap<Integer, Map<Integer, PlayerIds>>();
	/** 支付成功后的回调 */
	private Map<Long, Promise<Boolean>> payCallback = new HashMap<Long, Promise<Boolean>>() ; 
	
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

	public Map<Integer, PlayerIds> getOrCreateIdMap(int type) {
		Map<Integer, PlayerIds> map = idsMap.get(type);
		if (map == null) {
			map = new HashMap<Integer, PlayerIds>();
			idsMap.put(type, map);
		}
		return map;
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

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		builder.setPlayer(player.toProto());

		// 礼包
		Map<Integer, PlayerIds> map = idsMap.get(IdConstant.SHOP_GIFT);
		if (map != null) {
			builder.addAllShopGift(map.keySet());
		}
	}
	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		case LoginFinish: {
			PlayerManager.getInstance().online(playerId, ServerContext.getInstance().getServerId());
			break;
		}
		case PLAYER_CREATE: {

			PlayerManager.getInstance().online(playerId, ServerContext.getInstance().getServerId());
			break;
		}
		case Reconnect: {
			player.setActive(true);
			break;
		}
		case ResourceRemove: {
			int id = event.getIntParameter(0);
			player.getLevelMap().removeValue(id);
			break;
		}
		}
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

}
