package cn.game.games.net.game.module.draw;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.DrawConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.SupremeRandomGroupConfig;
import cn.game.protocol.generated.manager.DrawManager;
import cn.game.protocol.generated.manager.SupremeRandomGroupManager;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.DrawMsg.DrawInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;
import cn.game.util.IntMapWrapper;
import cn.game.util.Rnd;

public class DrawModule extends BasePlayerModule {

	/** 当前第几档必送神将,key：GiftCardConfig 表id */
	private IntMapWrapper giftIndex = new IntMapWrapper();
	/** 当前档位的抽卡次数,key：GiftCardConfig 表id */
	private IntMapWrapper drawTimes = new IntMapWrapper();
	/** 免费单抽的时间 key：DrawConfig 表id */
	private IntMapWrapper freeDrawTime = new IntMapWrapper();
	private boolean isFirstTen = true;

	/** 至尊抽卡，已经抽过的索引、下标 SupremeRandomGroupConfig */
	private List<Integer> supremeRandomGroupList = new ArrayList<Integer>();

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
//		List<GiftCardConfig> giftCardList = GiftCardManager.instance().getDrawIdList(id);

		List<RewardInfo> ret = new ArrayList<>();
		List<RewardInfo> giftList = new ArrayList<>();

		allRewards.add(ret);
		allRewards.add(giftList);

		DrawConfig drawConfig = DrawManager.instance().get(id);
		if (id == 2) { // 至尊抽卡走特殊逻辑。首次十连给指定的卡，接下来走特殊卡池
			List<SupremeRandomGroupConfig> list = SupremeRandomGroupManager.instance().list();
			if (count == 10 && isFirstTen) {
				List<RewardInfo> reward = PlayerHelper.addReward(player, GlobalConst.FirstMandatoryDraw, OpType.Draw);
				ret.addAll(reward);
				isFirstTen = false;
				// 相当于从卡池里抽了一个
				for (int i = 0; i < Rnd.RANDOM_MAX; i++) {
					int randomWeighableIndex = Rnd.randomWeighableIndex(list, supremeRandomGroupList);
					SupremeRandomGroupConfig supremeRandomGroupConfig = list.get(randomWeighableIndex);
					if (!supremeRandomGroupConfig.SupremeRandomParameterTarget) {
						supremeRandomGroupList.add(randomWeighableIndex);
						break;
					}
				}
				count--;
			}
			for (int i = 0; i < count; i++) {
				int randomWeighableIndex = Rnd.randomWeighableIndex(list, supremeRandomGroupList); 
				SupremeRandomGroupConfig supremeRandomGroupConfig = list.get(randomWeighableIndex); 
				if (supremeRandomGroupConfig.SupremeRandomParameterTarget) {
					supremeRandomGroupList.clear(); 
				}else {
					supremeRandomGroupList.add(randomWeighableIndex); 
					if (supremeRandomGroupList.size() >= list.size()) {
                        supremeRandomGroupList.clear();
					}
				}
				
				List<RewardInfo> reward = PlayerHelper.addReward(player, supremeRandomGroupConfig.GivenID, OpType.Draw);
				ret.addAll(reward);
			}
		} else {
			for (int i = 0; i < count; i++) {
				List<RewardInfo> reward = PlayerHelper.addReward(player, drawConfig.DrawRandomId, OpType.Draw);
				ret.addAll(reward);
			}
		}
		
		for (int[] money : drawConfig.DrawMoney) {
			PlayerHelper.addResources(player, money[0], money[1] * count, OpType.Draw);
		}
		if (freeOnce) {
			freeDrawTime.setValue(id, DateUtil.currentTimeSeconds());
		}
		return allRewards;
	}

}
