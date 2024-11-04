package cn.game.games.core.vertx;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.TextFormat;

import cn.game.core.base.ServerContext;
import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.core.GameServerStatus;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099;
import cn.game.util.HexUtil;
import cn.game.util.SpringContextLoader;
import cn.game.util.log.LoggerType;
import io.netty.buffer.ByteBuf;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.ContextInternal;

public class WebSocketVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(WebSocketVerticle.class);
	protected Logger gamerecvLog = LoggerFactory.getLogger("gamerecvLog");

	private int port = GameServerStatus.getInstance().getServerInfo().getPort();
//	private static int count = 0;

	public WebSocketVerticle() {
	}

	@Override
	public void start() throws Exception {
		log.debug("Starting WebSocketVerticle on thread: " + Thread.currentThread().getName());
		Processor processor = (Processor) SpringContextLoader.getContext().getBean("processor");
		HttpServerOptions serverOptions = new HttpServerOptions().setReusePort(true);
		vertx.createHttpServer(serverOptions).webSocketHandler(ws -> {
//			System.out.println("client connected: " + ws.textHandlerID());
//			System.out.println("client connected: " + ws.binaryHandlerID());
			ws.binaryMessageHandler(r -> {
				try {
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
							client.sendProtocol(PlayerErrorPush_01000099.getDefaultInstance(), ErrorMsgEnum.need_login.getId());
							log.warn("session[{}]新连接，但是没有先发登录请求，msgID[{}]", ws, msgID);
							return;
						}
						client.setContext(context);
						client.setIp(ws.remoteAddress().host());
					}
					if (ServerContext.getInstance().getRunMode().isPressure() && ServerContext.getInstance().isPressureDev()) {
						client.sendMessages.putIfAbsent(seq, Pair.of(message.getClass().getSimpleName(), System.nanoTime()));
					}

					if (msgID != PbProtocol.PlayerHeartbeatRequest_01000005) {
						if (LoggerType.Net.logger.isInfoEnabled()) {
							LoggerType.Net.logger.info("opType[recv]{}msgId[{}]msgName[{}]msgValue[{}]seq[{}]", client,
									HexUtil.toHexString(msgID), message.getClass().getSimpleName(),
									message instanceof MessageOrBuilder ? TextFormat.shortDebugString((MessageOrBuilder) message) : message,
									seq);
						}
					}

					ProtobufProtocol protocol = new ProtobufProtocol(msgID, message, seq);
					client.setLastRecvPacketTime(System.currentTimeMillis());
					processor.process(client, protocol);
				} catch (Exception e) {
					log.warn("{} message parse failed ", ws.binaryHandlerID());
//					ws.close();
				}
//				handlerState.addTotalPacketReceived();

			}).textMessageHandler(r -> {
				log.error("not support ws text message " + r);
			}).closeHandler(v -> GameClientManager.getInstance().removeGameClientConnection(ws.binaryHandlerID())).exceptionHandler(r -> {
				ws.close();
				if (r instanceof java.net.SocketException && r.getMessage().contains("Connection reset")) {
					return;
				}
				log.error("ws error", r);
			});
		}).connectionHandler(r -> {
			if (log.isDebugEnabled()) {
				log.debug("websocket connection create success , remoteAddress[{}] threadName[{}] ", r.remoteAddress(),
						Thread.currentThread().getName());
			}
		}).listen(port).onSuccess(r -> {
			log.debug("websocket listen on {} success ", port);
		}).onFailure(e -> {
			log.error("websocket start error : port  " + port, e);
			ServerContext.getInstance().handleStartFail(e);
			throw new RuntimeException("websocket start error");
		});
	}

}
