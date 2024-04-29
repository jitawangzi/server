public class GG {
	public static void main(String[] args) {

		int[] x = new int[6];
		for (int i = 0; i < x.length; i++) {
			x[i] = i;
//			System.out.println(i);
		}

		for (int i = 0; i < x.length; i += 2) {
			System.out.println(i);
			System.out.println(i + 1);
		}

	}
}
