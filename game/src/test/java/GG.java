import com.google.common.util.concurrent.RateLimiter;

public class GG {
	public static int x = 0;
	public static long firstTime = System.currentTimeMillis();

	public static void main(String[] args) throws Exception {
		String metafolder = System.getenv("metafolder");
		String dir = metafolder + "/../Excels";
		System.out.println(dir);
	}

	private static void test() throws InterruptedException {
		RateLimiter rateLimiter = RateLimiter.create(5);

		long start = System.currentTimeMillis();
		for (int i = 0; i < 2; i++) {
			Thread.sleep(50);
			if (!rateLimiter.tryAcquire()) {
				System.err.println("失败了" + i);
			}
		}
		System.out.println(System.currentTimeMillis() - start);
//		if (!rateLimiter.tryAcquire()) {
//			System.err.println("失败了");
//		}
	}

	private static void test2() throws InterruptedException {
		if (x++ >= 10) {
			if (System.currentTimeMillis() - firstTime < 1000) {
				System.err.println("太快了");
			} else {
				x = 0;
				firstTime = System.currentTimeMillis();
			}
		}
	}
}
