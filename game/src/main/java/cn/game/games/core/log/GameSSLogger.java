package cn.game.games.core.log;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.MonthCard;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.games.net.game.module.account.Account;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.games.net.game.module.shop.monthcard.MonthCardModule;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.RewardMsg;
import cn.game.util.Config;
import cn.game.util.DateUtil;
import cn.game.util.config.ConfigUtil;
import cn.game.util.log.DeprecatedLogger;
import cn.game.util.log.LoggerType;
import cn.game.util.log.SystemLogger;
import cn.thinkingdata.analytics.TDAnalytics;
import cn.thinkingdata.analytics.TDLoggerConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameSSLogger extends DeprecatedLogger {
    // 1. 创建私有静态实例
    private static volatile GameSSLogger instance;

    // 3. 公共静态方法获取实例
    public static GameSSLogger getInstance() {
        if (instance == null) {
            synchronized (GameSSLogger.class) {
                if (instance == null) {
                    instance = new GameSSLogger();
                }
            }
        }
        return instance;
    }

    /**
     * 默认字符串
     */
    protected static final String BI_DEFAULT_STR = "null";
    /**
     * 默认整数0
     */
    protected static final int BI_DEFAULT_INT = 0;



    //数数SDK相关
    private TDAnalytics te;

    private String ssLogDir = "F:\\party\\ideaserver\\logs\\cylog\\QYK2";

    /**
     * 初始化日志模块logger
     */
    public void init() {
        //按照标准日志300MB切割，按天生成。
    	String rootPath = ConfigUtil.getConfig("SEVER_PATH");
    	ssLogDir = rootPath + File.separator + "logs" + File.separator + "sslog";
        TDLoggerConsumer.Config config = new TDLoggerConsumer.Config(ssLogDir, 300);
        //config默认8K缓存再写入，暂时不修改
        //config.setBufferSize(4096);
        //默认不定时写入，采用有数据产生再写入。
        //config.setAutoFlush(true);
        //config.setInterval(300);//单位：秒，每5分钟定时flush写入一次。
        te = new TDAnalytics(new TDLoggerConsumer(config), false);
    }

    /**
     * 时间戳
     *
     * @return
     */
    public String getEventTime() {
        return getCurrentTimeLogText();
    }

    /**
     * appkey
     *
     * @return
     */
    private String getAppkey() {
        return  Config.APP_KEY;
    }

    /**
     * 游戏服务器id
     *
     * @return
     */
    public String getServerId() {
        return ServerContext.getInstance().getServerId(); //该玩家所在的登录服
    }

    /**
     * 推广渠道唯一标识
     *
     * @return
     */
    private String getGameChannel(Account account) {
        return account.adChannel != null ? account.adChannel : "null";
    }

    /**
     * billing分配的用户userid
     *
     * @return
     */
    private String getUserId(Player player) {
        return player.getAccount().accountId != null ? player.getAccount().accountId : "null";
    }

    /**
     * 获得玩家RoleID
     *
     * @return
     */
    private Long getRoleID(Player player) {
        return player.getPlayerId();
    }

    /**
     * 获取玩家等级
     *
     * @return
     */
    private int getPlayerLevel(Player player) {

        return player.getLevel();
    }

    /**
     * 设备唯一标识
     *
     * @return
     */
    private String getDeviceId(Player player) {
        return player.getData().getDeviceId() != null ? player.getData().getDeviceId() : "null";
    }


    /**
     * 6、	userid ：即微信的unionid
     *
     * @return
     */
    private String getUnionid(Player player) {
        return player.getUnionId() != null ? player.getUnionId() : "null";
    }

    /**
     * 1、	deviceid设备唯一标识：小游戏变为账号id ，即微信的openid
     *
     * @return
     */
    private String getOpenid(Player player) {

        return player.getOpenId() != null ? player.getOpenId() : "null";
    }

    /**
     * 获得玩家RoleName
     *
     * @return
     */
    private String getRoleName(Player player) {
        return player.getData().getName() == null ? "null" : player.getData().getName();
    }

    /**
     * 获取玩家在线时长
     *
     * @return
     */
    private int getOnlineTime(Player player) {
        return 0;
    }

    /**
     * 获取玩家性别
     *
     * @return
     */
    private int getRoleGender(Player player) {
        return 2; //全填2=女
    }

    //=================================================TE系统埋点，不区分小游戏和APP========================================================

    /**
     * 服务器端 默认不传
     *
     * @return
     */
    private String teDistinctId() {
        return null;
    }

    /**
     * 服务器端 默认不传
     *
     * @return
     */
    private String teDistinctId(Player player) {
        return null;
    }

    private static final String BI_MODULE_EVENT_NAME = "bi_module_name";

    /**
     * 初始事件头
     *
     * @return
     */
    private Map<String, Object> teInitEvent(LoggerType logType, String stepNumId) {
        Map<String, Object> event = new HashMap<>();
        event.put("#time", getEventTime());
        event.put(BI_MODULE_EVENT_NAME, logType.name());
        //固定填入步骤号，与BI系统保持一致。
        event.put("stepnumid", stepNumId);
        return event;
    }

    /**
     * 此时已登录，可以获取公共属性
     *
     * @return
     */
    private Map<String, Object> teHeaderEvent(Player player, LoggerType logType, String stepNumId) {
        Map<String, Object> event = teInitEvent(logType, stepNumId);
        event.put("rolelevel", getPlayerLevel(player));
        event.put("rolename", getRoleName(player));
        event.put("valueamount", player.getCurrencyModule().getCount(Asset.diamond.ID));
        event.put("factionid", String.valueOf(player.getGuildId()));
        event.put("login_server_id", getServerId());
        return event;
    }

    /**
     * 上报事件统一入口
     *
     * @param distinctId
     * @param event
     */
    private void uploadTEEvent(long roleId, String distinctId, Map<String, Object> event) {
        try {
            String eventName = event.remove(BI_MODULE_EVENT_NAME).toString();
            te.track(String.valueOf(roleId), distinctId, eventName, event);
        } catch (Exception e) {
            System.err.println("[SSLobbyLog][uploadTEEvent]track Error ");
        }
    }

    private void uploadTEEvent(String roleId, String distinctId, Map<String, Object> event) {
        try {
            String eventName = event.remove(BI_MODULE_EVENT_NAME).toString();
            te.track(roleId, distinctId, eventName, event);
        } catch (Exception e) {
            System.err.println("[SSLobbyLog][uploadTEEvent]track Error ");
        }
    }

    /**
     * 上报用户数据统一入口
     *
     * @param roleId
     * @param distinctId
     * @param userSet
     */
    private void uploadTEUserSet(String roleId, String distinctId, Map<String, Object> userSet) {
        try {
            te.userSet(roleId, distinctId, userSet);
        } catch (Exception e) {
            System.err.println("[SSLobbyLog][uploadTEUserSet]userSet Error,");
        }
    }

    private void uploadTEUserSet(long roleId, String distinctId, Map<String, Object> userSet) {
        try {
            te.userSet(String.valueOf(roleId), distinctId, userSet);
        } catch (Exception e) {
            System.err.println("[SSLobbyLog][uploadTEUserSet]userSet Error,");
        }
    }

    /**
     * 上报用户数据统一入口，只用于上报一次的  数据。
     *
     * @param roleId
     * @param distinctId
     * @param userSetOnce
     */
    private void uploadTEUserSetOnce(String roleId, String distinctId, Map<String, Object> userSetOnce) {
        try {
            te.userSetOnce(roleId, distinctId, userSetOnce);
        } catch (Exception e) {
            System.err.println("[SSLobbyLog][uploadTEUserSetOnce]userSetOnce Error");
        }
    }

    public void teFlush() {
        try {
            te.flush();
        } catch (Exception e) {
            System.err.println("[SSLobbyLog][teFlush]Error");
        }
    }

    public void teClose() {
        try {
            te.close();
        } catch (Exception e) {
            System.err.println("[SSLobbyLog][teClose]Error");
        }
        System.err.println("[SSLobbyLog][teClose]Call Finish!!!");
    }

    private   List<Long>  teCardObject(Player player) {
        List<Long> card = new ArrayList<>();
        MonthCardModule monthCardModule = player.getModule(MonthCardModule.class);
        MonthCard monthCard = monthCardModule.getMonthCard(1);
        MonthCard monthCard2 = monthCardModule.getMonthCard(2);
        card.add( monthCard == null ? 0 : monthCard.getExpireTime());
        card.add( monthCard2 == null ? 0 : monthCard2.getExpireTime());
        return card;
    }

    private List<Object> teHeroObject(List<Hero> battleHeros) {

        List<Object> heroInfos = new ArrayList<>();
        for (Hero battleHero : battleHeros) {
            heroInfos.add(battleHero.getConfigId());
        }
        return heroInfos;
    }

    /**
     * 服务器事件
     * 时间	yyyy-mm-dd HH:mi:ss	字符串	是
     * 游戏标识	appkey	字符串	是
     * SDK版本号	sdkversion	字符串	是	如：v1.07，由SDK传回
     * 系统	system	字符串	是	分服打印：“IOS”或“android”，混服打印：“all”（ios和Android混服）
     * 推广渠道id	gamechannel	字符串	是
     * 设备唯一标识	deviceid	字符串	是
     * 账号id	userid	字符串	是	billing生成的userid，未能获取的情况下填默认值“null”
     * _行为号	code	字符串	是	与billing、SDK行为号定义规则保持一致，最终返回对照表
     * 客户端版本号	version	字符串	是
     * 时区	Timezone	字符串	是	服务器时区，格式为UTC+8、UTC-5等取不到时默认值为-1.
     */
    public void logServerEvent(LoggerType logType, String stepNumId, String adChannel, String devicdId, String accountId, int code) {
        Map<String, Object> event = teInitEvent(logType, stepNumId);
        event.put("code", code);
        //此字段只 记录 小游戏的所有渠道，
        event.put("gamechannel", adChannel);
        event.put("device_id", devicdId);
        event.put("userid", accountId);
        event.put("appkey", getAppkey());
        uploadTEEvent(accountId, teDistinctId(), event);
    }

    /**
     * 心跳日志
     * 时间戳	yyyy-mm-dd HH:mi:ss	字符串	是
     * 游戏标识	appkey	字符串	是
     * 客户端版本	version	字符串	是
     * 日志模块名	heart	字符串	是	默认值“heart”
     * 服务器日志规范版本号	normversion	字符串	是
     * 步骤号	stepnumid	字符串	是	默认值“1010”
     * 游戏服务器id	serverId	字符串	是
     * 在线用户数 	onlineuser	正整数	是
     * 排队人数 	paidui	正整数	否
     * 时区	Timezone	字符串	是	服务器时区，格式为UTC+8、UTC-5等取不到时默认值为-1.
     */
    public void logHeart(int num) {
        Map<String, Object> event = teInitEvent(LoggerType.heart, "1010");
        event.put("onlineuser", num);
        event.put("paidui", 0);
        uploadTEEvent(getServerId(), teDistinctId(), event);
    }

    public void loglogin(Player player) {
        Map<String, Object> event = teInitEvent(LoggerType.login, "2050");
        event.put("heronum", player.getHeroModule().list().size());
        event.put("heronum1", 0);
        event.put("mcarddays", teCardObject(player));
        event.put("unionid", player.getUnionId());
        event.put("openid", player.getOpenId());
        uploadTEEvent(getServerId(), teDistinctId(), event);
    }

    /**
     * 做为小游戏和APP统一登录入口触发
     */
    public void loglogin_wxxcx(Player player) {
        //上传事件
        Map<String, Object> event = teHeaderEvent(player, LoggerType.login_wxxcx, "1011");
        event.put("unionid", player.getUnionId());
        event.put("openid", player.getOpenId());
        event.put("clue_path", player.getAccount().getClue_token() == null ? "{}" : player.getAccount().getClue_token());
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }

    /**
     * 创建角色
     */
    public void logRoleBuild(Player player) {
        //上传事件
        Map<String, Object> event = teHeaderEvent(player, LoggerType.rolebuild, "3025");
        event.put("rolebuild_rolename", getRoleName(player));
        event.put("gender", getRoleGender(player));
        event.put("unionid", player.getUnionId());
        event.put("openid", player.getOpenId());
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
        Map<String, Object> userSet = new HashMap<>();
        userSet.put("registtime", DateUtil.getStringDate());
        userSet.put("server_id", getServerId());
        userSet.put("device_id", getDeviceId( player));
        userSet.put("appkey", getAppkey( ));
        userSet.put("userid",getUserId(player));
        userSet.put("userid",getUserId(player));
        uploadTEUserSet(getRoleID(player), teDistinctId(player), userSet);
    }
    public void logrolename(Player player,String string) {
        Map<String, Object> userSet = new HashMap<>();
        userSet.put("rolename",string);
        uploadTEUserSet(getRoleID(player), teDistinctId(player), userSet);
    }

    public void logRoleLogin(Player player) {
        //上传事件
        Map<String, Object> event = teHeaderEvent(player, LoggerType.rolelogin, "3030");
        event.put("mcarddays", teCardObject(player));
        event.put("unionid", player.getUnionId());
        event.put("openid", player.getOpenId());
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }
    /**
     * 新手引导
     *
     * @param eventId 时间	yyyy-mm-dd HH:mi:ss	字符串	是
     */
    public void logNewStages(Player player, String stepNumId, int eventId) {
        //上传事件
        Map<String, Object> event = teHeaderEvent(player, LoggerType.newstages, stepNumId);
        event.put("eventid", eventId);
        event.put("isstep", 0);
        event.put("isback", 0);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }

    /**
     * 登出
     */
    public void logLogout(Player player) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.logout, "9999");
        event.put("onlinetimes", getOnlineTime(player));
        event.put("heronum", player.getHeroModule().list().size());
        event.put("heronum1", 0);
        event.put("mcarddays", teCardObject(player));
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }

    /**
     * 充值
     * <p>
     * 时间	yyyy-mm-dd HH:mi:ss	字符串	是
     * 游戏标识	appkey	字符串	是
     * 客户端版本	version	字符串	是
     * 日志模块名	recharge	字符串	是	默认值“recharge”
     * 服务器日志规范版本号	normversion	字符串	是
     * 步骤号	stepnumid	字符串	是	5000-5999自定义
     * 游戏服务器id	serverid	字符串	是	埋点完成后给出服务器id的对照表
     * 推广渠道id	gamechannel	字符串	是
     * 账号id	userid	字符串	是	billing生成的userid
     * 角色id	roleid	字符串	是	首次登陆可为空，默认值“null”
     * 角色等级	rolelevel	正整数	是	首次登陆默认为最低等级
     * 帮会id	factionid	正整数	是	null
     * 设备唯一标识	deviceid	字符串	是
     * <p>
     * 充值额度（RMB元）	amount	浮点数	是	例如：6.0
     * 充值渠道id	rechargechannel	字符串	是	与billing里“支付渠道”一致，例如：3006（91）、2001（AppStore）等（待支付系统正常接入后）
     * 价值虚拟币数量	valuequantity	正整数	是	当次充值所兑换的虚拟币数量，包括赠送的量
     * 币种	currency	字符串	是	1:人民币元；11：人民币分
     * 用户进行充值时设备的ip	ip	字符串	是
     * 价值虚拟币总量	valueamount	正整数	是	充值后用户的钻石总量
     * Vip等级	vip	字符串	否	充值后的VIP等级
     * 商品id	goods_id	字符串	否	输出对应充值档位商品对照表ID，当前暂未接入充值系统
     * 订单号	orderid	字符串	是
     * 时区	Timezone	字符串	是	服务器时区，格式为UTC+8、UTC-5等取不到时默认值为-1.
     */
    public void logRecharge(Player player, String stepNum, PayItem payItem) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.recharge, stepNum);
        event.put("amount", payItem.getRmb());
        event.put("rechargechannel", player.getAccount().sdkPayChannel);
        event.put("currency", payItem.getAddCount());// 币种 人民币元
        event.put("valuequantity", 1);
        event.put("recharge_valueamount", player.getGameClient().getIp());
        event.put("goods_id", payItem.getSdkGoodsId());
        event.put("orderid", payItem.getSdkOrderId());
        event.put("isvouchers", 2);
        event.put("packageid", payItem.getPayId());
        event.put("unionid", player.getUnionId());
        event.put("openid", player.getOpenId());
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }

    public void logAD(Player player, List<String> params) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.adwatching, "B9410");
        event.put("rolename", player.getPlayerName());
        event.put("vip", player.getVipLevel());
        event.put("adposition", params.get(0));
        event.put("adloadingstate", params.get(1));
        event.put("adid", params.get(2));
        event.put("adtype", params.get(3));
        event.put("watchtype", params.get(4));
        event.put("adplatform", params.get(5));
        event.put("adnetworksource", params.get(6));
        event.put("errorcodes", params.get(7));
        event.put("commoditysource", params.get(8));
        event.put("adtime", params.get(9));
        event.put("isskip", params.get(10));
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }

    /**
     * 升级
     * <p>
     * 时间	yyyy-mm-dd HH:mi:ss	字符串	是
     * 游戏标识	appkey	字符串	是
     * 客户端版本	version	字符串	是
     * 日志模块名	logout	字符串	是	默认值“logout”
     * 服务器日志规范版本号	normversion	字符串	是
     * 步骤号	stepnumid	字符串	是	默认值“9999”
     * 游戏服务器id	serverid	字符串	是	埋点完成后给出服务器id的对照表
     * 推广渠道id	gamechannel	字符串	是
     * 账号id	userid	字符串	是	billing生成的userid
     * 角色id	roleid	字符串	是	首次登陆可为空，默认值“null”
     * 角色等级	rolelevel	正整数	是	首次登陆默认为最低等级
     * 设备唯一标识	deviceid	字符串	是
     * * 时区	Timezone	字符串	是	服务器时区，格式为UTC+8、UTC-5等取不到时默认值为-1.
     * <p>
     * 角色名	rolename	字符串	是
     * 升级后等级	rolelevelaf	正整数	是
     * 升级前等级	rolelevelbf	正整数	是
     * 升级时长（单位S）	time	正整数	否	角色从上一等级升级到当前等级所需的时间（仅指在游戏内的时间，不包含离线时间）
     */
    public void logLevelup(Player player) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.levelup, "6010");
        event.put("aflevel", player.getLevel());
        event.put("bflevel", Math.max(0, player.getLevel() - 1));
        event.put("leveluptime", 0);//
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);

        Map<String, Object> userSet = new HashMap<>();
        userSet.put("rolelevel", player.getLevel());
        uploadTEUserSet(getRoleID(player), teDistinctId(player), userSet);
    }

    public void logrecruit(Player player, int id, int count, int beishu, int costId, int costCount) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.recruit, "6010");
        event.put("recruitid", 0);
        event.put("recruittype", 0);
        event.put("recruitnum", 0);//
        event.put("recruitcount", beishu);//
        event.put("recruitheroid", BI_DEFAULT_STR);//
        event.put("recruitheronum", 0);//
        event.put("recruititemid", id);//
        event.put("recruititemnum", count);//
        event.put("recruitmoneynum", 0);//
        event.put("recruitmoneyid", BI_DEFAULT_STR);//
        event.put("wishhero", 0);//
        event.put("recruituseitemid", costId);//
        event.put("recruituseitemnum", costCount);//
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);

    }


    public void logshoptrade(Player player, int shopId, int itemId, long itemNum, int costId, int costNum, int type) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.shoptrade, "7010");
        event.put("itemtypeid", type);
        event.put("goods_id", itemId);
        event.put("itemcount", itemNum);
        event.put("moneytypeid", costId);
        event.put("moneycount", costNum);
        event.put("shopid", shopId);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);

    }

    public void logMoney(Player player, int id, long count, OpType opType, boolean isIncrease) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.money, "8010");
        event.put("causeid", opType.name());
        event.put("moneyid", count);
        event.put("total", player.getCurrencyModule().getCount(id));
        event.put("typeid", id);
        event.put("subcauseid", BI_DEFAULT_STR);
        event.put("action", isIncrease ? 1 : -1);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);

        //如果钻石发生变动，更新用户数据
        if (id == Asset.diamond.ID) {
            //上传用户数据
            Map<String, Object> userSet = new HashMap<>();
            userSet.put("valueamount", player.getCurrencyModule().getCount(Asset.diamond.ID));
            uploadTEUserSet(getRoleID(player), teDistinctId(player), userSet);
        }
    }

    /**
     * 物品获得与消耗
     * 详细说明参见 BI通用埋点文档
     */
    public void logItem(Player player, int id, int count, OpType opType, boolean isAdd) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.item, "B2110");
        int type = ItemHelper.getGoodsType(id);
        event.put("itemtypeid", type);
        event.put("itemid", id);
        event.put("causeid", opType.name());
        event.put("subcauseid", BI_DEFAULT_STR);
        event.put("quantity", count);
        event.put("action", isAdd ? 1 : -1);
        event.put("totalleft", player.getGoodsModule(id).getCount(id));
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }

    public void logTask(Player player, String step, int taskId, int taskType) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.task, step);
        event.put("taskid", taskId);
        event.put("result", 1);
        event.put("tasktype", taskType);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }

    public void logHero(Player player, Hero hero, OpType opType) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.gethero, "B8110");
        event.put("heroid", hero.getConfigId());
        event.put("cardorderid",  hero.getId());;
        event.put("causeid", opType);
        event.put("heronum", player.getHeroModule().list().size());
        event.put("heronum1", 0);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }


    public void logheroraise (Player player, Hero hero,int operatetype, int addvalue, int endvalue, int beforeCombat, int afterCombat,String step) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.heroraise, step);
        event.put("heroid", hero.getConfigId());
        event.put("cardorderid",  hero.getId());;
        event.put("heroraise_stageid", operatetype);
        event.put("result",1);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }


    /**
     * 关卡战斗
     * <p>
     * 时间	yyyy-mm-dd HH:mi:ss	字符串	是
     * 游戏标识	appkey	字符串	是
     * 客户端版本	version	字符串	是
     * 日志模块名	pvefight	字符串	是	默认值“pvefight”
     * 服务器日志规范版本号	normversion	字符串	是
     * 步骤号	stepnumid	字符串	是	固定输出“B4120”
     * 游戏服务器id	serverid	字符串	是	埋点完成后给出服务器id的对照表
     * 推广渠道id	gamechannel	字符串	是
     * 账号id	userid	字符串	是	billing生成的userid
     * 角色id	roleid	字符串	是	首次登陆可为空，默认值“null”
     * 角色等级	rolelevel	正整数	是	首次登陆默认为最低等级
     * 帮会id	factionid	字符串	是	默认值“null”
     * 设备唯一标识	deviceid	字符串	是
     * <p>
     * 关卡id	stageid	字符串	否	 stageID
     * 战斗类型	stagetype	字符串	是	BattleType
     * 关卡用时	stagetype	字符串	是	BattleType
     */
    public void logPveFight(Player player, int stageId, int type, long time, boolean result) {
        List<Hero> battleHeros = player.getBattleModule().getDefaultLineupHeroes();
        Map<String, Object> event = teHeaderEvent(player, LoggerType.pvefight, "B4100");
        event.put("stageid", stageId);
        event.put("fighttype", type);
        event.put("npcid", BI_DEFAULT_STR);
        event.put("pvefightresult", result ? 1 : 2);
        event.put("mapid", BI_DEFAULT_STR);
        event.put("stagetype", BI_DEFAULT_STR);
        event.put("fighttime", time);
        event.put("energy", player.getAttrModule().getPower());
        event.put("suggestpower", 0);
        event.put("heroidlist", teHeroObject(battleHeros));

        uploadTEEvent(getRoleID(player), teDistinctId(player), event);

        if (type == DungeonTypeEnum.BattleChapter.getId()) {
            //主线关卡 上报
            //上传用户数据
            Map<String, Object> userSet = new HashMap<>();
            userSet.put("stageid_max", stageId);
            uploadTEUserSet(getRoleID(player), teDistinctId(player), userSet);
        }

    }

    /**
     * 战斗日志（pvpfight）
     * <p>
     * 时间	yyyy-mm-dd HH:mi:ss	字符串	是
     * 游戏标识	appkey	字符串	是
     * 客户端版本	version	字符串	是
     * 日志模块名	pvpfight	字符串	是	默认值“pvpfight”
     * 服务器日志规范版本号	normversion	字符串	是
     * 步骤号	stepnumid	字符串	是	固定输出“B4120”
     * 游戏服务器id	serverid	字符串	是	埋点完成后给出服务器id的对照表
     * 推广渠道id	gamechannel	字符串	是
     * 账号id	userid	字符串	是	billing生成的userid
     * 角色id	roleid	字符串	是	首次登陆可为空，默认值“null”
     * 角色等级	rolelevel	正整数	是	首次登陆默认为最低等级
     * 帮会id	factionid	字符串	是	默认值“null”
     * 设备唯一标识	deviceid	字符串	是
     * <p>
     * 战斗类型	battletype	字符串	是	用于区分战斗类型，如休闲赛，排位赛等；记录好后需返回对照表，目前就1个，输出1
     * 战斗模式id	battleid	字符串	是	用于区分不同的战斗模式，如休闲赛1v1，休闲赛3v3等，目前不分模式，输出1
     * 战斗难度	battlelevel	字符串	是	用于区分不同的战斗难度，如：普通、困难、英雄、王者
     * 战斗流水id	orderid	字符串	是	用于区分每天的不同战斗场次，每天更新流水号
     * 队伍id	teamid	字符串	否	对抗类战斗，用于记录不同队伍id
     * 战斗结果	result	字符串	否	步骤号为完成战斗时必须记录，如：0-失败 1-胜利 2-强行退出；或用于记录玩家的排名
     * 耗时（s）	time	正整数	否	步骤号为完成战斗时，用于记录战斗耗时
     * 对手类型	targettype	字符串	否	区分对手是普通玩家、AI或普通玩家+AI
     * 角色战力	rolecombatpower	字符串	是	直接输出A、B这种格式，方便阅读
     * 对手战力	enemycombatpower	字符串	是	直接输出A、B这种格式，方便阅读
     * 时装id	costumeid	字符串	否	记录pvp中使用的时装id情况
     * 对手时装id	enemycostumeid	字符串	否	记录pvp中对手使用的时装id情况
     * 战斗后积分	scoreaf	正整数	是
     * 战斗前积分	scorebf	正整数	是
     * result 1    开始
     */
    public void logpvpfight(Player player, String stepnumid, SimplePlayer targetPlayer, boolean win) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.pvpfight, stepnumid);
        event.put("power", player.getAttrModule().getPower());
        event.put("rankbf", BI_DEFAULT_INT);
        event.put("rankaf", BI_DEFAULT_INT);
        event.put("battleid", BI_DEFAULT_STR);
        event.put("targetroleid", targetPlayer.id);
        event.put("targetname", targetPlayer.getName());
        event.put("targetlevel", targetPlayer.getLevel());
        event.put("targetpower", targetPlayer.getCombatEffectiveness());
        event.put("targetrankbf", 0);
        event.put("targetrankaf", 0);
        event.put("result", win);
        event.put("time", 0);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }

    /**
     * 运营活动
     */
    public void logActivity(Player player, int activityId, long subId) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.activity, "B6110");
        event.put("activityid", activityId);
        event.put("subid", subId);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }

    public void logrank(int type, String rank, String value) {
        Map<String, Object> event = teInitEvent(LoggerType.rank, "B9510");
        event.put("ranktype", type);
        event.put("prank", rank);
        event.put("rankscore", value);
        uploadTEEvent(getServerId(), teDistinctId(null), event);
    }

    public void loggemtowerbuff(Player player, int buffId) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.gemtowerbuff, "C0200");
        event.put("buffid", buffId);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }
    public void loggemtowersweep(Player player, int towerType,int floor, int sweepTotal, int sweepCount, int sweepCountLeft) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.gemtowersweep, "C0201");
        event.put("towertype", towerType);
        event.put("tower_stageid", floor);
        event.put("sweepcountmax", sweepTotal);
        event.put("sweepcount", sweepCount);
        event.put("sweepcountleft", sweepCountLeft);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }
    public void logequiptowerassist (Player player,int floor, long help){
        Map<String, Object> event = teHeaderEvent(player, LoggerType.equiptowerassist, "C0300");
        event.put("helpfloor", floor);
        event.put("helpplayerId", help);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }
    public void logequiptowerassistbox(Player player, int floor) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.equiptowerassistbox, "C0301");
        event.put("helpfloor", floor);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }

    public void logequiptowerassistvideo(Player player, int battleId, int time) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.equiptowerassistvideo, "C0302");
        event.put("battleid", battleId);
        event.put("rassisttime", time);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }

    public void logrsgtreegrow(Player player, int type,int num) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.rsgtreegrow, "C0600");
        event.put("type", type);
        event.put("num", num);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }
    public void logdemonsweep(Player player, int count, int battleId, List<RewardMsg.RewardInfo> reward) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.demonsweep, "C0800");
        event.put("count", count);
        event.put("demon_stageid", battleId);
        event.put("reward", reward);
        event.put("diamondcost", 0);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }
    public void loglingshanpurchase(Player player, int count, int cost) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.lingshanpurchase, "C0900");
        event.put("count", count);
        event.put("diamondcost", cost);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }
    public void loglingshansweep(Player player, int count, int battleId, List<RewardMsg.RewardInfo> reward) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.lingshansweep, "C0902");
        event.put("lingshansweep_count", count);
        event.put("lingshansweep_stageid", battleId);
        event.put("reward", reward);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }

    public  void logdashengpurchase(Player player, int count, int cost) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.dashengpurchase, "C1000");
        event.put("count", count);
        event.put("diamondcost", cost);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }

    public void logdashengchallenge(Player player, String playerId, Object report) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.dashengchallenge, "C1001");
        event.put("ower", playerId);
        event.put("report", report);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }

        public void logherobook(Player player, int getScore, long totalScore,  int heroID) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.herobook, "C1200");
        event.put("addscore", getScore);
        event.put("toalscore", totalScore);
        event.put("heroid", heroID);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }
        public void logguildcreate(Player player, long guildId, String name, int flag) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.guildcreate, "C1300");
        event.put("guildid", guildId);
        event.put("guildname", name);
        event.put("guildflag", flag);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);

        Map<String, Object> userSet = new HashMap<>();
        userSet.put("factionid",guildId);
        uploadTEUserSet(getRoleID(player), teDistinctId(player), userSet);
    }
        public void logguildjoin(Player player, long guildId,int type) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.guildjoin, "C1301");
        event.put("joinguildid", guildId);
        event.put("guildJointype", type);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);

        Map<String, Object> userSet = new HashMap<>();
        userSet.put("factionid",guildId);
        uploadTEUserSet(getRoleID(player), teDistinctId(player), userSet);
    }




    public void logguildinfochange(Player player, long guildId, String name, int flag) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.guildinfochange, "C1304");
        event.put("joinguildid", guildId);
        event.put("guildname", name);
        event.put("guildflag", flag);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }

    public void logguildmemberpositionchange(Player player,  long memberId, int beforePosition, int afterPosition) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.guildmemberpositionchange, "C1305");
        event.put("playerid", memberId);
        event.put("before", beforePosition);
        event.put("after", afterPosition);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }
    public void logguildupgrade(Player player, int exp, int level) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.guildupgrade, "C1306");
        event.put("exp", exp);
        event.put("level", level);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }

        public void logguildbargain(Player player, long guildId, int num ,int bargainNum, int bargainPrice) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.guildbargain, "C1308");
        event.put("guildid", guildId);
        event.put("bargaintimes", num);
        event.put("bargain", bargainNum);
        event.put("left", bargainPrice);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }

    public void logguildbargainpurchase(Player player, int itemId, int num) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.guildbargainpurchase, "C1309");
        event.put("guildbargainpurchase_id", itemId);
        event.put("guildbargainpurchase_cost", num);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }
        public void logguilddonate(Player player,int type, int num, int contribute, long playerId) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.guilddonate, "C1310");
        event.put("type", type);
        event.put("count", num);
        event.put("getvalue", contribute);
        event.put("playerid", playerId);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);
    }

        public void logcommonlevelup(Player player, int expId, int level) {
        Map<String, Object> event = teHeaderEvent(player, LoggerType.commonlevelup, "C1312");
        event.put("exp", expId);
        event.put("level", level);
        uploadTEEvent(getRoleID(player), teDistinctId(player), event);

        if(expId==Asset.VIPExp.ID)
        {
            Map<String, Object> userSet = new HashMap<>();
            userSet.put("vip",level);
            uploadTEUserSet(getRoleID(player), teDistinctId(player), userSet);
        }
    }

}
