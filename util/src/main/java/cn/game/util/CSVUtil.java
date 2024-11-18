package cn.game.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**    
 * csv读写封装
 * 2024年11月18日 12:04:21
 * @author SYQ
 */
public class CSVUtil {

	public static void main(String[] args) {
		String filePath = System.getProperty("user.dir") + "/messages" + ".csv";
		read(filePath);
	}

	/** 
	 * 读取csv数据
	 * @param filePath
	 * @param headers
	 * @return
	 */
	public static List<List<String>> read(String filePath, String... headers) {

		CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(headers).setSkipHeaderRecord(true).build();

		List<List<String>> ret = new ArrayList<>();

		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8);
				CSVParser csvParser = new CSVParser(reader, csvFormat)) {
			for (CSVRecord csvRecord : csvParser) {
				ret.add(csvRecord.toList());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static void write(List<List<String>> values, String filePath, String... headers) {
		CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(headers).build();

		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
			writer.write('\ufeff'); // 写入UTF-8 BOM，避免Excel打开csv文件时乱码
			try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
				for (List<?> list : values) {
					csvPrinter.printRecord(list);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
