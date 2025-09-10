package cn.game.core.base;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.leader.Participant;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.framework.apollo.ConfigService;
import com.sun.tools.attach.VirtualMachine;

import cn.game.core.base.VirtualServerRegistry.VirtualServerView;
import cn.game.core.cache.CacheType;
import cn.game.core.event.AbstractEvent;
import cn.game.core.event.EventBus;
import cn.game.core.event.EventDispatcher;
import cn.game.core.event.EventHandler;
import cn.game.core.event.EventProcessor;
import cn.game.core.event.EventRegistry;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.core.net.process.Processor;
import cn.game.core.net.rpc.RpcClient;
import cn.game.core.net.rpc.vertx.VertxRpcClient;
import cn.game.core.zookeeper.ZkBackedCache;
import cn.game.core.zookeeper.ZkBackedCacheFactory;
import cn.game.core.zookeeper.ZkCacheRegistry;
import cn.game.core.zookeeper.ZkCacheType;
import cn.game.core.zookeeper.server.ValidServerService;
import cn.game.util.Config;
import cn.game.util.LockUtil;
import cn.game.util.MailUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringContextLoader;
import cn.game.util.ZkHelper;
import cn.game.util.log.LoggerType;
import cn.game.util.reflect.ClassHelper;

public class ServerContext {
	private static final Logger log = LoggerFactory.getLogger(ServerContext.class);

	private static final ServerContext instance = new ServerContext();
	public static final String SERVER_RUN_MODE = "server.run.mode";
	private static final String LEADER_PATH = "/server/leader/";
	private boolean pressureDev = Boolean.getBoolean("pressureDev");
	private RunMode runMode = RunMode.PRODUCTION;
	private RLock lock;
	private String serverId;
	private ServerType serverType = ServerType.Game; // 默认是游戏服务器类型;
	/** 是否是主节点 */
	private volatile boolean isLeader;
	private LeaderLatch leaderLatch;

	private Processor processor;

	private RpcClient rpcClient = new VertxRpcClient();

	private EventBus<?, ? extends AbstractEvent<?>> eventBus;

	private ZkCacheRegistry<ZkCacheType> zkCacheRegistry;

	private ValidServerService validGameService;

	private ServerContext() {
	};

	public static ServerContext getInstance() {
		return instance;
	}

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

	public RpcClient getRpcClient() {
		return rpcClient;
	}

	public void setRpcClient(RpcClient rpcClient) {
		this.rpcClient = rpcClient;
	}

	public Processor getProcessor() {
		if (processor == null) {
			processor = SpringContextLoader.getContext().getBean(Processor.class);
		}
		return processor;
	}

	/** 
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		initHotUpdate();
		startLeaderTask();
		waitOtherNodeStartup();
		initZkCacheRegistry();
		initValidServerService();
	}

	private void initValidServerService() {
		validGameService = new ValidServerService(zkCacheRegistry.get(ZkCacheType.VIRTUAL_SERVER_LIST), null);
		validGameService.addOpenListener(s -> {
			fireEvent(ServerEventTypeEnum.VirtualServerOpen, s.ID);
		});
		validGameService.initFromSnapshot();
	}

	private void initZkCacheRegistry() throws Exception {
		ZkBackedCache<String, VirtualServerView> virtualServerCache = ZkBackedCacheFactory.createVirtualServerCache();
		this.zkCacheRegistry = new ZkCacheRegistry<>(ZkCacheType.class);
		zkCacheRegistry.register(ZkCacheType.VIRTUAL_SERVER_LIST, virtualServerCache);
		zkCacheRegistry.startAllAndWarmup();
	}

	/** 
	 * 直接初始化，一般测试时使用，待优化。 
	 * @param serverId
	 * @param serverType
	 * @throws Exception
	 */
	public void init(String serverId, ServerType serverType) throws Exception {
		initBase(serverId, serverType);
		init();
	}

	public void initBase(String serverId, ServerType serverType) throws Exception {
		setServerId(serverId);
		setServerType(serverType);
		setRunMode();
		checkServerId(serverId);
	}

	public RunMode getRunMode() {
		return runMode;
	}

