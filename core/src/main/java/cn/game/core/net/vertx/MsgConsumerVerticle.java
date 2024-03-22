package cn.game.core.net.vertx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.protocol.bytes.DefaultByteProtocol;
import cn.game.core.net.protocol.json.DefaultJsonProtocol;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.ServerType;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class MsgConsumerVerticle extends AbstractVerticle {
	private static final Logger log = LoggerFactory.getLogger(MsgConsumerVerticle.class);

	private String serverId;
	private ServerType serverType;
	private Processor processor;

	public MsgConsumerVerticle(String serverId, ServerType serverType, Processor processor) {
		this.serverId = serverId;
		this.serverType = serverType;
		this.processor = processor;
	}
	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		init();
		super.start(startPromise);
	}

	public void init() {
		Handler<Message<Object>> handler = msg -> {
			Object body = msg.body();
			IProtocol protocol = null;
			int id = 0;
			if (body instanceof com.google.protobuf.Message) {
				id = PbProtocol.getInstance().getMsgId(body.getClass().getSimpleName());
				protocol = new ProtobufProtocol(id, (com.google.protobuf.Message) body);
			} else if (body instanceof Buffer) {
				Buffer buffer = (Buffer) body;
				id = buffer.getInt(0);
				byte[] datas = buffer.getBytes(4, buffer.length());
				protocol = new DefaultByteProtocol(id, datas);
			} else if (body instanceof ProtobufProtocol) {
				protocol = (ProtobufProtocol) body;
			} else if (body instanceof JsonObject) {
				protocol = (DefaultJsonProtocol) body;
			} else {
				throw new UnsupportedOperationException("unsupported msg :  " + body);
			}
			processor.process(new ServerClient(msg), protocol);
		};
		vertx.eventBus().consumer(serverType.name(), handler);
		vertx.eventBus().consumer(serverId, handler);
		log.info("Vertx  msg consumer start ...");

	}

}
