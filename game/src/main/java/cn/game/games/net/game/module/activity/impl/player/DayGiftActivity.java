package cn.game.games.net.game.module.activity.impl.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;

import cn.game.core.exception.LogicException;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.PlayerActivityBase;
import cn.game.games.net.game.module.award.Goods;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.ActivityMsg.ActivityDayGiftResponse_11000101;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;

/**    
 * 每日特惠礼包，常驻活动
 * 2025年3月24日 15:08:47
 * @author SYQ
 */
@ActivityType(type = ActivityTypeEnum.ActivityDayGift)
public class DayGiftActivity extends PlayerActivityBase {
	private static transient EventTypeEnum[] events = new EventTypeEnum[] {};

	/** key    */
	private Map<Integer, Integer> dayGiftMap = new HashMap<Integer, Integer>();
	private int endDay;

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
		if (endDay > 0) {
			int nowDay = DateUtil.getDay();
			if (nowDay >= endDay) {
				endDay = 0;
				rewardMail();
				dayGiftMap.clear();
			}else {
				// 自动购买所有礼包
				buyAll();
			}
		} else {
			rewardMail();
			dayGiftMap.clear();
		}
		return true;
	};

	/** 
	 * 如果玩家没有领，发邮件给奖励
	 */
	public void rewardMail() {
		dayGiftMap.forEach((k, v) -> {
			if (v == 1) {
				List<Goods> goods = new ArrayList<>();
				MailHelper.sendMail(player.getPlayerId(), 1, goods, true);
			}
		});
	}

	public void packageBuy() {
		if (endDay > 0) {
			throw new LogicException(ErrorMsgEnum.repeat_request.ID);
		}
		rewardMail();

		int day = DateUtil.getDay();
		endDay = day + 10;

		buyAll();
		GameLogger.activity(player, id, 0);
	}

	private void buyAll() {
		// TODO 一次性购买所有礼包
		for (int i = 1; i < 10; i++) {
			dayGiftMap.put(i, 1);
		}

	}

	public void buy(int cid) {
		if (dayGiftMap.containsKey(cid)) {
			throw new LogicException(ErrorMsgEnum.repeat_request.ID);
		}
		dayGiftMap.put(cid, 1);

		GameLogger.activity(player, id, cid);
	}

	public List<RewardInfo> reward(int cid) {
		Integer integer = dayGiftMap.get(cid);
		if (integer == null || integer != 1) {
			throw new LogicException(ErrorMsgEnum.pre_condition_check_error.ID);
		}
//		return PlayerHelper.addResources(player, firstChargeConfig.Item, OpType.FirstCharge);
		return null;
	}

	@Override
	public List<RewardInfo> receive(int id) {
		return reward(id);
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public Message buildActivityShowInfo() {
		ActivityDayGiftResponse_11000101.Builder builder = ActivityDayGiftResponse_11000101.newBuilder();
		builder.putAllBuyMap(dayGiftMap);
		return builder.build();
	}

}
