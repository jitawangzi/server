package redis;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.manager.PlayerNameManager;

public class CheckSimplePlayer {
	public static void main(String[] args) throws Exception {

		Set<String> testGetAllName = PlayerNameManager.getInstance().testGetAllName();
		System.out.println("所有名字数量 " + testGetAllName.size());

//		for (String string : testGetAllName) {
//			System.out.println(string);
//		}
		Map<String, Long> testGetAllId = PlayerNameManager.getInstance().testGetAllPlayerIdMap();
		System.out.println("所有id数量 " + testGetAllId.size());
		SetView<String> difference = Sets.difference(testGetAllName, testGetAllId.keySet()); 
		for (String string : difference) {
            System.err.println("名字 " + string + " 不存在id");
        }
		
		for (Long id : testGetAllId.values()) {
			
			SimplePlayer simplePlayer = RedisLocalCache.getInstance().get(CacheType.PLAYER_SIMPLE.key(id));
			if (simplePlayer == null) {
				System.err.println("simplePlayer id " + id + " 不存在");
			} else {
//				System.out.println("simplePlayer id " + id + " 存在 : " + simplePlayer);
			}
		}
		System.out.println("检查结束");

	}
}
