import cn.game.util.ByteHelp;
import cn.game.util.Rnd;

public class GG {
	public static int x = 0;
	public static long firstTime = System.currentTimeMillis();

	public static void main(String[] args) throws Exception {
		byte[] da = new byte[] { -1, 66, 100, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 };

		String string = ByteHelp.toString(da, 20);
		System.out.println(string);
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
