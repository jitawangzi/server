package cn.game.games.net.game.module.develop.hero;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.game.games.cache.entity.Hero;
import cn.game.games.core.GameServerStatus;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.protocol.generated.config.DayCardConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.DayCardManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.ObjUtil;
import cn.game.util.Rnd;

public class HeroModule extends AbstractItemNoStackModule<Hero> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay };

	/** 当前使用的英雄id */
	private long heroUid;
	/** 上阵的英雄列表 */
	private Set<Long> battleHeros = new HashSet<Long>();

	/** 免费日租卡英雄id */
	private List<Long> freeDayHeros = new ArrayList<>();

	/** 当前选择使用的英雄uid */
	private long freeDayHeroUid;

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
		case NewDay: {
			if (!freeDayHeros.isEmpty()) {
				for (Long uid : freeDayHeros) {
					del(uid, OpType.FreeHeroDayRent);
				}
				freeDayHeros.clear();
				refreshFreeDayHero();
				freeDayHeroUid = 0;
			}
			break;
		}
		}
	}

	/** 
	 * 刷新免费日租卡。
	 */
	public void refreshFreeDayHero() {
		int days = GameServerStatus.getInstance().getOpenDays();
		DayCardConfig dayCard = DayCardManager.instance().getNullable(days);
		if (dayCard == null) {
			dayCard = DayCardManager.instance().get(999);
		}
		int[] randomSubArray = Rnd.randomSubArray(dayCard.Hero, 3); 
		for (int i : randomSubArray) {
			List<Hero> list = (List<Hero>) add(i, OpType.FreeHeroDayRent);
			Hero hero = list.get(0);
			hero.setLevel(dayCard.Lv);
			hero.setQuality(dayCard.InitialQuality[0]);
			hero.setStar(dayCard.InitialQuality[1]);
			freeDayHeros.add(hero.getId());
		}
	}


//	@Override
//	public Class<?>[] defaultDbMapperClass() {
//		return new Class<?>[] { HeroMapper.class };
//	}

	@Override
	public List<Hero> add(int itemId, int count, OpType opType) {
		List<Hero> list = super.add(itemId, count, opType);
		
		for (Hero hero : list) {
			GameLogger.getHero(player, hero, opType);
		}
		return list;
	}

	@Override
	public void setInstanceAfter(Hero hero) {
		ObjUtil.setDefaultValue(hero);
		if (this.heroUid == 0) {
			this.heroUid = hero.getId();
		}
		HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId()); 
		hero.setStar(1);
		hero.setLevel(1);
		hero.setQuality(heroConfig.InitialQuality);

		// 拥有新英雄，奖励固定元宝
		PlayerHelper.addResources(player, Asset.gold.ID, GlobalConst.HeroBookAward, OpType.NewHeroReward);
		player.handleEvent(EventTypeEnum.Hero, heroConfig.ID);
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

	public boolean isInBattle(long uid) {
		return battleHeros.contains(uid);
	}

	public Set<Long> getBattleHeros() {
		return battleHeros;
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

}
