import io.vertx.core.Vertx;

public class GG2 {

	public static void main(String[] args) throws Exception {

		Vertx vertx = Vertx.vertx(); 
		vertx.setPeriodic(5000, r -> {
			System.out.println("timer fired");
        });
		System.out.println("timer started");

	}


}
