package cn.game.core.net.vertx;

import com.google.protobuf.MessageLite.Builder;

import cn.game.core.net.client.AbstractNetClient;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.serviceproxy.HelperUtils;
import io.vertx.serviceproxy.ServiceException;

/**
 * 服务器之间的连接客户端
 * 2021年4月9日 下午5:08:02
 * @author SYQ
 */
public class ServerClient extends AbstractNetClient {

	private Message<?> message;

	public ServerClient(Message<?> message) {
		this.message = message;
	}
	@Override
	public void sendProtocol(Object message) {
	    // 从收到的消息中获取 trace_id (如果存在)
	    String traceId = this.message.headers().get("trace_id");
	    DeliveryOptions deliveryOptions = traceId == null? VxHolder.universalOptions: new DeliveryOptions(VxHolder.universalOptions);
		if (message instanceof Builder) {
			message = ((Builder) message).build();
		}
		if (message instanceof Throwable) {
			this.message.reply(new ServiceException(500, HelperUtils.generateDebugInfo((Throwable) message).toString()),
					deliveryOptions);
		} else {
			this.message.reply(message, deliveryOptions);
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
