package cn.game.core.net.vertx.codec;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.PbProtocol;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class ProtobufMessageCodec implements MessageCodec<Message, Message> {

	@Override
	public void encodeToWire(Buffer buffer, Message s) {

		int msgId = PbProtocol.getInstance().getMsgId(s.getClass().getSimpleName());
		buffer.appendInt(msgId);
		buffer.appendBytes(s.toByteArray());

	}

	@Override
	public Message decodeFromWire(int pos, Buffer buffer) {
		int msgId = buffer.getInt(pos);
		pos += 4;
		byte[] bytes = buffer.getBytes(pos, buffer.length());
		Message message = PbProtocol.getInstance().parseFrom(msgId, bytes);
		return message;
	}

	@Override
	public Message transform(Message s) {
		return s;
	}

	@Override
	public String name() {
		return "ProtobufMessage";
	}

	@Override
	public byte systemCodecID() {
		return -1;
	}

}
