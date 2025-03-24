import java.io.IOException;

import cn.game.core.base.ServerContext;
import cn.game.protocol.generated.helper.ManagerHelper;
import cn.game.util.Config;
import cn.game.util.ServerType;
import cn.game.util.SpringApolloLoader;
import cn.game.util.ZkHelper;

public class GGameVertxTest {


	public static void main(String[] args) throws Exception {
		initEnv();

		ManagerHelper.init();

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

//		LoggerManager.init();
		Config.load();

//		RedisUtil.getInstance().init();
		ZkHelper.init();
		ServerContext.getInstance().setServerId(serverId);
		ServerContext.getInstance().setServerType(ServerType.Game);

//		IdUtil.init();

		SpringApolloLoader springApolloLoader = new SpringApolloLoader();
		springApolloLoader.init();

//		initGameServerConfig();
//		initVerticle();
//		initRemoteInterface();
	}
}
