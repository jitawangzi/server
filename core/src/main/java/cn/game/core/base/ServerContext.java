package cn.game.core.base;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;

import cn.game.core.cache.CacheType;
import cn.game.util.LockUtil;
import cn.game.util.ServerType;

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
}
