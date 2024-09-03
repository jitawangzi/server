package performance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import cn.game.util.LogbackConfig;
import cn.game.util.VxRedisUtil;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class RedisVertxTest {

	public static void main(String[] args) throws Exception {

		String url = "redis://:32SSDgSDFsa3dsdfgg@192.168.1.67:6379/2";
		try {
			LogbackConfig.init(true, "config/logback.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
//		new Config().load();

		VxRedisUtil.main(new String[] { url });

		// 获取vertx基类
		VertxOptions options = new VertxOptions();
		options.setEventLoopPoolSize(64);
		Vertx vertx = Vertx.vertx(options);
		VxRedisUtil.setRedisUrl(url);
		Future<String> deployVerticle = vertx.deployVerticle(new VxRedisUtil());
		deployVerticle.onComplete(r -> {

			String key = "abc";
			String value = "SDFSADFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDSF";
			for (int i = 0; i < 1000; i++) {
				VxRedisUtil.set(key, value);
			}

			int count = 3;
			long start = System.currentTimeMillis();

			try {
//				setAsync(count, value);
				getSync(count, value);
			} catch (Exception e) {
				e.printStackTrace();
			}
//		setSync(count, value);

//		getSync(count, key);
//		getAsync(count, key);

			System.out.println("花费时间: " + (System.currentTimeMillis() - start));

		});

		Thread.currentThread().join();
	}

	public static void setAsync(int count, String value) throws InterruptedException {

		CountDownLatch latch = new CountDownLatch(count);

		for (int i = 0; i < count; i++) {
			VxRedisUtil.setR(i + "", value, r -> {
				latch.countDown();
			});
		}
		latch.await(10, TimeUnit.SECONDS);
		System.err.println(latch.getCount());
	}
	public static void setSync(int count, String value) {

		for (int i = 0; i < count; i++) {
			VxRedisUtil.set(i + "", value);
		}
	}

	public static void getSync(int count, String key) {

		for (int i = 0; i < count; i++) {
			VxRedisUtil.getSync(key);
		}
	}

//	public static void getAsync(int count, String key) throws InterruptedException {
//
//		CountDownLatch latch = new CountDownLatch(count);
//
//		for (int i = 0; i < count; i++) {
//			RedisUtil.getAndRunAsync(key, r -> {
//				latch.countDown();
//			});
//		}
//		latch.await();
//	}
}
