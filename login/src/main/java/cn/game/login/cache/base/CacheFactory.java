package cn.game.login.cache.base;

import java.util.HashMap;
import java.util.Map;


public class CacheFactory {

	private static Map<Class<? extends BaseCache>, BaseCache> caches = new HashMap<>();
	
	public static <T> T getCache(Class<? extends BaseCache> cacheClass){
		BaseCache c = caches.get(cacheClass);
		if(c == null){
			try {
				c = cacheClass.newInstance();
				c.init();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace() ; 
			}
			caches.put(cacheClass, c);
		}
		return (T)c;
	}
}
