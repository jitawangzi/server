import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.apache.ibatis.cursor.Cursor;
import org.springframework.transaction.annotation.Transactional;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.util.Config;
import cn.game.util.ServerType;
import cn.game.util.SpringApolloLoader;
import cn.game.util.SpringContextLoader;
import cn.game.util.ZkHelper;
import io.vertx.core.Future;

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
		PlayerDataMapper mapper = SpringContextLoader.getContext().getBean(PlayerDataMapper.class);
		try (Cursor<PlayerData> cursor = mapper.streamAll()) {
			Iterator<PlayerData> iterator = cursor.iterator();
			while (iterator.hasNext()) {
				PlayerData next = iterator.next();
				System.out.println(next);

			}
			for (PlayerData playerData : cursor) {
				Future<Player> playerFromDb = PlayerHelper.loadPlayerFromDb(playerData);
				Player player = playerFromDb.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
				PlayerHelper.saveSimplePlayerToRedisSync(player);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
