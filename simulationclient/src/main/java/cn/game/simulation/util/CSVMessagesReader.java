package cn.game.simulation.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import cn.game.util.CSVUtil;
import cn.game.util.Rnd;

public class CSVMessagesReader {

	public static List<CSVMessage> messages = new ArrayList<>();
	/** 按权重找到某个排序后的组，组内按顺序请求消息 */
	public static Map<Integer, List<CSVMessage>> groupMessageMap = new java.util.HashMap<>();

	public static void main(String[] args) {
		String filePath = System.getProperty("user.dir") + "/messages" + ".csv";
		read(filePath);
	}

	/** 
	 * @param sendingGroup 正在发送的组
	 * @param msgNameSend  上一次发送的消息名
	 * @return
	 */
	public static CSVMessage randomGroupMessage(int sendingGroup, String msgNameSend) {

		List<CSVMessage> list = null; 
		if (sendingGroup > 0) {
			list = groupMessageMap.get(sendingGroup);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).msgName == msgNameSend && i != list.size() - 1) {
//					return list.get(i + 1);
					CSVMessage nextMsg = nextMessage(list, i + 1);
					if (nextMsg != null) {
						return nextMsg;
					} 
				}
			}
		}
		// 没有发过消息，随机一个组开始发送
		CSVMessage randomMessage = Rnd.randomElement(messages, r -> r.weight);
		list = groupMessageMap.get(randomMessage.group);
//		return list.get(0);
		return nextMessage(list, 0);
	}
	private static CSVMessage nextMessage(List<CSVMessage> list,int index) {
		if (index >= list.size()) {
			return null;
		}
		CSVMessage csvMessage = list.get(index);
		if (csvMessage.probability == 0 || Rnd.hitPercentage(csvMessage.probability)) {
			return csvMessage;
		} else {
			return nextMessage(list, index + 1);
		}
		
	}

	/** 
	 * 单纯按权重随机消息
	 * @return
	 */
	public static CSVMessage randomMessage() {
		CSVMessage randomMessage = Rnd.randomElement(messages, r -> r.weight);
		return randomMessage;
	}

	public static List<CSVMessage> getGroupMessages(int group) {
		return groupMessageMap.get(group);
	}

	public static void read(String filePath) {

		String[] headers = new String[] { "序号", "协议名", "协议号", "模块", "功能组", "组顺序", "权重", "描述" };

		List<List<String>> list = CSVUtil.read(filePath, headers);
		for (List<String> csvRecord : list) {

//			String serialNumber = csvRecord.get("序号");
//			String module = csvRecord.get("模块");
//			String protocol = csvRecord.get("协议");
//			String protocolNumber = csvRecord.get("协议号");
//			String weight = csvRecord.get("权重");
//			String description = csvRecord.get("描述");

			String seq = csvRecord.get(0);
			if (StringUtils.isEmpty(seq)) {
				continue;
			}
			String protocol = csvRecord.get(1);
			String protocolNumber = csvRecord.get(2);

//			String description = csvRecord.get("描述");
			CSVMessage message = new CSVMessage();
			message.msgName = protocol;
			message.msgId = Integer.parseInt(protocolNumber.substring(2), 16);
			message.group = StringUtils.isEmpty(csvRecord.get(4)) ? 0 : Integer.parseInt(csvRecord.get(4));
			message.order = StringUtils.isEmpty(csvRecord.get(5)) ? Integer.MAX_VALUE : Integer.parseInt(csvRecord.get(5));
			message.weight = StringUtils.isEmpty(csvRecord.get(6)) ? 0 : Integer.parseInt(csvRecord.get(6));
			message.probability = StringUtils.isEmpty(csvRecord.get(7)) ? 0 : Integer.parseInt(csvRecord.get(7));

			messages.add(message);
			groupMessageMap.computeIfAbsent(message.group, k -> new ArrayList<>()).add(message);
		}
		groupMessageMap.forEach((k, v) -> {
			v.sort(new Comparator<CSVMessage>() {
				@Override
				public int compare(CSVMessage o1, CSVMessage o2) {
					return o1.order - o2.order;
				}
			});
		});
	}

	public static class CSVMessage {
		public int msgId;
		public String msgName;
		public int group;
		public int order;
		public int weight;
		/** 单个协议发送概率，百分数 */
		public int probability;

		public String toString() {
			return "CSVMessage [msgId=" + msgId + ", msgName=" + msgName + ", group=" + group + ", order=" + order + ", weight=" + weight
					+ ", probability=" + probability + "]";
		}

	}
}
