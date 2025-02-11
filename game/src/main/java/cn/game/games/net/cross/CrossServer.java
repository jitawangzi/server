package cn.game.games.net.cross;

import cn.game.core.base.ServerContext;
import cn.game.core.net.process.Processor;
import cn.game.core.net.rpc.CallType;
import cn.game.core.net.rpc.RpcClient;
import cn.game.core.net.rpc.RpcFactory;
import cn.game.core.net.rpc.vertx.VertxRPCService;
import cn.game.core.net.rpc.vertx.VertxRpcClient;
import cn.game.core.net.vertx.BusinessLogicVerticle;
import cn.game.core.net.vertx.MsgConsumerVerticle;
import cn.game.core.net.vertx.VxContextRegistry;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.IdUtil;
import cn.game.games.net.cross.activity.CrossActivityService;
import cn.game.games.net.game.remote.GameServerInterface;
import cn.game.util.Config;
import cn.game.util.GameUtil;
import cn.game.util.RedisUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringApolloLoader;
import cn.game.util.SpringContextLoader;
import cn.game.util.ZkHelper;
import cn.game.util.log.LoggerManager;
import cn.game.util.log.LoggerType;
import io.vertx.core.VertxOptions;

public class CrossServer {

//	private static Logger					log	= LoggerFactory.getLogger(CrossServer.class);

	/** 唯一实例 */
	private static CrossServer instance = new CrossServer();

	private RpcClient rpcClient;

	private CrossServer() {
	};
	public static CrossServer getInstance()
	{
		return instance;
	}
	
	public void start(String args[]) throws Exception {
		long start = System.currentTimeMillis();

		String serverId = GameUtil.parseServerId(args, ServerType.Cross);
		LoggerManager.init();
		LoggerType.Stdout.logger.debug(System.getProperty("java.class.path"));
		LoggerType.Stdout.logger.info("启动跨服。。");

		RedisUtil.getInstance().init();
		ZkHelper.init();
		IdUtil.init();
		ServerContext.getInstance().init(serverId, ServerType.Cross);
		// init with apollo config
		Config.load();
		SpringApolloLoader springApolloLoader = new SpringApolloLoader();
		springApolloLoader.init();

		VxHolder.init();

		initVerticle();

		// 初始化业务数据
		CrossActivityService crossActivityService = new CrossActivityService();
		crossActivityService.init();

		LoggerType.Stdout.logger.info("跨服[{}]启动成功,耗时[{}]s", serverId, (System.currentTimeMillis() - start) / 1000);
	}

	public static void main(String args[]) {
		try {
			instance.start(args);
		} catch (Throwable e) {
			ServerContext.getInstance().handleStartFail(e);
		}
	}

	private void initVerticle() throws Exception {

		rpcClient = new VertxRpcClient();
		VxHolder.deployVerticleSync((VertxRpcClient) rpcClient);

		VxHolder.deployVerticleSync(new VertxRpcClient());
		
		int numVerticles = VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE;
		VxContextRegistry.getInstance().init(numVerticles);
		for (int i = 0; i < numVerticles; i++) {
			BusinessLogicVerticle verticle = new BusinessLogicVerticle(i);
			VxHolder.deployVerticleSync(verticle);
		}
		String serverId = ServerContext.getInstance().getServerId();
		ServerType serverType = ServerContext.getInstance().getServerType();
		Processor processor = SpringContextLoader.getContext().getBean(Processor.class);
		VxHolder.deployVerticleSync(new MsgConsumerVerticle(serverId, serverType, processor));

		Object remoteInterface = SpringContextLoader.getContext().getBean("crossRemote");
		VertxRPCService verticle = new VertxRPCService(remoteInterface, serverId, serverType, processor);
		VxHolder.deployVerticleSync(verticle);

	}

	/**
	 * 获取逻辑服远程调用接口
	 * @param serverId 逻辑服id,如果不是指定某个id的服务器,则传null
	 * @return
	 */
	public GameServerInterface getGameServerInterface(CallType callType, String serverId) {
		RpcClient crossRpcClient = (RpcClient) SpringContextLoader.getContext().getBean("crossRpcClient");
		return RpcFactory.getImpl(GameServerInterface.class, crossRpcClient, callType, serverId, ServerType.Game);
	}

}
