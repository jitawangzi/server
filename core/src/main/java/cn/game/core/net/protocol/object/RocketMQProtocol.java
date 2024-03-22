package cn.game.core.net.protocol.object;

import org.apache.rocketmq.common.message.Message;

public class RocketMQProtocol extends BaseObjectProtocol<Message> {


	public RocketMQProtocol(int msgID, Message data) {
		super(msgID, data);
	}
	@Override
	public int getMsgID() {
		String idString = getData().getUserProperty("MSG_ID");
		return Integer.parseInt(idString);
	}

	@Override
	public String getMsgName() {
		return null;
	}
}
