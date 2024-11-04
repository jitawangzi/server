package cn.game.games.core.log;

import static java.util.stream.Collectors.toList;

import java.util.List;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.GameServerStatus;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.protocol.generated.config.ShopItemConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.ConditionManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.generated.manager.QuestManager;
import cn.game.protocol.generated.manager.ShopItemManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.util.Config;
import cn.game.util.log.Logger;
import cn.game.util.log.LoggerType;
import cn.game.util.log.SystemLogger;

//
//import java.util.List;
//import java.util.Objects;
//
//import org.apache.commons.lang3.tuple.Pair;
//
//import cn.game.games.cache.entity.Mail;
//import cn.game.games.cache.entity.Player;
//import cn.game.games.net.game.module.activity.ActivityType;
//import cn.game.games.net.game.module.currency.Currency;
//import cn.game.protocol.protobuf.PbProtocol;
//import cn.game.util.log.Logger;
//import cn.game.util.log.LoggerType;
//import cn.game.util.log.SystemLogger;
//
///**
// * @author pangjiawei - [Created on 2018/1/30 22:14]
// */
public class GameLogger extends Logger {
//
//	/**
//	 * 构造玩家日志前缀模板
//	 * <p>
//	 * 匿名ID、角色ID、账号id、区服、角色名、角色等级、VIP等级
//	 */
//    static Object[] generatePlayerLogPrefix(Player player) {
//        String anonymousId = "-1";
//        Account account = player.getAccount();
//        if (Objects.nonNull(account)) {
//            anonymousId = account.anonymousId;
//        }
//
//        return new Object[]{anonymousId, player.getPlayerId(), player.getAccountId(), player.getServerNum(), player.getName(), player.getLevel(), player.getVipLevel()};
//    }
//
//    static Object[] generatePlayerLogPrefix(MiniPlayer miniPlayer) {
//        if (Objects.isNull(miniPlayer)) {
//            return new Object[]{"-1", 0L, "-1", Configuration.serverId, "none", 0, 0};
//        }
//
//        return new Object[]{miniPlayer.getAnonymousId(), miniPlayer.getPlayerId(), miniPlayer.getAccountId(), miniPlayer.getServerNum(), miniPlayer.getName(), miniPlayer.getLevel(),
////                miniPlayer.vipLevel
//        };
//    }
//
//	/**
//	 * 战斗记录
//	 */
//    public static void combat(Object object) {
//        combat(-1, object);
//    }
//
//	/**
//	 * 战斗记录
//	 */
//    public static void combat(long combatId, Object... objects) {
//        if (LoggerType.Combat.logger.isInfoEnabled()) {
//            LoggerType.Combat.logger.info(CombatManager.local.get() + " : " + LoggerType.splice(objects));
//        }
//    }
//
//    public static void combatError(long combatId, Object... objects) {
//        LoggerType.Combat.logger.error(CombatManager.local.get() + " : " + LoggerType.splice(objects));
//    }
//
//    public static void combat(Throwable object) {
//        LoggerType.Combat.logger.error(CombatManager.local.get() + " : " + object.getMessage());
//        for (StackTraceElement el : object.getStackTrace()) {
//            LoggerType.Combat.logger.error(CombatManager.local.get() + " : " + el.toString());
//        }
//    }
//
//	/**
//	 * 竞技场
//	 */
//    public static void arena(final Object... objects) {
//        try {
//            LoggerType.arena.logger.info(LoggerType.splice(objects));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//	/**
//	 * 时间，游戏标识，SDK版本号，系统，推广渠道id，设备唯一标识，账号id，自定义事件id，客户端版本号
//	 * 时区
//	 */
//    public static void clientEvent(PbProtocol.LoginCheckReq req, int eventId, String ip) {
//        try {
//            Object[] array;
//            if (req != null) {
//                String sdkDeviceId = req.getSdkDeviceId();
//                String adChannel = req.getAdChannel();
//                String system = req.getDeviceType();
//                array = new Object[]{
//                        getCurrentTimeLogText(), GameLogAssistant.APP_KEY, req.getSdkVersion(), "all", adChannel, sdkDeviceId, req.getAccountId(), eventId, req.getVersion(),
//                        GameLogAssistant.TIME_ZONE
//                };
//            } else {
//                String adChannel = "null";
//                String system = "null";
//                array = new Object[]{
//                        getCurrentTimeLogText(), GameLogAssistant.APP_KEY, "null", "all", adChannel, "null", "null", eventId, "null",
//                        GameLogAssistant.TIME_ZONE
//                };
//            }
//            LoggerType.serverevent.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//	/**
//	 * 时间、日志模块名、日志版本、匿名id、自定义事件id、自定义事件名、客户端当前版本、IP
//	 */
//    public static void clientEvent(Account account, int eventId) {
//        String sdkDeviceId = account.getSdkDeviceId();
//        String adChannel = account.adChannel;
//        String system = account.deviceType;
//        Object[] array = new Object[]{
//                getCurrentTimeLogText(), GameLogAssistant.APP_KEY, account.sdkVersion, system, adChannel, sdkDeviceId, account.getAccountId(), eventId, account.version,
//                GameLogAssistant.TIME_ZONE
//        };
//        LoggerType.serverevent.logger.info(LoggerType.splice(array));
//    }
//

