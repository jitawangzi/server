package cn.game.util;

import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**   
 * 
 * 2016-7-8 下午4:46:08
 * @author SYQ
 */
public final class DateUtil {
	/** 固定的起始日期,判断天数、周数等，避免跨年、月等问题 */
	private static final LocalDate DATE_START = LocalDate.of(2025, 1, 1);

	/** yyyy年MM月dd日 HH时mm分ss秒 */
	public static final String pattern_zh = "yyyy年MM月dd日 HH时mm分ss秒";
	/** yyyy-MM-dd HH:mm:ss  默认的日期格式**/
	public static final String pattern_en = "yyyy-MM-dd HH:mm:ss";

	public static final String pattern_day = "yyyy-MM-dd";
	public static final String pattern_time = "HH:mm:ss";

	public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern_en);
	public static final DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern(pattern_time);
	public static final DateTimeFormatter formatterEnYMD = DateTimeFormatter.ofPattern(pattern_day);
	// 一天的毫秒数 60*60*1000*24
	public final static long DAY_MILLIS = 86400000;
	// 一天的秒数 60*60*24
	public final static long DAY_SECONDS = 86400;
	// 一小时的毫秒数 60*60*1000
	public final static long HOUR_MILLIS = 3600000;
	// 一分钟的毫秒数 60*1000
	public final static long MINUTE_MILLIS = 60000;
	// 一秒钟的毫秒数 1*1000
	public final static long SECOND_MILLIS = 1000;

	/** 以凌晨5点为分界点 */
	private static final int DAY_BOUNDARY_HOUR = 5;

	/**
	 * 将时间字符串解析为 Date 对象
	 */
	/** 
	 * 
	 * @param textDate
	 * @return
	 */
	public static Date parseDate(String textDate) {
		LocalDateTime localDateTime = LocalDateTime.parse(textDate, formatter);
		Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	public static LocalDateTime parse(String textDate) {
		return LocalDateTime.parse(textDate, formatter);
	}

	/***
	 * 将 Date 转换为指定格式字符串
	 */
	public static String getTimeByPattern(Date time, String pattern) {
		if (time == null || pattern == null)
			return null;

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		LocalDateTime localDateTime = toLocalDateTime(time);
		return localDateTime.format(formatter);
	}


	/**
	 * 计算时间差
	 * 
	 * @param unit
	 *            ,时间单位
	 * @param date1
	 *            ,开始时间
	 * @param date2
	 *            ,结束时间
	 * @return
	 * @throws ParseException
	 */

	public static long howLong(TimeUnit unit, Date date1, Date date2) {
		return howLong(unit, date1.getTime(), date2.getTime());
	}
	public static long howLong(TimeUnit unit, long time1, long time2) {
		long ltime = Math.abs(time1 - time2);

		switch (unit) {
			case MILLISECONDS:
				return ltime;// 返回毫秒
			case SECONDS:
				return ltime / 1000;// 返回秒
			case MINUTES:
				return ltime / 60000;// 返回分钟
			case HOURS:
				return ltime / 3600000;// 返回小时
			case DAYS:
				return ltime / 86400000;// 返回天数

			default:
				break;
		}
		return -1;
	}

	/**
	 * 获取当前时间字符串
	 */
	public static String getStringDate() {
		return LocalDateTime.now().format(formatter);
	}


	/**
	 * 将时间字符串转为毫秒时间戳
	 */
	public static long getLongDate(String dateString) {
		LocalDateTime localDateTime = LocalDateTime.parse(dateString, formatter);
		return toEpochMilli(localDateTime);
	}
	
	/**
	 * 获取当前时间 n天后0点的毫秒时间戳
	 * 
	 * @param days 天数
	 * @return 毫秒时间戳
	 */
	public static long nextDayStartTime(int days) {
		return nextDayStartTime(System.currentTimeMillis(), days);
	}

	/** 
	 * 获取当前时间 n天后0点的秒时间戳
	 * 
	 * @param days 天数
	 * @return 秒时间戳
	 */
	public static int nextDayStartTimeSecond(int days) {
		return (int) (nextDayStartTime(System.currentTimeMillis(), days) / 1000);
	}

	/** 
	 * n天后的0点开始时间戳
	 * 
	 * @param startTime 开始时间戳（毫秒）
	 * @param days 天数
	 * @return 毫秒时间戳
	 */
	public static long nextDayStartTime(long startTime, int days) {
		// 使用系统默认时区（北京时间）
		ZoneId zoneId = ZoneId.systemDefault();

		// 转换时间戳为本地时间
		Instant instant = Instant.ofEpochMilli(startTime);
		LocalDateTime specificDateTime = LocalDateTime.ofInstant(instant, zoneId);

		// 计算目标日期
		LocalDate targetDate = specificDateTime.toLocalDate().plusDays(days);

		// 设置时间为 0 点 0 分 0 秒
		LocalDateTime targetDateTime = targetDate.atStartOfDay();

		// 转换回时间戳
		return targetDateTime.atZone(zoneId).toInstant().toEpochMilli();
	}

	/**
	 * 获取当天指定时间的时间戳
	 * @param hourOffset 0--23 小时
	 * @param minuteOffset 0--59 分钟
	 * @param secondOffset 0--59 秒
	 * @return 毫秒时间戳
	 */
	public static long getDayTimeBySet(int hourOffset, int minuteOffset, int secondOffset) {
		LocalDate today = LocalDate.now();
		LocalDateTime dateTime = today.atTime(hourOffset, minuteOffset, secondOffset);
		return toEpochMilli(dateTime);
	}

	/**
	 * 获取指定时间那天的指定时间的时间戳
	 * @param timer 毫秒时间戳
	 * @param hourOffset 0--23 小时
	 * @param minuteOffset 0--59 分钟
	 * @param secondOffset 0--59 秒
	 * @return 设置后的时间戳（毫秒）
	 */
	public static long getTimeBySet(long timer, int hourOffset, int minuteOffset, int secondOffset) {
		// 先把毫秒时间戳转为 LocalDateTime
		LocalDateTime dateTime = Instant.ofEpochMilli(timer).atZone(ZoneId.systemDefault()).toLocalDateTime();
		// 设置时分秒
		LocalDateTime newDateTime = dateTime.withHour(hourOffset).withMinute(minuteOffset).withSecond(secondOffset).withNano(0); // 清零纳秒
		return toEpochMilli(newDateTime);
	}

	/**
	 * 获取指定日期时间的天数（相对于起始日期）
	 * 以凌晨5点为一天的开始
	 */
	public static int getDayCustom(LocalDateTime dateTime) {
		return (int) ChronoUnit.DAYS.between(DATE_START, adjustToBusinessDay(dateTime));
	}

	/**
	 * 获取当前的天数（相对于起始日期）
	 * 以凌晨5点为一天的开始
	 */
	public static int getDayCustom() {
		return getDayCustom(LocalDateTime.now());
	}

	/** 
	 * 获取当前天数（相对于起始日期）
	 * @return
	 */
	public static int getDay() {
		return (int) ChronoUnit.DAYS.between(DATE_START, LocalDate.now());
	}

	/**
	 * 获取指定日期的天数（相对于起始日期）
	 */
	public static int getDayNumber(LocalDate date) {
		return (int) ChronoUnit.DAYS.between(DATE_START, date);
	}

	/**
	 * 获取指定日期的周数（相对于起始日期）
	 */
	public static int getWeek() {
		return (int) ChronoUnit.WEEKS.between(DATE_START, LocalDate.now());
	}
	
	/** 
	 * 获取指定日期的月数（相对于起始日期）
	 * @return
	 */
	public static int getMonth() {
		return (int) ChronoUnit.MONTHS.between(DATE_START, LocalDate.now());
	}

	/**
	 * 将日期时间调整为业务日期
	 * 如果时间在凌晨5点前，认为属于前一天
	 */
	private static LocalDateTime adjustToBusinessDay(LocalDateTime dateTime) {
		if (dateTime.getHour() < DAY_BOUNDARY_HOUR) {
			return dateTime.minusDays(1).withHour(DAY_BOUNDARY_HOUR).withMinute(0).withSecond(0).withNano(0);
		}
		return dateTime.withHour(DAY_BOUNDARY_HOUR).withMinute(0).withSecond(0).withNano(0);
	}

	/**
	 * 判断两个日期时间是否在同一个业务日
	 */
	public static boolean isSameBusinessDay(LocalDateTime dateTime1, LocalDateTime dateTime2) {
		return getDayCustom(dateTime1) == getDayCustom(dateTime2);
	}

	public static long addWeekBeginTimer(int offsetWeek){
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime targetWeekStart = now.plusWeeks(offsetWeek)
				.with(DayOfWeek.MONDAY)
				.truncatedTo(ChronoUnit.DAYS);
		return targetWeekStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

	}

	/**
	 * 获取当前日期的字符串格式
	 * 
	 * 格式: yyyy-mm-dd
	 * 
	 * @return 当前日期的字符串格式
	 */
    public static String nowDateStr() {
		LocalDate now = LocalDate.now();
		return now.format(formatterEnYMD);
    }

	/**
	 * 判断当前时间是否在两个时间之内
	 * @param start
	 * @param end
	 * @return
	 */
	public static boolean between(Date start, Date end) {
		if (start == null) {
			return true;
		}
		Date now = new Date();
		if (now.after(start)) {
			if (end == null) {
				return true;
			}
			return now.before(end);
		}
		return false;
	}


	/**
	 * 获取当前时间的字符串格式
	 * 格式: HH:mm:ss
	 * @return 当前时间的字符串格式
	 */
	public static String nowTimeStr() {
		return LocalTime.now().format(formatterTime);
	}

	/** 
	 * 计算当前时间与特定时间之间相隔的天数（日期数）
	 * @param timeMillis
	 * @return
	 */
	public static int diffDays(long timeMillis) {
		return diffDays(toLocalDate(timeMillis), LocalDate.now());
	}

	/**
	 * 计算两个时间戳之间相隔的天数（日期数）
	 * @param t1 第一个时间戳
	 * @param t2 第二个时间戳
	 * @return 相差的天数
	 */
	public static int diffDays(long t1, long t2) {
		// 计算日期差
		return diffDays(toLocalDate(t1), toLocalDate(t2));
	}

	/**
	 * 计算两个日期之间的天数差,同一天返回0
	 * @param date1 第一个日期
	 * @param date2 第二个日期
	 * @return 相差的天数
	 */
	public static int diffDays(LocalDate date1, LocalDate date2) {
		return diffDays(date1, date2, ChronoUnit.DAYS);
	}

	/** 
	 * 计算两个日期之间的时间差,同一天返回0
	 * @param date1
	 * @param date2
	 * @param unit 时间单位
	 * @return
	 */
	public static int diffDays(LocalDate date1, LocalDate date2, ChronoUnit unit) {
		return Math.abs((int) unit.between(date1, date2));
	}

	public static int diff(LocalDateTime time1, LocalDateTime time2, ChronoUnit unit) {
	    return Math.abs((int) unit.between(time1, time2));
	}

	/** 
	 * 计算当前时间和特定时间之间相隔的时间差
	 * @param timeMillis
	 * @param unit
	 * @return
	 */
	public static int diff(long timeMillis, ChronoUnit unit) {
		return diff(toLocalDateTime(timeMillis), LocalDateTime.now(), unit);
	}

	/**
	 * 判断俩时间戳 是否为同一天
	 * @param t1
	 * @param t2
	 * @return true 是同一天 ； false 不是同一天
	 */
	public static boolean isSameDay(long t1, long t2){
		LocalDate date1 = toLocalDate(t1);
		LocalDate date2 = toLocalDate(t2);
		return date1.equals(date2);
	}


	/**
	 * 获取当天的指定小时 整分整秒的时间戳
	 * @param hour 小时（0-23）
	 * @return 毫秒时间戳
	 */
	public static long getDayHourTimestamp(int hour) {
		if (hour < 0 || hour > 23) {
			throw new IllegalArgumentException("Hour must be between 0 and 23");
		}
		// 使用现代的日期时间API
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime targetDateTime = now.toLocalDate().atTime(hour, 0, 0); // 设置为指定小时的整点

		return toEpochMilli(targetDateTime);
	}

	/**
	 * 获取指定日期的指定小时时间戳
	 * @param date 指定日期
	 * @param hour 小时（0-23）
	 * @return 毫秒时间戳
	 */
	public static long getDayHourTimestamp(LocalDate date, int hour) {
		if (hour < 0 || hour > 23) {
			throw new IllegalArgumentException("Hour must be between 0 and 23");
		}
		return toEpochMilli(date.atTime(hour, 0, 0));
	}

	/** 
	 * 计算当前时间与特定时间之间相隔的天数（日期数）
	 * @param dateTimeStr  "yyyy-MM-dd HH:mm:ss"  格式
	 * @return
	 */
	public static int diffDays(String dateTimeStr) {
		LocalDateTime specificDateTime = LocalDateTime.parse(dateTimeStr, formatter);
		LocalDate specificDate = specificDateTime.toLocalDate();
		LocalDate currentDate = LocalDate.now();
		return diffDays(specificDate, currentDate);
	}
	
	public static int currentTimeSeconds() {
		
		return (int) (System.currentTimeMillis() / 1000); 
	}

	public static long currentTimeMillis() {

		return System.currentTimeMillis();
	}

	/**
	 * 将LocalDateTime转换为时间戳
	 */
	public static long toEpochMilli(LocalDateTime localDateTime) {
		return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	/** 
	 * 将LocalDateTime转换为秒级时间戳
	 * @param localDateTime
	 * @return
	 */
	public static int toEpochSecond(LocalDateTime localDateTime) {
		return (int) localDateTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
	}

	/**
	 * 将时间戳转换为LocalDate
	 */
	public static LocalDate toLocalDate(long timestamp) {
		return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
	}

	/**
	 * 将时间戳转换为LocalDateTime
	 */
	public static LocalDateTime toLocalDateTime(long timestamp) {
		return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	private static LocalDateTime toLocalDateTime(Date date) {
		return toLocalDateTime(date.getTime());
	}

	private static LocalDate toLocalDate(Date date) {
		return toLocalDate(date.getTime());
	}

	/**
	 * 按 period*periodPass 秒修改 date
	 */
	public static Date changeDateByPeriod(Date date, int period, int periodPass) {
		if (periodPass == 0) {
			return date;
		}
		if (date == null) {
			return null;
		}
		// 1. Date转Instant
		Instant instant = date.toInstant();
		// 2. 加上 period*periodPass 秒
		instant = instant.plusSeconds((long) period * periodPass);
		// 3. 再转回Date
		return Date.from(instant);
	}
	public Date changeDateByPeriod2(Date date, int period, int periodPass) {
		if (periodPass == 0) {
			return date;
		}
		if (date == null) {
			return null;
		}
		date = new Date(date.getTime() + period * periodPass * 1000L);
		return date;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LocalDateTime dateTime = LocalDateTime.now(); // 当前 LocalDateTime

		// 转换为时间戳（毫秒）
		long timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		int nowWeek = DateUtil.getWeek();
		System.out.println(nowWeek);
	}
}
