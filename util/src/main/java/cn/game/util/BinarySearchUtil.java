package cn.game.util;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.function.BiFunction;
import java.util.function.Function;

/**   
 * 2分查找和一些变体
 * 2019年3月1日 下午5:15:21
 * @author SYQ
 */
public class BinarySearchUtil {
    private static final int BINARYSEARCH_THRESHOLD   = 5000;

	public static void main(String args[]) {
//		int[] array = new int[] { 1, 2, 3, 3, 3, 4, 5, 8, 8 };
//		int[] array = new int[] { };
		// 244
//		System.out.println(search(array, 0, array.length - 1, 5));
//		System.out.println(searchFirst(array, 0, array.length - 1, 8));
//		System.out.println(searchLast(array, 0, array.length - 1, 3));
//		System.out.println(searchFirstBig(array, 0, array.length - 1, 6));
//		System.out.println(searchLastLess(array, 0, array.length - 1, 3));

		List<CmpObj> items = Arrays
				.asList(new CmpObj(10), new CmpObj(20), new CmpObj(30), new CmpObj(40), new CmpObj(50), new CmpObj(60), new CmpObj(70), new CmpObj(80),
						new CmpObj(90), new CmpObj(100));

		int targetValue = 25;
		int index = findIndexLastLessThanOrEqual(items, targetValue, CmpObj::getId);
		System.out.println("targetValue :" + targetValue + "  findIndexLastLessThanOrEqual ：" + index);

		targetValue = 20;
		CmpObj retObj = findFirstGreaterThanOrEqual(items, targetValue, CmpObj::getId);
		System.out.println("targetValue :" + targetValue + "  findFirstGreaterThanOrEqual ：" + retObj);

		targetValue = 30;
		retObj = findFirstLessThan(items, targetValue, CmpObj::getId);
		System.out.println("targetValue :" + targetValue + "  findFirstLessThan ：" + retObj);
		
		targetValue = 40;
		index = findElementIndexByField(items, 40, CmpObj::getId, (r1, r2) -> r1 - r2);
		System.out.println("targetValue :" + targetValue + "  findElementIndexByField ：" + index);
	}

	private static class CmpObj {
		private int id;

		public CmpObj(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		@Override
		public String toString() {
			return "CmpObj [id=" + id + "]";
		}
	}

	/**
	* 通用二分查找方法，查找第一个小于给定值的对象
	*
	* @param list 已排序的列表
	* @param value 用于比较的值
	* @param valueExtractor 从对象中提取比较值的函数
	* @param <T> 列表中对象的类型
	* @param <U> 比较值的类型
	* @return 第一个满足条件的对象，如果没有找到则返回null
	*/
	public static <T, U extends Comparable<U>> T findFirstLessThan(List<T> list, U value, Function<T, U> valueExtractor) {

		if (list == null || list.isEmpty()) {
			return null;
		}

		int left = 0;
		int right = list.size() - 1;

		while (left <= right) {
			int mid = left + (right - left) / 2;
			T midItem = list.get(mid);
			U midValue = valueExtractor.apply(midItem);

			if (midValue.compareTo(value) < 0) {
				if (mid == list.size() - 1 || valueExtractor.apply(list.get(mid + 1)).compareTo(value) >= 0) {
					return midItem;
				}
				left = mid + 1;
			} else {
				right = mid - 1;
			}
		}

		return null;
	}

	/**
	 * 通用二分查找方法，查找第一个大于等于给定值的对象
	 *
	 * @param list 已排序的列表
	 * @param value 用于比较的值
	 * @param valueExtractor 从对象中提取比较值的函数
	 * @param <T> 列表中对象的类型
	 * @param <U> 比较值的类型
	 * @return 第一个大于等于给定值的对象，如果没有找到则返回null
	 */
	public static <T, U extends Comparable<U>> T findFirstGreaterThanOrEqual(List<T> list, U value, Function<T, U> valueExtractor) {

		if (list == null || list.isEmpty()) {
			return null;
		}

		int left = 0;
		int right = list.size() - 1;
		T result = null; // 用于保存找到的结果
		while (left <= right) {
			int mid = left + (right - left) / 2;
			T midItem = list.get(mid);
			U midValue = valueExtractor.apply(midItem);

			if (midValue.compareTo(value) < 0) {
				left = mid + 1; // 向右查找
			} else {
				result = midItem; // 记录找到的元素
				right = mid - 1; // 继续向左查找，寻找第一个符合条件的元素
			}
		}

		return result;
	}

	/** 
	 * 找到最后一个小于或等于指定值的元素的索引
	300
	1000
	2000
	5000
	20000
	
	例如传入5500 的的时候，则返回   5000对应的index 3 。 
	出入2000的时候，返回2000对应的index 2 
	
	 * @param <T>
	 * @param <U>
	 * @param list
	 * @param value
	 * @param valueExtractor
	 * @return  -1,如果没有找到符合条件的元素
	 */
	public static <T, U extends Comparable<U>> int findIndexLastLessThanOrEqual(List<T> list, U value, Function<T, U> valueExtractor) {
		int result = -1;

		if (list == null || list.isEmpty()) {
			return result;
		}

		int left = 0;
		int right = list.size() - 1;

		while (left <= right) {
			int mid = left + (right - left) / 2;
			T midItem = list.get(mid);
			U midValue = valueExtractor.apply(midItem);

			if (midValue.compareTo(value) <= 0) {
				result = mid; // 记录当前索引
				left = mid + 1; // 继续在右侧查找
			} else {
				right = mid - 1; // 在左侧查找
			}
		}

		return result; // 返回最后一个小于等于给定值的元素的索引
	}

