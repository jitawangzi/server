import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class GG2 {

	static class CmpObj {
		private int id;

		public CmpObj(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
	}

	public static void main(String[] args) {
		List<CmpObj> items = Arrays.asList(new CmpObj(30000), new CmpObj(100000), new CmpObj(200000), new CmpObj(500000), new CmpObj(2000000));

		int targetValue = 100000;
		int index = findIndexLastLessThanOrEqual(items, targetValue, CmpObj::getId);
		System.out.println(index); // 应该输出：3（ID为500000的元素的索引）

		targetValue = 490000;
		index = findIndexLastLessThanOrEqual(items, targetValue, CmpObj::getId);
		System.out.println(index); // 应该输出：2（ID为200000的元素的索引）
	}

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

}
