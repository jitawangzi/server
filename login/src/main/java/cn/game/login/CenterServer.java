package cn.game.login;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.core.net.vertx.VxHolder;
import cn.game.login.net.clientpacket.vertx.wechat.IOSPayOrderProcessor;
import cn.game.util.Config;
import cn.game.util.MailUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringContextLoader;
import cn.game.util.ThreadUncaughtExceptionHandler;
import cn.game.util.ZkHelper;
import cn.game.util.log.LoggerManager;
import io.vertx.core.DeploymentOptions;

public class CenterServer {

	static {
		try {
			LoggerManager.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static final Logger log = LoggerFactory.getLogger(LoginServer.class);

	private static CenterServer instance = new CenterServer();

	public static CenterServer getInstance() {
		return instance;
	}

	private CenterServer() {
	}

	private String serverId;

	public void start(String[] args) throws Exception {
		serverId = parseServerId(args, ServerType.Center);

		long start = System.currentTimeMillis();
		log.info("正在启动中心服...");

		Config.load();
		ZkHelper.init();
//		RedisUtil.getInstance().init();
		ServerContext.getInstance().setServerId(serverId);
		ServerContext.getInstance().setServerType(ServerType.Center);

//		IdUtil.init();
//		SpringApolloLoader springApolloLoader = new SpringApolloLoader();
//		springApolloLoader.init();

		VxHolder.init();
		DeploymentOptions options = new DeploymentOptions();
		options.setInstances(4);
		VxHolder.deployVerticleSync(CenterRestServer.class, options);

		IOSPayOrderProcessor.startRefreshAccessTokenTask();

		System.gc();

		long freeMem = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory())
				/ 1048576;
		long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		log.info("LoginServer Started, free memory " + freeMem + " Mb of " + totalMem + " Mb");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutdown();
			}
		});

		log.info("中心服启动成功。耗时[{}]s", (System.currentTimeMillis() - start) / 1000);
		System.err.println("Center Server startup complete");
	}

	public static void main(String[] args) {
		// 设置异常处理类
		Thread.setDefaultUncaughtExceptionHandler(new ThreadUncaughtExceptionHandler());
		try {
			CenterServer.getInstance().start(args);
		} catch (Throwable e) {
			try {
				MailUtil.reportException("Center服务器【 " + instance.serverId + " 】启动失败", ExceptionUtils.getFullStackTrace(e));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			log.error("中心服启动失败", e);
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void shutdown() {
		log.info("Center Server Shutdown...");
		ServerContext.getInstance().shutdown();
		SpringContextLoader.getContext().close();
		log.info("Center Server Shutdown success...");
	}

	private String parseServerId(String[] args, ServerType serverType) {
		String serverId = null;
		String serverIdKey = serverType.getServerIdKey();
		if (args.length == 0) {
			serverId = System.getProperty(serverIdKey);
			if (serverId == null) {
				serverId = System.getenv(serverIdKey);
			}
		} else {
			serverId = args[0];
		}
		if (serverId == null) {
			throw new IllegalArgumentException("没有设置 serverId");
		}
		System.setProperty(serverIdKey, serverId);

		return serverId;
	}

}
