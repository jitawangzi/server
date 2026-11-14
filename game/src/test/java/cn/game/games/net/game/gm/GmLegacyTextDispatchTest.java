package cn.game.games.net.game.gm;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.net.process.IdVirtualThreadProcessor;
import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.core.net.socket.controller.DispatcherImpl;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.core.net.socket.handler.Handler;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.gm.command.ItemGm;
import cn.game.games.net.game.gm.command.PlayerGm;
import cn.game.games.net.game.handler.TestHandler;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.currency.CurrencyModule;
import cn.game.games.net.game.module.develop.DevelopModule;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.TestMsg.TestGmCmdRequest_6f000001;
import cn.game.protocol.protobuf.TestMsg.TestGmCmdResponse_6f000002;
import cn.game.util.CborSnappyUtil;
import cn.game.util.ObjUtil;

/**
 * 传统文本GM命令分发表征回归与冷重载验证（覆盖TestGmCmdRequest_6f000001与GmRegistry兼容委托）
 *
 * @author shuyongqiang
 */
@Isolated
public class GmLegacyTextDispatchTest {

	static final class RecordingNetClient extends GameClient {
		private final long playerId;
		private Object lastMessage;
		private int lastErrorCode;

		RecordingNetClient(long playerId) {
			super(null);
			this.playerId = playerId;
		}

		@Override
		public boolean needProcess(IProtocol<?> protocol) {
			return true;
		}

		@Override
		public void afterProcess(IProtocol<?> protocol) {
		}

		@Override
		public void sendProtocol(Object message) {
			this.lastMessage = message;
			this.lastErrorCode = 0;
		}

		@Override
		public void sendProtocol(Object message, int errorCode) {
			this.lastMessage = message;
			this.lastErrorCode = errorCode;
		}

		@Override
		public long getPlayerId() {
			return playerId;
		}
	}

	static final class InMemoryPersistenceStore {
		private final Map<Long, PlayerData> store = new ConcurrentHashMap<>();

		void save(Player player) {
			PlayerData snapshot = new PlayerData();
			snapshot.setPlayerId(player.getPlayerId());
			snapshot.setUid(player.getData().getUid());
			snapshot.setName(player.getData().getName());
			snapshot.setServerId(player.getData().getServerId());
			snapshot.setLevel(player.getData().getLevel());
			snapshot.setAccountId(player.getData().getAccountId());
			snapshot.setCreateDate(player.getData().getCreateDate());
			snapshot.setLoginDate(player.getData().getLoginDate());
			snapshot.setRefreshDay(player.getData().getRefreshDay());
			snapshot.setIsGm(player.getData().getIsGm());
			snapshot.setModules(CborSnappyUtil.compress(new HashMap<>(player.getModules())));
			store.put(player.getPlayerId(), snapshot);
		}

		PlayerData selectById(long playerId) {
			return store.get(playerId);
		}
	}

