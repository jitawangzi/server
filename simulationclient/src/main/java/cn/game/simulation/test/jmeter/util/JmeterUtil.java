package cn.game.simulation.test.jmeter.util;

import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.core.net.socket.controller.Dispatcher;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.util.ByteHelp;
import cn.game.util.SpringContextLoader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class JmeterUtil {

	protected static Logger log = LoggerFactory.getLogger(JmeterUtil.class);

	static {
		try {
			ServerTestContext.init();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("init spring error ", e);
		}
	}
	public static byte[] toByteArray(Message msg) {

		byte[] byteArray = msg.toByteArray();
		int messageLength = byteArray.length;
		ByteBuf buffer = Unpooled.buffer(messageLength + 12);
		buffer.writeInt(messageLength + 12);
		buffer.writeInt(0);
		buffer.writeInt(PbProtocol.getInstance().getMsgId(msg.getClass().getSimpleName()));
		buffer.writeBytes(byteArray);
		return buffer.array();
	}
	public static byte[] toByteArray(com.google.protobuf.Message.Builder builder) {
		return toByteArray(builder.build());
	}
	public static String toHexString(Message msg) {

		return ByteHelp.strhex(toByteArray(msg));
	}
	public static String toHexString(com.google.protobuf.Message.Builder builder) {

		return ByteHelp.strhex(toByteArray(builder.build()));
	}

	public static Message parseMessage(byte[] data) {

		ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
		int length = byteBuf.readInt();
		int seq = byteBuf.readInt();
		int msgID = byteBuf.readInt();
		int errorCode = byteBuf.readInt();
		byte[] msgData = new byte[byteBuf.readableBytes()];
		byteBuf.readBytes(msgData);
		Message message = PbProtocol.getInstance().parseFrom(msgID, msgData);
		return message;
	}
	public static ProtobufProtocol parseProtobufMessage(byte[] data) {

		ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
		int length = byteBuf.readInt();
		int seq = byteBuf.readInt();
		int msgID = byteBuf.readInt();
		int errorCode = byteBuf.readInt();
		byte[] msgData = new byte[byteBuf.readableBytes()];
		byteBuf.readBytes(msgData);

		Message message = PbProtocol.getInstance().parseFrom(msgID, msgData);

		ProtobufProtocol protocol = new ProtobufProtocol(msgID, message);

		return protocol;
	}
	public static void processResponseMessage(NetClient client, byte[] data) {

		try {
			if (data.length == 0)
				return;
			Dispatcher bean = SpringContextLoader.getContext().getBean(Dispatcher.class);
			bean.dispatch(client, parseProtobufMessage(data));

		} catch (Exception e) {
			log.error("消息处理出现异常 : ", e);
		}
	}

	public static void getRequestMessage(NetClient client, byte[] data) {

	}
	/**
	 * 构建副本请求
	 * 
	 * @param client
	 * @return
	 */
	public static String exploreRequest(Client client) {

		// 构建请求数据
		String req = null;
//		if (client.getExplore() == null) {
//			req = new ExploreChapterEnterRequest_52000831Test().getHexStringMessage(client);
//		} else {
//			if (client.getRoom() != null) {
//				req = new ExploreDeliveryRequest_52000001Test().getHexStringMessage(client);
//			} else if (client.getFloor() != null) {
//				// 地图移动
////				req = new ExploreMoveTargetPosSyncRequest_52000003Test().getHexStringMessage(client);
//				// 放弃探索
//				req = new ExploreGiveupRequest_52000807Test().getHexStringMessage(client);
//			} else {
//				log.info("什么请求也没有发出去");
//			}
//		}
		return req;
	}

	public static void main(String[] args) {

		processResponseMessage(null, null);


	}

	public static void test() {
		JMeterVariables vars = new JMeterVariables();
		byte[] resp = new byte[] {};
		//获取玩家数据
		Client client = (Client) vars.getObject("client");

		Message message = JmeterUtil.parseMessage(resp);


	}

}
