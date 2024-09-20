package cn.game.games.net.game.module.item;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.cache.entity.Item;
import cn.game.games.core.GoodsModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.ItemMapper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.currency.Currency;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BaseMsg.AssetInfo;
import cn.game.protocol.protobuf.BaseMsg.ItemInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.Rnd;

public class ItemModule extends AbstractItemModule<Item> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		}
	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		return new Class<?>[] { ItemMapper.class };
	}

	/**
	 * 增加一个道具数量
	 */
	@Override
	public Object add(int itemId, int count, OpType opType) {
		if (count <= 0) {
			return null;
		}
		ItemConfig itemConfig = ItemManager.instance().get(itemId);
		if (itemConfig.ItemType == 6) {
			// 给挂机金币
			ChapterModule chapterModule = player.getModule(ChapterModule.class);
			itemId = Asset.gold.ID ; 
			count *= chapterModule.calcPatrolGold(itemConfig.Para[0]);
			return player.getCurrencyModule().add(itemId, count, opType);
		} else if (itemConfig.ItemType == 5) {
			// 给经验
			ChapterModule chapterModule = player.getModule(ChapterModule.class);
			itemId = Asset.playerExp.ID;
			count *= chapterModule.calcPatrolExp(itemConfig.Para[0]);
			return player.getCurrencyModule().add(itemId, count, opType);
		} else if (itemConfig.ItemType == 7) {
			List<Object> ret = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				int item = Rnd.randomOne(itemConfig.Para);
				GoodsModule<? extends Item, ? extends Item> goodsModule = player.getGoodsModule(item);
				Object object = goodsModule.add(item, 1, opType);
				if (object instanceof List) {
					ret.addAll((List) object);
				} else {
					ret.add(object);
				}
			}
			return ret;
		} else if (itemConfig.ItemType == 8) {
			List<Object> ret = new ArrayList<>();
			for (int randomId : itemConfig.Para) {
				List<Goods> randomReward = PlayerHelper.randomReward(randomId);
				for (Goods goods : randomReward) {
					GoodsModule<? extends Item, ? extends Item> goodsModule = player.getGoodsModule(goods.getId());
					Object object = goodsModule.add(goods.getId(), goods.getCount(), opType);
					if (object instanceof List) {
						ret.addAll((List) object);
					} else {
						ret.add(object);
					}
				}
			}
			return ret;
		}
		return super.add(itemId, count, opType);
	}

//	@Override
//	protected void initFromDb(ListIterator<?> iterator) {
//		List<Item> list = (List<Item>) iterator.next();
//		for (Item item : list) {
//			initAddCache(item);
//		}
//	}
	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Item;
	}

	@Override
	public Item newInstance() {
		return new Item();
	}

	@Override
	public RewardInfo toRewardInfo(Item item) {
		if (item instanceof Currency) {
			return RewardInfo.newBuilder().setAsset(AssetInfo.newBuilder().setId(item.getConfigId()).setCount(item.getCount().intValue())).build();
		}
		return RewardInfo.newBuilder()
				.setItem(ItemInfo.newBuilder().setId(item.getConfigId()).setCount(item.getCount().intValue())).build();
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		for (Item item : list()) {
			builder.addItems(item.toItemInfo());
		}
	}

	@Override
	public void checkConfig(int id) {
		ItemManager.instance().get(id);
	}
}
