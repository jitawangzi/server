import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class GG {
	public static int x = 0;
	public static long firstTime = System.currentTimeMillis();

	public static void main(String[] args) throws Exception {
		Multimap<Integer, Integer> map = ArrayListMultimap.create();
		List list = new ArrayList<Integer>();
		list.add(11);
		map.putAll(1, list);
		System.out.println(map.keys().size());
		Collection<Integer> collection = map.get(1);
		for (Integer integer : collection) {
			System.out.println(integer);
		}

		map.remove(1, 11);
		System.out.println(map.keys().size());

		collection = map.get(1);
		for (Integer integer : collection) {
			System.out.println(integer);
		}

	}


}
