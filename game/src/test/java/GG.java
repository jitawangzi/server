import org.apache.commons.collections4.map.MultiKeyMap;

public class GG {

	
	public static void main(String[] args) throws Exception {
		// 创建一个MultiKeyMap对象，键的类型为String，值的类型为Double
		MultiKeyMap<Integer, Double> multiKeyMap = new MultiKeyMap<>();

		// 向MultiKeyMap中添加键值对
		multiKeyMap.put(1, 2, 10.5);
		multiKeyMap.put(2, 3, 20.5);

		// 通过多个键来获取值
		Double value1 = multiKeyMap.get(1, 2);
		Double value2 = multiKeyMap.get(2, 3);

		System.out.println("Value for key (1, 'A'): " + value1);
		System.out.println("Value for key (2, 'B'): " + value2);

	}
	
}
