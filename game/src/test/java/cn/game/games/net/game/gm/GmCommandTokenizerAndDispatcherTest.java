package cn.game.games.net.game.gm;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.tools.JavaCompiler;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.exception.LogicException;
import cn.game.core.execute.DeadlockGuard;
import cn.game.core.execute.MailboxProcessor;
import cn.game.core.execute.SaveMailboxFixture;
import cn.game.core.execute.TaskExecutorService;
import cn.game.core.net.process.IdVirtualThreadProcessor;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.game.gm.command.PlayerGm;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.manual.ErrorMsgEnum;

/**
 * GM分词器、反射参数绑定、邮箱队列隔离与异常隔离单元测试
 *
 * @author shuyongqiang
 */
@Isolated
public class GmCommandTokenizerAndDispatcherTest {

	@BeforeAll
	static void prepareClassMetadata() {
		ClassManager.getInstance().init();
	}

	enum SampleMode {
		FAST,
		SAFE
	}

	@GmGroup(name = "testunit", description = "测试专用GM分组")
	static class SampleGmGroup {

		final AtomicBoolean sawWriteScope = new AtomicBoolean(false);
		final AtomicLong executionMailboxId = new AtomicLong(-1L);
		final AtomicInteger counter = new AtomicInteger(0);
		final AtomicBoolean globalSawWriteScope = new AtomicBoolean(false);
		final AtomicBoolean globalSawMailbox = new AtomicBoolean(false);
		GmDispatcher dispatcherRef;

		@GmAction(name = "echo", aliases = { "echoalias" }, description = "测试参数绑定与邮箱作用域", security = GmSecurityTier.OPS_SAFE, scope = GmScope.PLAYER, sideEffects = "仅测试计数", idempotent = false, example = "testunit.echo 7 hello SAFE")
		public Map<String, Object> echo(Player player,
				@GmParam(description = "数量", ref = "ShopItem") int count,
				@GmParam(description = "标签", required = false, defaultValue = "default-tag") String label,
				@GmParam(description = "模式", required = false, defaultValue = "SAFE") SampleMode mode) {
			sawWriteScope.set(GmDispatcher.isCurrentMailboxPlayer(player.getPlayerId()));
			Long guardId = MailboxProcessor.currentEntityId();
			executionMailboxId.set(guardId != null ? guardId : -1L);
			counter.incrementAndGet();
			return Map.of("playerId", player.getPlayerId(), "count", count, "label", label, "mode", mode.name());
		}

		@GmAction(name = "boom", description = "模拟异常指令", security = GmSecurityTier.OPS_SAFE, scope = GmScope.PLAYER)
		public void boom(Player player) {
			throw new IllegalStateException("simulated-boom");
		}

		@GmAction(name = "nested", description = "测试同玩家邮箱重入不死锁", security = GmSecurityTier.OPS_SAFE, scope = GmScope.PLAYER)
		public GmResult nested(Player player, int value) {
			return dispatcherRef.dispatchText(player, "testunit.echo " + value + " \"nested call\" fast");
		}

		@GmAction(name = "globalprobe", description = "测试GLOBAL作用域不耦合PlayerWriteGuard", security = GmSecurityTier.OPS_SAFE, scope = GmScope.GLOBAL, idempotent = true)
		public String globalProbe(GmExecContext ctx, String note) {
			globalSawWriteScope.set(GmDispatcher.isCurrentMailboxPlayer(ctx.getPlayerId()));
			globalSawMailbox.set(MailboxProcessor.currentEntityId() != null || DeadlockGuard.getCurrentId() != null);
			return "global:" + note;
		}
	}

	@GmGroup(name = "twoitem", description = "两段式命令路由测试分组")
	static class TwoSegmentItemGm {

		@GmAction(name = "twoitem", description = "同前缀主命令", security = GmSecurityTier.OPS_SAFE, scope = GmScope.PLAYER)
		public String add(Player player, int id, int count) {
			return "add:" + id + ":" + count;
		}

		@GmAction(name = "itemdel", description = "同分组子命令", security = GmSecurityTier.OPS_SAFE, scope = GmScope.PLAYER)
		public String del(Player player, int id, int count) {
			return "del:" + id + ":" + count;
		}
	}

	@GmGroup(name = "conflict", description = "冲突测试分组")
	static class ConflictingGmGroup {

		@GmAction(name = "dup", aliases = { "echoalias" }, security = GmSecurityTier.OPS_SAFE, scope = GmScope.GLOBAL)
		public String dup() {
			return "dup";
		}
	}

