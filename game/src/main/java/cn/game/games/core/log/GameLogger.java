package cn.game.games.core.log;

import static java.util.stream.Collectors.toList;

import java.util.List;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.MonthCard;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.GameServerStatus;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.account.Account;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.games.net.game.module.shop.monthcard.MonthCardModule;
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
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.RewardMsg;
import cn.game.util.Config;
import cn.game.util.StrUtil;
import cn.game.util.log.DeprecatedLogger;
import cn.game.util.log.LoggerType;
import cn.game.util.log.SystemLogger;
public class GameLogger extends DeprecatedLogger {

	/**限时礼包的默认活动id*/
	public final static int ActivityXianShiLiBaoLogId = 10000000;
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
//    public static void clientEvent(LoginCheckReq req, int eventId, String ip) {
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


	public static void serverEvent(Account account, int eventId) {
		String sdkversion = account == null ? "null" : account.sdkVersion;
		String system = "all";
		String adChannel = account == null ? "null" : account.adChannel;
		String sdkDeviceId = account == null ? "null" : account.deviceId;
		String accountId = account == null ? "null" : account.accountId;
		String version = account == null ? "null" : account.version;
		Object[] array = new Object[] { getCurrentTimeLogText(), Config.APP_KEY, sdkversion, system, adChannel, sdkDeviceId, accountId,
				eventId, version };
		LoggerType.serverevent.logger.info(LoggerType.splice(array));
	}