	/**
	 * 心跳
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，在线用户数，排队人数
	 */
	public static void heart() {
		try {
			Object[] array = new Object[] { getCurrentTimeLogText(), Config.APP_KEY,
					GameServerStatus.getInstance().getServerInfo().getVersion(), LoggerType.heart.name(), LoggerType.heart.version,
					"1010", ServerContext.getInstance().getServerId(), PlayerManager.getInstance().getOnlineCount() };
			LoggerType.heart.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
//
	/**
	 * 登入服务器
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
	 * 账号id，角色id，角色等级，班级id，设备唯一标识
	 * 角色名，登录ip，价值虚拟币总量
	 */
	public static void login(Player player) {
		try {
			Object[] array = new Object[] {
					LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.login.name(), LoggerType.login.version, "2050")),
					player.getData().getName() == null ? "null" : player.getData().getName(),
					player.getGameClient().getIp(), player.getCurrencyModule().getCount(Asset.diamond.ID) };
			LoggerType.login.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

	public static void login_wxxcx(Player player) {
		try {
			Object[] array = new Object[] {
					LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.login_wxxcx.name(), LoggerType.login_wxxcx.version, "2051")),
					player.getData().getName() == null ? "null" : player.getData().getName(), player.getGameClient().getIp(),
					player.getCurrencyModule().getCount(Asset.diamond.ID),
					player.getAccount().getClue_token() == null ? "{}" : player.getAccount().getClue_token() };
			LoggerType.login_wxxcx.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

	/**
	 * 创建角色
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
	 * 账号id，角色id，角色等级，班级id，设备唯一标识
	 * 角色名`，角色性别，时区
	 */
	public static void rolebuild(Player player) {
		try {
			Object[] array = new Object[] {
					LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.rolebuild.name(), LoggerType.rolebuild.version, "3025")),
					player.getData().getName() == null ? "null" : player.getData().getName(), player.getData().getGender() ? 1 : 2,
					player.getAccount().getPlatform() };
			LoggerType.rolebuild.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

	/**
	 * 创建角色
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
	 * 账号id，角色id，角色等级，班级id，设备唯一标识
	 * 角色名，登录ip，价值虚拟币总量，时区
	 */
	public static void rolelogin(Player player) {
		try {
			Object[] array = new Object[] {
					LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.rolelogin.name(), LoggerType.rolelogin.version, "3030")),
					player.getData().getName() == null ? "null" : player.getData().getName(), player.getCurrencyModule().getCount(Asset.diamond.ID),
					player.getAccount().getPlatform() };
			LoggerType.rolelogin.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

	/**
	 * 登出游戏
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
	 * 账号id，角色id，角色等级，班级id，设备唯一标识
	 * 角色名，登录ip，价值虚拟币总量，在线时长（单位S）, Vip等级, 剩余体力值, 获取总卡牌数, 获取去重卡牌数, 时区
	 */
	public static void logout(Player player) {
		try {
			Object[] array = new Object[] {
					LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.logout.name(), LoggerType.logout.version, "9999")),
					player.getData().getName(),
					player.getCurrencyModule().getCount(Asset.diamond.ID), GameLogAssistant.calculatePlayerOnlineDurationSecond(player),
					player.getVipLevel(),
					player.getCurrencyModule().getCount(Asset.playerEnergy.ID), player.getAttrModule().getPower(),
					player.getChapterModule().getFightBattleId(DungeonTypeEnum.BattleChapter.getId()),
					player.getAccount().getPlatform() };
			LoggerType.logout.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

	/**
	 * 升级
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
	 * 账号id，角色id，角色等级，班级id，设备唯一标识
	 * 角色名，升级后等级,升级前等级,升级时长,时区
	 */
	public static void levelUp(Player player) {
		try {
			Object[] array = new Object[] {
					LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.levelup.name(), LoggerType.levelup.version, "6010")),
					player.getData().getName(), player.getLevel(), Math.max(0, player.getLevel() - 1), -1,
					player.getChapterModule().getFightBattleId(DungeonTypeEnum.BattleChapter.getId()), player.getAccount().getPlatform() };
			LoggerType.levelup.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

	public static void getHero(Player player, Hero hero, OpType opType) {
		try {
			HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
			Object[] array = new Object[] {
					LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.gethero.name(), LoggerType.gethero.version, "B8110")),
					hero.getConfigId(), heroConfig.InitialQuality, hero.getId(), opType, player.getHeroModule().list().size(),
					player.getHeroModule().getSizeDeduplication(), player.getAccount().getPlatform() };
			LoggerType.gethero.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

	/** 
	 * 
	 * @param player
	 * @param hero
	 * @param operatetype 1:升级
	2:进阶
	3.升星
	
	 * @param opType
	 */
	public static void heroraise(Player player, Hero hero, int operatetype, int addvalue, int endvalue, int beforeCombat, int afterCombat) {
		try {
			String step = "B8210";
			if (operatetype == 2) {
				step = "B8211";
			}
			if (operatetype == 3) {
				step = "B8212";
			}
			Object[] array = new Object[] { LoggerType
					.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.heroraise.name(), LoggerType.heroraise.version, step)),
					hero.getConfigId(), operatetype, addvalue <= 0 ? 1 : addvalue, endvalue, beforeCombat, afterCombat,
					player.getAccount().getPlatform() };
			LoggerType.heroraise.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

	/**
	 * 商城日志
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
	 * 账号id，角色id，角色等级，班级id，设备唯一标识
	 * 商品类型id,商品id,商品数量,消耗货币id,消耗货币数量,商城id,
	 * vip等级
	 * 时区
	 */
	public static void shoptrade(Player player, int shopId, int shopItemId) {
		try {
			ShopItemConfig shopItemConfig = ShopItemManager.instance().get(shopItemId);
			int itemId = shopItemConfig.Item[0][0];
			long itemCount = shopItemConfig.Item[0][1];
			int itemType = ItemHelper.getGoodsType(itemId);

			int[] buyParam = shopItemConfig.PurchaseParameter;
			int costId = 0;
			int costCount = 0;
			if (buyParam.length == 0 || buyParam.length == 1) {
				costId = Asset.gold.ID;
				costCount = 0;
			} else if (buyParam.length == 3) {
				costId = buyParam[1];
				costCount = buyParam[2];
			}
			Object[] array = new Object[] {
					LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.shoptrade.name(), LoggerType.shoptrade.version, "7010")), itemType,
					itemId, itemCount, costId, costCount, shopId, player.getVipLevel(), player.getAccount().getPlatform() };
			LoggerType.shoptrade.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

//	/**
//	 * 完成称号任务
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 称号id,称号小类,时区
//	 */
//    public static void achievement(Player player, int titleId, int type) {
//        try {
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.achievement.name(), LoggerType.achievement.version, "B5110")),
//                    titleId, type,
//                    GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.achievement.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
	/**
	 * 货币获得与消耗
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
	 * 账号id，角色id，角色等级，班级id，设备唯一标识
	 * 获得或消耗方式id, 获得或消耗数量, 获得或消耗后总量, 货币id,Vip等级, 获得与消耗位置, 行为 ,时区
	 */
	public static void money(Player player, int id, long count, OpType opType, boolean isIncrease) {
		try {
			Object[] array = new Object[] {
					LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.money.name(), LoggerType.money.version, "8010")),
					opType.name(), count, player.getCurrencyModule().getCount(id), id, player.getVipLevel(),
					"null", isIncrease ? 1 : -1, player.getAccount().getPlatform() };
			LoggerType.money.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

