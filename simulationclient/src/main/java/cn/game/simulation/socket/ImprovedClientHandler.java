package cn.game.simulation.socket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.PbProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class ImprovedClientHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

	private ByteArrayOutputStream messageBuffer;
	private int expectedLength = -1;
	private int seq = -1;
	private int id = -1;
	private int errorCode = -1;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
		if (frame instanceof BinaryWebSocketFrame) {
			handleBinaryFrame((BinaryWebSocketFrame) frame);
		} else if (frame instanceof ContinuationWebSocketFrame) {
			handleContinuationFrame((ContinuationWebSocketFrame) frame);
		}
	}

	private void handleBinaryFrame(BinaryWebSocketFrame frame) {
		ByteBuf content = frame.content();
		if (messageBuffer == null) {
			// This is the start of a new message
			expectedLength = content.readInt();
			seq = content.readInt();
			id = content.readInt();
			errorCode = content.readInt();
			messageBuffer = new ByteArrayOutputStream();
		}
		writeBufferContent(content);
		processMessageIfComplete(frame.isFinalFragment());
	}

	private void handleContinuationFrame(ContinuationWebSocketFrame frame) {
		if (messageBuffer == null) {
			throw new IllegalStateException("Received continuation frame without initial frame");
		}
		writeBufferContent(frame.content());
		processMessageIfComplete(frame.isFinalFragment());
	}

	private void writeBufferContent(ByteBuf content) {
		byte[] bytes = new byte[content.readableBytes()];
		content.readBytes(bytes);
		try {
			messageBuffer.write(bytes);
		} catch (IOException e) {
			throw new RuntimeException("Failed to write to message buffer", e);
		}
	}

	private void processMessageIfComplete(boolean isFinalFragment) {
		if (isFinalFragment) {
			byte[] completeMessage = messageBuffer.toByteArray();
			if (completeMessage.length == expectedLength - 16) { // 16 bytes for header
				try {
					Message parsedMessage = PbProtocol.getInstance().parseFrom(id, completeMessage);
					// 处理完整的消息
					handleCompleteMessage(parsedMessage, seq, errorCode);
				} catch (Exception e) {
					// 处理解析错误
				}
			} else {
				// 处理长度不匹配错误
			}
			// 重置缓冲区，为下一条消息做准备
			resetBuffer();
		}
	}

	private void resetBuffer() {
		messageBuffer = null;
		expectedLength = -1;
		seq = -1;
		id = -1;
		errorCode = -1;
	}

	private void handleCompleteMessage(Message message, int seq, int errorCode) {
		// 实现您的消息处理逻辑
	}
}
