package cn.game.core.base;

import java.lang.management.ManagementFactory;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;

import com.sun.tools.attach.VirtualMachine;

import cn.game.core.cache.CacheType;
import cn.game.util.Config;
import cn.game.util.LockUtil;
import cn.game.util.MailUtil;
import cn.game.util.ServerType;
import cn.game.util.log.LoggerType;
import cn.game.util.reflect.ClassHelper;

public class ServerContext {
	private static final ServerContext instance = new ServerContext();
	private static final String serverKey = "server.run.mode";
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

	public void init(ServerType serverType, String serverId) throws Exception {
		if (StringUtils.isEmpty(serverId)) {
			throw new IllegalArgumentException("serverId can not be null");
		}
		this.serverId = serverId;
		this.serverType = serverType;
		setRunMode();
		checkServerId(serverId);
		initHotUpdate();
	}

	public RunMode getRunMode() {
		return runMode;
	}

	private void setRunMode() {
		String mode = System.getProperty(serverKey);
		if (mode == null) {
			mode = System.getenv(serverKey);
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
			lock = LockUtil.tryLockNoExpiredNoWaitSync(CacheType.SERVER_LOCK.key(serverId));
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
			System.err.println("发送邮件失败," + e1.getMessage());
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
}
