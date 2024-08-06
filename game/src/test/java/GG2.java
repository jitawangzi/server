import java.util.ArrayList;
import java.util.List;

public class GG2 {

	public static void main(String[] args) throws Exception {
		
		List<Integer> list = new ArrayList<>();

		list.add(3);
		list.add(8);
		list.add(1);
		list.add(2);

		list.sort((a, b) -> a - b);

		for (Integer integer : list) {
			System.out.println(integer);
		}

	}

	static class JSTest {
		public String str;
		public int i;
		public int i2;
	}

}
