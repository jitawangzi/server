import java.nio.file.Files;
import java.nio.file.Paths;

public class GG2 {


	public static void main(String[] args) throws Exception {
		String dirString = "C:\\work_all\\work\\server\\protocol\\src\\main\\java\\cn\\game\\protocol\\protobuf";
		Files.list(Paths.get(dirString)).forEach(r -> {
			System.out.print(r.getFileName());
			System.out.print(",");
		});

	}


}
