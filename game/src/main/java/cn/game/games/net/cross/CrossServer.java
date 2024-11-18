package cn.game.games.net.cross;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.core.net.rpc.CallType;
import cn.game.core.net.rpc.RpcClient;
import cn.game.core.net.rpc.RpcFactory;
import cn.game.core.util.IdUtil;
import cn.game.games.net.game.remote.GameServerInterface;
import cn.game.util.Config;
import cn.game.util.MailUtil;
import cn.game.util.RedisUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringApolloLoader;
import cn.game.util.SpringContextLoader;
import cn.game.util.ZkHelper;

public class CrossServer {

	private static Logger					log	= LoggerFactory.getLogger(CrossServer.class);

	/** 唯一实例 */
	private static CrossServer instance = new CrossServer();

	private CrossServer() {
	};
	public static CrossServer getInstance()
	{
		return instance;
	}
	
	public void start(String args[]) throws Exception {

		ServerContext.getInstance().init();
		long start = System.currentTimeMillis();
		RedisUtil.getInstance().init();
		ZkHelper.init();

		String serverId = parseServerId(args);
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
				MailUtil.reportException("Cross服务器【 " + " 】启动失败", ExceptionUtils.getFullStackTrace(e));
			} catch (Throwable e1) {
				e1.printStackTrace();
			}
			System.exit(1);
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
