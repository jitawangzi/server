import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class ClarifiedFutureChainExample {

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();

		processChain(10).onComplete(ar -> {
			if (ar.succeeded()) {
				System.out.println("Final result: " + ar.result());
			} else {
				System.out.println("Process failed: " + ar.cause().getMessage());
			}
			vertx.close();
		});
	}

	private static Future<Integer> processChain(int input) {
		return step1(input).compose(result -> {
			if (result > 10) {
				System.out.println("Skipping step2 and step3");
				return Future.succeededFuture(result);
			}
			return step2(result);
		}).compose(result -> {
			if (result > 20) {
				System.out.println("Skipping step3");
				return Future.succeededFuture(result);
			}
			return step3(result);
		});
	}

	private static Future<Integer> step1(int value) {
		System.out.println("Executing step 1");
		return Future.succeededFuture(value * 2);
	}

	private static Future<Integer> step2(int value) {
		System.out.println("Executing step 2");
		return Future.succeededFuture(value * 2);
	}

	private static Future<Integer> step3(int value) {
		System.out.println("Executing step 3");
		return Future.succeededFuture(value + 5);
	}
}