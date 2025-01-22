package cn.game.games.net.game.module.draw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.chat.ChatHelper;
import cn.game.games.net.game.module.develop.hero.HeroHelper;
import cn.game.protocol.generated.config.DrawConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.GuidanceRandomGroupConfig;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.MarqueeConfig;
import cn.game.protocol.generated.config.SupremeRandomGroupConfig;
import cn.game.protocol.generated.manager.DrawManager;
import cn.game.protocol.generated.manager.GuidanceRandomGroupManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.generated.manager.MarqueeManager;
import cn.game.protocol.generated.manager.SupremeRandomGroupManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.DrawMsg.DrawInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;
import cn.game.util.IntMapWrapper;
import cn.game.util.Rnd;

public class DrawModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.NewDay };
	/** 当前第几档必送神将,key：GiftCardConfig 表id */
	@Deprecated
	private IntMapWrapper giftIndex = new IntMapWrapper();
	/** 当前档d位的抽卡次数,key：GiftCardConfig 表id */
	@Deprecated
	private IntMapWrapper drawTimes = new IntMapWrapper();
	/** 免费单抽的时间 key：DrawConfig 表id */
	private IntMapWrapper freeDrawTime = new IntMapWrapper();
	/** 免费单抽的次数 key：DrawConfig 表id */
	private IntMapWrapper freeDrawCount = new IntMapWrapper();
	private boolean isFirstTen = true;
	/** 新手期抽卡次数 */
	private int guidanceDrawCount;
	/** 至尊抽卡,新手卡池，已经抽过的组  */
	private List<Integer> guidanceGroupList = new ArrayList<Integer>();
	/** 至尊抽卡,新手卡池，当前没有抽完的组 */
	private int curGuidanceGroup;
	/** 至尊抽卡,新手卡池，当前没有抽完的组,抽到哪个位置了 */
	private int curGuidanceGroupIndex = -1;

	/** 至尊抽卡，已经抽过的索引、下标 SupremeRandomGroupConfig */
