package cn.game.games.net.game.gm;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import com.google.protobuf.Message;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.exception.LogicException;
import cn.game.core.execute.SaveMailboxFixture;
import cn.game.core.execute.TaskExecutorService;
import cn.game.core.net.process.IdVirtualThreadProcessor;
import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.core.net.socket.controller.DispatcherImpl;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.SimplePlayer;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.core.BasePlayerModule;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.GmMsg.GmPlayerRequest_77000021;
import cn.game.protocol.protobuf.GmMsg.GmPlayerResponse_77000022;
import cn.game.protocol.protobuf.GmMsg.GmPlayerTDLevelRequest_77000054;
import cn.game.protocol.protobuf.GmMsg.GmPlayerTDLevelResponse_77000055;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.CborSnappyUtil;
import cn.game.util.JsonUtil;

/**
 * RunMode安全矩阵与0x77协议表征单元测试
 *
 * @author shuyongqiang
 */
@Isolated
public class GmSecurityPolicyAnd0x77Test {

	@GmGroup(name = "secprobe", description = "安全策略探测分组")
	static class SecurityProbeGm {

		@GmAction(name = "devop", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.GLOBAL)
		public String devOp() {
			return "dev-ok";
		}

		@GmAction(name = "safeop", security = GmSecurityTier.OPS_SAFE, scope = GmScope.GLOBAL)
		public String safeOp() {
			return "safe-ok";
		}

		@GmAction(name = "repairop", security = GmSecurityTier.OPS_REPAIR, scope = GmScope.GLOBAL)
		public String repairOp() {
			return "repair-ok";
		}
	}

	static final class StoredPlayerRecord {
		final long playerId;
		final String name;
		final boolean isGm;
		final String modulesJson;

		StoredPlayerRecord(long playerId, String name, boolean isGm, String modulesJson) {
			this.playerId = playerId;
			this.name = name;
			this.isGm = isGm;
			this.modulesJson = modulesJson;
		}
	}

	/** 内存存储替身，用于模拟玩家状态完整序列化落盘与全新Player冷重载回读 */
	static class InMemoryPlayerStore {
		private final Map<Long, StoredPlayerRecord> rows = new HashMap<>();

		void save(Player player) {
			String modulesJson = JsonUtil.toJsonStringWithType(new HashMap<>(player.getModules()));
			rows.put(player.getPlayerId(), new StoredPlayerRecord(
					player.getPlayerId(),
					player.getData().getName(),
					player.getData().getIsGm(),
					modulesJson));
		}

		StoredPlayerRecord selectById(long playerId) {
			return rows.get(playerId);
		}

		StoredPlayerRecord getById(long playerId) {
			return rows.get(playerId);
		}

		StoredPlayerRecord findById(long playerId) {
			return rows.get(playerId);
		}

		Player reloadFresh(long playerId) {
			StoredPlayerRecord row = selectById(playerId);
			Assertions.assertNotNull(row, "存储记录不应为空");
			PlayerData freshData = new PlayerData();
			freshData.setPlayerId(row.playerId);
			freshData.setName(row.name);
			freshData.setIsGm(row.isGm);
			HashMap<String, BasePlayerModule> parsedModules = JsonUtil.parseObjectWithType(row.modulesJson);
			freshData.setModules(CborSnappyUtil.compress(parsedModules));
			return new Player(freshData);
		}
	}

	/** 捕获 0x77 响应与错误码的轻量客户端替身 */
	static class CapturingGameClient extends GameClient {
		final BlockingQueue<Message> responses = new LinkedBlockingQueue<>();
		final BlockingQueue<Integer> errorCodes = new LinkedBlockingQueue<>();

		CapturingGameClient(long playerId) {
			super(null);
			setPlayerId(playerId);
			this.state = GameClientState.IN_GAME;
		}

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public boolean needProcess(IProtocol<?> protocol) {
			return true;
		}

		@Override
		public void sendProtocol(Object msg) {
			if (msg instanceof Message message) {
				errorCodes.add(0);
				responses.add(message);
			}
		}

