package cn.game.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;


/**
 * @ClassName: Rnd
 * @Description: 随机数生成帮助类
 * @author luopeihuai luopeihuai@126.com
 * 2010-12-17 下午02:02:48
 */
public final class Rnd {
	private static final ThreadLocal<Random> threadLocalRandom = ThreadLocal.withInitial(Random::new);

	private static Random getRandom() {
		return threadLocalRandom.get();
	}
//	private static final Random random = new Random();
	private static final int RANDOM_CONST = 10000;
	/** 按百分比随机 */
	public static final int RANDOM_PERCENTAGE_CONST = 100;

	/** 最大随机次数 */
	public static final int RANDOM_MAX = 500;

	/**
	 * @Title: get 
	 * @Description: 随机生成一个0到1(不包括)的数 
	 * @return double 返回类型 
	 * @throws
	 */
	public static final double get() {
		return getRandom().nextDouble();
	}

	/**
	 * @Title: get
	 * @Description: 得到一个 min <= x <= max的随机数
	 * @param min
	 * @param max
	 * @return int 返回类型 @throws
	 */
	public static final int get(int min, int max) {
		return min + (int) Math.floor(getRandom().nextDouble() * (max - min + 1));
	}

	/**
	 * 得到一个 min <= x <= max的随机奇数
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public static final int getOdd(int min, int max) {
		if (max % 2 == 0)
			--max;
		if (min % 2 == 0)
			++min;
		return min + 2 * (int) (Math.random() * ((max - min) / 2 + 1));
	}

	/**
	 * @Title: nextInt
	 * @Description: 得到一个 0 到 n-1的随机数
	 * @param n
	 * @return int 返回类型 @throws
	 */
	public static final int nextInt(int n) {
		return (int) Math.floor(getRandom().nextDouble() * n);
	}
	
	/** 
	 * 返回一个 >= min, < max 的随机数
	 * @param min
	 * @param max
	 * @return
	 */
	public static final int nextInt(int min, int max) {
		return (int) Math.floor(min + getRandom().nextDouble() * (max - min));
	}

	/**
	 * @Title: nextFloat
	 * @Description: 得到一个 (0 -f] 的随机数
	 * @param n
	 * @return float 返回类型 @throws
	 */
	public static final float nextFloat(float f) {
		return (float) (getRandom().nextDouble() * f);
	}

	/**
	 * @Title: nextInt 
	 * @Description: 随机生成一个整形数 
	 * @return int 返回类型 
	 * @throws
	 */
	public static final int nextInt() {
		return getRandom().nextInt();
	}

	public static final long nextLong() {
		return getRandom().nextLong();
	}

	/**
	 * @Title: nextDouble 
	 * @Description: 生成一个双精度值 0.0d（包括）到 1.0d（不包括） 
	 * @return
	 *         double 返回类型 
	 * @throws
	 */
	public static final double nextDouble() {
		return getRandom().nextDouble();
	}

	/**
	 * @Title: nextDouble 
	 * 生成一个双精度值 ,from（包括）到 to（包括）
	 * @return
	 *         double 返回类型 
	 * @throws
	 */
	public static final double nextDouble(double from, double to) {
		return from + getRandom().nextDouble() * (to - from);
	}

	/**
	 * @Title: nextGaussian 
	 * @Description: 生成一个高斯双精度值 
	 * @return double 返回类型 
	 * @throws
	 */
	public static final double nextGaussian() {
		return getRandom().nextGaussian();
	}

	/**
	 * @Title: nextBoolean 
	 * @Description: 生成随机布尔值 
	 * @return boolean 返回类型 
	 * @throws
	 */
	public static final boolean nextBoolean() {
		return getRandom().nextBoolean();
	}

	/**
	 * @Title: nextBytes 
	 * @Description: 初始化数组 
	 * @param array 
	 * @return void
	 * @throws
	 */
	public static final void nextBytes(final byte[] array) {
		getRandom().nextBytes(array);
	}

	/**
	 * 根据奖励库类别随机出来奖励物品
	 */
	public static int randomIndex(int[] weight) {
		int total = 0;
		for (int i : weight) {
			total += i;
		}

		int rand = Rnd.nextInt(total);
		int current = 0;
		for (int i = 0; i < weight.length; i++) {
			current += weight[i];
			if (rand < current) {
				return i;
			}
		}

		return -1;
	}