//	@Deprecated
//	@JsonIgnore
	private List<Integer> supremeRandomGroupList = new ArrayList<Integer>();

	/** key：draw表id，value : 已经抽过的索引、下标 SupremeRandomGroupConfig */
	private Map<Integer, List<Integer>> supremeRandomGroupMap = new HashMap<>();

	private int wishHeroId;
	private int wishHeroTimes = GlobalConst.OrientationFrequency;

	@Override
	public EventTypeEnum[] getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {

		case NewDay: {
			freeDrawCount.clear();
			break;
		}
		}
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

	public int getNextFreeTime(int id) {
		int freeTime = freeDrawTime.getValue(id);
		if (freeTime == 0) {
			return 0;
		}
		int nextTime = (int) (freeTime + DateUtil.DAY_SECONDS - DateUtil.currentTimeSeconds());
		return nextTime < 0 ? 0 : nextTime;
	}

	public DrawInfo buildDrawInfo(int id) {
		cn.game.protocol.protobuf.DrawMsg.DrawInfo.Builder newBuilder = DrawInfo.newBuilder();
		newBuilder.setNextFreeTime(getNextFreeTime(id));
		/*		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
				Map<Integer, Integer> mapMax = new HashMap<Integer, Integer>();
				List<GiftCardConfig> drawIdList = GiftCardManager.instance().getDrawIdList(id);
				for (GiftCardConfig giftCardConfig : drawIdList) {
					int curTimes = drawTimes.getValue(giftCardConfig.ID);
					int maxTimes = giftCardConfig.GiftCardCount[giftIndex.getValue(giftCardConfig.ID)];
					mapMax.put(giftCardConfig.GiftCardQuality, maxTimes);
					map.put(giftCardConfig.GiftCardQuality, maxTimes - curTimes);
				}
		
				newBuilder.putAllGiftRemainingTimes(map);
				newBuilder.putAllGiftMaxTimes(mapMax);*/
		newBuilder.setWishHeroId(wishHeroId);
		newBuilder.setWishHeroTimes(wishHeroTimes);
		newBuilder.setFreeDrawCount(freeDrawCount.getValue(id));
		return newBuilder.build();
	}

	/** 
	 * 抽卡 
	 * @param id
	 * @param count
	 * @return  英雄奖励
	 */
	public List<List<RewardInfo>> draw(int id, int count, boolean freeOnce) {
		List<List<RewardInfo>> allRewards = new ArrayList<>();
//		List<GiftCardConfig> giftCardList = GiftCardManager.instance().getDrawIdList(id);

		List<RewardInfo> ret = new ArrayList<>();
		List<RewardInfo> giftList = new ArrayList<>();

		allRewards.add(ret);
		allRewards.add(giftList);
		MarqueeConfig marqueeConfig = MarqueeManager.instance().get(HeroHelper.getMarqueeId(count));
		int quality = marqueeConfig.Para;
		List<String> heroNames = new ArrayList<>();

		DrawConfig drawConfig = DrawManager.instance().get(id);
		if (id == 2) { // 至尊抽卡走特殊逻辑。首次十连给指定的卡，接下来走特殊卡池
			Map<Integer, List<GuidanceRandomGroupConfig>> randomParameterGroupIds = GuidanceRandomGroupManager.instance().getRandomParameterGroupIds();
			int guidanceDrawMax = 0;
			Iterator<Entry<Integer, List<GuidanceRandomGroupConfig>>> iterator = randomParameterGroupIds.entrySet().iterator(); 
			while (iterator.hasNext()) {
				Map.Entry<java.lang.Integer, java.util.List<cn.game.protocol.generated.config.GuidanceRandomGroupConfig>> entry = (Map.Entry<java.lang.Integer, java.util.List<cn.game.protocol.generated.config.GuidanceRandomGroupConfig>>) iterator
						.next();
				guidanceDrawMax += entry.getValue().size();
			}
			
			Set<Integer> guidanceKeySet = randomParameterGroupIds.keySet();
			if (count == 10 && isFirstTen) {
				for (int i = 0; i < count; i++) {
					List<Goods> reward = drawRewards(GlobalConst.FirstMandatoryDraw[i]);
					heroNames.addAll(getMarqueeHeroNames(reward, quality));
					ret.addAll(PlayerHelper.addResources(player, reward, OpType.Draw));
				}
				isFirstTen = false;
			} else {
				for (int i = 0; i < count; i++) {
					// 新手期
					if (guidanceDrawCount < guidanceDrawMax) {
						GuidanceRandomGroupConfig guidanceRandomGroupConfig = null;
						if (curGuidanceGroup == 0) {
							int group = Rnd.randomElementExcluded(guidanceKeySet, guidanceGroupList);
							curGuidanceGroup = group;
							curGuidanceGroupIndex = 0;
							List<GuidanceRandomGroupConfig> curList = randomParameterGroupIds.get(curGuidanceGroup);
							guidanceRandomGroupConfig = curList.get(curGuidanceGroupIndex);
						} else {
							curGuidanceGroupIndex++;
							List<GuidanceRandomGroupConfig> curList = randomParameterGroupIds.get(curGuidanceGroup);
							guidanceRandomGroupConfig = curList.get(curGuidanceGroupIndex);
							if (curGuidanceGroupIndex == curList.size() - 1) {
								guidanceGroupList.add(curGuidanceGroup);
								curGuidanceGroup = 0;
								curGuidanceGroupIndex = -1;
							}
						}
						List<Goods> reward = drawRewards(guidanceRandomGroupConfig.GivenID);
						heroNames.addAll(getMarqueeHeroNames(reward, quality));
						ret.addAll(PlayerHelper.addResources(player, reward, OpType.Draw));
						guidanceDrawCount++;
					} else {
						List<Goods> reward = supremeDraw(id);
						heroNames.addAll(getMarqueeHeroNames(reward, quality));
						ret.addAll(PlayerHelper.addResources(player, reward, OpType.Draw));
					}

				}
			}
		} else if (id == 201 || id == 301) {
			for (int i = 0; i < count; i++) {
				List<Goods> reward = supremeDraw(id);
				heroNames.addAll(getMarqueeHeroNames(reward, quality));
				ret.addAll(PlayerHelper.addResources(player, reward, OpType.Draw));
			}
		} else {
			for (int i = 0; i < count; i++) {
				List<Goods> drawRewards = drawRewards(drawConfig.DrawRandomId);
				heroNames.addAll(getMarqueeHeroNames(drawRewards, quality));
				ret.addAll(PlayerHelper.addResources(player, drawRewards, OpType.Draw));
			}
		}

		for (int[] money : drawConfig.DrawMoney) {
			PlayerHelper.addResources(player, money[0], money[1] * count, OpType.Draw);
		}
		if (freeOnce) {
			freeDrawTime.setValue(id, DateUtil.currentTimeSeconds());
			freeDrawCount.add(id, 1);
		}
		if (!heroNames.isEmpty()) {
			String marqueeText = ChatHelper.getHeroMarqueeText(player.getData().getName(), heroNames, count);
			ChatHelper.marquee(marqueeText, player.getServerId());
		}

		return allRewards;
	}


	private List<String> getMarqueeHeroNames(List<Goods> reward, int quality) {
		List<String> heroNames = new ArrayList<>();
		for (Goods goods : reward) {
			int hid = goods.getId();
			if (ItemHelper.getGoodsType(hid) == GoodsTypeEnum.Hero.getId()) {
				HeroConfig heroConfig = HeroManager.instance().get(hid);
				if (heroConfig.InitialQuality >= quality) {
					heroNames.add(heroConfig.name);
				}
			}
		}
		return heroNames;
	}

	private List<Goods> wishHeroRewards(int randomDropId) {

		List<Goods> randomReward = PlayerHelper.randomReward(randomDropId);
		if (wishHeroId == 0) {
			return randomReward;
		}

		// 检查里面有几个非心愿红卡
		int redHeroCount = 0;
		for (Goods goods : randomReward) {
			int hid = goods.getId();
			if (ItemHelper.getGoodsType(hid) == GoodsTypeEnum.Hero.getId()) {
				if (hid == wishHeroId) {
					wishHeroTimes = GlobalConst.OrientationFrequency;
				} else {
					HeroConfig heroConfig = HeroManager.instance().get(hid);
					if (heroConfig.InitialQuality == 6) {
						redHeroCount++;
					}
				}
			}
		}
		if (wishHeroTimes - redHeroCount <= 0) {
			wishHeroTimes = GlobalConst.OrientationFrequency - Math.abs(wishHeroTimes - redHeroCount);
			// 换心愿卡
			for (Goods goods : randomReward) {
				if (goods.getId() != wishHeroId) {
					goods.setId(wishHeroId);
					break;
				}
			}
		} else {
			wishHeroTimes -= redHeroCount;
		}
		return randomReward;
	}

	private List<Goods> drawRewards(int randomDropId) {
		List<Goods> wishHeroRewards = wishHeroRewards(randomDropId);
		return wishHeroRewards;
	}

	/** 
	 * 保底抽卡掉落方式
	 * @param id  draw表id
	 * @return
	 */
	private List<Goods> supremeDraw(int id) {
		List<SupremeRandomGroupConfig> list = SupremeRandomGroupManager.instance().getDrawIdList(id);

		List<Integer> supremeRandomGroupList = supremeRandomGroupMap.computeIfAbsent(id, k -> new ArrayList<>());

		int randomWeighableIndex = Rnd.randomWeighableIndexExcludeIndex(list, supremeRandomGroupList);
		SupremeRandomGroupConfig supremeRandomGroupConfig = list.get(randomWeighableIndex);
		if (supremeRandomGroupConfig.SupremeRandomParameterTarget) {
			supremeRandomGroupList.clear();
		} else {
			supremeRandomGroupList.add(randomWeighableIndex);
			if (supremeRandomGroupList.size() >= list.size()) {
				supremeRandomGroupList.clear();
			}
		}
		return drawRewards(supremeRandomGroupConfig.GivenID);
	}

	public int getWishHeroId() {
		return wishHeroId;
	}

	public void setWishHeroId(int wishHeroId) {
		this.wishHeroId = wishHeroId;
	}

}
