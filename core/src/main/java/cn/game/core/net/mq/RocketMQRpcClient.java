package cn.game.core.net.mq;

import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.RequestCallback;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

import cn.game.core.net.rpc.RpcClient;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.Config;
import cn.game.util.KryoUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;

public class RocketMQRpcClient implements RpcClient {

//	private static final Logger log = LoggerFactory.getLogger(RocketMQRpcClient.class);

	private static MQProducer producer;

	private String nameServerAddr;
	private String groupName;

	public RocketMQRpcClient(String nameServerAddr, String groupName) {
		super();
		this.nameServerAddr = nameServerAddr;
		this.groupName = groupName;
	}
	public void start() throws MQClientException {
		// 实例化消息生产者Producer
		producer = new DefaultMQProducer(groupName);
		// 设置NameServer的地址
//		producer.setNamesrvAddr("192.168.1.67:9876");
		((ClientConfig) producer).setNamesrvAddr(nameServerAddr);
		// 启动Producer实例
		producer.start();
	}
	public void shutdown() {
		producer.shutdown();
	}

	public static void send(String topic, String tags, int msgId, Object body) {
		send(topic, tags, msgId, KryoUtils.serializeClassAndObject(body));
	}
	public static void send(String topic, int msgId, Object body) {
		send(topic, "", msgId, KryoUtils.serializeClassAndObject(body));
	}
	public static void send(String topic, String tags, com.google.protobuf.Message body) {
		int msgId = PbProtocol.getInstance().getMsgId(body.getClass().getSimpleName());
		send(topic, tags, msgId, body.toByteArray());
	}
	public static void send(String topic, com.google.protobuf.Message body) {
		int msgId = PbProtocol.getInstance().getMsgId(body.getClass().getSimpleName());
		send(topic, "", msgId, body.toByteArray());
	}
	private static void send(String topic, String tags, int msgId, byte[] body) {

		Message msg = new Message(topic, tags, body);
		msg.putUserProperty("MSG_ID", msgId + "");
		// 发送消息到一个Broker
		try {
			producer.send(msg, new SendCallback() {

				@Override
				public void onSuccess(SendResult sendResult) {
//				log.info("send message Success : " + sendResult);
				}
				@Override
				public void onException(Throwable e) {
					log.error("send message error ", e);
				}
			});
		} catch (MQClientException | RemotingException | InterruptedException e) {
			log.error("mq message send error", e);
		}
	}
	public static void request(String topic, String tags, int msgId, byte[] body, RequestCallback callback) {

		Message msg = new Message(topic, tags, body);
		msg.putUserProperty("MSG_ID", msgId + "");
		// 发送消息到一个Broker
		try {
			producer.request(msg, callback, Config.remoteCallTimeOutMillisecond);
		} catch (MQClientException | RemotingException | InterruptedException | MQBrokerException e) {
			log.error("mq message request error", e);
		}
	}
	public static void request(String topic, com.google.protobuf.Message message, RequestCallback callback) {

		int msgId = PbProtocol.getInstance().getMsgId(message.getClass().getSimpleName());
		Message msg = new Message(topic, "", message.toByteArray());
		msg.putUserProperty("MSG_ID", msgId + "");
		// 发送消息到一个Broker
		try {
			producer.request(msg, callback, Config.remoteCallTimeOutMillisecond);
		} catch (MQClientException | RemotingException | InterruptedException | MQBrokerException e) {
			log.error("mq message request error", e);
		}
	}
	@Override
	public <T> void send(T message) {
		// TODO Auto-generated method stub

		try {
			producer.send((Message) message, new SendCallback() {

				@Override
				public void onSuccess(SendResult sendResult) {

				}
				@Override
				public void onException(Throwable e) {
					log.error("", e);
				}
			});
		} catch (MQClientException | RemotingException | InterruptedException e) {
			e.printStackTrace();
		}

	}
	public static void send(Message message) {
		try {
			producer.send(message, new SendCallback() {

				@Override
				public void onSuccess(SendResult sendResult) {

				}
				@Override
				public void onException(Throwable e) {
					log.error("", e);
				}
			});
		} catch (MQClientException | RemotingException | InterruptedException e) {
			e.printStackTrace();
		}

	}
	@Override
	public boolean checkAllowSync() {
		return true;
	}

	@Override
	public <T> void request(String serverId, T message, Handler<AsyncResult<io.vertx.core.eventbus.Message<T>>> replyHandler) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> Future<io.vertx.core.eventbus.Message<T>> request(String serverId, T message) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void send(String serverId, T message) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void broadcast(String addr, T message) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> Future<io.vertx.core.eventbus.Message<T>> request(String addr, T message, DeliveryOptions options) {
		// TODO Auto-generated method stub
		return null;
	}
}