	/** 
	 * 按照一个集合里面元素的权重随机。 
	 * @param <T>
	 * @param list
	 * @param function
	 * @return
	 */
	public static <T> int randomIndex(List<T> list, Function<T, Integer> function) {
		int total = 0;
		for (T i : list) {
			total += function.apply(i);
		}
		int rand = Rnd.nextInt(total);
		int current = 0;
		for (int i = 0; i < list.size(); i++) {
			current += function.apply(list.get(i));
			if (rand < current) {
				return i;
			}
		}
		return -1;
	}

	/** 
	 * 按照一个集合里面元素的权重随机。 
	 * @param <T>
	 * @param list
	 * @param function
	 * @return
	 */
	public static <T> T randomElement(List<T> list, Function<T, Integer> function) {
		return list.get(randomIndex(list, function));
	}
	
	/**
	 * 根据奖励的类别获取命中索引
	 * @param maxRandomNum
	 * @param weight
	 * @return
	 */
	public static int randomIndex(int maxRandomNum, int[] weight) {
		int rand = Rnd.nextInt(maxRandomNum);
		int current = 0;
		for (int i = 0; i < weight.length; i++) {
			current += weight[i];
			if (rand < current) {
				return i;
			}
		}

		return -1;
	}

	public static int randomIndex(List<Integer> weight) {
		int total = 0;
		for (int i : weight) {
			total += i;
		}

		int rand = Rnd.nextInt(total);
		int current = 0;
		for (int i = 0; i < weight.size(); i++) {
			current += weight.get(i);
			if (rand < current) {
				return i;
			}
		}

		return -1;
	}
	
	public static int randomIndex(float[] weight) {
		float total = 0;
		for (float i : weight) {
			total += i;
		}

		float rand = Rnd.nextFloat(total);
		int current = 0;
		for (int i = 0; i < weight.length; i++) {
			current += weight[i];
			if (current > rand) {
				return i;
			}
		}

		return -1;
	}


	public static int randomKey(List<Entry<Integer, Integer>> list) {

		int total = 0;
		for (Entry<Integer, Integer> entry : list) {
			total += entry.getValue();
		}
		int rand = Rnd.nextInt(total);
		int current = 0;

		for (Entry<Integer, Integer> entry : list) {
			current += entry.getValue();
			if (rand < current) {
				return entry.getKey();
			}
		}
		return 0;
	}
	/**
	 * 从带权重的对象集合里随机一个下标
	 * @param list
	 * @return
	 */
	public static int randomWeighableIndex(List<? extends Weightable> list) {
		return randomIndex(list, Weightable::weight);
	}
	/**
	 * 从带权重的对象集合里随机一个下标，排除指定索引的元素
	 * @param excludeIndexs
	 *            排除的索引
	 * @param list
	 * @return
	 */
	public static int randomWeighableIndexExcludeIndex(List<? extends Weightable> list, List<Integer> excludeIndexs) {
		return randomIndexExcludeIndex(list, Weightable::weight, excludeIndexs);
	}
	
	/**
	 * 从带权重的对象集合里随机一个下标，排除指定索引的元素
	 * @param excludeIndexs
	 *            排除的索引
	 * @param list
	 * @return
	 */
	public static <T> int randomIndexExcludeIndex(List<T> list, Function<T, Integer> function, List<Integer> excludeIndexs) {
		int total = 0;
		for (int i = 0; i < list.size(); i++) {
			if (excludeIndexs != null && excludeIndexs.contains(i)) {
				continue;
			}
			total += function.apply(list.get(i));
		}

		int rand = Rnd.nextInt(total);
		int current = 0;
		for (int i = 0; i < list.size(); i++) {
			if (excludeIndexs != null && excludeIndexs.contains(i)) {
				continue;
			}
			current += function.apply(list.get(i));
			if (rand < current)
				return i;
		}

		return -1;
	}

	/**
	 * 从多个集合中，按权重随机出来一个元素
	 * 
	 * @param list
	 * @return
	 */
	@SafeVarargs
	public static <T extends Weightable> T randomWeighableElementFromMultipleList(List<T>... list) {
		int total = 0;
		for (int i = 0; i < list.length; i++) {
			List<T> list2 = list[i];
			for (int j = 0; j < list2.size(); j++) {
				total += list2.get(j).weight();
			}
		}
		int rand = Rnd.nextInt(total);
		int current = 0;

		for (int i = 0; i < list.length; i++) {
			List<T> list2 = list[i];
			for (int j = 0; j < list2.size(); j++) {
				current += list2.get(j).weight();
				if (rand < current)
					return list2.get(j);
			}
		}
		return null;
	}

