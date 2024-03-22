package performance;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public class ToStringMapTest {

	public static void main(String[] args) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		for (int i = 0; i < 10000; i++) {
			map.put(i, i + 1);
		}

		for (int i = 0; i < 1000; i++) {
			JSON.toJSONString(map);
//			StrUtil.toString(map);
		}

//		long start = System.nanoTime();
		long start = System.currentTimeMillis();

		for (int i = 0; i < 1000; i++) {
		JSON.toJSONString(map);
//		StrUtil.toString(map);
		}
//		long end = System.nanoTime();
		long end = System.currentTimeMillis();

		System.out.println("耗时 :" + (end - start));
	}

}
