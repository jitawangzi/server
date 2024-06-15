import cn.game.util.Rnd;

public class GG {
	public static int x = 0;
	public static long firstTime = System.currentTimeMillis();

	public static void main(String[] args) throws Exception {
//		System.out.println(Integer.MAX_VALUE);
//		List<Integer> list = new ArrayList<>();
//
//		for (int i = 1; i < 10; i++) {
//			list.add(i);
//		}
//
//		Collections.sort(list);
//		for (Integer integer : list) {
//			System.out.println(integer);
//		}
		String string = "HCBattleRewardRequest_13000027";
		System.out.println(string.startsWith("Hc"));
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