		@Override
		public void sendProtocol(Object msg, int errorCode) {
			if (msg instanceof Message message) {
				errorCodes.add(errorCode);
				responses.add(message);
			}
		}
	}

	@Test
	void verifyRunModeSecurityAnd0x77Characterization() {
		try {
		ClassManager.getInstance().init();
		String prevMode = System.getProperty(ServerContext.SERVER_RUN_MODE);
		Field runModeField = ServerContext.class.getDeclaredField("runMode");
		runModeField.setAccessible(true);
		Field procField = ServerContext.class.getDeclaredField("processor");
		procField.setAccessible(true);
		Processor prevProcessor = (Processor) procField.get(ServerContext.getInstance());

		TaskExecutorService executor = SaveMailboxFixture.create(16);
		IdVirtualThreadProcessor processor = new IdVirtualThreadProcessor(executor);
		procField.set(ServerContext.getInstance(), processor);

		long nonGmPlayerId = 9002001L;
		long gmPlayerId = 9002002L;
		PlayerData nonGmData = new PlayerData();
		nonGmData.setPlayerId(nonGmPlayerId);
		nonGmData.setName("NonGmPlayer");
		nonGmData.setIsGm(false);
		Player nonGmPlayer = new Player(nonGmData);

		PlayerData gmData = new PlayerData();
		gmData.setPlayerId(gmPlayerId);
		gmData.setName("GmPlayerB");
		gmData.setIsGm(true);
		Player gmPlayer = new Player(gmData);

		CapturingGameClient gmClient = new CapturingGameClient(gmPlayerId);
		gmPlayer.setGameClient(gmClient);

		PlayerManager.getInstance().initAdd(nonGmPlayer);
		PlayerManager.getInstance().initAdd(gmPlayer);

		GmHandler testGmHandler = new GmHandler() {
			@Override
			protected SimplePlayer searchPlayerForInfo(String name, long playerId) {
				Player target = PlayerManager.getInstance().getPlayer(playerId);
				if (target == null) {
					return null;
				}
				SimplePlayer sp = new SimplePlayer();
				sp.id = target.getPlayerId();
				sp.name = target.getData().getName();
				sp.level = target.getData().getLevel();
				sp.serverId = "test-game-1";
				return sp;
			}
		};
		Field dispField = BaseHandler.class.getDeclaredField("dispatcher");
		dispField.setAccessible(true);
		dispField.set(testGmHandler, new DispatcherImpl());
		testGmHandler.init();

		try {
			// RunMode 未初始化为 null 时必须 Fail-Closed 返回 PRODUCTION，且 PRODUCTION 下 null 玩家必须被拒绝
			runModeField.set(ServerContext.getInstance(), null);
			Assertions.assertEquals(RunMode.PRODUCTION, GmSecurityPolicy.currentRunMode());
			LogicException null0x77Ex = Assertions.assertThrows(LogicException.class,
					() -> GmSecurityPolicy.check0x77Access(null, RunMode.PRODUCTION));
			Assertions.assertEquals(ErrorMsgEnum.gm_not.getId(), null0x77Ex.getErrorCode());
			LogicException nullChatEx = Assertions.assertThrows(LogicException.class,
					() -> GmSecurityPolicy.checkChatGmAccess(null, RunMode.PRODUCTION, GmSecurityTier.OPS_SAFE));
			Assertions.assertEquals(ErrorMsgEnum.gm_not.getId(), nullChatEx.getErrorCode());

			GmDispatcher dispatcher = new GmDispatcher(processor);
			dispatcher.registerBean(new SecurityProbeGm());

			// DEV 下全部安全等级可用且 schema 包含 DEV_TEST_ONLY
			System.setProperty(ServerContext.SERVER_RUN_MODE, "DEV");
			ServerContext.getInstance().setRunMode();
			Assertions.assertTrue(dispatcher.dispatchAction("secprobe.devop", 0L, Map.of(), "t-dev", false).isOk());
			GmSchemaCatalog devCatalog = dispatcher.getSchemaCatalog(RunMode.DEV);
			Assertions.assertTrue(devCatalog.getActions().stream().anyMatch(a -> "secprobe.devop".equals(a.getId())));

			// PRODUCTION 下禁用 DEV_TEST_ONLY，gm.schema 仅保留 OPS_SAFE / OPS_REPAIR
			System.setProperty(ServerContext.SERVER_RUN_MODE, "PRODUCTION");
			ServerContext.getInstance().setRunMode();
			GmResult denied = dispatcher.dispatchAction("secprobe.devop", 0L, Map.of(), "t-prod", false);
			Assertions.assertFalse(denied.isOk());
			Assertions.assertEquals(ErrorMsgEnum.production_gm_not_allow.getId(), denied.getErrorCode());
			Assertions.assertTrue(dispatcher.dispatchAction("secprobe.safeop", 0L, Map.of(), "t-safe", false).isOk());

			GmSchemaCatalog prodCatalog = dispatcher.getSchemaCatalog(RunMode.PRODUCTION);
			Assertions.assertFalse(prodCatalog.getActions().stream().anyMatch(a -> a.getSecurity() == GmSecurityTier.DEV_TEST_ONLY));
			Assertions.assertTrue(prodCatalog.getActions().stream().anyMatch(a -> "secprobe.safeop".equals(a.getId())));

			Assertions.assertDoesNotThrow(() -> GmSecurityPolicy.isRequireAuth());

			// gm0x77 表征：PRODUCTION 下校验 isGm 及未登录空玩家拦截
			CapturingGameClient unknownClient = new CapturingGameClient(0L);
			LogicException unknownDenied = Assertions.assertThrows(LogicException.class,
					() -> testGmHandler.checkExt(unknownClient, null));
			Assertions.assertEquals(ErrorMsgEnum.gm_not.getId(), unknownDenied.getErrorCode());

			CapturingGameClient nonGmClient = new CapturingGameClient(nonGmPlayerId);
			LogicException gmDeniedEx = Assertions.assertThrows(LogicException.class,
					() -> testGmHandler.checkExt(nonGmClient, null));
			Assertions.assertEquals(ErrorMsgEnum.gm_not.getId(), gmDeniedEx.getErrorCode());
			Assertions.assertTrue(testGmHandler.checkExt(gmClient, null));

			// bypassesPriorQuery=true：在无前置查询时直接执行 MUTATE:GmPlayerTDLevelRequest_77000054 并校验异步回包顺序与冷重载
			GmPlayerTDLevelRequest_77000054 badIdReq = GmPlayerTDLevelRequest_77000054.newBuilder()
					.setPlayerId("not-a-number")
					.setLevel(5)
					.build();
			ProtobufProtocol badIdProto = new ProtobufProtocol();
			badIdProto.setMsgID(PbProtocol.GmPlayerTDLevelRequest_77000054);
			badIdProto.setSeq(1);
			badIdProto.deserializeData(badIdReq.toByteArray());
			testGmHandler.dispatch(gmClient, badIdProto);
			Assertions.assertInstanceOf(GmPlayerTDLevelResponse_77000055.class, gmClient.responses.poll(3, TimeUnit.SECONDS));
			Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.getId(), gmClient.errorCodes.poll(3, TimeUnit.SECONDS));

			GmPlayerTDLevelRequest_77000054 badLvReq = GmPlayerTDLevelRequest_77000054.newBuilder()
					.setPlayerId(String.valueOf(gmPlayerId))
					.setLevel(0)
					.build();
			ProtobufProtocol badLvProto = new ProtobufProtocol();
			badLvProto.setMsgID(PbProtocol.GmPlayerTDLevelRequest_77000054);
			badLvProto.setSeq(2);
			badLvProto.deserializeData(badLvReq.toByteArray());
			testGmHandler.dispatch(gmClient, badLvProto);
			Assertions.assertInstanceOf(GmPlayerTDLevelResponse_77000055.class, gmClient.responses.poll(3, TimeUnit.SECONDS));
			Assertions.assertEquals(ErrorMsgEnum.gm_cmd_param.getId(), gmClient.errorCodes.poll(3, TimeUnit.SECONDS));

			GmPlayerTDLevelRequest_77000054 mutateReq = GmPlayerTDLevelRequest_77000054.newBuilder()
					.setPlayerId(String.valueOf(gmPlayerId))
					.setLevel(5)
					.build();
			ProtobufProtocol mutateProto = new ProtobufProtocol();
			mutateProto.setMsgID(PbProtocol.GmPlayerTDLevelRequest_77000054);
			mutateProto.setSeq(3);
			mutateProto.deserializeData(mutateReq.toByteArray());
			testGmHandler.dispatch(gmClient, mutateProto);
			Message mutateMsg = gmClient.responses.poll(3, TimeUnit.SECONDS);
			Assertions.assertInstanceOf(GmPlayerTDLevelResponse_77000055.class, mutateMsg);
			Assertions.assertEquals(0, gmClient.errorCodes.poll(3, TimeUnit.SECONDS));
			// 响应在 modifyPlayer 完成回调中发送，收到响应时状态变更必须已落定
			Assertions.assertEquals(5, gmPlayer.getDevelopModule().getHeavenlyDaoLevel());

			InMemoryPlayerStore store = new InMemoryPlayerStore();
			store.save(gmPlayer);
			Assertions.assertNotNull(store.getById(gmPlayerId));
			Assertions.assertNotNull(store.findById(gmPlayerId));
			Player freshPlayer = store.reloadFresh(gmPlayerId);
			Assertions.assertNotSame(gmPlayer, freshPlayer);
			Assertions.assertEquals(5, freshPlayer.getDevelopModule().getHeavenlyDaoLevel(),
					"gm0x77 修改后存储冷重载全新Player回读等级必须为5");

			// MUTATE 后执行 QUERY:GmPlayerRequest_77000021 验证查询路径完整可用
			GmPlayerRequest_77000021 queryReq = GmPlayerRequest_77000021.newBuilder()
					.setPlayerId(String.valueOf(gmPlayerId))
					.setName("GmPlayerB")
					.build();
			ProtobufProtocol queryProto = new ProtobufProtocol();
			queryProto.setMsgID(PbProtocol.GmPlayerRequest_77000021);
			queryProto.setSeq(4);
			queryProto.deserializeData(queryReq.toByteArray());
			testGmHandler.dispatch(gmClient, queryProto);
			Message queryMsg = gmClient.responses.poll(3, TimeUnit.SECONDS);
			Assertions.assertInstanceOf(GmPlayerResponse_77000022.class, queryMsg);
			Assertions.assertEquals(0, gmClient.errorCodes.poll(3, TimeUnit.SECONDS));
			GmPlayerResponse_77000022 queryResp = (GmPlayerResponse_77000022) queryMsg;
			Assertions.assertEquals(String.valueOf(gmPlayerId), queryResp.getPlayer().getPlayerId());
			Assertions.assertEquals("GmPlayerB", queryResp.getPlayer().getName());
		} finally {
			PlayerManager.getInstance().deletePlayer(nonGmPlayer);
			PlayerManager.getInstance().getAllPlayer().remove(nonGmPlayerId, nonGmPlayer);
			PlayerManager.getInstance().deletePlayer(gmPlayer);
			PlayerManager.getInstance().getAllPlayer().remove(gmPlayerId, gmPlayer);
			if (prevMode == null) {
				System.clearProperty(ServerContext.SERVER_RUN_MODE);
			} else {
				System.setProperty(ServerContext.SERVER_RUN_MODE, prevMode);
			}
			ServerContext.getInstance().setRunMode();
			procField.set(ServerContext.getInstance(), prevProcessor);
			processor.close();
			Field handlersField = DispatcherImpl.class.getDeclaredField("MODULE_HANDLERS");
			handlersField.setAccessible(true);
			((Map<?, ?>) handlersField.get(null)).remove(0x77, testGmHandler);
		}
	
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
}