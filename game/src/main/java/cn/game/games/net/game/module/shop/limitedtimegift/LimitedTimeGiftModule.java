package cn.game.games.net.game.module.shop.limitedtimegift;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.recharge.PayType;
import cn.game.protocol.generated.config.LimitedTimeGiftConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.LimitedTimeGiftManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.ShopMsg.LimitedTimeGiftPush_15100054;
import cn.game.util.DateUtil;
import cn.game.util.IntMapWrapper;
import io.vertx.core.Future;

/**    
 * 条件激活的限时礼包
 * 2025年8月14日 18:19:07
 * @author SYQ
 */
public class LimitedTimeGiftModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.LevelUp,
			EventTypeEnum.FuncOpen };

	/** 玩家当前已弹出的限时礼包,可以购买 key : id   */
	private Map<Integer, LimitedTimeGiftData> limitedTimeGifts = new java.util.HashMap<>();
	/** 礼包出现次数  */
	private IntMapWrapper activeCountMap = new IntMapWrapper();
	/** 玩家当前等级可能出现的限时礼包，还不能买。 key:组，每组产生一个高档位的礼包 */
	private transient Multimap<Integer, LimitedTimeGift> limitedTimeGiftCheck = ArrayListMultimap.create();

	/** 
	 * 增加新的限时礼包
	 * @param id
	 */
	public void addLimitedTimeGift(int id, boolean notify) {
		if (limitedTimeGifts.containsKey(id)) {
			return;
		}
		LimitedTimeGiftConfig limitedTimeGiftConfig = LimitedTimeGiftManager.instance().get(id);
		if (!PlayerHelper.checkCondition(player, limitedTimeGiftConfig.UnlockCondition)) {
			return ; 
		}
		boolean checkActiveCount = checkActiveCount(limitedTimeGiftConfig);
		if (!checkActiveCount) {
			return;
		}
		for (LimitedTimeGiftData data : limitedTimeGifts.values()) {
			LimitedTimeGiftConfig tmpConfig = LimitedTimeGiftManager.instance().get(data.getId());
			if (tmpConfig.Group == limitedTimeGiftConfig.Group) {
				// 同一组的礼包只能出现一次
				return;
			}
		}
		LimitedTimeGiftData data = new LimitedTimeGiftData();
		data.setId(id);
		data.setExpireTime(System.currentTimeMillis() + limitedTimeGiftConfig.Duration * 1000L);

		limitedTimeGifts.put(data.getId(), data);
		activeCountMap.add(id);
		if (notify) {
			player.getGameClient().sendProtocol(LimitedTimeGiftPush_15100054.newBuilder().setInfo(data.toProto()));
		}
		player.setTimerTask(limitedTimeGiftConfig.Duration * 1000L, r -> {
			// 过期了，删除这个礼包
			limitedTimeGifts.remove(data.getId());
		});
	}

	private boolean checkActiveCount(LimitedTimeGiftConfig limitedTimeGiftConfig) {
		int activeCount = activeCountMap.getValue(limitedTimeGiftConfig.ID);
		return limitedTimeGiftConfig.MaxTimes > 0 && activeCount < limitedTimeGiftConfig.MaxTimes;
	}
	private void refreshLimitedTimeGiftCheck() {
		Map<Integer, List<LimitedTimeGiftConfig>> map = LimitedTimeGiftManager.instance().getGroups();
		map.forEach((k, list) -> {
			// 倒序遍历list,先找符合的最高档位的
			loop:for (int i = 0; i < list.size(); i++) {
				LimitedTimeGiftConfig limitedTimeGiftConfig = list.get(i);
				if (limitedTimeGiftConfig.PlayerLevelMin > player.getLevel() || limitedTimeGiftConfig.PlayerLevelMax < player.getLevel()) {
					// 不符合等级要求
					continue;
				}
				if (limitedTimeGiftConfig.VipMin > player.getVipLevel() || limitedTimeGiftConfig.VipMax < player.getVipLevel()) {
					// 不符合vip等级要求
					continue;
				}
				boolean checkActiveCount = checkActiveCount(limitedTimeGiftConfig);
				if (!checkActiveCount) {
					continue;
				}
				Collection<LimitedTimeGift> limitedTimeGiftOldCollection = limitedTimeGiftCheck.get(limitedTimeGiftConfig.Group);

				if (limitedTimeGiftOldCollection != null && limitedTimeGiftOldCollection.size() > 0) {
					for (LimitedTimeGift old : limitedTimeGiftOldCollection) {
						if (old.getId() == limitedTimeGiftConfig.ID) {
							// 已经存在这个礼包了
							continue loop;
						}
					}
				}

				LimitedTimeGift limitedTimeGift = new LimitedTimeGift();
				limitedTimeGift.setId(limitedTimeGiftConfig.ID);
				limitedTimeGift.setPlayerId(playerId); 

				limitedTimeGift.initCondition(r -> addLimitedTimeGift(limitedTimeGiftConfig.ID, true));

				limitedTimeGiftCheck.put(limitedTimeGiftConfig.Group, limitedTimeGift);
			}
		});
	}

	public Future<List<RewardInfo>> buy(int id, boolean pay) {
		LimitedTimeGiftConfig limitedTimeGiftConfig = LimitedTimeGiftManager.instance().get(id);
		LimitedTimeGiftData limitedTimeGiftData = getLimitedTimeGifts(id);
		if (limitedTimeGiftData == null) {
			return Future.failedFuture(ErrorMsgEnum.player_data_not_found.ID + "");
		}
		if (limitedTimeGiftData.getBuyCount() >= limitedTimeGiftConfig.PurchasesNum) {
			return Future.failedFuture(ErrorMsgEnum.times_limit.ID + "");
		}
		Future<Boolean> payFuture = pay == false ? Future.succeededFuture()
				: player.pay(PayType.LimitedTimeGift, id, limitedTimeGiftConfig.BuyPrice);

		return payFuture.map(r -> {
			if (!r) {
				player.fail(ErrorMsgEnum.resource_not_enough);
			}
			limitedTimeGiftData.setBuyCount(limitedTimeGiftData.getBuyCount() + 1);
			// 如果购买次数超过了配置的次数，则删除这个礼包
			if (limitedTimeGiftConfig.PurchasesNum > 0 && limitedTimeGiftData.getBuyCount() >= limitedTimeGiftConfig.PurchasesNum) {
				// 删除这个礼包
				limitedTimeGifts.remove(id);
			}
			List<RewardInfo> resources = PlayerHelper.addResources(player, limitedTimeGiftConfig.DropGroupId, OpType.LimitedTimeGift);
			return resources;
		});
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
		case PLAYER_CREATE:
			break;
		case LevelUp:
			int exp = event.getIntParameter(0);
			if (exp == Asset.playerExp.ID || exp == Asset.VIPExp.ID) {
				refreshLimitedTimeGiftCheck();
			}
			break;
		case FuncOpen:
			InitialUI func = event.getParameter(0);
			if (func == InitialUI.XianShiLiBao) {
				refreshLimitedTimeGiftCheck();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onLogin() {
		refreshLimitedTimeGiftCheck(); 
		long now = DateUtil.currentTimeMillis();

		Iterator<Entry<Integer, LimitedTimeGiftData>> iterator = limitedTimeGifts.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<java.lang.Integer, cn.game.games.net.game.module.shop.limitedtimegift.LimitedTimeGiftData> entry = (Map.Entry<java.lang.Integer, cn.game.games.net.game.module.shop.limitedtimegift.LimitedTimeGiftData>) iterator
					.next();
			LimitedTimeGiftData data = entry.getValue();
			if (data.getExpireTime() <= now) {
				// 礼包过期了
				iterator.remove();
			} else {
				player.setTimerTask(data.getExpireTime() - now, r -> {
					limitedTimeGifts.remove(data.getId());
				});
			}
		}
	};

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		if (limitedTimeGifts.size() > 0) {
			for (LimitedTimeGiftData data : limitedTimeGifts.values()) {
				builder.addLimitedTimeGift(data.toProto());
			}
		}
	}

	public LimitedTimeGiftData getLimitedTimeGifts(int id) {
		return limitedTimeGifts.get(id);
	}

}
