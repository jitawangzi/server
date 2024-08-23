package cn.game.games.net.game.module.develop.pet;

import cn.game.games.cache.entity.Item;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.item.AbstractItemModule;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class PetModule extends AbstractItemModule<Pet> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay };

	/** 当前上阵的宠物id */
	private int battlePetId;

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {

		case PLAYER_CREATE: {
		}
		case NewDay: {
			break;
		}
		}
	}

	@Override
	public void checkConfig(int id) {
		// TODO Auto-generated method stub

	}

	@Override
	public Item newInstance() {
		return new Pet();
	}

	@Override
	public RewardInfo toRewardInfo(Pet reward) {
		return null;
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Pet;
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

}
