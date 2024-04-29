package cn.game.games.net.game.module.activity;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Message;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.FirstChargeConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.FirstChargeManager;
import cn.game.protocol.protobuf.ActivityMsg.FirstChargeActivityInfo;
import cn.game.protocol.protobuf.ActivityMsg.FirstChargeActivityInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * 首冲活动
 * @date 2024年4月23日 下午5:59:15
 * @author SYQ
 */
@ActivityType(type = ActivityTypeEnum.FirstCharge)
public class FirstChargeActivity extends ActivityBase {

	/** 当前充值可以购买的礼包id */
	private int chargeId;
	private List<Integer> selectedIndex = new ArrayList<>();

	@Override
	public List<RewardInfo> receive(int id) {
		return null;
	}

	@Override
	public Message buildActivityInfo() {
		Builder builder = FirstChargeActivityInfo.newBuilder();
		builder.setId(chargeId);
		if (chargeId < 5) {
			builder.addAllSelectedIndex(selectedIndex);
		}
		return builder.build();
	}

	public List<RewardInfo> buy(int cid, List<Integer> selectedList) {
		FirstChargeConfig firstChargeConfig = FirstChargeManager.instance().get(cid); 
		List<RewardInfo> ret = new ArrayList<>(); 
		if (!selectedList.isEmpty()) {
			for (int i = 0; i < selectedList.size(); i++) {
				List<RewardInfo> rewards = PlayerHelper.addResources(player, firstChargeConfig.Rewards[selectedList.get(i)][0],
						firstChargeConfig.Rewards[selectedList.get(i)][1]);
				ret.addAll(rewards);
			}
			this.selectedIndex.addAll(selectedList);
		}
		List<RewardInfo> rewards = PlayerHelper.addResources(player, firstChargeConfig.Rewards2);
		ret.addAll(rewards);

		FirstChargeConfig nextConfig = FirstChargeManager.instance().getNullable(chargeId + 1);
		if (nextConfig != null) {
			this.chargeId = nextConfig.ID;
		} else {
			player.getActivityModule().destroy(this.id);
		}
		return ret;
	}
	@Override
	public void setEvents(EventTypeEnum[] events) {

	}

	@Override
	public void startUp() {
		chargeId = 1;
	}

	@Override
	public void shutDown() {

	}

	public int getChargeId() {
		return chargeId;
	}

	public List<Integer> getSelectedIndex() {
		return selectedIndex;
	}

}
