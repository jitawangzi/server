import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import cn.game.games.net.game.module.award.Goods;
import cn.game.util.Rnd;

public class GT {
	public static int x = 0;
	public static long firstTime = System.currentTimeMillis();

	public static void main(String[] args) throws IOException {
		List<Goods> list = new ArrayList<Goods>();
		for (int i = 0; i < 10; i++) {
			Goods goods = new Goods();
			goods.setId(i * 100000);
			goods.setCount(i);
			list.add(goods);
		}
		String jsonString = JSON.toJSONString(list);
		System.out.println(jsonString);
		System.out.println(jsonString.length());
	}
	private static void loginCount() throws IOException {
		List<String> lines22 = Files.readLines(new File("D:/22.txt"), Charset.defaultCharset());
		Set<String> set22 = new java.util.HashSet<String>(lines22);
		List<String> lines23 = Files.readLines(new File("D:/23.txt"), Charset.defaultCharset());
		Set<String> set23 = new java.util.HashSet<String>(lines23);
		System.out.println("22日登陆： " + set22.size());
		System.out.println("23日登陆： " + set23.size());
		// 获取交集
		Set<String> intersection = Sets.intersection(set22, set23);

		System.out.println("交集数：  " + intersection.size());
		System.out.println("比例：  " + intersection.size() * 1.0f / set22.size());
		System.out.println(intersection);
	}
	private static void test() {
		int c2 = 0;

		for (int j = 0; j < 10000; j++) {

			int c1 = 0;
			for (int i = 0; i < 10; i++) {
				int nextInt = Rnd.nextInt(1, 11);
				if (nextInt == 1) {
					c1++;
				}
				if (c1 == 2) {
					c2++;
					break;
				}
			}
		}

		System.out.println(c2);
	}
}