	/**
	 * 货币获得与消耗
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
	 * 账号id，角色id，角色等级，班级id，设备唯一标识
	 * 物品类型id , 物品id, 获得或消耗方式id, 获得或消耗数量, Vip等级, 获得或消耗位置, 行为, 剩余总量, 时区
	 */
	public static void item(Player player, int id, int count, OpType opType, boolean isAdd) {
		try {
			int type = ItemHelper.getGoodsType(id);

			Object[] array = new Object[] {
					LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.item.name(), LoggerType.item.version, "B2110")), type, id,
					opType.name(), count, player.getVipLevel(),
					"null", isAdd ? 1 : -1, player.getGoodsModule(id).getCount(id),
					player.getAccount().getPlatform() };
			LoggerType.item.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
//
	/**
	 * 充值
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
	 * 账号id，角色id，角色等级，班级id，设备唯一标识
	 * 充值额度（RMB元）,充值渠道id, 价值虚拟币数量, 币种, 用户进行充值时设备的ip,
	 * 价值虚拟币总量, Vip等级,商品id, 订单号
	 * 时区
	 */
	public static void recharge(Player player, PayItem payItem) {
		try {
//			int cur = "CNY".equals(currency) ? 11 : 1;
			int cur = 1;
			int stepNum = payItem.getPayType().getId() + 5000;
			Object[] array = new Object[] { LoggerType
					.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.recharge.name(), LoggerType.recharge.version, stepNum + "")),
					payItem.getRmb(), player.getAccount().sdkPayChannel, payItem.getAddCount(), cur, player.getGameClient().getIp(),
					payItem.getAddId() > 0 ? player.getGoodsModule(payItem.getAddId()).getCount(payItem.getAddId())
							: player.getCurrencyModule().getCount(Asset.diamond.ID),
					player.getVipLevel(),
					payItem.getPayId(),
					payItem.getOrderId(),
					player.getAccount().getPlatform() };
			LoggerType.recharge.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

