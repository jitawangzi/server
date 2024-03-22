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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;

public class WebSocketEchoHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

	public WebSocketEchoHandler() {
	}
    @Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {

		String req = frame.text();

		String resp = "resp ： " + req;

		ChannelFuture write = ctx.writeAndFlush(new TextWebSocketFrame(resp));
		write.addListener(f -> {
			if (f.isDone()) {
				if (f.isSuccess()) {
					System.out.println("消息发送成功");
				} else {
					System.err.println(f.cause());
				}
			} else {
				System.out.println("没有完成");
			}
		});
		ctx.flush();
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
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);
	}

}
