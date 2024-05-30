import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GG {
	public static int x = 0;
	public static long firstTime = System.currentTimeMillis();

	public static void main(String[] args) throws Exception {
		List<Integer> list = new ArrayList<>();
		
		list.add(1);
		list.add(3);
		list.add(2);
		Collections.sort(list);
		for (Integer integer : list) {
			System.out.println(integer);
		}

	}
}
