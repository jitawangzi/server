package cn.game.games.net.game.module.activity.impl.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.PlayerActivityBase;
import cn.game.protocol.generated.config.ActivityMeiRiTeHuiConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.ActivityMeiRiTeHuiManager;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.ActivityMsg.ActivityDayGiftResponse_11000101;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * 每日特惠礼包，常驻活动
 * 2025年3月24日 15:08:47
 * @author SYQ
 */
@ActivityType(type = ActivityTypeEnum.ActivityDayGift)
public class DayGiftActivity extends PlayerActivityBase {
	private static transient EventTypeEnum[] events = new EventTypeEnum[] {};

	/** key id,value 购买次数    */
	private Map<Integer, Integer> buyCountMap = new HashMap<Integer, Integer>();

	@Override
	public boolean hasRed() {
//		List<Pair<Integer, Integer>> status = getStatus();
//		for (Pair<Integer, Integer> pair : status) {
//			if (pair.second == 1) {
//				return true;
//			}
//		}
		return false;
	}

	@Override
	public boolean newDay() {
		buyCountMap.clear();
		return true;
	};

	public List<RewardInfo> packageBuy() {
		List<RewardInfo> ret = new ArrayList<>();
		for (ActivityMeiRiTeHuiConfig meiRiTeHuiConfig : ActivityMeiRiTeHuiManager.instance().list()) {
			if (meiRiTeHuiConfig.ID == 1 && buyCountMap.get(meiRiTeHuiConfig.ID) != null) {
				continue;
			}
			if (meiRiTeHuiConfig.ID == 99) {
				List<RewardInfo> reward = PlayerHelper.addResources(player, meiRiTeHuiConfig.Item2, OpType.ActivityMeiRiTeHui);
				ret.addAll(reward);
				continue;
			}
			List<RewardInfo> reward = PlayerHelper.addResources(player, meiRiTeHuiConfig.Item1, OpType.ActivityMeiRiTeHui);
			ret.addAll(reward);
			buyCountMap.compute(meiRiTeHuiConfig.ID, (k, v) -> v == null ? 1 : v + 1);
		}
		buyCountMap.compute(99, (k, v) -> v == null ? 1 : v + 1);
		return ret;
	}

	public int getBuyCount(int cid) {
		Integer integer = buyCountMap.get(cid);
		return integer == null ? 0 : integer;
	}

	public List<RewardInfo> buy(int giftId) {
		ActivityMeiRiTeHuiConfig meiRiTeHuiConfig = ActivityMeiRiTeHuiManager.instance().get(giftId);
		List<RewardInfo> reward = PlayerHelper.addResources(player, meiRiTeHuiConfig.Item1, OpType.ActivityMeiRiTeHui);
		buyCountMap.compute(giftId, (k, v) -> v == null ? 1 : v + 1);
		return reward;
	}

	@Override
	public List<RewardInfo> receive(int id) {
		return null;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public Message buildActivityShowInfo() {
		ActivityDayGiftResponse_11000101.Builder builder = ActivityDayGiftResponse_11000101.newBuilder();
		builder.putAllBuyCountMap(buyCountMap);
		return builder.build();
	}

}
