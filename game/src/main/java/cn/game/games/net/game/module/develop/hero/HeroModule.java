package cn.game.games.net.game.module.develop.hero;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import cn.game.games.cache.entity.Hero;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.ObjUtil;

public class HeroModule extends AbstractItemNoStackModule<Hero> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE };

	/** 当前使用的英雄id */
	private long heroUid;
	/** 上阵的英雄列表 */
	private Set<Long> battleHeros = new HashSet<Long>();

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		
		case PLAYER_CREATE: {
			Collection<Hero> list = list(); 
			// 初始英雄全上阵
			for (Hero hero : list) {
//				hero.setBattle(true);
				battleHeros.add(hero.getId());
			}
			break;
			}
		}
	}

	// 给英雄这里，把数量当成品质来用。
	/*	@Override
		public List<Hero> add(int itemId, int count) {
			if (count <= 0) {
				return null;
			}
			ItemHelper.checkConfig(itemId);
			// TODO 检查id，是不是存在，涉及到多个表。
			List<Hero> ret = new ArrayList<Hero>();
			Hero item = newInstance();
			setInstance(item, itemId, 1);
			setInstanceAfter(item);
			initAddCache(item);
			item.insert();
			ret.add(item);
			return ret;
		}*/

//	@Override
//	public Class<?>[] defaultDbMapperClass() {
//		return new Class<?>[] { HeroMapper.class };
//	}

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

	public boolean isInBattle(long uid) {
		return battleHeros.contains(uid);
	}

	public Set<Long> getBattleHeros() {
		return battleHeros;
	}

}
