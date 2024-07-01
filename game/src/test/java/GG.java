import cn.game.util.JsonUtil;
import cn.game.util.Rnd;

public class GG {
	public static int x = 0;
	public static long firstTime = System.currentTimeMillis();

	public static void main(String[] args) throws Exception {
		JsObject jsObject = new JsObject();
		jsObject.setName("name");
		jsObject.setAge(12);
		jsObject.setAddress("address");
		System.out.println(jsObject);
		String jsonString = JsonUtil.toJsonString(jsObject);
		System.out.println(jsonString);
		String string = "[\"JsObject\",{\"name\":\"name\",\"age\":12,\"address\":\"address\"}]";
		JsObject fromJson = JsonUtil.parseObject(string, JsObject.class);
		System.out.println(fromJson);
	}

	private static void test() {
		int c2 = 0;

		for (int j = 0; j < 10000; j++) {

			int c1 = 0;
			for (int i = 0; i < 10; i++) {
				int nextInt = Rnd.nextInt(1, 11);
				if (nextInt == 1) {
					c1++;
				}
				if (c1 == 2) {
					c2++;
					break;
				}
			}
		}

		System.out.println(c2);
	}
}
