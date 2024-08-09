import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class GG2 {

	public static void main(String[] args) throws Exception {
		Vertx vertx = Vertx.vertx();
		Future<String> futureWithTimeout = Future.future(promise -> {
			long setTimer = vertx.setTimer(5000, id -> promise.fail("Operation timed out"));
			promise.future().onComplete(ar -> vertx.cancelTimer(setTimer));
			promise.complete("the result");
		});

		futureWithTimeout.onSuccess(r -> {
			System.out.println("The result is: " + r);
		}).onFailure(e -> {
			System.err.println("Error caught in onFailure: " + e.getMessage());
		});
	}


}
