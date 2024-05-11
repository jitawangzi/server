import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
	public static void main(String[] args) throws Exception {
		// 创建 HashMap 对象
		HashMap<Integer, Integer> map = new HashMap<>();
		map.put(1, 3);
		map.put(2, 3);

		// 序列化为 JSON 字符串
		String jsonString = new ObjectMapper().writeValueAsString(map);
		System.out.println(jsonString);

		// 反序列化为 HashMap 对象
		HashMap<Integer, Integer> object = new ObjectMapper().readValue(jsonString, HashMap.class);
		System.out.println(object.get("1")); // 获取到了正确的值
		System.out.println(object.get(1)); // 获取到了正确的值
	}
}