	/** 
	 * 按照权重随机出来一个元素
	 * @param <T>
	 * @param list
	 * @return
	 */
	public static <T extends Weightable> T randomWeighableElement(List<T> list) {
		return list.get(randomWeighableIndex(list));
	}
	/**
	 * 按权重随机出指定数量的不重复的元素索引
	 * 
	 * @param list
	 * @param count
	 * @return
	 */
	public static List<Integer> randomWeighableIndexsNonRepeating(List<? extends Weightable> list, int count) {

		List<Integer> ret = new ArrayList<Integer>();

		for (int i = 0; i < count; i++) {

			int index = randomWeighableIndexExcludeIndex(list, ret);
			if (index != -1) {
				ret.add(index);
			}
		}
		return ret;
	}

	public static <T> List<Integer> randomIndexsNonRepeating(List<T> list, Function<T, Integer> function, int count) {

		List<Integer> ret = new ArrayList<Integer>();

		for (int i = 0; i < count; i++) {

			int index = randomIndexExcludeIndex(list, function, ret);
			if (index != -1) {
				ret.add(index);
			}
		}
		return ret;
	}
	public static int[] getRandomNumbers(int[] array, int count) {
		if (count > array.length) {
			throw new IllegalArgumentException("Count cannot be greater than the array length.");
		}

		Random random = new Random();

		// Fisher-Yates 洗牌算法
		for (int i = array.length - 1; i > 0; i--) {
			int j = getRandom().nextInt(i + 1);
			// 交换 array[i] 和 array[j]
			int temp = array[i];
			array[i] = array[j];
			array[j] = temp;
		}

		// 取出前 count 个元素
		int[] result = new int[count];
		System.arraycopy(array, 0, result, 0, count);

		return result;
	}
	/**
	 * 按权重随机出指定数量的不重复的元素索引
	 * 
	 * @param list
	 * @param count
	 * @return
	 */
	public static <T extends Weightable> List<T> randomWeighableElementsNonRepeating(List<T> list, int count) {
		List<T> ret = new ArrayList<>();
		List<Integer> indexs = randomWeighableIndexsNonRepeating(list, count);
		for (int i = 0; i < indexs.size(); i++) {
			T weightable = list.get(indexs.get(i));
			ret.add(weightable);
		}
		return ret;
	}

	/**
	 * 按权重随机出指定数量的不重复的元素索引
	 * 
	 * @param list
	 * @param count
	 * @return
	 */
	public static <T> List<T> randomElementsNonRepeating(List<T> list, Function<T, Integer> function, int count) {
		List<T> ret = new ArrayList<>();
		List<Integer> indexs = randomIndexsNonRepeating(list, function, count);
		for (int i = 0; i < indexs.size(); i++) {
			T weightable = list.get(indexs.get(i));
			ret.add(weightable);
		}
		return ret;
	}
	
	/**
	 * 索引在start和end之间，按权重随机出来一个
	 * @param weight
	 * @param start
	 * @param end
	 *            可能超过数组最大长度，则从0继续开始
	 * @return
	 */
	public static int random(int[] weight, int start, int end) {

		int ret = 0;
		int max = 0;
		for (int i = start; i < end; i++) {
			if (i > weight.length - 1) {
				max += weight[i - weight.length];
			} else {
				max += weight[i];
			}
		}
		int rand = new Random().nextInt(max);
		int current = 0;
		for (; start < end; start++) {
			if (start > weight.length - 1) {
				current += weight[start - weight.length];
			} else {
				current += weight[start];
			}
			if (rand < current) {
				ret = start;
				break;
			}
		}
		return ret > weight.length - 1 ? ret - weight.length : ret;
	}

	/**
	 * 索引在start和end之间，按权重随机出来一个
	 * @param weight
	 * @param start
	 * @param end
	 *            可能超过数组最大长度，则从0继续开始
	 * @return
	 */
	public static int random(List<Integer> weight, int start, int end) {
		int ret = 0;
		int max = 0;
		for (int i = start; i < end; i++) {
			if (i > weight.size() - 1) {
				max += weight.get(i - weight.size());
			} else {
				max += weight.get(i);
			}
		}
		int rand = new Random().nextInt(max);
		int current = 0;
		for (; start < end; start++) {
			if (start > weight.size() - 1) {
				current += weight.get(start - weight.size());
			} else {
				current += weight.get(start);
			}
			if (rand < current) {
				ret = start;
				break;
			}
		}
		return ret > weight.size() - 1 ? ret - weight.size() : ret;
	}

