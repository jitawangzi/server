package cn.game.games.net.game;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.RequestCallback;

import com.ctrip.framework.apollo.ConfigService;
import com.google.common.io.Files;
import com.google.protobuf.Message;
import com.sun.tools.attach.VirtualMachine;

import cn.game.core.base.ServerContext;
import cn.game.core.net.mq.RocketMQRpcClient;
import cn.game.core.net.remote.LoginGameServerInterface;
import cn.game.core.net.rpc.RpcClient;
import cn.game.core.net.rpc.RpcFactory;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.net.vertx.rpc.VertxRpcClient;
import cn.game.core.task.TaskManager;
import cn.game.core.util.IdUtil;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.GameServerStatus;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.core.vertx.WebSocketVerticle;
import cn.game.games.net.cross.remote.CrossRemoteServerInterface;
import cn.game.games.net.data.remote.DataGameServerInterface;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.manager.PressureTestManager;
import cn.game.games.net.game.remote.GameRemoteServerInterface;
import cn.game.games.util.BIHelper;
import cn.game.games.util.KeywordFilter;
import cn.game.protocol.generated.helper.ManagerHelper;
import cn.game.protocol.protobuf.ServerMsg.GameStatusPublish_7d000017;
import cn.game.util.Config;
import cn.game.util.JsonUtil;
import cn.game.util.RedissonUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringApolloLoader;
import cn.game.util.SpringContextLoader;
import cn.game.util.ThreadUncaughtExceptionHandler;
import cn.game.util.TreeWordFilter;
import cn.game.util.ZkHelper;
import cn.game.util.file.WatchServiceManager;
import cn.game.util.log.CommonLogger;
import cn.game.util.log.LoggerManager;
import cn.game.util.log.SystemLogger;
import cn.game.util.quartz.QuartzInitializer;
import io.vertx.core.DeploymentOptions;

/**
 * vertx重构通讯
 * 2021年4月12日 上午10:17:27
 * @author SYQ
 */
public class GameServer implements GameServerMBean {

//	static {
//		// 在这里初始化log，为了下面定义的log实例，能够正常被初始化。
//		try {
//			LoggerManager.init();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	private static final String gameServerKey = "game.serever.id";

//	private final Logger log = LoggerFactory.getLogger(GameServer.class);
	private Properties initialProp;
	private QuartzInitializer quartzInitializer;
//	private String serverId;
	private static final GameServer instance = new GameServer();

	@Deprecated
	private LoginGameServerInterface loginGameServerInterface;
	private CrossRemoteServerInterface crossGameServerInterface;
	private CrossRemoteServerInterface crossGameServerInterfaceSync;
	private ConcurrentMap<String, GameRemoteServerInterface> gameServerInterfacesSync = new ConcurrentHashMap<String, GameRemoteServerInterface>();
	private ConcurrentMap<String, GameRemoteServerInterface> gameServerInterfacesAsync = new ConcurrentHashMap<String, GameRemoteServerInterface>();

	private RpcClient rpcClient;
	private String[] serverIds = new String[ServerType.values().length];
	private String wsVerticle;

	private GameServer() {
	};

	public static GameServer getInstance() {
		return instance;
	}

	public static void main(String args[]) {
		try {
//			ServerContext.parseGameServerId(args);
//			LoggerManager.init();
//			System.err.println(System.getProperty("log4j2.level"));
//			CommonLogger.info("启动逻辑服。。");
//			instance.log.info("启动逻辑服。。");

			instance.start(args);
		} catch (Throwable e) {
//			try {
//				MailUtil.reportException("Game服务器【 " + instance.serverId + " 】启动失败", ExceptionUtils.getFullStackTrace(
//						e));
//			} catch (Exception e1) {
//				e1.printStackTrace();
//			}
			e.printStackTrace();
			System.exit(1);
		}

	}

