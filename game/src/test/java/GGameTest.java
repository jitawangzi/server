import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Function;

import cn.game.core.base.ServerContext;
import cn.game.core.net.process.Processor;
import cn.game.core.net.vertx.VxContextRegistry;
import cn.game.core.process.BatchProcessorUtil;
import cn.game.core.process.ProcessingConfig;
import cn.game.core.process.ProcessingMode;
import cn.game.core.process.processor.DataProcessor;
import cn.game.core.process.provider.DataProvider;
import cn.game.core.task.BatchProcessResult;
import cn.game.core.util.AsyncUtils;
import cn.game.core.util.VertxFutureConverter;
import cn.game.games.cache.entity.Friend;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.net.data.mapper.FriendMapper;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.protocol.generated.helper.ManagerHelper;
import cn.game.util.Config;
import cn.game.util.ServerType;
import cn.game.util.SpringApolloLoader;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Context;

/**    
 * 一些GameServer的测试代码
 * 2024年11月7日 14:59:37
 * @author SYQ
 */
public class GGameTest {


	public static void main(String[] args) throws Exception {
		initEnv();
		PlayerDataMapper playerDataMapper = SpringContextLoader.getContext().getBean(PlayerDataMapper.class);
		FriendMapper friendMapper = SpringContextLoader.getContext().getBean(FriendMapper.class);
//		List<PlayerData> batch = mapper.selectAll();
//		System.err.println(batch.size());

		ManagerHelper.init();
		
//		batchProcess(playerDataMapper, friendMapper);

		extracted();

	}

	private static void extracted() {
		Callable<String> supplier = () -> {
			return "hello sync";
		};
		Callable<Future<String>> supplierJdkFuture = () -> {
			return CompletableFuture.supplyAsync(() -> {
				return "hello CompletableFuture";
			});
		};
		Callable<CompletionStage<String>> supplierCompletionStage = () -> {
			return CompletableFuture.supplyAsync(() -> {
				return "hello CompletionStage";
			});
		};

		Processor processor = ServerContext.getInstance().getProcessor();
		Context context = VxContextRegistry.getInstance().getContext(1);
		context.runOnContext(r44 -> {
			io.vertx.core.Future<String> f1 = processor.process(1, supplier, null);
			io.vertx.core.Future<String> f2 = processor.process(2, supplierJdkFuture, VertxFutureConverter.jdkFutureConverter());
			io.vertx.core.Future<String> f3 = processor.process(3, supplierCompletionStage,
					VertxFutureConverter.completionStageConverter());

			f1.onComplete(r -> {
				System.err.println(Thread.currentThread().getName() + r.result());
			});
			f2.onComplete(r -> {
				System.err.println(Thread.currentThread().getName() + r.result());
			});
			f3.onComplete(r -> {
				System.err.println(Thread.currentThread().getName() + r.result());
			});
		});

		io.vertx.core.Future<String> f4 = AsyncUtils.runOnContextAuto(context, supplier);
		io.vertx.core.Future<String> f5 = AsyncUtils.runOnContextAuto(context, true, supplierJdkFuture);
		io.vertx.core.Future<String> f6 = AsyncUtils.runOnContextAuto(context, true, supplierCompletionStage);
		f4.onComplete(r -> {
			System.err.println("f4 " + Thread.currentThread().getName() + r.result());
		});
		f5.onComplete(r -> {
			System.err.println("f5 " + Thread.currentThread().getName() + r.result());
		});
		f6.onComplete(r -> {
			System.err.println("f6 " + Thread.currentThread().getName() + r.result());
		});
	}


	private static void batchProcess(PlayerDataMapper playerDataMapper, FriendMapper friendMapper) {
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

		BiFunction<Long, Integer, List<PlayerData>> queryFunctionPlayer = (cursor, batchSize) -> {
			return playerDataMapper.getBatchCursor(cursor, batchSize);
		};
		Function<PlayerData, Long> extractorPlayer = r -> {
			return r.getPlayerId();
		};

		DataProvider<PlayerData> cursorProviderPlayer = BatchProcessorUtil.createSingleCursorProvider(queryFunctionPlayer,
				extractorPlayer, 100, 0L);
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
