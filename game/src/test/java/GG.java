import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GG {
	public static int x = 0;
	public static long firstTime = System.currentTimeMillis();

	public static void main(String[] args) throws Exception {

		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			list.add(i);
		}
		Iterator<Integer> iterator = list.iterator();
		while (iterator.hasNext()) {
			Integer integer = (Integer) iterator.next();
			if (integer % 2 == 0) {
				iterator.remove();
			}

		}
		for (Integer integer : list) {
			System.out.println(integer);
		}
		
	}


}
