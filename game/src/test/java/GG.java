import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import cn.game.games.cache.entity.Item;
import cn.game.util.JsonUtil;

public class GG {

	public static void main(String[] args) throws Exception {

		
		List<Integer> list = new ArrayList<>();
		list.add(3); 
		System.out.println(list.contains(3));
		
		BitSet bitSet = new BitSet();
		bitSet.set(1, true);
		bitSet.set(5, true);

		String jsonString2 = JsonUtil.toJsonString(bitSet);
		System.out.println(jsonString2);
		Item item = new Item();
		item.setConfigId(333);
		item.setPlayerId(332223L);
		String jsonString = JsonUtil.toJsonString(item);
		System.out.println(jsonString);
		
		


	}
}
