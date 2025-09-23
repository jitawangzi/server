package cn.game.games.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.game.games.cache.entity.Item;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.DateUtil;

/**    
 * 处理带过期时间的物品
 * 2025年8月20日 11:07:28
 * @author SYQ
 */
public class ItemExpiredModule extends BasePlayerModule {

	@Override
	public void onLogin() {
		Collection<GoodsModule<? extends Item>> allGoodsModule = player.getAllGoodsModule();
		int now = DateUtil.currentTimeSeconds();
		List<Item> toRemove = new ArrayList<>();
		for (GoodsModule<? extends Item> goodsModule : allGoodsModule) {
			for (Item item : goodsModule.list()) {
				if (item.getExpiredTime() > 0) {
					if (item.getExpiredTime() <= now) {
						toRemove.add(item);
					} else {
						player.setTimerTask((item.getExpiredTime() - now) * 1000, r -> {
							goodsModule.del(item, OpType.Expired);
						});
					}
				}
			}
		}
		for (Item item : toRemove) {
			GoodsModule<? extends Item> goodsModule = player.getGoodsModule(item.getConfigId());
			goodsModule.del(item, OpType.Expired);
		}
	}

	@Override
	protected int getInitOrder() {
		return INIT_PRIORITY_LOW;
	}

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
}
