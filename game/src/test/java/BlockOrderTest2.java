import cn.game.util.Rnd;
import io.vertx.core.Vertx;

public class BlockOrderTest2 {

	
	public static void main(String[] args) throws Exception {
		Vertx vertx = Vertx.vertx();

		for (int i = 0; i < 10; i++) {

			vertx.executeBlocking(() -> {
				System.out.println("任务执行 : " + Thread.currentThread().getName());
				try {
					Thread.sleep(Rnd.get(500, 1000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;

			}, true);
		}

		new Thread(() -> {
			vertx.executeBlocking(() -> {
				System.out.println("任务执行1 : " + Thread.currentThread().getName());
				try {
					Thread.sleep(Rnd.get(500, 1000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}, true);
		}).start();

		Thread.currentThread().join();
	}

}
