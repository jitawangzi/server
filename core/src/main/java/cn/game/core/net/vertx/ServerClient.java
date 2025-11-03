package cn.game.core.net.vertx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;

import cn.game.core.base.ServerContext;
import cn.game.core.net.client.AbstractNetClient;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.serviceproxy.HelperUtils;
import io.vertx.serviceproxy.ServiceException;

/**
 * 服务器之间的连接客户端
 * 2021年4月9日 下午5:08:02
 * @author SYQ
 */
public class ServerClient extends AbstractNetClient {
	private static final Logger logger = LoggerFactory.getLogger(ServerClient.class);

	private Message<?> message;

	public ServerClient(Message<?> message) {
		this.message = message;
	}

	@Override
	public void sendProtocol(Object message) {
		String traceId = this.message.headers().get("trace-id");
		String replyAddress = this.message.replyAddress();

		DeliveryOptions deliveryOptions = traceId == null ? VxHolder.universalOptions
				: new DeliveryOptions(VxHolder.universalOptions).addHeader("trace-id", traceId);

		if (message instanceof Builder) {
			message = ((Builder) message).build();
		}

		if (ServerContext.getInstance().getRunMode().isTest()) {
			// 添加发送前日志
			logger.debug("ServerClient准备发送回复: replyAddress={}, traceId={}, messageType={}, thread={}", replyAddress, traceId,
					message.getClass().getName(), Thread.currentThread().getName());
		}

		try {
			if (message instanceof Throwable) {
				this.message.reply(new ServiceException(500, HelperUtils.generateDebugInfo((Throwable) message).toString()),
						deliveryOptions);
			} else {
				this.message.reply(message, deliveryOptions);
			}

			if (ServerContext.getInstance().getRunMode().isTest()) {
				// 发送成功后的确认
				logger.debug("ServerClient回复发送成功: replyAddress={}, traceId={}", replyAddress, traceId);

				// 验证发送状态
				VxHolder.vertx.setTimer(500, id -> {
					logger.debug("ServerClient回复发送后验证(500ms): replyAddress={}, traceId={}, 集群节点数={}", replyAddress, traceId,VxHolder.getClusterNodeCount());
				});
			}
		} catch (Exception e) {
			logger.error("回复发送失败: replyAddress={}, traceId={}, error={}", replyAddress, traceId, e.getMessage(), e);
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

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
