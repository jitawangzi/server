package cn.game.games.net.game.module.develop.hchero;

import java.util.Collection;
import java.util.List;

import cn.game.games.cache.entity.HCHero;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.protocol.generated.config.HCHeroConfig;
import cn.game.protocol.generated.manager.HCHeroManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.IntMapWrapper;
import cn.game.util.ObjUtil;

public class HCHeroModule extends AbstractItemNoStackModule<HCHero> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay };

	/** 当前使用的英雄id */
	private long heroUid;
	private int freeHcHeroUpTimes;
	private int freeHcHeroItemTimes;

	private IntMapWrapper heroItemTimesMap = new IntMapWrapper();

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {

		case PLAYER_CREATE: {
			Collection<HCHero> list = list();
			// 初始英雄全上阵
			int pos = 1;
			for (HCHero hero : list) {
				heroUid = hero.getId();
			}
			break;
		}
		case NewDay: {
			freeHcHeroUpTimes = 0;
			freeHcHeroItemTimes = 0;
			heroItemTimesMap.clear();
			break;
		}
		}
	}



//	@Override
//	public Class<?>[] defaultDbMapperClass() {
//		return new Class<?>[] { HCHeroMapper.class };
//	}

	@Override
	public List<HCHero> add(int itemId, int count, OpType opType) {
		List<HCHero> list = super.add(itemId, count, opType);
		
//		for (HCHero hero : list) {
//			GameLogger.getHCHero(player, hero, opType);
//		}
		return list;
	}

	@Override
	public void setInstanceAfter(HCHero hero) {
		ObjUtil.setDefaultValue(hero);
		if (this.heroUid == 0) {
			this.heroUid = hero.getId();
		}
		HCHeroConfig heroConfig = HCHeroManager.instance().get(hero.getConfigId());
		hero.setStar(1);
		hero.setLevel(1);
//		hero.setQuality(heroConfig.InitialQuality);

		// 拥有新英雄，奖励固定元宝
//		PlayerHelper.addResources(player, Asset.gold.ID, GlobalConst.HCHeroBookAward, OpType.NewHCHeroReward);
		player.handleEvent(EventTypeEnum.HCHero, heroConfig.ID);
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.HCHero;
	}

	@Override
	public RewardInfo toRewardInfo(HCHero hero) {
		return RewardInfo.newBuilder().setHcHero(hero.toHCHeroInfo()).build();
	}

	@Override
	public HCHero newInstance() {
		return new HCHero();
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		builder.setFreeHcHeroItemTimes(freeHcHeroItemTimes);
		builder.setFreeHcHeroUpTimes(freeHcHeroUpTimes);
		builder.setCurHcHeroUid(heroUid + "");
		Collection<HCHero> list = list();
		for (HCHero hcHero : list) {
			builder.addHcHeros(hcHero.toHCHeroInfo());
		}
		builder.putAllFreeHcHeroItemTimesMap(heroItemTimesMap.getMap());
	}

	public long getHCHeroId() {
		return heroUid;
	}

	public void setHCHeroId(long heroId) {
		this.heroUid = heroId;
	}

	public HCHero getCurHCHero() {
		return get(heroUid);
	}

	public int getFreeHcHeroUpTimes() {
		return freeHcHeroUpTimes;
	}

	public void setFreeHcHeroUpTimes(int freeHcHeroUpTimes) {
		this.freeHcHeroUpTimes = freeHcHeroUpTimes;
	}

	public int getFreeHcHeroItemTimes() {
		return freeHcHeroItemTimes;
	}

	public void setFreeHcHeroItemTimes(int freeHcHeroItemTimes) {
		this.freeHcHeroItemTimes = freeHcHeroItemTimes;
	}

	public IntMapWrapper getHeroItemTimesMap() {
		return heroItemTimesMap;
	}

	@Override
	public void checkConfig(int id) {
		HCHeroManager.instance().get(id);
	}
}
