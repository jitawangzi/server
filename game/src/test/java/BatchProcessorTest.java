import java.io.IOException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import cn.game.core.base.ServerContext;
import cn.game.core.process.BatchProcessorUtil;
import cn.game.core.process.ProcessingConfig;
import cn.game.core.process.ProcessingMode;
import cn.game.core.process.processor.DataProcessor;
import cn.game.core.process.provider.DataProvider;
import cn.game.core.task.BatchProcessResult;
import cn.game.games.cache.entity.Friend;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.net.data.mapper.FriendMapper;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.protocol.generated.helper.ManagerHelper;
import cn.game.util.Config;
import cn.game.util.ServerType;
import cn.game.util.SpringApolloLoader;
import cn.game.util.SpringContextLoader;

public class BatchProcessorTest {


	public static void main(String[] args) throws Exception {
		initEnv();
		PlayerDataMapper playerDataMapper = SpringContextLoader.getContext().getBean(PlayerDataMapper.class);
		FriendMapper friendMapper = SpringContextLoader.getContext().getBean(FriendMapper.class);
//		List<PlayerData> batch = mapper.selectAll();
//		System.err.println(batch.size());

		ManagerHelper.init();
		
		
		BiFunction<Object[], Integer, List<Friend>> queryFunction = (cursors, batchSize) -> {
			return friendMapper.getBatchCursor((Long) cursors[0], (Long) cursors[1], batchSize);
		} ; 
		Function<Friend, Object[]> extractor = friend -> {
			return (Object[]) friend.primaryKey();
		} ; 
		
		DataProvider<Friend> cursorProvider = BatchProcessorUtil.createMultipleCursorProvider(queryFunction, extractor, 2, 0L, 0L);
		DataProcessor<Friend> processor = BatchProcessorUtil.createProcessor(r -> {
			System.err.println(r);
		});


		// 测试查询玩家
		BiFunction<Long, Integer, List<PlayerData>> queryFunctionPlayer = (cursor, batchSize) -> {
			return playerDataMapper.getBatchCursor(cursor, batchSize);
		};

		DataProvider<PlayerData> cursorProviderPlayer = BatchProcessorUtil.createSingleCursorProvider(queryFunctionPlayer,
				PlayerData::getPlayerId, 100, 0L);

		DataProcessor<PlayerData> processorPlayer = BatchProcessorUtil.createProcessor(r -> {
			System.err.println(r);
		});

		ProcessingConfig config = ProcessingConfig.defaultConfig().setMode(ProcessingMode.SEQUENTIAL).setContinueOnError(true);
		BatchProcessResult result = processor.process(cursorProvider, config);
		System.out.println(result);

		result = processorPlayer.process(cursorProviderPlayer, config);
		System.out.println(result);

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
