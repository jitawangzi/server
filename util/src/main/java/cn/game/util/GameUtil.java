package cn.game.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

/**    
 * 一些常用方法
 * 2022年4月12日 下午12:12:57
 * @author SYQ
 */
public class GameUtil {
	private static final int[] EMPTY_INT_ARRAY = new int[] {};
	
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

	/** 是否包含 */
	public static boolean contains(String[] array, String o) {
		for (String i : array) {
			if (i.equals(o)) {
				return true;
			}
		}
		return false;
	}
	public static boolean containsAll(List<Integer> list, int[] array) {
		if (list == null || list.isEmpty() || array == null || array.length == 0) {
			return false;
		}
		for (int i : array) {
			if (!list.contains(i)) {
				return false;
			}
		}
		return true;
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
	 * 给数量做倍数
	 * @param array 0：id 1：数量
	 * @param multiple 倍数
	 * @return
	 */
	public static int[] arrayMultiple(int[] array, int multiple) {
		if (multiple <= 1) {
			return array;
		}
		int[] ret = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			if (i % 2 == 0) {
				ret[i] = array[i];
			} else {
				ret[i] = array[i] * multiple;
			}
		}
		return ret;
	}

	/** 
	 * 给数量做倍数
	 * @param array 0：id 1：数量
	 * @param multiple 倍数
	 * @return
	 */
	public static int[][] arrayMultiple(int[][] array, int multiple) {
		if (multiple <= 1) {
			return array;
		}
		int[][] ret = new int[array.length][array[0].length];
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				if (j % 2 == 0) {
					ret[i][j] = array[i][j];
				} else {
					ret[i][j] = array[i][j] * multiple;
				}
			}
		}
		return ret;
	}

	public static int[] transformIdAndCount(List<Integer> idList, List<Integer> countList) {
		if (idList == null || countList == null) {
			return EMPTY_INT_ARRAY;
		}
		if (idList.size() != countList.size()) {
			throw new IllegalArgumentException("idList.size()!=countList.size()" + idList.size() + "  " + countList.size());
		}
		int[] ret = new int[idList.size() * 2];

		for (int i = 0; i < idList.size(); i++) {
			ret[i * 2] = idList.get(i);
			ret[i * 2 + 1] = countList.get(i);
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

	/**
	 * 从 drops 中减去 items 包含的道具数量
	 * @param drops 原始掉落奖励
	 * @param items 需要减去的道具数量
	 * @return
	 */
	public static int[][] subItems(int[][] drops, int[][] items) {
		int[][] newDrops = new int[drops.length][2];
		Map<Integer,Integer> dropMaps = new HashMap<>();
		for (int i = 0; i < drops.length; i++) {
			int itemId = drops[i][0];
			int itemNum = drops[i][1];
			dropMaps.put(itemId,itemNum);
		}
		for(int i = 0; i < items.length; i++) {
			int itemId = items[i][0];
			int itemNum = items[i][1];
			if (!dropMaps.containsKey(itemId)){
				continue;
			}
			dropMaps.put(itemId,Math.max(0,dropMaps.get(itemId) - itemNum));
		}
		for(int i = 0; i < drops.length; i++) {
			int itemId = drops[i][0];
			newDrops[i][0] = itemId;
			newDrops[i][1] = dropMaps.getOrDefault(itemId,0);
		}
		return newDrops;
	}
	/**
	 * 计算输入字符串的 MD5 十六进制结果
	 *
	 * @param input 输入字符串
	 * @return MD5 哈希的 16 进制字符串
	 */
	public static String md5Hex(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : digest) {
				sb.append(String.format("%02x", b & 0xff));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("MD5 算法不可用", e);
		}
	}
	/** 
	 * 解析server id，使用程序运行时参数或者环境变量设置的server id
	 * @param args
	 * @param serverType
	 * @return
	 */
	public static String parseServerId(String[] args, ServerType serverType) {
		String serverId = null;
		String serverIdKey = serverType.getServerIdKey();
		if (args.length == 0) {
			serverId = System.getProperty(serverIdKey);
			if (serverId == null) {
				serverId = System.getenv(serverIdKey);
			}
		} else {
			serverId = args[0];
		}
		if (serverId == null) {
			throw new IllegalArgumentException(serverType.name() + "没有设置 serverId, 请使用参数或者环境变量设置。");
		}
		System.setProperty(serverIdKey, serverId);
		return serverId;
	}

	/** 
	 * 获取一个数组中，大于0的元素个数
	 * @param array
	 * @return
	 */
	public static int length(int[] array) {
		if (array == null) {
			return 0;
		}
		int ret = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] > 0) {
				ret++;
			}
		}
		return ret;
	}

}
