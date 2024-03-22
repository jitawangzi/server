package performance;

import java.util.BitSet;

import com.alibaba.fastjson.JSON;

public class BitSetTest {

	public static void main(String[] args) {
		BitSet set = new BitSet();
		for (int i = 0; i < 50; i++) {
			if (i % 3 == 0) {

				set.set(i, true);
			} else {
				set.set(i, false);
			}
		}

		long[] longArray = set.toLongArray();
		for (long ll : longArray) {
			System.out.println(ll);
		}

		System.out.println(" to string");
		System.out.println(set.toString());

		String jsonString = JSON.toJSONString(longArray);
		System.out.println(jsonString);
		long[] array = JSON.parseObject(jsonString, long[].class);
		for (long l : array) {
			System.out.println(l);
		}

		BitSet bitSet = BitSet.valueOf(longArray);

		System.out.println("新" + bitSet.toString());

	}

}