	/**
	 * 心跳
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，在线用户数，排队人数
	 */
	public static void heart() {
		try {
			Object[] array = new Object[] { getCurrentTimeLogText(), Config.APP_KEY,
					GameServerStatus.getInstance().getServerInfo().getVersion(), LoggerType.heart.name(), LoggerType.heart.version,
					"1010", ServerContext.getInstance().getServerId(), PlayerManager.getInstance().getOnlineCount(),0 };
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
		Account account = player.getAccount();

		try {
			Object[] array = new Object[] {
					LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.login.name(), LoggerType.login.version, "2050")),
					player.getData().getName() == null ? "null" : player.getData().getName(),
					player.getGameClient().getIp(), player.getCurrencyModule().getCount(Asset.diamond.ID)
			        ,player.getHeroModule().list().size(),0,
					getMonthCardInfo(player),
                    account.unionid,
					account.openid
			};
			LoggerType.login.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

	public static void login_wxxcx(Player player) {
		Account account = player.getAccount();
		try {
			Object[] array = new Object[] {
					LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.login_wxxcx.name(), LoggerType.login_wxxcx.version, "2051")),
					player.getData().getName() == null ? "null" : player.getData().getName(), player.getGameClient().getIp(),
					player.getCurrencyModule().getCount(Asset.diamond.ID),
					account.unionid,
					account.openid,
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
					player.getAccount().unionid,
					player.getAccount().openid,
					};
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
					getMonthCardInfo(player),
					player.getAccount().unionid,
					player.getAccount().openid,
					 };
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
					player.getCurrencyModule().getCount(Asset.playerEnergy.ID),
					player.getHeroModule().list().size(),0,// 默认
					getMonthCardInfo(player)//月卡剩余天数
			};
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
					player.getData().getName(), player.getLevel(), Math.max(0, player.getLevel() - 1),
					0// 默认
					};
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
					hero.getConfigId(), hero.getId(), opType, player.getHeroModule().list().size()
					,0//默认值
					};
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
	 * @param
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
					hero.getConfigId(), operatetype,"null",1, addvalue <= 0 ? 1 : addvalue, endvalue,"null"
					};
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
					itemId, itemCount, costId, costCount, shopId, player.getVipLevel()};
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
					"null", isIncrease ? 1 : -1 };
			LoggerType.money.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

	/**
	 * 货币获得与消耗
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
	 * 账号id，角色id，角色等级，班级id，设备唯一标识
	 * 物品类型id , 物品id, 获得或消耗方式id, 获得或消耗数量, Vip等级, 获得或消耗位置, 行为, 剩余总量
	 */
	public static void item(Player player, int id, int count, OpType opType, boolean isAdd) {
		try {
			int type = ItemHelper.getGoodsType(id);

			Object[] array = new Object[] {
					LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.item.name(), LoggerType.item.version, "B2110")), type, id,
					opType.name(), count, player.getVipLevel(),
					"null", isAdd ? 1 : -1, player.getGoodsModule(id).getCount(id),
				 };
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
					payItem.getSdkGoodsId(), // 商品id
					payItem.getSdkOrderId(),
					2,
					payItem.getPayId(),
					player.getUnionId(),// unionid
					player.getOpenId()// openid

			};
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
					taskId, "1", questConfig.Type + ""};
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
					taskId, conditionTypeEnum.name()
					,0//默认值
					 };
			LoggerType.achievement.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 邀请好友日志
	 * @param player
	 * @param invitePid 邀请我的人
	 */
	public static void invite(Player player, long invitePid) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.achievement.name(), LoggerType.achievement.version, "C0103"))
					 ,invitePid};
			LoggerType.invite.logger.info(LoggerType.splice(array));
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
	public static void pvefight(Player player, int stageId, int type,  long time,boolean result,int subId) {
		try {
			List<Hero> battleHeros = player.getBattleModule().getDefaultLineupHeroes();
			List<Integer> heroList = battleHeros.stream().map(r -> r.getConfigId()).collect(toList());

			Object[] array = new Object[] {
					LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.pvefight.name(), LoggerType.pvefight.version, "B4100")), stageId,
					type,  "null",result?1:2, "null", subId>0?subId:"null", "null", time, player.getAttrModule().getPower(),
					0,0,
					heroList.size() > 0 ? heroList.get(0) : "null",
					heroList.size() > 1 ? heroList.get(1) : "null", heroList.size() > 2 ? heroList.get(2) : "null",
					heroList.size() > 3 ? heroList.get(3) : "null", heroList.size() > 4 ? heroList.get(4) : "null",
					heroList.size() > 5 ? heroList.get(5) : "null",	heroList.size() > 6 ? heroList.get(6) : "null",
					heroList.size() > 7 ? heroList.get(7) : "null",
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
	//1:胜利  2:战斗失败
	 * @param startFlag
	 * @param targetPlayer

	 */
	public static void pvpfight(Player player,boolean startFlag, SimplePlayer targetPlayer, boolean win){

		try {
			String stepnumid = startFlag? "B8310" : "B8320";

			Object[] array = new Object[] {
					LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.pvpfight.name(), LoggerType.pvpfight.version, stepnumid)),
					player.getAttrModule().getPower(), //上阵英雄战力
					0, //战斗前排名
					0, //战斗后排名
					"NULL", //战场id
					targetPlayer.id , //对手角色id
					targetPlayer.getName(), //对手角色名
                    targetPlayer.getLevel(), //对手角色等级
					targetPlayer.getCombatEffectiveness(), //对手战力
                    0, //对手战斗前排名
					0, //对手战斗后排名
					win, //战斗结果
                    0, //持续时长

			};
			LoggerType.pvpfight.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}

	}

	public static void recruit(Player player, int id ,int count, int beishu,int costId,int costCount) {
		try {

			Object[] array = new Object[] { LoggerType
					.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.recruit.name(), LoggerType.recruit.version, "B9210")),
					0,0,0,beishu,"null",0,id,count,"null",0,"null",costId,costCount
					};
			LoggerType.recruit.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}


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
					small ,0,0};
			LoggerType.newstages.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

	/**
	 * B9510 榜快照
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
	 * 账号id，角色id，角色等级，班级id，设备唯一标识
	 * 榜单类型,榜单名次,榜单对应值,
	 * 时区
	 */
    public static void rank( int type, String rank, String value) {

		try {
            Object[] array = new Object[]{
					getCurrentTimeLogText(), Config.APP_KEY, "null", LoggerType.rank.name(), LoggerType.rank.version, "B9510",
					ServerContext.getInstance().getServerId(),  "null",
					"null","server",
					0, "null","null", "null",0,0,
                    type,0, rank, value,0,0,0
            };
            LoggerType.rank.logger.info(LoggerType.splice(array));
        } catch (Exception e) {
            SystemLogger.error(e);
        }
    }
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
	/**
	 * 广告观看
	 * 时间，游戏标识，客户端版本号，日志模块名，日志版本，步骤号，区服id，推广渠道id
	 * 账号id，角色id，角色等级，班级id，设备唯一标识
	 * 活动id ,活动档位
	 * 时区
	 */
    public static void adwatching(Player player,List<String> info) {

        try {
            Object[] array = new Object[]{
                    LoggerType.splice(GameLogAssistant.buildLogCYPrefix(player, LoggerType.adwatching.name(), LoggerType.adwatching.version, "B9410")),
                    player.getPlayerName(), player.getVipLevel(), info.get(0), info.get(1), info.get(2), info.get(3), info.get(4), info.get(5), info.get(6), info.get(7), info.get(8), info.get(9), info.get(10),
            };
            LoggerType.adwatching.logger.info(LoggerType.splice(array));
        } catch (Exception e) {
            SystemLogger.error(e);
        }
    }
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

	/**
	 * 宝石塔每日刷新buff
	 * @param player  宝石塔类型	宝石塔buff1	宝石塔buff2
	 */
	public static void gemtowerbuff(Player player, int buffId) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.gemtowerbuff.name(), LoggerType.gemtowerbuff.version, "C0200"))
					, buffId};
			LoggerType.gemtowerbuff.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 宝石塔扫荡
	 * @param player  宝石塔类型	扫荡层数id	扫荡总次数	已经扫荡次数	剩余扫荡次数
	 */
	public static void gemtowersweep(Player player, int towerType,int floor, int sweepTotal, int sweepCount, int sweepCountLeft) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.gemtowersweep.name(), LoggerType.gemtowersweep.version, "C0201"))
					,towerType,floor, sweepTotal, sweepCount, sweepCountLeft};
			LoggerType.gemtowersweep.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 装备本助战
	 * @param player  助战层数	助战玩家id
	 */
	public static void equiptowerassist(Player player,int floor,long help) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.equiptowerassist.name(), LoggerType.equiptowerassist.version, "C0301"))
					, floor ,help};
			LoggerType.equiptowerassist.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 装备本助战宝箱
	 * @param player  助战层
	 */
	public static void equiptowerassistbox(Player player, int floor) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.equiptowerassistbox.name(), LoggerType.equiptowerassistbox.version, "C0301"))
					,floor};
			LoggerType.equiptowerassistbox.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 装备本助战录像
	 * @param player  挑战关卡id	录像上传时间
	 */
	public static void equiptowerassistvideo(Player player, int battleId, int time) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.equiptowerassistvideo.name(), LoggerType.equiptowerassistvideo.version, "C0302"))
					,battleId, time};
			LoggerType.equiptowerassistvideo.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 大王来巡山副本闯关
	 * @param player 结束时层数	结束时关卡	关卡类型	战斗结果	持有buffid	本次战斗时长	杀怪获得积分	剩余商店代币	总计积分	本期最高积分
	 */
	public static void patrolmountainend(Player player, int battleId, int type, int time) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.patrolmountainend.name(), LoggerType.patrolmountainend.version, "C0400"))
					,battleId, type, time};
			LoggerType.patrolmountainend.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
