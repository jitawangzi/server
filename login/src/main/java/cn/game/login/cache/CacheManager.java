package cn.game.login.cache;

import java.util.function.Consumer;

import cn.game.core.cache.CacheType;
import cn.game.util.VxRedisUtil;
import io.reactivex.rxjava3.annotations.Nullable;
import io.vertx.core.Future;
import io.vertx.redis.client.Response;

/**
 * redis缓存管理
 * 2020年8月26日 下午5:48:19
 * @author SYQ
 */
@Deprecated
public class CacheManager {

	private static CacheManager instance = new CacheManager() ; 

	private CacheManager(){
		
	}

	public static CacheManager getInstance() {
		return instance;
	}
	
	/**
	 * 异步载入缓存
	 * @param consumer
	 *            查询后的操作
	 * @param cacheType
	 * @param ks
	 */
	public void load(Consumer<String> consumer, CacheType cacheType, Object... ks) {
		VxRedisUtil.get(cacheType.key(ks), consumer);
	}

	public void incr(Consumer<Long> consumer, CacheType cacheType, Object... ks) {
		VxRedisUtil.incr(cacheType.key(ks), consumer);
	}
	/**
	 * 异步载入缓存
	 * @param cacheType
	 * @param ks
	 * @return
	 */
	public Future<@Nullable Response> loadAsync(CacheType cacheType, Object... ks) {
		return VxRedisUtil.get(cacheType.key(ks));
	}

	public String loadSync(CacheType cacheType, Object... ks) {
		return VxRedisUtil.getSync(cacheType.key(ks));
	}
	public void put(String value, CacheType cacheType, Object... ks) {
		VxRedisUtil.set(cacheType.key(ks), value);
	}
	public void put(int expire, String value, CacheType cacheType, Object... ks) {
		VxRedisUtil.setex(cacheType.key(ks), value, expire);
	}
}
