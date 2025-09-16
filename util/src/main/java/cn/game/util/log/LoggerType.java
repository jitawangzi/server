package cn.game.util.log;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author pangjiawei - [Created on 2018/2/1 0:09]
 */
public enum LoggerType {

    /* *********************** 公共 ************************* */
    System,                                                                     // 系统日志，用来输出系统级别的信息，以及工具组件信息
    Info,                                                                       // Info级别日志
    Warn,                                                                       // Warn级别日志
    Error,                                                                      // Error级别日志
    Net,                                                                        // 网络日志
    Elapsed,                                                                    // 时间统计日志
    Combat,                                                                     // 战斗日志
    NetCheck,                                                                     // 网络监控
	SystemOut,
	Stdout,
	Monitor,

    /* *********************** 一级 ************************* */
    heart("v3"),                                                     // 心跳
    login,                                                                      // 登录
	login_wxxcx, // 微信小程序登录
    rolebuild,                                                                  // 创建角色
    rolelogin,                                                                  // 创建角色
    logout,                                                                     // 登出
//    serverevent,                                                                // 服务器事件
    recharge,                                                                   // 充值

    levelup,                                                                    // 升级
    money,                                                                      // 货币获得与消耗
    newstages,                                                                  // 新手引导
    shoptrade,                                                                  // 商城日志

    /* *********************** 二级 ************************* */
    item,                                                                       // 物品获得与消耗
    pvpfight,                                                                   // pvp战斗
    pvefight,                                                                   // 关卡战斗

    task,                                                                       // 任务
    achievement,                                                                // 个人完成成就
    activity,                                                                   // 参与活动
//    arena,                                                                      // 竞技场
//    activationcode,                                                             //激活码激活
	gethero, // 获取卡牌
	heroraise, // 卡牌养成
    rank , // 排行榜结算
    /* *********************** 其他 ************************* */
//	equipmentshape, // 装备幻化
//	equipmentwear, // 装备穿戴
//	onhook, // 挂机
//    boxlevelup,                                                                 //宝箱升级
//    boxopen,                                                                    //宝箱开启
//	mail, // 邮件
//	mailbox, // 邮箱
//	chat, // 聊天
//    rankkz,                                                                     // 排行榜快照
//    rankNumInfo,                                                                // 排行榜数量信息
//    rankInfo,                                                                   // 排行榜信息
//    bleachRedis,                                                                // Redis日志
//    roleinfochange,                                                             // 玩家信息修改
//	equipgems, // 宝石镶嵌
//	mergegems, // 宝石合成
//    propertysnap,                                                               // 玩家属性快照
//    resourcegrab,                                                               // 领地资源抢夺
    orderCreate,                                                                // 创建订单
    orderFinish,                                                                // 订单完成
    invite,                                                                     // 邀请好友
//    guildmanagement,                                                            // 公会管理
//    guildsnap,                                                                  // 公会成员快照
//    leagueboss,                                                                 // 公会讨伐
    gemtowerbuff	,//宝石塔每日刷新buff
    gemtowersweep	,//宝石塔扫荡
    equiptowerassist	,//装备本助战
    equiptowerassistbox,//	装备本助战宝箱
    equiptowerassistvideo	,//装备本助战录像
    patrolmountainend	,//大王来巡山副本闯关
    ultimateCardDraw	,//常规刷新招募
    rSGTreeGrow	,//人参果树培养
   // RSGTreeReward	,//人参果奖励
  //  RSGTreeShopExchange,//	人参果商店兑换
   // RSGBondCard	,//人参果羁绊卡
  //  DefenceLine	,//五行界
    demonSweep	,//降妖伏魔扫荡
    lingShanPurchase,//	灵山问禅次数购买
    lingShanSweep	,//灵山问禅扫荡
    lingShanProgressReward,//	灵山问禅进度奖励
    daShengPurchase	,//大圣擂台次数购买
    daShengChallenge	,//大圣擂台挑战

//    SamsaraPurchase	,//轮回西行次数购买
//    SamsaraBattle	,//轮回西行战斗
//    SamsaraBUFF	,//轮回西行BUFF
//    SamsaraFlipReward	,//轮回西行翻牌奖励
//    SamsaraGVG	,//轮回西行GVG
    heroBook	,//图鉴
    guildCreate	,//仙会创建
    guildJoin	,//仙会加入
    guildDisband	,//仙会解散
    guildExit	,//仙会退出
    guildInfoChange,//	仙会信息变化
    guildMemberPositionChange,//	仙会成员职位变化
    guildUpgrade	,//仙会升级
    guildShop	,//仙会商店
    guildBargain	,//仙会砍价
    guildBargainPurchase,//	仙会砍价购买
    guildDonate,//仙会捐献
    guildQuest,//仙会任务
    commonLevelUp,//通用等级升级
    ;


    public static final char delimiter = 0x01;

    public static String splice(Object[] objects) {
        return ArrayUtils.isEmpty(objects) ? null : StringUtils.join(objects, LoggerType.delimiter);
    }

    public final Logger logger;

    public final String version;

    LoggerType() {
        // 现在默认v3版本了
		this("v3");
    }

    LoggerType(String version) {
        logger = LogManager.getLogger(this.name());
        this.version = version;
    }
}
