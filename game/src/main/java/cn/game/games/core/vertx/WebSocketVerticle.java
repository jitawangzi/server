package cn.game.games.core.vertx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;

import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.protocol.manual.OldErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099;
import cn.game.util.SpringContextLoader;
import io.netty.buffer.ByteBuf;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.impl.ContextInternal;

public class WebSocketVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(WebSocketVerticle.class);
	private int port;

	public WebSocketVerticle(int port) {
		this.port = port;
	}
	@Override
	public void start() throws Exception {
		Processor processor = (Processor) SpringContextLoader.getContext().getBean("processor");

		vertx.createHttpServer().webSocketHandler(ws -> {
//			System.out.println("client connected: " + ws.textHandlerID());
//			System.out.println("client connected: " + ws.binaryHandlerID());
			ws.binaryMessageHandler(r -> {

				ContextInternal context = (ContextInternal) VxHolder.vertx.getOrCreateContext();

				ByteBuf byteBuf = r.getByteBuf();
				int length = byteBuf.readInt();
				int seq = byteBuf.readInt();
				int msgID = byteBuf.readInt();

				byte[] data = new byte[byteBuf.readableBytes()];
				byteBuf.readBytes(data);
				Message message = PbProtocol.getInstance().parseFrom(msgID, data);

				GameClient client = GameClientManager.getInstance().getGameClientByConnection(ws.binaryHandlerID());
				if (client == null) {
					client = new GameClient(ws);
					if (msgID == PbProtocol.PlayerLoginRequest_01000001) {
//						log.debug("客户端创建新session，id ：{}", client.getSessionId());
						GameClientManager.getInstance().addGameClientConnection(ws.binaryHandlerID(), client);

					} else {
						client.sendProtocol(PlayerErrorPush_01000099.getDefaultInstance(), OldErrorMsgEnum.need_login.getId());
						log.warn("session[{}]新连接，但是没有先发登录请求，msgID[{}]", ws, msgID);
						return;
					}
					client.setContext(context);
					client.setIp(ws.remoteAddress().host());
				}

				ProtobufProtocol protocol = new ProtobufProtocol(msgID, message, seq);
				client.setLastRecvPacketTime(System.currentTimeMillis());
				processor.process(client, protocol);
//				handlerState.addTotalPacketReceived();

			}).closeHandler(v -> GameClientManager.getInstance().removeGameClientConnection(ws.binaryHandlerID()));
		}).connectionHandler(r -> {
			if (log.isDebugEnabled()) {
				log.debug("websocket connection create success , remoteAddress[{}] ", r.remoteAddress());
			}
		}).listen(port).onSuccess(r -> {
			log.info("websocket listen on {} success ", port);

		}).onFailure(e -> {
			log.error("websocket start error : port  " + port, e);
			throw new RuntimeException("websocket start error");
		});
	}

}