	public void start(String[] args) throws Exception {
		String serverId = parseGameServerId(args);
		LoggerManager.init();
//		System.err.println(System.getProperty("log4j2.level"));
		CommonLogger.info("启动逻辑服。。");
		Thread.setDefaultUncaughtExceptionHandler(new ThreadUncaughtExceptionHandler());

//		instance.log.info("启动逻辑服。。");

		long start = System.currentTimeMillis();
		RedissonUtil.getInstance().init();
		ZkHelper.init();
		ServerContext.getInstance().init(ServerType.Game, serverId);
		IdUtil.init();

//		util.SpringContextLoader.main(args);
		// init with apollo config
		Config.load();
		SpringApolloLoader springApolloLoader = new SpringApolloLoader();
		springApolloLoader.init();
		initQuartz();
		initGameServerConfig();
		initVerticle();
		initRemoteInterface();
//		DAO.listenPauseUpdateDb();
		TreeWordFilter.init("filterWord.txt");

//		this.maxPlayerId = initialProp.getIntProperty("player.max.id", 0);
//		this.minPlayerId = initialProp.getIntProperty("player.min.id", 0);
//		int port = initialProp.getIntProperty("netty.port", 0);

		new Thread(WatchServiceManager.getInstance().setWatchDirs("xml", "config"), "WatchServiceManager").start();
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		mBeanServer.registerMBean(instance,
				new ObjectName("net.game:type=GameServer,name=GameServer_" + ServerContext.getInstance().getServerId()));
		initHotUpdate();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutdown();
			}
		});
		initScheduleTask();
		// ******************** 业务逻辑启动 **************************

		ManagerHelper.init();

		ActivityStateManager.getInstance().start();
//		ActivityStateManager.getInstance().initGlobal();
//		UnionManager.getInstance().init();
//		ChatManager.getInstance().init();
		PlayerManager.getInstance().init();
		ClassManager.getInstance().init();
		PressureTestManager.getInstance().init();
		BIHelper.start();
		checkPlayerJsonStruct();
		KeywordFilter.initializeFromFile();
//		Long playerId = (Long) dataGameServerInterfaceSync.exec(PlayerExtMapper.class,
//				"selectMaxId", null);
//		this.dbMaxPlayerId = new AtomicLong(playerId == null ? minPlayerId : playerId);
//		log.info("max player id :" + dbMaxPlayerId);
//		log.info("逻辑服[{}]启动成功,耗时[{}]s", serverId, (System.currentTimeMillis() - start) / 1000);
		CommonLogger.info(String.format("逻辑服[%s]启动成功,耗时[%s]s", serverId, (System.currentTimeMillis() - start) / 1000));

		// 记录bi