	/** 
	 * 活动。 
	 * @param player
	 * @param activityId
	 * @param subId
	 */
	public static void activity(Player player, int activityId, long subId) {
		try {
			Object[] array = new Object[] { LoggerType
					.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.activity.name(), LoggerType.activity.version, "B6110")),
					activityId, subId };
			LoggerType.activity.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

//	/**
//	 * 充值
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 激活码id ,礼包ID
//	 * 时区
//	 */
//    public static void activationcode(Player player, String cdKey, int id) {
//        try {
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.activationcode.name(), LoggerType.activationcode.version, "B7110")),
//                    cdKey, id,
//                    GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.activationcode.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//
//	/**
//	 * pvp 战斗
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 上阵英雄战力， 我放战斗前排名，我放战斗后排名，我放战斗前分数， 我方战斗后分数， 战场ID，
//	 * 对手角色ID， 对手角色名， 对手队伍等级， 对手战力，对手战斗前排名，对手战斗后排名，对手战斗前分数，对手战斗后分数，
//	 * 战斗结果，持续时长，回合数，
//	 * 时区
//	 */
//    public static void pvpfight(Player player, int type, int atkOldRank, int atkNewRank, long atkOldScore, long atkNewScore, StageType stageType,
//                                long defId, String defName, int defLevel, int defOldRank, int defNewRank, long defOldScore, long defNewScore,
//                                boolean atkWin, long battleTime, int battleRound
//    ) {
////        try {
////            Object[] array = new Object[]{
////                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.pvpfight.name(), LoggerType.pvpfight.version, type == 1 ? "B8310" : "B8320")),
////                    0, atkOldRank, atkNewRank, atkOldScore, atkNewScore, stageType.getId(),
////                    defId, defName == null ? "null" : defName, defLevel, 0, defOldRank, defNewRank, defOldScore, defNewScore,
////                    atkWin ? -1 : 1,
////                    -1,
////                    battleRound,
////                    GameLogAssistant.TIME_ZONE
////            };
////            LoggerType.pvpfight.logger.info(LoggerType.splice(array));
////        } catch (Exception e) {
////            SystemLogger.error(e);
////        }
//    }
//
//
//	/**
//	 * [C0119] 领地（land）三级日志
//	 * <p>
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 领地等级， 苦工数量， 苦工疲劳
//	 * 时区
//	 */
//    public static void land(Player player, int level, int workerCount, int workerTime) {
//        try {
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.land.name(), LoggerType.land.version, "C0119")),
//                    level,
//                    workerCount,
//                    workerTime,
//                    GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.land.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//	/**
//	 * [C0123] 领地刷新（landfresh）三级日志
//	 * <p>
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 领地等级， 苦工数量， 苦工疲劳, 刷新形式（1：广告 2:钻石）， 刷新前领地剩余物品数量（矿车数量）， 剩余免费次数(广告), 观看广告次数
//	 * 时区
//	 */
//    public static void landfresh(Player player, int level, int workerCount, int workerTime, boolean isAd, int resourceCountBefore, int adCount, int doAdCount) {
//        try {
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.landfresh.name(), LoggerType.landfresh.version, "C0123")),
//                    level,
//                    workerCount,
//                    workerTime,
//                    isAd ? 1 : 2,
//                    resourceCountBefore,
//                    adCount,
//                    doAdCount,
//                    GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.landfresh.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//
//	/**
//	 * [C0125]  领地提升（landupdate）三级日志
//	 * <p>
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 操作类型（1：领地升级，2：苦工增加）， 升级前领地等级, 升级前苦工数量, 升级后领地等级, 升级后苦工数量
//	 * 时区
//	 */
//    public static void landupdate(Player player, boolean isLevelUp, int beforeLv, int beforeWorker, int afterLv, int afterWorker) {
//        try {
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.landupdate.name(), LoggerType.landupdate.version, "C0125")),
//                    isLevelUp ? 1 : 2,
//                    beforeLv,
//                    beforeWorker,
//                    afterLv,
//                    afterWorker,
//                    GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.landupdate.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//
	/**
	 * 任务
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
	 * 账号id，角色id，角色等级，班级id，设备唯一标识
	 * 任务id,结果,任务类型 时区
	 */
	public static void task(Player player, int taskId, boolean finish) {
		try {
			String stepnumid = "B3110";
			if (finish) {
				stepnumid = "B3120";
			}
			QuestConfig questConfig = QuestManager.instance().get(taskId);

			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.task.name(), LoggerType.task.version, stepnumid)),
					taskId, "1", questConfig.Type + "", player.getAccount().getPlatform() };
			LoggerType.task.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

	/** 
	 * 成就
	 * @param player
	 * @param taskId
	 */
	public static void achievement(Player player, int taskId) {
		try {
			QuestConfig questConfig = QuestManager.instance().get(taskId);
			ConditionConfig conditionConfig = ConditionManager.instance().get(questConfig.Condition);
			ConditionTypeEnum conditionTypeEnum = ConditionTypeEnum.get(conditionConfig.type);
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
									.buildLogCYPrefix(player, LoggerType.achievement.name(), LoggerType.achievement.version, "B5110")),
					taskId, conditionTypeEnum.name(), -1 };
			LoggerType.achievement.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

	/**
	 * pve 战斗
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
	 * 账号id，角色id，角色等级，班级id，设备唯一标识
	 * 关卡id,战斗类型,NpcId,战斗结果,地图id,评价星级,关卡类型,关卡用时(秒),关卡所用回合
	 * 剩余体力,上阵卡牌战力总和,当前关卡推荐战力,上阵卡牌1,上阵卡牌2,上阵卡牌3,角色属性,
	 * 时区
	 */
	public static void pvefight(Player player, int stageId, int type, boolean result, long time, int battleCount) {
		try {
			type = 1;
//			BattleConfig battleConfig = BattleManager.instance().get(stageId);
			int re = 2;
			if (result) {
				re = 1;
			}
			List<Hero> battleHeros = player.getHeroModule().getBattleHeroList();
			List<Integer> heroList = battleHeros.stream().map(r -> r.getConfigId()).collect(toList());

			Object[] array = new Object[] {
					LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.pvefight.name(), LoggerType.pvefight.version, "B4100")), stageId,
					type, re, "null", time,
					heroList.size() > 0 ? heroList.get(0) : "null",
					heroList.size() > 1 ? heroList.get(1) : "null", heroList.size() > 2 ? heroList.get(2) : "null",
					heroList.size() > 3 ? heroList.get(3) : "null", heroList.size() > 4 ? heroList.get(4) : "null",
					heroList.size() > 5 ? heroList.get(5) : "null",
					player.getAccount().getPlatform(), player.getAttrModule().getPower(), battleCount
			};
			LoggerType.pvefight.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

	/**
	 *
	 *
	 * @param player
	 * @param startFlag
	 * @param selfRankBf
	 * @param selfRankAf
	 * @param battleType
	 * @param targetPlayer
	 * @param targetRankBf
	 * @param targetRankAf
	 * @param battleTime
	 */
	public static void pvpfight(Player player,boolean startFlag, int selfRankBf, int selfRankAf, int battleType, SimplePlayer targetPlayer, int targetRankBf, int targetRankAf, int battleTime, int endType,boolean win){
		int sellteType = -1; //1:胜利 0:超时失败 -1:战斗失败 2：退出
		if (!startFlag) {
		  if (endType == 0 && win) { // 结算类型: 0 正常结算; 1 主动退出战斗; 2 扫荡结算
			sellteType = 1;
		  } else if (endType == 1) {
			{
			  sellteType = 2;
			}
		  }
		}

		try {
			String stepnumid = startFlag? "B8310" : "B8320";

			Object[] array = new Object[] {
					LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.pvpfight.name(), LoggerType.pvpfight.version, stepnumid)),
					player.getAttrModule().getPower(), //上阵英雄战力
					selfRankBf, //战斗前排名
					selfRankAf, //战斗后排名
					battleType, //战场id
					targetPlayer.id , //对手角色id
					targetPlayer.getName(), //对手角色名
                    targetPlayer.getLevel(), //对手角色等级
					targetPlayer.getCombatEffectiveness(), //对手战力
                    targetRankBf, //对手战斗前排名
					targetRankAf, //对手战斗后排名
					sellteType, //战斗结果
                    battleTime, //持续时长
					player.getAccount().getPlatform() //平台标识
			};
			LoggerType.pvpfight.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}

	}
