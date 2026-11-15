package cn.game.util;

import org.junit.jupiter.api.Assertions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 跨平台 JVM 虚拟时钟与 DateUtil/WeekSequenceUtils/MonthSequenceUtils 工具类联动收敛测试
 *
 * @author shuyongqiang
 */
public class DateUtilVirtualClockTest {

	@BeforeEach
	@AfterEach
	public void tearDown() {
		DateUtil.resetTimeOffset();
	}

	@Test
	public void testVirtualClockConvergence() {
		// 1. 初始状态断言
		Assertions.assertEquals(0L, DateUtil.getTimeOffsetMillis());
		long sysNow = System.currentTimeMillis();
		long utilNow = DateUtil.currentTimeMillis();
		Assertions.assertTrue(Math.abs(utilNow - sysNow) <= 100L);

		// 2. 设置虚拟时间偏移 +1 小时 (3600000 毫秒)
		long offset = 3600000L;
		DateUtil.setTimeOffsetMillis(offset);
		Assertions.assertEquals(offset, DateUtil.getTimeOffsetMillis());

		long virtualNow = DateUtil.currentTimeMillis();
		long expectedNow = System.currentTimeMillis() + offset;
		Assertions.assertTrue(Math.abs(virtualNow - expectedNow) <= 100L);

		int virtualSeconds = DateUtil.currentTimeSeconds();
		Assertions.assertEquals((int) (virtualNow / 1000), virtualSeconds);

		// 3. 基础时间对象生成
		LocalDateTime nowDateTime = DateUtil.nowDateTime();
		LocalDate nowDate = DateUtil.nowDate();
		LocalTime nowTime = DateUtil.nowTime();
		Date nowDateObj = DateUtil.nowDateObj();

		Assertions.assertNotNull(nowDateTime);
		Assertions.assertNotNull(nowDate);
		Assertions.assertNotNull(nowTime);
		Assertions.assertNotNull(nowDateObj);
		Assertions.assertEquals(nowDate, nowDateTime.toLocalDate());
		Assertions.assertEquals(nowTime.getHour(), nowDateTime.toLocalTime().getHour());
		Assertions.assertTrue(Math.abs(nowDateObj.getTime() - virtualNow) <= 100L);

		// 4. 收敛的常用方法验证
		String dateStr = DateUtil.getStringDate();
		Assertions.assertNotNull(dateStr);
		Assertions.assertFalse(dateStr.isEmpty());

		String nowDateStr = DateUtil.nowDateStr();
		Assertions.assertEquals(nowDate.toString(), nowDateStr);

		String nowTimeStr = DateUtil.nowTimeStr();
		Assertions.assertNotNull(nowTimeStr);

		int dayCustom = DateUtil.getDayCustom();
		Assertions.assertTrue(dayCustom >= 0);

		int day = DateUtil.getDay();
		Assertions.assertTrue(day >= 0);

		long day5Timestamp = DateUtil.getDayHourTimestamp(5);
		Assertions.assertTrue(day5Timestamp > 0);

		long nextDayTime = DateUtil.nextDayStartTime(1);
		Assertions.assertTrue(nextDayTime > virtualNow);

		int nextDayTimeSec = DateUtil.nextDayStartTimeSecond(1);
		Assertions.assertTrue(nextDayTimeSec > virtualSeconds);
		Assertions.assertEquals((int) (nextDayTime / 1000), nextDayTimeSec);

		// 5. 跨周与跨月工具类联动验证
		int currentWeekIndex = WeekSequenceUtils.getCurrentWeekIndex();
		Assertions.assertTrue(currentWeekIndex >= 0);

		LocalDate pastDate = nowDate.minusWeeks(2);
		Assertions.assertFalse(WeekSequenceUtils.isSameWeekWithNow(pastDate));
		Assertions.assertTrue(WeekSequenceUtils.isCrossWeekWithNow(pastDate));
		Assertions.assertEquals(2, WeekSequenceUtils.diffWeeksWithNow(pastDate));

		int currentMonthIndex = MonthSequenceUtils.getCurrentMonthIndex();
		Assertions.assertTrue(currentMonthIndex >= 0);

		LocalDate pastMonthDate = nowDate.minusMonths(3);
		Assertions.assertFalse(MonthSequenceUtils.isSameMonthWithNow(pastMonthDate));
		Assertions.assertTrue(MonthSequenceUtils.isCrossMonthWithNow(pastMonthDate));
		Assertions.assertEquals(3, MonthSequenceUtils.diffMonthsWithNow(pastMonthDate));

		// 6. 增量偏移与重置
		long addedOffset = DateUtil.addTimeOffsetMillis(1800000L); // 再加 30 分钟
		Assertions.assertEquals(5400000L, addedOffset);
		Assertions.assertEquals(5400000L, DateUtil.getTimeOffsetMillis());

		DateUtil.resetTimeOffset();
		Assertions.assertEquals(0L, DateUtil.getTimeOffsetMillis());
	}
}
