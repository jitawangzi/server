import io.vertx.core.Future;
import io.vertx.core.Promise;

public class GG2 {

	public static void main(String[] args) throws Exception {
		
		Promise<Object> promise = Promise.promise();
		Future<Object> future = promise.future();
		
		future.onSuccess(result -> {
			System.out.println("The result is: " + result);
			System.out.println(1 / 0);

		}).onFailure(r -> {
			System.out.println("The error is: " + r.getMessage());
			System.err.println("error occurred");
        });
		promise.fail(new RuntimeException());
	}


}
