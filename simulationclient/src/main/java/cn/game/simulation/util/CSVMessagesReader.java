package cn.game.simulation.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import cn.game.util.Rnd;

public class CSVMessagesReader {

	public static String[] messageNames;
	public static float[] messageWeights;

	public static void main(String[] args) {
		read();
	}

	public static String randomMessage() {

		int randomIndex = Rnd.randomIndex(messageWeights);
		return messageNames[randomIndex];
	}

	public static void read() {
		String filePath = System.getProperty("user.dir") + "/messages" + ".csv";

		CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader("序号", "模块", "协议", "协议号", "权重", "描述").withSkipHeaderRecord();

		List<String> namesList = new ArrayList<>();
		List<Float> weightList = new ArrayList<>();

		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8);
				CSVParser csvParser = new CSVParser(reader, csvFormat)) {
			for (CSVRecord csvRecord : csvParser) {
				String serialNumber = csvRecord.get("序号");
				String module = csvRecord.get("模块");
				String protocol = csvRecord.get("协议");
				String protocolNumber = csvRecord.get("协议号");
				String weight = csvRecord.get("权重");
				String description = csvRecord.get("描述");

				namesList.add(protocol);
				weightList.add(Float.parseFloat(weight));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		messageNames = namesList.toArray(new String[] {});
		messageWeights = new float[weightList.size()];
		for (int i = 0; i < weightList.size(); i++) {
			messageWeights[i] = weightList.get(i);
		}
	}
}
