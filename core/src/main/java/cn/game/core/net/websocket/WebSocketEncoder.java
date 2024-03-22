/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.game.core.net.websocket;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encodes incoming buffers in a manner that makes the receiving client type
 * transparent to the encoders further up in the filter chain. If the receiving
 * client is a native client then the buffer contents are simply passed through.
 * If the receiving client is a websocket, it will encode the buffer contents in
 * to WebSocket DataFrame before passing it along the filter chain.
 * 
 * Note: you must wrap the IoBuffer you want to send around a
 * WebSocketCodecPacket instance.
 * 
 * @author DHRUV CHOPRA
 */
public class WebSocketEncoder extends ProtocolEncoderAdapter {
	private static Logger log = LoggerFactory.getLogger(WebSocketEncoder.class);

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		boolean isHandshakeResponse = message instanceof WebSocketHandShakeResponse;
		boolean isDataFramePacket = message instanceof WebSocketCodecPacket;
		boolean isRemoteWebSocket = session.containsAttribute(WebSocketUtils.SessionAttribute)
				&& (true == (Boolean) session.getAttribute(WebSocketUtils.SessionAttribute));
		IoBuffer resultBuffer;
		if (isHandshakeResponse) {
			WebSocketHandShakeResponse response = (WebSocketHandShakeResponse) message;
			resultBuffer = WebSocketEncoder.buildWSResponseBuffer(response);
		} else if (isDataFramePacket) {
			WebSocketCodecPacket packet = (WebSocketCodecPacket) message;
			resultBuffer = isRemoteWebSocket ? WebSocketEncoder.buildWSDataFrameBuffer(packet)
					: WebSocketEncoder.buildDataFrameBuffer(packet);
		} else {
			throw (new Exception("message not a websocket type"));
		}

		out.write(resultBuffer);
	}

	// Web Socket handshake response go as a plain string.
	private static IoBuffer buildWSResponseBuffer(WebSocketHandShakeResponse response) {
		IoBuffer buffer = IoBuffer.allocate(response.getResponse().getBytes().length, false);
		buffer.setAutoExpand(true);
		buffer.put(response.getResponse().getBytes());
		buffer.flip();
		return buffer;
	}

	// Encode the in buffer according to the Section 5.2. RFC 6455
	private static IoBuffer buildWSDataFrameBuffer(WebSocketCodecPacket packet) {
		// 自定义了消息id和数据包长度
		IoBuffer buffer = null, buf = packet.getPacket();
		int len = buf.limit() + 12;
		if (len <= 125) {
			buffer = IoBuffer.allocate(len + 2, false);
			buffer.put((byte) 0x82);
			buffer.put((byte) len);
		} else if (buf.limit() <= 0xFFFF) {
			buffer = IoBuffer.allocate(len + 4, false);
			buffer.put((byte) 0x82);
			buffer.put((byte) 126);
			buffer.putShort((short) len);
		} else {
			buffer = IoBuffer.allocate(len + 10, false);
			buffer.put((byte) 0x82);
			buffer.put((byte) 127);
			buffer.putLong(len);
		}
		buffer.putInt(buffer.limit());
		buffer.putInt(packet.getMsgId());
		buffer.putInt(packet.getErrorCode());
		buffer.put(buf);
		buffer.flip();
		return buffer;
	}

	// Encode the in buffer according to the Section 5.2. RFC 6455
	private static IoBuffer buildDataFrameBuffer(WebSocketCodecPacket packet) {
		// 自定义了消息id和数据包长度
		IoBuffer buf = packet.getPacket();
		int length = buf.limit() + 8;
		IoBuffer buffer = IoBuffer.allocate(length, false);
		buffer.putInt(length);
		buffer.putInt(packet.getMsgId());
		buffer.put(buf);
		buffer.flip();
		return buffer;
	}

}
