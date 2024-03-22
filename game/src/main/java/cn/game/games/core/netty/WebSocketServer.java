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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public final class WebSocketServer {

	private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

	private boolean SSL;
	private int PORT;

	private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	private EventLoopGroup workerGroup = new NioEventLoopGroup();

	public void start() throws Exception {
		// Configure SSL.
		final SslContext sslCtx;
		if (SSL) {
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
		} else {
			sslCtx = null;
		}
//        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
//        EventLoopGroup workerGroup = new NioEventLoopGroup();

		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
		b.option(ChannelOption.SO_REUSEADDR, true).childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_KEEPALIVE,
				false)
//				.handler(new LoggingHandler(LogLevel.INFO))
//				.childHandler(initializer);
				.childHandler(new WebSocketServerInitializer(sslCtx));

		b.bind(PORT).sync();
		log.info("Netty Server listen on port : " + PORT);
	}

	public void shutdown() {

		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
		log.info("Netty Server shutdown complete ");

	}
//	public void setInitializer(WebSocketServerInitializer initializer) {
//		this.initializer = initializer;
//	}
	public void setSSL(boolean sSL) {
		SSL = sSL;
	}
	public void setPORT(int pORT) {
		PORT = pORT;
	}
}
