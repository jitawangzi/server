package cn.game.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**   
 * 
 * 2016-7-8 下午4:46:08
 * @author SYQ
 */
public final class DateUtil {
	/** yyyy年MM月dd日 HH时mm分ss秒 */
	public static final String pattern_zh = "yyyy年MM月dd日 HH时mm分ss秒";
	/** yyyy-MM-dd HH:mm:ss  默认的日期格式**/
	public static final String pattern_en = "yyyy-MM-dd HH:mm:ss";

	public static final String pattern_en_yyyy_MM_dd = "yyyy-MM-dd";

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

	private static ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected synchronized SimpleDateFormat initialValue() {
			return new SimpleDateFormat(pattern_en);
		}
	};

	public static DateFormat getDateFormat() {
		return threadLocal.get();
	}

	public static Date parse(String textDate) {
		try {
			return getDateFormat().parse(textDate);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("时间格式错误： " + textDate);
		}
	}

	/***
	 * 将字符串时间转换成 yyyy年MM月dd日 HH时mm分ss秒 格式
	 * 
	 * @return
	 */
	public static String getTimeByPattern(Date time) {
		return getTimeByPattern(time, pattern_zh);
	}

	/***
	 * 将字符串时间转换成Pattern格式
	 * 
	 * @param pattern
	 * @return
	 */
	public static String getTimeByPattern(Date time, String pattern) {

		try {
			SimpleDateFormat format = new SimpleDateFormat(pattern);
			return getTimeByPattern(time, format);
		} catch (Exception e) {

			e.printStackTrace();
		}

		return null;
	}

	/***
	 * 将字符串时间转换成Pattern格式
	 * 
	 * @return
	 */
	public static String getTimeByPattern(Date time, SimpleDateFormat format) {
		synchronized (format) {
			try {
				return format.format(time);
			} catch (Exception e) {

				e.printStackTrace();
			}
			return null;
		}
	}

	/***
	 * 将字符串时间转换成 yyyy年MM月dd日 HH:mi 格式
	 * 
	 * @param timeStr
	 *            ,格式 yyyymmdd hh24:mi
	 * @return
	 */
	public static String timeStrToCn(String timeStr) {
		StringBuffer sb = new StringBuffer();
		if (timeStr.length() > 4)
			sb.append(timeStr.substring(0, 4)).append("年");
		else
			return null;
		if (timeStr.length() > 6)
			sb.append(timeStr.substring(4, 6)).append("月");
		else
			return sb.toString();
		if (timeStr.length() > 8)
			sb.append(timeStr.substring(6, 8)).append("日");
		else
			return sb.toString();
		if (timeStr.length() > 11)
			sb.append(timeStr.substring(9));

		return sb.toString();
	}

	/**
	 * 获取当前月的总天数
	 * @return
	 */
	public static int getNowMonthHowDays() {
		return getDaysOfMonth(now());
	}
	
	/**
	 * 获取某个日期所在月的总天数
	 * @param date
	 * @return
	 */
	public static int getDaysOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	} 

	/**
	 * 计算时间差 (时间单位,开始时间,结束时间)
	 * 调用方法 howLong("h","2007-08-09 10:22:26","2007-08-09 20:21:30") ///9小时56分
	 * 返回9小时
	 */
	public static long howLong(TimeUnit unit, String time1, String time2) throws ParseException {
		// 时间单位(如：不足1天(24小时) 则返回0)，开始时间，结束时间
		// System.out.println("time1=" + time1 + " time2=" + time2);
		Date date1 = getDateFormat().parse(time1);
		Date date2 = getDateFormat().parse(time2);
		return howLong(unit, date1, date2);
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
	 * @param dateStr
	 * @return
	 */
	public static Date getDate(String dateStr, String pattern) {
		try {
			return new SimpleDateFormat(pattern).parse(dateStr);
		} catch (ParseException e) {

			return null;
		}
	}

	/**
	 * @param dateStr
	 * @return
	 */
	public static Date getDate(String dateStr) {
		try {
			return new SimpleDateFormat(pattern_en).parse(dateStr);
		} catch (ParseException e) {

			return null;
		}
	}

	public static String getStringDate() {
		return getDateFormat().format(new Date());

	}

	public static long getLongDate(String dateString) throws ParseException {
		Date parse = getDateFormat().parse(dateString);
		return parse.getTime();

	}

	/**
	 * 获取当前时间 n天后0点的毫秒时间戳
	 * 
	 * @return
	 */
	public static long nextDayStartTime(int days) {
		return nextDayStartTime(System.currentTimeMillis(), days);
	}

	/** 
	 * 获取当前时间 n天后0点的秒时间戳
	 * @return
	 */
	public static int nextDayStartTimeSecond(int days) {

		return (int) (nextDayStartTime(System.currentTimeMillis(), days) / 1000);
	}

	public static long nextDayStartTime(long startTime, int days) {
		Instant instant = Instant.ofEpochMilli(startTime);
		LocalDateTime specificDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
		LocalDate targetDate = specificDateTime.toLocalDate().plusDays(days);
		// 设置时间为 0 点 0 分 0 秒
		LocalDateTime targetDateTime = targetDate.atStartOfDay();
		// 获取时间戳（秒）
		long timestamp = targetDateTime.toEpochSecond(ZoneOffset.UTC);
		return timestamp * 1000;
	}

	/**
	 * 得到当前星期数
	 * 
	 * @return 如周一返回1
	 */
	public static int getDayOfWeek() {
		Calendar calendar = Calendar.getInstance();
		int today = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		if (today == 0) {
			today = 7;
		}
		return today;
	}

	/**
	 * 将毫秒级的timeMillis转化成格式为（HH:mm:ss）的字符串
	 * 
	 * @param timeMillis
	 * @param type
	 * @return type=0 返回HH:mm:ss格式 <br>
	 *         type!=0 返回 x小时m分钟s秒格式
	 */
	public static String getTimeLongStr(long timeMillis, int type) {
		// 秒
		int second = (int) (timeMillis % 60000) / 1000;
		// 分
		int minute = (int) (timeMillis % (60000 * 60)) / 60000;
		// 小时
		int hour = (int) (timeMillis / (60000 * 60));
		if (type == 0) {
			return getDoubleNum(hour) + ":" + getDoubleNum(minute) + ":" + getDoubleNum(second);
		} else {
			String time = "";
			if (hour > 0) {
				time += hour + " 小时";
			}
			if (minute > 0) {
				time += minute + " 分钟";
			}
			if (second > 0) {
				time += second + " 秒";
			}
			return time;
		}

	}

	public static String getDoubleNum(int num) {
		if (num < 10) {
			return "0" + num;
		} else {
			return "" + num;
		}
	}

	/**
	 * 距离一天的重置时间还有多少秒
	 * @param hour  以某个小时为重置点
	 * @return
	 */
	public static int getDayResetTimeSeconds(int hour) {

		Calendar calendar = Calendar.getInstance();
		int h = calendar.get(Calendar.HOUR_OF_DAY);
		if (h > hour) {
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		return (int) (calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000;
	}



	/**
	 * 获取当天 指定时间的时间戳
	 * @param hourOffset 0--23 小时
	 * @param minuteOffset 0--59 分钟
	 * @param secondOffset 0--59 秒
	 * @return
	 */
	public static long getDayTimeBySet(int hourOffset,int minuteOffset, int secondOffset){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY,hourOffset);
		calendar.set(Calendar.MINUTE,minuteOffset);
		calendar.set(Calendar.SECOND,secondOffset);
		return calendar.getTimeInMillis();
	}

	/**
	 * 获取指定时间那天的指定时间的时间戳
	 * @param hourOffset 0--23 小时
	 * @param minuteOffset 0--59 分钟
	 * @param secondOffset 0--59 秒
	 * @return
	 */
	public static long getTimeBySet(long timer ,int hourOffset,int minuteOffset, int secondOffset){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timer);
		calendar.set(Calendar.HOUR_OF_DAY,hourOffset);
		calendar.set(Calendar.MINUTE,minuteOffset);
		calendar.set(Calendar.SECOND,secondOffset);
		return calendar.getTimeInMillis();
	}

	/**
	 * 以当天的几点作为一天的分割点
	 * @param hour
	 * @return
	 */
	public static int getDay(int hour) {
		Calendar calendar = Calendar.getInstance();
		int h = calendar.get(Calendar.HOUR_OF_DAY);
		if (h >= hour) {
			return calendar.get(Calendar.DAY_OF_YEAR);
		}
		return calendar.get(Calendar.DAY_OF_YEAR) - 1;
	}

	public static int getDay() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.DAY_OF_YEAR);
	}
	/**今天是当前月的第几天*/
	public static int getDayOfMonth() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	public static int getHour() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.HOUR_OF_DAY);
	}

	public static int getWeek() {
		Calendar calendar = Calendar.getInstance();
		int week = calendar.get(Calendar.WEEK_OF_YEAR);
		return week;
	}
	
	public static int getMonth() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH);
	}

	public static long addWeekBeginTimer(int offsetWeek){
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime targetWeekStart = now.plusWeeks(offsetWeek)
				.with(DayOfWeek.MONDAY)
				.truncatedTo(ChronoUnit.DAYS);
		return targetWeekStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

	}

	/**当前年份*/
	public static int getYear() {
		Calendar calendar = Calendar.getInstance(); 
		int year = calendar.get(Calendar.YEAR);
		return year;
	}

	/**
	 * 获取当前日期的字符串格式
	 * 
	 * 格式: yyyy-mm-dd
	 * 
	 * @return 当前日期的字符串格式
	 */
	public static String nowDateStr() {

		try {
			SimpleDateFormat format = new SimpleDateFormat(pattern_en_yyyy_MM_dd);
			return format.format(now());
		} catch (Exception e) {
			return "";
		}
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
	 * 
	 * 格式: hh24:mi:ss
	 * 
	 * @return 当前日期的字符串格式
	 */
	public static String nowTimeStr() {
		// return ConvertUtil.time2str(now());
		Calendar c = Calendar.getInstance();
		int h = c.get(Calendar.HOUR_OF_DAY);
		int m = c.get(Calendar.MINUTE);
		int s = c.get(Calendar.SECOND);

		String sh = h < 10 ? "0" + h : "" + h;
		String sm = m < 10 ? "0" + m : "" + m;
		String ss = s < 10 ? "0" + s : "" + s;
		return sh + ":" + sm + ":" + ss;

	}

	/**
	 * 获取当前日期
	 * 
	 * @return 当前日期
	 */
	public static Date now() {
		Calendar calendar = Calendar.getInstance();
		return calendar.getTime();
	}
	
	/**
	 * 获取当天的指定小时 整分整秒的时间戳
	 * @param hour
	 * @return
	 */
	public static long getDayHourTimestamp(int hour) {
		long current = System.currentTimeMillis();
		long zero = current/(DAY_MILLIS)*(DAY_MILLIS) - TimeZone.getDefault().getRawOffset();
		long newTime = zero + hour * HOUR_MILLIS + DAY_MILLIS;
		return newTime;
	}
	
	public static int getStamp() {
		return (int) (System.currentTimeMillis()/1000);
	}

	/** 
	 * 计算当前时间与特定时间之间相隔的天数（日期数）
	 * @param timeMillis
	 * @return
	 */
	public static int diffDays(long timeMillis) {
		return diffDays(timeMillis, System.currentTimeMillis());
	}

	/** 
	 * 计算两个时间戳之间相隔的天数（日期数）
	 * @param t1
	 * @param t2
	 * @return
	 */
	public static int diffDays(long t1, long t2) {

		LocalDate d1 = Instant.ofEpochMilli(t1).atZone(ZoneOffset.UTC).toLocalDate();
		LocalDate d2 = Instant.ofEpochMilli(t2).atZone(ZoneOffset.UTC).toLocalDate();
		return (int) ChronoUnit.DAYS.between(d1, d2);
	}

	/**
	 * 判断俩时间戳 是否为同一天
	 * @param t1
	 * @param t2
	 * @return true 是同一天 ； false 不是同一天
	 */
	public static boolean isSameDay(long t1, long t2){
		return diffDays(t1,t2) == 0;
	}


	/** 
	 * 计算当前时间与特定时间之间相隔的天数（日期数）
	 * @param dateTimeStr  "yyyy-MM-dd HH:mm:ss"  格式
	 * @return
	 */
	public static int diffDays(String dateTimeStr) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern_en);
		LocalDateTime specificDateTime = LocalDateTime.parse(dateTimeStr, formatter);
		LocalDate specificDate = specificDateTime.toLocalDate();
		LocalDate currentDate = LocalDate.now();
		return (int) ChronoUnit.DAYS.between(specificDate, currentDate);
	}

	
	public static int currentTimeSeconds() {
		
		return (int) (System.currentTimeMillis() / 1000); 
	}

	public static long currentTimeMillis() {

		return System.currentTimeMillis();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long addWeek = addWeekBeginTimer(2);
        System.out.println(addWeek);
		System.out.println(diffDays(System.currentTimeMillis() - DAY_MILLIS));

		// System.out.println(DateUtil.getTimeByPattern(new Date()));
		// System.out.println(DateUtil.timeStrToCn("20081212 22:22"));
		// Calendar calendar = Calendar.getInstance();
		// int day = calendar.get(Calendar.DAY_OF_MONTH);
		// calendar.set(Calendar.DAY_OF_MONTH, day + 30);
		// String result = getTimeByPattern(calendar.getTime(),"yyyy年MM月dd日");
		// System.out.println("result : " + result);
		// System.out.println("millis : " + calendar.getTimeInMillis());
		// System.out.println("millis : " + getFutureTimeMillis(TimeUnit.DAYS,
		// 30));
		// System.out.println(getDayResetTimeSeconds(5));

	}
}
