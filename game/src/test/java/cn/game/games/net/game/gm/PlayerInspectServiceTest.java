package cn.game.games.net.game.gm;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.execute.TaskExecutorService;
import cn.game.core.net.process.IdVirtualThreadProcessor;
import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.core.net.socket.controller.DispatcherImpl;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.core.net.socket.handler.Handler;
import cn.game.games.cache.entity.Chapter;
import cn.game.games.cache.entity.Friend;
import cn.game.games.cache.entity.ItemNoStack;
import cn.game.games.cache.entity.Mail;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.data.mapper.FriendMapper;
import cn.game.games.net.data.mapper.InviteMapper;
import cn.game.games.net.data.mapper.MailMapper;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.game.handler.TestHandler;
import cn.game.games.net.game.helper.PlayerSaveCoordinator;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.BattleModule;
import cn.game.games.net.game.module.develop.AttrModule;
import cn.game.games.net.game.module.friend.FriendModule;
import cn.game.games.net.game.module.invite.InviteModule;
import cn.game.games.net.game.module.mail.MailModule;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.TestMsg.TestInspectPlayerRequest_6f000108;
import cn.game.protocol.protobuf.TestMsg.TestInspectPlayerResponse_6f000109;
import cn.game.testsupport.InspectTestModules.CircularRefModule;
import cn.game.testsupport.InspectTestModules.DfsBoundsTestModule;
import cn.game.testsupport.InspectTestModules.FaultyInspectModule;
import cn.game.testsupport.InspectTestModules.SpyGetterModule;
import cn.game.testsupport.InspectTestModules.TestItemNoStackModule;
import cn.game.util.FloatMapWrapper;
import cn.game.util.CborSnappyUtil;
import cn.game.util.ObjUtil;
import cn.game.util.SpringContextLoader;

/**
 * gm.inspect 只读白盒状态观测、6个分表/特殊模块覆写、邮箱并发隔离与单模块异常隔离单元测试
 *
 * @author shuyongqiang
 */
@Isolated
public class PlayerInspectServiceTest {

	@BeforeAll
	static void initMetadata() {
		ClassManager.getInstance().init();
	}

	/**
	 * 自省协议测试用桩客户端
	 *
	 * @author shuyongqiang
	 */
	static final class InspectNetClient extends GameClient {
		private final long playerId;
		private Object lastMessage;
		private int lastErrorCode;