//	/**
//	 * 常规刷新招募
//	 * @param player 招募进度	倍数	获得神将ID	获得碎片数量
//	 */
//	public static void patrolmountainend(Player player, int jindu, int beishu, int Id, int num) {
//		try {
//			Object[] array = new Object[] {
//					LoggerType
//							.splice(GameLogAssistant
//							.buildLogCYPrefix(player, LoggerType.ultimateCardDraw.name(), LoggerType.ultimateCardDraw.version, "C0500"))
//							,jindu, beishu, Id, num};
//			LoggerType.ultimateCardDraw.logger.info(LoggerType.splice(array));
//		} catch (Exception e) {
//			SystemLogger.error(e);
//		}
//	}
	/**
	 * 人参果树培养
	 * @param player  浇水1 手动 2 杀虫剂3  施肥4	杀虫剂购买数量
	 */
	public static void RSGTreeGrow(Player player, int type,int num) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.rsgtreegrow.name(), LoggerType.rsgtreegrow.version, "C0600"))
					,type, num};
			LoggerType.rsgtreegrow.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
//	/**
//	 * 人参果奖励
//	 * @param player 捉虫奖励	升级奖励	人参果成熟奖励
//	 */
//	public static void RSGTreeReward(Player player, int bugReward, int upgradeReward, int matureReward) {
//		try {
//			Object[] array = new Object[] {
//					LoggerType
//							.splice(GameLogAssistant
//							.buildLogCYPrefix(player, LoggerType.RSGTreeReward.name(), LoggerType.RSGTreeReward.version, "C0601"))
//					,bugReward, upgradeReward, matureReward};
//			LoggerType.RSGTreeReward.logger.info(LoggerType.splice(array));
//		} catch (Exception e) {
//			SystemLogger.error(e);
//		}
//	}
//	/**
//	 * 人参果商店兑换
//	 * @param player 兑换商品ID
//	 */
//	public static void RSGTreeShopExchange(Player player, int id) {
//		try {
//			Object[] array = new Object[] {
//					LoggerType
//							.splice(GameLogAssistant
//							.buildLogCYPrefix(player, LoggerType.RSGTreeShopExchange.name(), LoggerType.RSGTreeShopExchange.version, "C0602"))
//					,id};
//			LoggerType.RSGTreeShopExchange.logger.info(LoggerType.splice(array));
//		} catch (Exception e) {
//			SystemLogger.error(e);
//		}
//	}
//	/**
//	 * 人参果羁绊卡
//	 * @param player 羁绊卡获得ID	羁绊卡消耗ID
//	 */
//	public static void RSGBondCard(Player player, int getCardId, int useCardId) {
//		try {
//			Object[] array = new Object[] {
//					LoggerType
//							.splice(GameLogAssistant
//							.buildLogCYPrefix(player, LoggerType.RSGBondCard.name(), LoggerType.RSGBondCard.version, "C0603"))
//					,getCardId, useCardId};
//			LoggerType.RSGBondCard.logger.info(LoggerType.splice(array));
//		} catch (Exception e) {
//			SystemLogger.error(e);
//		}
//	}

	/**
	 * 降妖伏魔扫荡
	 * @param player 升至等级	今日扫荡次数	花费元宝	关卡ID	扫荡奖励
	 */
	public static void DemonSweep(Player player,int count, int battleId, List<RewardMsg.RewardInfo> reward) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.demonsweep.name(), LoggerType.demonsweep.version, "C0800"))
					,count,0, battleId,"null"};
			LoggerType.demonsweep.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 灵山问禅次数购买
	 * @param player 今日购买次数	花费元宝
	 */
	public static void LingShanPurchase(Player player, int count, int cost) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.lingshanpurchase.name(), LoggerType.lingshanpurchase.version, "C0900"))
					,count, cost};
			LoggerType.lingshanpurchase.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 灵山问禅扫荡
	 * @param player 今日扫荡次数	关卡ID	扫荡奖励
	 */
	public static void LingShanSweep(Player player, int count, int battleId, List<RewardMsg.RewardInfo> reward) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.lingshansweep.name(), LoggerType.lingshansweep.version, "C0902"))
					,count, battleId,"null"};
			LoggerType.lingshansweep.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
