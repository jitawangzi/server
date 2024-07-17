package cn.game.core.net.zero_rpc;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.google.protobuf.Message;

import cn.game.protocol.parser.ProtocolParser;
import cn.game.util.ByteHelp;
import cn.game.util.SpringContextLoader;

/**
 * @Description
 * 2020年9月11日 下午2:45:39
 * @author SYQ
 */
@Deprecated
public class ZeroPublisher implements Runnable {

	private Socket pusher;

	private static ZeroPublisher instance = new ZeroPublisher();

//	private ProtocolParser pbParser;

	private ZeroPublisher() {
	};

	private BlockingQueue<ZMsg> queue = new LinkedBlockingDeque<ZMsg>();

	public static ZeroPublisher getInstance() {
		return instance;
	}


	/**
	 * @Description 发布的消息前两帧为serverId，消息类型，后面的随意
	 * @param msg
	 */
	public void pubMessage(ZMsg msg) {
		this.queue.add(msg);
	}

	public void bind(ZContext context, String address) {
		pusher = context.createSocket(ZMQ.PUB);
		pusher.bind(address);
	}

	/**
	 * @Description 给指定server发布protobuf类型的消息
	 * @param serverId
	 * @param message
	 */
	public void pubMessage(String serverId, Message message) {
		ProtocolParser pbParser = SpringContextLoader.getContext().getBean(ProtocolParser.class);
		int msgId = pbParser.getMsgId(message.getClass().getSimpleName());
		ZMsg msg = new ZMsg();
		msg.add(StringUtils.isEmpty(serverId) ? "all" : serverId);
		msg.add(ZeroMQRpcClient.PUB_SUB);
		msg.add(ByteHelp.toByteArray(msgId));
		msg.add(message.toByteArray());
		ZeroPublisher.getInstance().pubMessage(msg);
	}
	/**
	 * @Description 给指定server发布protobuf类型的消息
	 * @param serverId
	 * @param data
	 */
	public void pubMessage(String serverId, int msgId, byte[] data) {
		ZMsg msg = new ZMsg();
		msg.add(StringUtils.isEmpty(serverId) ? "all" : serverId);
		msg.add(ZeroMQRpcClient.PUB_SUB);
		msg.add(ByteHelp.toByteArray(msgId));
		msg.add(data);
		ZeroPublisher.getInstance().pubMessage(msg);
	}
	/**
	 * @Description 发布protobuf类型的消息
	 * @param message
	 */
	public void pubMessage(Message message) {
		pubMessage(null, message);
	}

	@Override
	public void run() {

		while (!Thread.currentThread().isInterrupted()) {
			ZMsg poll = null;
			try {
				poll = this.queue.poll(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (poll != null) {
				poll.send(pusher);
			}
		}
	}

//	public void setPbParser(ProtocolParser pbParser) {
//		this.pbParser = pbParser;
//	}

}
