package cn.game.games.net.cross;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.core.net.rpc.RpcClient;
import cn.game.core.net.rpc.RpcFactory;
import cn.game.core.util.IdUtil;
import cn.game.games.net.game.remote.GameRemoteServerInterface;
import cn.game.util.Config;
import cn.game.util.MailUtil;
import cn.game.util.RedissonUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringApolloLoader;
import cn.game.util.SpringContextLoader;
import cn.game.util.ZkHelper;

public class CrossServer {

	private static Logger					log	= LoggerFactory.getLogger(CrossServer.class);

	/** 唯一实例 */
	private static CrossServer instance = new CrossServer();
	private ConcurrentMap<String, GameRemoteServerInterface> gameServerInterfaces = new ConcurrentHashMap<String, GameRemoteServerInterface>();

	private CrossServer() {
	};
	public static CrossServer getInstance()
	{
		return instance;
	}
	
	public void start(String args[]) throws Exception {

		long start = System.currentTimeMillis();
		RedissonUtil.getInstance().init();
		ZkHelper.init();

		String serverId = parseServerId(args);
		ServerContext.getInstance().init(ServerType.Cross, serverId);
		IdUtil.init();

		// init with apollo config
		Config.load();
		SpringApolloLoader springApolloLoader = new SpringApolloLoader();
		springApolloLoader.init();

		log.info("中心服[{}]启动成功,耗时[{}]s", serverId, (System.currentTimeMillis() - start) / 1000);
	}
	public static void main(String args[]) {
		try {
			instance.start(args);
		} catch (Throwable e) {
			e.printStackTrace();
			try {
				MailUtil.reportException("Cross服务器【 " + " 】启动失败", ExceptionUtils.getFullStackTrace(
						e));
			} catch (Throwable e1) {
				e1.printStackTrace();
			}
			System.exit(1);
		}
	}

	/**
	 * @Description 获取逻辑服远程调用接口,同步的
	 * @param serverId
	 *            逻辑服id
	 * @return
	 */
	public GameRemoteServerInterface getGameServer(String serverId) {
		GameRemoteServerInterface gameCrossServerInterface = gameServerInterfaces.get(serverId);
		if (gameCrossServerInterface == null) {
			RpcClient crossRpcClient = (RpcClient) SpringContextLoader.getContext().getBean("crossRpcClient");
			gameCrossServerInterface = RpcFactory.getImpl(GameRemoteServerInterface.class, crossRpcClient, true, serverId);
			gameServerInterfaces.put(serverId, gameCrossServerInterface);
		}
		return gameCrossServerInterface;
	}

	private String parseServerId(String[] args) {
		String serverKey = "cross.serever.id";
		String serverId = null;
		if (args.length == 0) {
			serverId = System.getProperty(serverKey);
			if (serverId == null) {
				serverId = System.getenv(serverKey);
			}
		} else {
			serverId = args[0];
		}
		if (serverId == null) {
			throw new IllegalArgumentException("没有设置 serverId");
		}
		return serverId;
	}

}
