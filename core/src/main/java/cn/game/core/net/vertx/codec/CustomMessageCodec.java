package cn.game.core.net.vertx.codec;

import cn.game.util.SerializationUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class CustomMessageCodec implements MessageCodec<Object, Object> {

	@Override
	public void encodeToWire(Buffer buffer, Object s) {

		buffer.appendBytes(SerializationUtil.serializeClassAndObject(s));
	}

	@Override
	public Object decodeFromWire(int pos, Buffer buffer) {
		byte[] bytes = buffer.getBytes(pos, buffer.length());
		return SerializationUtil.deserializeClassAndObject(bytes);
	}

	@Override
	public Object transform(Object s) {
		return s;
	}

	@Override
	public String name() {
		return "CustomMessage";
	}

	@Override
	public byte systemCodecID() {
		return -1;
	}

}
