package cn.game.games.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**    
 * 检查bi需要的日志文件，里面有没有空值
 * 2024年6月29日 下午6:58:05
 * @author SYQ
 */
public class CyLogCheck {
	public static final char delimiter = 0x01;
	public static final String dir = "C:\\work_all\\work\\server\\logs\\cylog\\SYQ";

	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();
		File file = new File(dir);
		if (!file.isDirectory()) {
			throw new IllegalArgumentException("不是目录： " + dir);
		}
//		Map<String, String> nullValueLogs = new HashMap<String, String>();

		Set<String> nullValueLogs = new HashSet<String>();
		File[] listFiles = file.listFiles(); 
		for (File file2 : listFiles) {
			String name = file2.getName();
			String logName = name.substring(0, name.indexOf("."));
			if (nullValueLogs.contains(logName)) {
				continue;
			}
			if (hasNullValueReadLine(file2)) {
				nullValueLogs.add(logName);
				continue;
			}
		}
		System.err.println("有空值的log文件 ： ");
		for (String name : nullValueLogs) {
			System.out.println(name);
		}
		System.out.println("耗时： " + (System.currentTimeMillis() - start));
	}

	private static boolean hasNullValue(String line) {
		for (int i = 0; i < line.length() - 1; i++) {
			if (line.charAt(i) == delimiter && line.charAt(i + 1) == delimiter) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasNullValueReadChar(File file) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			int prevChar = -1;
			int currentChar;
			while ((currentChar = reader.read()) != -1) {
				if (prevChar == delimiter && currentChar == delimiter) {
					return true;
				}
				prevChar = currentChar;
			}
		}
		return false;
	}

	public static boolean hasNullValueReadLine(File file) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (hasNullValue(line)) {
					return true;
				}
			}
		}
		return false;
	}

}
