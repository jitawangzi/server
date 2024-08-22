import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GG2 {

	public static void main(String[] args) throws Exception {

		Map<Integer, Long> idUpdateTimeMap = new HashMap<Integer, Long>();
		idUpdateTimeMap.put(1, 333L);
		idUpdateTimeMap.put(2, 333L);

		Set<Integer> idsSet = idUpdateTimeMap.keySet();
		Iterator<Integer> iterator = idsSet.iterator();
		while (iterator.hasNext()) {
			Integer id = (Integer) iterator.next();
			if (id == 1) {
				iterator.remove();
			}
		}

		for (Integer integer : idsSet) {
			System.out.println(integer);
		}

	}


}
