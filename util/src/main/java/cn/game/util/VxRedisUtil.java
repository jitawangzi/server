package cn.game.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.redis.client.RedisAPI;

/**
 * vert.x redis客户端
 * 2020年8月26日 下午2:53:14
 * @author SYQ
 */
@Deprecated
public class VxRedisUtil extends AbstractVerticle {

	private static Logger logger = LoggerFactory.getLogger(VxRedisUtil.class);
	private static RedisAPI redis;
	private static String redisUrl;
/** 
	public static void main(String args[]) throws Exception {
		redisUrl = args[0];
		Runner.runExample(VxRedisUtil.class);
	}
	public static void setRedisUrl(String redisUrl) {
		VxRedisUtil.redisUrl = redisUrl;
	}
	@Override
	public void start() throws Exception {
//		String connectionString = "redis://:32SSDgSDFsa3dsdfgg@192.168.1.67:6379/0";
		RedisOptions options = new RedisOptions();
		options.addConnectionString(redisUrl);
		options.setMaxPoolWaiting(512);
		Redis redisClient = Redis.createClient(vertx, options);
		redis = RedisAPI.api(redisClient);
	}

	public static void set(String key, String value) {
		redis.set(Lists.newArrayList(key, value), ret -> {
			if (ret.failed()) {
				logger.error("key {} value {} 保存失败", key, value);
				logger.error("", ret.cause());
			} else {
			}
		});
	}
	public static void setR(String key, String value, Consumer<?> consumer) {
		redis.set(Lists.newArrayList(key, value), ret -> {
			if (ret.failed()) {
				logger.error("key {} value {} 保存失败", key, value);
				logger.error("", ret.cause());
			} else {
				consumer.accept(null);
			}
		});
	}
	public static void setex(String key, String value, int expire) {
		redis.setex(key, expire + "", value, ret -> {
			if (ret.failed()) {
				logger.error("key {} value {} 保存失败", key, value);
				logger.error("", ret.cause());
			}
		});
	}
	public static void get(String key, Consumer<String> consumer) {
		redis.get(key, ret -> {
			if (ret.succeeded()) {
				Response response = ret.result();
				consumer.accept(response == null ? null : response.toString());
			} else {
				logger.error("key {}  获取失败", key);
				logger.error("", ret.cause());
				consumer.accept(null);
			}
		});
	}

	public static void incr(String key, Consumer<Long> consumer) {
		redis.incr(key, ret -> {
			if (ret.succeeded()) {
				Response response = ret.result();
				consumer.accept(response.toLong());
			} else {
				logger.error("", ret.cause());
				consumer.accept(null);
			}
		});
	}
	public static Future<@Nullable Response> get(String key) {
		Future<@Nullable Response> future = redis.get(key);
		return future;
	}
	public static String getSync(String key) {

		CompletableFuture<String> future = new CompletableFuture<String>();
		String result = null;
		get(key, ret -> {
			future.complete(ret);
		});
		try {
			// 默认等待5秒
			result = future.get(Config.remoteCallTimeOut, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		}
		return result;
	}
	**/
}