	@Test
	void verifyTokenizerBindingAndQueueErrorIsolation() throws Exception {
		List<String> tokens = GmCommandTokenizer.tokenize(
				"testunit.echo 12 \"hello \\\"gm\\\" world\" 'it\\'s quoted' \"\"");
		Assertions.assertEquals(
				List.of("testunit.echo", "12", "hello \"gm\" world", "it's quoted", ""),
				tokens);
		Assertions.assertThrows(IllegalArgumentException.class, () -> GmCommandTokenizer.tokenize("item \"unclosed"));

		SampleGmGroup sampleGm = new SampleGmGroup();
		GmDispatcher conflictDispatcher = new GmDispatcher(null);
		conflictDispatcher.registerBean(sampleGm);
		Assertions.assertThrows(IllegalStateException.class, () -> conflictDispatcher.registerBean(new ConflictingGmGroup()));

		// 校验缺失 -parameters 编译期形参名且未显式标注 @GmParam(name) 时启动期快速失败
		assertMissingParametersFailFast();

		// 校验 PlayerGm#time 参数解析与缺失参数防护（严禁触发 System.exit(2)）
		Assertions.assertArrayEquals(new String[] { "set", "2026-05-01 04:59:55" },
				PlayerGm.resolveTimeArgs("set", "2026-05-01 04:59:55"));
		Assertions.assertArrayEquals(new String[] { "set", "2026-05-01 04:59:55" },
				PlayerGm.resolveTimeArgs("set 2026-05-01 04:59:55", null));
		Assertions.assertArrayEquals(new String[] { "status" }, PlayerGm.resolveTimeArgs("status", null));
		LogicException missingSetVal = Assertions.assertThrows(LogicException.class, () -> PlayerGm.resolveTimeArgs("set", null));
		Assertions.assertEquals(ErrorMsgEnum.gm_cmd_param.getId(), missingSetVal.getErrorCode());
		LogicException missingAddVal = Assertions.assertThrows(LogicException.class, () -> PlayerGm.resolveTimeArgs("add", " "));
		Assertions.assertEquals(ErrorMsgEnum.gm_cmd_param.getId(), missingAddVal.getErrorCode());

		TaskExecutorService executor = SaveMailboxFixture.create(16);
		IdVirtualThreadProcessor processor = new IdVirtualThreadProcessor(executor);
		long playerId = 9001001L;
		Player player = new Player();
		player.setPlayerId(playerId);
		String previousMode = System.getProperty(ServerContext.SERVER_RUN_MODE);
		Field deadlockEnabledField = DeadlockGuard.class.getDeclaredField("enabled");
		deadlockEnabledField.setAccessible(true);
		boolean prevDeadlockEnabled = deadlockEnabledField.getBoolean(null);
		System.setProperty(ServerContext.SERVER_RUN_MODE, "TEST");
		ServerContext.getInstance().setRunMode();
		PlayerManager.getInstance().initAdd(player);
		try {
			GmDispatcher dispatcher = new GmDispatcher(processor);
			sampleGm.dispatcherRef = dispatcher;
			dispatcher.registerBean(sampleGm);
			dispatcher.registerBean(new TwoSegmentItemGm());

			// 校验 Schema 补齐字段（tier/sideEffects/idempotent/example/ref/options）
			GmSchemaCatalog schemaCatalog = dispatcher.getSchemaCatalog(RunMode.TEST, "testunit");
			GmActionSchema echoSchema = schemaCatalog.getActions().stream()
					.filter(a -> "testunit.echo".equals(a.getId()))
					.findFirst()
					.orElseThrow();
			Assertions.assertEquals(GmSecurityTier.OPS_SAFE, echoSchema.getTier());
			Assertions.assertEquals("仅测试计数", echoSchema.getSideEffects());
			Assertions.assertFalse(echoSchema.isIdempotent());
			Assertions.assertEquals("testunit.echo 7 hello SAFE", echoSchema.getExample());
			Assertions.assertEquals("ShopItem", echoSchema.getParams().get(0).getRef());
			Assertions.assertEquals(List.of("FAST", "SAFE"), echoSchema.getParams().get(2).getOptions());

			GmResult ok = dispatcher.dispatchText(player, "echoalias 7 \"hello world\" fast");
			Assertions.assertTrue(ok.isOk(), () -> "expected ok but got: " + ok.getSummary());
			Assertions.assertEquals("1.0", ok.getSchemaVersion());
			Assertions.assertEquals(0, ok.getErrorCode());
			Assertions.assertTrue(sampleGm.sawWriteScope.get(), "必须在 PlayerWriteGuard 作用域内执行");
			Assertions.assertEquals(playerId, sampleGm.executionMailboxId.get(), "必须在对应玩家邮箱线程执行");
			@SuppressWarnings("unchecked")
			Map<String, Object> data = (Map<String, Object>) ok.getData();
			Assertions.assertEquals(7, data.get("count"));
			Assertions.assertEquals("hello world", data.get("label"));
			Assertions.assertEquals("FAST", data.get("mode"));

			// 两段式命令优先命中 twoitem.itemdel，不被同名 short 命令 twoitem 抢占
			GmResult twoSegment = dispatcher.dispatchText(player, "twoitem itemdel 1001 5");
			Assertions.assertTrue(twoSegment.isOk());
			Assertions.assertEquals("del:1001:5", twoSegment.getData());

			// GLOBAL 作用域即使传入在线玩家也不进入 PlayerWriteGuard 与邮箱锁
			GmResult globalRes = dispatcher.dispatchText(player, "testunit.globalprobe ok");
			Assertions.assertTrue(globalRes.isOk());
			Assertions.assertEquals("global:ok", globalRes.getData());
			Assertions.assertFalse(sampleGm.globalSawWriteScope.get(), "GLOBAL动作不应进入PlayerWriteGuard");
			Assertions.assertFalse(sampleGm.globalSawMailbox.get(), "GLOBAL动作不应进入玩家邮箱线程");

			GmResult defaulted = dispatcher.dispatchText(player, "testunit.echo 3");
			Assertions.assertTrue(defaulted.isOk());
			@SuppressWarnings("unchecked")
			Map<String, Object> defaultData = (Map<String, Object>) defaulted.getData();
			Assertions.assertEquals("default-tag", defaultData.get("label"));
			Assertions.assertEquals("SAFE", defaultData.get("mode"));

			GmResult unknown = dispatcher.dispatchText(player, "not_existing_cmd 1 2");
			Assertions.assertFalse(unknown.isOk());
			Assertions.assertEquals(ErrorMsgEnum.gm_cmd_not_exist.getId(), unknown.getErrorCode());

			GmResult badParam = dispatcher.dispatchText(player, "testunit.echo notAnInt");
			Assertions.assertFalse(badParam.isOk());
			Assertions.assertEquals(ErrorMsgEnum.gm_cmd_param.getId(), badParam.getErrorCode());

			// 模拟 PRODUCTION 下 DeadlockGuard 关闭时的同玩家邮箱重入，验证 MailboxProcessor.currentEntityId() 防止自死锁
			deadlockEnabledField.setBoolean(null, false);
			GmResult nested = dispatcher.dispatchText(player, "testunit.nested 99");
			Assertions.assertTrue(nested.isOk());

			GmResult boom = dispatcher.dispatchText(player, "testunit.boom");
			Assertions.assertFalse(boom.isOk());
			GmResult afterBoom = dispatcher.dispatchText(player, "testunit.echo 5");
			Assertions.assertTrue(afterBoom.isOk());
		} finally {
			deadlockEnabledField.setBoolean(null, prevDeadlockEnabled);
			PlayerManager.getInstance().deletePlayer(player);
			PlayerManager.getInstance().getAllPlayer().remove(playerId, player);
			if (previousMode == null) {
				System.clearProperty(ServerContext.SERVER_RUN_MODE);
			} else {
				System.setProperty(ServerContext.SERVER_RUN_MODE, previousMode);
			}
			ServerContext.getInstance().setRunMode();
			processor.close();
		}
	}