	public void setRunMode() {
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
		if (leaderLatch != null) {
			try {
				leaderLatch.close();
			} catch (Exception e) {
				e.printStackTrace();
				log.error("leaderLatch close error", e);
			}
		}
		if (zkCacheRegistry != null) {
			zkCacheRegistry.close();
		}
		if (processor != null) {
			processor.close();
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
		if (Config.hotUpdate) {
			return;
		}
		String className = ManagementFactory.getRuntimeMXBean().getName();
		String pid = className.split("@")[0];
		Thread attachThread = new Thread(() -> {
			VirtualMachine vm = null;
			try {
				String jarName = "hotupdate-1.0.jar";
				String agentPath = ClassHelper.findJarPath(jarName);
				if (agentPath == null) {
//					throw new RuntimeException("Agent JAR not found : " + jarName);
					return;
				}
				vm = VirtualMachine.attach(pid);
				vm.loadAgent(agentPath);
				LoggerType.Stdout.logger.info("hotUpdate agent loaded, pid: " + pid + ", agentPath: " + agentPath);
			} catch (Exception e) {
				throw new RuntimeException("hotUpdate agent start failed", e);
			} finally {
				if (vm != null) {
					try {
						vm.detach();
					} catch (IOException e) {
						LoggerType.Stdout.logger.error("Failed to detach from VM", e);
					}
				}
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
	 * 是否用一张表存储玩家所有数据-----暂时用不到了
	 * @return
	 */
	public boolean isSinglePlayerTable() {
//		return false ; 
		return ConfigService.getAppConfig().getBooleanProperty("player_db_single_table", true);
	}

	private void startLeaderTask() throws Exception {
		String latchPath = LEADER_PATH + serverType.name().toLowerCase();
		log.info("Starting leader election for node: {}, path: {}", serverId, latchPath);

		leaderLatch = new LeaderLatch(ZkHelper.curator, latchPath, serverId);

		leaderLatch.addListener(new LeaderLatchListener() {
			@Override
			public void isLeader() {
				isLeader = true;
				log.info("I am leader: {}", serverId);
			}

			@Override
			public void notLeader() {
				isLeader = false;
				log.info("I am not leader: {}", serverId);
			}
		});
		try {
			leaderLatch.start();
//			leaderLatch.await(30,TimeUnit.SECONDS); 
			log.info("LeaderLatch started successfully");
		} catch (Exception e) {
			log.error("Failed to start LeaderLatch", e);
			throw e;
		}
		log.info("Leader elected: {}", leaderLatch.getLeader());
	}

	private void waitOtherNodeStartup() throws Exception {
		if (Config.ExpectedNodeCount <= 1 || !isLeader) {
			return;
		}
		Set<String> serverSet = ActiveServerListManager.getInstance().getServerSet(serverType);
		if (serverSet.size() < Config.ExpectedNodeCount) {
			int waitCount = 0;
			while (serverSet.size() < Config.ExpectedNodeCount) {
				log.info("等待[{}]节点数达到预期数量[{}],当前节点{}", serverType, Config.ExpectedNodeCount, serverSet);
				Thread.sleep(3000);
				serverSet = ActiveServerListManager.getInstance().getServerSet(serverType);
				waitCount++;
				if (waitCount > 100) {
					throw new RuntimeException(
							serverType.name() + "预期节点数未达到，当前节点数：" + serverSet.size() + "，预期节点数：" + Config.ExpectedNodeCount);
				}
			}
			log.info("节点数达到预期值: {},当前节点数量: {}", Config.ExpectedNodeCount, serverSet.size());
		}
	}

	/** 
	 * 同步获取当前leader的id
	 * 尽量使用异步方法。 
	 * @return
	 * @throws Exception
	 */
	public String getCurrentLeader() throws Exception {
		Participant leader = leaderLatch.getLeader();
		return leader.getId();
	}

	/** 
	 * 异步获取当前leader的id
	 * @return
	 * @throws Exception
	 */
	public CompletionStage<String> getCurrentLeaderAsync() {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return getCurrentLeader();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

	}

	/** 
	 * 当前节点是否是主节点
	 * @return
	 */
	public boolean isLeader() {
		return isLeader;
	}

	public EventBus<?, ? extends AbstractEvent<?>> getEventBus() {
		return eventBus;
	}

	public void setEventBus(EventBus<?, ? extends AbstractEvent<?>> eventBus) {
		this.eventBus = eventBus;
	}

	public ZkCacheRegistry<ZkCacheType> getZkCacheRegistry() {
		return zkCacheRegistry;
	}

	public ValidServerService getValidGameService() {
		return validGameService;
	}

	@SuppressWarnings("unchecked")
	public <T, E extends AbstractEvent<T>> void registerEventHandler(EventHandler<T, E> handler) {
		// 强制类型转换
		EventRegistry<T, E> registration = (EventRegistry<T, E>) eventBus;
		registration.register(handler);
	}

	@SuppressWarnings("unchecked")
	public <T, E extends AbstractEvent<T>> void registerEventHandler(T eventType, EventProcessor<E> processor) {
		// 强制类型转换
		EventRegistry<T, E> registration = (EventRegistry<T, E>) eventBus;
		registration.register(eventType, processor);
	}

	@SuppressWarnings("unchecked")
	public <T, E extends AbstractEvent<T>> void unregisterEventHandler(EventHandler<T, E> handler) {
		// 强制类型转换
		EventRegistry<T, E> registration = (EventRegistry<T, E>) eventBus;
		registration.unregister(handler);
	}

	public <T, E extends AbstractEvent<T>> void fireEvent(T eventType, Object... params) {
		@SuppressWarnings("unchecked")
		EventDispatcher<T, E> dispatcher = (EventDispatcher<T, E>) eventBus;
		dispatcher.dispatch(eventType, params);
	}
}
