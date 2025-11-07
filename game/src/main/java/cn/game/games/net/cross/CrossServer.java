package cn.game.games.net.cross;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;

import cn.game.core.base.ActiveServerListManager;
import cn.game.core.base.ServerContext;
import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.cache.id.IdCache;
import cn.game.core.net.process.Processor;
import cn.game.core.net.rpc.CallType;
import cn.game.core.net.rpc.RpcFactory;
import cn.game.core.net.rpc.vertx.VertxRPCService;
import cn.game.core.net.rpc.vertx.VertxRpcClient;
import cn.game.core.net.vertx.BusinessLogicVerticle;
import cn.game.core.net.vertx.MsgConsumerVerticle;
import cn.game.core.net.vertx.VxContextRegistry;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.AsyncUtils;
import cn.game.core.util.IdUtil;
import cn.game.games.net.common.module.activity.CrossActivityService;
import cn.game.games.net.cross.data.CrossServerDataLoader;
import cn.game.games.net.cross.remote.CrossServerInterface;
import cn.game.games.net.cross.guild.GuildManager;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.init.GameIdManagerInitializer;
import cn.game.games.net.game.remote.GameServerInterface;
import cn.game.protocol.generated.helper.ManagerHelper;
import cn.game.util.Config;
import cn.game.util.GameUtil;
import cn.game.util.KryoUtils;
import cn.game.util.RedisUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringApolloLoader;
import cn.game.util.SpringContextLoader;
import cn.game.util.ZkHelper;
import cn.game.util.file.WatchServiceManager;
import cn.game.util.log.Log4j2ApolloLoader;
import cn.game.util.log.LoggerType;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.VertxOptions;

public class CrossServer {

//	private static Logger					log	= LoggerFactory.getLogger(CrossServer.class);

	/** 唯一实例 */
	private static CrossServer instance = new CrossServer();

	private CrossServer() {
	};
	public static CrossServer getInstance()
	{
		return instance;
	}
	
	public void start(String args[]) throws Exception {
		long start = System.currentTimeMillis();

		String serverId = GameUtil.parseServerId(args, ServerType.Cross);
		Log4j2ApolloLoader.getInstance().init();
		LoggerType.Stdout.logger.debug(System.getProperty("java.class.path"));
		LoggerType.Stdout.logger.info("启动跨服。。");
		RedisUtil.init();
		ZkHelper.init(); 

		IdUtil.init();
		ActiveServerListManager.getInstance().start(ServerType.Cross);
		ServerContext.getInstance().init(serverId, ServerType.Cross);
		// init with apollo config
		Config.load();
		SpringApolloLoader springApolloLoader = new SpringApolloLoader();
		springApolloLoader.init();

		VxHolder.init();

		initVerticle();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutdown();
			}
		});
		// 初始化业务数据
		CrossActivityService crossActivityService = new CrossActivityService();
