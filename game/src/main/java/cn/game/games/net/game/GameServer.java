package cn.game.games.net.game;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.redisson.api.RKeys;
import org.redisson.api.RLock;

import com.ctrip.framework.apollo.ConfigService;
import com.google.common.io.Files;

import cn.game.core.base.ActiveServerListManager;
import cn.game.core.base.ServerContext;
import cn.game.core.base.ServerList;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.cache.id.IdCache;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.core.net.client.LogoutType;
import cn.game.core.net.process.Processor;
import cn.game.core.net.remote.RemoteLoginServerInterface;
import cn.game.core.net.rpc.CallType;
import cn.game.core.net.rpc.RpcFactory;
import cn.game.core.net.rpc.vertx.VertxRPCService;
import cn.game.core.net.rpc.vertx.VertxRpcClient;
import cn.game.core.net.vertx.BusinessLogicVerticle;
import cn.game.core.net.vertx.MsgConsumerVerticle;
import cn.game.core.net.vertx.VxContextRegistry;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.performance.LoadManager;
import cn.game.core.performance.evaluation.LoadState;
import cn.game.core.task.SchedulerService;
import cn.game.core.task.TaskManager;
import cn.game.core.util.AsyncUtils;
import cn.game.core.util.IdUtil;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.GameServerStatus;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.core.collector.PlayerConcurrencyCollector;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.core.push.PushService;
import cn.game.games.core.vertx.WebSocketVerticle;
import cn.game.games.net.cross.remote.CrossServerInterface;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.init.GameIdManagerInitializer;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.manager.DataFixManager;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.manager.PlayerNameManager;
import cn.game.games.net.game.manager.PressureTestManager;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.games.net.game.remote.GameServerInterface;
import cn.game.games.util.BIHelper;
import cn.game.protocol.generated.helper.ManagerHelper;
import cn.game.protocol.protobuf.ServerMsg.GameStatusPublish_7d000017;
import cn.game.util.Config;
import cn.game.util.GameUtil;
import cn.game.util.JsonUtil;
import cn.game.util.LockUtil;
import cn.game.util.RedisUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringApolloLoader;
import cn.game.util.SpringContextLoader;
import cn.game.util.ThreadUncaughtExceptionHandler;
import cn.game.util.file.WatchServiceManager;
import cn.game.util.log.Log4j2ApolloLoader;
import cn.game.util.log.LoggerType;
import cn.game.util.quartz.QuartzInitializer;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.jmx.JmxMeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.micrometer.backends.BackendRegistries;

/**
 * vertx重构通讯
 * 2021年4月12日 上午10:17:27
 * @author SYQ
 */
public class GameServer implements GameServerMBean {

//	private final Logger log = LoggerFactory.getLogger(GameServer.class);
	private Properties initialProp;
	private QuartzInitializer quartzInitializer;
//	private String serverId;
	private static final GameServer instance = new GameServer();

	private String wsVerticle;

	private GameServer() {
	};

	public static GameServer getInstance() {
		return instance;
	}

	public static void main(String args[]) {
		try {
			instance.start(args);
		} catch (Throwable e) {
			ServerContext.getInstance().handleStartFail(e);
		}

	}

	public void start(String[] args) throws Exception {
		ServerType serverType = ServerType.Game;
		long start = System.currentTimeMillis();
		String serverId = GameUtil.parseServerId(args, serverType);
//		LoggerManager.init();
		Log4j2ApolloLoader.getInstance().init();

		ServerContext.getInstance().initBase(serverId, serverType);
		ServerContext.getInstance().setEventBus(ServerEventBus.getInstance());

		LoggerType.Stdout.logger.debug(System.getProperty("java.class.path"));
		LoggerType.Stdout.logger.info("启动逻辑服。。");
		Thread.setDefaultUncaughtExceptionHandler(new ThreadUncaughtExceptionHandler());
//		instance.log.info("启动逻辑服。。");
		Config.load();
		VxHolder.init();
		IdUtil.init();

		ActiveServerListManager.getInstance().start(ServerType.values());
		ServerContext.getInstance().init();

//		util.SpringContextLoader.main(args);
		// init with apollo config
		SpringApolloLoader springApolloLoader = new SpringApolloLoader();
		springApolloLoader.init();
		initQuartz();
		initGameServerConfig();
		initVerticle();

//		initRemoteInterface();
//		DAO.listenPauseUpdateDb();
//		TreeWordFilter.init("filterWord.txt");
//		this.maxPlayerId = initialProp.getIntProperty("player.max.id", 0);
//		this.minPlayerId = initialProp.getIntProperty("player.min.id", 0);
//		int port = initialProp.getIntProperty("netty.port", 0);

		new Thread(WatchServiceManager.getInstance().setWatchDirs("xml", "config"), "WatchServiceManager").start();
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		mBeanServer.registerMBean(instance,
				new ObjectName("net.game:type=GameServer,name=GameServer_" + ServerContext.getInstance().getServerId()));
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutdown();
			}
		});
		initScheduleTask();