		InspectNetClient(long playerId) {
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

	/**
	 * 验证gm.inspect只读语义（零DAO写库、零getter副作用、PlayerData前后字节一致）、
	 * 6个特殊模块快照、日志Throwable告警、循环引用单字段降级、未知模块与scope=global契约
	 *
	 * @throws Exception 反射或测试异常
	 */
	@Test
	@SuppressWarnings("unchecked")
	void verifyInspectStateReadOnlyAndStandaloneModules() {
		try {
		ClassManager.getInstance().init();
		Field runModeField = ServerContext.class.getDeclaredField("runMode");
		runModeField.setAccessible(true);
		Field procField = ServerContext.class.getDeclaredField("processor");
		procField.setAccessible(true);
		Field springCtxField = SpringContextLoader.class.getDeclaredField("appContextGeneric");
		springCtxField.setAccessible(true);

		RunMode previousMode = (RunMode) runModeField.get(ServerContext.getInstance());
		Processor previousProcessor = (Processor) procField.get(ServerContext.getInstance());
		Object previousSpringCtx = springCtxField.get(null);

		long playerId = 880002L;
		Map<Long, Player> playersMap = getPlayersMap();
		ObjectMapper mapper = new ObjectMapper();
		AtomicInteger daoInvocationCount = new AtomicInteger(0);
		GenericXmlApplicationContext probeContext = new GenericXmlApplicationContext();
		registerCountingMapper(probeContext, PlayerDataMapper.class, daoInvocationCount);
		registerCountingMapper(probeContext, MailMapper.class, daoInvocationCount);
		registerCountingMapper(probeContext, FriendMapper.class, daoInvocationCount);
		registerCountingMapper(probeContext, InviteMapper.class, daoInvocationCount);
		probeContext.refresh();

		Logger inspectLogger = (Logger) LogManager.getLogger(PlayerInspectService.class);
		Level previousLevel = inspectLogger.getLevel();
		List<LogEvent> warnEvents = new ArrayList<>();
		AbstractAppender logProbe = new AbstractAppender("inspect-warn-probe", null,
				PatternLayout.createDefaultLayout(), false, Property.EMPTY_ARRAY) {
			@Override
			public void append(LogEvent event) {
				if (event.getLevel().isMoreSpecificThan(Level.WARN)) {
					warnEvents.add(event.toImmutable());
				}
			}
		};
		logProbe.start();
		inspectLogger.addAppender(logProbe);
		inspectLogger.setLevel(Level.WARN);

		TaskExecutorService executor = new TaskExecutorService();
		IdVirtualThreadProcessor processor = new IdVirtualThreadProcessor(executor);

		try {
			springCtxField.set(null, probeContext);
			runModeField.set(ServerContext.getInstance(), RunMode.TEST);
			procField.set(ServerContext.getInstance(), processor);

			PlayerData data = new PlayerData();
			ObjUtil.setDefaultValue(data);
			data.setPlayerId(playerId);
			data.setUid(playerId);
			data.setName("InspectPlayer");
			data.setServerId("game_test");
			data.setLevel(15);
			byte[] beforeBytes = CborSnappyUtil.compress(data);

			Player player = new Player(data);

			SpyGetterModule spyModule = new SpyGetterModule();
			FaultyInspectModule faultyModule = new FaultyInspectModule();
			CircularRefModule circularModule = new CircularRefModule();

			MailModule mailModule = new MailModule();
			mailModule.setGlobalMailId(12L);
			Mail mail = new Mail();
			mail.setId(5001L);
			mail.setMailId(101);
			mail.setTitle("GM测试邮件");
			setField(mailModule, "mails", Map.of(5001L, mail));

			FriendModule friendModule = new FriendModule();
			Friend friend = new Friend();
			friend.setFriendId(9001L);
			friend.setRelation(Friend.FRIEND);
			setField(friendModule, "friends", Map.of(9001L, friend));

			BattleModule battleModule = new BattleModule();
			setField(battleModule, "mainBattleHighest", 1050);

			InviteModule inviteModule = new InviteModule();
			inviteModule.updateTargetLv(7001L, 35);

			TestItemNoStackModule itemNoStackModule = new TestItemNoStackModule();
			ItemNoStack equip = new ItemNoStack();
			equip.setId(6001L);
			equip.setConfigId(30001);
			equip.setLevel(5);
			itemNoStackModule.initAddCache(equip);

			AttrModule attrModule = new AttrModule();
			setField(attrModule, "power", 98765L);

			Map<String, BasePlayerModule> moduleMap = new HashMap<>();
			moduleMap.put(SpyGetterModule.class.getName(), spyModule);
			moduleMap.put(FaultyInspectModule.class.getName(), faultyModule);
			moduleMap.put(CircularRefModule.class.getName(), circularModule);
			moduleMap.put(MailModule.class.getName(), mailModule);
			moduleMap.put(FriendModule.class.getName(), friendModule);
			moduleMap.put(BattleModule.class.getName(), battleModule);
			moduleMap.put(InviteModule.class.getName(), inviteModule);
			moduleMap.put(TestItemNoStackModule.class.getName(), itemNoStackModule);
			moduleMap.put(AttrModule.class.getName(), attrModule);
			player.setModules(moduleMap);
			PlayerManager.getInstance().initAdd(player);

			PlayerInspectService service = new PlayerInspectService();
			String firstSnapshot = null;
			for (int i = 0; i < 3; i++) {
				GmResult res = service.inspectPlayer(playerId, List.of(), "player", processor);
				Assertions.assertTrue(res.isOk(), "第" + (i + 1) + "次只读自省应成功");
				Map<String, Object> payload = (Map<String, Object>) res.getData();
				Assertions.assertEquals(Boolean.FALSE, payload.get("hasUnknownModule"));
				Map<String, Object> modules = (Map<String, Object>) payload.get("modules");
				Assertions.assertNotNull(modules.get("MailModule"));
				Assertions.assertNotNull(modules.get("FriendModule"));
				Assertions.assertNotNull(modules.get("BattleModule"));
				Assertions.assertNotNull(modules.get("InviteModule"));
				Assertions.assertNotNull(modules.get("TestItemNoStackModule"));
				Assertions.assertNotNull(modules.get("AttrModule"));
				Map<String, Object> faultNode = (Map<String, Object>) modules.get("FaultyInspectModule");
				Assertions.assertTrue(faultNode.containsKey("_inspectError"));
				Map<String, Object> circularSnap = (Map<String, Object>) modules.get("CircularRefModule");
				Assertions.assertEquals("sibling-ok", circularSnap.get("healthySibling"));
				List<?> circularNodeSnap = (List<?>) circularSnap.get("circularNode");
				Assertions.assertEquals(1, circularNodeSnap.size());
				Assertions.assertEquals("<circular:ArrayList>", circularNodeSnap.get(0));
				String serialized = mapper.writeValueAsString(payload);
				Assertions.assertFalse(serialized.contains("StackOverflowError"),
						"循环引用必须由祖先链环检测拦截为<circular:...>，绝不应触发StackOverflowError");
				if (firstSnapshot == null) {
					firstSnapshot = serialized;
				} else {
					Assertions.assertEquals(firstSnapshot, serialized, "连续3次只读自省快照应完全一致");
				}
			}

			Assertions.assertEquals(0, spyModule.getterCalls.get(), "只读自省不得调用任何业务getter");
			Assertions.assertEquals(0, daoInvocationCount.get(), "只读自省不得触发任何DAO写库或查询调用");
			PlayerSaveCoordinator.Status saveStatus = PlayerSaveCoordinator.shared().status(playerId);
			Assertions.assertNotNull(saveStatus);
			Assertions.assertEquals(0L, saveStatus.generation(), "只读自省不应推进保存代次");
			Assertions.assertEquals(0, saveStatus.queued(), "只读自省不应产生写库排队任务");
			Assertions.assertFalse(saveStatus.observedDirty(), "只读自省不应标记玩家脏状态");
			Assertions.assertFalse(saveStatus.writeProtected(), "只读自省不应触发写保护");

			byte[] afterBytes = CborSnappyUtil.compress(player.getData());
			Assertions.assertArrayEquals(beforeBytes, afterBytes, "连续3次自省后PlayerData二进制快照必须完全一致");
			PlayerData coldReloaded = reloadFresh(afterBytes);
			Assertions.assertEquals(playerId, coldReloaded.getPlayerId());
			Assertions.assertEquals("InspectPlayer", coldReloaded.getName());
			Assertions.assertEquals(15, coldReloaded.getLevel());

			boolean loggedFaultThrowable = warnEvents.stream()
					.anyMatch(e -> e.getThrown() instanceof IllegalStateException
							&& e.getMessage().getFormattedMessage().contains("FaultyInspectModule"));
			Assertions.assertTrue(loggedFaultThrowable, "单模块自省异常必须记录WARN日志并携带Throwable");

			// 校验未知模块名过滤必须显式标记错误且不静默返回空ok=true
			GmResult unknownModuleResult = service.inspectPlayer(playerId, List.of("MailModule", "NoSuchModuleXYZ"), "player", processor);
			Assertions.assertFalse(unknownModuleResult.isOk());
			Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.getId(), unknownModuleResult.getErrorCode());
			Map<String, Object> unknownPayload = (Map<String, Object>) unknownModuleResult.getData();
			Assertions.assertEquals(Boolean.TRUE, unknownPayload.get("hasUnknownModule"));
			Assertions.assertTrue(((List<?>) unknownPayload.get("unknownModules")).contains("NoSuchModuleXYZ"));
			Map<String, Object> unknownModulesMap = (Map<String, Object>) unknownPayload.get("modules");
			Assertions.assertNotNull(unknownModulesMap.get("MailModule"));
			Assertions.assertTrue(((Map<String, Object>) unknownModulesMap.get("NoSuchModuleXYZ")).containsKey("_inspectError"));

			// 校验 scope="global" 必须按DC-07返回gm_cmd_not_exist且不回退到player
			GmResult globalScopeResult = service.inspectPlayer(playerId, List.of(), "global", processor);
			Assertions.assertFalse(globalScopeResult.isOk());
			Assertions.assertEquals(ErrorMsgEnum.gm_cmd_not_exist.getId(), globalScopeResult.getErrorCode());

			// 校验默认构造器在缺少Processor且非邮箱线程时必须Fail-Closed拒绝裸读
			procField.set(ServerContext.getInstance(), null);
			GmResult noProcessorFail = service.inspectPlayer(player, List.of("SpyGetterModule"), "player", null);
			Assertions.assertFalse(noProcessorFail.isOk());
			Assertions.assertEquals(ErrorMsgEnum.unknown.getId(), noProcessorFail.getErrorCode());
			PlayerInspectService testDirectService = new PlayerInspectService(true);
			GmResult testDirectOk = testDirectService.inspectPlayer(player, List.of("SpyGetterModule"), "player", null);
			Assertions.assertTrue(testDirectOk.isOk());
			procField.set(ServerContext.getInstance(), processor);

			// 校验 QUERY:TestInspectPlayerRequest_6f000108 协议通路 (variantKeys: structuredGmExec)
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
			InspectNetClient client = new InspectNetClient(playerId);
			TestInspectPlayerRequest_6f000108 inspectReq = TestInspectPlayerRequest_6f000108.newBuilder()
					.setTargetPlayerId(playerId)
					.addModules("MailModule")
					.addModules("AttrModule")
					.setScope("player")
					.build();
			handler.dispatch(client, new ProtobufProtocol(PbProtocol.TestInspectPlayerRequest_6f000108, inspectReq, 21));
			Assertions.assertEquals(0, client.lastErrorCode);
			Assertions.assertTrue(client.lastMessage instanceof TestInspectPlayerResponse_6f000109);
			TestInspectPlayerResponse_6f000109 inspectResp = (TestInspectPlayerResponse_6f000109) client.lastMessage;
			Assertions.assertEquals("1.0", inspectResp.getSchemaVersion());
			JsonNode root = mapper.readTree(inspectResp.getResultJson());
			Assertions.assertTrue(root.get("ok").asBoolean());
			JsonNode filteredModules = root.get("data").get("modules");
			Assertions.assertEquals(2, filteredModules.size());
			Assertions.assertEquals(12L, filteredModules.get("MailModule").get("globalMailId").asLong());
			Assertions.assertEquals(98765L, filteredModules.get("AttrModule").get("power").asLong());
			Assertions.assertEquals(0, daoInvocationCount.get(), "0x6f000108自省全链路不得触发DAO调用");
		} finally {
			inspectLogger.removeAppender(logProbe);
			inspectLogger.setLevel(previousLevel);
			logProbe.stop();
			executor.close();
			probeContext.close();
			springCtxField.set(null, previousSpringCtx);
			PlayerManager.getInstance().deletePlayer(playerId);
			playersMap.remove(playerId);
			runModeField.set(ServerContext.getInstance(), previousMode);
			procField.set(ServerContext.getInstance(), previousProcessor);
		}
	
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * 验证DFS非父子共享引用正常展开两次、超深度/超元素数截断、6模块大集合上限保留总数与JSON数组过滤兼容
	 *
	 * @throws Exception 反射或序列化异常
	 */
	@Test
	@SuppressWarnings("unchecked")
	void verifyCycleSharedRefDepthElementBoundsAndJsonFilter() throws Exception {
		PlayerData data = new PlayerData();
		ObjUtil.setDefaultValue(data);
		data.setPlayerId(880077L);
		data.setUid(880077L);
		data.setName("BoundsPlayer");
		data.setServerId("game_test");
		Player player = new Player(data);

		DfsBoundsTestModule boundsModule = new DfsBoundsTestModule();
		MailModule mailModule = new MailModule();
		Map<Long, Mail> manyMails = new java.util.LinkedHashMap<>();
		for (long i = 1; i <= 225; i++) {
			Mail mail = new Mail();
			mail.setId(i);
			mail.setMailId((int) i);
			mail.setTitle("m" + i);
			manyMails.put(i, mail);
		}
		setField(mailModule, "mails", manyMails);

		FriendModule friendModule = new FriendModule();
		Map<Long, Friend> manyFriends = new java.util.LinkedHashMap<>();
		for (long i = 1; i <= 210; i++) {
			Friend friend = new Friend();
			friend.setFriendId(i);
			friend.setRelation(Friend.FRIEND);
			manyFriends.put(i, friend);
		}
		setField(friendModule, "friends", manyFriends);

		TestItemNoStackModule itemModule = new TestItemNoStackModule();
		for (long i = 1; i <= 210; i++) {
			ItemNoStack item = new ItemNoStack();
			item.setId(i);
			item.setConfigId(30001);
			itemModule.initAddCache(item);
		}

		BattleModule battleModule = new BattleModule();
		Map<Integer, Chapter> manyChapters = new java.util.LinkedHashMap<>();
		for (int i = 1; i <= 210; i++) {
			Chapter ch = new Chapter();
			ch.setBattleId(i);
			manyChapters.put(i, ch);
		}
		setField(battleModule, "chapters", manyChapters);

		InviteModule inviteModule = new InviteModule();
		for (long i = 1; i <= 210; i++) {
			inviteModule.updateTargetLv(i, 10);
		}

		AttrModule attrModule = new AttrModule();
		Map<Long, FloatMapWrapper> manyHeroAttrs = new java.util.LinkedHashMap<>();
		for (long i = 1; i <= 210; i++) {
			FloatMapWrapper wrapper = new FloatMapWrapper();
			wrapper.setValue(1, (float) i);
			manyHeroAttrs.put(i, wrapper);
		}
		setField(attrModule, "heroAttrs", manyHeroAttrs);

		Map<String, BasePlayerModule> moduleMap = new HashMap<>();
		moduleMap.put(DfsBoundsTestModule.class.getName(), boundsModule);
		moduleMap.put(MailModule.class.getName(), mailModule);
		moduleMap.put(FriendModule.class.getName(), friendModule);
		moduleMap.put(TestItemNoStackModule.class.getName(), itemModule);
		moduleMap.put(BattleModule.class.getName(), battleModule);
		moduleMap.put(InviteModule.class.getName(), inviteModule);
		moduleMap.put(AttrModule.class.getName(), attrModule);
		player.setModules(moduleMap);

		PlayerInspectService service = new PlayerInspectService(true);
		GmResult fullRes = service.inspectSnapshot(player, List.of(), "player");
		Assertions.assertTrue(fullRes.isOk());
		Map<String, Object> modules = (Map<String, Object>) ((Map<String, Object>) fullRes.getData()).get("modules");

		// 1) 非父子共享引用必须正常展开两次
		Map<String, Object> dfsSnap = (Map<String, Object>) modules.get("DfsBoundsTestModule");
		Map<String, Object> leftSnap = (Map<String, Object>) dfsSnap.get("sharedLeft");
		Map<String, Object> rightSnap = (Map<String, Object>) dfsSnap.get("sharedRight");
		Assertions.assertEquals("shared-ok", leftSnap.get("tag"));
		Assertions.assertEquals(7, leftSnap.get("count"));
		Assertions.assertEquals("shared-ok", rightSnap.get("tag"));
		Assertions.assertEquals(7, rightSnap.get("count"));

		// 2) 深度超过MAX_INSPECT_DEPTH(8)必须拦截为<maxDepth>
		Object cursor = dfsSnap.get("deepChain");
		for (int depth = 0; depth <= BasePlayerModule.MAX_INSPECT_DEPTH; depth++) {
			Assertions.assertTrue(cursor instanceof Map, "depth=" + depth + " 应正常展开为Map");
			cursor = ((Map<String, Object>) cursor).get("next");
		}
		Assertions.assertEquals("<maxDepth>", cursor, "超过MAX_INSPECT_DEPTH的子节点必须返回<maxDepth>");

		// 3) 容器元素上限截断与JDK包安全降级
		List<?> boundedList = (List<?>) dfsSnap.get("oversizedList");
		Assertions.assertEquals(BasePlayerModule.MAX_INSPECT_ELEMENTS + 1, boundedList.size());
		Map<?, ?> listTail = (Map<?, ?>) boundedList.get(BasePlayerModule.MAX_INSPECT_ELEMENTS);
		Assertions.assertEquals(Boolean.TRUE, listTail.get("_truncated"));
		Assertions.assertEquals(220, listTail.get("_totalSize"));

		Map<String, Object> boundedMap = (Map<String, Object>) dfsSnap.get("oversizedMap");
		Assertions.assertEquals(Boolean.TRUE, boundedMap.get("_truncated"));
		Assertions.assertEquals(220, boundedMap.get("_totalSize"));
		Assertions.assertEquals("<opaque:ByteArrayInputStream>", dfsSnap.get("opaqueJdkStream"));
		Assertions.assertEquals("2026-01-15", dfsSnap.get("temporalDate"));
		Assertions.assertNotNull(new ObjectMapper().writeValueAsString(dfsSnap));

		// 4) 6个模块大集合保留真实总数并限制输出上限为200
		Map<String, Object> mailSnap = (Map<String, Object>) modules.get("MailModule");
		Assertions.assertEquals(225, mailSnap.get("mailCount"));
		Assertions.assertEquals(BasePlayerModule.MAX_INSPECT_ELEMENTS, ((List<?>) mailSnap.get("mails")).size());
		Assertions.assertEquals(Boolean.TRUE, mailSnap.get("mailsTruncated"));
		Assertions.assertEquals(Boolean.TRUE, mailSnap.get("truncated"));

		Map<String, Object> friendSnap = (Map<String, Object>) modules.get("FriendModule");
		Assertions.assertEquals(210, friendSnap.get("friendCount"));
		Assertions.assertEquals(BasePlayerModule.MAX_INSPECT_ELEMENTS, ((List<?>) friendSnap.get("friends")).size());
		Assertions.assertEquals(Boolean.TRUE, friendSnap.get("friendsTruncated"));

		Map<String, Object> itemSnap = (Map<String, Object>) modules.get("TestItemNoStackModule");
		Assertions.assertEquals(210, itemSnap.get("itemCount"));
		Assertions.assertEquals(BasePlayerModule.MAX_INSPECT_ELEMENTS, ((List<?>) itemSnap.get("items")).size());
		Assertions.assertEquals(Boolean.TRUE, itemSnap.get("itemsTruncated"));

		Map<String, Object> battleSnap = (Map<String, Object>) modules.get("BattleModule");
		Assertions.assertEquals(210, battleSnap.get("chapterCount"));
		Assertions.assertEquals(BasePlayerModule.MAX_INSPECT_ELEMENTS, ((Map<?, ?>) battleSnap.get("chapters")).size());
		Assertions.assertEquals(Boolean.TRUE, battleSnap.get("chaptersTruncated"));

		Map<String, Object> inviteSnap = (Map<String, Object>) modules.get("InviteModule");
		Assertions.assertEquals(210, inviteSnap.get("targetCount"));
		Assertions.assertEquals(BasePlayerModule.MAX_INSPECT_ELEMENTS, ((Map<?, ?>) inviteSnap.get("targetLvMap")).size());
		Assertions.assertEquals(Boolean.TRUE, inviteSnap.get("targetLvMapTruncated"));

		Map<String, Object> attrSnap = (Map<String, Object>) modules.get("AttrModule");
		Assertions.assertEquals(210, attrSnap.get("heroAttrCount"));
		Assertions.assertEquals(BasePlayerModule.MAX_INSPECT_ELEMENTS, ((Map<?, ?>) attrSnap.get("heroAttrs")).size());
		Assertions.assertEquals(Boolean.TRUE, attrSnap.get("heroAttrsTruncated"));

		// 5) gm.inspect文本modules参数同时兼容JSON数组字符串
		GmResult jsonArrayFilterRes = service.inspect(player, "[\"MailModule\",\"AttrModule\"]", "player");
		Assertions.assertTrue(jsonArrayFilterRes.isOk());
		Map<String, Object> jsonFilteredModules = (Map<String, Object>) ((Map<String, Object>) jsonArrayFilterRes.getData()).get("modules");
		Assertions.assertEquals(2, jsonFilteredModules.size());
		Assertions.assertTrue(jsonFilteredModules.containsKey("MailModule"));
		Assertions.assertTrue(jsonFilteredModules.containsKey("AttrModule"));

		GmResult malformedJsonFilterRes = service.inspect(player, "[\"MailModule", "player");
		Assertions.assertFalse(malformedJsonFilterRes.isOk());
		Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.getId(), malformedJsonFilterRes.getErrorCode());
	}

	/**
	 * 验证真实IdVirtualThreadProcessor邮箱线程内自省当前玩家自身不会发生5秒自死锁，且外部线程投递同样正常返回
	 *
	 * @throws Exception 执行器或异步等待异常
	 */
	@Test
	void verifyRealMailboxSelfAndExternalInspectNoDeadlock() throws Exception {
		long playerId = 880099L;
		PlayerData data = new PlayerData();
		ObjUtil.setDefaultValue(data);
		data.setPlayerId(playerId);
		data.setUid(playerId);
		data.setName("MailboxSelfInspectPlayer");
		data.setServerId("game_test");
		Player player = new Player(data);
		SpyGetterModule spyModule = new SpyGetterModule();
		Map<String, BasePlayerModule> moduleMap = new HashMap<>();
		moduleMap.put(SpyGetterModule.class.getName(), spyModule);
		player.setModules(moduleMap);

		PlayerInspectService service = new PlayerInspectService();
		try (TaskExecutorService executor = new TaskExecutorService()) {
			IdVirtualThreadProcessor processor = new IdVirtualThreadProcessor(executor);

			// 外部线程经邮箱串行自省
			GmResult externalResult = service.inspectPlayer(player, List.of("SpyGetterModule"), "player", processor);
			Assertions.assertTrue(externalResult.isOk());

			// 已处于同一玩家邮箱内部时直接执行快照，毫秒级返回且绝不二次投递自死锁
			CompletableFuture<GmResult> inMailboxFuture = new CompletableFuture<>();
			long startNs = System.nanoTime();
			processor.process(playerId, () -> {
				Assertions.assertTrue(GmDispatcher.isCurrentMailboxPlayer(playerId),
						"IdVirtualThreadProcessor邮箱内必须被识别为当前玩家邮箱上下文");
				inMailboxFuture.complete(service.inspectPlayer(player, List.of("SpyGetterModule"), "player", processor));
			});
			GmResult selfResult = inMailboxFuture.get(1500, TimeUnit.MILLISECONDS);
			long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
			Assertions.assertTrue(selfResult.isOk(), "同玩家邮箱内自省自身必须成功");
			Assertions.assertTrue(elapsedMs < 1500L, "同玩家邮箱内自省不应触发5秒死锁超时 elapsedMs=" + elapsedMs);
			Assertions.assertEquals(0, spyModule.getterCalls.get());
		}
	}

	private static <T> void registerCountingMapper(GenericXmlApplicationContext ctx, Class<T> mapperClass, AtomicInteger calls) {
		Object proxy = Proxy.newProxyInstance(mapperClass.getClassLoader(), new Class<?>[] { mapperClass },
				(obj, method, args) -> {
					calls.incrementAndGet();
					return null;
				});
		ctx.registerBean(mapperClass.getSimpleName(), mapperClass, () -> mapperClass.cast(proxy));
	}

	private static void setField(Object target, String fieldName, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}

	@SuppressWarnings("unchecked")
	private static PlayerData reloadFresh(byte[] bytes) {
		return CborSnappyUtil.decompress(bytes, PlayerData.class);
	}

	private static Map<Long, Player> getPlayersMap() throws Exception {
		Field field = PlayerManager.class.getDeclaredField("id_players");
		field.setAccessible(true);
		return (Map<Long, Player>) field.get(PlayerManager.getInstance());
	}
}