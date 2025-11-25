package cn.game.util;
import java.time.LocalDate;
import java.time.temporal.WeekFields;

/**
 * 周序列工具类：
 * 把“周基年 + 第几周”转换为一个可比较的“周序号”，
 * 用于判断是否同一周 / 是否跨周 / 相差多少周。
 *
 * 基于 ISO 周：周一为一周第一天。
 */
public class WeekSequenceUtils {

    private static final WeekFields ISO_WEEK_FIELDS = WeekFields.ISO;

    /**
     * 获取指定日期的周基年（week-based year）
     */
    public static int getWeekYear(LocalDate date) {
        return date.get(ISO_WEEK_FIELDS.weekBasedYear());
    }

    /**
     * 获取指定日期是该周基年的第几周
     */
    public static int getWeekOfYear(LocalDate date) {
        return date.get(ISO_WEEK_FIELDS.weekOfWeekBasedYear());
    }

    /**
     * 获取指定日期的“全局周序号”
     * 编码：weekYear * 100 + weekOfYear
     *
     * 例如：
     *  2024 年第 1 周 -> 202401
     *  2024 年第 10 周 -> 202410
     *  2025 年第 1 周 -> 202501
     */
    public static int getWeekIndex(LocalDate date) {
        int weekYear = getWeekYear(date);
        int weekOfYear = getWeekOfYear(date);
        return weekYear * 100 + weekOfYear;
    }

    /**
     * 获取当前日期的“全局周序号”
     */
    public static int getCurrentWeekIndex() {
        return getWeekIndex(LocalDate.now());
    }

    /**
     * 判断两个日期是否在同一自然周（ISO 周：周一开始）
     */
    public static boolean isSameWeek(LocalDate d1, LocalDate d2) {
        return getWeekIndex(d1) == getWeekIndex(d2);
    }

    /**
     * 判断当前日期与指定日期是否在同一自然周
     */
    public static boolean isSameWeekWithNow(LocalDate other) {
        return isSameWeek(LocalDate.now(), other);
    }

    /**
     * 判断两个日期是否跨周（即不在同一自然周）
     */
    public static boolean isCrossWeek(LocalDate d1, LocalDate d2) {
        return !isSameWeek(d1, d2);
    }

    /**
     * 判断当前日期与指定日期是否跨周
     */
    public static boolean isCrossWeekWithNow(LocalDate other) {
        return isCrossWeek(LocalDate.now(), other);
    }

    /**
     * 计算两个日期之间相差多少周（基于周序号，整数，可正可负）
     *
     * 注意：这里是用 weekIndex 相减，
     * 不会逐日精确到“相差多少个 7 天”，
     * 而是按“周编号”的差值：
     *   2024年第1周 vs 2024年第3周 -> 2
     */
    public static int diffWeeks(LocalDate from, LocalDate to) {
        return getWeekIndex(to) - getWeekIndex(from);
    }

    /**
     * 计算指定日期距离当前日期相差多少周（基于周编号）
     */
    public static int diffWeeksWithNow(LocalDate other) {
        return diffWeeks(other, LocalDate.now());
    }
}