//		initLoadManager();
		// ******************** 业务逻辑启动 **************************

		ManagerHelper.init();

		ActivityStateManager.getInstance().start();
//		ActivityStateManager.getInstance().initGlobal();
		PlayerManager.getInstance().init2();
		ClassManager.getInstance().init();
		PressureTestManager.getInstance().init();
		BIHelper.start();
		checkPlayerJsonStruct();
//		KeywordFilter.initializeFromFile();
		RankService.getInstance().initRewardTask();
		PushService.getInstance().init(PlayerHelper::sendProtocol);
		initSimplePlayers();
//		initAllSimplePlayers();
		kickClientsAfterChangeTime();

		MailHelper.initLoadGlobalMail();

		DataFixManager.getInstance().init();

		GameIdManagerInitializer.initialize();

//		Long playerId = (Long) dataGameServerInterfaceSync.exec(PlayerExtMapper.class,
//				"selectMaxId", null);
//		this.dbMaxPlayerId = new AtomicLong(playerId == null ? minPlayerId : playerId);
//		log.info("max player id :" + dbMaxPlayerId);
//		log.info("逻辑服[{}]启动成功,耗时[{}]s", serverId, (System.currentTimeMillis() - start) / 1000);
		LoggerType.Stdout.logger.info(String.format("逻辑服[%s]启动成功,耗时[%s]s", ServerContext.getInstance().getServerId(),
				(System.currentTimeMillis() - start) / 1000));
		System.err.println("Game Server startup complete");
	}

	/** 
	 * 如果redis中清空数据了，则重新把数据库中的数据同步到redis
	 * 同步SimplePlayer和名字
	 */
	private void initSimplePlayers() {
		RLock lock = LockUtil.tryLockSync(0, 30, TimeUnit.MINUTES, CacheType.GAME_SERVER_LOCK.name());
		if (lock == null) {
			return;
		}
		try {
			RKeys keys = RedisUtil.getRedis().getKeys();
			boolean found = false;

			Stream<String> keyStream = keys.getKeysStreamByPattern(CacheType.PLAYER_SIMPLE.name() + "*");
			if (keyStream.count() > 0) {
				found = true;
			}
			if (!found) {

				Function<Player, Boolean> function = player -> {
					PlayerHelper.saveSimplePlayerToRedisSync(player);
					// 初始化名字，名字--id
					PlayerNameManager.getInstance().addExistingUsername(player.getData().getName());
					PlayerNameManager.getInstance().saveName2IdSync(player.getData().getName(), player.getData().getPlayerId());
					return false;
				};
				PlayerHelper.loadAndProcessPlayers(function);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			lock.unlock();
		}
	}

	private void initAllSimplePlayers() {
		try {
			Function<Player, Boolean> function = player -> {
				PlayerHelper.saveSimplePlayerToRedisSync(player);
				// 初始化名字，名字--id
				PlayerNameManager.getInstance().addExistingUsername(player.getData().getName());
				PlayerNameManager.getInstance().saveName2IdSync(player.getData().getName(), player.getData().getPlayerId());
				return false;
			};
			PlayerHelper.loadAndProcessPlayers(function);

		} catch (Exception e) {
			throw e;
		} finally {
		}
	}

	/** 
	 * 当修改时间测试某些和时间相关的功能时，如果时间往后调了超过一个小时，则自动踢出客户端
	 * 主要方便测试跨天的一些逻辑。 
	 */
	private void kickClientsAfterChangeTime() {
		if (!ServerContext.getInstance().getRunMode().isTest()) {
			return; // 非测试模式不需要
		}
		AtomicLong lastCheckTime = new AtomicLong(System.currentTimeMillis());
		SchedulerService.getInstance().scheduleAtFixedRate(() -> {
			long now = System.currentTimeMillis();
			if (Math.abs(now - lastCheckTime.get()) > 60 * 60 * 1000) {
				GameClientManager.getInstance().logoutAll(LogoutType.TimeChange);
			}
			lastCheckTime.set(now);

		}, 1, TimeUnit.SECONDS);
	}

	/** 
	 * 检查 player 模块数据 结构是否有变化
	 * @throws Exception
	 */
	private void checkPlayerJsonStruct() throws Exception {
		String fileNameString = "player.json";
		File file = new File(fileNameString);
		if (file.exists()) {
//			String json = FileUtils.readFileToString(file, Charset.defaultCharset());
			String json = Files.readFirstLine(file, Charset.defaultCharset());

			Player player = null;
			try {
				player = JsonUtil.parseObjectWithType(json);
			} catch (Exception e) {
				throw new RuntimeException(
						"Player结构有变化，json反序列化失败，修正数据兼容后重试。" + " ---- 如果十分肯定是开发过程中的正常调整，不会影响到线上数据，可以删除" + fileNameString + "文件后重新启动", e); // 反序列化失败，

			}
			Files.write(JsonUtil.toJsonStringWithType(player), file, Charset.defaultCharset());
//			FileUtils.writeStringToFile(file, JsonUtil.toJsonString(player), Charset.defaultCharset());
		} else {
			Player player = new Player();
			player.initModule(null);
			Files.write(JsonUtil.toJsonStringWithType(player), file, Charset.defaultCharset());
//			FileUtils.writeStringToFile(file, JsonUtil.toJsonString(player), Charset.defaultCharset());
		}
	}

	private void initGameServerConfig() throws Exception {
		GameServerStatus.getInstance().start().toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
		if (GameServerStatus.getInstance().getServerInfo() == null) {
			throw new IllegalArgumentException(
					"GameServerInfo is null，cant find serverId from zookeeper ,serverId " + ServerContext.getInstance().getServerId());
		}
	}

	private void initScheduleTask() {
		SchedulerService.getInstance().scheduleAtFixedRate(() -> {
			VxHolder.broadcastRemoteServer(ServerType.Login,
					GameStatusPublish_7d000017.newBuilder()
							.setServerId(ServerContext.getInstance().getServerId())
							.setOnlinePlayerCount(PlayerManager.getInstance().getOnlineCount())
							.build());
		}, 1, TimeUnit.MINUTES);
	}

	private void initVerticle() throws Exception {
		VxHolder.deployVerticleSync((VertxRpcClient) ServerContext.getInstance().getRpcClient());

		int numVerticles = VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE;
		VxContextRegistry.getInstance().init(numVerticles);
		for (int i = 0; i < numVerticles; i++) {
			BusinessLogicVerticle verticle = new BusinessLogicVerticle(i);
//			VxHolder.deployVerticleSync(verticle, new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD));
			VxHolder.deployVerticleSync(verticle);
		}
		DeploymentOptions options = new DeploymentOptions().setInstances(numVerticles);
//		options.setThreadingModel(ThreadingModel.VIRTUAL_THREAD);
		wsVerticle = VxHolder.deployVerticleSync(WebSocketVerticle.class, options);

		String serverId = ServerContext.getInstance().getServerId();
		ServerType serverType = ServerContext.getInstance().getServerType();
		Processor processor = SpringContextLoader.getContext().getBean(Processor.class);
		VxHolder.deployVerticleSync(new MsgConsumerVerticle(serverId, serverType, processor));
		VertxRPCService verticle = new VertxRPCService(null, serverId, serverType, processor);
		VxHolder.deployVerticleSync(verticle);

	}

	private void initQuartz() throws IOException {
		com.ctrip.framework.apollo.Config initialProp = ConfigService.getAppConfig();// config instance is singleton
		String configFile = initialProp.getProperty("quartzConfig", null);
		if (configFile != null) {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			Properties properties = new Properties();
			properties.load(loader.getResourceAsStream(configFile));
			quartzInitializer = new QuartzInitializer(properties);
			quartzInitializer.Initializer();
		}
	}

	public void shutdown() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
//		log.info("Game Server starts to shutdown ...");
		LoggerType.Stdout.logger.info("Game Server starts to shutdown ...");
		// 通知玩家退出
		GameClientManager.getInstance().notifyLogoutAllClients();
		stopWatch.split();
		LoggerType.Stdout.logger.info("Game Server notifyLogoutAllClients complete, use time {} ms", stopWatch.getSplitTime());
		try {
			// 关闭websocket服务
			AsyncUtils.await(VxHolder.vertx.undeploy(wsVerticle));
		} catch (Exception e) {
			LoggerType.Stdout.logger.error("undeploy wsVerticle fail", e);
		}
		stopWatch.split();
		LoggerType.Stdout.logger.info("Game Server undeploy wsVerticle complete, use time {} ms", stopWatch.getSplitTime());
		TaskManager.getInstance().shutdown();
		try {
			stopWatch.split();
			LoggerType.Stdout.logger.info("TaskManager shutdown complete, use time {} ms", stopWatch.getSplitTime());

			VxContextRegistry.getInstance().shutdown();
			stopWatch.split();
			LoggerType.Stdout.logger.info("VxContextRegistry shutdown complete, use time {} ms", stopWatch.getSplitTime());

			LoggerType.Stdout.logger.info("start storeAllPlayers on shutdown");
			// 同步存储所有玩家的数据
			Config.remoteCallTimeOut = Config.shutdownWaitTime;
//			setDataServerSyncDefault();
			GameClientManager.getInstance().storeAllPlayers();
			stopWatch.split();
			LoggerType.Stdout.logger.info("Game Server storeAllPlayers complete, use time {} ms", stopWatch.getSplitTime());
			SpringContextLoader.getContext().close();
			quartzInitializer.destroyed();

			ServerContext.getInstance().shutdown();
			LoggerType.Stdout.logger.info("start close vertx on shutdown");
			AsyncUtils.await(VxHolder.vertx.close(), 300, TimeUnit.SECONDS); // 等待vertx关闭完成
			stopWatch.split();
			LoggerType.Stdout.logger.info("close vertx complete, use time {} ms", stopWatch.getSplitTime());

			stopWatch.stop();
			LoggerType.Stdout.logger.info("Game Server  safe  shutdown, use time {} ms ", stopWatch.getTime());
			// 安全关闭logback
//			LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
//			context.stop();
			LogManager.shutdown(); // 关闭log4j2日志

		} catch (Throwable e) {
//			log.error("Game Server Shutdown err ", e);
			e.printStackTrace();
			System.err.println("Game Server Shutdown err ");
			LoggerType.Stdout.logger.error("Game Server Shutdown err ", e);
		}

	}

	/**
	 * 是否是本地服务器
	 * @param serverId
	 * @return
	 */
	@Deprecated
	public boolean isLocalServer(String serverId) {
		return StringUtils.isEmpty(serverId) || ServerContext.getInstance().getServerId().equals(serverId);
	}

	/**
	 * 获取逻辑服远程调用接口
	 * @param serverId 逻辑服id,如果不是指定某个id的服务器,则传null
	 * @return
	 */
	public GameServerInterface getGameServerInterface(CallType callType, String serverId) {
		return RpcFactory.getImpl(GameServerInterface.class, ServerContext.getInstance().getRpcClient(), callType, serverId,
				ServerType.Game);
	}

	/**
	 * 获取处理某类型对象的逻辑服远程调用接口
	 * @param @DistributedObjectType 什么类型的对象
	 * @param targetId  对象的唯一id
	 * @return
	 */
	public GameServerInterface getGameServerInterface(DistributedObjectType objectType, long targetId) {

		String serverId = IdCache.getManager(objectType).getServerId(targetId);
		if (StringUtils.isEmpty(serverId) || serverId.equals(ServerContext.getInstance().getServerId())) {
			// 对象不在线，或者在当前服务器，直接由当前服务器处理
			return (GameServerInterface) SpringContextLoader.getContext().getBean(GameServerInterface.class);
		}
		// 其他服务器在线，通过远程调用
		return RpcFactory.getImpl(GameServerInterface.class, ServerContext.getInstance().getRpcClient(), CallType.PointToPoint, serverId,
				ServerType.Game, targetId);

	}

	/**
	 * 获取处理某类型对象的跨服远程调用接口
	 * @param @DistributedObjectType 什么类型的对象
	 * @param targetId  对象的唯一id
	 * @return
	 */
	public CrossServerInterface getCrossServerInterface(DistributedObjectType objectType, long targetId) {

		CallType callType = CallType.PointToPoint;
		String serverId = IdCache.getManager(objectType).getServerId(targetId);
		if (StringUtils.isEmpty(serverId)) {
			callType = CallType.LoadBalancer;
		}
		// 其他服务器在线，通过远程调用
		return RpcFactory.getImpl(CrossServerInterface.class, ServerContext.getInstance().getRpcClient(), callType, serverId,
				ServerType.Cross, targetId);
	}

	/** 
	 * 获取所有跨服的远程接口，用于点对点通讯。 
	 * @return
	 */
	public List<CrossServerInterface> getAllCrossServerInterface() {

		Set<String> serverSet = ActiveServerListManager.getInstance().getServerSet(ServerType.Cross);
		List<CrossServerInterface> ret = new ArrayList<>();

		for (String serverId : serverSet) {
			CrossServerInterface impl = RpcFactory.getImpl(CrossServerInterface.class, ServerContext.getInstance().getRpcClient(),
					CallType.PointToPoint, serverId, ServerType.Cross, 0);
			ret.add(impl);
		}
		return ret;
	}

	/** 
	 * 获取登陆远程通讯接口，一般不需要指定具体的登陆服id，也就是不使用 PointToPoint 方式
	 * @param callType
	 * @return
	 */
	public RemoteLoginServerInterface getRemoteLoginServerInterface(CallType callType) {
		return RpcFactory.getImpl(RemoteLoginServerInterface.class, ServerContext.getInstance().getRpcClient(), callType, null,
				ServerType.Login);

	}

