package cn.game.games.net.game.module.draw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.DrawConfig;
import cn.game.protocol.generated.config.GiftCardConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.DrawManager;
import cn.game.protocol.generated.manager.GiftCardManager;
import cn.game.protocol.protobuf.DrawMsg.DrawInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;
import cn.game.util.IntMapWrapper;

public class DrawModule extends BasePlayerModule {

	/** 当前第几档必送神将,key：DrawConfig 表id */
	private IntMapWrapper giftIndex = new IntMapWrapper();
	/** 当前档位的抽卡次数,key：DrawConfig 表id */
	private IntMapWrapper drawTimes = new IntMapWrapper();
	/** 免费单抽的时间 key：DrawConfig 表id */
	private IntMapWrapper freeDrawTime = new IntMapWrapper();

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
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		List<GiftCardConfig> drawIdList = GiftCardManager.instance().getDrawIdList(id);
		for (GiftCardConfig giftCardConfig : drawIdList) {
			int curTimes = drawTimes.getValue(giftCardConfig.ID);
//			int[] giftCardCount = giftCardConfig.GiftCardCount;
			map.put(giftCardConfig.GiftCardQuality, giftCardConfig.GiftCardCount[giftIndex.getValue(giftCardConfig.ID)] - curTimes);
//			int totalCount = 0;
//			int index = -1;
//			int countRemaining = 0;
//			for (int i = 0; i < giftCardCount.length; i++) {
//				totalCount += giftCardCount[i];
//				if (count < totalCount) {
//					index = i;
//					countRemaining = totalCount - count;
//					break;
//				}
//			}
		}

		newBuilder.putAllGiftRemainingTimes(map);
		return newBuilder.build();
	}

	/** 
	 * 抽卡 
	 * @param id
	 * @param count
	 * @return  英雄奖励
	 */
	public List<RewardInfo> draw(int id, int count, boolean freeOnce) {
		List<GiftCardConfig> giftCardList = GiftCardManager.instance().getDrawIdList(id);

		List<RewardInfo> ret = new ArrayList<>();
		DrawConfig drawConfig = DrawManager.instance().get(id);
		int gold = drawConfig.DrawMoney * count;
		for (int i = 0; i < count; i++) {
			List<RewardInfo> reward = PlayerHelper.addReward(player, drawConfig.DrawRandomId);
			ret.addAll(reward);


			for (GiftCardConfig giftCardConfig : giftCardList) {

				drawTimes.add(giftCardConfig.ID, 1);
				int curIndex = giftIndex.getValue(giftCardConfig.ID);
				int curTimes = drawTimes.getValue(giftCardConfig.ID);
				int remaining = giftCardConfig.GiftCardCount[curIndex] - curTimes;
				if (remaining == 0) {
					// 送卡
					ret.addAll(PlayerHelper.addReward(player, giftCardConfig.GiftCardRandomId[curIndex]));
					// next index
					if (curIndex < giftCardConfig.GiftCardRandomId.length - 1) {
						giftIndex.add(giftCardConfig.ID, 1);
					}
					drawTimes.setValue(giftCardConfig.ID, 0);
				}
			}
		}
		if (gold > 0) {
			PlayerHelper.addResources(player, Asset.gold.ID, gold);
		}
		if (freeOnce) {
			freeDrawTime.setValue(id, DateUtil.currentTimeSeconds());
		}
		return ret;
	}

}
