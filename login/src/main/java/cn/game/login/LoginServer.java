package cn.game.login;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.framework.apollo.ConfigService;

import cn.game.core.base.ActiveServerListManager;
import cn.game.core.base.ServerContext;
import cn.game.core.base.ServerListManager;
import cn.game.core.net.vertx.MsgConsumerVerticle;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.net.vertx.rpc.VertxRPCService;
import cn.game.core.util.IdUtil;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.util.Config;
import cn.game.util.MailUtil;
import cn.game.util.RedisUtil;
import cn.game.util.RedissonUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringApolloLoader;
import cn.game.util.SpringContextLoader;
import cn.game.util.ThreadUncaughtExceptionHandler;
import cn.game.util.ZkHelper;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;

/**
 * @Description
 * @date 2017年4月13日 下午7:21:32
 * @author SYQ
 */
public class LoginServer {
	private static final Logger log = LoggerFactory.getLogger(LoginServer.class);

	private static LoginServer instance = new LoginServer();

	public static LoginServer getInstance() {
		return instance;
	}
	private LoginServer() {
	}

	private int serverId;

	public void start(String[] args) throws Exception {

		long start = System.currentTimeMillis();
		log.info("正在启动登录服...");
		Config.load();
		ZkHelper.init();
		RedissonUtil.getInstance().init();
		com.ctrip.framework.apollo.Config config = ConfigService.getAppConfig(); // config instance is singleton for
																					// each namespace and is never null
		int vertHttpPort = config.getIntProperty("vertx.http.port", 0);
		String serverId = config.getProperty("login.server.id", "");
		ServerContext.getInstance().init(ServerType.Login, serverId);
		IdUtil.init();
//		LogbackConfig.init(config.getBooleanProperty("initLogback", false),
//				config.getProperty("logbackFile", "config/logback-loginServer.xml"));

		// 加载配置文件
		// initManager() ;
//		SpringContextLoader.main(args);
		SpringApolloLoader springApolloLoader = new SpringApolloLoader();
		springApolloLoader.init();

		RedisUtil.main(new String[] { Config.vertxRedisUrl });

		VxHolder.init();
		RedisUtil.setRedisUrl(Config.vertxRedisUrl);
		VxHolder.deployVerticleSync(new RedisUtil());
		// 部署发布rest服务
		RestServer.setPort(vertHttpPort);
		DeploymentOptions options = new DeploymentOptions();
		options.setInstances(VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE);
		VxHolder.deployVerticleSync(RestServer.class, options);
		VxHolder.deployVerticleSync(SpringContextLoader.getContext().getBean(VertxRPCService.class));
		VxHolder.deployVerticleSync(SpringContextLoader.getContext().getBean(MsgConsumerVerticle.class));

		ServerListManager.getInstance().start();
		ActiveServerListManager.getInstance().start(ServerType.Game);
		GlobalConst.instance().load();

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

	public void shutdown() {
		log.info("Login Server Shutdown...");
		SpringContextLoader.getContext().close();
		log.info("Login Server Shutdown success...");
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

}
