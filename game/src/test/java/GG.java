import java.util.NavigableMap;
import java.util.TreeMap;

public class GG {
	public static void main(String[] args) {
		// 创建一个NavigableMap对象
		NavigableMap<Integer, String> navigableMap = new TreeMap<>();

		// 添加键值对
		navigableMap.put(1, "Apple");
		navigableMap.put(2, "Banana");
		navigableMap.put(3, "Orange");
		navigableMap.put(44, "Grapes");
		navigableMap.put(5, "Mango");

		String string = navigableMap.get(5);
		System.err.println(string);
		// 获取键值对
		System.out.println("NavigableMap: " + navigableMap);

		// 获取第一个键值对
		System.out.println("First Entry: " + navigableMap.firstEntry());

		// 获取最后一个键值对
		System.out.println("Last Entry: " + navigableMap.lastEntry());

		// 获取小于等于指定键的最大键值对
		System.out.println("Floor Entry: " + navigableMap.floorEntry(3));

		// 获取大于等于指定键的最小键值对
		System.out.println("Ceiling Entry: " + navigableMap.ceilingEntry(3));

		// 获取小于指定键的最大键值对
		System.out.println("Lower Entry: " + navigableMap.lowerEntry(3));

		// 获取大于指定键的最小键值对
		System.out.println("Higher Entry: " + navigableMap.higherEntry(3));

		// 获取指定范围的键值对
		System.out.println("SubMap: " + navigableMap.subMap(2, true, 4, true));

		// 删除键值对
		navigableMap.remove(3);

		// 获取键值对个数
		System.out.println("Size: " + navigableMap.size());

		// 清空键值对
		navigableMap.clear();
	}
}
