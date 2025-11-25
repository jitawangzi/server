package cn.game.util;
import java.time.LocalDate;

/**
 * 月份序列工具类：
 * 把年月转换为一个全局递增的“月序号”，方便比较和计算间隔。
 */
public class MonthSequenceUtils {

    /**
     * 获取指定日期的“全局月序号”
     * 计算规则：year * 12 + (month - 1)
     *
     * 例如：
     *  2024-01 -> 2024 * 12 + 0
     *  2024-12 -> 2024 * 12 + 11
     *  2025-01 -> 2025 * 12 + 0
     */
    public static int getMonthIndex(LocalDate date) {
        return date.getYear() * 12 + (date.getMonthValue() - 1);
    }

    /**
     * 获取当前日期的“全局月序号”
     */
    public static int getCurrentMonthIndex() {
        return getMonthIndex(LocalDate.now());
    }

    /**
     * 判断两个日期是否在同一自然月
     * （通过全局月序号判断，天然区分年份）
     */
    public static boolean isSameMonth(LocalDate d1, LocalDate d2) {
        return getMonthIndex(d1) == getMonthIndex(d2);
    }

    /**
     * 判断当前日期与指定日期是否在同一月
     */
    public static boolean isSameMonthWithNow(LocalDate other) {
        return isSameMonth(LocalDate.now(), other);
    }

    /**
     * 判断两个日期是否跨月（即不在同一自然月）
     */
    public static boolean isCrossMonth(LocalDate d1, LocalDate d2) {
        return !isSameMonth(d1, d2);
    }

    /**
     * 判断当前日期与指定日期是否跨月
     */
    public static boolean isCrossMonthWithNow(LocalDate other) {
        return isCrossMonth(LocalDate.now(), other);
    }

    /**
     * 计算两个日期之间相差多少个月（整数，可正可负）
     * 例如：
     *   2024-01 vs 2024-03 -> 2
     *   2024-12 vs 2025-01 -> 1
     */
    public static int diffMonths(LocalDate from, LocalDate to) {
        return getMonthIndex(to) - getMonthIndex(from);
    }

    /**
     * 计算指定日期距离当前日期相差多少个月
     */
    public static int diffMonthsWithNow(LocalDate other) {
        return diffMonths(other, LocalDate.now());
    }
}