//		RocketMQRpcClient producer = new RocketMQRpcClient("192.168.1.67:9876", "SYQ_GROUP");
//		producer.start();
//		testUpdateBatch();
	}

	/** 
	 * 检查 player 模块数据 结构是否有变化
	 * @throws Exception
	 */
	private void checkPlayerJsonStruct() throws Exception {
		File file = new File("player.json");
		if (file.exists()) {
//			String json = FileUtils.readFileToString(file, Charset.defaultCharset());
			String json = Files.readFirstLine(file, Charset.defaultCharset());

			Player player = null;
			try {
				player = JsonUtil.parseObject(json, Player.class);
			} catch (Exception e) {
				throw new RuntimeException("Player结构有变化，json反序列化失败，修正数据兼容后重试", e); // 反序列化失败，
			}
			Files.write(JsonUtil.toJsonString(player), file, Charset.defaultCharset());
//			FileUtils.writeStringToFile(file, JsonUtil.toJsonString(player), Charset.defaultCharset());
		} else {
			Player player = new Player();
			player.initModule(null);
			Files.write(JsonUtil.toJsonString(player), file, Charset.defaultCharset());
//			FileUtils.writeStringToFile(file, JsonUtil.toJsonString(player), Charset.defaultCharset());
		}
	}
	private void initGameServerConfig() throws Exception {
		GameServerStatus.getInstance().start().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
		if (GameServerStatus.getInstance().getServerInfo() == null) {
			throw new IllegalArgumentException(
					"GameServerInfo is null，cant find serverId from zookeeper ,serverId "
							+ ServerContext.getInstance().getServerId());
		}
	}

	private String parseGameServerId(String[] args) {
		String serverId = null;
		if (args.length == 0) {
			serverId = System.getProperty(gameServerKey);
			if (serverId == null) {
				serverId = System.getenv(gameServerKey);
			}
		} else {
			serverId = args[0];
		}
		if (serverId == null) {
			throw new IllegalArgumentException("没有设置 gameServerId");
		}
		System.setProperty(gameServerKey, serverId);
		return serverId;
	}

	private void initHotUpdate() {
		if (Config.hotUpdate) {
			String className = ManagementFactory.getRuntimeMXBean().getName();
			String pid = className.split("@")[0];
			Thread attachThread = new Thread(() -> {
				try {
					VirtualMachine vm = VirtualMachine.attach(pid);
					// 这个路径是相对于被热更的服务的，也就是这个pid的服务，也可以使用绝对路径。
					vm.loadAgent(Config.agentJar);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}, "agent attach");
			attachThread.setDaemon(true);
			attachThread.start();
		}
	}

	private void initScheduleTask() {
		TaskManager.getInstance().scheduleGeneralAtFixedRate(() -> {
			PlayerManager.getInstance().setPlayerServerId();
		}, 180000, 180000);
		TaskManager.getInstance().scheduleGeneralAtFixedRate(() -> {
			VxHolder.broadcastRemoteServer(ServerType.Login,
					GameStatusPublish_7d000017.newBuilder().setServerId(ServerContext.getInstance().getServerId())
							.setOnlinePlayerCount(PlayerManager.getInstance().getOnlineCount()).build());
		}, 0, 60000);
	}

	private void initRemoteInterface() throws Exception {
		String serverId = ServerContext.getInstance().getServerId();
		com.ctrip.framework.apollo.Config initialProp = ConfigService.getAppConfig();
		// serverId = initialProp.getProperty("game.serever.id", "");
		String loginServerId = initialProp.getProperty("login.serever.id", "");
		String crossServerId = initialProp.getProperty("cross.serever.id", "");
		// String dataServerId = initialProp.getProperty("data.serever.id", "");
		String dataServerId = "data_" + serverId;

		serverIds[ServerType.Login.ordinal()] = loginServerId;
		serverIds[ServerType.Cross.ordinal()] = crossServerId;
		serverIds[ServerType.Data.ordinal()] = dataServerId;

		for (String string : serverIds) {
			if (string == null)
				continue;
			if (string.equals(""))
				throw new IllegalArgumentException("serverId can not be null");
		}

		this.loginGameServerInterface = RpcFactory.getImpl(LoginGameServerInterface.class, rpcClient, false,
				loginServerId);
		this.crossGameServerInterface = RpcFactory.getImpl(CrossRemoteServerInterface.class, rpcClient, false,
				crossServerId);
		this.crossGameServerInterfaceSync = RpcFactory.getImpl(CrossRemoteServerInterface.class, rpcClient, true,
				crossServerId);
	}

	private void initVerticle() throws Exception {
		rpcClient = new VertxRpcClient();
		VxHolder.deployVerticleSync((VertxRpcClient) rpcClient);

		DeploymentOptions options = new DeploymentOptions().setInstances(Runtime.getRuntime().availableProcessors() * 2);
		wsVerticle = VxHolder.deployVerticleSync(WebSocketVerticle.class, options);
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
		long start = System.currentTimeMillis();
//		log.info("Game Server starts to shutdown ...");
		CommonLogger.info("Game Server starts to shutdown ...");

		// 停止超时维护线程
		// ClientMaintaining clientMaintaining =
		// ClientManage.getInstance().getClientMaintaining();
		// if (clientMaintaining != null)
		// {
		// clientMaintaining.setShutdown(true);
		// }
		// 关闭websocket服务
//		WebSocketServer socketServer = SpringContextLoader.getContext().getBean(WebSocketServer.class);
//		socketServer.shutdown();

		// 通知玩家退出
		GameClientManager.getInstance().logoutAll();
		try {
			// 关闭websocket
			VxHolder.vertx.undeploy(wsVerticle).toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
		} catch (Exception e) {
//			log.error("", e);
			CommonLogger.error(e);
		}
		TaskManager.getInstance().shutdown();
		try {

			// 同步存储所有玩家的数据
			Config.remoteCallTimeOut = Config.shutdownWaitTime;
//			setDataServerSyncDefault();
			GameClientManager.getInstance().storeAllPlayers();
			SpringContextLoader.getContext().close();
			quartzInitializer.destroyed();

			VxHolder.vertx.close().toCompletionStage().toCompletableFuture().get(300, TimeUnit.SECONDS);

//			log.info("Game Server  safe  shutdown, use  time {} ms ", System.currentTimeMillis() - start);
			CommonLogger.warn("Game Server  safe  shutdown, use  time {} ms ", System.currentTimeMillis() - start);
			String.format("Game Server  safe  shutdown, use  time %d ms ", System.currentTimeMillis() - start);
			// 安全关闭log
//			LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
//			context.stop();

		} catch (Throwable e) {
//			log.error("Game Server Shutdown err ", e);
			SystemLogger.error("Game Server Shutdown err ", e);
			e.printStackTrace();
		}

	}

//	public long nextPlayerId() {
//		if (this.dbMaxPlayerId.get() >= this.maxPlayerId) {
//			return 0;
//		}
//		return this.dbMaxPlayerId.incrementAndGet();
//	}

	public LoginGameServerInterface getLoginGameServerInterface() {
		return loginGameServerInterface;
	}


	public CrossRemoteServerInterface getCrossGameServerInterface() {
		return crossGameServerInterface;
	}


	public CrossRemoteServerInterface getCrossGameServerInterfaceSync() {
		return crossGameServerInterfaceSync;
	}

	public DataGameServerInterface getDataGameCallback(Consumer<?> callBackTask) {
		return RpcFactory.getImplCallback(rpcClient, DataGameServerInterface.class, callBackTask,
				getServerId(ServerType.Data));
	}

//	public void setDataServerSyncDefault() {
//		this.dataGameServerInterface = dataGameServerInterfaceSync;
//	}

	/**
	 * 是否是本地服务器
	 * @param serverId
	 * @return
	 */
	@Deprecated
	public boolean isLocalServer(String serverId) {
		return StringUtils.isEmpty(serverId) || ServerContext.getInstance().getServerId().equals(serverId);
	}

	public String getServerId(ServerType serverType) {
		return serverIds[serverType.ordinal()];
	}

	public RpcClient getRpcClient() {
		return rpcClient;
	}

	/**
	 * 获取逻辑服远程调用接口,异步的
	 * @param serverId
	 *            逻辑服id
	 * @return
	 */
	public GameRemoteServerInterface getGameServerRemoteAsync(String serverId) {
		GameRemoteServerInterface gameCrossServerInterface = gameServerInterfacesAsync.get(serverId);
		if (gameCrossServerInterface == null) {
			gameCrossServerInterface = RpcFactory.getImpl(GameRemoteServerInterface.class, rpcClient, false, serverId);
			GameRemoteServerInterface put = gameServerInterfacesAsync.put(serverId, gameCrossServerInterface);
			if (put != null) {
				gameCrossServerInterface = put;
			}
		}
		return gameCrossServerInterface;
	}

	/**
	 * 获取逻辑服远程调用接口,同步的
	 * @param serverId
	 *            逻辑服id
	 * @return
	 */
	public GameRemoteServerInterface getGameServerRemoteSync(String serverId) {
		GameRemoteServerInterface gameCrossServerInterface = gameServerInterfacesSync.get(serverId);
		if (gameCrossServerInterface == null) {
			gameCrossServerInterface = RpcFactory.getImpl(GameRemoteServerInterface.class, rpcClient, true, serverId);
			GameRemoteServerInterface put = gameServerInterfacesSync.put(serverId, gameCrossServerInterface);
			if (put != null) {
				gameCrossServerInterface = put;
			}
		}
		return gameCrossServerInterface;
	}

	public void requestDataServer(Message message, RequestCallback callback) {
		RocketMQRpcClient.request(getServerId(ServerType.Data), message, callback);
	}
	/** 
	 * 是否用一张表存储玩家所有数据
	 * @return
	 */
	public boolean isSinglePlayerTable() {
//		return false ; 
		return  ConfigService.getAppConfig().getBooleanProperty("player_db_single_table", false);
	}

}
