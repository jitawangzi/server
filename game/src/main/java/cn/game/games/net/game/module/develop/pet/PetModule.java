package cn.game.games.net.game.module.develop.pet;

import java.util.HashMap;
import java.util.Map;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.item.AbstractItemModule;
import cn.game.protocol.generated.manager.SoulPetManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class PetModule extends AbstractItemModule<Pet> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay };

	/** 当前上阵的宠物id */
	private int battlePetId;
	/** 图鉴等级 */
	private Map<Integer, Integer> petBookMap = new HashMap<Integer, Integer>();

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}


	public void setBattlePetId(int battlePetId) {
		this.battlePetId = battlePetId;
	}

	public Map<Integer, Integer> getPetBookMap() {
		return petBookMap;
	}

	public int getBattlePetId() {
		return battlePetId;
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {

		case PLAYER_CREATE: {
			break;
		}
		case NewDay: {
			break;
		}
		}
	}

	@Override
	public void checkConfig(int id) {
		SoulPetManager.instance().get(id);
	}

	@Override
	public Pet newInstance() {
		return new Pet();
	}

	@Override
	public RewardInfo toRewardInfo(Pet reward) {
		return RewardInfo.newBuilder().setPet(reward.toPetInfo()).build();
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Pet;
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		for (Pet pet : list()) {
			builder.addPets(pet.toPetInfo());
		}
		builder.putAllPetBook(petBookMap);
		builder.setPetBattleId(battlePetId);

	}

}
