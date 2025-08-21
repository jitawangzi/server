package cn.game.games.net.game.module.shop.limitedtimegift;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	private transient Map<Integer, LimitedTimeGift> limitedTimeGiftCheck = new HashMap<Integer, LimitedTimeGift>();

	/** 
	 * 增加新的限时礼包
	 * @param id
	 */
	public void addLimitedTimeGift(int id, boolean notify) {
		if (limitedTimeGifts.containsKey(id)) {
			return;
		}
		LimitedTimeGiftConfig limitedTimeGiftConfig = LimitedTimeGiftManager.instance().get(id);
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

	private void initLimitedTimeGiftCheck() {
		Map<Integer, List<LimitedTimeGiftConfig>> map = LimitedTimeGiftManager.instance().getGroups();
		map.forEach((k, list) -> {
			boolean isGroupActive = false;
			// 倒序遍历list,先找符合的最高档位的
			for (int i = list.size() - 1; i >= 0; i--) {
				LimitedTimeGiftConfig limitedTimeGiftConfig = list.get(i);
				if (limitedTimeGiftConfig.PlayerLevelMin > player.getLevel() || limitedTimeGiftConfig.PlayerLevelMax < player.getLevel()) {
					// 不符合等级要求
					continue;
				}
				boolean checkActiveCount = checkActiveCount(limitedTimeGiftConfig);
				if (!checkActiveCount) {
					continue;
				}
				LimitedTimeGift limitedTimeGift = new LimitedTimeGift();
				limitedTimeGift.setId(limitedTimeGiftConfig.ID);
				limitedTimeGift.setPlayerId(playerId); 

				limitedTimeGift.initCondition(r -> addLimitedTimeGift(limitedTimeGiftConfig.ID, false));

				limitedTimeGiftCheck.put(limitedTimeGiftConfig.Group, limitedTimeGift);
				isGroupActive = true;
				break; // 找到一个就行
			}
			if (!isGroupActive) {
				return; // 没有符合条件的礼包，直接跳出，依靠填表顺序
			}
		});

	}

	private void refreshLimitedTimeGiftCheck(boolean notify) {
		Map<Integer, List<LimitedTimeGiftConfig>> map = LimitedTimeGiftManager.instance().getGroups();
		map.forEach((k, list) -> {
			boolean isGroupActive = false;
			// 倒序遍历list,先找符合的最高档位的
			for (int i = list.size() - 1; i >= 0; i--) {
				LimitedTimeGiftConfig limitedTimeGiftConfig = list.get(i);
				if (limitedTimeGiftConfig.PlayerLevelMin > player.getLevel() || limitedTimeGiftConfig.PlayerLevelMax < player.getLevel()) {
					// 不符合等级要求
					continue;
				}
				boolean checkActiveCount = checkActiveCount(limitedTimeGiftConfig);
				if (!checkActiveCount) {
					continue;
				}
				LimitedTimeGift limitedTimeGiftOld = limitedTimeGiftCheck.get(limitedTimeGiftConfig.Group);

				if (limitedTimeGiftOld != null) {
					LimitedTimeGiftConfig limitedTimeGiftConfigOld = LimitedTimeGiftManager.instance().get(limitedTimeGiftOld.getId());
					if (limitedTimeGiftOld.getId() == limitedTimeGiftConfig.ID) {
						continue;
					}
					if (limitedTimeGiftConfigOld.GroupLevel >= limitedTimeGiftConfig.GroupLevel) {
						continue;
					}
					// 这个组解锁新档位的礼包了，使用新档位礼包替换老档位礼包
					limitedTimeGiftOld.unregEvent();
					limitedTimeGiftCheck.remove(limitedTimeGiftConfigOld.Group);
				}

				LimitedTimeGift limitedTimeGift = new LimitedTimeGift();
				limitedTimeGift.setId(limitedTimeGiftConfig.ID);
				limitedTimeGift.setPlayerId(playerId); 

				limitedTimeGift.initCondition(r -> addLimitedTimeGift(limitedTimeGiftConfig.ID, true));

				limitedTimeGiftCheck.put(limitedTimeGiftConfig.Group, limitedTimeGift);
				isGroupActive = true;
				break; // 找到一个就行
			}
			if (!isGroupActive) {
				return; // 没有符合条件的礼包，直接跳出，依靠填表顺序
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
			if (exp == Asset.playerExp.ID) {
				refreshLimitedTimeGiftCheck(true);
			}
			break;
		case FuncOpen:
			InitialUI func = event.getParameter(0);
			if (func == InitialUI.XianShiLiBao) {
				refreshLimitedTimeGiftCheck(player.isIslogining() ? false : true);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onLogin() {
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
