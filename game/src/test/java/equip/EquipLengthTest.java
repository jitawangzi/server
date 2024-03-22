package equip;

import java.util.Map;
import java.util.TreeSet;

public class EquipLengthTest {

	public static void main(String[] args) {

//		int equipCount = 3300;
//		Mail mail = new Mail();
//		PbBuilder.buildMailInfo(mail);
//		System.out.println("end");

		TreeSet<Integer> set = new TreeSet();
		set.add(3);
		set.add(7);
		set.add(1);
		set.add(5);
		for (Integer integer : set) {
			System.out.println(integer);
		}

	}

	public void test(Map<Integer, Integer> map, int key, int value) {
//		map.merge(1, 2, (a, b) -> {
//			return a + b;
//		});

		map.merge(key, value, Integer::sum);
	}

}
