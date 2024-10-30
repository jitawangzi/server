package cn.game.games.net.game.module.activity.impl.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.PlayerActivityBase;
import cn.game.protocol.generated.config.FirstChargeConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.FirstChargeManager;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeResponse_11000008;
import cn.game.protocol.protobuf.ActivityMsg.FirstChargeActivityInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;

/**    
 * 单充活动
 * 2024年4月30日 上午11:27:15
 * @author SYQ
 */
@ActivityType(type = ActivityTypeEnum.FirstCharge)
public class FirstChargeActivity extends PlayerActivityBase {
	private static transient EventTypeEnum[] events = new EventTypeEnum[] {};

	/** key  FirstCharge表id  */
	private Map<Integer, SingleCharge> chargeMap = new HashMap<Integer, SingleCharge>();

	@Override
	public Message buildActivityShowInfo(int id) {
		ActivityFirstChargeResponse_11000008.Builder resp = ActivityFirstChargeResponse_11000008.newBuilder();
		int nowDay = DateUtil.getDay();
		Collection<FirstChargeConfig> list = FirstChargeManager.instance().list();
		for (FirstChargeConfig firstChargeConfig : list) {
			if (firstChargeConfig.ActivityiDIndex != id) {
				continue;
			}
			SingleCharge singleCharge = chargeMap.get(firstChargeConfig.ID);
//			if (singleCharge == null && firstChargeConfig.Preconditions > 0) {
//				continue;
//			}
//			if (firstChargeConfig.Price.length == 0) {
//				continue;
//			}
			cn.game.protocol.protobuf.ActivityMsg.FirstChargeActivityInfo.Builder builder = FirstChargeActivityInfo.newBuilder();
			builder.setId(firstChargeConfig.ID);
			int status = getFirstChargeStatus(nowDay, firstChargeConfig, singleCharge);
			builder.setStatus(status);
			resp.addSingleCharges(builder.build());

		}
//		for (Entry<Integer, SingleCharge> entry : chargeMap.entrySet()) {
//			Integer key = entry.getKey();
//			SingleCharge singleCharge = entry.getValue();
//			cn.game.protocol.protobuf.ActivityMsg.FirstChargeActivityInfo.Builder builder = FirstChargeActivityInfo.newBuilder();
//			builder.setId(key) ; 
//			builder.setCanRewardIndex(nowDay - singleCharge.getDay());
//			builder.addAllSelectedIndex(singleCharge.getSelectedIndex());
//			resp.addSingleCharges(builder.build());
//		}
		return resp.build();
	}

	private int getFirstChargeStatus(int nowDay, FirstChargeConfig firstChargeConfig, SingleCharge singleCharge) {
		int status = 0;
		if (singleCharge != null) {
			if (singleCharge.getSelectedIndex().contains(firstChargeConfig.ID)) {
				status = 2;
			} else {
				if (nowDay - singleCharge.getDay() >= firstChargeConfig.Order - 1) {
					status = 1;
				}
			}
		}
//		if (firstChargeConfig.Preconditions > 0) {
//			FirstChargeConfig preConfig = FirstChargeManager.instance().get(firstChargeConfig.Preconditions);
//			if (!chargeMap.containsKey(preConfig.ID)) {
//				status = 0;
//			}
//		}
		return status;
	}

	public boolean check(int cid) {
		
//		FirstChargeActivityConfig singleChargeActivityConfig = SingleChargeActivityManager.instance().get(cid);
		FirstChargeConfig firstChargeConfig = FirstChargeManager.instance().get(cid);
		if (firstChargeConfig.Price.length == 0) {
			return false;
		}
		if (firstChargeConfig.Preconditions > 0) {
			FirstChargeConfig preConfig = FirstChargeManager.instance().get(firstChargeConfig.Preconditions);

//			FirstChargeActivity otherActivity = (FirstChargeActivity) player.getActivityModule().get(preConfig.ActivityiDIndex);
//			if (otherActivity == null || !otherActivity.getChargeMap().containsKey(preConfig.ActivityiD)) {
//				return false;
//			}
			if (!chargeMap.containsKey(preConfig.ID)) {
				return false;
			}
		}

		if (chargeMap.containsKey(firstChargeConfig.ID)) {
			return false;

		}
		return true;
	}

	public void buy(int cid) {

		FirstChargeConfig firstChargeConfig = FirstChargeManager.instance().get(cid);
		SingleCharge charge = new SingleCharge();
		charge.setDay(DateUtil.getDay());
		chargeMap.put(firstChargeConfig.ID, charge);
		GameLogger.activity(player, id, cid);
	}

	public List<RewardInfo> reward(int cid) {
		FirstChargeConfig firstChargeConfig = FirstChargeManager.instance().get(cid);
		SingleCharge singleCharge = chargeMap.get(cid);
		if (singleCharge == null) {
			return null;
		}
		int nowDay = DateUtil.getDay();
		int status = getFirstChargeStatus(nowDay, firstChargeConfig, singleCharge);
		if (status != 1) {
			return null;
		}
		singleCharge.getSelectedIndex().add(cid);
		return PlayerHelper.addResources(player, firstChargeConfig.Item, OpType.FirstCharge);
	}

//	public SingleCharge getSingleCharge(int chargeId) {
//		return chargeMap.get(chargeId);
//	}
	@Override
	public List<RewardInfo> receive(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public Message buildActivityShowInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<Integer, SingleCharge> getChargeMap() {
		return chargeMap;
	}

}

class SingleCharge {
	/** 购买日期 */
	private int day;
	/** 领过的id */
	private List<Integer> selectedIndex = new ArrayList<>();

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public List<Integer> getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(List<Integer> selectedIndex) {
		this.selectedIndex = selectedIndex;
	}


}