	/** 
	 * 从一个指定集合中，随机出指定数量的元素
	 * @param list
	 * @param count
	 * @return
	 */
	public static <E> List<E> randomSubList(List<E> list, int count) {
		if (count <= 0) {
			return Collections.emptyList();
		}
		list = new ArrayList<>(list);
		if (list.size() <= count) {
			return list ; 
		}
		Collections.shuffle(list);
		return list.subList(0, count);

	}
	
	/** 
	 * 从一个数组中，随机出指定数量的元素
	 * @param list
	 * @param count
	 * @return
	 */
	public static List<Integer> randomSubList(int[] array, int count) {
		List<Integer> res = new ArrayList<>();
		for (int i : array) {
			res.add(i);
		}
		return randomSubList(res, count);
	}

	/** 
	 * 从一个数组中，随机出指定数量的元素
	 * @param array
	 * @param count
	 * @return
	 */
	public static int[] randomSubArray(int[] array, int count) {
		if (count >= array.length) {
			return array;
		}
		int[] ret = new int[count];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = -1;
		}
		int a = 0;
		for (int i = 0; i < RANDOM_MAX; i++) {
			int index = nextInt(array.length);
			boolean has = false;
			for (int j = 0; j < ret.length; j++) {
				if (ret[j] == array[index]) {
					has = true;
					break;
				}
			}
			if (!has) {
				ret[a++] = array[index];
				if (a == count) {
					break;
				}
			}
		}
		return ret;
	}

	/** 
	 * 一个权重集合，如果当前命中，则继续计算下一个，每命中一个 返回值 + 1
	 * @param weights
	 * @return
	 */
	public static int randomCount(float[] weights) {
		int count = 0;
		for (int i = 0; i < weights.length; i++) {
			if (Rnd.hit(weights[i])) {
				count++;
			} else {
				break;
			}
		}
		return count;
	}
	/** 
	 * 万分比随机
	 * @param value
	 * @return
	 */
	public static boolean hit(int value)
	{
		return Rnd.nextInt(RANDOM_CONST) < value;
	}
	public static boolean hit(float value) {
		return nextDouble() < value;
	}
	/**
	 * 按百分比随机
	 * @param value
	 * @return
	 */
	public static boolean hitPercentage(int value) {
		return Rnd.nextInt(RANDOM_PERCENTAGE_CONST) < value;
	}
	
	/**
	 * 包含两个数字的的数组，0为最小数，1为最大数，在范围中随机
	 * @param value
	 * @return
	 */
	public static int random21(int[] value) {
		if (value.length == 0 || value.length > 2) {
			throw new IllegalArgumentException("范围随机数据配置错误： " + value);
		}
		if (value.length == 1 || value[0]==value[1]) {
			return value[0] ; 
		}
		return get(value[0], value[1]) ; 
	}

	public static <T> T randomOne(List<T> list) {
		return list.get(Rnd.nextInt(list.size())) ; 
	}
	public static <T> T randomOne(T[] array) {
		return array[Rnd.nextInt(array.length)];
	}
	public static int randomOne(int[] array) {
		return array[Rnd.nextInt(array.length)];
	}

	/** 
	 * 从一个Set中随机出一个元素，并且这个元素不在排除的集合中
	 * @param sourceSet
	 * @param excluded
	 * @return
	 */
	public static <T> T randomElementExcluded(Set<T> sourceSet, Collection<T> excluded) {
		if (sourceSet == null || sourceSet.isEmpty()) {
			return null;
		}
		Set<T> validElements = new HashSet<>(sourceSet);
		validElements.removeAll(excluded);

		if (validElements.isEmpty()) {
			return null;
		}
		int randomIndex = nextInt(validElements.size());
		int i = 0;
		for (T element : validElements) {
			if (i == randomIndex) {
				return element;
			}
			i++;
		}
		return null;
	}

	/** 
	 * id;weight|id;weight
	 * 按weight随机出来id
	 * @param array
	 * @return
	 */
	public static int randomId(int[][] array) {
		if (array.length == 1 && array[0].length == 1) {
			return array[0][0];
		}
		int total = 0;
		for (int i = 0; i < array.length; i++) {
			total += array[i][1];
		}
		int rand = Rnd.nextInt(total);
		int current = 0;

		for (int i = 0; i < array.length; i++) {
			current += array[i][1];
			if (rand < current)
				return array[i][0];
		}
		return -1;
	}

	public static void main(String args[]) {
		int[] x = new int[] {11,22,33,44,55,66} ; 
		int[] randomSubArray = randomSubArray(x, 3);
		for (int i : randomSubArray) {
			System.out.println(i);
		}
	}
}