	/** 
	 * 根据List中元素的某个属性值，来查找对应的元素，需要保证属性值存在且唯一
	 * @param <T>
	 * @param <U>
	 * @param list
	 * @param targetFieldValue
	 * @param fieldExtractor
	 * @param comparator  比较函数，如果元素没有实现Comparator接口
	 * @return 元素在list中的索引。  如果没找到返回-1 。 
	 */
	public static <T, U> int findElementIndexByField(List<T> list, U targetFieldValue, Function<T, U> fieldExtractor, BiFunction<U, U, Integer> comparator) {
		if (list == null || list.isEmpty()) {
			return -1; // 返回 -1 表示未找到
		}

		int left = 0;
		int right = list.size() - 1;

		while (left <= right) {
			int mid = left + (right - left) / 2;
			T midItem = list.get(mid);
			U midFieldValue = fieldExtractor.apply(midItem);

			int comparisonResult = comparator.apply(midFieldValue, targetFieldValue);

			if (comparisonResult == 0) {
				return mid; // 找到目标元素，返回索引
			} else if (comparisonResult < 0) {
				left = mid + 1; // 目标在右侧
			} else {
				right = mid - 1; // 目标在左侧
			}
		}

		return -1; // 未找到目标元素，返回 -1
	}

	/**
	 *  普通查找
	 * @param array
	 * @param low
	 * @param high
	 * @param key
	 * @return
	 */
	public static int search(int array[], int low, int high, int key) {

		while (low <= high) {
			int mid = (low + high) >>> 1;
			if (array[mid] == key) {
				return mid;
			} else if (array[mid] > key) {
				high = mid - 1;
			} else {
				low = mid + 1;
			}
		}
		return -1;
	}

	/**
	 * 第一个值等于给定元素
	 * @param array
	 * @param low
	 * @param high
	 * @param key
	 * @return
	 */
	public static int searchFirst(int array[], int low, int high, int key) {

		while (low <= high) {
			int mid = (low + high) >>> 1;
			if (array[mid] == key) {
				if (mid == 0 || array[mid - 1] == key) {
					high = mid - 1;
				} else {
					return mid;
				}
			} else if (array[mid] > key) {
				high = mid - 1;
			} else {
				low = mid + 1;
			}
		}
		return -1;
	}

	/**
	 * 最后一个值等于给定元素
	 * @param array
	 * @param low
	 * @param high
	 * @param key
	 * @return
	 */
	public static int searchLast(int array[], int low, int high, int key) {

		while (low <= high) {
			int mid = (low + high) >>> 1;
			if (array[mid] == key) {
				if (mid < array.length - 1 && array[mid + 1] == key) {
					low = mid + 1;
				} else {
					return mid;
				}
			} else if (array[mid] > key) {
				high = mid - 1;
			} else {
				low = mid + 1;
			}
		}
		return -1;
	}

	/**
	 * 第一个值大于等于给定元素
	 * @param array
	 * @param low
	 * @param high
	 * @param key
	 * @return
	 */
	public static int searchFirstBig(int array[], int low, int high, int key) {

		while (low <= high) {
			int mid = (low + high) >>> 1;
			if (array[mid] >= key) {
				if (mid == 0 || array[mid - 1] >= key) {
					high = mid - 1;
				} else {
					return mid;
				}
			} else {
				low = mid + 1;
			}
		}
		return -1;
	}

	/**
	 * 最后一个小于等于给定元素
	 * @param array
	 * @param low
	 * @param high
	 * @param key
	 * @return
	 */
	public static int searchLastLess(int array[], int low, int high, int key) {

		while (low <= high) {
			int mid = (low + high) >>> 1;
			if (array[mid] <= key) {
				if (mid == 0 || array[mid + 1] <= key) {
					low = mid + 1;
				} else {
					return mid;
				}
			} else {
				high = mid - 1;
			}
		}
		return -1;
	}
	
    public static <T> int searchFirstBig(List<? extends Comparable<? super T>> list, T key) {
        if (list instanceof RandomAccess || list.size()<BINARYSEARCH_THRESHOLD)
            return indexedBinarySearch(list, key);
        else
            return iteratorBinarySearch(list, key);
    }
    
    private static <T> int indexedBinarySearch(List<? extends Comparable<? super T>> list, T key) {
        int low = 0;
        int high = list.size()-1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            Comparable<? super T> midVal = list.get(mid);
            int cmp = midVal.compareTo(key);
			if (cmp < 0) {
				if (mid == 0 || list.get(mid + 1).compareTo(key)<=0) {
					low = mid + 1;
				} else {
					return mid;
				}
			} else {
				high = mid - 1;
			}
		
        }
        return -(low + 1);  // key not found
    }

    private static <T> int iteratorBinarySearch(List<? extends Comparable<? super T>> list, T key)
    {
        int low = 0;
        int high = list.size()-1;
        ListIterator<? extends Comparable<? super T>> i = list.listIterator();

        while (low <= high) {
            int mid = (low + high) >>> 1;
            Comparable<? super T> midVal = get(i, mid);
            int cmp = midVal.compareTo(key);

			if (cmp < 0) {
				if (mid == 0 || list.get(mid + 1).compareTo(key)<=0) {
					low = mid + 1;
				} else {
					return mid;
				}
			} else {
				high = mid - 1;
			}
        }
        return -(low + 1);  // key not found
    }
    
    /**
     * Gets the ith element from the given list by repositioning the specified
     * list listIterator.
     */
    private static <T> T get(ListIterator<? extends T> i, int index) {
        T obj;
        int pos = i.nextIndex();
        if (pos <= index) {
            do {
                obj = i.next();
            } while (pos++ < index);
        } else {
            do {
                obj = i.previous();
            } while (--pos > index);
        }
        return obj;
    }

}
