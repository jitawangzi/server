package cn.game.core.base;

import java.lang.management.ManagementFactory;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.attach.VirtualMachine;

import cn.game.core.cache.CacheType;
import cn.game.util.Config;
import cn.game.util.LockUtil;
import cn.game.util.MailUtil;
import cn.game.util.ServerType;
import cn.game.util.log.LoggerType;
import cn.game.util.reflect.ClassHelper;

public class ServerContext {
	private static final Logger log = LoggerFactory.getLogger(ServerContext.class);

	private static final ServerContext instance = new ServerContext();
	public static final String SERVER_RUN_MODE = "server.run.mode";
	private boolean pressureDev = Boolean.getBoolean("pressureDev");
	private RunMode runMode = RunMode.PRODUCTION;
	private RLock lock;

	private ServerContext() {
	};

	public static ServerContext getInstance() {
		return instance;
	}

	private String serverId;
	private ServerType serverType;

	
	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public ServerType getServerType() {
		return serverType;
	}

	public void setServerType(ServerType serverType) {
		this.serverType = serverType;
	}

	/** 
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		setRunMode();
		checkServerId(serverId);
		initHotUpdate();
	}

	/** 
	 * 直接初始化，一般测试时使用，待优化。 
	 * @param serverId
	 * @param serverType
	 * @throws Exception
	 */
	public void init(String serverId, ServerType serverType) throws Exception {
		setServerId(serverId);
		setServerType(serverType);
		init();
	}

	public RunMode getRunMode() {
		return runMode;
	}

	private void setRunMode() {
		String mode = System.getProperty(SERVER_RUN_MODE);
		if (mode == null) {
			mode = System.getenv(SERVER_RUN_MODE);
		}
		if (mode != null) {
			this.runMode = RunMode.valueOf(mode.toUpperCase());
		}

	}
	
	public boolean isPressureDev() {
		return pressureDev;
	}

	public void checkServerId(String serverId) {
		if (getRunMode().isProduction()) {
			lock = LockUtil.tryLockNoExpiredNoWaitSync(CacheType.SERVER_ID_LOCK.key(serverId));
			if (lock == null) {
				throw new RuntimeException(serverId + " Server启动失败，可能有其他服务器使用这个id了，或者这个id的服务器关闭和启动的间隔太短，可以等待30秒后在试");
			}
		}
	}

	public void shutdown() {
		if (lock != null) {
			lock.forceUnlock();
		}
	}

	/** 
	 * 服务器启动失败
	 * @param e
	 */
	public void handleStartFail(Throwable e) {
		try {
			MailUtil.reportException(serverType.name() + "服务器【 " + serverId + " 】启动失败", ExceptionUtils.getFullStackTrace(e));
		} catch (Exception e1) {
			log.error("发送邮件失败", e1);
		}
		e.printStackTrace();
		System.exit(1);
	}

	private void initHotUpdate() {
		if (!Config.hotUpdate) {
			return;
		}
		String className = ManagementFactory.getRuntimeMXBean().getName();
		String pid = className.split("@")[0];
		Thread attachThread = new Thread(() -> {
			try {
				String jarName = "hotupdate-1.0.jar";
				String agentPath = ClassHelper.findJarPath(jarName);
				if (agentPath == null) {
					throw new RuntimeException("Agent JAR not found : " + jarName);
				}
				VirtualMachine vm = VirtualMachine.attach(pid);
				vm.loadAgent(agentPath);
				LoggerType.Stdout.logger.info("hotUpdate agent loaded, pid: " + pid + ", agentPath: " + agentPath);
			} catch (Exception e) {
				throw new RuntimeException("hotUpdate agent start failed", e);
			}
		}, "CodeHotUpdateThread");

		// 设置未捕获异常处理器
		attachThread.setUncaughtExceptionHandler((t, e) -> {
			ServerContext.getInstance().handleStartFail(e);
		});
		attachThread.setDaemon(true);
		attachThread.start();

	}

	/** 
	 * 解析服务器唯一id
	 * @param args 服务器启动参数
	 * @param serverType 服务器类型
	 * @return
	 */
	public String parseServerId(String[] args, ServerType serverType) {
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

		this.serverId = serverId;
		this.serverType = serverType;

		return serverId;
	}
}
