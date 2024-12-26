package cn.game.games.cache.entity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;

import cn.game.core.exception.LogicException;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.GoodsModule;
import cn.game.games.core.event.EventHandler;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.account.Account;
import cn.game.games.net.game.module.activity.ActivityModule;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.currency.CurrencyModule;
import cn.game.games.net.game.module.develop.AttrModule;
import cn.game.games.net.game.module.develop.DevelopModule;
import cn.game.games.net.game.module.develop.dragon.DragonModule;
import cn.game.games.net.game.module.develop.fairyfriend.FairyFriend;
import cn.game.games.net.game.module.develop.fairyfriend.FairyFriendModule;
import cn.game.games.net.game.module.develop.hccommon.HCCommonModule;
import cn.game.games.net.game.module.develop.hchero.HCHeroModule;
import cn.game.games.net.game.module.develop.hero.HeroModule;
import cn.game.games.net.game.module.develop.pet.PetModule;
import cn.game.games.net.game.module.develop.secretscript.SecretscriptModule;
import cn.game.games.net.game.module.develop.skill.DragonSkillModule;
import cn.game.games.net.game.module.event.EventModule;
import cn.game.games.net.game.module.func.FuncModule;
import cn.game.games.net.game.module.invite.InviteModule;
import cn.game.games.net.game.module.item.ItemModule;
import cn.game.games.net.game.module.mail.MailModule;
import cn.game.games.net.game.module.player.PlayerModule;
import cn.game.games.net.game.module.player.VarModule;
import cn.game.games.net.game.module.player.pointreward.PointRewardModule;
import cn.game.games.net.game.module.pvp.OfflineBattleModule;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.games.net.game.module.recharge.PayType;
import cn.game.games.net.game.module.shop.ShopHelper;
import cn.game.games.net.game.module.shop.ShopModule;
import cn.game.games.net.game.module.shop.monthcard.MonthCardModule;
import cn.game.games.net.game.module.vip.VipModule;
import cn.game.protocol.generated.config.FairyFriendFavorabilityConfig;
import cn.game.protocol.generated.config.MonthCardConfig;
import cn.game.protocol.generated.config.VIPConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.enume.WelfareTypeEnum;
import cn.game.protocol.generated.manager.FairyFriendFavorabilityManager;
import cn.game.protocol.generated.manager.MonthCardManager;
import cn.game.protocol.generated.manager.VirtualServerManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.protocol.protobuf.GmMsg.GmPlayerInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099;
import cn.game.protocol.protobuf.PlayerMsg.PlayerInfo;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderCreateRequest_7d000020;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderCreateResponse_7d000021;
import cn.game.protocol.protobuf.ShopMsg.PaymentOrderPush_15010020;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;
import cn.game.util.ServerType;
import cn.game.util.reflect.ClassHelper;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;

