package performance;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;

public class ToStringListTest {

	public static void main(String[] args) {

		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < 10000; i++) {
			list.add(i);
		}

		for (int i = 0; i < 1000; i++) {
			JSON.toJSONString(list);
//			StrUtil.toString(list);
		}

//		long start = System.nanoTime();
		long start = System.currentTimeMillis();

		for (int i = 0; i < 1000; i++) {
			JSON.toJSONString(list);
//			StrUtil.toString(list);
		}
//		long end = System.nanoTime();
		long end = System.currentTimeMillis();

		System.out.println("耗时 :" + (end - start));

	}

}
