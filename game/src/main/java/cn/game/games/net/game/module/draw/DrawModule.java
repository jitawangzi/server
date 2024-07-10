package cn.game.games.net.game.module.draw;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.DrawConfig;
import cn.game.protocol.generated.config.GiftCardConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.DrawManager;
import cn.game.protocol.generated.manager.GiftCardManager;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.DrawMsg.DrawInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;
import cn.game.util.IntMapWrapper;

public class DrawModule extends BasePlayerModule {

	/** 当前第几档必送神将,key：GiftCardConfig 表id */
	private IntMapWrapper giftIndex = new IntMapWrapper();
	/** 当前档位的抽卡次数,key：GiftCardConfig 表id */
	private IntMapWrapper drawTimes = new IntMapWrapper();
	/** 免费单抽的时间 key：DrawConfig 表id */
	private IntMapWrapper freeDrawTime = new IntMapWrapper();
	private boolean isFirstTen = true;

	@Override
	public EventTypeEnum[] getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleEvent(GameEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initFromDbAfter() {
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
		List<GiftCardConfig> giftCardList = GiftCardManager.instance().getDrawIdList(id);

		List<RewardInfo> ret = new ArrayList<>();
		List<RewardInfo> giftList = new ArrayList<>();

		allRewards.add(ret);
		allRewards.add(giftList);

		DrawConfig drawConfig = DrawManager.instance().get(id);
		int gold = drawConfig.DrawMoney * count;
		int firstDrawRandomId = drawConfig.DrawRandomId;
		int nextDrawRandomId = drawConfig.DrawRandomId;
		if (count == 10 && isFirstTen) {
			firstDrawRandomId = GlobalConst.FirstMandatoryDraw;
			isFirstTen = false;
		}
		
		for (int i = 0; i < count; i++) {
			List<RewardInfo> reward = PlayerHelper.addReward(player, i == 0 ? firstDrawRandomId : nextDrawRandomId, OpType.Draw);
			ret.addAll(reward);

			for (GiftCardConfig giftCardConfig : giftCardList) {

				int giftCardId = giftCardConfig.ID;
				drawTimes.add(giftCardId, 1);
				/*				int curIndex = giftIndex.getValue(giftCardId);
								int curTimes = drawTimes.getValue(giftCardId);
								int remaining = giftCardConfig.GiftCardCount[curIndex] - curTimes;
								if (remaining == 0) {
									// 送卡
									giftList.addAll(PlayerHelper.addReward(player, giftCardConfig.GiftCardRandomId[curIndex], OpType.Draw));
									// next index
									if (curIndex < giftCardConfig.GiftCardRandomId.length - 1) {
										giftIndex.add(giftCardId, 1);
									}
									drawTimes.setValue(giftCardId, 0);
								}*/
			}
		}
		if (gold > 0) {
			PlayerHelper.addResources(player, Asset.gold.ID, gold, OpType.Draw);
		}
		if (freeOnce) {
			freeDrawTime.setValue(id, DateUtil.currentTimeSeconds());
		}
		return allRewards;
	}

}
