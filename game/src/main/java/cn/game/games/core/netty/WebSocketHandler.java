/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package cn.game.games.core.netty;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;

import cn.game.games.net.client.GameClient;
import cn.game.protocol.protobuf.PbProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import cn.game.util.MBeanManager;
import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.object.ProtobufProtocol;

public class WebSocketHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {

	private final static Logger log = LoggerFactory.getLogger(WebSocketHandler.class);
	public static final AttributeKey<GameClient> NETTY_CHANNEL_KEY = AttributeKey.valueOf(GameClient.CLIENT_KEY);
	private Processor processor;
	private HandlerState handlerState;

	public WebSocketHandler(Processor processor, HandlerState handlerState2) {
		this.processor = processor;
		handlerState = handlerState2;
		MBeanManager.registerMBean(handlerState, "cn.game.games.core.netty:type=HandlerState,name=HandlerState");
	}
    @Override
	protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) throws Exception {

		ByteBuf byteBuf = frame.content();
		int length = byteBuf.readInt();
		int msgID = byteBuf.readInt();

		byte[] data = new byte[byteBuf.readableBytes()];
		byteBuf.readBytes(data);
		Attribute<GameClient> attr = ctx.channel().attr(NETTY_CHANNEL_KEY);
		GameClient client = attr.get();
		if (client == null) {
			if (msgID == PbProtocol.PlayerLoginRequest_01000001) {

//				client = new GameClient(ctx.channel());
				attr.set(client);

				InetSocketAddress isa = (InetSocketAddress) ctx.channel().remoteAddress();
				client.setIp(isa.getAddress().getHostAddress());
				log.debug("客户端创建新session，id ：{}", client.getSessionId());

			} else {
				log.warn("session[{}]新连接，但是没有先发登录请求，msgID[{}]", ctx.channel(), msgID);

				// WebSocketCodecPacket resp = WebSocketCodecPacket
				// .buildPacket(IoBuffer.wrap(PlayerNeedLoginResponse_01000041.getDefaultInstance().toByteArray()),
				// PbProtocol.PlayerNeedLoginResponse_01000041, 0);
				// session.write(resp);
				return;
			}
		}
		Message message = PbProtocol.getInstance().parseFrom(msgID, data);
		ProtobufProtocol protocol = new ProtobufProtocol(msgID, message);
		client.setLastRecvPacketTime(System.currentTimeMillis());
		processor.process(client, protocol);
		handlerState.addTotalPacketReceived();

    }

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

		if (evt instanceof IdleStateEvent) {
			ctx.close();
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		handlerState.addTotalChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		handlerState.addTotalChannelInactive();
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		handlerState.addTotalExceptionCaught();
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
		handlerState.addTotalChannelRegistered();
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);
		handlerState.addTotalChannelUnregistered();
	}

}
