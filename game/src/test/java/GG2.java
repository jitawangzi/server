import java.util.HashMap;

import cn.game.util.JsonUtil;

public class GG2 {

	public static void main(String[] args) throws Exception {
		
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		map.put(1, 3);
		map.put(2, 3);
		MapObj mapobj = new MapObj();
		mapobj.map = map;
		
		// 这里使用jackson进行序列化和反序列化
		String jsonString = JsonUtil.toJsonString(mapobj);
		System.out.println(jsonString);
		
		MapObj object = JsonUtil.parseObject(jsonString, MapObj.class);
		System.out.println(object.map.get("1")); // 正确的值
		System.out.println(object.map.get(1)); // null

	}

	static class MapObj {
		public HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
	}

}
