package cn.game.core.net.vertx;

import com.google.protobuf.MessageLite.Builder;

import cn.game.core.net.client.AbstractNetClient;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import io.vertx.core.eventbus.Message;
import io.vertx.serviceproxy.HelperUtils;
import io.vertx.serviceproxy.ServiceException;

/**
 * @Description 服务器之间的连接客户端
 * @date 2021年4月9日 下午5:08:02
 * @author SYQ
 */
public class ServerClient extends AbstractNetClient {

	private Message<?> message;

	public ServerClient(Message<?> message) {
		this.message = message;
	}
	@Override
	public void sendProtocol(Object message) {
		if (message instanceof Builder) {
			message = ((Builder) message).build();
		}
		if (message instanceof ProtobufProtocol) {
			this.message.reply(message);
		} else if (message instanceof com.google.protobuf.Message) {
			this.message.reply(message, VxHolder.protobufOptions);
		}else if (message instanceof Throwable) {
			this.message.reply(new ServiceException(500, HelperUtils.generateDebugInfo((Throwable) message).toString()),
					VxHolder.defaultOptions);
		} else {
			throw new UnsupportedOperationException();
		}
	}


	@Override
	public boolean isActive() {
		return true;
	}
	@Override
	public void sendProtocol(Object message, int errorCode) {
		this.message.fail(errorCode, "");
//		throw new UnsupportedOperationException();
	}

}
