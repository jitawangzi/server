import java.math.BigDecimal;

import cn.game.util.Rnd;

public class GG {
	public static int x = 0;
	public static long firstTime = System.currentTimeMillis();

	public static void main(String[] args) {
		BigDecimal a = new BigDecimal("9.8").setScale(2);
		BigDecimal b = new BigDecimal("9.11").setScale(2);

		System.out.println("a: " + a); // 输出 a 的值
		System.out.println("b: " + b); // 输出 b 的值
		System.out.println(a.compareTo(b)); // 应该输出 -1 表示 a < b
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