//@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class Player  {
	private static  transient Logger log = LoggerFactory.getLogger(Player.class);

	private long playerId;
	private transient static Set<Class<? extends BasePlayerModule>> allModuleClass;
	static {
		allModuleClass = ClassHelper.findSubclasses("cn.game.games", BasePlayerModule.class);
		Iterator<Class<? extends BasePlayerModule>> iterator = allModuleClass.iterator();
		while (iterator.hasNext()) {
			Class<? extends cn.game.games.core.BasePlayerModule> c = (Class<? extends cn.game.games.core.BasePlayerModule>) iterator.next();
			if (Modifier.isAbstract(c.getModifiers())) {
				iterator.remove();
				continue;
			}
			BasePlayerModule instance = null;
			try {
				instance = c.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				e.printStackTrace();
			}
			if (!instance.isComplete()) {
				iterator.remove();
				continue;
			}
		}
	}
	private Map<String, BasePlayerModule> modules = new HashMap<>();
	private transient Map<Integer, GoodsModule<? extends Item, ? extends Item>> goodsModules = new HashMap<>();
	/* ******************** 内存数据 ******************** */
	private transient EventModule eventModule = new EventModule();
	/** TODO 长时间闲置设置false，先不清数据,暂停定时存库 */
	private volatile boolean isActive = true;
	/** 是否正在退出 */
	private volatile boolean islogouting;
	/** 玩家基本数据 */
	private PlayerData data;
	private Account account;
	/** 玩家的网络连接 */
	private transient GameClient gameClient;
	private List<Long> timerTask = new ArrayList<>();
	private boolean isOnline = true;

	public <T extends BasePlayerModule> T getModule(Class<? extends BasePlayerModule> clazz) {
		return (T) this.modules.get(clazz.getName());
	}

	/** 
	 * 周期性的运行某一任务
	 * @param delay 间隔时间（ms）
	 * @param handler
	 * @return
	 */
	public long setPeriodicTask(long delay, Handler<Long> handler) {
		long timer = gameClient.getContext().setPeriodic(delay, handler);
//		log.info("player : " + playerId + "添加定时任务：" + timer);
		timerTask.add(timer);
		return timer;
	}

	/** 
	 * 延迟一段时间后，执行一个任务
	 * @param delay 延迟时间（ms）
	 * @param handler
	 * @return
	 */
	public long setTimerTask(long delay, Handler<Long> handler) {
		if (delay <= 0) {
			gameClient.getContext().runOnContext(v -> handler.handle(0L));
		} else {
			long timer = gameClient.getContext().setTimer(delay, handler);
			timerTask.add(timer);
			return timer;
		}
		return 0;
	}

