import cn.game.util.JsonUtil;

public class GG {

	public static void main(String[] args) throws Exception {
		GGGG g = new GGGG();
		g.name = "sadfasdf";
		String jsonString = JsonUtil.toJsonString(g);
		System.out.println(jsonString);
		
		String string = "[\"GGGG\",{\"name\":\"sadfasdf\",\"age\":33}]";

		System.out.println(JsonUtil.parseObject(string, GGGG.class));

	}
	
}

class GGGG {
	public String name;
//	public int age;

	@Override
	public String toString() {
		return "GGGG [name=" + name + ", age=" + 0 + "]";
	}

}

class GGG extends GGGG {
	public String name;

}
