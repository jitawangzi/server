package cn.game.games.net.game;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.RequestCallback;
import org.redisson.api.RKeys;
import org.redisson.api.RLock;

import com.ctrip.framework.apollo.ConfigService;
import com.google.common.io.Files;
import com.google.protobuf.Message;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.net.client.LogoutType;
import cn.game.core.net.mq.RocketMQRpcClient;
import cn.game.core.net.remote.RemoteLoginServerInterface;
import cn.game.core.net.rpc.CallType;
import cn.game.core.net.rpc.RpcClient;
import cn.game.core.net.rpc.RpcFactory;
import cn.game.core.net.rpc.vertx.VertxRpcClient;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.task.SchedulerService;
import cn.game.core.task.TaskManager;
import cn.game.core.util.IdUtil;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.GameServerStatus;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.core.push.PushService;
import cn.game.games.core.vertx.WebSocketVerticle;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
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
import cn.game.util.KeywordFilter;
import cn.game.util.LockUtil;
import cn.game.util.RedisUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringApolloLoader;
import cn.game.util.SpringContextLoader;
import cn.game.util.ThreadUncaughtExceptionHandler;
import cn.game.util.ZkHelper;
import cn.game.util.file.WatchServiceManager;
import cn.game.util.log.LoggerManager;
import cn.game.util.log.LoggerType;
import cn.game.util.quartz.QuartzInitializer;
import io.vertx.core.DeploymentOptions;

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
//      System.setProperty("user.dir", "D:\\Party\\server\\server\\game");

			instance.start(args);
		} catch (Throwable e) {
			ServerContext.getInstance().handleStartFail(e);
		}

	}

	public void start(String[] args) throws Exception {

		long start = System.currentTimeMillis();
		String serverId = GameUtil.parseServerId(args, ServerType.Game);
		LoggerManager.init();
		LoggerType.Stdout.logger.debug(System.getProperty("java.class.path"));
		LoggerType.Stdout.logger.info("启动逻辑服。。");
		Thread.setDefaultUncaughtExceptionHandler(new ThreadUncaughtExceptionHandler());
//		instance.log.info("启动逻辑服。。");
		Config.load();
		ZkHelper.init();
		RedisUtil.getInstance().init();
		IdUtil.init();

		ServerContext.getInstance().init(serverId, ServerType.Game);

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
		RankService.getInstance().initRewardTask();
		PushService.getInstance().init(PlayerHelper::sendProtocol);
		initSimplePlayers();
		kickClientsAfterChangeTime();

		MailHelper.initLoadGlobalMail();

		DataFixManager.getInstance().init();
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
	/** 
	 * 当修改时间测试某些和时间相关的功能时，如果时间往后调了超过一个小时，则自动踢出客户端
	 * 主要方便测试跨天的一些逻辑。 
	 */
	private void kickClientsAfterChangeTime() {
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
		File file = new File("player.json");
		if (file.exists()) {
//			String json = FileUtils.readFileToString(file, Charset.defaultCharset());
			String json = Files.readFirstLine(file, Charset.defaultCharset());

			Player player = null;
			try {
				player = JsonUtil.parseObjectWithType(json);
			} catch (Exception e) {
				throw new RuntimeException("Player结构有变化，json反序列化失败，修正数据兼容后重试", e); // 反序列化失败，
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
		GameServerStatus.getInstance().start().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
		if (GameServerStatus.getInstance().getServerInfo() == null) {
			throw new IllegalArgumentException(
					"GameServerInfo is null，cant find serverId from zookeeper ,serverId "
							+ ServerContext.getInstance().getServerId());
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
		LoggerType.Stdout.logger.info("Game Server starts to shutdown ...");
		// 通知玩家退出
		GameClientManager.getInstance().notifyLogoutAllClients();
		// 关闭websocket服务
//		WebSocketServer socketServer = SpringContextLoader.getContext().getBean(WebSocketServer.class);
//		socketServer.shutdown();
		/*		try {
					// 关闭websocket
					VxHolder.vertx.undeploy(wsVerticle).toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
				} catch (Exception e) {
					LoggerType.Stdout.logger.error("undeploy wsVerticle fail", e);
				}*/
		TaskManager.getInstance().shutdown();
		try {
			LoggerType.Stdout.logger.warn("storeAllPlayers on shutdown");
			// 同步存储所有玩家的数据
			Config.remoteCallTimeOut = Config.shutdownWaitTime;
//			setDataServerSyncDefault();
			GameClientManager.getInstance().storeAllPlayers();
			SpringContextLoader.getContext().close();
			quartzInitializer.destroyed();

			ServerContext.getInstance().shutdown();
			LoggerType.Stdout.logger.warn("close vertx on shutdown");
			VxHolder.vertx.close().toCompletionStage().toCompletableFuture().get(300, TimeUnit.SECONDS);

//			log.info("Game Server  safe  shutdown, use  time {} ms ", System.currentTimeMillis() - start);
			String shutdownSucess = String.format("Game Server  safe  shutdown, use  time %d ms ", System.currentTimeMillis() - start);
			LoggerType.Stdout.logger.warn(shutdownSucess);
			System.err.println(shutdownSucess);
			// 安全关闭log
//			LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
//			context.stop();

		} catch (Throwable e) {
//			log.error("Game Server Shutdown err ", e);
			e.printStackTrace();
			System.err.println("Game Server Shutdown err ");
			LoggerType.Stdout.logger.error("Game Server Shutdown err ", e);
		}

	}

//	public long nextPlayerId() {
//		if (this.dbMaxPlayerId.get() >= this.maxPlayerId) {
//			return 0;
//		}
//		return this.dbMaxPlayerId.incrementAndGet();
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
	 * 获取逻辑服远程调用接口
	 * @param serverId 逻辑服id,如果不是指定某个id的服务器,则传null
	 * @return
	 */
	public GameServerInterface getGameServerInterface(CallType callType, String serverId) {
		return RpcFactory.getImpl(GameServerInterface.class, rpcClient, callType, serverId, ServerType.Game);
	}

	/** 
	 * 获取登陆远程通讯接口，一般不需要指定具体的登陆服id，也就是不使用 PointToPoint 方式
	 * @param callType
	 * @return
	 */
	public RemoteLoginServerInterface getRemoteLoginServerInterface(CallType callType) {
		return RpcFactory.getImpl(RemoteLoginServerInterface.class, rpcClient, callType, null, ServerType.Login);

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
