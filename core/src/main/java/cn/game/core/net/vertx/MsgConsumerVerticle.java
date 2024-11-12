package cn.game.core.net.vertx;

import cn.game.core.net.message.AbstractMessageHandlerService;
import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.protocol.bytes.ByteArrayProtocol;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.ServerType;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;

public class MsgConsumerVerticle extends AbstractMessageHandlerService {
	private Processor processor;

	public MsgConsumerVerticle(String serverId, ServerType serverType, Processor processor) {
		super(serverId, serverType);
		this.processor = processor;
	}

	@Override
	public void handleMessage(Message<Object> message) {
		Object body = message.body();
		IProtocol protocol = convertToProtocol(body);
		processor.process(new ServerClient(message), protocol);
	}

	private IProtocol convertToProtocol(Object body) {
		if (body instanceof IProtocol) {
			return (IProtocol) body;
		}
		if (body instanceof com.google.protobuf.Message) {
			int id = PbProtocol.getInstance().getMsgId(body.getClass().getSimpleName());
			return new ProtobufProtocol(id, (com.google.protobuf.Message) body);
		} else if (body instanceof Buffer) {
			Buffer buffer = (Buffer) body;
			int id = buffer.getInt(0);
			byte[] data = buffer.getBytes(4, buffer.length());
			return new ByteArrayProtocol(id, data);
		} else {
			throw new UnsupportedOperationException("Unsupported message type: " + body);
		}
	}

	@Override
	public void initConsumer(Handler<Message<Object>> handler) {
		vertx.eventBus().consumer(serverId, handler);
		if (serverType != null) {
			vertx.eventBus().consumer(serverType.name(), handler);
		}
	}
}