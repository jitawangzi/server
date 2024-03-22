package cn.game.login.cache;

import java.util.function.Consumer;

import cn.game.core.cache.CacheType;
import cn.game.util.RedisUtil;
import io.reactivex.rxjava3.annotations.Nullable;
import io.vertx.core.Future;
import io.vertx.redis.client.Response;

/**
 * @Description redis缓存管理
 * @date 2020年8月26日 下午5:48:19
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
	 * @Description 异步载入缓存
	 * @param consumer
	 *            查询后的操作
	 * @param cacheType
	 * @param ks
	 */
	public void load(Consumer<String> consumer, CacheType cacheType, Object... ks) {
		RedisUtil.get(cacheType.key(ks), consumer);
	}

	public void incr(Consumer<Long> consumer, CacheType cacheType, Object... ks) {
		RedisUtil.incr(cacheType.key(ks), consumer);
	}
	/**
	 * @Description 异步载入缓存
	 * @param cacheType
	 * @param ks
	 * @return
	 */
	public Future<@Nullable Response> loadAsync(CacheType cacheType, Object... ks) {
		return RedisUtil.get(cacheType.key(ks));
	}

	public String loadSync(CacheType cacheType, Object... ks) {
		return RedisUtil.getSync(cacheType.key(ks));
	}
	public void put(String value, CacheType cacheType, Object... ks) {
		RedisUtil.set(cacheType.key(ks), value);
	}
	public void put(int expire, String value, CacheType cacheType, Object... ks) {
		RedisUtil.setex(cacheType.key(ks), value, expire);
	}
}
