package cn.game.simulation.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;

import cn.game.simulation.test.base.ServerTest;
import cn.game.simulation.util.CSVMessagesReader;
import cn.game.simulation.util.CSVMessagesReader.CSVMessage;
import cn.game.util.SpringContextLoader;
import cn.game.util.log.LoggerManager;

/**    
 * 一般开发中模拟协议使用，发送某个消息组中的所有消息
 * 2025年3月25日 09:49:09
 * @author SYQ
 */
public class SequentialMessageTest {

	public static void main(String args[]) throws Exception {
		SequentialMessageTest test = new SequentialMessageTest();
		test.start();
	}

	public void start() throws Exception {
		LoggerManager.init();
		String filePath = System.getProperty("user.dir") + "/messages.csv";
		CSVMessagesReader.read(filePath);
//		ManagerHelper.init();

		ServerTestContext.init();
		run();
	}

	public void run() throws Exception {

		Map<String, ServerTest> beansRead = SpringContextLoader.getContext().getBeansOfType(ServerTest.class, true, true);
		Map<String, ServerTest> beansMap = new HashMap<String, ServerTest>();
		beansRead.forEach((k, v) -> {
//			beansMap.put(WordUtils.capitalize(k).substring(0, k.length() - 4), v);
			beansMap.put(k.substring(0, k.length() - 4).toLowerCase(), v);
		});
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId,
				ServerTestContext.version);

		client.loginPassportProto(ServerTestContext.loginServerUrl);
		client.loginGateway(ServerTestContext.gateServerIp, ServerTestContext.gateServerPort);

		client.waitInit();
		List<CSVMessage> messages = getMessages();
		for (CSVMessage csvMessage : messages) {

			ServerTest serverTest = beansMap.get(csvMessage.msgName.toLowerCase());
			if (serverTest == null) {
				throw new IllegalArgumentException("test message not found : " + csvMessage);
			}
			Message message = serverTest.getMessage(client);
			if (message != null) {
				client.sendProtocol(message);
				client.waitLastMessageReturn();
			}
		}

	}

	public List<CSVMessage> getMessages() {
		return CSVMessagesReader.getGroupMessages(ServerTestContext.msgGroup);
	}

}
