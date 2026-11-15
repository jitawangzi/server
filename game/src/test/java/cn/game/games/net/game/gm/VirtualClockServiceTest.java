package cn.game.games.net.game.gm;

import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import cn.game.games.core.event.server.ServerEventBus;
import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.core.exception.LogicException;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.util.DateUtil;

/**
 * 跨平台 JVM 虚拟时钟内核、持久化、倒退门禁与跨周期结算测试 (TC-B06-TIME-SETTLEMENT, TC-B07-BACKWARD-GUARD-PERSIST)
 *
 * @author shuyongqiang
 */
@Isolated
public class VirtualClockServiceTest {

	private Path testStatePath;
	private RunMode originalRunMode;

	@BeforeEach
	public void setUp() throws Exception {
		testStatePath = Paths.get("target/test-virtual-clock-" + System.currentTimeMillis() + ".properties");
		Files.deleteIfExists(testStatePath);

		Field runModeField = ServerContext.getInstance().getClass().getDeclaredField("runMode");
		runModeField.setAccessible(true);
		originalRunMode = (RunMode) runModeField.get(ServerContext.getInstance());
		runModeField.set(ServerContext.getInstance(), RunMode.DEV);

		if (ServerContext.getInstance().getEventBus() == null) {
			ServerContext.getInstance().setEventBus(ServerEventBus.getInstance());
		}
		DateUtil.resetTimeOffset();
		VirtualClockService.getInstance().setStateFilePath(testStatePath);
	}

	@AfterEach
	public void tearDown() throws Exception {
		DateUtil.resetTimeOffset();
		if (testStatePath != null) {
			Files.deleteIfExists(testStatePath);
		}
		if (originalRunMode != null) {
			Field runModeField = ServerContext.getInstance().getClass().getDeclaredField("runMode");
			runModeField.setAccessible(true);
			runModeField.set(ServerContext.getInstance(), originalRunMode);
		}
	}

	@Test
	public void testOneStopCrossBoundarySettlement() {
		// TC-B06-TIME-SETTLEMENT
		AtomicInteger sysTimeChangeEvents = new AtomicInteger();
		AtomicInteger newDayEvents = new AtomicInteger();
		AtomicInteger newWeekEvents = new AtomicInteger();
		AtomicInteger newMonthEvents = new AtomicInteger();

		ServerContext.getInstance().registerEventHandler(ServerEventTypeEnum.SystemTimeChange, e -> sysTimeChangeEvents.incrementAndGet());
		ServerContext.getInstance().registerEventHandler(ServerEventTypeEnum.NewDay, e -> newDayEvents.incrementAndGet());
		ServerContext.getInstance().registerEventHandler(ServerEventTypeEnum.NewWeek, e -> newWeekEvents.incrementAndGet());
		ServerContext.getInstance().registerEventHandler(ServerEventTypeEnum.NewMonth, e -> newMonthEvents.incrementAndGet());

		// 1. 设置 +1d 偏移触发跨天
		VirtualClockService.VirtualClockResult res1 = VirtualClockService.getInstance().applyOffset("+1d", false, false);
		Assertions.assertTrue(res1.isSuccess());
		Assertions.assertTrue(res1.isCrossedDay());
		Assertions.assertTrue(DateUtil.getTimeOffsetMillis() >= 86400000L - 1000L);
		Assertions.assertTrue(sysTimeChangeEvents.get() >= 1);
		Assertions.assertTrue(newDayEvents.get() >= 1);

		// 2. 验证文本兼容命令 time add 7d
		VirtualClockService.VirtualClockResult res2 = VirtualClockService.getInstance().executeLegacyCommand("add", "7d", false, false);
		Assertions.assertTrue(res2.isSuccess());
		Assertions.assertTrue(res2.getOffsetMillis() >= 8 * 86400000L - 1000L);
		Assertions.assertTrue(newWeekEvents.get() >= 1);

		// 3. 验证文本命令 status
		VirtualClockService.VirtualClockStatus status = VirtualClockService.getInstance().getStatus();
		Assertions.assertNotNull(status);
		Assertions.assertTrue(status.getOffsetMillis() >= 8 * 86400000L - 1000L);
		Assertions.assertNotNull(status.getVirtualTimeText());
		Assertions.assertNotNull(status.getPhysicalTimeText());
	}

	@Test
	public void testBackwardGuardAndStatePersistence() throws Exception {
		// TC-B07-BACKWARD-GUARD-PERSIST
		// 1. 正向快进 2 小时并持久化
		VirtualClockService.VirtualClockResult res1 = VirtualClockService.getInstance().applyOffset("+2h", false, false);
		Assertions.assertTrue(res1.isSuccess());
		Assertions.assertEquals(7200000L, DateUtil.getTimeOffsetMillis());
		Assertions.assertTrue(Files.exists(testStatePath));

		// 2. 未设置 allowBackward 尝试倒退 1 小时 -> 拒绝
		LogicException ex1 = Assertions.assertThrows(LogicException.class, () -> {
			VirtualClockService.getInstance().applyOffset("-1h", false, false);
		});
		Assertions.assertEquals(ErrorMsgEnum.gm_cmd_param.ID, ex1.getErrorCode());
		Assertions.assertEquals(7200000L, DateUtil.getTimeOffsetMillis());

		// 3. 显式 allowBackward=true 倒退 1 小时 -> 允许并附带 warning
		VirtualClockService.VirtualClockResult res2 = VirtualClockService.getInstance().applyOffset("-1h", true, false);
		Assertions.assertTrue(res2.isSuccess());
		Assertions.assertEquals(3600000L, DateUtil.getTimeOffsetMillis());
		Assertions.assertNotNull(res2.getWarnings());
		Assertions.assertFalse(res2.getWarnings().isEmpty());
		Assertions.assertTrue(res2.getWarnings().get(0).contains("虚拟时钟已向后回退"));

		// 4. 重启恢复：清零内存后调用 initOnStartup 恢复 3600000L
		DateUtil.resetTimeOffset();
		Assertions.assertEquals(0L, DateUtil.getTimeOffsetMillis());
		VirtualClockService.getInstance().initOnStartup();
		Assertions.assertEquals(3600000L, DateUtil.getTimeOffsetMillis());

		// 5. 损坏文件容错降级
		Files.writeString(testStatePath, "offsetMillis=NOT_A_NUMBER");
		DateUtil.resetTimeOffset();
		VirtualClockService.getInstance().initOnStartup();
		Assertions.assertEquals(0L, DateUtil.getTimeOffsetMillis());

		// 6. 生产环境硬阻断
		Field runModeField = ServerContext.getInstance().getClass().getDeclaredField("runMode");
		runModeField.setAccessible(true);
		runModeField.set(ServerContext.getInstance(), RunMode.PRODUCTION);

		LogicException prodEx = Assertions.assertThrows(LogicException.class, () -> {
			VirtualClockService.getInstance().applyOffset("+1h", true, false);
		});
		Assertions.assertEquals(ErrorMsgEnum.production_gm_not_allow.ID, prodEx.getErrorCode());
		Assertions.assertEquals(0L, DateUtil.getTimeOffsetMillis());
	}
}
