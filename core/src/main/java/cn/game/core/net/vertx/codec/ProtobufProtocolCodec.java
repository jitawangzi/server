package cn.game.core.net.vertx.codec;

import com.google.protobuf.Message;

import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.protocol.protobuf.PbProtocol;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class ProtobufProtocolCodec implements MessageCodec<ProtobufProtocol, ProtobufProtocol> {

	@Override
	public void encodeToWire(Buffer buffer, ProtobufProtocol s) {
		int id = s.getMsgID();
		if (id == 0) {
			id = PbProtocol.getInstance().getMsgId(s.getData().getClass().getSimpleName());
		}
		buffer.appendInt(id);
		buffer.appendBytes(s.getData().toByteArray());
	}

	@Override
	public ProtobufProtocol decodeFromWire(int pos, Buffer buffer) {
		int msgId = buffer.getInt(pos);
		pos += 4;
		byte[] bytes = buffer.getBytes(pos, buffer.length());
		Message message = PbProtocol.getInstance().parseFrom(msgId, bytes);
		return new ProtobufProtocol(msgId, message);
	}

	@Override
	public ProtobufProtocol transform(ProtobufProtocol s) {
		return s;
	}

	@Override
	public String name() {
		return "ProtobufProtocol";
	}

	@Override
	public byte systemCodecID() {
		return -1;
	}

}
