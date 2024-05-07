package cn.game.games.net.game.module.item;

import cn.game.games.cache.entity.Item;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.ItemMapper;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.protobuf.BaseMsg.ItemInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

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
	public Item add(int itemId, int count) {
		if (count <= 0) {
			return null;
		}
		ItemConfig itemConfig = ItemManager.instance().get(itemId);
		if (itemConfig.ItemType == 6) {
			// TODO 给挂机金币
			ChapterModule chapterModule = player.getModule(ChapterModule.class);
			itemId = Asset.gold.ID ; 
			count = chapterModule.calcPatrolGold(itemConfig.Para);
			return player.getCurrencyModule().add(itemId, count);
		}
		return super.add(itemId, count);
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
		return RewardInfo.newBuilder()
				.setItem(ItemInfo.newBuilder().setId(item.getConfigId()).setCount(item.getCount().intValue())).build();
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		for (Item item : list()) {
			builder.addItems(item.toProto());
		}
	}
}