//	public long setPeriodicTaskAfter(long delay, Handler<Long> handler, long period) {
//		return setTimerTask(delay, r -> {
//			setPeriodicTask(period, handler);
//		});
//	}

	public void cancelTimer(long id) {
//		log.info("player : " + playerId + "取消定时器：" + id);
		timerTask.remove(id);
		VxHolder.vertx.cancelTimer(id);
	}

	public void cancelAllTimer() {
		for (Long id : timerTask) {
//			log.info("player : " + playerId + "取消定时器：" + id);
			VxHolder.vertx.cancelTimer(id);
		}
		timerTask.clear();
	}

	/**
	 * 注册事件处理器
	 */
	public void registerEventHandler(EventHandler handler) {

		EventTypeEnum[] eventTypes = handler.getEventTypes();
		if (eventTypes != null) {
			eventModule.registerEventHandler(handler);
		}
	}

	public void registerEventHandler(EventTypeEnum[] eventTypes, EventHandler handler) {
		if (eventTypes != null) {
			eventModule.registerEventHandler(eventTypes, handler);
		}
	}

	public void registerEventHandler(EventTypeEnum eventType, EventHandler handler) {
		if (eventType != null) {
			eventModule.registerEventHandler(eventType, handler);
		}
	}

	public void handleEvent(GameEvent gameEvent) {
		eventModule.handleEvent(gameEvent);
	}

	public void handleEvent(EventTypeEnum eventType) {
		eventModule.handleEvent(new GameEvent(eventType));
	}

	public void handleEvent(EventTypeEnum eventType, Object... params) {
		eventModule.handleEvent(new GameEvent(eventType, params));
	}

	public EventModule getEventModule() {
		return eventModule;
	}

	public PlayerModule getPlayerModule() {
		return getModule(PlayerModule.class);
	}

	public HeroModule getHeroModule() {
		return getModule(HeroModule.class);
	}

	public HCHeroModule getHCHeroModule() {
		return getModule(HCHeroModule.class);
	}

	public HCCommonModule getHCCommonModule() {
		return getModule(HCCommonModule.class);
	}

	public DragonModule getDragonModule() {
		return getModule(DragonModule.class);
	}

	public DragonSkillModule getDragonSkillModule() {
		return getModule(DragonSkillModule.class);
	}

	public ItemModule getItemModule() {
		return getModule(ItemModule.class);
	}

	public ShopModule getShopModule() {
		return getModule(ShopModule.class);
	}

	public VarModule getVarModule() {
		return getModule(VarModule.class);
	}
	public MailModule getMailModule() {
		return getModule(MailModule.class);
	}

	public QuestModule getQuestModule() {
		return getModule(QuestModule.class);
	}

	public CurrencyModule getCurrencyModule() {
		return getModule(CurrencyModule.class);
	}

	public ActivityModule getActivityModule() {
		return getModule(ActivityModule.class);
	}

	public FuncModule getFuncModule() {
		return getModule(FuncModule.class);
	}

	public AttrModule getAttrModule() {
		return getModule(AttrModule.class);
	}

	public ChapterModule getChapterModule() {
		return getModule(ChapterModule.class);
	}

	public PointRewardModule getPointRewardModule() {
		return getModule(PointRewardModule.class);
	}

	public PetModule getPetModule() {
		return getModule(PetModule.class);
	}

	public DevelopModule getDevelopModule() {
		return getModule(DevelopModule.class);
	}

	public VipModule getVipModule(){
		return getModule(VipModule.class);
	}
	public Player() {
	}

	public Player(PlayerData data) {
		this.data = data;
		this.playerId = data.getPlayerId();
		initPlayerModule() ; 
	}

	@SuppressWarnings("unchecked")
	public void initPlayerModule() {
		HashMap<String, BasePlayerModule> modules = null;
		String modules2 = getData().getModules();
		if (!StringUtils.isEmpty(modules2) && !"[]".equals(modules2)) {
			modules = JsonUtil.parseObjectWithType(modules2);
		}
		initModule(modules);
	}

	public void initModule(HashMap<String, BasePlayerModule> modulesFromDb) {
		for (Class<? extends BasePlayerModule> clazz : allModuleClass) {
			try {
//				if (Modifier.isAbstract(clazz.getModifiers())) {
//					continue;
//				}
				BasePlayerModule instance = createBasePlayerModuleInstance(clazz, modulesFromDb);
//				if (!instance.isComplete()) {
//					continue;
//				}
				instance.initDefault(this);
				modules.put(clazz.getName(), instance);
				if (instance instanceof GoodsModule) {
					GoodsModule goodsModule = (GoodsModule) instance;
					GoodsTypeEnum goodsTypeEnum = goodsModule.getGoodsTypeEnum();
					goodsModules.put(goodsTypeEnum.getId(), goodsModule);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public BasePlayerModule  createBasePlayerModuleInstance(Class<? extends BasePlayerModule> clazz,HashMap<String, BasePlayerModule> modulesFromDb) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String name = clazz.getName() ; 
		if (modulesFromDb !=null) {
			BasePlayerModule basePlayerModule = modulesFromDb.get(name); 
			if (basePlayerModule != null) {
				return basePlayerModule ; 
			}
		}
		return clazz.getDeclaredConstructor().newInstance();
	}

	public List<BasePlayerModule> getModuleSorted() {
		List<BasePlayerModule> ret = new ArrayList<BasePlayerModule>(modules.values());
		Collections.sort(ret);
		return ret;
	}

	public boolean isEnough(int id, int count) {
		return getGoodsModule(id).isEnough(id, count);
	}

	public Collection<BasePlayerModule> getAllModule()
	{
		return modules.values();
	}

	public Map<String, BasePlayerModule> getModules() {
		return modules;
	}

	public List<Long> getTimerTask() {
		return timerTask;
	}

	public void setModules(Map<String, BasePlayerModule> modules) {
		this.modules = modules;
	}

	/** 
	 * 获取处理这个id的物品模块
	 * @param id 配置表id
	 * @return
	 */
	public GoodsModule<? extends Item, ? extends Item> getGoodsModule(int id) {
		int goodsType = ItemHelper.getGoodsType(id);
		return this.goodsModules.get(goodsType);
	}

	public PlayerInfo toProto() {
		PlayerInfo.Builder builder = PlayerInfo.newBuilder();
		builder.setId((int) getData().getPlayerId().longValue());
		builder.setName(getData().getName());
//		builder.setLevel(getData().getLevel());
//		builder.setExp(getData().getExp());
		builder.setHead(getData().getHead());
		builder.setHeadFrame(getData().getHeadFrame());
		builder.setImage(getData().getImage());
		builder.setIsMan(getData().getGender());
//		builder.setVipLevel(getData().getVipLevel());
//		builder.setVipExp(getData().getVipExpTotal());
//		builder.setPowerRecoverTime(PlayerHelper.recoverPower(this) * 1000 + "");
//		builder.setSpiritReceiveInfo(getData().getSpiritReceiveInfo());
//		builder.setActionPower(getData().getActionPower());
//		builder.setActionPowerRecoverTime(PlayerHelper.recoverActionPower(this));
		builder.setOfflineTime(getData().getOfflineTime().toString());
		builder.setServerId(getData().getServerId());
		builder.setServerName(VirtualServerManager.instance().get(getData().getServerId()).ServerName);
		return builder.build();
	}

	public GmPlayerInfo toGmProto() {
		GmPlayerInfo.Builder builder = GmPlayerInfo.newBuilder();
		builder.setPlayerId(String.valueOf(getData().getPlayerId()));
		builder.setChannel(getData().getChannelId());
		builder.setServerId(getData().getServerId()); 
		builder.setPlantform("未知");
		builder.setName(getData().getName()); 
		builder.setLevel(getLevel());
		builder.putAllAssets(getCurrencyModule().getCurrencyMap().getMap());
		builder.setUnionId("不存在");
		builder.setUnionName("不存在"); 
		builder.setIsOnline(getGameClient()!=null); 
		builder.setCreateTime((int) (DateUtil.parse(getData().getCreateDate()).getTime()/1000)) ; 
		builder.setLastLoginTime((int) (DateUtil.parse(getData().getLoginDate()).getTime() / 1000));
		ChapterModule chapterModule = getChapterModule();

		builder.setCurBattleId(chapterModule.getFightMainBattleId());
		builder.setPower(getAttrModule().getPower());
		builder.setChargeCumulation(getQuestModule().getCumulativeCount(ConditionTypeEnum.AccumulatedRecharge));
		
		return builder.build();
	}
	
	/** 
	 * 支付，有可能支付普通货币，也有可能支付rmb
	 * 一般普通货币支付的，尽量不要调用这个方法，这个方法尽量处理rmb支付的。 
	 * 
	 * @param payType  支付类型，购买的什么类型的东西
	 * @param id   针对支付类型的id，例如购买月卡，id就是月卡id
	 * @param cost   费用，第一个是支付类型，第二个是支付的id，第三个是支付的数量
	 * @return
	 */
	public Future<Boolean> pay(PayType payType,int id, int[] cost,int... otherId) {
		if (cost == null || cost.length == 0 || (cost.length == 1 && cost[0] == 0)) {
			return Future.succeededFuture(true);
		}
		int costType = cost[0] ; 

		Promise<Boolean> promise = Promise.promise(); 
		
		if (costType == ShopHelper.COST_TYPE_RESOURCE) {
			boolean delResources = PlayerHelper.delResources(this, cost[1], cost[2], OpType.BuyGoods);
			if (!delResources) {
				PlayerHelper.sendErrorProtocol(getPlayerId(), ErrorMsgEnum.resource_not_enough.getId());
				promise.complete(false);
			}else {
				promise.complete(true);
			}
		} else if (costType == ShopHelper.COST_TYPE_RECHARGE) {
			if (Boolean.getBoolean("DisableRecharge")) {
				handleEvent(EventTypeEnum.Charge, cost[1]);
				return Future.succeededFuture(true);
			}
//			String platform = "Android";
//			if (getAccount().getPlatform() == 1 || getAccount().getPlatform() == 3){
//				platform = "IOS";
//			}
			String platform = getAccount().sdkPayChannel.equals("0010") ? "Android" : "IOS";
			if (platform.equals("IOS") && getAccount().version.equals(cn.game.util.Config.disableIosPayClientVersion)) {
				fail(ErrorMsgEnum.disable_ios_pay);
			}
			final int rmbCost = Boolean.getBoolean("AllRecharge1") ? 1 : cost[1];
			final int chargeItemId = Boolean.getBoolean("AllRecharge1") ? 1007 : cost[2];
			// 有代金券用代金券，不用支付
			long vouchersCount = getCurrencyModule().get(Asset.Vouchers);
			if (vouchersCount >= rmbCost) {
				PlayerHelper.delResources(this, Asset.Vouchers.ID, rmbCost, OpType.BuyGoods);
				handleEvent(EventTypeEnum.Charge, rmbCost);
				return Future.succeededFuture(true);
			}

			PaymentOrderCreateRequest_7d000020 paymentOrderCreate = PaymentOrderCreateRequest_7d000020.newBuilder().setPlayerId(getPlayerId()).setPlatform(platform)
					.setSessionId(getGameClient().getSessionId())
					.setGoodsPrice(rmbCost * 100)
					.setItemId(chargeItemId + "")
					.build();
			Future<PaymentOrderCreateResponse_7d000021> requestRemoteServer = VxHolder.requestRemoteServer(ServerType.Login,
					paymentOrderCreate);
			requestRemoteServer.map(r -> {
				PaymentOrderCreateResponse_7d000021 body = r;
				if (body.getOrderId() == 0) {
					getGameClient().sendProtocol(PaymentOrderPush_15010020.getDefaultInstance(), ErrorMsgEnum.payment_order_create_fail.getId());
				} else {
					getGameClient().sendProtocol(PaymentOrderPush_15010020.newBuilder().setOrder(body.getOrder()).setOrderId(body.getOrderId()+"").build());
					getPlayerModule().addPayCallback(body.getOrderId(), promise);
					PayItem payItem = new PayItem();
					payItem.setOrderId(body.getOrderId());
					payItem.setRmb(rmbCost);
					payItem.setPayType(payType);
					payItem.setPayId(id);
					if (otherId != null){
						payItem.addPaySubIds(otherId);
					}
					getPlayerModule().addPayItems(payItem);
				}
				return null;
			}).onFailure(r -> {
				log.error("登录服创建充值订单失败： ", r);
				PlayerHelper.sendErrorProtocol(getPlayerId(), ErrorMsgEnum.unknown.getId()); 
				promise.complete(false);
			}); 
		} else if (costType == ShopHelper.COST_TYPE_ADVERTISE) {
			handleEvent(EventTypeEnum.WatchAds);
			promise.complete(true);
		}
		return promise.future() ; 
	}

	/** 
	 * 获取福利的加成值
	 * @param type
	 * @return
	 */
	public int getWelfareValue(WelfareTypeEnum type) {
		int ret = 0;
		// 月卡加成
		MonthCardModule monthCardModule = getModule(MonthCardModule.class);
		Set<Integer> keySet = monthCardModule.getMonthCards().keySet();
		for (Integer id : keySet) {
			MonthCardConfig monthCardConfig = MonthCardManager.instance().get(id);
			if (monthCardConfig.Benefit1.containsKey(type.ID)) {
				ret += monthCardConfig.Benefit1.get(type.ID);
			}
		}

		// Vip加成
		VipModule vipModule = getVipModule();
		VIPConfig curVipConfig = vipModule.getCurVipConfig();
		if (curVipConfig != null && curVipConfig.Benefit.containsKey(type.ID)){
			ret += curVipConfig.Benefit.get(type.ID);
		}
		// 仙友加成
		FairyFriendModule fairyFriendModule = getModule(FairyFriendModule.class);
		Collection<FairyFriend> list = fairyFriendModule.list();
		for (FairyFriend fairyFriend : list) {
			FairyFriendFavorabilityConfig favorabilityConfig = FairyFriendFavorabilityManager
					.instance()
					.getUIFairyListIDLV(fairyFriend.getConfigId(), fairyFriend.getLevel());
			if (favorabilityConfig == null) {
				continue;
			}
			if (favorabilityConfig.FavorabilityAward.containsKey(type.ID)) {
				ret += favorabilityConfig.FavorabilityAward.get(type.ID);
			}
		}

		return ret;
	}

	/** 
	 * 是否有某种福利
	 * @param type
	 * @return
	 */
	public boolean hasWelfare(WelfareTypeEnum type) {
		return getWelfareValue(type) > 0;
	}

	/** 
	 * 某个功能是否开启了
	 * @param type
	 * @return
	 */
	public boolean isFuncOpen(InitialUI type) {
		return getLevel() >= type.DisplayLevel;
	}

	public SimplePlayerInfo buildSimplePlayerInfo() {

		Long playerId = getData().getPlayerId();
		SimplePlayerInfo.Builder builder = SimplePlayerInfo.newBuilder();
		builder.setId(playerId + "");
		builder.setLevel(getData().getLevel());
		builder.setName(getData().getName());
		builder.setOnline(true);
		builder.setOfflineTime(0);
		builder.setHead(getData().getHead());
		builder.setHeadFrame(getData().getHeadFrame());
		builder.setServerId(getData().getServerId());
		builder.setServerName(VirtualServerManager.instance().get(getData().getServerId()).ServerName);
		builder.setTiandaoLevel(getDevelopModule().getHeavenlyDaoLevel());
		builder.setCombatEffectiveness(getAttrModule().getPower());
		
		return builder.build();

	}


	/** 
	 * 获取玩家等级
	 * @return
	 */
	public int getLevel() {
		return getLevel(Asset.playerExp);
	}

	/** 
	 * 获取某种等级
	 * @param exp 代表经验的id
	 * @return
	 */
	public int getLevel(Asset exp) {
		return getPlayerModule().getExpLevelMap().getValue(exp.ID);
	}

	public void handleFail(Throwable t) {
		handleFail(PlayerErrorPush_01000099.getDefaultInstance(), t);
	}

	/** 
	 * 处理客户端请求出现的异常 ，发送默认错误返回并记录异常日志。 
	 * 一般用在异步调用的异常处理
	 * @param response 发生错误时的返回消息
	 * @param t  异常
	 */
	public void handleFail(Message response, Throwable t) {
		// 逻辑错误，非法逻辑
		if (t instanceof LogicException) {
			LogicException logicException = (LogicException) t;
			getGameClient().sendProtocol(response, logicException.getErrorCode());
			return;
		}
		// 一般是vertx主动生成的错误码错误
		if (StringUtils.isNumeric(t.getMessage())) {
			getGameClient().sendProtocol(response, Integer.parseInt(t.getMessage()));
			return;
		}
		// 未知异常，记录日志
		getGameClient().sendProtocol(response, ErrorMsgEnum.unknown.getId());
		log.error("", t);
	}

	/** 
	 * 处理function类型的异步调用异常
	 * @param t
	 * @return
	 */
	public String handleFailFunction(Throwable t) {
		handleFail(t);
		return "";
	}

	/** 
	 * 主动抛出一个错误，中断当前流程。 
	 * @param errorMsgEnum
	 */
	public void fail(ErrorMsgEnum errorMsgEnum) {
		throw new LogicException(errorMsgEnum.ID);
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public PlayerData getData() {
		return data;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isIslogouting() {
		return islogouting;
	}

	public void setIslogouting(boolean islogouting) {
		this.islogouting = islogouting;
	}

	public GameClient getGameClient() {
		return gameClient;
	}

	public void setGameClient(GameClient gameClient) {
		this.gameClient = gameClient;
	}


	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	public int getVipLevel(){
    return getPlayerModule().getExpLevelMap().getValue(Asset.VIPExp.ID);
	}



	public OfflineBattleModule getOfflineBattleModule() {
		return getModule(OfflineBattleModule.class);
	}
	public SecretscriptModule getSecretscriptModule() {
        return getModule(SecretscriptModule.class);
    }
	public String getServerId() {
		return getData().getServerId();
	}

	public long getLastLoginTimer() {
		return  DateUtil.parse(getData().getLoginDate()).getTime();
	}

	public long getCreateTimer() {
		return DateUtil.parse(getData().getCreateDate()).getTime();
	}

	public String getOpenId(){
		return account.deviceId;
	}

	public InviteModule getInviteModule(){
		return getModule(InviteModule.class);
	}
}