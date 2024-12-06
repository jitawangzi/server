package cn.game.games.net.game.module.activity.impl.player;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.module.award.Goods;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.enume.WelfareTypeEnum;
import cn.game.util.GameUtil;
import com.google.protobuf.Message;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.PlayerActivityBase;
import cn.game.protocol.generated.config.SevenDaysSigninConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.SevenDaysSigninManager;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninInfoResponse_11000025;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

@ActivityType(type = ActivityTypeEnum.SevenDaysSignin)
public class SevenDaysSignin extends PlayerActivityBase {
	private static transient EventTypeEnum[] events = new EventTypeEnum[] {};
	/** 今天登陆是否签到了 */
	private boolean isSignin;
	/** 已经签到的天数   1 - 7 */
	private int day;
	/** 领取过的额外奖励 */
	private List<Integer> rewardExtra = new ArrayList<>();


	@Override
	public Message buildActivityShowInfo() {
		ActivitySevenDaysSigninInfoResponse_11000025.Builder builder = ActivitySevenDaysSigninInfoResponse_11000025.newBuilder();
		builder.setActivityId(id);
		builder.setDay(day);
		builder.setCanSignin(!isSignin);
		builder.addAllExtRewardIds(rewardExtra);
		return builder.build();
	}

	@Override
	public boolean newDay() {
		isSignin = false;
		return true;
	}

	List<SevenDaysSigninConfig> getSevenDaysSigninConfigList(int activityId) {
		return SevenDaysSigninManager.instance().list().stream().filter(config -> config.ActivityID == activityId).toList();
	}

	public SevenDaysSigninConfig getSevenDaysSigninConfig(int day) {
		for (SevenDaysSigninConfig config : getSevenDaysSigninConfigList(id)) {
			if (config.Days == day + 1) {
				return config;
			}
		}
		return null;
	}

	@Override
	public List<RewardInfo> receive(int id) {
		SevenDaysSigninConfig config = getSevenDaysSigninConfig(day);
		//月卡加成额外掉落
		int[][] drops =  GameUtil.arrayAddition(config.Item, player.getWelfareValue(WelfareTypeEnum.MonthClock));
		List<RewardInfo> resources = PlayerHelper.addResources(player, drops, OpType.SevenDaysSignin);
		day++;
		isSignin = true;
		GameLogger.activity(player, super.id, day);
		if (day == getSevenDaysSigninConfigList(id).size()) {
			player.getActivityModule().destroy(super.id, true);
		}
		return resources;
	}

	public List<RewardInfo> rewardExtra(SevenDaysSigninConfig config) {
		//月卡加成额外掉落
		if (config.Reward1.length > 0){
			rewardExtra.add(config.ID);
			GameLogger.activity(player, super.id, day);
			List<RewardInfo> resources = PlayerHelper.addResources(player, config.Reward1, OpType.SevenDaysSignin);
			return resources;
		}
		return new ArrayList<>();
	}

	public List<Integer> getRewardExtra() {
		return rewardExtra;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public boolean isSignin() {
		return isSignin;
	}

	@Override
	public void destroy() {
		List<Goods> drops = new ArrayList<>();
    	getSevenDaysSigninConfigList(id).forEach(
            config -> {
              if (config.Reward1.length > 0 && !rewardExtra.contains(config.ID)) {
                drops.addAll(Goods.valueOf(config.Reward1));
              }
            });
		MailHelper.sendMail(player.getPlayerId(), 20, drops, true);
	}

	public int getSigninDay() {
		return day;
	}
}
