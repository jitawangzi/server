import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.game.core.base.ServerContext;
import cn.game.core.net.process.Processor;
import cn.game.core.net.remote.RemoteLoginServerInterface;
import cn.game.core.net.rpc.CallType;
import cn.game.core.net.rpc.RpcFactory;
import cn.game.core.net.rpc.vertx.VertxRPCService;
import cn.game.core.net.rpc.vertx.VertxRpcClient;
import cn.game.core.net.vertx.BusinessLogicVerticle;
import cn.game.core.net.vertx.MsgConsumerVerticle;
import cn.game.core.net.vertx.VxContextRegistry;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.core.GameServerStatus;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.core.vertx.WebSocketVerticle;
import cn.game.protocol.generated.helper.ManagerHelper;
import cn.game.util.Config;
import cn.game.util.ServerType;
import cn.game.util.SpringApolloLoader;
import cn.game.util.SpringContextLoader;
import cn.game.util.ZkHelper;
import cn.game.util.log.LoggerType;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

/**    
 * GameServer的测试代码
 * 2025年6月12日 18:00:09
 * @author SYQ
 */
public class GGameVertxTest {


	public static void main(String[] args) throws Exception {
		initEnv();
		ManagerHelper.init();

		testcode();

	}


	/** 
	 * 初始化GameServer运行环境
	 * @throws IOException
	 * @throws Exception
	 */
	private static void initEnv() throws IOException, Exception {
		String serverId = "SYQ";
		String[] args = new String[] { serverId };
		System.setProperty(ServerContext.SERVER_RUN_MODE, "test");
		ServerType serverType = ServerType.Game;

		ServerContext.getInstance().initBase(serverId, serverType);
		ServerContext.getInstance().setEventBus(ServerEventBus.getInstance());

//		LoggerManager.init();
		Config.load();

//		RedisUtil.getInstance().init();
		ZkHelper.init();
		VxHolder.init();

//		IdUtil.init();

		SpringApolloLoader springApolloLoader = new SpringApolloLoader();
		springApolloLoader.init();

		initGameServerConfig();
		initVerticle();
	}

	private static void initGameServerConfig() throws Exception {
		GameServerStatus.getInstance().start().toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
		if (GameServerStatus.getInstance().getServerInfo() == null) {
			throw new IllegalArgumentException(
					"GameServerInfo is null，cant find serverId from zookeeper ,serverId " + ServerContext.getInstance().getServerId());
		}
	}

	private static void initVerticle() throws Exception {
		VxHolder.deployVerticleSync((VertxRpcClient) ServerContext.getInstance().getRpcClient());

		int numVerticles = VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE;
		VxContextRegistry.getInstance().init(numVerticles);
		for (int i = 0; i < numVerticles; i++) {
			BusinessLogicVerticle verticle = new BusinessLogicVerticle(i);
			VxHolder.deployVerticleSync(verticle);
		}
		DeploymentOptions options = new DeploymentOptions().setInstances(numVerticles);
		VxHolder.deployVerticleSync(WebSocketVerticle.class, options);

		String serverId = ServerContext.getInstance().getServerId();
		ServerType serverType = ServerContext.getInstance().getServerType();
		Processor processor = SpringContextLoader.getContext().getBean(Processor.class);
		VxHolder.deployVerticleSync(new MsgConsumerVerticle(serverId, serverType, processor));
		VertxRPCService verticle = new VertxRPCService(null, serverId, serverType, processor);
		VxHolder.deployVerticleSync(verticle);

	}

	private static void testcode() {
		String serverId = "login_test";
		RemoteLoginServerInterface remoteLoginServerInterface = RpcFactory.getImpl(RemoteLoginServerInterface.class,
				ServerContext.getInstance().getRpcClient(), CallType.PointToPoint, serverId, ServerType.Login);
		Future<Long> future = remoteLoginServerInterface.getUid2("testSessionId");
		future.onComplete(r -> {
			if (r.succeeded()) {
				LoggerType.Stdout.logger.info("Game Server login server uid:{}", r.result());
			} else {
				LoggerType.Stdout.logger.error("Game Server login server getUid failed", r.cause());
			}
		});
	}
}
