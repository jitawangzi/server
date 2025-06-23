
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class InterruptChainExample extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		vertx.executeBlocking(() -> {
			// 模拟耗时操作1
				Thread.sleep(1000);
			return "Step 1 completed";
		}).compose(result -> {
			System.out.println(result);
			// 模拟条件判断，如果满足条件，则中断后续流程
			if (Math.random() < 0.9) {
				return Future.succeededFuture("Process interrupted");
			} else {
				// 模拟耗时操作2
				return vertx.executeBlocking(() -> {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return "Step 2 completed";
				});
			}
		}).compose(result -> {
			System.out.println(result);
			// 模拟耗时操作3
			return vertx.executeBlocking(() -> {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return "Step 3 completed";
			});
		}).onComplete(ar -> {
			if (ar.succeeded()) {
				String result = (String) ar.result();
				if (result.equals("Process interrupted")) {
					System.out.println("Process interrupted");
				} else {
					System.out.println("All steps completed: " + ar.result());
				}
			} else {
				System.out.println("Process failed: " + ar.cause());
			}
		});
	}

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx(); // Create a Vertx instance
        vertx.deployVerticle(new InterruptChainExample()); // Deploy the verticle
    }
}
