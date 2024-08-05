import java.util.Map;

import cn.game.util.JsonUtil;

public class GG2 {

	public static void main(String[] args) throws Exception {
		JSTest jsTest = new JSTest();
		jsTest.i2 = 2;

		String jsonString = JsonUtil.toJsonString(jsTest);
		System.out.println(jsonString);
		GG2.JSTest object = JsonUtil.parseObject(jsonString, JSTest.class);
		System.out.println(object.i);
		System.out.println(object.i2);
		
		Map<Integer, JSTest> map = new java.util.HashMap<>();
		map.put(1, jsTest);

		String mapString = JsonUtil.toJsonString(map);
		System.err.println(mapString);

	}

	static class JSTest {
		public String str;
		public int i;
		public int i2;
	}

}
