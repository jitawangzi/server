package cn.game.games.net.game.gm.command;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericXmlApplicationContext;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheConfig;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.cache.id.GenericDistributedIDManager;
import cn.game.core.cache.id.IdCache;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.net.data.mapper.FriendApplicationMapper;
import cn.game.games.net.data.mapper.FriendMapper;
import cn.game.games.net.data.mapper.MailMapper;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.data.mapper.QuestMapper;
import cn.game.games.net.game.gm.GmDispatcher;
import cn.game.games.net.game.gm.GmResult;
import cn.game.games.net.game.gm.proto.VirtualGameClient;
import cn.game.games.net.game.helper.PlayerSaveCoordinator;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.util.ObjUtil;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * 好友、邮件、任务 GM 指令单元与调度集成测试（全量隔离外部 DB/Redis/Spring 上下文）
 *
 * @author shuyongqiang
 */
public class FriendMailQuestGmTest {

	private static final long TEST_PLAYER_ID = 88776655L;

	private static int prevFriendMax;
	private static Object prevSpringCtx;
	private static GenericXmlApplicationContext testSpringCtx;
	private static Vertx prevVertx;
	private static Vertx testVertx;
	private static GenericDistributedIDManager prevPlayerIdMgr;

	private Player player;
	private GmDispatcher dispatcher;

	@BeforeAll
	@SuppressWarnings("unchecked")
	public static void beforeAll() throws Exception {
		System.setProperty(ServerContext.SERVER_RUN_MODE, "DEV");
		ServerContext.getInstance().setRunMode();
		if (ServerContext.getInstance().getEventBus() == null) {
			ServerContext.getInstance().setEventBus(ServerEventBus.getInstance());
		}

		prevFriendMax = GlobalConst.FriendMax;
		if (GlobalConst.FriendMax <= 0) {
			GlobalConst.FriendMax = 50;
		}

		// 1. 隔离 Spring Context 并注册 Mapper 空操作代理，免改生产 DAO
		Field springCtxField = SpringContextLoader.class.getDeclaredField("appContextGeneric");
		springCtxField.setAccessible(true);
		prevSpringCtx = springCtxField.get(null);

		testSpringCtx = new GenericXmlApplicationContext();
		DefaultListableBeanFactory bf = testSpringCtx.getDefaultListableBeanFactory();
		registerNoOpMapper(bf, PlayerDataMapper.class);
		registerNoOpMapper(bf, FriendMapper.class);
		registerNoOpMapper(bf, FriendApplicationMapper.class);
		registerNoOpMapper(bf, MailMapper.class);
		registerNoOpMapper(bf, QuestMapper.class);
		testSpringCtx.refresh();
		springCtxField.set(null, testSpringCtx);

		// 2. 隔离 Vert.x 上下文，免改生产 VxHolder
		prevVertx = VxHolder.vertx;
		if (VxHolder.vertx == null) {
			testVertx = Vertx.vertx();
			VxHolder.vertx = testVertx;
		}

		// 3. 隔离 IdCache 在线探测，免改生产 IdCache / FriendModule
		Field mgrsField = IdCache.class.getDeclaredField("managers");
		mgrsField.setAccessible(true);
		Map<DistributedObjectType, GenericDistributedIDManager> mgrs =
				(Map<DistributedObjectType, GenericDistributedIDManager>) mgrsField.get(null);
		prevPlayerIdMgr = mgrs.get(DistributedObjectType.PLAYER);
		mgrs.put(DistributedObjectType.PLAYER, new GenericDistributedIDManager(
				DistributedObjectType.PLAYER,
				new CacheConfig(64, 60, CacheType.PLAYER_SERVER_ID)) {
			@Override
			public String getServerIdInternal(long objectId, boolean useCacheIfAbsent) {
				return "";
			}

			@Override
			public Future<String> getServerIdAsync(long objectId, boolean useCacheIfAbsent) {
				return Future.succeededFuture("");
			}

			@Override
			public boolean isObjectInCurrentServer(long objectId) {
				return false;
			}

			@Override
			public Collection<Long> getAllIds() {
				return Collections.emptyList();
			}
		});

		ClassManager.getInstance().init();
	}

	@AfterAll
	@SuppressWarnings("unchecked")
	public static void afterAll() throws Exception {
		GlobalConst.FriendMax = prevFriendMax;

		Field springCtxField = SpringContextLoader.class.getDeclaredField("appContextGeneric");
		springCtxField.setAccessible(true);
		springCtxField.set(null, prevSpringCtx);
		if (testSpringCtx != null) {
			testSpringCtx.close();
		}

		if (testVertx != null) {
			VxHolder.vertx = prevVertx;
			testVertx.close();
		}

		Field mgrsField = IdCache.class.getDeclaredField("managers");
		mgrsField.setAccessible(true);
		Map<DistributedObjectType, GenericDistributedIDManager> mgrs =
				(Map<DistributedObjectType, GenericDistributedIDManager>) mgrsField.get(null);
		if (prevPlayerIdMgr != null) {
			mgrs.put(DistributedObjectType.PLAYER, prevPlayerIdMgr);
		} else {
			mgrs.remove(DistributedObjectType.PLAYER);
		}
	}

	private static <T> void registerNoOpMapper(DefaultListableBeanFactory bf, Class<T> mapperClass) {
		Object proxy = Proxy.newProxyInstance(
				mapperClass.getClassLoader(),
				new Class<?>[] { mapperClass },
				(p, method, args) -> {
					Class<?> ret = method.getReturnType();
					if (ret == int.class || ret == Integer.class) {
						return 1;
					}
					if (ret == long.class || ret == Long.class) {
						return 1L;
					}
					if (ret == boolean.class || ret == Boolean.class) {
						return true;
					}
					if (List.class.isAssignableFrom(ret)) {
						return Collections.emptyList();
					}
					return null;
				});
		bf.registerSingleton(mapperClass.getName(), proxy);
	}

