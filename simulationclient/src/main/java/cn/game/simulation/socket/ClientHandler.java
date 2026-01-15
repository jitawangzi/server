package cn.game.simulation.socket;

import java.text.MessageFormat;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.core.net.socket.controller.Dispatcher;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.simulation.client.Client;
import cn.game.util.HexUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * 客户端业务处理器
* 2017年4月27日 下午7:01:27
 * @author SYQ
 */
public class ClientHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {

	public static final AttributeKey<Client> NETTY_CHANNEL_KEY = AttributeKey.valueOf("client");
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);
	private static Logger recvLog = LoggerFactory.getLogger("recvLog");
	private static Logger sendLog = LoggerFactory.getLogger("sendLog");
	public static Logger netLogger = LoggerFactory.getLogger("Net");


	private Dispatcher dispatcher;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) throws Exception {

		try {
			// recvLog.info("opType[recv]Packet[{}]Message[{}]Session[{}]", new
			// Object[]
			// { message.getClass().getSimpleName(),
			// TextFormat.printToString((Message) message), session });

			Attribute<Client> attr = ctx.channel().attr(NETTY_CHANNEL_KEY);
			Client client = attr.get();
			ByteBuf buffer = msg.content();
			int len = buffer.readInt();
			int seq = buffer.readInt();
			int id = buffer.readInt();
			int errorCode = buffer.readInt();

			byte[] data = new byte[buffer.readableBytes()];
			buffer.readBytes(data);
			Message parseFrom = PbProtocol.getInstance().parseFrom(id, data);
			client.recvMessages.putIfAbsent(seq, Pair.of(parseFrom.getClass().getSimpleName(), System.nanoTime()));
			
//			System.err.println("msgId: " + Integer.toHexString(id) + " data len " + data.length);
			if (errorCode == 0) {
				ProtobufProtocol p = new ProtobufProtocol(id, parseFrom, seq);
				dispatcher.dispatch(client, p);
			}
			client.onResponse(id, seq, errorCode, parseFrom);
			String resp = MessageFormat.format("opType[recv]player[{5}]errorCode[{0}]id[{1}]name[{2}]content[{3}]seq[{4}]", errorCode,
					HexUtil.toHexString(id),
					parseFrom.getClass().getSimpleName(), TextFormat.shortDebugString(parseFrom), seq, client);
			if (id != PbProtocol.PlayerHeartbeatResponse_01000006) {
				if (errorCode > 0) {
//				System.err.println(resp);
					netLogger.info(resp);
				} else {
					netLogger.info(resp);
//				log.info(resp);
				}
			}
			client.recvCount.getAndIncrement() ; 

		} catch (Exception e) {
			log.error("ClientHandler接收数据异常", e);
		}
	}

	public void setDispatcher(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}
	

}
