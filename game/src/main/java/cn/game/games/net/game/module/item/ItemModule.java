package cn.game.games.net.game.module.item;

import java.util.List;
import java.util.ListIterator;

import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.Item;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.ItemMapper;
import cn.game.protocol.generated.enume.GoodsTypeEnum;
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

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		List<Item> list = (List<Item>) iterator.next();
		for (Item item : list) {
			initAddCache(item);
		}
	}
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