//	/**
//	 *灵山问禅进度奖励   无用
//	 * @param player 领取奖励ID
//	 */
//	public static void LingShanProgressReward(Player player, int count, int battleId, String reward) {
//		try {
//			Object[] array = new Object[] {
//					LoggerType
//							.splice(GameLogAssistant
//							.buildLogCYPrefix(player, LoggerType.lingShanProgressReward.name(), LoggerType.lingShanProgressReward.version, "C0903"))
//					,count, battleId, reward};
//			LoggerType.lingShanProgressReward.logger.info(LoggerType.splice(array));
//		} catch (Exception e) {
//			SystemLogger.error(e);
//		}
//	}


	/**
	 * 大圣擂台次数购买
	 * @param player 今日购买次数	花费元宝
	 */
	public static void DaShengPurchase(Player player, int count, int cost) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.dashengpurchase.name(), LoggerType.dashengpurchase.version, "C1000"))
					,count,cost};
			LoggerType.dashengpurchase.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 大圣擂台挑战
	 * @param player  战报所属玩家 战报内容
	 */
	public static void DaShengChallenge(Player player, String playerId, Object report) {
		String[] split = playerId.split("_");
		String playerIdStr = split[split.length-1];
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.dashengchallenge.name(), LoggerType.dashengchallenge.version, "C1001"))
					,playerIdStr, report.toString()};
			LoggerType.dashengchallenge.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}

	/**
	 * 图鉴
	 * @param player 神将图鉴积分获得	获得后总积分		妖怪图鉴开启ID
	 */
	public static void HeroBook(Player player, int getScore, long totalScore,  int heroID) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.herobook.name(), LoggerType.herobook.version, "C1200"))
					,getScore, totalScore, heroID};
			LoggerType.herobook.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 仙会创建
	 * @param player  仙会ID	仙会名称	仙会旗帜
	 */
	public static void guildCreate(Player player, long guildId, String name, int flag) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.guildcreate.name(), LoggerType.guildcreate.version, "C1300"))
					,guildId, name, flag};
			LoggerType.guildcreate.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 仙会加入
	 * @param player  仙会ID
	 */
	public static void guildJoin(Player player, long guildId,int type) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.guildjoin.name(), LoggerType.guildjoin.version, "C1301"))
					,guildId,type};
			LoggerType.guildjoin.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 仙会解散
	 * @param player  仙会ID	仙会名称
	 */
	public static void guildDisband(Player player, long guildId, String name) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.guilddisband.name(), LoggerType.guilddisband.version, "C1302"))
					,guildId, name};
			LoggerType.guilddisband.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 仙会退出
	 * @param player  仙会ID	仙会名称
	 */
	public static void guildExit(Player player, long guildId, String name) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.guildexit.name(), LoggerType.guildexit.version, "C1303"))
					,guildId, name};
			LoggerType.guildexit.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 仙会信息变化
	 * @param player  仙会ID	仙会修改名称	仙会修改旗帜
	 */
	public static void guildInfoChange(Player player, long guildId  ,String name, int flag) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.guildinfochange.name(), LoggerType.guildinfochange.version, "C1304"))
					,guildId, name,flag};
			LoggerType.guildinfochange.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 仙会成员职位变化
	 * @param player  成员ID	变更前职位	变更后职位
	 */
	public static void guildMemberPositionChange(Player player, long guildId  ,int beforePosition, int afterPosition) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.guildmemberpositionchange.name(), LoggerType.guildmemberpositionchange.version, "C1305"))
					,guildId, beforePosition,afterPosition};
			LoggerType.guildmemberpositionchange.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 仙会升级
	 * @param player  获得经验值	升至等级
	 */
	public static void guildUpgrade(Player player, int exp, int level) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.guildupgrade.name(), LoggerType.guildupgrade.version, "C1306"))
					, exp, level};
			LoggerType.guildupgrade.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 仙会商店
	 * @param player  仙会ID	兑换商品ID
	 */
	public static void guildShop(Player player,  long guildId, int itemId) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.guildshop.name(), LoggerType.guildshop.version, "C1307"))
					,guildId,itemId};
			LoggerType.guildshop.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 仙会砍价
	 * @param player  仙会ID	 兑今日仙会第几次砍价	砍掉金额	砍后价格
	 */
	public static void guildBargain(Player player,  long guildId, int num ,int bargainNum, int bargainPrice) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.guildbargain.name(), LoggerType.guildbargain.version, "C1308"))
					,guildId, num, bargainNum, bargainPrice};
			LoggerType.guildbargain.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 仙会砍价购买
	 * @param player  商品ID	花费元宝
	 */
	public static void guildBargainPurchase(Player player, int itemId, int num) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.guildbargainpurchase.name(), LoggerType.guildbargainpurchase.version, "C1309"))
					,itemId, num};
			LoggerType.guildbargainpurchase.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 仙会捐献
	 * @param player  捐献类型	该类型今日次数	获得贡献值	成员ID
	 */
	public static void GuildDonate(Player player, int type, int num, int contribute, long playerId) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.guilddonate.name(), LoggerType.guilddonate.version, "C1310"))
					,type, num, contribute, playerId};
			LoggerType.guilddonate.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 仙会任务
	 * @param player  完成任务ID	领取奖励ID	今日累计完成次数	领取累计次数奖励ID
	 */
	public static void GuildQuest(Player player ,int taskId, int rewardId, int num, int rewardNum) {
		try {
			Object[] array = new Object[]{
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.guildquest.name(), LoggerType.guildquest.version, "C1311"))
					, taskId, rewardId, num, rewardNum};
			LoggerType.guildquest.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}
	/**
	 * 通用等级升级
	 * @param player 经验ID	等级
	 */
	public static void ComonLevelUp(Player player, int expId, int level) {
		try {
			Object[] array = new Object[] {
					LoggerType
							.splice(GameLogAssistant
							.buildLogCYPrefix(player, LoggerType.commonlevelup.name(), LoggerType.commonlevelup.version, "C1312"))
					,expId, level};
			LoggerType.commonlevelup.logger.info(LoggerType.splice(array));
		} catch (Exception e) {
			SystemLogger.error(e);
		}
	}


	public static String getMonthCardInfo(Player player) {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			MonthCardModule monthCardModule = player.getModule(MonthCardModule.class);
			MonthCard monthCard = monthCardModule.getMonthCard(1);
			stringBuilder.append("1:");
			if (monthCard != null) {
				stringBuilder.append(monthCard.getExpireTime());
			} else {
				stringBuilder.append(0);
			}
			stringBuilder.append(";2:");
			var monthCard2 = monthCardModule.getMonthCard(1);
			if (monthCard2 != null) {
				stringBuilder.append(monthCard2.getExpireTime());
			} else {
				stringBuilder.append(0);
			}
		} catch (Exception e) {
			SystemLogger.error(e);
		}
		return stringBuilder.toString();
	}


	public static String rewardString(List<RewardMsg.RewardInfo> reward) {
		StringBuilder stringBuilder=new StringBuilder();
		reward.forEach(
				info -> stringBuilder.append(info.getItem().getId() + ":" +info.getItem().getCount() + ";")
		);
		stringBuilder.deleteCharAt(stringBuilder.length()-1);
		return stringBuilder.toString();
	}


}
