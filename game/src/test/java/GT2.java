import java.nio.file.Files;
import java.nio.file.Paths;

import io.vertx.core.Vertx;

public class GT2 {


	public static void main(String[] args) throws Exception {
		String dirString = "C:\\work_all\\work\\server\\protocol\\src\\main\\java\\cn\\game\\protocol\\protobuf";
		Files.list(Paths.get(dirString)).forEach(r -> {
			System.out.print(r.getFileName());
			System.out.print(",");
		});
		System.out.println();
		System.out.println(Long.MAX_VALUE);

//		System.out.println(Integer.parseInt("922746882", 16));

//		System.out.println(0x55001501);
		System.out.println(Integer.toHexString(922746882));
		

		Vertx vertx = Vertx.vertx(); 
		System.out.println("start");
		vertx.setPeriodic(3000, r -> {
            System.out.println("hello");
		});

	}
}
