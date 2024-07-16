import java.util.ArrayList;
import java.util.List;

import cn.game.util.Rnd;

public class GG {
	public static int x = 0;
	public static long firstTime = System.currentTimeMillis();

	public static void main(String[] args) throws Exception {
		List<List<String>> ret = new ArrayList<>();
		List<String> list = new ArrayList<>();
		list.add("1");
		
		ret.set(1, list);
		
		System.out.println(ret.get(0));
		
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
