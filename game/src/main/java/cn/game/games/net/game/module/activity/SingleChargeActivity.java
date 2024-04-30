package cn.game.games.net.game.module.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.protobuf.Message;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.ShopGiftConfig;
import cn.game.protocol.generated.config.SingleChargeActivityConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.ShopGiftManager;
import cn.game.protocol.generated.manager.SingleChargeActivityManager;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySingleChargeResponse_11000008;
import cn.game.protocol.protobuf.ActivityMsg.SingleChargeActivityInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;

/**    
 * 单充活动
 * @date 2024年4月30日 上午11:27:15
 * @author SYQ
 */
@ActivityType(type = ActivityTypeEnum.SingleCharge)
public class SingleChargeActivity extends ActivityBase {

	private Map<Integer, SingleCharge> chargeMap = new HashMap<Integer, SingleCharge>();

	@Override
	public Message buildActivityInfo() {
		ActivitySingleChargeResponse_11000008.Builder resp = ActivitySingleChargeResponse_11000008.newBuilder();
		int nowDay = DateUtil.getDay();
		for (Entry<Integer, SingleCharge> entry : chargeMap.entrySet()) {
			Integer key = entry.getKey();
			SingleCharge singleCharge = entry.getValue();
			cn.game.protocol.protobuf.ActivityMsg.SingleChargeActivityInfo.Builder builder = SingleChargeActivityInfo.newBuilder();
			builder.setId(key) ; 
			builder.setCanRewardIndex(nowDay - singleCharge.getDay());
			builder.addAllSelectedIndex(singleCharge.getSelectedIndex());
			resp.addSingleCharges(builder.build());
		}
		return resp.build();
	}

	public boolean buy(int cid) {
		
		SingleChargeActivityConfig singleChargeActivityConfig = SingleChargeActivityManager.instance().get(cid); 
		
		if (cid != 1 && !chargeMap.containsKey(cid - 1)) {
			return false;
		}
		if (chargeMap.containsKey(cid)) {
			return false;
		}
		SingleCharge charge = new SingleCharge();
		charge.setDay(DateUtil.getDay());
		chargeMap.put(cid, charge);
		return true;
	}

	public List<RewardInfo> reward(int cid, int index) {
		SingleChargeActivityConfig singleChargeActivityConfig = SingleChargeActivityManager.instance().get(cid);
		SingleCharge singleCharge = getSingleCharge(cid);
		singleCharge.getSelectedIndex().add(index);

		ShopGiftConfig shopGiftConfig = ShopGiftManager.instance().get(singleChargeActivityConfig.BundleID[index]);

		return PlayerHelper.addResources(player, shopGiftConfig.Item);
	}
	@Override
	public void setEvents(EventTypeEnum[] events) {

	}

	@Override
	public void startUp() {
	}

	@Override
	public void shutDown() {

	}

	public SingleCharge getSingleCharge(int chargeId) {
		return chargeMap.get(chargeId);
	}
	@Override
	public List<RewardInfo> receive(int id) {
		// TODO Auto-generated method stub
		return null;
	}
}

class SingleCharge {
	/** 购买日期 */
	private int day;
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
