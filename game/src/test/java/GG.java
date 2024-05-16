import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class GG {
	public static void main(String[] args) throws Exception {
		
		List<String> allLines = Files.readAllLines(Paths.get("d:/ErrorMsgEnum.java"), Charset.forName("utf-8"));
		for (String string : allLines) {
			string = string.replaceFirst(",", "");
			int index1 = string.indexOf("\"", 0);
			int index2 = string.indexOf("\"", index1 + 1);
			if (index1 < 0) {
				System.out.println(string);
				continue;
			}
			String string1 = string.substring(0, index1 - 1);
			String string2 = string.substring(index2 + 1, string.length());

			System.out.println(string1 + string2);
		}

	}
}
