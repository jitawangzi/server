package cn.game.core.net.vertx.codec;

import cn.game.core.net.protocol.BaseProtocol;
import cn.game.core.net.protocol.IProtocol;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class ProtocolCodec<T> implements MessageCodec<IProtocol<T>, IProtocol<T>> {
	@Override
	public void encodeToWire(Buffer buffer, IProtocol<T> protocol) {
		// 1. 写入协议类的完整类名，用于解码时重建对象
		String className = protocol.getClass().getName();
		buffer.appendInt(className.length());
		buffer.appendString(className);

		// 2. 写入基础属性
		buffer.appendInt(protocol.getMsgID());
		buffer.appendInt(protocol.getErrorCode());

		// 3. 写入具体的序列化数据
		byte[] data = protocol.serializeData();
		if (data != null && data.length > 0) {
			buffer.appendInt(data.length);
			buffer.appendBytes(data);
		} else {
			buffer.appendInt(0);
		}
	}

	@Override
	public IProtocol<T> decodeFromWire(int pos, Buffer buffer) {

		// 读取位置指针
		int currentPos = pos;

		// 1. 读取类名长度和类名
		int strLength = buffer.getInt(currentPos);
		currentPos += 4;
		String className = buffer.getString(currentPos, currentPos + strLength);
		currentPos += strLength;

		try {
			Class<?> clazz = Class.forName(className);
			IProtocol<T> protocol = (IProtocol<T>) clazz.getDeclaredConstructor().newInstance();

			// 2. 读取基础属性
			int msgId = buffer.getInt(currentPos);
			currentPos += 4;

			int errorCode = buffer.getInt(currentPos);
			currentPos += 4;

			// 3. 读取数据并反序列化
			int dataLength = buffer.getInt(currentPos);
			currentPos += 4;

			if (dataLength > 0) {
				byte[] data = buffer.getBytes(currentPos, currentPos + dataLength);
				protocol.deserializeData(data);
			}

			// 设置基础属性
			((BaseProtocol) protocol).setMsgID(msgId);
			((BaseProtocol) protocol).setErrorCode(errorCode);

			return protocol;

		} catch (Exception e) {
			throw new RuntimeException("Failed to decode protocol", e);
		}
	}

	@Override
	public IProtocol<T> transform(IProtocol<T> protocol) {
		// 本地传递时直接返回原对象
		return protocol;
	}

	@Override
	public String name() {
		return "IProtocol";
	}

	@Override
	public byte systemCodecID() {
		return -1;
	}
}
