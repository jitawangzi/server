import java.io.IOException;

import org.springframework.transaction.annotation.Transactional;

import cn.game.core.base.ServerContext;
import cn.game.util.Config;
import cn.game.util.ServerType;
import cn.game.util.SpringApolloLoader;
import cn.game.util.SpringContextLoader;
import cn.game.util.ZkHelper;

/**    
 * 一些GameServer的测试代码
 * 2024年11月7日 14:59:37
 * @author SYQ
 */
public class GGameTest {


	public static void main(String[] args) throws Exception {
		initEnv();
//		GGameTest test = new GGameTest();
		GGameTest test = SpringContextLoader.getContext().getBean(GGameTest.class);
		test.runTest();
	}

	@Transactional(readOnly = true)
	private void runTest() {
	}

	/** 
	 * 初始化GameServer运行环境
	 * @throws IOException
	 * @throws Exception
	 */
	private static void initEnv() throws IOException, Exception {
		String serverId = "SYQ";
		System.setProperty(ServerContext.SERVER_RUN_MODE, "test");

//		LoggerManager.init();
		Config.load();

//		RedisUtil.getInstance().init();
		ZkHelper.init();
		ServerContext.getInstance().init(ServerType.Game, serverId);
//		IdUtil.init();

		SpringApolloLoader springApolloLoader = new SpringApolloLoader();
		springApolloLoader.init();

//		initGameServerConfig();
//		initVerticle();
//		initRemoteInterface();
	}
}
