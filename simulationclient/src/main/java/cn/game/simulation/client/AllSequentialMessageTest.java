package cn.game.simulation.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.game.simulation.util.CSVMessagesReader;
import cn.game.simulation.util.CSVMessagesReader.CSVMessage;

public class AllSequentialMessageTest extends SequentialMessageTest {

	public static void main(String args[]) throws Exception {
		AllSequentialMessageTest test = new AllSequentialMessageTest();
		test.start();
	}

	@Override
	public List<CSVMessage> getMessages() {
		List<CSVMessage> retList = new ArrayList<>();
		Map<Integer, List<CSVMessage>> groupMessageMap = CSVMessagesReader.groupMessageMap;
		groupMessageMap.forEach((k, v) -> {
			retList.addAll(v);
		});
		return retList;
	}
}
