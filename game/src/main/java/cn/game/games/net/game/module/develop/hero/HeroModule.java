package cn.game.games.net.game.module.develop.hero;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.Item;
import cn.game.games.core.GameServerStatus;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.games.net.game.module.battle.BattleModule;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.games.net.game.module.player.IdConstant;
import cn.game.protocol.generated.config.DayCardConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.manager.DayCardManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.Rnd;

public class HeroModule extends AbstractItemNoStackModule<Hero> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay, EventTypeEnum.CostUidItem,
			EventTypeEnum.Hero, EventTypeEnum.HeroQuality };

	/** 当前使用的英雄id */
	@Deprecated
	private long heroUid;
	/** 上阵的英雄列表 */
	@Deprecated
	private Map<Long, Integer> battleHeros = new HashMap<Long, Integer>();

	/** 免费日租卡英雄id */
	private List<Long> freeDayHeros = new ArrayList<>();

	/** 当前选择使用的英雄uid */
	private long freeDayHeroUid;

	/** 领取过图鉴奖励的英雄id,领取到什么品质了 */
	@JsonIgnore
	@Deprecated
	private Map<Integer, Integer> illustrationsHeroQualitys = new HashMap<Integer, Integer>();

	/** 某个英雄id，达到的最大品质。 */
	@JsonIgnore
	@Deprecated
	private Map<Integer, Integer> illustrationsHeroQualitysMax = new HashMap<Integer, Integer>();

	/** 曾经拥有过的英雄id */
	private List<Integer> ownedHeroIds = new ArrayList<>();
	/**  */
	private Map<Integer, QualityStarObj> illustrationsHeroStars = new HashMap<Integer, QualityStarObj>();
	/** 领取过图鉴等级奖励的等级 */
	private int illustrationRewardLevel;
	
	/** key: 解锁了哪些怪物 value：解锁的怪物是否领取了奖励 */
	private Map<Integer, Boolean> illustrationsMonsterRewardMap = new HashMap<Integer, Boolean>();


	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {

		case PLAYER_CREATE: {
			Collection<Hero> list = list();
			// 初始英雄全上阵
			BattleModule battleModule = player.getBattleModule();
			List<String> collect = list.stream().map(hero -> hero.getId() + "").limit(8).collect(toList());
			battleModule.updateLineup(DungeonTypeEnum.BattleChapter.getId(), 0, collect);
			battleModule.updateLineup(DungeonTypeEnum.BattleChapter.getId(), 1, collect);
			battleModule.updateLineup(DungeonTypeEnum.BattleChapter.getId(), 2, collect);
//			int pos = 1;
//			for (Hero hero : list) {
//				battleHeros.put(hero.getId(), pos++);
//			}
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
		case CostUidItem: {
			int configId = event.getIntParameter(1);
			int goodsType = ItemHelper.getGoodsType(configId);
			if (goodsType == GoodsTypeEnum.Hero.getId()) {
				Collection<Hero> heros = getByConfigId(configId);
				if (heros.isEmpty()) {
					if (!ownedHeroIds.contains(configId)) {
						ownedHeroIds.add(configId);
					}
				}
			}
			break;
		}
		case Hero: {
			int configId = event.getIntParameter(0);
			if (ownedHeroIds.contains(configId)) {
				ownedHeroIds.remove(Integer.valueOf(configId));
			}
			break;
		}
		case HeroQuality: {
//			Hero hero = event.getParameter(0);
//			HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
//			illustrationsHeroStars.compute(hero.getConfigId(),
//					(k, v) -> v == null ? new Pair<Integer, Integer>(heroConfig.InitialQuality, 1)
//					: new Pair<Integer, Integer>(v.first, v.second + 1));
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
	public Object add(int itemId, int count, OpType opType) {
		if (count > 100) {
			throw new IllegalArgumentException("add hero , count > 100 : " + count);
		}
//		如果品质小于某品质： 
//		只是返回一个hero对象，并不真正加入到程序中。只是构建rewardInfo对象。  
//		同时转成货币，直接加上，push给客户端。 
		HeroConfig heroConfig = HeroManager.instance().get(itemId);
//		if (heroConfig.InitialQuality == 3) {
//			List<Hero> list = new ArrayList<>(1);
//			Hero hero = new Hero();
//			hero.setConfigId(itemId);
//			hero.setQuality(heroConfig.InitialQuality);
//			hero.setStar(1);
//			hero.setLevel(1);
//			hero.setPlayerId(playerId);
//			hero.setSkin(0);
//			list.add(hero);
//			PlayerHelper.addResources(player, GlobalConst.GachaConversion, OpType.GachaConversion, true);
//			return list;
//		}
		Collection<Hero> sameHeros = getByConfigId(itemId);
		if (!sameHeros.isEmpty()) {
			// 同英雄的转碎片id。
			return player.getItemModule()
					.add(heroConfig.Fragment, GlobalConst.HeroSynthesisDisassemble.get(heroConfig.InitialQuality) * count, opType);
		}

		Object list = super.add(itemId, count, opType);
		if (list instanceof List) {
			for (Item hero : (List<Item>) list) {
				if (hero instanceof Hero) {
					GameLogger.getHero(player, (Hero) hero, opType);
				}
			}

		}
		return list;
	}

	@Override
	public void setInstanceExt(Hero hero) {
//		ObjUtil.setDefaultValue(hero);
		if (this.heroUid == 0) {
			this.heroUid = hero.getId();
		}
		HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId()); 
//		hero.setStar(1);
		hero.setLevel(1);
		hero.setQuality(heroConfig.InitialQuality);
		hero.setSkin(heroConfig.HeroSkinID);
		player.getPlayerModule().addId(IdConstant.HERO_SKIN, heroConfig.HeroSkinID);

		player.fireAndHandleEvent(EventTypeEnum.Hero, heroConfig.ID);
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
		builder.putAllIllustrationsMonsterReward(illustrationsMonsterRewardMap); 
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

	public Map<Integer, QualityStarObj> getIllustrationsHeroStars() {
		return illustrationsHeroStars;
	}

	public int getIllustrationRewardLevel() {
		return illustrationRewardLevel;
	}

	public void setIllustrationRewardLevel(int illustrationRewardLevel) {
		this.illustrationRewardLevel = illustrationRewardLevel;
	}
	
	public Map<Integer, Boolean> getIllustrationsMonsterRewardMap() {
		return illustrationsMonsterRewardMap;
	}

	public int getHeroRateInAllHeros() {
		int allHeroCount = HeroManager.instance().list().size();
		if (allHeroCount == 0) {
			return 100;
		}
		return (int) (list().size() * 100L / allHeroCount);
	}

	@Override
	public void checkConfig(int id) {
		HeroManager.instance().get(id);
	}

}
