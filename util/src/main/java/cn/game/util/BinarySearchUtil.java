package cn.game.util;

import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
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
		int[] array = new int[] { };
		// 244
		System.out.println(search(array, 0, array.length - 1, 5));
		System.out.println(searchFirst(array, 0, array.length - 1, 8));
		System.out.println(searchLast(array, 0, array.length - 1, 3));
		System.out.println(searchFirstBig(array, 0, array.length - 1, 6));
		System.out.println(searchLastLess(array, 0, array.length - 1, 3));
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
	 * 通用二分查找方法，查找第一个大于给定值的对象
	 *
	 * @param list 已排序的列表
	 * @param value 用于比较的值
	 * @param valueExtractor 从对象中提取比较值的函数
	 * @param <T> 列表中对象的类型
	 * @param <U> 比较值的类型
	 * @return 第一个大于给定值的对象，如果没有找到则返回null
	 */
	public static <T, U extends Comparable<U>> T findFirstGreaterThan(List<T> list, U value, Function<T, U> valueExtractor) {

		if (list == null || list.isEmpty()) {
			return null;
		}

		int left = 0;
		int right = list.size() - 1;

		while (left <= right) {
			int mid = left + (right - left) / 2;
			T midItem = list.get(mid);
			U midValue = valueExtractor.apply(midItem);

			if (midValue.compareTo(value) <= 0) {
				left = mid + 1;
			} else {
				if (mid == 0 || valueExtractor.apply(list.get(mid - 1)).compareTo(value) <= 0) {
					return midItem;
				}
				right = mid - 1;
			}
		}

		return null;
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
