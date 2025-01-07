package cn.game.games.net.cross;

import cn.game.core.base.ServerContext;
import cn.game.core.net.rpc.CallType;
import cn.game.core.net.rpc.RpcClient;
import cn.game.core.net.rpc.RpcFactory;
import cn.game.core.util.IdUtil;
import cn.game.games.net.common.ServerHelper;
import cn.game.games.net.game.remote.GameServerInterface;
import cn.game.util.Config;
import cn.game.util.RedisUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringApolloLoader;
import cn.game.util.SpringContextLoader;
import cn.game.util.ZkHelper;
import cn.game.util.log.LoggerManager;
import cn.game.util.log.LoggerType;

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

		String serverId = ServerHelper.parseServerId(args, ServerType.Cross);
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

		LoggerType.Stdout.logger.info("跨服[{}]启动成功,耗时[{}]s", serverId, (System.currentTimeMillis() - start) / 1000);
	}

	public static void main(String args[]) {
		try {
			instance.start(args);
		} catch (Throwable e) {
			ServerContext.getInstance().handleStartFail(e);
		}
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
