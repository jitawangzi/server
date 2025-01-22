import java.io.IOException;
import java.util.List;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.protocol.generated.config.RankRewardConfig;
import cn.game.protocol.generated.helper.ManagerHelper;
import cn.game.protocol.generated.manager.RankRewardManager;
import cn.game.util.BinarySearchUtil;
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
		PlayerDataMapper mapper = SpringContextLoader.getContext().getBean(PlayerDataMapper.class);
		List<PlayerData> batch = mapper.selectAll();
		System.err.println(batch.size());

		ManagerHelper.init();

		List<RankRewardConfig> rewardList = RankRewardManager.instance().getTypeList(3);
		for (int i = 1; i < 11; i++) {

			RankRewardConfig rankStageConfig = BinarySearchUtil.findFirstGreaterThanOrEqual(rewardList, i, r -> r.RewardStage);

			System.out.println(rankStageConfig.ID);
		}
		System.out.println("end");

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
