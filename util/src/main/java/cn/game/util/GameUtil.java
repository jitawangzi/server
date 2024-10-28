package cn.game.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

/**    
 * 一些常用方法
 * 2022年4月12日 下午12:12:57
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
	 * 给数量做加成
	 * @param array 0：id 1：数量
	 * @param addition,加成值，除10000使用
	 * @return
	 */
	public static int[] arrayAddition(int[] array, int addition) {
		if (addition <= 0) {
			return array;
		}
		int[] ret = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			if (i % 2 == 0) {
				ret[i] = array[i];
			} else {
				ret[i] = (int) (array[i] * (1 + addition / 10000f));
			}
		}
		return ret;
	}

	/** 
	 * 给数量做加成
	 * @param array 0：id 1：数量
	 * @param addition
	 * @return
	 */
	public static int[][] arrayAddition(int[][] array, int addition) {
		if (addition <= 0) {
			return array;
		}
		int[][] ret = new int[array.length][array[0].length];
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				if (j % 2 == 0) {
					ret[i][j] = array[i][j];
				} else {
					ret[i][j] = (int) (array[i][j] * (1 + addition / 10000f));
				}
			}
		}
		return ret;
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
