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

	public static String[] transformToStringArray(List<Long> list) {
		String[] ret = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			ret[i] = String.valueOf(list.get(i));
		}
		return ret;
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
	 * 按万分比对数量进行缩放（正数=增加，负数=减少）
	 * @param array 二维数组：每行 [id, 数量, id, 数量, ...]
	 * @param rate 万分比，正数增加，负数减少，例如 500 表示 +5%，-250 表示 -2.5%
	 * @return 新数组（不会修改入参）
	 */
	public static int[][] arrayZoomBy10k(int[][] array, int rate) {
		if (array == null || array.length == 0)
			return array;
		if (array[0] == null || array[0].length == 0)
			return array;

		int rows = array.length;
		int cols = array[0].length;
		int[][] ret = new int[rows][cols];

		final float factor = 1f + rate / 10000f;

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < array[i].length; j++) {
				if ((j & 1) == 0) {
					ret[i][j] = array[i][j]; 
				} else {
					// 计算时用 double 提高精度，最后再截断/四舍五入
					double scaled = array[i][j] * (double) factor;
					// 数量不能为负，去掉小数部分
					long rounded =  (long) Math.max(0,scaled);
					ret[i][j] = (int) rounded;
				}
			}
		}
		return ret;
	}

	/** 
	 * 给数量做增加
	 * @param array 0：id 1：数量
	 * @param addition
	 * @param multiple
	 * @return
	 */
	public static int[][] arrayAddition(int[][] array, int[] addition, int multiple) {
		if (addition == null || addition.length == 0 || multiple <= 0) {
			return array;
		}
		int[][] ret = new int[array.length][array[0].length];
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				if (j % 2 == 0) {
					ret[i][j] = array[i][j];
				} else {
					ret[i][j] = array[i][j] + addition[i] * multiple;
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
	 * @param array 0 类型  1：id 2：数量
	 * @param multiple 倍数
	 * @return
	 */
	public static int[] arrayMultiple3(int[] array, int multiple) {
		if (multiple <= 1) {
			return array;
		}
		int[] ret = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			if (i % 3 == 0 || i % 3 == 1) {
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

	/** 
	 * 合并两个数组，result中相同id的数量加上add中的数量
	 * 需要确保id的顺序是一致的,例如： 
	 * arr1 
		202002;10
		202003;10
		202004;20
		202005;20
		
		arr2
		202002;30
		202003;30
	 * @param result 结果
	 * @param add  增加的数据（不可变类型）
	 * @return
	 */
	public static int[][] fastMergeAddPrefix(int[][] result, int[][] add) {
		if (add == null || add.length == 0)
			return result;
		if (result == null || result.length == 0) {
			// 返回add的深拷贝
			int[][] ret = new int[add.length][2];
			for (int i = 0; i < add.length; i++) {
				ret[i][0] = add[i][0];
				ret[i][1] = add[i][1];
			}
			return ret;
		}

		int m = result.length, n = add.length;

		// result更长或等长，则直接原地合并前n项
		if (m >= n) {
			for (int i = 0; i < n; i++) {
				if (result[i][0] != add[i][0]) {
					throw new IllegalArgumentException("ID mismatch at index " + i);
				}
				result[i][1] += add[i][1];
			}
			return result;
		}

		// add更长，需分配新空间，合并前m项，其余部分拷贝add
		int[][] ret = new int[n][2];
		// 合并前m项
		for (int i = 0; i < m; i++) {
			if (result[i][0] != add[i][0]) {
				throw new IllegalArgumentException("ID mismatch at index " + i);
			}
			ret[i][0] = result[i][0];
			ret[i][1] = result[i][1] + add[i][1];
		}
		// 拷贝add剩余部分
		for (int i = m; i < n; i++) {
			ret[i][0] = add[i][0];
			ret[i][1] = add[i][1];
		}
		return ret;
	}

	/** 
	 * 合并两个数组，result中相同id的数量加上add中的数量
	 * @param result 结果
	 * @param add  增加的数据（不可变类型）
	 * @return
	 */
	public static int[][] mergeAdd(int[][] result, int[][] add) {
		if (add == null || add.length == 0)
			return result;
		if (result == null || result.length == 0) {
			// 返回add的深拷贝
			int[][] ret = new int[add.length][2];
			for (int i = 0; i < add.length; i++) {
				ret[i][0] = add[i][0];
				ret[i][1] = add[i][1];
			}
			return ret;
		}

		int m = result.length, n = add.length;
		int i = 0, j = 0;

		// 先判断是否需要扩容
		boolean needExpand = false;
		while (i < m && j < n) {
			if (result[i][0] == add[j][0]) {
				i++;
				j++;
			} else if (result[i][0] < add[j][0]) {
				i++;
			} else {
				// add中有result没有的id，且还没遍历完result
				needExpand = true;
				break;
			}
		}
		// 如果add有剩余，也说明有新增的id
		if (j < n)
			needExpand = true;

		if (!needExpand) {
			// 直接原地合并
			i = 0;
			j = 0;
			while (i < m && j < n) {
				if (result[i][0] == add[j][0]) {
					result[i][1] += add[j][1];
					i++;
					j++;
				} else if (result[i][0] < add[j][0]) {
					i++;
				} else {
					// 理论不会到达
					j++;
				}
			}
			return result;
		}

		// 扩容合并
		int[][] ret = new int[m + n][2];
		i = 0;
		j = 0;
		int k = 0;
		while (i < m && j < n) {
			if (result[i][0] == add[j][0]) {
				ret[k][0] = result[i][0];
				ret[k][1] = result[i][1] + add[j][1];
				i++;
				j++;
			} else if (result[i][0] < add[j][0]) {
				ret[k][0] = result[i][0];
				ret[k][1] = result[i][1];
				i++;
			} else {
				ret[k][0] = add[j][0];
				ret[k][1] = add[j][1];
				j++;
			}
			k++;
		}
		// 剩余部分
		while (i < m) {
			ret[k][0] = result[i][0];
			ret[k][1] = result[i][1];
			i++;
			k++;
		}
		while (j < n) {
			ret[k][0] = add[j][0];
			ret[k][1] = add[j][1];
			j++;
			k++;
		}
		// 截取有效长度
		int[][] finalRet = new int[k][2];
		for (int t = 0; t < k; t++) {
			finalRet[t][0] = ret[t][0];
			finalRet[t][1] = ret[t][1];
		}
		return finalRet;
	}

	/**
	 * 合并两个数组：对相同id进行数量减少，最少减到0；不生成负数条目；
	 * 若某条数量为0，保留该条。
	 * 输入要求：result 与 sub 按 id 升序且 id 唯一。
	 * @param result 已有数据
	 * @param sub    要减少的数据（不可变类型）
	 * @return 合并后的数组（按id升序）
	 */
	public static int[][] mergeSubtractFloorZeroKeepZero(int[][] result, int[][] sub) {
		if (sub == null || sub.length == 0)
			return result;
		if (result == null || result.length == 0) {
			// result为空，且不允许负数，不生成sub-only条目
			return new int[0][2];
		}

		int m = result.length, n = sub.length;
		int i = 0, j = 0;

		// 不需要扩容：因为不新增id（sub-only忽略），长度至多不变
		// 可原地更新 result，再返回 result 即可。
		while (i < m && j < n) {
			int idR = result[i][0];
			int idS = sub[j][0];
			if (idR == idS) {
				int after = result[i][1] - sub[j][1];
				result[i][1] = after > 0 ? after : 0; // 夹到0
				i++;
				j++;
			} else if (idR < idS) {
				i++;
			} else {
				// sub-only，忽略（不能生成负数条目）
				j++;
			}
		}
		return result;
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
		return version1.substring(0, version1.lastIndexOf(".")).equals(version2.substring(0, version2.lastIndexOf(".")));
	}

	/**
	 * 从 drops 中减去 items 包含的道具数量
	 * @param drops 原始掉落奖励
	 * @param items 需要减去的道具数量
	 * @return
	 */
	public static int[][] subItems(int[][] drops, int[][] items) {
		int[][] newDrops = new int[drops.length][2];
		Map<Integer, Integer> dropMaps = new HashMap<>();
		for (int i = 0; i < drops.length; i++) {
			int itemId = drops[i][0];
			int itemNum = drops[i][1];
			dropMaps.put(itemId, itemNum);
		}
		for (int i = 0; i < items.length; i++) {
			int itemId = items[i][0];
			int itemNum = items[i][1];
			if (!dropMaps.containsKey(itemId)) {
				continue;
			}
			dropMaps.put(itemId, Math.max(0, dropMaps.get(itemId) - itemNum));
		}
		for (int i = 0; i < drops.length; i++) {
			int itemId = drops[i][0];
			newDrops[i][0] = itemId;
			newDrops[i][1] = dropMaps.getOrDefault(itemId, 0);
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

	public static int[] getArrayCost(int[][] array, int count) {
		return count >= array.length ? array[array.length - 1] : array[count];
	}

}
