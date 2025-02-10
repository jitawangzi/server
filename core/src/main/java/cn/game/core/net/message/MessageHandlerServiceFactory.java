package cn.game.core.net.message;

import cn.game.core.net.process.Processor;
import cn.game.core.net.rpc.vertx.VertxRPCService;
import cn.game.core.net.vertx.MsgConsumerVerticle;
import cn.game.util.ServerType;
import io.vertx.core.Vertx;

public class MessageHandlerServiceFactory {
	public static AbstractMessageHandlerService createService(String serverId, ServerType serverType, Vertx vertx, Object serviceImpl) {
		if (serviceImpl instanceof Processor) {
			return new MsgConsumerVerticle(serverId, serverType, (Processor) serviceImpl);
		} else {
			return new VertxRPCService<>(serviceImpl, serverId, serverType, (Processor) serviceImpl);
		}
	}
}