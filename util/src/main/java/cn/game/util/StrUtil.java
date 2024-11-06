package cn.game.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

public class StrUtil {

	public static List<Integer> toList(String string) {
		List<Integer> list = new ArrayList<Integer>();
		if (string != null && string.length() > 0) {
			String[] split = string.split(",");
			for (String s : split) {
				list.add(Integer.parseInt(s));
			}
		}
		return list;
	}
	public static List<Long> toListLong(String string) {
		List<Long> list = new ArrayList<Long>();
		if (string != null && string.length() > 0) {
			String[] split = string.split(",");
			for (String s : split) {
				list.add(Long.parseLong(s));
			}
		}
		return list;
	}

	public static int[] toIntArray(String string) {
		return toIntArray(string, 10);
	}

	public static int[] toIntArray(String string, int radix) {
		int[] ret = null;
		if (string != null && string.length() > 0) {
			String[] split = string.split(",");
			ret = new int[split.length];
			for (int i = 0; i < split.length; i++) {
				ret[i] = Integer.parseInt(split[i], radix);
			}
		}
		return ret == null ? new int[] {} : ret;
	}
	
	public static List<String> toStrList(String string){
		List<String> list = new ArrayList<String>();
		if (string != null && string.length() > 0) {
			String[] split = string.split(",");
			for (String s : split) {
				list.add(s);
			}
		}
		return list;
	}
	
	public static Set<Integer> toSet(String string) {
		Set<Integer> set = new HashSet<Integer>();
		if (string != null && string.length() > 0) {
			String[] split = string.split(",");
			for (String s : split) {
				set.add(Integer.parseInt(s));
			}
		}
		return set;
	}
	public static Set<Long> toLongSet(String string) {
		Set<Long> set = new HashSet<>();
		if (string != null && string.length() > 0) {
			String[] split = string.split(",");
			for (String s : split) {
				set.add(Long.parseLong(s));
			}
		}
		return set;
	}
	public static Map<Integer, Integer> toMap(String string) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		if (string != null && string.length() > 0) {
			String[] split = string.split("\\|");
			for (String s : split) {
				String[] split2 = s.split(":");
				map.put(Integer.parseInt(split2[0]), Integer.parseInt(split2[1]));
			}
		}
		return map;
	}
	
	
	
	public static String entryToString(List<Entry<Integer, Integer>> list){
		StringBuilder sb = new StringBuilder();
		for (Entry<Integer, Integer> entry : list) {
			sb.append(entry.getKey()).append(":");
			sb.append(entry.getValue()).append("|");
		}
		return sb.toString();
	}
	
	public static List<Entry<Integer, Integer>> toEntryList(String string) {
		String[] split = string.split("\\|");
		List<Entry<Integer, Integer>> list = new ArrayList<>(split.length);
		for (int i = 0; i < split.length; i++) {
			String[] split1 = split[i].split(":", 2);
			list.add(new Entry<Integer, Integer>() {
				@Override
				public Integer setValue(Integer value) {
					return null;
				}

				@Override
				public Integer getValue() {
					return Integer.parseInt(split1[1]);
				}

				@Override
				public Integer getKey() {
					return Integer.parseInt(split1[0]);

				}
			});
		}
		return list;
	}

	// 字符串转数组List,如1:2:3|1:3:4|
	public static List<int[]> toArrList(String string) {
		List<int[]> list = new ArrayList<>();
		String[] split = string.split("\\|");

		for (String s : split) {
			String[] strArr = s.split(":");
			
			int[] arr = new int[strArr.length];
			for (int i = 0; i < strArr.length; i++) {
				arr[i] = Integer.parseInt(strArr[i]);
			}
			
			list.add(arr);
		}
		return list;
	}


	public static String toString(Map<Integer, Integer> map) {
		StringBuilder sb = new StringBuilder();
		for (Entry<Integer, Integer> element : map.entrySet()) {
			sb.append(element.getKey()).append(":").append(element.getValue());
			sb.append("|");
		}
		return sb.toString();
	}

	public static String tripletoString(List<Triple<Integer, Integer, Integer>> triple) {
		StringBuilder sb = new StringBuilder();
		triple.forEach(e -> {
			sb.append(e.first).append(":").append(e.second).append(":").append(e.third);
			sb.append("|");
		});
		return sb.toString();
	}

	public static String toString(List<Integer> list) {
		return StringUtils.join(list, ",");
	}
	public static String toStringLong(List<Long> list) {
		return StringUtils.join(list, ",");
	}
	public static String toString(int[] list) {
		StringBuilder buffer = new StringBuilder();
		for (int i : list) {
			buffer.append(i).append(",");
		}
		return buffer.toString();
	}
	public static String toStrString(List<String> list) {
		return StringUtils.join(list, ",");
	}
	public static String toString(Set<Integer> list) {
		return StringUtils.join(list, ",");
	}
	public static String longSetToString(Set<Long> list) {
		return StringUtils.join(list, ",");
	}

	
	public static String addListString(List<Integer> list, String addString) {
		if(addString.length() > 0) {
			return addString + ',' + toString(list);
		}
		
		return toString(list);
	}
	
	public static String toString(byte[] array) {
		return StringUtils.join(array, ',');
	}
	
	public static byte[] toByteArray(String str) {
		String[] arr=str.split(",");
		byte[] bArr=new byte[arr.length];
		int index=0;
		for (String s : arr) {
			bArr[index++]=(byte) Integer.parseInt(s);
		}
		return bArr;
	}
	
	/**
	 * 把字符串按|分隔为数组
	 * @param array
	 * @return
	 */
	public static int[] parseArray(String array) {
		if (StringUtils.isEmpty(array)) { 
			return new int[] {};
		}
		String[] split = array.split("\\|");
		int[] ret = new int[split.length];
		for (int i = 0; i < split.length; i++) {
			ret[i] = Integer.parseInt(split[i]);
		}
		return ret;
	}
	/**
	 * 把字符串解析为两个数组,例如： 10010:8|10011:6|10012:6 转换成
	 *              [[10010,10011,10012],[8,6,6]]
	 * @param 第一数组是id集合，第二个数组是权重集合
	 * @return
	 */
	public static int[][] parseArray2(String string) {
		if (StringUtils.isEmpty(string)) {
			return new int[][] {};
		}
		String[] array = string.split("\\|");
		int[] ret1 = new int[array.length];
		int[] ret2 = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			String[] split = array[i].split(":");
			ret1[i] = Integer.parseInt(split[0]);
			ret2[i] = Integer.parseInt(split[1]);
		}
		return new int[][] { ret1, ret2 };
	}
	
	public static <T> String toJsonStr(T t) {
		return JSONObject.toJSONString(t);
	}
	
	

	public static void main(String args[]) {
		String str="1:2:3|1:3:4|";

		List<int[]> arr=toArrList(str);
		for (int[] is : arr) {
			for (int  is2 : is) {
				System.out.println(is2);
			}
		}

	}

}
