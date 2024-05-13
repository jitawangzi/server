import java.nio.file.Files;
import java.nio.file.Paths;

public class GG {
	public static void main(String[] args) throws Exception {
		
		byte[] allBytes = Files.readAllBytes(Paths.get("d:/log4j2.xml"));
		System.out.println(allBytes.length);
	}
}
