package cn.game.games.net.game.module.develop.hero;

import cn.game.games.cache.entity.Hero;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.HeroMapper;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.ObjUtil;

public class HeroModule extends AbstractItemNoStackModule<Hero> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE };

	/** 当前使用的英雄id */
	private long heroUid;

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
		if (this.heroUid == 0) {
			this.heroUid = hero.getId();
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
}
