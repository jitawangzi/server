package performance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.redisson.api.BatchResult;
import org.redisson.api.RFuture;

/*import ch.qos.logback.core.joran.spi.JoranException;
import cn.game.util.LogbackConfig;*/
import cn.game.util.RedissonUtil;

public class RedissonTest {

	public static void main(String[] args) throws Exception {
		/*
				try {
					LogbackConfig.init(true, "config/logback.xml");
				} catch (JoranException e) {
					e.printStackTrace();
				}*/
		String key = "1234";
		String value = "SDFSADFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDSF";
		for (int i = 0; i < 1000; i++) {
			RedissonUtil.set("1234", value);
		}

		int count = 100000;
		long start = System.currentTimeMillis();

//		setAsync(count, value);
//		setBatchAsync(count, value);
//		getSync(count, key);
		getAsync(count, key);
		System.out.println("花费时间: " + (System.currentTimeMillis() - start));
	}

	public static void setBatchAsync(int count, String value) throws InterruptedException {
		List<String> keys = new ArrayList<>(count);
		List<String> values = new ArrayList<>(count);

		for (int i = 0; i < count; i++) {
			keys.add(i + "");
			values.add(value);
		}
		RFuture<BatchResult<?>> futrue = RedissonUtil.setAsyncBatch2(keys, values);
		futrue.await();

	}
	public static void setAsync(int count, String value) throws InterruptedException {

		CountDownLatch latch = new CountDownLatch(count);

		for (int i = 0; i < count; i++) {
			RedissonUtil.setAsyncAndRun(i + "", value, r -> {
				if (r) {
					latch.countDown();
				}
			});
		}
		latch.await();
	}
	public static void setSync(int count, String value) {

		for (int i = 0; i < count; i++) {
			RedissonUtil.set(i + "", value);
		}
	}

	public static void getSync(int count, String key) {

		for (int i = 0; i < count; i++) {
			RedissonUtil.get(key);
		}
	}

	public static void getAsync(int count, String key) throws InterruptedException {

		CountDownLatch latch = new CountDownLatch(count);

		for (int i = 0; i < count; i++) {
			RedissonUtil.getAndRunAsyncBatch(r -> {
				latch.countDown();
			}, key);
		}
		latch.await();
	}
}