	private void assertMissingParametersFailFast() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		Assertions.assertNotNull(compiler, "JDK JavaCompiler 必须可用");
		Path tempDir = Files.createTempDirectory("gm-noparam-test");
		try {
			String source = """
					package cn.game.games.net.game.gm.tmp;
					import cn.game.games.net.game.gm.GmAction;
					import cn.game.games.net.game.gm.GmGroup;
					import cn.game.games.net.game.gm.GmScope;
					import cn.game.games.net.game.gm.GmSecurityTier;
					@GmGroup(name = "noparam", description = "无parameters测试")
					public class NoParamGmBean {
						@GmAction(name = "ping", security = GmSecurityTier.OPS_SAFE, scope = GmScope.GLOBAL)
						public String ping(int amount) {
							return String.valueOf(amount);
						}
					}
					""";
			SimpleJavaFileObject fileObj = new SimpleJavaFileObject(
					URI.create("string:///cn/game/games/net/game/gm/tmp/NoParamGmBean.java"),
					javax.tools.JavaFileObject.Kind.SOURCE) {
				@Override
				public CharSequence getCharContent(boolean ignoreEncodingErrors) {
					return source;
				}
			};
			Boolean compiled = compiler.getTask(
					null,
					null,
					null,
					List.of("-d", tempDir.toAbsolutePath().toString()),
					null,
					List.of(fileObj)).call();
			Assertions.assertTrue(Boolean.TRUE.equals(compiled), "动态测试类应编译成功");
			try (URLClassLoader loader = new URLClassLoader(new URL[] { tempDir.toUri().toURL() }, getClass().getClassLoader())) {
				Class<?> cls = loader.loadClass("cn.game.games.net.game.gm.tmp.NoParamGmBean");
				Object bean = cls.getDeclaredConstructor().newInstance();
				GmDispatcher dispatcher = new GmDispatcher(null);
				Assertions.assertThrows(IllegalStateException.class, () -> dispatcher.registerBean(bean));
			}
		} finally {
			try (var walk = Files.walk(tempDir)) {
				walk.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
					try {
						Files.deleteIfExists(p);
					} catch (Exception e) {
						System.err.println("清理临时编译目录失败: " + p + ", " + e.getMessage());
					}
				});
			}
		}
	}
}