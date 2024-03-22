package cn.game.games.net.game.module.hero;

import java.util.List;
import java.util.ListIterator;

import cn.game.games.cache.entity.Hero;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.HeroMapper;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.protocol.generated.enume.GoodsTypeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.ObjUtil;

public class HeroModule extends AbstractItemNoStackModule<Hero> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE };


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
		return new Class<?>[] { HeroMapper.class };
	}

	@Override
	public void setInstanceAfter(Hero hero) {
		ObjUtil.setDefaultValue(hero);
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		List<Hero> list = (List<Hero>) iterator.next();
		for (Hero hero : list) {
			initAddCache(hero);
		}
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Hero;
	}

	@Override
	public RewardInfo toRewardInfo(Hero hero) {
		return RewardInfo.newBuilder().build();
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
	}
}