//		crossActivityService.init();

		GameIdManagerInitializer.initialize();
		
		initLeaderTask();

		new Thread(WatchServiceManager.getInstance().setWatchDirs("xml", "config"), "WatchServiceManager").start();
		ManagerHelper.init();
		//初始化公会
		GuildManager.getInstance().init();
		LoggerType.Stdout.logger.info("跨服[{}]启动成功,耗时[{}]s", serverId, (System.currentTimeMillis() - start) / 1000);
		System.err.println("Cross Server startup complete");

	}


	public static void main(String args[]) {
		try {
			instance.start(args);
		} catch (Throwable e) {
			ServerContext.getInstance().handleStartFail(e);
		}
	}

	private void shutdown() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		LoggerType.Stdout.logger.info("CrossServer shutdown start");

		IdCache.clearAllCurrentServerId();
		GuildManager.getInstance().saveAllGuildData(true);

		// 关闭系统服务
		SpringContextLoader.getContext().close();
		ServerContext.getInstance().shutdown();
		AsyncUtils.await(VxHolder.vertx.close(), 300, TimeUnit.SECONDS); // 等待vertx关闭完成
		
		stopWatch.stop();
		LoggerType.Stdout.logger.info("CrossServer shutdown complete, elapsed time: {} ms", stopWatch.getTime());
		LogManager.shutdown(); // 关闭log4j2日志

	}

	private void initVerticle() throws Exception {

		VxHolder.deployVerticleSync((VertxRpcClient) ServerContext.getInstance().getRpcClient());

		int numVerticles = VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE;
		VxContextRegistry.getInstance().init(numVerticles);
		for (int i = 0; i < numVerticles; i++) {
			BusinessLogicVerticle verticle = new BusinessLogicVerticle(i);
			VxHolder.deployVerticleSync(verticle);
		}
		String serverId = ServerContext.getInstance().getServerId();
		ServerType serverType = ServerContext.getInstance().getServerType();
		Processor processor = SpringContextLoader.getContext().getBean(Processor.class);
		
		DeploymentOptions options = new DeploymentOptions().setInstances(1);
		options.setThreadingModel(ThreadingModel.VIRTUAL_THREAD);
		
		VxHolder.deployVerticleSync(new MsgConsumerVerticle(serverId, serverType, processor),options);

		VertxRPCService verticle = new VertxRPCService(null, serverId, serverType, processor);
		VxHolder.deployVerticleSync(verticle,options);

	}

	private void initLeaderTask() {
		if (!ServerContext.getInstance().isLeader()) {
			return;
		}
		loadCrossDistributedObject();
	}

	private void loadCrossDistributedObject() {

		CrossServerDataLoader bean = SpringContextLoader.getContext().getBean(CrossServerDataLoader.class);
		bean.load();
//		bean.reload();
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

	public CrossServerInterface getCrossServerInterface() {
		return RpcFactory.getImpl(CrossServerInterface.class, ServerContext.getInstance().getRpcClient(), CallType.LoadBalancer, null,
				ServerType.Cross);
	}

	public CrossServerInterface getCrossServerInterface(long targetId) {
		return RpcFactory.getImpl(CrossServerInterface.class, ServerContext.getInstance().getRpcClient(), CallType.LoadBalancer, null,
				ServerType.Cross, targetId);
	}

	public CrossServerInterface getCrossServerInterface(String serverId) {
		return getCrossServerInterface(serverId, 0);
	}

	public CrossServerInterface getCrossServerInterface(String serverId, long targetId) {
		return RpcFactory.getImpl(CrossServerInterface.class, ServerContext.getInstance().getRpcClient(), CallType.PointToPoint, serverId,
				ServerType.Cross, targetId);
	}

	/** 
	 * 获取所有跨服远程调用接口，一般是请求所有服务器并需要返回值的情况下使用
	 * @return
	 */
	public List<CrossServerInterface> getAllCrossServerInterface() {
		return ServerHelper.getAllServerInterface(ServerType.Cross, CrossServerInterface.class); 
	}
	/**
	 * 获取处理某类型对象的跨服远程调用接口
	 * @param DistributedObjectType 什么类型的对象
	 * @param targetId  对象的唯一id
	 * @return
	 */
	public CrossServerInterface getCrossServerInterface(DistributedObjectType objectType, long targetId) {

		String serverId = IdCache.getManager(objectType).getServerId(targetId);
		if (StringUtils.isEmpty(serverId) || serverId.equals(ServerContext.getInstance().getServerId())) {
			// 对象不在线，或者在当前服务器，直接由当前服务器处理
			return (CrossServerInterface) SpringContextLoader.getContext().getBean("crossRemote");
		}
		// 其他服务器在线，通过远程调用
		return RpcFactory.getImpl(CrossServerInterface.class, ServerContext.getInstance().getRpcClient(), CallType.PointToPoint, serverId,
				ServerType.Cross, targetId);

	}

	/** 
	 * 给初始化对象分配服务器
	 * @param objectType
	 * @param targetId
	 * @return
	 */
	public CrossServerInterface getCrossServerInterfaceForInit(DistributedObjectType objectType, long targetId) {

		String serverId = IdCache.getManager(objectType).selectServerId(targetId);
		if (!StringUtils.isEmpty(serverId)) {
			// 该对象可能已经初始化过了,或者redis中没有正常释放id数据
			LoggerType.Stdout.logger.warn("对象[{}]id[{}]已经初始化过了", objectType, targetId);
			return null;
		}
		return RpcFactory.getImpl(CrossServerInterface.class, ServerContext.getInstance().getRpcClient(), CallType.LoadBalancer, null,
				ServerType.Cross);
	}

}
