package cn.game.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

/**    
 * 一些常用方法
 * @date 2022年4月12日 下午12:12:57
 * @author SYQ
 */
public class GameUtil {
	
	public static List<Long> transform(List<Integer> list) {
		return Lists.transform(list, r -> Long.valueOf(r));
	}
	public static int[] transformList(List<Integer> list) {
		int[] ret = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			ret[i] = list.get(i);
		}
		return ret;
	}
	public static List<Long> transform(int[] array) {
		List<Long> list = new ArrayList<>(array.length);
		for (int i = 0; i < array.length; i++) {
			list.add(Long.valueOf(array[i]));
		}
		return list;
	}
	
	public static List<Integer> transform1(int[] array) {
		List<Integer> list = new ArrayList<>(array.length);
		for (int i = 0; i < array.length; i++) {
			list.add(Integer.valueOf(array[i]));
		}
		return list;
	}
	
	public static long[] transformArray(int[] array) {
		long[] ret = new long[array.length];

		for (int i = 0; i < array.length; i++) {
			ret[i] = array[i];
		}
		return ret;
	}
	
	/** 是否包含 */
	public static boolean contains(int[] array, int o) {
		for (int i : array) {
			if (i == o) {
				return true;
			}
		}
		return false;
	}

	/** 
	 * 是不是同一个大版本
	 * @param version1
	 * @param version2
	 * @return
	 */
	public static boolean equalsVersion(String version1, String version2) {
		if (StringUtils.isEmpty(version1) || StringUtils.isEmpty(version2)) {
			return false;
		}
		return version1.substring(0, version1.lastIndexOf("."))
				.equals(version2.substring(0, version2.lastIndexOf(".")));
	}

}