	@BeforeEach
	public void setUp() {
		dispatcher = GmDispatcher.getInstance();
		if (!dispatcher.hasCommand("friend.add")) {
			dispatcher.registerBean(new FriendGm());
		}
		if (!dispatcher.hasCommand("mail.send")) {
			dispatcher.registerBean(new MailGm());
		}
		if (!dispatcher.hasCommand("quest.quest")) {
			dispatcher.registerBean(new QuestGm());
		}

		PlayerData data = new PlayerData();
		ObjUtil.setDefaultValue(data);
		data.setPlayerId(TEST_PLAYER_ID);
		data.setUid(TEST_PLAYER_ID);
		data.setName("GmTestPlayer");
		data.setServerId("game_dev_1");
		data.setLevel(30);

		player = new Player(data);
		player.setOnline(true);
		player.setIslogouting(false);
		player.setGameClient(new VirtualGameClient(TEST_PLAYER_ID));

		PlayerManager.getInstance().initAdd(player);
	}

	@AfterEach
	public void tearDown() {
		if (player != null) {
			try {
				Field lanesField = PlayerSaveCoordinator.class.getDeclaredField("lanes");
				lanesField.setAccessible(true);
				Map<?, ?> lanes = (Map<?, ?>) lanesField.get(PlayerSaveCoordinator.shared());
				lanes.remove(player.getPlayerId());
			} catch (Throwable ignored) {
			}
			PlayerManager.getInstance().deletePlayer(player);
			PlayerManager.getInstance().getAllPlayer().remove(player.getPlayerId(), player);
		}
	}

	@Test
	public void testFriendGmFullWorkflow() {
		GmResult resAdd = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "friend.add 10002", null, false);
		Assertions.assertTrue(resAdd.isOk(), resAdd.getSummary());

		GmResult resList = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "friend.list", null, false);
		Assertions.assertTrue(resList.isOk(), resList.getSummary());
		@SuppressWarnings("unchecked")
		Map<String, Object> listMap = (Map<String, Object>) resList.getData();
		Assertions.assertEquals(1, listMap.get("friendCount"));

		GmResult resApply = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "friend.apply 10003", null, false);
		Assertions.assertTrue(resApply.isOk(), resApply.getSummary());

		GmResult resAccept = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "friend.accept 10003", null, false);
		Assertions.assertTrue(resAccept.isOk(), resAccept.getSummary());

		GmResult resListAfterAccept = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "friend.list", null, false);
		@SuppressWarnings("unchecked")
		Map<String, Object> listMapAfterAccept = (Map<String, Object>) resListAfterAccept.getData();
		Assertions.assertEquals(2, listMapAfterAccept.get("friendCount"));

		GmResult resGift = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "friend.gift 10002 true false", null, false);
		Assertions.assertTrue(resGift.isOk(), resGift.getSummary());

		GmResult resIntimate = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "friend.intimate 10002 50", null, false);
		Assertions.assertTrue(resIntimate.isOk(), resIntimate.getSummary());

		GmResult resBlack = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "friend.black 10004", null, false);
		Assertions.assertTrue(resBlack.isOk(), resBlack.getSummary());

		GmResult resUnblack = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "friend.unblack 10004", null, false);
		Assertions.assertTrue(resUnblack.isOk(), resUnblack.getSummary());

		GmResult resDel = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "friend.del 10002", null, false);
		Assertions.assertTrue(resDel.isOk(), resDel.getSummary());

		GmResult resClear = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "friend.clear", null, false);
		Assertions.assertTrue(resClear.isOk(), resClear.getSummary());

		GmResult resListAfterClear = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "friend.list", null, false);
		@SuppressWarnings("unchecked")
		Map<String, Object> finalMap = (Map<String, Object>) resListAfterClear.getData();
		Assertions.assertEquals(0, finalMap.get("friendCount"));
	}

	@Test
	public void testMailGmFullWorkflow() {
		GmResult resSend = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "mail.send 0 测试邮件 自动化测试邮件内容 100002 100", null, false);
		Assertions.assertTrue(resSend.isOk(), resSend.getSummary());

		GmResult resList = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "mail.list", null, false);
		Assertions.assertTrue(resList.isOk(), resList.getSummary());
		@SuppressWarnings("unchecked")
		Map<String, Object> mailMap = (Map<String, Object>) resList.getData();
		Assertions.assertTrue(((Number) mailMap.get("totalCount")).intValue() >= 1);

		GmResult resRead = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "mail.read 0", null, false);
		Assertions.assertTrue(resRead.isOk(), resRead.getSummary());

		GmResult resReceive = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "mail.receive 0", null, false);
		Assertions.assertTrue(resReceive.isOk(), resReceive.getSummary());

		GmResult resClear = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "mail.clear", null, false);
		Assertions.assertTrue(resClear.isOk(), resClear.getSummary());

		GmResult resListAfterClear = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "mail.list", null, false);
		@SuppressWarnings("unchecked")
		Map<String, Object> finalMailMap = (Map<String, Object>) resListAfterClear.getData();
		Assertions.assertEquals(0, ((Number) finalMailMap.get("totalCount")).intValue());
	}

	@Test
	public void testQuestGmFullWorkflow() {
		GmResult resList = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "quest.list 0", null, false);
		Assertions.assertTrue(resList.isOk(), resList.getSummary());
		Assertions.assertNotNull(resList.getData());

		GmResult resFinishAll = dispatcher.dispatchHttpText(TEST_PLAYER_ID, "quest.finishAll 0", null, false);
		Assertions.assertTrue(resFinishAll.isOk(), resFinishAll.getSummary());
	}
}