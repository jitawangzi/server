import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;

public class TVxAwaitTest {

	public static void main(String[] args) throws Exception {

		Vertx vertx = Vertx.vertx();

		// 平台线程
		runOnPlatformThread();

		// 虚拟线程
		runOnVirtualThread();

		// Vert.x event loop线程
		runOnVertxEventLoopThread(vertx);

		// Vert.x worker线程
		runOnVertxWorkerThread(vertx);

		// Vert.x虚拟线程（Vert.x 5 新特性）
		runOnVertxVirtualThread(vertx);

		// 等待异步输出
		Thread.sleep(2000);

		vertx.close();
	}

	static void runOnPlatformThread() {
		System.out.println("---- Platform Thread ----");
		try {
			Future<String> future = Future.succeededFuture("hello world");
			String result = Future.await(future);
			System.out.println(Thread.currentThread() + " | result: " + result);
		} catch (Throwable t) {
			System.out.println(Thread.currentThread() + " | PlatformThread Exception: " + t.getMessage());
		}
	}

	static void runOnVirtualThread() {
		System.out.println("---- Virtual Thread ----");
		Thread.startVirtualThread(() -> {
			try {
				Future<String> future = Future.succeededFuture("hello world");
				String result = Future.await(future);
				System.out.println(Thread.currentThread() + " | result: " + result);
			} catch (Throwable t) {
				System.out.println(Thread.currentThread() + " | VirtualThread Exception: " + t.getMessage());
			}
		});
	}

	static void runOnVertxEventLoopThread(Vertx vertx) {
		System.out.println("---- Vert.x Event Loop Thread ----");
		vertx.runOnContext(v -> {
			try {
				Context ctx = Vertx.currentContext();
				System.out.println(Thread.currentThread() + " | EventLoop ThreadingModel: " + ctx.threadingModel());
				Future<String> future = Future.succeededFuture("hello world");
				String result = Future.await(future);
				System.out.println(Thread.currentThread() + " | result: " + result);
			} catch (Throwable t) {
				System.out.println(Thread.currentThread() + " | EventLoop Exception: " + t.getMessage());
			}
		});
	}

	static void runOnVertxWorkerThread(Vertx vertx) {
		System.out.println("---- Vert.x Worker Thread ----");
		vertx.executeBlocking(() -> {
			try {
				Context ctx = Vertx.currentContext();
				System.out.println(Thread.currentThread() + " | Worker ThreadingModel: " + ctx.threadingModel());
				Future<String> future = Future.succeededFuture("hello world");
				String result = Future.await(future);
				System.out.println(Thread.currentThread() + " | result: " + result);
				return result;
			} catch (Throwable t) {
				System.out.println(Thread.currentThread() + " | Worker Exception: " + t.getMessage());
			}
			return null;
		}).onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println(Thread.currentThread() + " | Worker completed successfully");
            } else {
                System.out.println(Thread.currentThread() + " | Worker failed: " + ar.cause().getMessage());
            }
		});
	}

	static class MyVerticle extends AbstractVerticle {

	}

	static void runOnVertxVirtualThread(Vertx vertx) {
		Future<String> deployVerticle = vertx.deployVerticle(new MyVerticle(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD));
//		deployVerticle.onComplete(ar -> {
//			try {
//				System.out.println("---- Vert.x Virtual  Thread ----");
//
//				System.out.println(Thread.currentThread() + " | VertxVirtualThread ThreadingModel: ");
//				Future<String> future = Future.succeededFuture("hello world");
//				String result = Future.await(future);
//				System.out.println(Thread.currentThread() + " | result: " + result);
//			} catch (Throwable t) {
//				System.out.println(Thread.currentThread() + " | VertxVirtualThread Exception: " + t.getMessage());
//			}
//		});
	}
}