//
	/**
	 * 新手引导
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
	 * 账号id，角色id，角色等级，班级id，设备唯一标识
	 * 引导步
	 * 时区
	 */
	public static void newstages(Player player, int big, int small) {
		try {
			int step = 4000 + big * 10;
			small = big * 1000 + small;
			String stepNum = String.valueOf(step);
			Object[] array = new Object[] { LoggerType
					.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.newstages.name(), LoggerType.newstages.version, stepNum)),
					small };
			LoggerType.newstages.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
//
//
//	/**
//	 * C0102 装备幻化
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 选择前装备id,选择后装备id,装备类型
//	 */
//    public static void equipmentshape(Player player, int beforeShapeId, int afterShapeId, int position) {
//        try {
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.equipmentshape.name(), LoggerType.equipmentshape.version, "C0102")),
//                    beforeShapeId, afterShapeId, position, GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.equipmentshape.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//	/**
//	 * C0103 翅膀强化
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 操作类型:(1.升级 2.升阶),单次升级增加的经验,操作前的等级,操作后的等级阶数
//	 */
//    public static void wingupdate(Player player, int wingId, int type, int exp, int beforeLv, int beforeStage, int afterLv, int afterStage) {
//        try {
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.wingupdate.name(), LoggerType.wingupdate.version, "C0103")),
//                    wingId, type, type == 1 ? exp : -1, type == 1 ? beforeLv : beforeStage, type == 1 ? afterLv : afterStage, GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.wingupdate.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//	/**
//	 * C0104 挂机
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 操作类型:(1:普通挂机 2:快速挂机),该次挂机总时长(s),当日剩余快速挂机次数,玩家总快速挂机次数
//	 */
//    public static void onhook(Player player, int type, long hangupTime, int dailyLimitHangupTime, int totalHangupTime) {
//        try {
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.onhook.name(), LoggerType.onhook.version, "C0104")),
//                    type, hangupTime, dailyLimitHangupTime, totalHangupTime, GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.onhook.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//	/**
//	 * C0105 宝箱升级
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 操作前宝箱等级,操作后宝箱等级,加速卷使用数量,升级实际所用时间
//	 */
//    public static void boxlevelup(Player player, int beforeBoxLevel, int afterBoxLevel, long quantityNum, long time, long useAdTime) {
//        try {
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.boxlevelup.name(), LoggerType.boxlevelup.version, "C0105")),
//                    beforeBoxLevel, afterBoxLevel, quantityNum, time, useAdTime, GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.boxlevelup.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//	/**
//	 * C0106 宝箱开启
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 装备实例id,装备模板id,装备品质,装备等级,挑战卷数量
//	 */
//    public static void boxopen(Player player, int equipId, int equipTemplate, int quality, int level, int num) {
//        try {
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.boxopen.name(), LoggerType.boxopen.version, "C0106")),
//                    equipId, equipTemplate, quality, level, num, GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.boxopen.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//	/**
//	 * C0107 荣誉榜快照
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 榜单类型,榜单名次,榜单对应值,
//	 * 时区
//	 */
//    public static void rankkz(MiniPlayer miniPlayer, int type, long rank, long value) {
//        try {
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(miniPlayer, LoggerType.rankkz.name(), LoggerType.rankkz.version, "C0107")),
//                    type, rank, value, GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.rankkz.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//
//	/**
//	 * C0109 聊天
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 聊天频道,聊天类型,聊天内容,对方id,是否为好友,
//	 * 时区
//	 */
//    public static void chat(Player player, int channelId, String typeId, String content, long targetId, boolean friend) {
//        try {
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.chat.name(), LoggerType.chat.version, "C0109")),
//                    channelId, typeId, content, targetId, friend ? 1 : 0, GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.chat.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//	/**
//	 * C0110 邮箱
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 邮件id,邮件名称,邮件类型(1:无奖励邮件 2:有奖励邮件),操作类型
//	 * 时区
//	 */
//    public static void mailbox(Player player, long mailId, long mailName, int mailType, int type) {
//        try {
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.mailbox.name(), LoggerType.mailbox.version, "C0110")),
//                    mailId, mailName, mailType, type, GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.mailbox.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//	/**
//	 * C0111 玩家信息修改
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 操作类型,原内容,新内容
//	 * 时区
//	 */
//    public static void roleinfochange(Player player, int type, String oldString, String newString) {
//        try {
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.roleinfochange.name(), LoggerType.roleinfochange.version, "C0111")),
//                    type, oldString, newString, GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.roleinfochange.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//	/**
//	 * C0113 宝石合成
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 合成结果,合成宝石id,合成宝石品质
//	 * 合成消耗宝石1,合成消耗宝石品质1
//	 * 合成消耗宝石2,合成消耗宝石品质2
//	 * 合成消耗宝石3,合成消耗宝石品质3
//	 * 时区
//	 */
//    public static void mergegems(Player player, int result, int newGemId, int newGemQuality, List<Pair<Integer, Integer>> costGemMap) {
//        try {
//            Object[] gemLogDat = {0, 0, 0, 0, 0, 0};
//            if (CollectionUtil.isNotEmpty(costGemMap)) {
//                int size = Math.min(3, costGemMap.size());
//                for (int i = 0; i < size; i++) {
//                    Pair<Integer, Integer> pair = costGemMap.get(i);
//                    gemLogDat[i * 2] = pair.getLeft();
//                    gemLogDat[i * 2 + 1] = pair.getRight();
//                }
//            }
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.mergegems.name(), LoggerType.mergegems.version, "C0113")),
//                    result, newGemId, newGemQuality,
//                    gemLogDat[0], gemLogDat[1], gemLogDat[2], gemLogDat[3], gemLogDat[4], gemLogDat[5], GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.mergegems.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//	/**
//	 * C0114 玩家属性快照
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 属性信息
//	 * 时区
//	 */
//    public static void propertysnap(MiniPlayer miniPlayer) {
////        try {
////            Player player = PlayerManager.getOnlinePlayer(miniPlayer.getPlayerId());
////            if (player != null) {//在线玩家取最新
////                Map<Integer, Long> playerAttribute = player.getRoleModel().getAttributes().getAttributeMap();
////                if (CollectionUtil.isNotEmpty(playerAttribute)) {
////                    Object[] array = new Object[]{
////                            LoggerType.splice(GameLogAssistant.buildLogCYPrefix(miniPlayer, LoggerType.propertysnap.name(), LoggerType.propertysnap.version, "C0114")),
////                            playerAttribute.get(AttributeKey.速度.getId()) == null ? 0 : playerAttribute.get(AttributeKey.速度.getId()),
////                            playerAttribute.get(AttributeKey.生命.getId()) == null ? 0 : playerAttribute.get(AttributeKey.生命.getId()),
////                            playerAttribute.get(AttributeKey.atk.getId()) == null ? 0 : playerAttribute.get(AttributeKey.atk.getId()),
////                            playerAttribute.get(AttributeKey.防御.getId()) == null ? 0 : playerAttribute.get(AttributeKey.防御.getId()),
////                            playerAttribute.get(AttributeKey.吸血率.getId()) == null ? 0 : playerAttribute.get(AttributeKey.吸血率.getId()),
////                            playerAttribute.get(AttributeKey.反击率.getId()) == null ? 0 : playerAttribute.get(AttributeKey.反击率.getId()),
////                            playerAttribute.get(AttributeKey.连击率.getId()) == null ? 0 : playerAttribute.get(AttributeKey.连击率.getId()),
////                            playerAttribute.get(AttributeKey.闪避率.getId()) == null ? 0 : playerAttribute.get(AttributeKey.闪避率.getId()),
////                            playerAttribute.get(AttributeKey.暴击率.getId()) == null ? 0 : playerAttribute.get(AttributeKey.暴击率.getId()),
////                            playerAttribute.get(AttributeKey.击晕率.getId()) == null ? 0 : playerAttribute.get(AttributeKey.击晕率.getId()),
////                            playerAttribute.get(AttributeKey.吸血抵抗.getId()) == null ? 0 : playerAttribute.get(AttributeKey.吸血抵抗.getId()),
////                            playerAttribute.get(AttributeKey.反击抵抗.getId()) == null ? 0 : playerAttribute.get(AttributeKey.反击抵抗.getId()),
////                            playerAttribute.get(AttributeKey.连击抵抗.getId()) == null ? 0 : playerAttribute.get(AttributeKey.连击抵抗.getId()),
////                            playerAttribute.get(AttributeKey.闪避抵抗.getId()) == null ? 0 : playerAttribute.get(AttributeKey.闪避抵抗.getId()),
////                            playerAttribute.get(AttributeKey.暴击抵抗.getId()) == null ? 0 : playerAttribute.get(AttributeKey.暴击抵抗.getId()),
////                            playerAttribute.get(AttributeKey.击晕抵抗.getId()) == null ? 0 : playerAttribute.get(AttributeKey.击晕抵抗.getId()),
////                            playerAttribute.get(AttributeKey.暴击伤害.getId()) == null ? 0 : playerAttribute.get(AttributeKey.暴击伤害.getId()),
////                            playerAttribute.get(AttributeKey.暴伤抵抗.getId()) == null ? 0 : playerAttribute.get(AttributeKey.暴伤抵抗.getId()),
////                            playerAttribute.get(AttributeKey.迟缓.getId()) == null ? 0 : playerAttribute.get(AttributeKey.迟缓.getId()),
////                            playerAttribute.get(AttributeKey.禁疗.getId()) == null ? 0 : playerAttribute.get(AttributeKey.禁疗.getId()),
////                            playerAttribute.get(AttributeKey.治愈率.getId()) == null ? 0 : playerAttribute.get(AttributeKey.治愈率.getId()),
////                            playerAttribute.get(AttributeKey.重击.getId()) == null ? 0 : playerAttribute.get(AttributeKey.重击.getId())
////                            , GameLogAssistant.TIME_ZONE
////                    };
////                    LoggerType.propertysnap.logger.info(LoggerType.splice(array));
////                }
////            } else {//离线玩家取存储的数据
////                Player offlinePlayer = PlayerManager.getPlayer(miniPlayer.getPlayerId());
////                if (offlinePlayer != null) {
////                    PlayerRoleModel playerRoleModel = offlinePlayer.getRoleModel();
////                    if (playerRoleModel != null) {
////                        Map<Integer, Long> playerAttribute = offlinePlayer.getRoleModel().getAttributes().getAttributeMap();
////                        if (CollectionUtil.isNotEmpty(playerAttribute)) {
////                            Object[] array = new Object[]{
////                                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(miniPlayer, LoggerType.propertysnap.name(), LoggerType.propertysnap.version, "C0114")),
////                                    playerAttribute.get(AttributeKey.速度.getId()) == null ? 0 : playerAttribute.get(AttributeKey.速度.getId()),
////                                    playerAttribute.get(AttributeKey.生命.getId()) == null ? 0 : playerAttribute.get(AttributeKey.生命.getId()),
////                                    playerAttribute.get(AttributeKey.atk.getId()) == null ? 0 : playerAttribute.get(AttributeKey.atk.getId()),
////                                    playerAttribute.get(AttributeKey.防御.getId()) == null ? 0 : playerAttribute.get(AttributeKey.防御.getId()),
////                                    playerAttribute.get(AttributeKey.吸血率.getId()) == null ? 0 : playerAttribute.get(AttributeKey.吸血率.getId()),
////                                    playerAttribute.get(AttributeKey.反击率.getId()) == null ? 0 : playerAttribute.get(AttributeKey.反击率.getId()),
////                                    playerAttribute.get(AttributeKey.连击率.getId()) == null ? 0 : playerAttribute.get(AttributeKey.连击率.getId()),
////                                    playerAttribute.get(AttributeKey.闪避率.getId()) == null ? 0 : playerAttribute.get(AttributeKey.闪避率.getId()),
////                                    playerAttribute.get(AttributeKey.暴击率.getId()) == null ? 0 : playerAttribute.get(AttributeKey.暴击率.getId()),
////                                    playerAttribute.get(AttributeKey.击晕率.getId()) == null ? 0 : playerAttribute.get(AttributeKey.击晕率.getId()),
////                                    playerAttribute.get(AttributeKey.吸血抵抗.getId()) == null ? 0 : playerAttribute.get(AttributeKey.吸血抵抗.getId()),
////                                    playerAttribute.get(AttributeKey.反击抵抗.getId()) == null ? 0 : playerAttribute.get(AttributeKey.反击抵抗.getId()),
////                                    playerAttribute.get(AttributeKey.连击抵抗.getId()) == null ? 0 : playerAttribute.get(AttributeKey.连击抵抗.getId()),
////                                    playerAttribute.get(AttributeKey.闪避抵抗.getId()) == null ? 0 : playerAttribute.get(AttributeKey.闪避抵抗.getId()),
////                                    playerAttribute.get(AttributeKey.暴击抵抗.getId()) == null ? 0 : playerAttribute.get(AttributeKey.暴击抵抗.getId()),
////                                    playerAttribute.get(AttributeKey.击晕抵抗.getId()) == null ? 0 : playerAttribute.get(AttributeKey.击晕抵抗.getId()),
////                                    playerAttribute.get(AttributeKey.暴击伤害.getId()) == null ? 0 : playerAttribute.get(AttributeKey.暴击伤害.getId()),
////                                    playerAttribute.get(AttributeKey.暴伤抵抗.getId()) == null ? 0 : playerAttribute.get(AttributeKey.暴伤抵抗.getId()),
////                                    playerAttribute.get(AttributeKey.迟缓.getId()) == null ? 0 : playerAttribute.get(AttributeKey.迟缓.getId()),
////                                    playerAttribute.get(AttributeKey.禁疗.getId()) == null ? 0 : playerAttribute.get(AttributeKey.禁疗.getId()),
////                                    playerAttribute.get(AttributeKey.治愈率.getId()) == null ? 0 : playerAttribute.get(AttributeKey.治愈率.getId()),
////                                    playerAttribute.get(AttributeKey.重击.getId()) == null ? 0 : playerAttribute.get(AttributeKey.重击.getId())
////                                    , GameLogAssistant.TIME_ZONE
////                            };
////                            LoggerType.propertysnap.logger.info(LoggerType.splice(array));
////                        }
////                    }
////                }
////            }
////        } catch (Exception e) {
////            SystemLogger.error(e);
////        }
//    }
//
//
//	/**
//	 * C0116 坐骑招募刷新
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 刷新类型,刷出的野马数量和品质
//	 * 时区
//	 */
//    public static void horserefresh(Player player, int type, List<Pair<Integer, Integer>> wildInfo) {
//    }
//
//	/**
//	 * C0117 坐骑招募刷新
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 刷新类型,刷出的野马数量和品质
//	 * 时区
//	 */
//    public static void horsesummon(Player player, int wildTemplateId, int wildHorseId, int wilHorseQuality, int costNum, int getHorseTemplateId, int getHorseId, int getHorseQuality) {
//    }
//
//	/**
//	 * C0118 坐骑图鉴
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 图鉴id,已激活马匹,是否激活属性
//	 * 时区
//	 */
//    public static void horseImage(Player player, int imageId, List<Integer> canUseHorse, boolean active, int horseTemplateId, int horseId) {
//    }
//
//	/**
//	 * [C0121] 公会成员快照（guildsnap）三级日志
//	 * <p>
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 公会职位,今日贡献
//	 * 时区
//	 */
//    public static void guildsnap(MiniPlayer miniPlayer, PbCommons.LeagueJob leagueJob, int todayActive) {
//        try {
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(miniPlayer, LoggerType.guildsnap.name(), LoggerType.guildsnap.version, "C0121")),
//                    leagueJob.getNumber(), todayActive,
//                    GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.guildsnap.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//	/**
//	 * [C0122] 翅膀解锁（wingunlock）三级日志
//	 * <p>
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 翅膀模板id,购买前拥有翅膀数量,购买后拥有翅膀数量
//	 * 时区
//	 */
//    public static void wingunlock(Player player, int wingTemplateId, int beforeNum, int afterNum) {
//        try {
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.wingunlock.name(), LoggerType.wingunlock.version, "C0122")),
//                    wingTemplateId, beforeNum, afterNum,
//                    GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.wingunlock.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//	/**
//	 * 广告观看
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * 活动id ,活动档位
//	 * 时区
//	 */
//    public static void adwatching(Player player, int adposition) {
//        try {
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.adwatching.name(), LoggerType.adwatching.version, "C0126")),
//                    adposition, 1,
//                    GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.adwatching.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
//	/**
//	 * 邮件
//	 * 时间、日志模块名、日志版本、匿名ID、角色ID、账号id、区服、角色名、角色等级、VIP等级、邮件类型、邮件类型描述、标题、附件内容
//	 */
//    public static void mail(MiniPlayer miniPlayer, Mail mail) {
//        String title = String.valueOf(mail.getInfo().getTitle().getMessageId());
//        if (mail.getType() == MailType.system_msg || mail.getType() == MailType.system_repay || mail.getType() == MailType.system_welfare) {
//            if (mail.getInfo().getTitle().getParamsCount() > 0) {
//                title = mail.getInfo().getTitle().getParams(0).getText();
//            }
//        }
//
//        Object[] array = new Object[]{getCurrentTimeLogText(), LoggerType.mail.name(), LoggerType.mail.version, LoggerType.splice(generatePlayerLogPrefix(miniPlayer)), mail.getType().name(), mail.getType().getDesc(), title, Reward.toString(mail.getInfo().getAttachments())};
//        LoggerType.mail.logger.info(LoggerType.splice(array));
//    }
//
//	/**
//	 * C0127 公会讨伐
//	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
//	 * 账号id，角色id，角色等级，班级id，设备唯一标识
//	 * bossId,单次挑战玩家造成伤害,挑战前玩家排名,挑战后玩家排名,挑战前公会排名,挑战后公会排名,挑战累计伤害,挑战次数类型
//	 */
//    public static void leagueboss(Player player, int bossId, long damage, long beforePlayerRank, long afterPlayerRank, long beforeLeagueRank, long afterLeagueRank, long dailyTotalDamage, int challengeType) {
//        try {
//            Object[] array = new Object[]{
//                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.leagueboss.name(), LoggerType.leagueboss.version, "C0127")),
//                    bossId, damage, beforePlayerRank, afterPlayerRank, beforeLeagueRank, afterLeagueRank, dailyTotalDamage, challengeType, GameLogAssistant.TIME_ZONE
//            };
//            LoggerType.leagueboss.logger.info(LoggerType.splice(array));
//        } catch (Exception e) {
//            SystemLogger.error(e);
//        }
//    }
//
////    public static void rankNumInfo(RankType rankType, int cacheRankNum, int dbRankNum) {
////        LoggerType.rankNumInfo.logger.info(String.format("rank Type : %s, cacheRankNum : %s, dbRankNum : %s ", rankType.name(), cacheRankNum, dbRankNum));
////    }
//
////    public static void rankInfo(boolean isEndServer, RankItem item) {
////        LoggerType.rankInfo.logger.info(String.format("/***********************************isEndServer:%s***************************************/", isEndServer));
////        LoggerType.rankInfo.logger.info(String.format("rank info , id : %s, rankId : %s, itemIdText : %s, valueText %s, extraText : %s", item.getId(), item.getRankId(), item.getItemIdText(), item.getValueText(), item.getExtraText()));
////    }
//
//
//	/**
//	 * 创建订单
//	 * 时间、日志模块名、日志版本、角色ID、账号id、区服、角色名、角色等级、VIP等级、内部订单号、充值渠道、内部商品Id、第三方商品Id、商品价格x100、币种、IP
//	 */
//    public static void orderCreate(Player player, Receipt receipt, PayItem payItem) {
//        Object[] array = new Object[]{getCurrentTimeLogText(), LoggerType.orderCreate.name(), LoggerType.orderCreate.version, LoggerType.splice(generatePlayerLogPrefix(player)), receipt.getOrderId(), receipt.getChannel(), receipt.getGoodsId(), receipt.getCoGoodsId(), receipt.getPrice(), payItem.currency, player.getIp()};
//        LoggerType.orderCreate.logger.info(LoggerType.splice(array));
//    }
//
//	/**
//	 * 订单完成
//	 * 时间、日志模块名、日志版本、角色ID、账号id、区服、角色名、角色等级、VIP等级、内部订单号、充值渠道、内部商品Id、第三方商品Id、商品价格x100、币种、第三方订单号、VIP经验
//	 */
//    public static void orderFinish(Player player, Receipt receipt, PayItem payItem, boolean isFirstCharge, int oldVipLevel) {
//        if (player.isRobot()) {
//            return;
//        }
//        int activityId = 0;
//        int itemId = 0;
//        PayActivityData activityItem = player.getPayModel().getActivityOrderItems().get(receipt.getOrderId());
//        if (activityItem != null) {
//            activityId = activityItem.getActivityId();
//            itemId = activityItem.getItemId();
//        }
//        Object[] array = new Object[]{getCurrentTimeLogText(), LoggerType.orderFinish.name(), LoggerType.orderFinish.version, LoggerType.splice(generatePlayerLogPrefix(player)), receipt.getOrderId(), receipt.getChannel(), receipt.getGoodsId(), receipt.getCoGoodsId(), receipt.getPrice(), payItem.currency, receipt.getCoOrderId(),
//                isFirstCharge ? 1 : 0, player.getIp(), activityId, itemId, oldVipLevel,};
//        LoggerType.orderFinish.logger.info(LoggerType.splice(array));
//    }
//
//
//	/**
//	 * 1 ：ios app 2： 安卓  app
//	 * 3 ：ios  微信小游戏 4 ：安卓 微信小游戏
//	 * 5：window 微信小游戏 6： mac 微信小游戏
//	 */
//    public static int getPlatformCode(String deviceType, int smallGame) {
//        int rst = 2;
//        if (Account.DEVICE_TYPE_ANDROID.equalsIgnoreCase(deviceType)) {
//            rst = smallGame == 0 ? 2 : 4;
//        } else if (Account.DEVICE_TYPE_IOS.equalsIgnoreCase(deviceType)) {
//            rst = smallGame == 0 ? 1 : 3;
////        } else if (Account.DEVICE_TYPE_WINDOWS.equalsIgnoreCase(deviceType)) {
////            rst = 5;
////        } else if (Account.DEVICE_TYPE_MAC.equalsIgnoreCase(deviceType)) {
////            rst = 6;
//        } else { //都不是以上就按 安卓处理
//            rst = smallGame == 0 ? 2 : 4;
//        }
//        return rst;
//    }
//
//
}
