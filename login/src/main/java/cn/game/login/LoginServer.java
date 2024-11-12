package cn.game.login;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.framework.apollo.ConfigService;

import cn.game.core.base.ActiveServerListManager;
import cn.game.core.base.ServerContext;
import cn.game.core.base.ServerListManager;
import cn.game.core.cache.CacheType;
import cn.game.core.net.remote.RemoteGameServerInterface;
import cn.game.core.net.rpc.CallType;
import cn.game.core.net.rpc.RpcClient;
import cn.game.core.net.rpc.RpcFactory;
import cn.game.core.net.rpc.vertx.VertxRPCService;
import cn.game.core.net.rpc.vertx.VertxRpcClient;
import cn.game.core.net.vertx.MsgConsumerVerticle;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.IdUtil;
import cn.game.login.mapper.UserMapper;
import cn.game.login.net.clientpacket.vertx.gm.IpWhitelistManger;
import cn.game.login.net.clientpacket.vertx.gm.NoticeManger;
import cn.game.login.net.clientpacket.vertx.wechat.IOSPayOrderProcessor;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.util.Config;
import cn.game.util.MailUtil;
import cn.game.util.RedisUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringApolloLoader;
import cn.game.util.SpringContextLoader;
import cn.game.util.ThreadUncaughtExceptionHandler;
import cn.game.util.ZkHelper;
import cn.game.util.log.LoggerManager;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;

/**
 * @Description
 * 2017年4月13日 下午7:21:32
 * @author SYQ
 */
public class LoginServer {
	static {
		try {
			LoggerManager.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static final Logger log = LoggerFactory.getLogger(LoginServer.class);

	private static LoginServer instance = new LoginServer();

	public static LoginServer getInstance() {
		return instance;
	}
	private LoginServer() {
	}

	private String serverId;
	private RpcClient rpcClient;

	public void start(String[] args) throws Exception {

		long start = System.currentTimeMillis();
		log.info("正在启动登录服...");
		Config.load();
		ZkHelper.init();
		RedisUtil.getInstance().init();
		com.ctrip.framework.apollo.Config config = ConfigService.getAppConfig(); // config instance is singleton for
																					// each namespace and is never null
		int vertHttpPort = config.getIntProperty("vertx.http.port", 0);
		serverId = config.getProperty("login.server.id", "");
		ServerContext.getInstance().init(ServerType.Login, serverId);
		IdUtil.init();
//		LogbackConfig.init(config.getBooleanProperty("initLogback", false),
//				config.getProperty("logbackFile", "config/logback-loginServer.xml"));

		// 加载配置文件
		// initManager() ;
//		SpringContextLoader.main(args);
		SpringApolloLoader springApolloLoader = new SpringApolloLoader();
		springApolloLoader.init();

		VxHolder.init();
//		RedisUtil.main(new String[] { Config.vertxRedisUrl });
//		RedisUtil.setRedisUrl(Config.vertxRedisUrl);
//		VxHolder.deployVerticleSync(new RedisUtil());
		// 部署发布rest服务
		RestServer.setPort(vertHttpPort);
		DeploymentOptions options = new DeploymentOptions();
		options.setInstances(VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE);
		VxHolder.deployVerticleSync(RestServer.class, options);
		VxHolder.deployVerticleSync(SpringContextLoader.getContext().getBean(VertxRPCService.class));
		VxHolder.deployVerticleSync(SpringContextLoader.getContext().getBean(MsgConsumerVerticle.class));

		rpcClient = new VertxRpcClient();
		VxHolder.deployVerticleSync((VertxRpcClient) rpcClient);

		ServerListManager.getInstance().start();
		ActiveServerListManager.getInstance().start(ServerType.Game);
		GlobalConst.instance().load();

		//白名单管理 初始化
		IpWhitelistManger.getInstance().init();
		//公告管理初始化
        NoticeManger.getInstance().init();

		IOSPayOrderProcessor.startRefreshAccessTokenTask();

		initPlayerMaxId();
		System.gc();

		long freeMem = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()
				+ Runtime.getRuntime().freeMemory()) / 1048576;
		long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		log.info("LoginServer Started, free memory " + freeMem + " Mb of " + totalMem + " Mb");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutdown();
			}
		});

		log.info("登录服启动成功。耗时[{}]s", (System.currentTimeMillis() - start) / 1000);
	}

	public static void main(String[] args) {
		// 设置异常处理类
		Thread.setDefaultUncaughtExceptionHandler(new ThreadUncaughtExceptionHandler());
		// java.util.logging.Logger rootLogger =
		// LogManager.getLogManager().getLogger("");
		// Handler[] handlers = rootLogger.getHandlers();
		// for (Handler handler : handlers)
		// {
		// rootLogger.removeHandler(handler);
		// }
		// SLF4JBridgeHandler.install();
		try {
			LoginServer.getInstance().start(args);

		} catch (Throwable e) {
			try {
				MailUtil.reportException("Login服务器【 " + instance.serverId + " 】启动失败",
						ExceptionUtils.getFullStackTrace(e));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			log.error("登录服启动失败", e);
			System.exit(1);
		}
	}

	public void initPlayerMaxId() {
		String key = CacheType.Player_MAX_ID.key();
		Integer maxPlayerId = RedisUtil.get(key);
		if (maxPlayerId == null) {
			UserMapper userMapper = SpringContextLoader.getContext().getBean(UserMapper.class);
			Long selectMaxId = userMapper.selectMaxId();
			if (selectMaxId == null) {
				int[] createUID = GlobalConst.CreateUID;
				selectMaxId = (long) (createUID[0] + createUID[1]);
			} else {
				selectMaxId += 1;
			}
			RedisUtil.getRedis().getBucket(key).compareAndSet(null, selectMaxId.intValue());
		}
	}

	public void shutdown() {
		log.info("Login Server Shutdown...");
		ServerContext.getInstance().shutdown();
		SpringContextLoader.getContext().close();
		log.info("Login Server Shutdown success...");
	}

	/**
	 * 获取逻辑服远程调用接口
	 * @param serverId 逻辑服id,如果不是指定某个id的服务器,则传null
	 * @return
	 */
	public RemoteGameServerInterface getRemoteGameServerInterface(CallType callType, String serverId) {
		return RpcFactory.getImpl(RemoteGameServerInterface.class, rpcClient, callType, serverId, ServerType.Game);
	}

}