	@Test
	void verifyLegacyTextGmDispatchAndColdReload() {
		try {
		ClassManager.getInstance().init();
		Field runModeField = ServerContext.class.getDeclaredField("runMode");
		runModeField.setAccessible(true);
		Field procField = ServerContext.class.getDeclaredField("processor");
		procField.setAccessible(true);
		RunMode previousMode = (RunMode) runModeField.get(ServerContext.getInstance());
		Processor previousProcessor = (Processor) procField.get(ServerContext.getInstance());
		long playerId = 880001L;
		Map<Long, Player> playersMap = getPlayersMap();

		try {
			runModeField.set(ServerContext.getInstance(), RunMode.TEST);
			IdVirtualThreadProcessor processor = new IdVirtualThreadProcessor();
			procField.set(ServerContext.getInstance(), processor);

			GmDispatcher globalDispatcher = GmDispatcher.getInstance();
			if (!globalDispatcher.hasCommand("item.item")) { globalDispatcher.registerBean(new ItemGm()); }
			if (!globalDispatcher.hasCommand("player.time")) { globalDispatcher.registerBean(new PlayerGm()); }

			PlayerData data = new PlayerData();
			ObjUtil.setDefaultValue(data);
			data.setPlayerId(playerId);
			data.setUid(playerId);
			data.setName("LegacyTestPlayer");
			data.setServerId("game_test");
			data.setLevel(1);
			data.setAccountId("acc_legacy_1");

			Player player = new Player(data);
			player.initModule(new HashMap<String, BasePlayerModule>());
			player.getDevelopModule().setHeavenlyDaoLevel(1);
			PlayerManager.getInstance().initAdd(player);

			InMemoryPersistenceStore persistence = new InMemoryPersistenceStore();
			persistence.save(player);

			TestHandler handler = new TestHandler();
			Field dispField = BaseHandler.class.getDeclaredField("dispatcher");
			dispField.setAccessible(true);
			dispField.set(handler, new DispatcherImpl() {
				@Override
				public void put(int module, Handler h) {
					if (!DispatcherImpl.getModuleHandlers().containsKey(module)) {
						super.put(module, h);
					}
				}
			});
			handler.init();
			RecordingNetClient client = new RecordingNetClient(playerId);

			// CHARACTERIZATION: 直接绕过前置查询触发 MUTATE:TestGmCmdRequest_6f000001 (variantKeys: legacyTextGm)
			TestGmCmdRequest_6f000001 itemReq = TestGmCmdRequest_6f000001.newBuilder()
					.setCmd("item 100001 100")
					.build();
			handler.dispatch(client, new ProtobufProtocol(PbProtocol.TestGmCmdRequest_6f000001, itemReq, 10));
			Assertions.assertEquals(0, client.lastErrorCode);
			Assertions.assertTrue(client.lastMessage instanceof TestGmCmdResponse_6f000002);
			TestGmCmdResponse_6f000002 itemResp = (TestGmCmdResponse_6f000002) client.lastMessage;
			JsonNode itemJson = new ObjectMapper().readTree(itemResp.getDebugInfo());
			Assertions.assertTrue(itemJson.get("ok").asBoolean());
			Assertions.assertEquals("1.0", itemJson.get("schemaVersion").asText());
			Assertions.assertEquals(100L, player.getCurrencyModule().getCount(100001));

			// 验证 GmRegistry 委托及 tdlv 指令执行
			boolean legacyHandled = GmRegistry.dispatch(player, "tdlv", new String[] { "tdlv", "3" });
			Assertions.assertTrue(legacyHandled);
			Assertions.assertEquals(3, player.getDevelopModule().getHeavenlyDaoLevel());

			// 持久化保存并销毁内存态，从 selectById 冷重载验证状态落盘一致性
			persistence.save(player);
			PlayerManager.getInstance().deletePlayer(playerId);
			playersMap.remove(playerId);

			PlayerData freshData = persistence.selectById(playerId);
			Assertions.assertNotNull(freshData);
			Player reloaded = new Player(freshData);

			CurrencyModule reloadedCurrency = reloaded.getModule(CurrencyModule.class);
			DevelopModule reloadedDevelop = reloaded.getModule(DevelopModule.class);
			Assertions.assertEquals(100L, reloadedCurrency.getCount(100001));
			Assertions.assertEquals(3, reloadedDevelop.getHeavenlyDaoLevel());
		} finally {
			PlayerManager.getInstance().deletePlayer(playerId);
			playersMap.remove(playerId);
			runModeField.set(ServerContext.getInstance(), previousMode);
			procField.set(ServerContext.getInstance(), previousProcessor);
		}
	
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<Long, Player> getPlayersMap() throws Exception {
		Field field = PlayerManager.class.getDeclaredField("id_players");
		field.setAccessible(true);
		return (Map<Long, Player>) field.get(PlayerManager.getInstance());
	}
}