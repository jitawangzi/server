package cn.game.games.net.game.module.item;

import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

import cn.game.games.cache.entity.Item;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.DateUtil;

public class ExpireItemModule extends BasePlayerModule {
	/** 有过期时间的物品  key1 uid，key2 configId， value 过期时间(秒时间戳) */
	private MultiKeyMap<Long, Integer> expiredGoodsMap = new MultiKeyMap<Long, Integer>();
	@Override
	public EventTypeEnum[] getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

	public void addExpiredGoods(long uid, long configId, int expiredTime) {
		expiredGoodsMap.put(uid, configId, expiredTime);
		expiredGoodTask(uid, configId, expiredTime);
	}

	/** 
	 * 获取某个物品的过期时间（秒时间戳）
	 * @param item
	 * @return  0表示不过期。 
	 */
	public int getExpiredTime(Item item) {
		Integer time = expiredGoodsMap.get(item.getId(), (long) item.getConfigId());
		return time == null ? 0 : time;
	}

	private void expiredGoodTask(long uid, long configId, int expiredTime) {
		player.setTimerTask(expiredTime * 1000, r -> {
			player.getGoodsModule((int) configId).del(uid);
			// TODO 推送客户端？
		});
	}

	@Override
	public void initFromDbAfter() {
		int timeSeconds = DateUtil.currentTimeSeconds();
		Set<Entry<MultiKey<? extends Long>, Integer>> entrySet = expiredGoodsMap.entrySet();
		for (Entry<MultiKey<? extends Long>, Integer> entry : entrySet) {
			Integer expiredTime = entry.getValue();
			if (expiredTime >= timeSeconds) {
				player.getGoodsModule(entry.getKey().getKey(1).intValue()).del(entry.getKey().getKey(0));
			} else {
				expiredGoodTask(entry.getKey().getKey(0), entry.getKey().getKey(1), expiredTime);
			}
		}

	}

	@Override
	protected int getInitOrder() {
		return INIT_PRIORITY_LOW;
	}

}
