package cn.game.core.net.zero_rpc;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * 2020年11月27日 下午2:47:37
 * @author SYQ
 */
public class ZmqSender implements Runnable {

	private Socket sender;
	private static final Logger log = LoggerFactory.getLogger(ZmqSender.class);

//	private static ZmqSender instance = new ZmqSender();

//	private ProtocolParser pbParser;
	public static final byte[] PUB_SUB = new byte[] { 0 };
	public static final byte[] PUSH_SUB = new byte[] { 1 };
	public static final byte[] REQ_RESP = new byte[] { 2 };
	public static final byte[] FORWARD_PLAYER = new byte[] { 3 };

	ZmqSender() {
	};

	private BlockingQueue<ZMsg> queue = new LinkedBlockingDeque<ZMsg>();

//	public static ZmqSender getInstance() {
//		return instance;
//	}


	/**
	 * 发布的消息前两帧为serverId，消息类型，后面的随意
	 * @param msg
	 */
	public void pubMessage(ZMsg msg) {
		this.queue.add(msg);
	}

	public ZmqSender connect(ZContext context, String address) {
		sender = context.createSocket(ZMQ.ROUTER);
		sender.connect(address);
		return this;
	}

	public ZmqSender bind(ZContext context, String address) {
		sender = context.createSocket(ZMQ.PUB);
		sender.bind(address);
		return this;
	}

	/**
	 * 给指定server发布protobuf类型的消息
	 * @param serverId
	 * @param message
	 */
	public void pubMessage(String serverId, String myServerId, Message message) {
		ProtocolParser pbParser = SpringContextLoader.getContext().getBean(ProtocolParser.class);
		int msgId = pbParser.getMsgId(message.getClass().getSimpleName());
		pubMessage(serverId, myServerId, msgId, message.toByteArray());
	}
	/**
	 * 给指定server发布protobuf类型的消息
	 * @param recvServer
	 * @param sendServer
	 * @param msgId
	 *            消息id
	 * @param data
	 *            消息二进制数据
	 */
	public void pubMessage(String recvServer, String sendServer, int msgId, byte[] data) {
		ZMsg msg = new ZMsg();
		msg.add(StringUtils.isEmpty(recvServer) ? "all" : recvServer);
		msg.add(sendServer);
		msg.add(PUB_SUB);
		msg.add(ByteHelp.toByteArray(msgId));
		msg.add(data);
		pubMessage(msg);
	}

	@Override
	public void run() {

		ZMsg poll = null;
		while (!Thread.currentThread().isInterrupted()) {
			try {
				poll = this.queue.poll(10, TimeUnit.SECONDS);
				if (poll != null) {
					if (log.isDebugEnabled()) {
						log.debug("ZmqSender [{}] send msg [{}]", sender.getType(), poll);
					}
					poll.send(sender);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
