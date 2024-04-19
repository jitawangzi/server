package cn.game.games.cache.entity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.net.vertx.VxHolder;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.GoodsModule;
import cn.game.games.core.event.EventHandler;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.GameServer;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.currency.CurrencyModule;
import cn.game.games.net.game.module.develop.dragon.DragonModule;
import cn.game.games.net.game.module.develop.hero.HeroModule;
import cn.game.games.net.game.module.develop.skill.DragonSkillModule;
import cn.game.games.net.game.module.event.EventModule;
import cn.game.games.net.game.module.item.ItemModule;
import cn.game.games.net.game.module.mail.MailModule;
import cn.game.games.net.game.module.player.PlayerModule;
import cn.game.games.net.game.module.player.VarModule;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.games.net.game.module.shop.ShopHelper;
import cn.game.games.net.game.module.shop.ShopModule;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.ResourceConsumeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerInfo;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderCreateRequest_7d000020;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderCreateResponse_7d000021;
import cn.game.protocol.protobuf.ShopMsg.PaymentOrderPush_15010020;
import cn.game.util.JsonUtil;
import cn.game.util.ServerType;
import cn.game.util.reflect.ClassHelper;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;

//@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class Player  {
	private static  transient Logger log = LoggerFactory.getLogger(Player.class);

	private long playerId;
	private transient static Set<Class<? extends BasePlayerModule>> allModuleClass;
	static {
		allModuleClass = ClassHelper.findSubclasses("cn.game.games", BasePlayerModule.class);
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
	/** 玩家扩展数据 */
	private PlayerExt ext;
	private transient GameClient gameClient;
	private List<Long> timerTask = new ArrayList<>();

	/** 支付后的操作 */
	private Consumer<?> paymentAction;

	public <T extends BasePlayerModule> T getModule(Class<? extends BasePlayerModule> clazz) {
		return (T) this.modules.get(clazz.getName());
	}

	public long setPeriodic(long delay, Handler<Long> handler) {
		long timer = gameClient.getContext().setPeriodic(delay, handler);
		timerTask.add(timer);
		return timer;
	}

	public void cancelTimer(long id) {
		timerTask.remove(id);
		VxHolder.vertx.cancelTimer(id);
	}
	public void cancelAllTimer() {
		for (Long id : timerTask) {
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
	public Player() {
	}

	public Player(PlayerData data) {
		this.data = data;
		this.playerId = data.getPlayerId();
		initPlayerModule() ; 
	}

	public void initPlayerModule() {
		if (GameServer.getInstance().isSinglePlayerTable()) {
			HashMap<String, BasePlayerModule> modules = null;
			String modules2 = getData().getModules();
			if (!StringUtils.isEmpty(modules2) && !"[]".equals(modules2)) {
				modules = JsonUtil.parseObject(modules2, HashMap.class);
			}
			initModule(modules);
		}else {
			initModule(null); 
		}
	}
	private void initModule(HashMap<String, BasePlayerModule> modulesFromDb) {
		for (Class<? extends BasePlayerModule> clazz : allModuleClass) {
			try {
				if (Modifier.isAbstract(clazz.getModifiers())) {
					continue;
				}
				BasePlayerModule instance = createBasePlayerModuleInstance(clazz, modulesFromDb)  ; 
				if (!instance.isComplete()) {
					continue;
				}
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
		List<BasePlayerModule> ret = new ArrayList<BasePlayerModule>();
		ret.addAll(modules.values());
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
	public GoodsModule getGoodsModule(int id) {
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
		builder.setIsMan(getData().getGender());
//		builder.setVipLevel(getData().getVipLevel());
//		builder.setVipExp(getData().getVipExpTotal());
//		builder.setPowerRecoverTime(PlayerHelper.recoverPower(this) * 1000 + "");
//		builder.setSpiritReceiveInfo(getData().getSpiritReceiveInfo());
//		builder.setActionPower(getData().getActionPower());
//		builder.setActionPowerRecoverTime(PlayerHelper.recoverActionPower(this));
		builder.setOfflineTime(getData().getOfflineTime().toString());
		return builder.build();
	}
	
	/** 
	 * 支付，有可能支付普通货币，也有可能支付rmb
	 * @param cost 
	 * @return
	 */
	public Future<Boolean> pay(int[] cost) {
		int costType = cost[0] ; 
		Promise<Boolean> promise = Promise.promise(); 
		
		if (costType == ShopHelper.COST_TYPE_RESOURCE) {
			boolean delResources = PlayerHelper.delResources(this, cost[1], cost[2], ResourceConsumeEnum.BuyGoods);
			if (!delResources) {
				PlayerHelper.sendErrorProtocol(getPlayerId(), ErrorMsgEnum.resource_not_enough.getId());
				promise.complete(false);
			}else {
				promise.complete(true);
			}
		} else if (costType == ShopHelper.COST_TYPE_RECHARGE) {
			
			PaymentOrderCreateRequest_7d000020 paymentOrderCreate = PaymentOrderCreateRequest_7d000020.newBuilder().setPlayerId(getPlayerId()).setSessionId(getGameClient().getSessionId()).setGoodsPrice(cost[1]).build();
			Future<Message<PaymentOrderCreateResponse_7d000021>> requestRemoteServer = VxHolder.requestRemoteServer(ServerType.Login, paymentOrderCreate);
			requestRemoteServer.onSuccess(r -> {
				PaymentOrderPush_15010020 paymentOrderPush_15010020 = PaymentOrderPush_15010020.newBuilder().setOrder(r.body().getOrder()).build(); 
				getGameClient().sendProtocol(paymentOrderPush_15010020); 
				getPlayerModule().addPayCallback(r.body().getOrderId(), promise); 
			}).onFailure(r -> {
				log.error("",r) ;
				PlayerHelper.sendErrorProtocol(getPlayerId(), ErrorMsgEnum.unknown.getId()); 
				promise.complete(false);
			}); 
		} else if (costType == ShopHelper.COST_TYPE_ADVERTISE) {
			handleEvent(EventTypeEnum.WatchAds);
			promise.complete(true);
		}
		return promise.future() ; 
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

	public void setData(PlayerData data) {
		this.data = data;
	}

	public PlayerExt getExt() {
		return ext;
	}

	public void setExt(PlayerExt ext) {
		this.ext = ext;
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

	public Consumer<?> getPaymentAction() {
		return paymentAction;
	}

	public void setPaymentAction(Consumer<?> paymentAction) {
		this.paymentAction = paymentAction;
	}

	
	
}