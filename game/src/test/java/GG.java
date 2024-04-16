import java.util.HashMap;
import java.util.Map;

public class GG<T extends Number> {

	public static void main(String[] args) throws Exception {
		Map<Integer, Integer> m1 = new HashMap<Integer, Integer>();
		m1.put(1, 2);
		m1.put(2, 33);

		Map<Integer, Integer> m2 = new HashMap<Integer, Integer>();
		m1.put(1, 9);
		m1.put(3, 33);

		m1.putAll(m2);

		System.out.println(m1);
	}

}

