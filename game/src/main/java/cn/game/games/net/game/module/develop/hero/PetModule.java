package cn.game.games.net.game.module.develop.hero;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cn.game.games.cache.entity.Hero;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.develop.pet.Pet;
import cn.game.games.net.game.module.item.AbstractItemModule;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class PetModule extends AbstractItemModule<Pet> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay, EventTypeEnum.CostUidItem,
			EventTypeEnum.Hero, EventTypeEnum.HeroQuality };

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
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Hero;
	}

	@Override
	public RewardInfo toRewardInfo(Hero hero) {
		return RewardInfo.newBuilder().setRole(hero.toHeroInfo()).build();
	}

	@Override
	public Hero newInstance() {
		return new Hero();
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		for (Hero hero : list()) {
			builder.addHeros(hero.toHeroInfo());
		}
		for (Long uid : freeDayHeros) {
			builder.addFreeDayRentHeros(uid.toString());
		}
		builder.setFreeDayRentHeroUid(freeDayHeroUid + "");
	}

	public long getHeroId() {
		return heroUid;
	}

	public void setHeroId(long heroId) {
		this.heroUid = heroId;
	}

	public Hero getCurHero() {
		return get(heroUid);
	}


	public Map<Long, Integer> getBattleHeros() {
		return battleHeros;
	}

	public List<Hero> getBattleHeroList() {
		List<Hero> list = new ArrayList<>();
		for (Entry<Long, Integer> entry : battleHeros.entrySet()) {
			list.add(get(entry.getKey()));
		}
		return list;
	}

	public Set<Long> getBattleHeroIds() {
		return battleHeros.keySet();
	}

	public int getBattleHeroPos(long id) {
		Integer pos = battleHeros.get(id);
		return pos == null ? 0 : pos;
	}

	public List<Long> getFreeDayHeros() {
		return freeDayHeros;
	}

	public long getFreeDayHeroUid() {
		return freeDayHeroUid;
	}

	public void setFreeDayHeroUid(long freeDayHeroUid) {
		this.freeDayHeroUid = freeDayHeroUid;
	}

	public Map<Integer, Integer> getIllustrationsHeroQualitys() {
		return illustrationsHeroQualitys;
	}

	public List<Integer> getOwnedHeroIds() {
		return ownedHeroIds;
	}

	public Map<Integer, Integer> getIllustrationsHeroQualitysMax() {
		return illustrationsHeroQualitysMax;
	}

	@Override
	public void checkConfig(int id) {
		HeroManager.instance().get(id);
	}

}
