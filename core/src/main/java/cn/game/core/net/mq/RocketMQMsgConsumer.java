package cn.game.core.net.mq;

import java.util.List;

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.consumer.LitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;

import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.ServerType;

public class RocketMQMsgConsumer {

	private static final Logger log = LoggerFactory.getLogger(RocketMQMsgConsumer.class);
	public volatile boolean running = true;
	private Processor processor;
	/** 所在服务器id */
	private String serverId;
	private ServerType serverType;
	private String nameServerAddr;
	private String[] topics;
	private LitePullConsumer consumer;

	public RocketMQMsgConsumer(Processor processor, String serverId, ServerType serverType, String nameServerAddr, String... topics) {
		super();
		this.processor = processor;
		this.serverId = serverId;
		this.serverType = serverType;
		this.nameServerAddr = nameServerAddr;
		this.topics = topics;
	}

	public void start() throws InterruptedException, MQClientException {
		// 实例化消费者
		DefaultLitePullConsumer consumer = new DefaultLitePullConsumer(serverType.name());
		consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
		consumer.setNamesrvAddr(nameServerAddr);

		// 订阅一个或者多个Topic，以及Tag来过滤需要消费的消息,一般使用本服务器的id
		for (String topic : topics) {
			consumer.subscribe(topic, "*");
		}
		this.consumer = consumer;
		// 启动消费者实例
		this.consumer.start();

		new Thread(() -> {
			while (running) {
				try {
					List<MessageExt> messageExts = this.consumer.poll();

					for (MessageExt messageExt : messageExts) {
						String idString = messageExt.getUserProperty("MSG_ID");
						int id = Integer.parseInt(idString);
						byte[] body = messageExt.getBody();
						Message message = PbProtocol.getInstance().parseFrom(id, body);
						ProtobufProtocol protocol = new ProtobufProtocol(id, message);
						processor.process(new MqNetClient(messageExt), protocol);
					}
//					System.out.printf("%s%n", messageExts);
				} catch (Exception e) {
					log.error("", e);
				} finally {
				}
			}
		}, "MqConsumer").start();

	}

	public void shutdown() {
		this.running = false;
		this.consumer.shutdown();
	}

}
