import org.apache.commons.collections4.map.MultiKeyMap;

import cn.game.util.JsonUtil;

public class GG {
	public static void main(String[] args) {
		// 创建一个 MultiKeyMap 对象
		MultiKeyMap<Long, Integer> map = new MultiKeyMap<Long, Integer>();

		// 向 MultiKeyMap 添加键值对
		map.put(323232L, 1001L, 3323232);
		map.put(323233L, 10012L, 33232323);

		String jsonString = JsonUtil.toJsonString(map);
		MultiKeyMap object = JsonUtil.parseObject(jsonString, MultiKeyMap.class);
		System.out.println(object);

//		Set<Entry<MultiKey<? extends Long>, Integer>> entrySet = map.entrySet();
//		for (Entry<MultiKey<? extends Long>, Integer> entry : entrySet) {
//			System.out.println(entry.getKey().getKey(0).getClass());
//			System.out.println(entry.getKey().getKey(1).getClass());
//			System.out.println(entry.getValue());
//		}
	}
}
