import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import cn.game.games.cache.entity.ShopItem;
import cn.game.util.JsonUtil;

public class GG {

	static class ccc{
		
		 Map<Long, ShopItem> itemsMap = new HashMap<Long, ShopItem>();
		 Multimap<Integer, ShopItem> groupItemsMap = ArrayListMultimap.create();
	}
	
	public static void main(String[] args) throws Exception {
		
		ShopItem shopItem = new ShopItem(); 
		shopItem.setCreateDay(3333);
		shopItem.setId(3323232L);
		shopItem.setPlayerId(33232L);
		
		ShopItem shopItem2 = new ShopItem(); 
		shopItem2.setCreateDay(3333);
		shopItem2.setId(3323232222L);
		shopItem2.setPlayerId(33232L);
		
		ccc c = new ccc() ; 
		c.itemsMap.put(shopItem.getId(), shopItem); 
		c.groupItemsMap.put(3, shopItem); 
		c.itemsMap.put(shopItem2.getId(), shopItem2); 
		c.groupItemsMap.put(3, shopItem2); 
		
		String jsonString = JsonUtil.toJsonString(c); 
		System.out.println(jsonString);
		
		GG.ccc object = JsonUtil.parseObject(jsonString, ccc.class); 
		System.out.println(object.itemsMap.get(3323232L));
		System.out.println(object.groupItemsMap.get(3));
		
		
	}
	
}