//	public void requestDataServer(Message message, RequestCallback callback) {
//		RocketMQRpcClient.request(getServerId(ServerType.Data), message, callback);
//	}

	// 初始化负载管理器
	private void initLoadManager() throws Exception {
		if (!SystemUtils.IS_OS_LINUX) {
			return;
		}
		LoadManager loadManager = LoadManager.getInstance();
		loadManager.init(VxHolder.vertx);

		// 调整某些指标的权重
		loadManager.setMetricWeight("vertx.eventloop", 0.3); // 提高事件循环监控的权重

		// 注册应用特定的自定义收集器（
		loadManager.registerCollector(new PlayerConcurrencyCollector());

		// 系统启动时，先尝试恢复到正常状态
		GameServerStatus.getInstance()
				.updateServerStatus(ServerContext.getInstance().getServerId(), ServerList.STATUS_OVERLOAD, ServerList.STATUS_RUN);

		// 监听负载状态变化，写入到zk中
		ServerContext.getInstance().registerEventHandler(ServerEventTypeEnum.ServerLoad, event -> {
			LoadState oldState = event.getParameter(0);
			LoadState newState = event.getParameter(1);
			try {
				if (newState == LoadState.CRITICAL) {
					GameServerStatus.getInstance()
							.updateServerStatus(ServerContext.getInstance().getServerId(), ServerList.STATUS_RUN,
									ServerList.STATUS_OVERLOAD);
				} else if (newState == LoadState.NORMAL || newState == LoadState.WARNING) {
					GameServerStatus.getInstance()
							.updateServerStatus(ServerContext.getInstance().getServerId(), ServerList.STATUS_OVERLOAD,
									ServerList.STATUS_RUN);
				}
			} catch (Exception e) {
				LoggerType.Stdout.logger.error("LoadState update to zookeeper failed oldState{} newState{} currentScore{}", oldState,
						newState);
			}

		});

		// 监听状态变化，只是记录日志
		VxHolder.vertx.setPeriodic(5000, id -> {
			LoadState currentState = loadManager.getCurrentState();
			int score = loadManager.getCurrentScore();
			LoggerType.Monitor.logger.info("Current system state: {}, score: {}", currentState, score);
			LoggerType.Monitor.logger.info("Metric:{}", loadManager.showMetrics());
		});

		// 在应用关闭时
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			loadManager.shutdown();
		}));

		// 获取默认注册表
		MeterRegistry registry = BackendRegistries.getDefaultNow();
		// 检查注册表类型，并根据不同类型进行相应配置
		if (registry instanceof PrometheusMeterRegistry) {
			// Prometheus 特定配置
			PrometheusMeterRegistry prometheusRegistry = (PrometheusMeterRegistry) registry;
			prometheusRegistry.config().meterFilter(new MeterFilter() {
				@Override
				public Meter.Id map(Meter.Id id) {
					// 统一路由指标标签
					if (id.getName().startsWith("http.server.requests")) {
						return id.withTag(Tag.of("uri", getNormalizedUri(id)));
					}
					return id;
				}

				private String getNormalizedUri(Meter.Id id) {
					// 根据实际路由逻辑返回统一 URI
					return id.getTag("uri"); // 或自定义映射逻辑
				}
			});
		} else if (registry instanceof JmxMeterRegistry) {
			// JMX 特定配置（如果需要）
			JmxMeterRegistry jmxRegistry = (JmxMeterRegistry) registry;
			// 可以添加JMX特定配置，如果有需要的话
			System.out.println("JMX registry detected, no special configuration needed");
		} else if (registry != null) {
			// 其他类型注册表的通用配置
			registry.config().meterFilter(new MeterFilter() {
				@Override
				public Meter.Id map(Meter.Id id) {
					// 通用的标签处理
					if (id.getName().startsWith("http.server.requests")) {
						return id.withTag(Tag.of("uri", id.getTag("uri")));
					}
					return id;
				}
			});
		} else {
			// 没有找到注册表
			System.err.println("Warning: No metrics registry found");
		}
	}
}
