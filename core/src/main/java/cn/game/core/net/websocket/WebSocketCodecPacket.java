/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.game.core.net.websocket;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Defines the class whose objects are understood by websocket encoder.
 * 
 * @author DHRUV CHOPRA
 */
public class WebSocketCodecPacket {
	private IoBuffer packet;
	private int msgId;
	private int errorCode;

	/*
	 * Builds an instance of WebSocketCodecPacket that simply wraps around the
	 * given IoBuffer.
	 */
	public static WebSocketCodecPacket buildPacket(IoBuffer buffer) {
		return new WebSocketCodecPacket(buffer);
	}

	/**
	 * @Description 增加了消息id和错误码
	 * @param buffer
	 * @param msgId
	 * @param errorCode
	 * @return
	 */
	public static WebSocketCodecPacket buildPacket(IoBuffer buffer, int msgId, int errorCode) {
		WebSocketCodecPacket webSocketCodecPacket = new WebSocketCodecPacket(buffer);
		webSocketCodecPacket.errorCode = errorCode;
		webSocketCodecPacket.msgId = msgId;
		return webSocketCodecPacket;
	}

	private WebSocketCodecPacket(IoBuffer buffer) {
		packet = buffer;
	}

	public IoBuffer getPacket() {
		return packet;
	}

	public int getMsgId() {
		return msgId;
	}

	public int getErrorCode() {
		return errorCode;
	}
}
