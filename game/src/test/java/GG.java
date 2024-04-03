import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.game.games.cache.entity.Mail;

public class GG {

	
	public static void main(String[] args) throws Exception {
		
		Map<Integer, Integer>  map= new HashMap<Integer, Integer>(); 
		
		for (int i = 0; i < 100; i++) {
			map.put(i, i)	; 
		}
		Iterator<Integer> iterator = map.values().iterator(); 
		while (iterator.hasNext()) {
			Integer integer = (Integer) iterator.next();
			if (integer%2 == 0 ) {
				iterator.remove(); ; 
			}
		}
		System.out.println(map);
		
	}
	
}
