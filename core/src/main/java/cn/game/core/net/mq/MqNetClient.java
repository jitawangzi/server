package cn.game.core.net.mq;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.utils.MessageUtil;
import org.apache.rocketmq.common.message.Message;

import cn.game.core.net.client.AbstractNetClient;
import cn.game.util.KryoUtils;

public class MqNetClient extends AbstractNetClient {

	private Message message;

	public MqNetClient(Message message) {
		this.message = message;
	}
	@Override
	public void sendProtocol(Object message) {
		byte[] body = KryoUtils.serializeClassAndObject(message);
		Message replyMessage = null;
		try {
			replyMessage = MessageUtil.createReplyMessage(this.message, body);
		} catch (MQClientException e) {
			e.printStackTrace();
		}
		RocketMQRpcClient.send(replyMessage);
	}

	@Override
	public boolean isActive() {
		return true;
	}
	@Override
	public void sendProtocol(Object message, int errorCode) {
		throw new UnsupportedOperationException();
	}


}
