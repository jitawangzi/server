package cn.game.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**   
 * @Description 
 * @date 2016-7-8 下午4:46:08
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
	 * @param timeStr
	 * @return
	 */
	public static String getTimeByPattern(Date time) {
		return getTimeByPattern(time, pattern_zh);
	}

	/***
	 * 将字符串时间转换成Pattern格式
	 * 
	 * @param timeStr
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
	 * @param timeStr
	 * @param pattern
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

	/***
	 * 返回当前月的第一天
	 * 
	 * @return , yyyyMMdd
	 */
	public static String getCurrentMonthOfStart() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.YEAR) + "" + (calendar.get(Calendar.MONTH) + 1) + "01";
	}

	/***
	 * 返回指定月的第一天
	 * 
	 * @return , yyyyMMdd
	 */
	public static String getMonthOfStart(int month) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, month);
		return calendar.get(Calendar.YEAR) + "" + (calendar.get(Calendar.MONTH) + 1) + "01";
	}

	/***
	 * 返回当前月的最后一天
	 * 
	 * @return, yyyyMMdd
	 */
	public static String getCurrentMonthOfEnd() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.YEAR) + "" + (calendar.get(Calendar.MONTH) + 1) + "" + calendar.get(Calendar.DAY_OF_MONTH);
	}

	/***
	 * 返回上月的第一天
	 * 
	 * @return , yyyyMMdd
	 */
	public static String getPreMonthOfStart() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
		return calendar.get(Calendar.YEAR) + "" + (calendar.get(Calendar.MONTH) + 1) + "01";
	}

	/***
	 * 返回上月的最后一天
	 * 
	 * @return, yyyyMMdd
	 */
	public static String getPreMonthOfEnd() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
		return calendar.get(Calendar.YEAR) + "" + (calendar.get(Calendar.MONTH) + 1) + "" + calendar.get(Calendar.DAY_OF_MONTH);
	}

	/***
	 * 返回下一个月份的总天数
	 * 
	 * @return
	 */
	public static int getNextMonthOfDays() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH + 1));
		return calendar.get(Calendar.DAY_OF_MONTH);
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

	/***
	 * 返回指定月的最后一天
	 * 
	 * @return, yyyyMMdd
	 */
	public static String getMonthOfEnd(int month) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, month);
		return calendar.get(Calendar.YEAR) + "" + (calendar.get(Calendar.MONTH) + 1) + "" + calendar.get(Calendar.DAY_OF_MONTH);
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
	 * 获取距离当前分钟
	 * 
	 * @param step
	 * @return
	 */
	public static String getNextMinute(int step) {
		String result = "";
		Calendar calendar = Calendar.getInstance();
		int min = calendar.get(Calendar.MINUTE);
		calendar.set(Calendar.MINUTE, min + step);
		result = getTimeByPattern(calendar.getTime(), "yyyy年MM月dd日HH时mm分");
		return result;
	}

	/**
	 * 获取距离当前时间
	 * 
	 * @param step
	 * @return
	 */
	public static String getNextHour(int step) {
		String result = "";
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		calendar.set(Calendar.HOUR_OF_DAY, hour + step);
		result = getTimeByPattern(calendar.getTime(), "yyyy年MM月dd日HH时");
		return result;
	}

	/**
	 * 获取下一天日期
	 * 
	 * @param step
	 * @return
	 */
	public static String getNextDay(int step) {
		String result = "";
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DAY_OF_MONTH, day + step);
		result = getTimeByPattern(calendar.getTime(), "yyyy年MM月dd日");
		return result;
	}

	public static String getNextDay2(int step) {
		String result = "";
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DAY_OF_MONTH, day + step);
		result = getTimeByPattern(calendar.getTime(), "yyyy-MM-dd");
		return result;
	}

	/**
	 * @Title: getDayByStep
	 * @Description: 返回离当前日期指定步长的日期
	 * @param step
	 * @return String 返回类型
	 */
	public static String getDayByStep(int step, String pattern) {
		String result = "";
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DAY_OF_MONTH, day - step);
		result = getTimeByPattern(calendar.getTime(), pattern);
		return result;
	}

	/**
	 * 获取下一个星期日期
	 * 
	 * @return
	 */
	public static String getNextWeek(int step) {
		String result = "";
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int dayOweek = calendar.get(Calendar.DAY_OF_WEEK);
		int diff = step - dayOweek;
		calendar.set(Calendar.DAY_OF_MONTH, day + diff);
		result = getTimeByPattern(calendar.getTime(), "yyyy年MM月dd日");
		return result;
	}

	/**
	 * 是否是周二
	 * 
	 * @return boolean true 是 false不是
	 */
	public static boolean isTuesday() {
		Calendar calendar = Calendar.getInstance();
		int dayOweek = calendar.get(Calendar.DAY_OF_WEEK);
		return dayOweek == Calendar.TUESDAY;
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
	 * 返回当前日期（一个月中）
	 * 
	 * @param time
	 * @return
	 */
	public static int getmonthofNow(Date time) {
		try {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(time);
			return calendar.get(Calendar.DAY_OF_MONTH);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return 0;
	}

	/**
	 * 返回当前时间戳后的指定时间戳
	 * 
	 * @param unit
	 * @param expires
	 * @return
	 */
	public static long getFutureTimeMillis(TimeUnit unit, long expires) {
		long futureTimeMillis = 0;
		futureTimeMillis = unit.toMillis(expires) + System.currentTimeMillis();
		return futureTimeMillis;
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
	 * @Title: getMinute
	 * @Description: 返回两时间戳之间的分钟数
	 * @param start
	 * @param end
	 * @return long 返回类型
	 */
	public static long getMinute(Date start, Date end) {
		long ret = 0;
		long time1 = start.getTime();
		long time2 = end.getTime();
		long remainingTime = time2 - time1;
		if (remainingTime > 0) {
			long days = remainingTime / DAY_MILLIS;
			long hours = (remainingTime % DAY_MILLIS) / HOUR_MILLIS;
			long minutes = (remainingTime % HOUR_MILLIS) / MINUTE_MILLIS;
			ret += days * 24 * 60;
			ret += hours * 60;
			ret += minutes;
		}
		return ret;
	}

	/**
	 * @Title: getLastSunday
	 * @Description: 取当前日期之前的星期日
	 * @return String 返回类型
	 */
	public static String getLastSunday() {
		String val = "";
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int dayOweek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		calendar.set(Calendar.DAY_OF_MONTH, day - dayOweek);
		val = getTimeByPattern(calendar.getTime(), "yyyyMMdd");
		return val;
	}

	/**
	 * 是否为星期六
	 * 
	 * @return
	 */
	public static boolean isSaturday() {
		Calendar calendar = Calendar.getInstance();
		int dayOweek = calendar.get(Calendar.DAY_OF_WEEK);
		return dayOweek == Calendar.SATURDAY;
	}

	/**
	 * 是否为星期天
	 * 
	 * @return
	 */
	public static boolean isSunDay() {
		Calendar calendar = Calendar.getInstance();
		int dayOweek = calendar.get(Calendar.DAY_OF_WEEK);
		return dayOweek == Calendar.SUNDAY;
	}

	/**
	 * @Description 距离一天的重置时间还有多少秒
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
	 * @Description 以当天的几点作为一天的分割点
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
	 * @Description 判断当前时间是否在两个时间之内
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

	public static int calcBetweenDays(Date startDate, Date endDate) {
		if (startDate != null && endDate != null) {
			Date startDate0AM = getAM0Date(startDate);
			Date endDate0AM = getAM0Date(endDate);
			long v1 = startDate0AM.getTime() - endDate0AM.getTime();
			BigDecimal bd1 = new BigDecimal(v1);
			BigDecimal bd2 = new BigDecimal(86400000L);
			return Math.abs((int) bd1.divide(bd2, 0, 0).doubleValue());
		} else {
			return 0;
		}
	}

	public static Date getAM0Date(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

//		int day = getDay(1);
//		System.out.println(day);
		System.out.println(parse("2202-03-05 13:02:00"));

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
